#!/usr/bin/env bash
#
# Fetch a Jira ticket as JSON from the Jira Cloud REST API.
#
# Usage:
#   ./fetch-jira.sh <JIRA-KEY>
#
# Required environment variables:
#   JIRA_BASE_URL   e.g. https://your-company.atlassian.net
#   JIRA_EMAIL      e.g. your-email@company.com
#   JIRA_API_TOKEN  API token from https://id.atlassian.com/manage-profile/security/api-tokens
#
# The ticket JSON goes to stdout and errors go to stderr. Exits non-zero on failure.

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $(basename "$0") <JIRA-KEY>" >&2
  exit 2
fi

jira_key="$1"

if [[ ! "$jira_key" =~ ^[A-Za-z][A-Za-z0-9_]*-[0-9]+$ ]]; then
  echo "Error: '$jira_key' is not a valid Jira key (expected something like VWO-1234)." >&2
  exit 2
fi

missing=0
for var in JIRA_BASE_URL JIRA_EMAIL JIRA_API_TOKEN; do
  if [[ -z "${!var:-}" ]]; then
    echo "Error: environment variable $var is not set." >&2
    missing=1
  fi
done
if [[ "$missing" -ne 0 ]]; then
  exit 2
fi

response=$(curl --silent --show-error --request GET \
  --url "${JIRA_BASE_URL%/}/rest/api/3/issue/${jira_key}" \
  --user "${JIRA_EMAIL}:${JIRA_API_TOKEN}" \
  --header 'Accept: application/json' \
  --write-out '\n%{http_code}')

http_code="${response##*$'\n'}"
body="${response%$'\n'*}"

if [[ "$http_code" != "200" ]]; then
  echo "Error: Jira returned HTTP $http_code for $jira_key." >&2
  echo "$body" >&2
  exit 1
fi

echo "$body"
