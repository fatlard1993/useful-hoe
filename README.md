# Useful Hoe

A **server-side** Minecraft Fabric mod that makes hoes actually useful! Till, plant, bonemeal, and harvest larger areas with a single click.

**Works with vanilla clients** - no client mod required for multiplayer.

## Features

- **Area-Based Actions** - Till, plant, bonemeal, and harvest multiple blocks at once
- **Staged Actions** - One click per action type (till all > plant all > bonemeal all > harvest all)
- **Auto-Plant** - Hold seeds in off-hand to plant on farmland
- **Auto-Bonemeal** - Hold bone meal in off-hand to fertilize growing crops
- **Auto-Harvest** - Harvest mature crops with automatic replanting
- **Sweet Berry Harvesting** - Pick berries without breaking the bush
- **Vertical Crop Harvesting** - Sugar cane, bamboo, cactus, and kelp - harvest all above base
- **Fortune Support** - Fortune enchantment on hoe increases crop drops
- **Visual Preview** - Colored particles show affected area when holding a hoe
- **Reach Enchantment** - New enchantment to increase hoe area (5 levels)
- **Fully Configurable** - JSON config for area sizes, durability costs, and action toggles

## Area Sizes by Reach Level

All hoes work the same way. Area size is determined by the **Reach** enchantment:

| Reach Level | Area Size |
|-------------|-----------|
| None        | 1x1       |
| I           | 1x3       |
| II          | 4x4       |
| III         | 4x9       |
| IV          | 9x9       |
| V           | 9x18      |

These are the defaults. All sizes are configurable.

## How to Use

1. **Tilling** - Right-click on grass/dirt with a hoe to till the area
2. **Planting** - Hold seeds in your off-hand and right-click farmland
3. **Bonemealing** - Hold bone meal in your off-hand and right-click crops
4. **Harvesting** - Right-click farmland with mature crops to harvest and replant

Actions happen one stage at a time in priority order: **Till > Plant > Bonemeal > Harvest**

Hold **Sneak** to use vanilla single-block behavior.

### Visual Preview Colors

- **Brown** - Blocks that will be tilled
- **Green** - Farmland that will be planted
- **White** - Crops that will be bonemealed
- **Gold** - Mature crops that will be harvested

## Durability Cost

Area actions cost **1 base + 1 per affected block** durability (configurable). Creative mode does not consume durability.

## Configuration

A config file is created at `config/useful-hoe.json` on first run. Changes require a server restart.

| Field | Default | Description |
|-------|---------|-------------|
| `reach0` - `reach5` | See table above | Area `[width, depth]` per Reach enchantment level (max 32) |
| `durabilityBaseCost` | `1` | Flat durability cost per area action |
| `durabilityPerBlock` | `1` | Additional durability cost per affected block |
| `particlePreviewEnabled` | `true` | Show colored particle preview when holding a hoe |
| `particleTickInterval` | `4` | Ticks between particle updates (higher = less frequent) |
| `tillEnabled` | `true` | Enable area tilling |
| `plantEnabled` | `true` | Enable area planting |
| `bonemealEnabled` | `true` | Enable area bonemealing |
| `harvestEnabled` | `true` | Enable area harvesting |

## Requirements

- Minecraft 1.21+
- Fabric Loader 0.16.0+
- Fabric API

## Installation

**Server (dedicated or singleplayer):**
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the mod JAR and place it in your `mods` folder

**Multiplayer clients:** No installation needed - works with vanilla clients!

## License

MIT License
