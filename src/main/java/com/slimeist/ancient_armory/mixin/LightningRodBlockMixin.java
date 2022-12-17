package com.slimeist.ancient_armory.mixin;

import com.slimeist.ancient_armory.AncientArmory;
import com.slimeist.ancient_armory.entities.ThorHammerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.RodBlock;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningRodBlock.class)
public abstract class LightningRodBlockMixin extends RodBlock {
    private LightningRodBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        if (world.getBlockState(pos.down()).isIn(BlockTags.ANVIL)) {
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getStack();
                if (stack.isOf(Items.DIAMOND)) {
                    stack.decrement(1);
                    if (!stack.isEmpty()) {
                        itemEntity.setStack(stack);
                    } else {
                        itemEntity.kill();
                    }
                    // spawn a thor hammer
                    ThorHammerEntity hammer = null;
                    if (world instanceof ServerWorld serverWorld) {
                        Entity thrower = serverWorld.getEntity(itemEntity.getThrower());
                        if (thrower instanceof LivingEntity livingThrower)
                            hammer = ThorHammerEntity.thrown(world, livingThrower, new ItemStack(AncientArmory.THOR_HAMMER_ITEM));
                    }
                    if (hammer == null)
                        hammer = new ThorHammerEntity(AncientArmory.THOR_HAMMER, world);
                    hammer.setPosition(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
                    hammer.preventStrike();
                    world.spawnEntity(hammer);

                    // spawn lightning
                    LightningEntity decorativeLightning = EntityType.LIGHTNING_BOLT.create(world);
                    if (decorativeLightning != null) {
                        decorativeLightning.setCosmetic(true);
                        decorativeLightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                        world.spawnEntity(decorativeLightning);
                        world.playSound(null, pos, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 5.0f, 1.0f);
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}
