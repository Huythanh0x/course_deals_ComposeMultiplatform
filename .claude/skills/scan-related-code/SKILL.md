---
name: scan-related-code
description: Find the files and modules relevant to a ticket's requirements and record them into its workspace, before any implementation starts. Use after read-requirements, standalone or as part of ticket-flow.
---

# Scan related code

1. Read the ticket workspace's Requirements section — it must already exist and be
   filled in (run `read-requirements` first if not).
2. Narrow scope fast using `CLAUDE.md`'s module map — most requirements touch one or
   two modules, not the whole repo. Use an Explore agent when the scope is genuinely
   uncertain or spans multiple areas.
3. Record findings in the workspace's Code scan section as a short list — file path +
   one-line "why this matters", not a full exploration transcript:
   ```markdown
   - `feature/detail/.../CouponDetailViewModel.kt` — existing UiState this issue extends
   - `data:coupons/.../CouponRepository.kt` — where the new field needs to come from
   ```
4. Note any existing pattern that should be matched (e.g. "every ViewModel here uses
   `MutableStateFlow`/`asStateFlow()`, see `CLAUDE.md`") rather than re-deriving
   conventions from scratch in the implementation step.
5. Append one line to Progress log: `<date> scanned code, N files found relevant`.

## Output

Report the code-scan list — this is what `confirm-edge-cases` and `implement-change`
build on next.
