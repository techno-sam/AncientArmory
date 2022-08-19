package com.slimeist.ancient_armory.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.slimeist.ancient_armory.AncientArmory;
import com.slimeist.ancient_armory.entities.ThorHammerEntity;
import eu.pb4.polymer.api.networking.PolymerPacketUtils;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ThorHammerItem extends SimpleCustomModelItem implements Vanishable {
    public static final int field_30926 = 10;
    public static final float field_30927 = 8.0f;
    public static final float field_30928 = 2.5f;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
    protected static final UUID ATTACK_KNOCKBACK_MODIFIER_ID = UUID.fromString("F7C1C1D3-34B7-4255-AABA-2D68F2A96D94");
    private int chargedData = -1;

    public ThorHammerItem(Settings settings, Item polymerItem) {
        super(settings, polymerItem);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 5.0, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", -2.1f, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(ATTACK_KNOCKBACK_MODIFIER_ID, "Tool modifier", 5.0f, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity playerEntity)) {
            return;
        }
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        if (i < 10) {
            return;
        }
        if (!world.isClient) {
            stack.damage(1, playerEntity, p -> p.sendToolBreakStatus(user.getActiveHand()));
            ThorHammerEntity hammerEntity = ThorHammerEntity.thrown(world, playerEntity, stack);
            hammerEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f, 2.5f + 0 * 0.5f, 1.0f);
            if (playerEntity.getAbilities().creativeMode) {
                hammerEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
            world.spawnEntity(hammerEntity);
            world.playSoundFromEntity(null, hammerEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!playerEntity.getAbilities().creativeMode) {
                playerEntity.getInventory().removeOne(stack);
            }
        }
        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            return TypedActionResult.fail(itemStack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.world.playSoundFromEntity(null, target, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 0.5f);

        EntityAttributeInstance knockbackInstance = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, (instance) -> {});
        stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_KNOCKBACK).forEach(knockbackInstance::addTemporaryModifier);

        double i = knockbackInstance.getValue();
        target.takeKnockback((float)i * 0.5f, MathHelper.sin(attacker.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(attacker.getYaw() * ((float)Math.PI / 180)));
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if ((double)state.getHardness(world, pos) != 0.0) {
            stack.damage(2, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.attributeModifiers;
        }
        return super.getAttributeModifiers(slot);
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        if (i == 10) {
            user.world.playSoundFromEntity(null, user, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.7f, 0.8f);
            stack.getOrCreateSubNbt("charged");
        } else if (i < 10) {
            stack.removeSubNbt("charged");
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!selected) {
            stack.removeSubNbt("charged");
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    public void setChargedModelData(int chargedData) {
        this.chargedData = chargedData;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (player != null && this.chargedData != -1 && player.isUsingItem()) {
            int i = this.getMaxUseTime(itemStack) - player.getItemUseTimeLeft();
            if (i >= 10) {
                return this.chargedData;
            }
        }
        return super.getPolymerCustomModelData(itemStack, player);
    }
}
