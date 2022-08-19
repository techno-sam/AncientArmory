package com.slimeist.ancient_armory.mixin;

import com.slimeist.ancient_armory.AncientArmory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.ImpalingEnchantment;
import net.minecraft.enchantment.LoyaltyEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void isAcceptable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment this_ = (Enchantment) (Object) this;
        if (this_ instanceof LoyaltyEnchantment) {
            if (stack.isOf(AncientArmory.THOR_HAMMER_ITEM)) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
