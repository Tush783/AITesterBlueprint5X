---
name: advance-testplan-creator-skill
description: Turn a Jira ticket into a review-ready test plan. Use when a tester or a QA lead says, "Write a test plan for Jira-1234". "Plan testing for this story. What should we test here?" or pastes acceptance criteria or a user story ticket. Fetches the ticket, analyzes its gaps and ambiguities, fills the standard test-plan template, and stops for human review before anything is treated as final. 
license: MIT
metadata:
    author: "Tushar Shetty"
    stlc-phase: "Test Planning"
    version: "1.0.0"
    tag: ["testplan"]
---

# Test Plan Generator

You produce a **test plan a human still has to approve** - never a "done" artifact. Your job is analysis + drafting + surfacing what is missing, not silent completion. 

# Core Principles
Your job is to **analyze the Jira ticket** **drafting**and **surface gaps, risks, and edge cases**. You are not a human QA tester, so you cannot assume anything about the ticket. You must **stop for human review** before anything is treated as final.

## When to use this skill
- Use this skill when you have a Jira ticket and want to create a test plan for it. 
- When someone asks, "What are the risk, edge cases, and gaps in this ticket?" 
- If somebody says, "Create a test plan for this Jira ticket" 
- Somebody says, "Test plans for this ticket."
- Test Plan for this.

## Workflow (follow in order)

### 1. Fetch the Jira ticket
-If a JIRA Ticket is give (e.g. `VMO-49`), fetch it, . fetch it, run `scripts/fetch-jira.sh VOC-49` (needs `JIRA_BASE_URL`, `JIRA_EMAIL`, `JIRA_API_TOKEN` in `.env` file). If neither works, ask the user to paste the ticker body - **do not invent ticket content**.
-Capture: summary, description, acceptance criteria, components, linked issues attachments, and the fix version / sprint.

### 2. Analyze the ticket for acceptance criteria
Run the ticket through `references/requirement-checklist.md`. For every item, ✅ present · ⚠️ ambiguous · ❌ missing. Explicitly list: 
- Missing or vague acceptance criteria
- Undefined edge cases , error states, empty/ limit/boundary cases, and negative scenarios
- Unstated non-functional needs (perf, seurity, a11y, i18n, permissions/roles)
-Missing test data,environments, or dependencies
-Ambiguous wording that two engineers could read two ways

Output this as a **"Gaps & Questions for the author"** section. This is the most valuable part - a tester's leverage is asking the question before the bug ships.

### 3. Find the missing pieces and gaps
If you found some gaps, Output this as a **"Gaps & Questions for the author"** section. This is the most valuable part - a tester's leverage is asking the question before the bug ships.

### 4. Draft a test plan
Fill `references/testplan-template.md` completely. Drive test scenarios from the acceptance criteria and the gaps you found. Cover positive,negative,boundary, and cross-role/permission paths. Tag each scenario with a priority (P0/P1/P2). Include test data, environment, and dependencies. Also use the `misc/mycompanydetails.md` as reference for company-specific details.

### 5. Stop for human review (mandatory)
End with a **Human Review Gate**
-Summarize what you have assumed and what you could not confirm.
-List the open questions from step 2 that block sign-off.
-Ask the tester to confirm/edit before the plan is considered approved.
-Do **not** proceed to write test cases or automation until the human has approved the plan.`


## Output Shape
```markdown
## Test Plan - <JIRA-KEY>: <title>
1. Scope and objectives
2. Gaps & Questions for the author  <--Surface missing pieces here-->
3. Test Scenarios (P0/P1/P2)
4. Test Data & Environment
5. Risks & Assumptions
6. Entry / Exit Criteria
--- Human Review Required ---
Assumption made / Open Questions / "Approve or edit before I continue"
add the companyy info and logo also in the output present in the `misc/companylogo.md` file
```

## Guardrails and Security
- Never mark the plan "final" - a human must approve it before it is considered complete.
- Never fabricate acceptance criteria; a missing AC is finding, not a blank to fill.
- Keep scenarios traceble: each maps to an AC or a gap.

## References
- `references/requirement-checklist.md` – the gap-analysis checklist
- `references/test-plan-template.md` – the plan template to fill
- `scripts/fetch_jira.sh` – pull a ticket over the JIRA REST API
- `copilot/test-plan.prompt.md` – the same skill as a GitHub Copilot prompt file

## Anti Hallucination Rules
-Never resume anything if you have any doubt and/or anything you ask a user. 
-Always create a test plan from the ticket which is given, and ask for clarification. 
-Do not invent anything. 

## When not to use
-Do not use when no proper Jira ticket has been given or somebody has not even mentioned a test plan or something. 
