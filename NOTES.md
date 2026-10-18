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
* Generators, Additions and Tools follow the same pattern: `mekanismgenerators_fabric`, `mekanismadditions_fabric`, `mekanismtools_fabric`, each providing its upstream id.

### D10. Source layout & upstream-merge workflow
* `port/` mirrors upstream's `src/` layout (`src/api`, `src/main`, `src/generators`, ..., `src/datagen/generated`), so `diff -r` against upstream is meaningful.
* Fabric-only code lives in `port/src/fabric/{java,resources}` and is compiled into the core jar. Files that do not exist upstream are never mixed into upstream directories.
* **Vendor import commits:** each upstream snapshot is copied in by `port/scripts/import_upstream.sh`, which copies the source sets and runs `normalize_upstream.py` for the import relocation. It is committed as a standalone "vendor:" commit **before** any manual edits. For a future version bump (26.x), import the new snapshot onto a branch from the previous vendor commit, then `git merge` it into the port branch. Git's 3-way merge then only conflicts where both upstream and our edits touched the same lines.
* Manual edits to upstream files should be minimal. Where a change is non-obvious it gets a `// FABRIC:` comment so it can be found with grep.

### D11. Resources / datagen
* Datagen is **not** ported for now. The upstream `src/datagen/generated/**` output is committed and used directly, as upstream itself does.
* NeoForge-specific JSON is converted at build time by a `processResources` transform (`port/scripts` / Gradle): `neoforge:conditions` becomes `fabric:load_conditions`, `neoforge:mod_loaded` becomes `fabric:all_mods_loaded`, `neoforge:not`/`tag_empty` map to the fabric equivalents, and `neoforge:difference`/`compound`/`intersection` ingredients map to `fabric:difference`/`any_of`/`all_of`. Biome modifier JSON is replayed through Fabric `BiomeModifications` by BiomeModifierBridge, and NeoForge model loaders (including `neoforge:fluid_container`) are handled by a Fabric model plugin. `#neoforge:` tag references become optional `#c:` entries. Nothing in the committed resources is hand-edited.
* AccessTransformers are converted to `mekanism.accesswidener` by script (`public-f` → `accessible` + `mutable`/`extendable`; `protected` → `accessible`).

### D12. Toolchain
Fabric Loom `1.11.8` on Gradle 9.2.1 (remapping, intermediary), Minecraft 1.21.1, Fabric Loader 0.16+/0.17+, Fabric API `0.116.17+1.21.1`, Mojang mappings + Parchment `2024.11.17` (the same Parchment version as upstream, so parameter names match), and Java 21 toolchain (the local JDK 21 is at `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot`).

## Module plan / status

| Phase | Scope | Status |
|---|---|---|
| 0 | Clone, survey, decisions (this file) | done |
| 1 | Gradle/Loom scaffold, vendor import of upstream sources, AT→AW, fabric.mod.json | done |
| 2 | neoshim: registries, event bus, config bridge, networking, fluids, items, energy, capabilities, extension interfaces | done |
| 3 | Core compiles (`api` + `main`) | done |
| 4 | Dev server + client launch with core content registered | done |
| 5 | Machines tick, energy/fluid/chemical transfer, transmitters, multiblocks | done (GameTest verified: machine processing, cable network, dynamic tank formation) |
| 6 | GUIs, client rendering, models (OBJ loader, ModelData → FRAPI), particles, sounds, keybinds, shaders | done (smoke verified: 0 missing models, every GUI block opens, creative tabs render) |
| 7 | Resource transform (conditions/ingredients), worldgen, biome modifiers | done (ores and Additions baby mobs GameTest verified) |
| 8 | Generators, Additions, Tools modules | done (each a separate Fabric mod and release jar) |
| 9 | Integrations | done where the other mod exists on Fabric 1.21.1 (see "Integrations") |
| 10 | Release packaging + production verification | done (4 remapped jars; production server/client/world smoke) |

Remaining before a public release is the user's call (nothing is published): versioning/metadata review, a play-test pass by a human, and the later
roadmap versions (26.2, 26.1.2, 26.3), which have not been started.

