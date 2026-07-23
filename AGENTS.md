# TV — FongMi Android TV/Phone Video Player

## Repository Purpose

An open-source Android video player app based on [CatVod](https://github.com/CatVodTVOfficial/CatVodTVJarLoader), supporting both **Android TV** (`leanback` flavor) and **phone** (`mobile` flavor) via product flavors. Content is extended through external JSON configuration (vod/live sources, spiders).

**License**: GPLv3 for app source, but the repo is **not fully open-source** — several engine and player binaries ship without corresponding sources (see [Open Source Status](#open-source-status)).

**Remote**: `https://github.com/861052359/fongmi.git`  
**Default branch (this fork)**: `main` (not `fongmi`)

---

## Handoff — Current Status (2026-07-23)

> Read this section first in a new session.

### What works

| Item | Status |
|------|--------|
| App / catvod / quickjs / chaquo Java sources | Present |
| Engine AARs in `app/libs/` (`thunder`, `jianpian`, `forcetech`, `tvbus`, `hook`) | Present & tracked |
| Native `.so` under engine dirs | Present |
| Gradle wrapper + `gradle.properties` | Tracked (needed for CI) |
| GitHub Actions workflow | Exists, runs on `main` |
| CI: JDK 21, Android SDK, Python 3.10, auto keystore, `local.properties` | Working |
| CI: Chaquopy Python deps install | Working |

### What is blocking the build

**Missing custom Media3 AAR files: `app/libs/lib-*.aar`**

- Code depends on `androidx.media3.*` via local AARs:
  ```groovy
  // app/build.gradle
  implementation fileTree(dir: "libs", include: ["*.aar"])
  ```
- **Not** pure Maven Media3 — custom / forked APIs are used, including:
  - `androidx.media3.mpvplayer.*`
  - `androidx.media3.ui.danmaku.*`
  - `androidx.media3.common.MediaChapter` / `MediaEdition`
  - `androidx.media3.ui.PlayerSeekView`
- These files are **gitignored**:
  - Root `.gitignore`: `lib-*.aar`
  - `app/libs/.gitignore`: `lib-*.aar`
- Disk and Git currently have **no** `lib-*.aar` → compile fails with:
  ```text
  package androidx.media3.common does not exist
  package androidx.media3.session does not exist
  ```

### Next steps for a new session

1. Obtain custom Media3 `lib-*.aar` from a machine that can already build, or from the original author / private channel.
2. Place them under `app/libs/`.
3. Either:
   - Commit them (and relax `lib-*.aar` ignore rules), **or**
   - Download them in CI from private storage / a Release.
4. Re-run: `./gradlew assembleMobileArm64_v8aDebug` (or push to trigger Actions).
5. Do **not** assume adding public `androidx.media3:*` Maven deps alone is enough — custom packages will still fail.

### CI progress so far (failures fixed)

| Failure | Fix applied |
|---------|-------------|
| `.github/` ignored by gitignore | Narrowed ignore so workflows can be tracked |
| Manual `sdkmanager` NDK version missing | Removed manual SDK install; use runner preinstall |
| `gradle-wrapper.properties` / `gradle.properties` ignored by `*.properties` | Added `!gradle.properties` and `!gradle/wrapper/gradle-wrapper.properties` |
| Chaquopy: `Couldn't find Python 3.10` | Added `actions/setup-python@v5` with `python-version: '3.10'` |
| Missing `androidx.media3.*` | **Not fixed** — needs `lib-*.aar` |

### Useful commands (this machine)

```bash
# gh is installed but often not on PATH:
"/c/Program Files/GitHub CLI/gh.exe" run list --repo 861052359/fongmi --limit 5
"/c/Program Files/GitHub CLI/gh.exe" run view <run-id> --repo 861052359/fongmi --log-failed
```

Latest CI target: `assembleMobileArm64_v8aDebug` → artifact `mobile-arm64_v8a`.

---

## Open Source Status

| Component | Open? |
|-----------|--------|
| App / catvod / quickjs / chaquo Java sources | Yes (GPLv3) |
| Engine AARs (`thunder`, `jianpian`, `forcetech`, `tvbus`, `hook`) | Binary only |
| Engine native `.so` (jianpian / zlive / forcetech) | Binary only |
| Custom Media3 (`lib-*.aar`) | Binary; **not in repo** (gitignored) |

---

## Quick Start & Build

```bash
# Preferred CI target (phone ARM64 debug)
./gradlew assembleMobileArm64_v8aDebug

# Other examples
./gradlew assembleDebug
./gradlew assembleLeanbackRelease
./gradlew assembleMobileRelease
# Release APKs are copied to Release/apk/ after assemble*Release
```

### Toolchain

| Item | Value |
|------|--------|
| Java | 21 |
| Gradle | 9.6.1 (wrapper) |
| AGP | 9.2.1 |
| minSdk / targetSdk / compileSdk | 24 / 37 / 37 |
| ABI | `arm64-v8a`, `armeabi-v7a` |
| Chaquopy BuildPython | **3.10** (`chaquo/build.gradle`) |

### Required local files (not in Git)

Create `local.properties` at repo root (required even for **debug** — `app/build.gradle` always loads it):

```properties
sdk.dir=C\:\\Users\\YOU\\AppData\\Local\\Android\\Sdk
storeFile=../release.keystore
keyAlias=your_alias
storePassword=your_password
```

Also need Android SDK Platform **37**, NDK (for Chaquopy), and host Python **3.10**.

---

## Project Structure

```
TV/
├── app/                    # Main app (shared logic + UI flavors)
│   ├── libs/               # Local AARs (engines + optional lib-*.aar Media3)
│   └── src/
│       ├── main/           # Shared business logic
│       ├── leanback/       # Android TV UI
│       └── mobile/         # Phone UI
├── catvod/                 # :catvod — Spider + OkHttp stack
├── quickjs/                # :quickjs — QuickJS bridge
├── chaquo/                 # :chaquo — Python 3.10 (Chaquopy)
├── thunder|tvbus|hook|jianpian|zlive|forcetech/  # Engine sources/wrappers (not in settings.gradle)
├── .github/workflows/build.yml
└── docs/                   # CONFIG / SPIDER / LOCAL / LIVE (also gitignored as /docs)
```

**Docs** (when present): `docs/CONFIG.md`, `docs/SPIDER.md`, `docs/LOCAL.md`, `docs/LIVE.md`.

---

## Architecture

### Packages (`app/src/main/java/com/fongmi/android/tv/`)

| Package | Purpose |
|---------|---------|
| `api/` | Network / config loading |
| `bean/` | Models |
| `browse/` | Browse UI logic |
| `db/` | Room |
| `dlna/` | JUPnP DLNA |
| `event/` | EventBus |
| `player/` | Exo / MPV engines, extractors |
| `server/` | NanoHTTPD (ports 9978–9998) |
| `service/` | Playback / Media3 session |
| `setting/` | Preferences |
| `ui/` | Shared UI helpers |
| `utils/` | Utilities |

### Variants

- **mode**: `leanback` | `mobile`
- **abi**: `arm64_v8a` | `armeabi_v7a`
- Output name pattern: `{mode}-{abi}.apk`

### Module chain

`:app` → `:quickjs`, `:chaquo` → `:catvod`

### Conventions

- Java 21, ViewBinding, EventBus (annotation processor index), Room (`app/schemas/`), Gson
- Shared logic in `main/`; UI split by flavor sourcesets
- Do not add Gradle modules without updating `settings.gradle`

---

## Dependencies & binaries

### Maven / catalog (`gradle/libs.versions.toml`)

Appcompat, Room, Glide, OkHttp, EventBus, JUPnP, NanoHTTPD, NewPipeExtractor, QuickJS wrappers, Chaquopy plugin, etc.

**Note**: Catalog has `androidx.media` (support library), **not** a full public Media3 bundle. Player Media3 comes from **local AARs**.

### Local AARs in `app/libs/`

| File | Role | Tracked? |
|------|------|----------|
| `thunder-release.aar` | Xunlei | Yes |
| `jianpian-release.aar` | Jianpian | Yes |
| `forcetech-release.aar` | ForceTech | Yes |
| `tvbus-release.aar` | TVBus | Yes |
| `hook-release.aar` | Hook | Yes |
| `lib-*.aar` | Custom Media3 (+ FFmpeg/MPV/danmaku extensions) | **No (gitignored) — MISSING** |

---

## CI/CD

File: `.github/workflows/build.yml`

- **Triggers**: push / PR to `main`, `workflow_dispatch`
- **Build**: `./gradlew assembleMobileArm64_v8aDebug --stacktrace`
- **Steps**: checkout → JDK 21 → Python 3.10 → Android SDK setup → generate keystore → write `local.properties` → Gradle → upload APK artifact `mobile-arm64_v8a`
- **Secrets** (optional for debug; used if set for signing): `KEY_ALIAS`, `STORE_PASSWORD`, `KEY_PASSWORD`

Ubuntu runners already include Android SDK 37 / NDK; do not pin obsolete NDK package IDs.

---

## Known Gotchas

1. `local.properties` is mandatory for **all** builds (debug included).
2. `*.properties` is gitignored — exceptions exist only for `gradle.properties` and `gradle/wrapper/gradle-wrapper.properties`.
3. `lib-*.aar` is gitignored at repo root and under `app/libs/` — custom Media3 never ships with a clean clone.
4. Chaquopy needs host Python **3.10** (CI and local).
5. Release uses signing + ProGuard (`proguard-rules.pro` + `proguard-rules-media.pro`).
6. Room schema / EventBus index are annotation-processor generated.
7. Engine folders under repo root are **not** Gradle modules; packaged AARs live in `app/libs/`.
8. Upstream FongMi/TV same structure: engines committed, `lib-*.aar` ignored — clean clone does not compile without private Media3 AARs.
9. Do not “fix” Media3 solely with public Maven artifacts without verifying custom packages (`mpvplayer`, `danmaku`, etc.).
10. `gh` CLI path on this Windows machine: `C:\Program Files\GitHub CLI\gh.exe`.
