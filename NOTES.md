# Mekanism → Fabric 1.21.1: porting log

Unofficial port. Not affiliated with or endorsed by the Mekanism team.

## Upstream reference

| | |
|---|---|
| Repo | https://github.com/mekanism/Mekanism |
| Tag | `v1.21.1-10.7.19.85` (release 10.7.19, line `1.21.x`) |
| Commit | `a00109e4856fd38b9c5b3dd7f22ce4a59cd65a80` (2026-04-10, "bump version to 10.7.19") |
| Local clone | `upstream/` (gitignored, shallow clone of the tag above) |

Re-create with:
`git clone --branch v1.21.1-10.7.19.85 --depth 1 https://github.com/mekanism/Mekanism.git upstream`

## Upstream survey (10.7.19)

| Source set | Java files | Lines | Files importing `net.neoforged` |
|---|---:|---:|---:|
| `src/api` | 265 | 30.3k | 98 |
| `src/main` (core) | 1760 | 182.0k | 472 |
| `src/generators` | 121 | 11.2k | 25 |
| `src/additions` | 64 | 4.4k | 16 |
| `src/tools` | 52 | 3.3k | 16 |
| `src/datagen` | 147 | 20.9k | 57 |
| `src/gameTest` + `src/test` | 36 | 3.8k | n/a |
| `annotation-processor` | small | | none (plain javac AP that generates computer-method handlers) |

* Upstream has no "defense" module on 1.21.1 (it was removed years ago). The modules are core, generators, additions and tools.
* There are 238 distinct NeoForge classes in total. The heaviest by import count: `FluidStack` (162), `DeferredHolder` (58), `IPayloadContext` (57), `FluidType` (49), `ModelData` (31), `IFluidHandler` (29), `IItemHandler` (27), `BlockCapabilityCache` (26), `IEventBus` (26), `ModConfigSpec` (25), `BlockCapability` (25), `RegisterCapabilitiesEvent` (22).
* Core package sizes (lines): tile 25k, client/gui 21k, content 20k, integration 15k, lib 13k, client/render 10k, recipe_viewer 8k, inventory 8k, item 7k.
* Integrations in core: CraftTweaker 6.2k, CC/computer 2.9k, energy compat 1.4k, Jade/TOP/WTHIT 1.5k, ProjectE 1.2k, FramedBlocks 0.8k, JEI/EMI 8.2k.
* Resources: `src/main/resources` 13 MB (textures, hand-written models, lang). `src/datagen/generated` 13 MB / 5.9k files (blockstates, models, recipes, tags, loot, advancements).
* Generated data uses NeoForge-only JSON: `neoforge:difference` ingredients (704), `neoforge:conditions` (637), `neoforge:mod_loaded` (519), `neoforge:intersection` (128), `neoforge:tag_empty`/`not` (64 each), `neoforge:compound` (45), `neoforge:fluid_container` model loader (22), and 6 `neoforge:add_features` biome modifiers.
* AccessTransformers: `main` has 120 lines, `additions` has 9.

## Architecture decisions

### D1. Thin NeoForge compatibility layer ("neoshim") instead of rewriting call sites
Mekanism code calls NeoForge APIs from hundreds of files. Rewriting every call site to Fabric idioms would make each future upstream merge a large manual job. Instead the port contains a compatibility layer that re-implements, with the same signatures, the subset of the NeoForge API that Mekanism uses. Upstream code then compiles largely unchanged.

* **Package relocation:** the layer lives under `mekanism.neoshim.*`, not `net.neoforged.*`. This avoids class clashes with Sinytra Connector or other mods that ship real NeoForge classes. A deterministic script (`port/scripts/normalize_upstream.py`) rewrites imports such as `net.neoforged.neoforge.X` to `mekanism.neoshim.neoforge.X`. It is applied identically to every upstream snapshot we import, so merges stay mechanical (see D10).
* **Exceptions:** classes provided by Forge Config API Port keep their real `net.neoforged` names: `net.neoforged.neoforge.common.ModConfigSpec`, `net.neoforged.neoforge.common.TranslatableEnum`, `net.neoforged.fml.config.*`, and `net.neoforged.neoforge.client.gui.ConfigurationScreen`.
* **Clean-room implementation:** NeoForge is LGPL-2.1. The shim classes are written from scratch against the public API shape. NeoForge source is not copied into this MIT project.
* **Vanilla extension methods:** NeoForge patches methods into vanilla classes, for example `IBlockExtension`, `IItemExtension`, `IItemStackExtension`, `ILevelExtension#getCapability`, `IPlayerExtension#openMenu` and `IFluidStateExtension`. These are reproduced with **Loom interface injection** (`loom:injected_interfaces` in fabric.mod.json) plus Mixins that make the vanilla classes implement the shim interfaces at runtime. This lets upstream `@Override`s and calls compile unchanged. Where vanilla should *call* one of these hooks (for example light emission, custom drops or enchantability), a targeted Mixin is added only when Mekanism actually depends on that behaviour. Every such hook is listed in the "Hook status" table below.

