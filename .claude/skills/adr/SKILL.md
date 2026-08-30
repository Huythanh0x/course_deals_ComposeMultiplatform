---
name: adr
description: Draft a new Architecture Decision Record under docs/adr/ following this repo's adr-guide.md template and naming convention. Use when a real architectural tradeoff has been decided or needs recording (not for trivial/reversible choices) — e.g. choosing a testing stack, a networking library, or planning the KMP migration.
---

# ADR

Drafts a new ADR matching `docs/adr-guide.md` — read that file first, it defines what
actually qualifies (a real tradeoff with consequences, not a trivial or easily-reversed
choice).

## Steps

1. **Check it's ADR-worthy** per `docs/adr-guide.md` — if the decision is trivial or
   easily reversed, it doesn't need one; say so instead of drafting one anyway.
2. **Find the next number**: list `docs/adr/*.md`, take the highest `NNNN-` prefix, add
   one, zero-pad to 4 digits (the existing one is `0001-kotlin-multiplatform-over-flutter.md`).
3. **Write `docs/adr/NNNN-short-title.md`** using the template from
   `docs/adr-guide.md`:
   ```markdown
   # NNNN: <short title>

   ## Status
   Proposed

   ## Context
   <the problem/situation forcing a decision, and constraints>

   ## Decision
   <what was decided, stated plainly>

   ## Consequences
   <tradeoffs accepted, including negative ones — don't only list upsides>
   ```
4. Status starts as `Proposed` unless the user says the decision is already final (then
   `Accepted`, matching how `0001` was recorded).
5. If this ADR touches something covered elsewhere in `CLAUDE.md` (e.g. the testing
   stack, or KMP migration status), flag that `CLAUDE.md` may need a matching update —
   don't update it silently without asking, since it's read every session.

## Notes

- One ADR = one decision. If the user describes two decisions, that's two files.
- Reference `docs/adr/0001-kotlin-multiplatform-over-flutter.md` as the style example —
  it's short, states the real tradeoff (Web target maturity risk) honestly, and is
  explicit about what's *not* changing (existing native UI stays as-is).
