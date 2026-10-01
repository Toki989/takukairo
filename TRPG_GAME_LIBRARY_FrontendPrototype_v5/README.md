# TRPG GAME LIBRARY - Frontend Prototype v5

## Open

Open `index.html` in a browser.

This is a static frontend prototype using mock data. It is aligned to the 2026-09-28 integrated specification as far as a static prototype can represent it.

## Main routes available in the prototype

- Home / Scenario Curated Gallery
- Scenario View / Session Archive
- Desktop Table Detail based on PC Focus v5.9
- PL / PC Collection and PC Focus
- Scenario create/edit
- Table create/edit
- PC create/edit
- PL-change interaction mock
- Person-name-edit interaction mock
- Import INPUT / REVIEW / DETAIL / REGISTER / COMPLETE
- Shared dialog / toast / disabled / empty-state patterns

## Important

- Desktop Table Detail keeps the v5.9 visual direction, but mock-only v5.9 fields are not promoted to formal data.
- SAN / HP / MP are represented as Participation-scoped EndPcState history, not current PC attributes.
- KP and PL are represented as Participation roles in the mock normalization layer.
- Home summary and PL/KP filters are SelfPerson-based.
- Unconfirmed implementation values are not treated as formal specification.

See `QA_REPORT_v5.md` for the audit, fixes, and QA limitation in this execution environment.