### How things are verified (no manual play-testing)
* `gradlew build`: compiles everything, runs upstream's unit tests (`test`, 91 tests under fabric-loader-junit with a vanilla bootstrap extension, plus
  `propertyTest`, 3 jqwik properties), builds the four release jars, rewrites remapping bridges (`bridgeReleaseJars`) and runs
  `checkRemappedImplementations` (scripts/ImplChecker.java: fails on any interface method left unimplemented or any unresolved method reference in the
  release jars, honouring injected interfaces).
* `gradlew runServer`: dedicated dev server; logs a registration summary and the loaded recipe count (`[Fabric port] ...`).
* `gradlew runSmokeClient`: dev client with `-Dmekanism_fabric.smokeTest=true`; exits after resource loading, logs missing block-state/item models and
  runs a Mixin audit (applies every mixin).
* `gradlew runSmokeWorld`: dev client with `-Dmekanism_fabric.smokeWorld=true` (SmokeWorldDriver): creates a flat world, places machines/transmitters,
  checks client sync, server energy, positional light and entity spawn data, opens every Mekanism GUI block, renders all Mekanism creative tabs and checks
  their label colour, checks fog events fired, then quits.
* `gradlew runGametest`: headless Fabric GameTests (`src/fabric/gametest`), junit report at `build/gametest/junit.xml`. 22 tests.
* `gradlew prodSmokeClient` / `prodSmokeWorld`: the same smoke runs against the **release jars** in a production (intermediary) client.
* Optional integrations are added to dev runs by property (never bundled): `-Precipe_viewer=jei|emi`, `-Plooking_at=jade|wthit`, `-Pcomputers=cc`,
  `-Paccessories=trinkets`, `-Ptweaker=crt` (also installs a test ZenScript), `-Pgender=true`. Their GameTests skip when the mod is absent.

Current numbers (2026-10-18, all modules): 499 blocks, 782 items, 178 block entity types, 44 fluids, 10 entity types, 87 menus, 68 chemicals,
2814 Mekanism recipes, 0 recipe parse errors; 0/15322 block states and 0/782 items without a model; 104 GUI blocks open a screen (the 17 that do not
are multiblock parts that only open on a formed structure); 22/22 GameTests; 94/94 upstream unit tests.

