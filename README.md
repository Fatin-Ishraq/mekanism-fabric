# Mekanism for Fabric (unofficial)

An unofficial port of [Mekanism](https://github.com/mekanism/Mekanism) to the [Fabric](https://fabricmc.net) mod loader for **Minecraft 1.21.1**,
based on Mekanism **10.7.19** (upstream commit `a00109e4`).

> **Not affiliated with or endorsed by the Mekanism team.** Please report problems with this port here, never on the official Mekanism issue tracker.

## Status

All four Mekanism mods are ported: **Mekanism**, **Mekanism: Generators**, **Mekanism: Additions** and **Mekanism: Tools**.

What has been verified, all by automated runs (see [How it is tested](#how-it-is-tested)):

* Everything registers: 499 blocks, 782 items, 2814 Mekanism recipes, no recipe errors, no missing block or item models.
* Machines process recipes, cables move energy, multiblocks form, ores and Additions mobs generate, every GUI block opens its screen.
* The release jars boot on a real (non-development) Fabric server and client.

**It has not been play-tested by a human yet.** Expect bugs that only show up in real play. Back up your worlds.

## Installing

Requirements:

* Minecraft **1.21.1**
* [Fabric Loader](https://fabricmc.net/use/) **0.16.14** or newer
* [Fabric API](https://modrinth.com/mod/fabric-api)
* [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) **21.1.6** or newer (provides Mekanism's config system)

Put `MekanismFabricated-*.jar` in your `mods` folder. It is a single jar that contains Mekanism, Generators, Additions and Tools. Team Reborn
Energy is bundled.

Block, item and recipe ids stay in the `mekanism` namespace, so datapacks and scripts written for Mekanism keep working. The Fabric mod id is
`mekanism_fabric` (it also provides `mekanism`).

### Energy and storage interop

* Energy is exposed through **Team Reborn Energy** (1 FE = 1 E), so Mekanism cables and machines connect to Tech Reborn and other TR Energy mods.
* Items and fluids use Fabric's **Transfer API** (1 mB = 81 droplets), for blocks and for items such as tanks, buckets and energy tablets.

### Optional integrations

| Mod | Notes |
|---|---|
| JEI, EMI | Recipe categories for all four mods |
| Jade, WTHIT | Energy, fluid and chemical info for Mekanism blocks |
| CC: Tweaked | Mekanism machines are ComputerCraft peripherals |
| CraftTweaker | Mekanism recipe types are scriptable |
| Trinkets | Stands in for Curios: jetpacks, free runners and other gear work in trinket slots that your pack assigns |
| Wildfire's Female Gender Mod | Armor settings for Mekanism and Mekanism: Tools armor |

Not available because these mods have no Fabric 1.21.1 release: ProjectE, The One Probe, FramedBlocks, JsonThings, OpenComputers 2,
Flux Networks, Grand Power.

## Building

Requires JDK 21 or newer to run Gradle (a Java 21 toolchain is downloaded automatically if missing).

```sh
cd port
./gradlew build
```

The release jar is written to `port/build/release/` (the four individual mod jars it bundles are in `port/build/libs/`).

## How it is tested

No manual play-testing so far; everything below runs without a human:

| Command (in `port/`) | What it checks |
|---|---|
| `./gradlew build` | Compiles everything, runs upstream Mekanism's unit tests, builds the release jars and checks them for remapping errors |
| `./gradlew runGametest` | Headless in-game tests: machines, energy/fluid/item transfer, multiblocks, worldgen, gear hooks, chunk loading |
| `./gradlew runSmokeWorld` | Starts a client, creates a world, opens every Mekanism GUI, renders the creative tabs, then quits |
| `./gradlew prodSmokeWorld` | The same, using the release jars in a production (non-development) client |

Add `-Precipe_viewer=jei|emi`, `-Plooking_at=jade|wthit`, `-Pcomputers=cc`, `-Paccessories=trinkets`, `-Ptweaker=crt` or `-Pgender=true` to run
these with an optional integration installed.

## How the port works

Mekanism's code is used almost unchanged. A compatibility layer (`port/src/fabric/java/mekanism/neoshim`) re-implements, from scratch, the parts of the
NeoForge API that Mekanism calls, on top of Fabric API, Mixins and interface injection. Upstream files that needed changes carry a `//FABRIC:` comment.
[NOTES.md](NOTES.md) is the detailed porting log: design decisions, how each NeoForge hook is reproduced, verification, and known limitations.

## Repository layout

* `port/`: the Fabric project (Gradle + Fabric Loom). Upstream sources live in `port/src/{api,main,generators,additions,tools,datagen}`, Fabric-only
  code in `port/src/fabric`.
* `NOTES.md`: porting log.
* `upstream/` (not committed): an optional local clone of upstream Mekanism for reference, see NOTES.md.

## Roadmap

1. **1.21.1** (Mekanism 10.7.19): this release.
2. Later Minecraft versions following upstream's branches (26.2, 26.1.2, 26.3): not started.

## License and credits

[MIT](LICENSE), like Mekanism itself. Mekanism is created by Aidan C. Brady and the Mekanism team; all original code and assets are theirs. This port
does not include NeoForge code: the compatibility layer is a clean-room implementation of its API.
