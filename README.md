# Customizable Brightness

A client-side Fabric mod that lets you cycle through brightness levels beyond Minecraft's normal 100% limit, for Minecraft 1.14.4 through 26.3.

**Press `B` in game** to step to the next brightness level: 0% → 100% → 200% → 500% → 1000% by default. The current level appears above your hotbar.

## Features

- **One key**: press `B` (rebindable under *Options → Controls → Key Binds → Customizable Brightness*)
- **Your own levels**: defaults are 0%, 100%, 200%, 500%, 1000%; set any values you like
- **Remembered between sessions**: the game reopens at the brightness you left it on, whether you chose it with `B` or with the vanilla Brightness slider
- **Action-bar feedback** on every press
- **Hard to miss**: a tutorial-style hint in the corner shows your key until you first press it
- **In-game config** through Mod Menu + Cloth Config (1.16.5 and newer)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api)
2. Download the file for your Minecraft version from [Modrinth](https://modrinth.com/mod/customizable-brightness) or CurseForge
3. Put it in your `.minecraft/mods` folder and launch the game

Optional, for the in-game config screen (1.16.5+): [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config).

## Supported versions

| Minecraft | Java |
|-----------|------|
| 1.14.4 – 1.16.5 | 8+ |
| 1.17 – 1.17.1 | 16+ |
| 1.18 – 1.20.4 | 17+ |
| 1.20.5 – 1.21.11 | 21+ |
| 26.1 – 26.3 | 25+ |

Each download lists exactly the Minecraft versions it has been tested on.

## Configuration

Use Mod Menu → Customizable Brightness → config, or edit `config/customizable_brightness.properties`:

```properties
brightness_stops=0.0,1.0,2.0,5.0,10.0
```

Values are multipliers (`1.0` = 100%) and are shown as percentages in game. For example, `brightness_stops=0.0,0.5,1.0,1.5,3.0,7.0,15.0` gives 0%, 50%, 100%, 150%, 300%, 700%, 1500%. Keep the list sorted for the most natural cycling. The file also stores the brightness in use (`current_brightness`); you don't need to edit that.

## Known limitations

- **Shader packs** (Iris, OptiFine shaders) mostly do their own lighting and ignore Minecraft's brightness above 100%. With a shader pack active, 0%–100% works, but higher levels look like 100%. Without shaders every level works, including with Sodium.
- The vanilla Brightness slider only goes to 100%. Moving it sets the brightness to the slider's value, which is kept like any other level; the next `B` press continues from the nearest stop above it.

## Building

This is a [Stonecutter](https://stonecutter.kikugie.dev/) multi-version project. See [CONTRIBUTING.md](CONTRIBUTING.md) for the JDKs each target needs, the release groups, and the release process (clean build, smoke test, staged publish, reconcile).

## License

MIT. See [LICENSE](LICENSE).