## Hook status (vanilla behaviour NeoForge adds that Mekanism relies on)
| Hook | Used for | Fabric implementation | Status |
|---|---|---|---|
| DeferredRegister / RegisterEvent order | all content | ModBusLifecycle: data components, attributes, sounds, effects, particles first, then modded registries, then vanilla order | done |
| Registry aliases | renamed ids in old saves | RegistryAliases + MappedRegistryAliasMixin | done (GameTest) |
| Block capabilities | everything | Fabric API Lookup + CapabilityBridge (Transfer API, TR Energy) both ways | done (GameTest) |
| Item capabilities | buckets, tanks, batteries, tablets | FluidStorage.ITEM / EnergyStorage.ITEM export via ContainerItemContext exchange; import of foreign item storages written back into the stack | done (GameTest) |
| BlockCapabilityCache invalidation | transmitters, machines | CapabilityInvalidation (BE load/unload, chunk load/unload, `invalidateCapabilities`) | done |
| BlockEntity#onLoad / onChunkUnloaded / onDataPacket / handleUpdateTag | all tiles, client sync | Fabric BE/chunk events, ClientPacketListenerMixin, LevelChunkMixin | done (smoke) |
| Extended menus (`openMenu(provider, buf)`) | all GUIs | payload before the open-screen packet + MenuTypeMixin | done (smoke) |
| Payloads, config, data maps, attachments, load conditions, annotation scan data | various | see D2–D11 | done |
| PlayerTickEvent, EntityTickEvent.Post | MekaSuit, jetpack, radiation | PlayerTickMixin, ServerLevelEntityTickMixin | done |
| Server/level ticks, lifecycle, commands, tags, datapack sync, login/logout/respawn/clone/dimension change, tracking | various | Fabric API events (GameEventBridge) | done |
| Block break / right-click / BreakSpeed / ItemCrafted | security, configurator, Atomic Disassembler, bins | Fabric events, BlockBreakSpeedMixin, ResultSlotMixin | done |
| LivingIncomingDamage / LivingFall / LivingJump / EntityInvulnerabilityCheck | MekaSuit, free runners, gas mask, radiation immunity | LivingEntityHooksMixin, EntityInvulnerabilityMixin | done (GameTest) |
| ItemAttributeModifierEvent | Meka-Tool, MekaSuit, free runners | ItemStackAttributesMixin | done (GameTest) |
| `neoforge:swim_speed`, `neoforge:creative_flight` attributes | MekaSuit | NeoAttributes (registered with vanilla's), travel/abilities mixins | done (GameTest: present) |
| FinalizeSpawnEvent | Tools mob gear | MobFinalizeSpawnMixin | done |
| Enchantment hooks | Meka-Tool modules | not needed: modules write the vanilla ENCHANTMENTS component | n/a |
| LightningBolt#setDamage | SPS | LightningBoltMixin + EntityThunderHitMixin | done |
| Positional light emission | glowing fluid tanks | DynamicLightEmission + BlockLightEngineMixin + ChunkAccessLightMixin | done (GameTest + client smoke) |
| Chunk tickets (TicketController) | anchor upgrade, dimensional stabilizer | ForcedChunkStore (per-level SavedData, reinstated + validated on server start) | done (GameTest) |
| IEntityWithComplexSpawn | flame, balloon | ComplexSpawns payload in the pairing bundle | done (smoke) |
| Biome modifiers | ores, Additions baby mobs | BiomeModifierBridge (add_features + any registered modifier codec); server configs loaded for the pass | done (GameTest) |
| Models: geometry loaders, OBJ, ModelData, fluid_container, separate transforms | block/item rendering | PreparableModelLoadingPlugin + NeoBlockModel + FRAPI bridge | done (smoke) |
| Client extensions, shaders, GUI layers, key mappings, particles, item decorators | rendering, HUD | client bridges and mixins | done (smoke) |
| ViewportEvent fog colour / render fog | MekaSuit vision | FogRendererMixin | done (smoke) |
| RenderGuiLayerEvent.Pre (crosshair), RenderHighlightEvent, ScreenEvent.Render.Post, ClientPlayerNetworkEvent.Clone | radial menus, outlines, GUIs | GuiCrosshairMixin, Fabric events, ClientRespawnMixin | done |
| Creative tab label colour | Mekanism tabs | per-tab colour + CreativeModeInventoryScreenMixin | done (smoke) |

## Integrations
| Mod | Fabric 1.21.1 | Status |
|---|---|---|
| JEI | yes | plugins for all modules via `jei_mod_plugin`; fluid ingredients converted to JEI's droplet type (smoke with JEI) |
| EMI | yes | plugins via `emi`; fluids converted to droplet EmiStacks (smoke with EMI) |
| Jade, WTHIT | yes | upstream code unchanged (`jade` entrypoint, `wthit_plugins.json`) (smoke with each) |
| CC: Tweaked | yes | peripherals through CC:T's Fabric lookup `computercraft:peripheral` (GameTest with CC:T) |
| CraftTweaker | yes | upstream integration; 4 conversions rewritten (GameTest with CrT + script) |
| Curios → Trinkets | Trinkets | CuriosIntegration override implemented on Trinkets; MekanismHooks checks `trinkets` (GameTest with Trinkets) |
| Wildfire's Female Gender Mod | yes | registers through WildfireAPI#addGenderArmor (GameTest with the mod) |
| Team Reborn Energy | yes | energy interop (D8) |
| The One Probe | no 1.21.1 Fabric build | excluded |
| ProjectE, FramedBlocks, JsonThings, OpenComputers2, Flux Networks, Grand Power | NeoForge only | excluded; the user decided (2026-10-18) not to port these mods for now |
| JeiTweaker | not released for 1.21.1 | excluded (upstream does not load it either) |

## Overrides / manual upstream edits
* `src/fabric/overrides/java/.../CuriosIntegration.java`: Curios entry points implemented on Trinkets.
* 50 upstream files carry `//FABRIC:` edits (`grep -rln "FABRIC" src/main src/api src/generators src/additions src/tools`): AdditionsClient, AdditionsTags,
  BabyEndermanHeldBlockLayer, BaseModelCache, BaseRecipeCategory, BlockPlasticFenceGate, CCCapabilityHelper, ChemicalBuilder, ClientRegistration,
  ClientRegistrationUtil, ComputerCapabilityHelper, CrTUtils, CreativeTabDeferredRegister, EmiStackUnderMouseProvider, EnergyCompatUtils, EntityFlame,
  FilterManager, FissionReactorMultiblockData, FrequencyManager, GeneratorTags, GeneratorsClient, GeneratorsClientRegistration, GuiElement,
  ItemMekanismSword, JEIAliasHelper, JadeLookingAtHelper, LogisticalTransporterBase, MekKeyBinding, MekaSuitEnergyLevel, MekanismAccess,
  MekanismClient, MekanismEmiRecipe, MekanismGenderArmor, MekanismHooks, MekanismJEI, MekanismKeyHandler, MekanismSavedData, MekanismShaders,
  MekanismStatusOverlay, MekanismTags, MeltdownLevelData, MultiblockManager, PersonalStorageData, RadiationLevelData, SPSMultiblockData,
  SyncAllSecurityData, ToolsClient, ToolsTags, Upgrade, VirtualSlotContainerScreen.

## Build-time fixes for the release jars
* Module jars are remapped with the module compile classpath (otherwise inherited Minecraft members called through Mekanism subclasses stay unmapped).
* **Interface bridge:** Mekanism interfaces (`ITileWrapper`, `IRecipeLookupHandler`, `IQIOCraftingWindowHolder`) declare `getLevel()`/`getBlockPos()` that
  block entities only implement through inherited vanilla methods. Remapping renames the vanilla methods but not Mekanism's, which threw
  AbstractMethodError in production. `bridgeReleaseJars` renames every Mekanism declaration with those exact signatures, and all references, to the
  intermediary names in one TinyRemapper pass over all four jars. `checkRemappedImplementations` guards against regressions.

## Known limitations
* Fabric storage adapters execute on outer commit: several operations on the same slot inside one transaction each see the unmodified slot.
* Biome modifier files are read from the mod jars at startup, so datapacks cannot override them.
* `creative_flight` is granted by the server toggling `abilities.mayfly` (synced by the vanilla abilities packet) rather than by patching every mayfly check.
* `EffectiveSide` treats only threads named "Server thread", or ones marked at server start, as server threads.
* Harmless log noise: "No data fixer registered for <mekanism entity>" (vanilla dev warning), EMI's "Recipe mekanism:info/... not present" (same as upstream).

## Log
* **2026-10-18**: Cloned upstream at the 10.7.19 tag and surveyed it. Recorded architecture decisions D1–D12. User decisions: Team Reborn Energy, Forge Config API Port, mod id `mekanism_fabric` with namespace `mekanism`.
* **2026-10-18**: Vendor import, Loom scaffold, and the neoshim layer. Core compiles. The dedicated server starts with all core content and recipes. The client completes resource loading (automated smoke run). Added the Transfer/TR Energy bridges, data maps and BE sync mixins. 5 GameTests pass.
* **2026-10-18**: Models (loaders, FRAPI bridge), client hooks, extended menus, world smoke driver, ore worldgen. Generators, Additions and Tools load as separate Fabric mods.
* **2026-10-18**: Gameplay hooks (damage/fall/jump/attributes/spawn/crafting/break speed), complex spawn data, persistent chunk tickets, positional light, custom biome modifiers, fog/clone/crosshair events, registry aliases, item-level Transfer API interop, tab label colour.
* **2026-10-18**: Integrations: JEI, EMI, Jade, WTHIT, CC: Tweaked, Trinkets (Curios replacement), CraftTweaker, Wildfire's Gender Mod. User decided not to port ProjectE/TOP/Flux Networks/Grand Power etc. for now.
* **2026-10-18**: Release jars per module, upstream unit tests, production server/client/world verification; fixed two production-only remapping bugs.
