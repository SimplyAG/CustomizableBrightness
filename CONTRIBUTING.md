# Contributing / Building

This is a [Stonecutter](https://stonecutter.kikugie.dev/) multi-version Fabric mod. The
targets span **two toolchains**, and they must be built with **two different JDKs in
separate Gradle invocations** — do **not** run a single aggregate task across both worlds.

| Targets | Mappings / Loom | Build script | **JDK** |
|---------|-----------------|--------------|---------|
| MC 1.14.3 – 1.21.11 (Yarn) | Yarn + `fabric-loom` 1.13 | `build.gradle` | **JDK 21** |
| MC 26.1, 26.1.1, 26.1.2 (Mojmap) | Mojang mappings + `net.fabricmc.fabric-loom` 1.15 | `mojmap.gradle` | **JDK 25** |

Minecraft 26.1 is the first **unobfuscated** release, so it dropped Yarn/intermediary: it
compiles at `--release 25` and needs JDK 25, while the Yarn targets compile down to
`--release 8` and need a JDK 21-era toolchain. The single shared Gradle wrapper (9.4.0)
runs both, but no one JDK can compile both worlds at once.

## Build the Yarn targets (JDK 21)

```bash
# build one target
./gradlew :1.21.11:build
# build all Yarn targets
./gradlew build   # run this with JDK 21 active; do NOT include the 26.x nodes
```

## Build the 26.x targets (JDK 25)

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew :26.1:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :26.1.1:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :26.1.2:build
```

> The 26.x nodes *configure* fine under JDK 21 (so building a Yarn target with the 26.x
> nodes present works), but they only *compile* under JDK 25. Restoring a single
> all-targets aggregate build via a Java 25 toolchain is tracked as a follow-up.
