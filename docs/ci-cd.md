# CI/CD & Deployment

## Environments

Start minimal: **local** (your dev machine, via the `local` product flavor pointed at your
dev server) → **production** (the `prod` flavor, pointed at `coupons-api.thanh0x.com`).
Don't add a `staging` build config preemptively — it's real ongoing overhead that only
pays for itself once there's a concrete reason to need a pre-prod check step.

## CI pipeline (runs on every PR, see `.github/workflows/ci.yml`)

Minimum gate, in order, all required to pass before merge is allowed:

1. **Detekt** — static analysis across all 17 modules (`./gradlew detekt --continue`), catches style/consistency issues fast. Config lives at `config/detekt/detekt.yml`.
2. **Build & Unit Tests** — `./gradlew build --continue` (assembles Android `local`/`prod` flavors, Desktop JVM, and runs unit tests across KMP modules).
3. **iOS Framework Link Gate (`macos-latest`)** — `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` verifies that `commonMain` and `iosMain` compile and link cleanly into `ComposeApp.framework`.

Configure these as **required status checks** in the repo's branch protection settings
for `main` — this makes the gate structural, not a habit you can forget under time
pressure. See [`scripts/gh-setup-branch-protection.sh`](../scripts/gh-setup-branch-protection.sh).

## CD pipeline (runs on tagging a release `v*.*.*` or manual dispatch, see `.github/workflows/release-cd.yml`)

- **Automated Multi-Platform Release Pipeline (`.github/workflows/release-cd.yml` & `scripts/release-build.sh`)**:
  - Triggered on pushing a `vX.Y.Z` tag or via `workflow_dispatch`.
  - **Android**: Builds R8-minified, resource-shrunk production APK (`:app:assembleProdRelease` → `CourseDeals-<ver>-android.apk`).
  - **iOS**: Links release `ComposeApp.framework` (`:shared:linkReleaseFrameworkIosArm64`) and archives `iosApp.xcodeproj` into `CourseDeals-<ver>-ios.ipa` / `.xcarchive.zip`.
  - **Desktop (Local / Script)**: `bash scripts/release-build.sh --version <ver> --platform all` also packages ProGuard-minified Compose Desktop `.dmg`/`.app` and UberJAR artifacts.
  - Computes `SHA256SUMS.txt` and publishes all platform binaries to **GitHub Releases**.
- Publishing to store tracks (Google Play / App Store Connect) remains a **separate, deliberate step** with a human decision point.

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
