# Customizable Brightness

A Fabric mod for Minecraft 1.21.1 that allows you to cycle through configurable brightness levels beyond Minecraft's normal 100% limit.

## Features

- **Keybind**: Press `B` (configurable) to cycle through brightness levels
- **Configurable Stops**: Default brightness levels: 0%, 100%, 200%, 500%, 1000%
- **Beyond Normal Limits**: Go way beyond Minecraft's standard 100% brightness
- **Visual Feedback**: See your current brightness level in the action bar
- **Persistent Settings**: Your last brightness level is saved and restored on game restart
- **Fully Customizable**: Edit the config file to add your own brightness levels
- **Mod Menu Integration**: Configure brightness stops through an in-game GUI (requires Mod Menu and Cloth Config)

## Building

To build the mod from source:

```bash
./gradlew build
```

On Windows:
```cmd
gradlew.bat build
```

The compiled mod will be in `build/libs/customizable-brightness-1.0.0.jar`

## Installation

### Required:
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Place the mod jar in your `.minecraft/mods` folder
4. Launch Minecraft 1.21.1 with Fabric

### Optional (for GUI config):
5. Install [Mod Menu](https://modrinth.com/mod/modmenu)
6. Install [Cloth Config](https://modrinth.com/mod/cloth-config)
7. Access config through Mods menu in-game

## Configuration

### Option 1: GUI (Recommended)
If you have Mod Menu and Cloth Config installed:
1. Open the Mods menu in-game
2. Find "Customizable Brightness" and click the config button
3. Edit brightness stops in the GUI
4. Click "Save" to apply changes

### Option 2: Manual Config File
The config file is located at `config/customizable_brightness.properties` in your Minecraft directory.

### Default Configuration

```properties
brightness_stops=0.0,1.0,2.0,5.0,10.0
last_brightness_index=1
```

### Customizing Brightness Levels

Edit the `brightness_stops` value with comma-separated multipliers:
- `0.0` = 0% brightness (complete darkness)
- `1.0` = 100% brightness (normal)
- `2.0` = 200% brightness
- `5.0` = 500% brightness
- `10.0` = 1000% brightness

**Note:** Values are stored as multipliers internally (1.0 = 100%), but displayed as percentages everywhere in the UI.

Example custom configuration:
```properties
brightness_stops=0.0,0.5,1.0,1.5,3.0,7.0,15.0
```
This will create stops at: 0%, 50%, 100%, 150%, 300%, 700%, 1500%

The config file is automatically created with defaults if it doesn't exist.

## Usage

1. Press `B` (default keybind) to cycle through brightness levels
2. The current brightness level will be displayed above your hotbar
3. Your selected brightness persists between game sessions

You can change the keybind in Minecraft's Controls settings under the "Customizable Brightness" category.

## Requirements

### Required:
- Minecraft 1.21.1
- Fabric Loader 0.16.0+
- Fabric API
- Java 21+

### Optional (for GUI config):
- Mod Menu 11.0.0+
- Cloth Config 15.0.0+

## License

MIT License - See LICENSE file for details

## Author

SimplyAG
