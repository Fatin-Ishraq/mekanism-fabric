# 26.1.2 port — status (compiles; unit + property tests pass; smoke runs next)

Last compile: `./gradlew compileJava compileGametestJava compileTestJava` → **0 errors** (was 2,008 error lines at the ingredient-rewrite handoff). `test` + `propertyTest` (58 tests) → **BUILD SUCCESSFUL** under the Fabric Loader JUnit runtime.

Merge: `origin/26.1` advanced by two "not compiled yet" WIP commits (dca50bf7) that reworked the client shim/mixin layer and deleted files this tree depends on (`VanillaFluidClientExtensions`, `AbstractContainerScreenInjectMixin`, `IScreenExtension`). The merge is resolved to the locally verified content — the merged tree is byte-identical to `c5a7b329` and the remote-only additions (dead fluid-ingredient island, orphan mixins) are dropped. Compile + tests re-green after the merge.

## Done
- Toolchain: non-remapping Loom 1.19.1, Java 25, Gradle 9.8.1, Fabric API 0.155.3+26.1.2 (core only, integrations excluded).
- Transfer layer: NeoForge 26.1 `ResourceHandler`/`Transaction`/`ItemAccess` shim over the Fabric Transfer API; `Capabilities` + `CapabilityBridge` rewritten.
- Extension interfaces updated to NeoForge 26.1 signatures: `IItemExtension`, `IItemStackExtension`, `IBlockExtension`, `IBlockStateExtension`;
  `setNoCombineRepair` implemented (ItemPropertiesMixin + ItemNoCombineRepairMixin). `FluidStack` implements `FluidInstance`.
- Model system rebuilt for 26.1 (old BakedModel shim and bridge deleted):
  - `DynamicBlockModel` renders through Fabric `emitQuads`; `BlockStateModelExtension`, `UnbakedGeometryExtension`, `UnbakedModelExtension`,
    `ResolvedModelExtension`, `ModelBakerExtension`, `BlockStateModelPartExtension`, `BakingResultExtension` injected (scripts/gen_injections.py).
  - `ModelData` moved to `neoforge.model.data`; `BlockGetter.getModelData(pos)` reads Fabric block entity render data.
  - Standalone models -> Fabric extra models; custom block state models -> Fabric `CustomUnbakedBlockStateModel`; model loaders -> Fabric
    `UnbakedModelDeserializer` (build rewrites model JSON `loader` to `fabric:type`); clean-room `neoforge:composite` (fabric/client/model/CompositeModel).
  - Item model / special renderer / select & conditional property events write to the vanilla ID mappers.
  - `ModelEvent.BakingCompleted` fired from ModelManagerApplyMixin; ModelDiscoveryMixin resolves extra dependencies.
- Ingredient rewrite (item + fluid) finished against the 26.1 `Ingredient(HolderSet)`/codec API and Fabric custom ingredients; `RecipeViewerUtils`,
  registration and JSON rewrites follow.
- Shim sweep for 26.1 API drift (this pass): registry `get/getValue` aliases (`MappedRegistryAliasMixin`), `ServerExplosion`-based meltdown with AW'd internals,
  `ChunkRenderTypeSet` on `ChunkSectionLayer`, `ITransformationExtension` mirroring NeoForge bytecode (`applyOrigin`, normal matrix via `Matrix4fc.normal(Matrix3f)`),
  `putBakedQuad`/`QuadInstance` vertex path, `SubmitNodeCollector` render events, creative-tab API, spawn-egg properties, `ParticleEngine`/terrain-particle rework,
  `getBooleanOr` NBT access, `PlayerLookup.level`, `isInvulnerableTo(ServerLevel, …)`, `getDefaultAttributeModifiers(ItemStack)`, `HolderSet.isBound/iterator`,
  TriConsumer `forEachModifier(EquipmentSlotGroup, …)` mixin. Dead shim/mixin code deleted (`RecipesUpdatedEvent`, `IParticleEngineExtension`,
  `DeferredSpawnEggItem`, `ParticleEngineInjectMixin`).
- Access widener regenerated and extended by hand at the bottom (`mekanism.accesswidener`); private 26.1 `BlockEntityType` constructor opened for mods.

## Next
1. Smoke runs: `runServer`, `runSmokeClient`, `runGametest`, `runSmokeWorld` — iterate until they start cleanly (compile+tests pass, but mixins for classes not loaded by the unit tests
   (client render/model/mixin paths) still need runtime validation; "No refMap loaded" is expected in dev, mojmap targets must be exact).
2. Mixin target fixes validated by the test run: `EntityInvulnerabilityMixin` → `isInvulnerableToBase`, `PlayerHooksMixin` save hooks → `ValueOutput`/`ValueInput`,
   `LivingEntityHooksMixin` → `hurtServer`/`causeFallDamage(double,…)`/swim-speed `@ModifyArg` moved to `travelInWater`, `EntityThunderHitMixin` @Redirect handler takes all enclosing args.
3. `neoforge:obj` loader (transmitters, Meka-Tool) and `neoforge:fluid_container` item model (buckets) are not implemented yet.
4. Re-apply needed //FABRIC edits from PORT_26.1_REAPPLY.txt, then modules (Generators/Additions/Tools) and integrations.