### D2. Event buses
Shim `IEventBus` / `NeoForge.EVENT_BUS` is a small typed dispatcher. It resolves the listener's event type from the lambda the same way NeoForge does, via TypeTools (Apache-2.0, jar-in-jar). It supports priorities and `ICancellableEvent`. Fabric callbacks are bridged into shim events in `mekanism.fabric.bridge.*`, for example `ServerLifecycleEvents` to `ServerStartedEvent`/`ServerStoppedEvent`, `ServerTickEvents` to `ServerTickEvent`, and `CommandRegistrationCallback` to `RegisterCommandsEvent`. Mod-bus lifecycle events (`FMLCommonSetupEvent`, `RegisterCapabilitiesEvent`, `RegisterPayloadHandlersEvent`, and so on) are fired by our `ModInitializer` in NeoForge order. `@EventBusSubscriber` classes are registered explicitly from a list, without classpath scanning.

### D3. Registration
Shim `DeferredRegister` queues entries when `register(IEventBus)` is called. After the Mekanism constructor returns, the bootstrap flushes all queues **in `BuiltInRegistries.REGISTRY` order** to mimic NeoForge's `RegisterEvent` ordering, so blocks are registered before items. Custom registries (`NewRegistryEvent`: chemicals, modules, robit skins, chemical ingredient types) are created with `FabricRegistryBuilder` and synced. `DeferredHolder` is a lazy `Holder.Reference` lookup, the same model as NeoForge.

### D4. Capabilities → Fabric API Lookup + adapters
* The shim `BlockCapability<T,C>`, `ItemCapability<T,C>` and `EntityCapability<T,C>` are thin wrappers over Fabric `BlockApiLookup`, `ItemApiLookup` and `EntityApiLookup` with the same identifiers. `RegisterCapabilitiesEvent` registers providers into those lookups.
* `BlockCapabilityCache` is implemented by the shim with NeoForge semantics, including **invalidation listeners**, which Mekanism transmitters need. Mixins on chunk block-entity set/remove, chunk load/unload, and the shim `ILevelExtension#invalidateCapabilities` notify listeners.
* **Interop boundary:** Mekanism keeps its internal simulate/execute handlers (`IItemHandler`, `IFluidHandler` in mB, `IEnergyStorage`, plus its own chemical handlers). Adapters translate at the boundary:
  * items: `IItemHandler` ⇄ Fabric Transfer `Storage<ItemVariant>` (`ItemStorage.SIDED`)
  * fluids: `IFluidHandler` (mB) ⇄ `Storage<FluidVariant>` (droplets; 1 mB = 81 droplets) (`FluidStorage.SIDED`, `FluidStorage.ITEM`)
  * energy: Mekanism Joules ⇄ **Team Reborn Energy** `EnergyStorage` (E). The conversion rate comes from upstream's FE config rate, treating 1 E = 1 FE.
  * Transactions: an adapter simulates during `insert`/`extract` and commits the real action in an outer-close callback when the transaction commits. When wrapping a foreign `Storage` as `IItemHandler`/`IFluidHandler`, simulate uses a nested transaction that is aborted, and execute uses a committed transaction.
  * Wrappers unwrap each other to avoid adapter chains.

### D5. Data attachments → Fabric Data Attachment API
`AttachmentType` maps to `AttachmentRegistry` (Fabric API 0.116 supports persistence). Sync is not available on 1.21.1 Fabric, so where Mekanism needs client sync we use our own payloads.

### D6. Networking
`RegisterPayloadHandlersEvent`, `PayloadRegistrar` and `IPayloadContext` are shimmed over Fabric Networking API v1 (`PayloadTypeRegistry` plus `ServerPlayNetworking`/`ClientPlayNetworking`). `PacketDistributor` maps to Fabric send helpers and `PlayerLookup`. Configuration-phase tasks map to Fabric `ServerConfigurationNetworking` tasks.

### D7. Config → Forge Config API Port (user decision, 2026-10-18)
Fuzs' Forge Config API Port (FCAP, `fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.6`) provides the real NeoForge `ModConfigSpec`/`ModConfig` API on Fabric, so `mekanism.common.config` stays almost identical to upstream. `ModContainer#registerConfig` delegates to `NeoForgeConfigRegistry`. `ModConfigEvent` is bridged from `NeoForgeModConfigEvents`. FCAP is a required dependency, declared in fabric.mod.json and not bundled for now.

