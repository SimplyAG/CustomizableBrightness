# Contributing / Building

This is a [Stonecutter](https://stonecutter.kikugie.dev/) multi-version Fabric mod with two kinds of target:

| Targets | Mappings / Loom | Build script | Compiler |
|---------|-----------------|--------------|----------|
| MC 1.14.4 – 1.21.11 (Yarn) | Yarn + `fabric-loom` 1.13 | `build.gradle` | javac 21 (toolchain) |
| MC 26.1 – 26.3 (Mojmap) | Mojang names + `net.fabricmc.fabric-loom` 1.17 | `mojmap.gradle` | javac 25 (toolchain) |

Minecraft 26.1 is the first **unobfuscated** release, so it dropped Yarn/intermediary. Each build script pins its
compiler through a Gradle toolchain, so **one invocation builds every target**. You need JDK 21 and JDK 25 installed
(Gradle finds them in the usual locations); Gradle itself can run on either. The Yarn targets stay on javac 21
on purpose: a different javac numbers lambdas differently, which would change unchanged classes.

The shared Gradle wrapper is 9.6.0 (Loom 1.17 needs Gradle 9.5 or newer).

```bash
./gradlew :1.21.11:build                 # one target
./gradlew build                          # every target
```

## Release groups

One jar serves several Minecraft versions when they compile to byte-identical classes. `release-groups.json` lists
the groups; the first version of each group (the *lead*) builds the published jar and its `fabric.mod.json` lists
exactly the group's versions. Every member compiles at the lead's Java level so the comparison is exact.

```bash
./gradlew build verifyGroupClasses       # fails if any member's classes differ from its lead's
```

Never add a version to a group unless `verifyGroupClasses` passes for it **and** the lead's staged jar passes
`smokeTest` on it.

## Smoke test (pre-publish, test-only)

`src/smoke` is a separate source set that is never part of a release jar. `:<mc>:smokeTest` starts the real
production client up to ten times (Loom's `ClientProductionRunTask`: official game jar, Fabric Loader, the mod as a plain
jar) with the group's staged jar, Fabric API, and Mod Menu + Cloth Config where they exist. In game it presses `B`
through the keyboard handler, takes F2 screenshots, drives the Brightness option with Video Settings open, and quits;
each launch checks what the previous one quit with. Results land in `release-staging/<version>/smoke/<mc>.json`.

```bash
./gradlew :1.20.1:smokeTest                                 # the staged group jar on 1.20.1 (+ Sodium variant)
./gradlew :1.20.1:smokeTest -PsmokeJar=path/to/other.jar     # any jar, e.g. an old release as a baseline
```

Java 8 for the 1.14–1.16 runs: Gradle must be able to find a JDK 8, e.g.
`-Dorg.gradle.java.installations.paths=C:/path/to/jdk8`.

**Full vs quick.** The full persistence matrix (launches 0–9) runs on each group's lead, on versions with
`smoke_full=true`, and on the Sodium variant. Launch 0 creates the world and quits without pressing `B`, so launch 1
proves the key hint toast comes back for a player who ignored it. Every run also checks that the toast is in the
game's toast manager on first join, is gone after the first `B` press with the dismissal saved, and never shows
again. Other members get a quick check (2 launches: key hint toast, B with screenshots,
keybind category, logs, one restart), since their classes are byte-identical to the lead's. `-PsmokeFull` forces the
full matrix.

**The smoke launch matches a real launcher**, because Loom's production run differs in two ways that matter:

- **LWJGL.** Loom swaps in LWJGL 3.3.2-snapshot when Gradle runs on Java 19+. Versions whose Mojang Java is below 19 set
  `fabric.loom.runtimeJavaCompatibilityVersion` in their `gradle.properties` to get the LWJGL vanilla ships. Without
  it, Sodium refuses to start and 1.14–1.15 crash on exit.
- **Mojang's `-XX:` JVM options.** Loom drops the ones in the version manifest, so the smoke script reads them from
  Loom's cached manifest and passes them, as Prism does. 26.3 needs `-XX:StackShadowPages=32`.

Every launch also runs headless, with a 15-minute timeout and `-XX:ErrorFile` pointing into the game directory.

**Per-version smoke properties** (test-only, they never change a build):

| Property | Effect |
|---|---|
| `smoke_sodium` | Adds a Sodium variant |
| `smoke_modmenu`, `smoke_cloth` | The versions players use where the compile-time one is wrong for that Minecraft |
| `smoke_without=modmenu,cloth` | Leaves them out where no compatible Mod Menu exists |
| `smoke_full` | Forces the full matrix |
| `smoke_startup_retries` | Retries a launch that dies before the game finished starting |

Every crashed attempt is recorded in the results.

## Release process

1. Clean build and stage, in one invocation (staging refuses otherwise and never overwrites a staged release):
   `./gradlew clean build stageRelease` → `release-staging/<mod_version>/` with every group jar and `manifest.json`
   (SHA-1/256/512, Minecraft list, mixin level, jar entries, git commit and tree).
2. `smokeTest` on **every** Minecraft version of every group.
3. Publish **per target**, only group leads: `:<lead>:publishModrinth`, `:<lead>:publishCurseforge`. There is no
   aggregate publish task. `verifyStagedRelease` runs first and refuses unless the staged file still matches the
   manifest, the checked-out tree is the one it was staged from, and every group version has a passing smoke result
   for that exact file. Uploads use the staged file, never a fresh build. `-PpublishDryRun=true` for a dry run.
4. `./gradlew reconcileRelease` downloads every published file of the version from both platforms and checks it
   against the manifest (hash, version, Minecraft list, mixin level, allowlist, platform tags). Any gap fails loudly.
