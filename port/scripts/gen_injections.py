#!/usr/bin/env python3
"""Generates the NeoForge-extension interface injection plumbing (NOTES.md D1):

 * the "loom:injected_interfaces" block of src/fabric/resources/fabric.mod.json (keys are runtime class names: intermediary on obfuscated versions, Mojang names on 26.1+), and
 * one boilerplate mixin per target class under src/fabric/java/mekanism/fabric/mixin/inject/ that makes the vanilla class implement the
   extension interfaces at runtime, plus their entries in mekanism_fabric.mixins.json.

Targets marked manual=True get the injected interface but no generated mixin (a hand-written mixin in mekanism.fabric.mixin implements it).

Usage: gen_injections.py [<mappings.tiny with official/intermediary/named>]  (omit the mappings on unobfuscated versions)
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXT = "mekanism/neoshim/neoforge/common/extensions/"
CEXT = "mekanism/neoshim/neoforge/client/extensions/"
ATT = "mekanism/neoshim/neoforge/attachment/IAttachmentHolder"

# (named target, is_interface, generics of target, [interfaces with generic args], client_only, manual)
SPEC = [
    ("net/minecraft/world/item/Item", False, "", [EXT + "IItemExtension"], False, False),
    ("net/minecraft/world/item/ItemStack", False, "", [EXT + "IItemStackExtension", EXT + "IMutableDataComponentHolderExtension"], False, False),
    ("net/minecraft/world/item/ItemInstance", True, "", [EXT + "ItemInstanceExtension"], False, False),
    ("net/minecraft/core/component/DataComponentGetter", True, "", [EXT + "IDataComponentGetterExtension"], False, False),
    ("net/minecraft/core/component/DataComponentHolder", True, "", [EXT + "IDataComponentHolderExtension"], False, False),
    ("net/minecraft/resources/ResourceKey", False, "T", [EXT + "IResourceKeyExtension"], False, False),
    ("net/minecraft/resources/Identifier", False, "", [EXT + "IResourceLocationExtension"], False, False),
    ("net/minecraft/world/level/storage/ValueInput", True, "", [EXT + "IValueInputExtension"], False, False),
    ("net/minecraft/world/level/storage/ValueOutput", True, "", [EXT + "IValueOutputExtension"], False, False),
    ("net/minecraft/core/component/DataComponentMap$Builder", False, "", [EXT + "IDataComponentMapBuilderExtension"], False, False),
    ("net/minecraft/core/component/DataComponentPatch$Builder", False, "", [EXT + "IDataComponentPatchBuilderExtension"], False, False),
    ("net/minecraft/world/item/Item$Properties", False, "", [EXT + "IItemPropertiesExtension"], False, True),
    ("net/minecraft/world/level/block/Block", False, "", [EXT + "IBlockExtension"], False, False),
    ("net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase", False, "", [EXT + "IBlockStateExtension"], False, False),
    ("net/minecraft/world/level/material/Fluid", False, "", [EXT + "IFluidExtension"], False, False),
    ("net/minecraft/world/level/material/FluidState", False, "", [EXT + "IFluidStateExtension"], False, False),
    ("net/minecraft/world/level/Level", False, "", [EXT + "ILevelExtension", ATT], False, False),
    ("net/minecraft/core/HolderLookup$Provider", True, "", [EXT + "IHolderLookupProviderExtension"], False, False),
    ("net/minecraft/world/level/LevelReader", True, "", [EXT + "ILevelReaderExtension"], False, False),
    ("net/minecraft/world/level/BlockGetter", True, "", [EXT + "IBlockGetterExtension"], False, False),
    ("net/minecraft/world/entity/Entity", False, "", [EXT + "IEntityExtension", ATT], False, False),
    ("net/minecraft/world/entity/LivingEntity", False, "", [EXT + "ILivingEntityExtension"], False, False),
    ("net/minecraft/world/entity/player/Player", False, "", [EXT + "IPlayerExtension"], False, False),
    ("net/minecraft/world/level/block/entity/BlockEntity", False, "", [EXT + "IBlockEntityExtension", ATT], False, False),
    ("net/minecraft/world/level/chunk/ChunkAccess", False, "", [ATT], False, False),
    ("net/minecraft/core/Holder", True, "T", [EXT + "IHolderExtension<T>"], False, False),
    ("net/minecraft/core/Registry", True, "T", [EXT + "IRegistryExtension<T>"], False, False),
    ("net/minecraft/world/item/crafting/Ingredient", False, "", [EXT + "IIngredientExtension"], False, False),
    ("net/minecraft/util/random/WeightedList$Builder", False, "E", [EXT + "IWeightedListBuilderExtension<E>"], False, True),
    ("net/minecraft/world/effect/MobEffectInstance", False, "", [EXT + "IMobEffectInstanceExtension"], False, False),
    ("net/minecraft/world/entity/EntityType", False, "T", [EXT + "IEntityTypeExtension"], False, False),
    ("net/minecraft/world/level/biome/Biome", False, "", [EXT + "IBiomeExtension"], False, False),
    ("com/mojang/math/Transformation", False, "", [EXT + "ITransformationExtension"], False, False),
    ("net/minecraft/world/MenuProvider", True, "", [EXT + "IMenuProviderExtension"], False, False),
    ("net/minecraft/world/inventory/Slot", False, "", [EXT + "ISlotExtension"], False, True),
    ("net/minecraft/world/level/block/BucketPickup", True, "", [EXT + "IBucketPickupExtension"], False, False),
    ("net/minecraft/world/entity/LightningBolt", False, "", [EXT + "ILightningBoltExtension"], False, True),
    ("net/minecraft/network/protocol/configuration/ServerConfigurationPacketListener", True, "", [EXT + "IServerConfigurationPacketListenerExtension"], False, False),
    ("net/minecraft/world/item/CreativeModeTab", False, "", [EXT + "ICreativeModeTabExtension"], False, False),
    ("net/minecraft/world/entity/EntityType$Builder", False, "T", [EXT + "IEntityTypeBuilderExtension<T>"], False, False),
    ("net/minecraft/world/item/CreativeModeTab$Builder", False, "", [EXT + "ICreativeModeTabBuilderExtension"], False, False),
    ("net/minecraft/world/item/DispensibleContainerItem", True, "", [EXT + "IDispensibleContainerItemExtension"], False, False),
    # client
    ("net/minecraft/client/KeyMapping", False, "", [CEXT + "IKeyMappingExtension"], True, True),
    ("net/minecraft/client/gui/components/AbstractWidget", False, "", [CEXT + "IAbstractWidgetExtension"], True, True),
    ("net/minecraft/client/gui/screens/Screen", False, "", [CEXT + "IScreenExtension"], True, False),
    ("net/minecraft/client/gui/screens/inventory/AbstractContainerScreen", False, "T", [CEXT + "IAbstractContainerScreenExtension"], True, False),
    ("com/mojang/blaze3d/vertex/VertexConsumer", True, "", [CEXT + "IVertexConsumerExtension"], True, False),
    ("net/minecraft/client/resources/model/ModelManager", False, "", [CEXT + "IModelManagerExtension"], True, True),
    ("net/minecraft/client/renderer/entity/EntityRenderDispatcher", False, "", [CEXT + "IEntityRenderDispatcherExtension"], True, False),
    ("net/minecraft/client/renderer/LevelRenderer", False, "", [CEXT + "ILevelRendererExtension"], True, False),
    ("net/minecraft/client/renderer/blockentity/BlockEntityRenderer", True, "T", [CEXT + "IBlockEntityRendererExtension<T>"], True, False),
    ("net/minecraft/client/particle/Particle", False, "", [CEXT + "IParticleExtension"], True, False),
    ("net/minecraft/client/particle/TerrainParticle", False, "", [CEXT + "ITerrainParticleExtension"], True, False),
    ("net/minecraft/client/renderer/block/dispatch/BlockStateModel", True, "", [CEXT + "BlockStateModelExtension"], True, False),
    ("net/minecraft/client/renderer/block/dispatch/BlockStateModelPart", True, "", [CEXT + "BlockStateModelPartExtension"], True, False),
    ("net/minecraft/client/resources/model/geometry/UnbakedGeometry", True, "", [CEXT + "UnbakedGeometryExtension"], True, False),
    ("net/minecraft/client/resources/model/UnbakedModel", True, "", [CEXT + "UnbakedModelExtension"], True, False),
    ("net/minecraft/client/resources/model/ResolvedModel", True, "", [CEXT + "ResolvedModelExtension"], True, False),
    ("net/minecraft/client/resources/model/ModelBaker", True, "", [CEXT + "ModelBakerExtension"], True, False),
    ("net/minecraft/client/resources/model/ModelBakery$BakingResult", False, "", [CEXT + "BakingResultExtension"], True, False),
]

# Bounds for type parameters of generic targets (for the generated mixin declarations)
BOUNDS = {
    "net/minecraft/world/entity/EntityType": "T extends net.minecraft.world.entity.Entity",
    "net/minecraft/world/entity/EntityType$Builder": "T extends net.minecraft.world.entity.Entity",
    "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen": "T extends net.minecraft.world.inventory.AbstractContainerMenu",
    "net/minecraft/client/renderer/blockentity/BlockEntityRenderer": "T extends net.minecraft.world.level.block.entity.BlockEntity",
    "net/minecraft/client/renderer/entity/layers/ElytraLayer":
        "T extends net.minecraft.world.entity.LivingEntity, M extends net.minecraft.client.model.EntityModel<T>",
}


def load_named_to_intermediary(path):
    mapping = {}
    for line in open(path, encoding="utf-8"):
        if line.startswith("c\t"):
            parts = line.rstrip("\n").split("\t")
            mapping[parts[3]] = parts[2]
    return mapping


def signature(iface):
    """'a/b/I<T>' -> 'La/b/I<TT;>;' (Loom generic interface injection syntax)."""
    m = re.match(r"^([^<]+)(?:<(.+)>)?$", iface)
    base, args = m.group(1), m.group(2)
    if not args:
        return base
    return base + "<" + "".join("T" + a.strip() + ";" for a in args.split(",")) + ">"


def java_name(path):
    return path.replace("/", ".").replace("$", ".")


def main():
    #26.1+ is not obfuscated: without a mappings file, the named (Mojang) class names are the runtime names
    mapping = load_named_to_intermediary(sys.argv[1]) if len(sys.argv) > 1 else None
    injected = {}
    mixins_common, mixins_client = [], []
    out_dir = ROOT / "src/fabric/java/mekanism/fabric/mixin/inject"
    out_dir.mkdir(parents=True, exist_ok=True)
    for f in out_dir.glob("*.java"):
        f.unlink()
    for target, is_iface, generics, ifaces, client, manual in SPEC:
        inter = target if mapping is None else mapping.get(target)
        if inter is None:
            if target.startswith("com/mojang/"):
                inter = target  # not obfuscated
            else:
                sys.exit(f"No intermediary name for {target}")
        injected.setdefault(inter, []).extend(signature(i) for i in ifaces)
        if manual:
            continue
        simple = target.split("/")[-1].replace("$", "_")
        name = simple + "InjectMixin"
        type_params = BOUNDS.get(target, generics)
        decl_generics = f"<{type_params}>" if type_params else ""
        iface_list = ", ".join(java_name(i) for i in ifaces)
        kind = "interface" if is_iface else "abstract class"
        relation = "extends" if is_iface else "implements"
        src = (
            "package mekanism.fabric.mixin.inject;\n\n"
            "import org.spongepowered.asm.mixin.Mixin;\n\n"
            "/** GENERATED by scripts/gen_injections.py - do not edit. Applies NeoForge-style extension interfaces at runtime. */\n"
            f"@Mixin({java_name(target)}.class)\n"
            f"public {kind} {name}{decl_generics} {relation} {iface_list} {{\n}}\n"
        )
        (out_dir / f"{name}.java").write_text(src, encoding="utf-8", newline="\n")
        (mixins_client if client else mixins_common).append("inject." + name)

    fmj_path = ROOT / "src/fabric/resources/fabric.mod.json"
    fmj = json.loads(fmj_path.read_text(encoding="utf-8"))
    fmj.setdefault("custom", {})["loom:injected_interfaces"] = injected
    fmj_path.write_text(json.dumps(fmj, indent=2) + "\n", encoding="utf-8", newline="\n")

    mix_path = ROOT / "src/fabric/resources/mekanism_fabric.mixins.json"
    mix = json.loads(mix_path.read_text(encoding="utf-8"))
    mix["mixins"] = sorted([m for m in mix.get("mixins", []) if not m.startswith("inject.")] + mixins_common)
    mix["client"] = sorted([m for m in mix.get("client", []) if not m.startswith("inject.")] + mixins_client)
    mix_path.write_text(json.dumps(mix, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"gen_injections: {len(injected)} targets, {len(mixins_common)} common + {len(mixins_client)} client mixins")


if __name__ == "__main__":
    main()