### D8. Energy → Team Reborn Energy (user decision, 2026-10-18)
`teamreborn:energy` is jar-in-jar'd. See D4 for the adapter.

### D9. Mod identity (user decision, 2026-10-18)
* Fabric mod id `mekanism_fabric`, with `"provides": ["mekanism"]` so Fabric addons that depend on `mekanism` still resolve.
* Resource namespace stays **`mekanism`** (`mekanism:*` block, item and recipe IDs), so datapacks, scripts and saves are compatible. `Mekanism.MODID` stays `"mekanism"`. The shim `ModList.isLoaded("mekanism")` works because of `provides`.
* Display name: **"Mekanism (Unofficial Fabric Port)"**. The description states it is unofficial and unaffiliated. MIT LICENSE and the upstream copyright notice are kept.
* Generators, additions and tools will follow the same pattern (`mekanismgenerators_fabric`, and so on).

### D10. Source layout & upstream-merge workflow
* `port/` mirrors upstream's `src/` layout (`src/api`, `src/main`, `src/generators`, ..., `src/datagen/generated`), so `diff -r` against upstream is meaningful.
* Fabric-only code lives in `port/src/fabric/{java,resources}` and is compiled into the core jar. Files that do not exist upstream are never mixed into upstream directories.
* **Vendor import commits:** each upstream snapshot is copied in by `port/scripts/import_upstream.sh`, which copies the source sets and runs `normalize_upstream.py` for the import relocation. It is committed as a standalone "vendor:" commit **before** any manual edits. For a future version bump (26.x), import the new snapshot onto a branch from the previous vendor commit, then `git merge` it into the port branch. Git's 3-way merge then only conflicts where both upstream and our edits touched the same lines.
* Manual edits to upstream files should be minimal. Where a change is non-obvious it gets a `// FABRIC:` comment so it can be found with grep.

### D11. Resources / datagen
* Datagen is **not** ported for now. The upstream `src/datagen/generated/**` output is committed and used directly, as upstream itself does.
* NeoForge-specific JSON is converted at build time by a `processResources` transform (`port/scripts` / Gradle): `neoforge:conditions` becomes `fabric:load_conditions`, `neoforge:mod_loaded` becomes `fabric:all_mods_loaded`, `neoforge:not`/`tag_empty` map to the fabric equivalents, and `neoforge:difference`/`compound`/`intersection` ingredients map to `fabric:difference`/`any_of`/`all_of`. Biome modifiers are replaced by Fabric `BiomeModifications` in code, and `neoforge:fluid_container` item models by a Fabric model plugin. Nothing in the committed resources is hand-edited.
* AccessTransformers are converted to `mekanism.accesswidener` by script (`public-f` → `accessible` + `mutable`/`extendable`; `protected` → `accessible`).

### D12. Toolchain
Fabric Loom `1.11.x` (remapping, intermediary), Minecraft 1.21.1, Fabric Loader 0.16+/0.17+, Fabric API `0.116.17+1.21.1`, Mojang mappings + Parchment `2024.11.17` (the same Parchment version as upstream, so parameter names match), and Java 21 toolchain (the local JDK 21 is at `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot`).

## Module plan / status

| Phase | Scope | Status |
|---|---|---|
| 0 | Clone, survey, decisions (this file) | done |
| 1 | Gradle/Loom scaffold, vendor import of upstream sources, AT→AW, fabric.mod.json | in progress |
| 2 | neoshim: registries, event bus, config bridge, networking, fluids (FluidStack/FluidType/IFluidHandler), items (IItemHandler), energy, capabilities | in progress |
| 3 | Core compiles (`api` + `main`), integrations excluded | todo |
| 4 | Dev client launches into a world with core blocks/items registered | todo |
| 5 | Machines tick, energy/fluid/chemical transfer, transmitters, multiblocks | todo |
| 6 | GUIs, client rendering, models (OBJ loader, ModelData → FRAPI), particles, sounds, keybinds | todo |
| 7 | Resources transform (conditions/ingredients), worldgen (ores via BiomeModifications) | todo |
| 8 | Generators, Additions, Tools modules | todo |
| 9 | Integrations: EMI/JEI, Jade/WTHIT, CC:Tweaked (Fabric), others as available | todo |

## Hook status (vanilla behaviour NeoForge adds that Mekanism relies on)
_Filled in as hooks are implemented. Columns: hook, used for, implementation, status._

## Stubbed / disabled
_Nothing yet._

## Known bugs
_None yet (nothing runs yet)._

## Log
* **2026-10-18**: Cloned upstream at the 10.7.19 tag and surveyed it. Recorded architecture decisions D1–D12. User decisions: Team Reborn Energy, Forge Config API Port, mod id `mekanism_fabric` with namespace `mekanism`.
