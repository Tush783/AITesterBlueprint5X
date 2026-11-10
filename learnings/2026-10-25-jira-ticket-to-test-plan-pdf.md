# Jira ticket to review-ready test plan PDF

**Problem:** Turn Jira ticket VWO-49 into a formal test plan PDF (company logo, gap analysis, scenarios) using the `testplan-creator-skill_a` skill, on a machine without reportlab or PIL.

## Approach
1. **Read the whole skill folder first** (SKILL.md, references/, misc/, scripts/). File names in SKILL.md had drifted from disk (`fetch_jira.sh` vs `fetch-jira.sh`, `mycompanydetails.md` path), so trust `ls` over the prose.
2. **Fetch with the skill's own script.** Load `.env` with `set -a; source .env; set +a`. Print only the variable names from `.env`, never the values.
3. **Flatten the description before reading it.** Jira REST v3 returns the description as ADF (nested JSON). A 40-line Python walker (paragraph, list, table, hardBreak) turns it into readable text. Analyze the text, not the JSON.
4. **Score every checklist row** (Present / Ambiguous / Missing), with evidence from the ticket and a pointer to a gap ID. Every Ambiguous or Missing row becomes a question for the author. Mark which questions block execution.
5. **Write the plan as data, not prose.** Keep it in a Python generator (lists of AC, gaps, scenarios, risks) and add asserts: every scenario maps to a known AC or gap, every AC has at least one scenario, every ticket-listed scenario is covered, the blocking gaps match the entry criteria, and there are no em dashes. Counts and the traceability matrix are computed, never hand-typed.
6. **Render HTML to PDF with headless Chrome:** `--headless=new --no-pdf-header-footer --print-to-pdf=out.pdf file://...`. CSS `@page { @bottom-right { content: "Page " counter(page) " of " counter(pages) } }` works in current Chrome. Embed the logo as a base64 data URI (check the real type with `file`: `company.png` was actually WebP).
7. **Visual QA:** `pdftoppm -r 70 -png` and look at every page. This found IDs wrapping at the hyphen (`G-` / `01`), fixed by wrapping IDs in `white-space: nowrap` spans, and a two-column block splitting across pages, fixed with `break-inside: avoid`.

## Judgment calls (what was NOT done)
- **No invented content.** Resources, timelines and sprint are not on the ticket, so they are marked TBD with the roles needed, not filled with plausible names or dates.
- **Ticket defects are findings, not things to fix silently.** The priority field said Medium while the body said High, the epic was text only, and leftover AI-chat text was pasted into the description. Each became a gap row.
- **No silent correction of source facts.** The company file says "Bingify" (probably Wingify). The PDF names the product (VWO) and the mismatch is flagged to the user.
- **No pip installs.** reportlab was missing, and Chrome was already installed and renders tables and CSS better.
- **No test cases or automation.** The skill's human review gate forbids them until a QA owner approves.

## Reusable rule
When a document must stay traceable (requirements to tests), store it as data with integrity asserts and render it, then check the rendered pages by eye. Asserts catch broken mappings and screenshots catch broken layout, and neither catches the other's failures.
