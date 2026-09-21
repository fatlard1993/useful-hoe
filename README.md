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
- **Whole-Plant Crops** - Modded plants that are taken whole rather than picked and replanted
- **Fortune Support** - Fortune enchantment on hoe increases crop drops
- **Visual Preview** - Colored particles show affected area when holding a hoe
- **Reach Enchantment** - New enchantment to increase hoe area (5 levels)
- **Rain Growth** - Crops standing out in the rain get extra growth rolls; nothing under a roof is affected
- **Fully Configurable** - JSON config for area sizes, durability costs, action toggles and rain growth

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

## Rain Growth

A crop that can see the sky while it rains gets extra growth rolls each time the game random-ticks
it (one by default). Each extra roll is the crop's own random tick, so light, moisture and spacing
still decide whether it grows: rain makes the roll come round more often, it does not make a crop
in the dark grow. Snow does not count.

What counts is the block tag `#useful-hoe:rain_grown`: vanilla crops (`#minecraft:crops`), sugar
cane, bamboo, sweet berry bushes and cocoa. Another mod adds its own crops by shipping a tag file,
with no dependency on this one.

## Whole-Plant Crops

Some modded crops are a plant rather than a row: one block holds the whole thing, breaking it is
the harvest, and there is no seed to put back. Those are taken whole - drops and all, nothing
replanted - instead of being harvested and re-sown the way a wheat row is.

What counts is the block tag `#useful-hoe:harvested_whole`, which nothing vanilla is in; a mod adds
its own by shipping a tag file, again with no dependency on this one. Whether one is ready is asked
of the block itself, by the one question vanilla lets any block answer about its own growth: a crop
that bone meal would still bring on is left standing, and one it would not is taken.

## Durability Cost

Area actions cost **1 base + 1 per affected block** durability (configurable). Creative mode does not consume durability.

## Configuration

A config file is created at `config/useful-hoe.json` on first run. With Pandorical installed, the
switches, the durability costs and the rain growth settings are also on the Useful Hoe page of the
mod menu, for ops, and a change there takes effect at once; a change to the file needs a server
restart.

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
| `rainGrowthEnabled` | `true` | Crops standing in the rain get extra growth rolls |
| `rainGrowthBonusTicks` | `1` | Extra growth rolls per random tick in the rain (0-8) |

## With Other Mods

- **[Dirt Slab](https://github.com/fatlard1993/dirt-slab)** - The area tills dirt, grass, path and coarse dirt slabs the same way a single right-click on one does
- **[Hemp Craft](https://github.com/fatlard1993/hemp-craft)** - A hemp plant is one of those whole-plant crops: the sweep takes the ones that are ripe or gone to seed, root and all, and leaves the ones still growing
- **[Village Quests](https://github.com/fatlard1993/village-quests)** - A farmer takes apprentices: five lessons on the Reach enchantment, the off-hand, replanting and Fortune, berries and column crops, and what the wide swing costs a hoe, plus a sixth on the emerald hoe when Emerald Tools is installed. Finishing gets you sixteen bone meal

## Development

Installing is in [DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT, see [LICENSE](LICENSE).
