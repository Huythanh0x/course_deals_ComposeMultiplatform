# CI/CD & Deployment

## Environments

Start minimal: **local** (your dev machine, via the `local` product flavor pointed at your
dev server) → **production** (the `prod` flavor, pointed at `coupons-api.thanh0x.com`).
Don't add a `staging` build config preemptively — it's real ongoing overhead that only
pays for itself once there's a concrete reason to need a pre-prod check step.

## CI pipeline (runs on every PR, see `.github/workflows/ci.yml`)

Minimum gate, in order, all required to pass before merge is allowed:

1. **Detekt** — static analysis, catches style/consistency issues fast, before spending
   time on the rest. Config lives at `config/detekt/detekt.yml`.
2. **Build** — `./gradlew build` (assembles the `prod` flavor and every module,
   confirming the whole multi-module graph actually compiles, not just `:app`).
3. **Unit tests** — run as part of `./gradlew build`'s `check` task. Add real coverage as
   features land; the repo currently ships only the Android Studio boilerplate test.

Configure these as **required status checks** in the repo's branch protection settings
for `main` — this makes the gate structural, not a habit you can forget under time
pressure. See [`scripts/gh-setup-branch-protection.sh`](../scripts/gh-setup-branch-protection.sh).

## CD pipeline (runs on merge to `main`, or on tagging a release)

- CI produces a signed release build artifact (`prod` flavor, `release` build type).
- Publishing to a Play Store track (internal/beta/production) is a **separate,
  deliberate, manual step** — never fully automate store publishing for a personal
  project; you want a human decision point before something goes out to real users,
  however few. (This app also can't currently ship to Play in the first place — see
  the README — so today "CD" mainly means "produce and archive a signed artifact for
  manual sideloading/testing," not an actual store release.)

## Secrets

Store the release signing key and any API keys as GitHub Actions **encrypted secrets**
(repo or environment-level), never committed to the repo, never hardcoded in a workflow
file even "temporarily." Rotate a secret immediately if it's ever accidentally exposed in
a log or committed file — don't assume a quick `git revert` is sufficient, since the
value already existed in the exposed commit's history.

## Rollback plan

Decide this *before* you need it, not while something's actively broken:

- Don't promote a new build past internal/manual testing until you're confident — the
  "rollback" for a distributed APK is really "never let it reach real users in a broken
  state in the first place," since you can't un-push an update that's already installed.
- If a bad build is ever pushed to a Play track, halt the rollout and re-release the last
  known-good `versionCode` immediately rather than trying to patch forward under pressure.

## Release checklist

- [ ] All linked issues for this version closed
- [ ] `CHANGELOG.md` updated
- [ ] `versionCode`/`versionName` bumped, tagged `vX.Y.Z`
- [ ] CI green on `main`
- [ ] Installed and manually smoke-tested on a device/emulator
- [ ] ADRs written for any real decisions made this cycle
