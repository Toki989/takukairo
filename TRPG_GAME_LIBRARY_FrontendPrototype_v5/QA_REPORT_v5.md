# TRPG GAME LIBRARY Frontend Prototype v5 - QA Report

## Positioning

This v5 is a reconstruction of v4 based on the 2026-09-28 integrated source of truth, the latest Visual Design documents, and the v5.9 Desktop Table Detail basis.

It does not add new Entity / CRUD / business rules. Unconfirmed items remain prototype adjustments only.

## High-priority fixes applied

- Home Gallery grid item bug fixed: grid span is now applied to the actual `article` grid item.
- Favorite state is separated from Scenario mock data into a User x Scenario style set.
- Home summary now uses SelfPerson-based activity instead of total registered data.
- Home PL/KP filters now mean "this logged-in user participated as PL/KP".
- KP mock records are normalized to Participation-like records with Role=KP.
- New Table no longer clones Participation / EndPcState from an existing Table.
- New Table no longer auto-fills today's date.
- New Table no longer auto-confirms KP.
- `+ PL` now creates a draft PL Participation; `remove PL` removes the current draft tab.
- Table form HO / quote layout collapses to one column on mobile.
- Mobile 4-column PC Collection no longer hides current PL.
- PC Collection now includes "recently used" sorting.
- PC-image-missing state is represented as a neutral PC placeholder in the collection/focus prototype.
- Desktop Table Detail uses PL Participation only in the selector; KP remains header context.
- Selector offsets now follow distance from the active Participation rather than fixed item index.
- Selector uses roving tabindex and moves keyboard focus when Arrow/Home/End changes selection.
- Spotlight state is normalized when the selected Participation only has HO or only has quote.
- Spotlight visible text now has an accessible semantic equivalent.
- Empty EndPcState values are omitted instead of fabricating `—` values.
- Scenario name in Desktop Table Detail is an external link only when Scenario URL exists.
- 3+ TableDate header display exposes all dates through a supplemental title in this prototype.
- Recording action remains available in Table-level empty / PC-none states when recording URL exists.
- Search re-render restores input focus/caret.
- Labels are programmatically associated with form controls after render.
- Dialogs now support Escape, focus trapping, and focus return.
- Import register now ends at an Import Complete view instead of silently returning to INPUT.
- Scenario/PC danger actions and image actions now have prototype-safe interaction stubs.
- PC Focus PL-change and Person-name-edit are no longer explanation-only alerts; each opens a dedicated interaction mock with impact context.

## Static QA completed

- `node --check app.js`: pass.
- `data-action` values were compared with action handlers: no unhandled actions remain.
- JavaScript brace balance: pass.
- CSS brace balance: pass.
- Legacy new-Table cloning (`tables[0].parts`): removed.
- Hard-coded new-Table date (`2026-09-28`): removed.
- Mobile PC 4-column rule that hid PL: removed/overridden so PL remains visible.
- Existing `prefers-reduced-motion` handling remains present.

## Browser render QA limitation in this environment

Automated render QA was attempted with system Chromium and Playwright at both `file://` and local `http://127.0.0.1` URLs. The execution environment blocked both navigation routes with `ERR_BLOCKED_BY_ADMINISTRATOR`, so new v5 screenshots could not be truthfully generated here.

Therefore this package does **not** claim successful rendered QA for the new build. Before final production use, open `index.html` in a normal browser and verify at minimum:

- 1366x768
- 1440x900
- 1672x941
- 1680x800
- 1920x1080
- 390x844
- Desktop Table Detail with 1 / 2 / 3 / 4 / 5 / 6 / 7+ PL Participations
- PC image present / PC image missing / PC missing Participation
- Participation count 0
- active selector at left edge / right edge
- keyboard ArrowLeft / ArrowRight / Home / End
- Dialog Tab / Shift+Tab / Escape / focus return
- mobile Table form horizontal overflow

## Still intentionally prototype-level / not newly specified

The following remain intentionally unresolved or mock-level where the source of truth keeps them unconfirmed or where backend/storage is outside this static prototype:

- exact Home Gallery ratio-classification algorithm and final spacing values
- exact breakpoints / motion timings / focus-ring dimensions
- final PC-none / PC-image-placeholder visual treatment
- final HO/quote switch visual
- actual BOOTH network acquisition implementation
- actual image upload/storage/crop persistence
- backend ownership authorization
- actual Scenario/Table/PC persistence
- Import parser, ImportSession storage, transaction behavior and retention jobs
- exact account/login latest Visual Design

