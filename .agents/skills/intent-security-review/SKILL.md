---
name: intent-security-review
description: Audit AndroidManifest.xml exported components and Intent-extras handling code for security issues, adapted from Google's official android-intent-security guidance. Use when reviewing exported activities/services/receivers, code that reads getIntent()/intent extras, or code that forwards/relays a received Intent to launch another component.
---

# Intent security review

Adapted from Google's official `android/skills` `android-intent-security` skill, scoped
to what this repo actually has. Current known state (re-verify, don't trust this as
still accurate) — `app/src/main/AndroidManifest.xml` exports three activities:
`SplashActivity` (has the `MAIN`/`LAUNCHER` intent-filter, expected/required),
`LoginActivity`, `CouponDetailActivity` (both `exported="true"`, no intent-filter shown
at last check — meaning any app on the device can still launch them explicitly by
component name).

## What to check

1. **Every `exported="true"` component in `AndroidManifest.xml`** — is `exported="true"`
   actually needed? If nothing outside this app needs to launch it directly, it
   shouldn't be exported. If it must be exported, does it declare a `android:permission`
   (signature-level, if only this app's own other processes should reach it)?
2. **Any code reading `getIntent()`/`intent.extras`/`getStringExtra`/
   `getParcelableExtra` inside an exported component** — never trust these as if they
   can only originate from inside this app. Validate/sanitize before acting on them
   (e.g. don't navigate to an arbitrary destination or execute a privileged action
   purely from an unvalidated extra).
3. **Any code that constructs an `Intent` from data in a received `Intent` and launches
   it** (intent redirection/forwarding) — this is the highest-severity pattern. Before
   launching, either:
   - Use `androidx.core.content.IntentSanitizer` (`androidx.core:core:1.9.0+`, already
     satisfied by this repo's AndroidX versions) to explicitly allowlist components,
     actions, data, and extras, or
   - Verify the target component's package matches this app's own package and that the
     target component is itself exported, before launching it.
   - Never launch a nested/forwarded Intent directly without one of the above.
4. **`PendingIntent` construction** — default to `PendingIntent.FLAG_IMMUTABLE`. Only
   use `FLAG_MUTABLE` for a specific feature that genuinely needs it (inline
   notification replies, etc.), and note why in a comment when you do.
5. **Custom `BroadcastReceiver`s**, if any exist or get added — protect with a
   signature-level permission, or `RECEIVER_NOT_EXPORTED` for a receiver registered
   dynamically at runtime, so arbitrary apps can't trigger it.

## Report, don't silently fix

This skill diagnoses. If it finds a real issue (e.g. an exported activity reading an
untrusted extra without validation), report it clearly with the file/line — treat a
confirmed intent-redirection or unauthorized-launch vulnerability as security-relevant
enough to flag explicitly to the user rather than quietly patching mid-task on unrelated
work, per the security-review conventions this session otherwise follows.
