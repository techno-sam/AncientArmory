package com.slimeist.ancient_armory;

import com.slimeist.ancient_armory.entities.PendulumEntity;
import com.slimeist.ancient_armory.entities.ThorHammerEntity;
import com.slimeist.ancient_armory.items.CustomModelItem;
import com.slimeist.ancient_armory.items.ThorHammerItem;
import com.slimeist.server_mobs.api.server_rendering.model.ServerEntityModelLoader;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.SERVER)
public class AncientArmory implements DedicatedServerModInitializer {

    public static final String MOD_ID = "ancient_armory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //ITEMS
    public static final ThorHammerItem THOR_HAMMER_ITEM = new ThorHammerItem(
            new FabricItemSettings()
                    .group(ItemGroup.COMBAT)
                    .maxDamage(500),
            Items.IRON_SWORD
    );

    //ENTITIES
    public static final EntityType<ThorHammerEntity> THOR_HAMMER = Registry.register(
        Registry.ENTITY_TYPE,
        id("thor_hammer"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ThorHammerEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(8).trackedUpdateRate(20).build()
    );
    private static final ServerEntityModelLoader THOR_HAMMER_LOADER = new ServerEntityModelLoader(THOR_HAMMER, false);
    static {
        ThorHammerEntity.setBakedModelSupplier(THOR_HAMMER_LOADER::getBakedModel);
    }

    public static final EntityType<PendulumEntity> PENDULUM = Registry.register(
        Registry.ENTITY_TYPE,
        id("pendulum"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, PendulumEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(8).trackedUpdateRate(20).build()
    );
    private static final ServerEntityModelLoader PENDULUM_LOADER = new ServerEntityModelLoader(PENDULUM, false);
    static {
        PendulumEntity.setBakedModelSupplier(PENDULUM_LOADER::getBakedModel);
    }

    private static <I extends Item & PolymerItem & CustomModelItem> void registerCustomModelItem(I item, String name) {
        PolymerModelData data = PolymerRPUtils.requestModel(item.getPolymerItem(new ItemStack(item, 1), null), id("item/" + name));
        item.setCustomModelData(data.value());
        Registry.register(Registry.ITEM, id(name), item);
    }

    @Override
    public void onInitializeServer() {
        if (PolymerRPUtils.addAssetSource(MOD_ID)) {
            LOGGER.info("Successfully marked as asset source");
        } else {
            LOGGER.error("Failed to mark as asset source");
        }
        PolymerRPUtils.markAsRequired();

        registerCustomModelItem(THOR_HAMMER_ITEM, "thor_hammer");
        {
            PolymerModelData data = PolymerRPUtils.requestModel(THOR_HAMMER_ITEM.getPolymerItem(new ItemStack(THOR_HAMMER_ITEM, 1), null), id("item/thor_hammer_charged"));
            THOR_HAMMER_ITEM.setChargedModelData(data.value());
        }

        PolymerEntityUtils.registerType(THOR_HAMMER);
        PolymerEntityUtils.registerType(PENDULUM);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
