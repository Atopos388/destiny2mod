package com.Atopos.destiny2mod.item.custom;

import com.Atopos.destiny2mod.client.renderer.item.PerfectRetrogradeItemRenderer;
import com.Atopos.destiny2mod.entity.custom.PerfectRetrogradeProjectile;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class PerfectRetrogradeItem extends DestinyWeaponItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PerfectRetrogradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        startBurst(stack, player);
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            startBurst(stack, player);
        }
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return false;
    }

    private void startBurst(ItemStack stack, Player player) {
        if (player.getCooldowns().isOnCooldown(this)) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt("BurstShotsLeft") <= 0) {
            tag.putInt("BurstShotsLeft", 3);
            tag.putInt("BurstTimer", 0);
            float delayMult = DestinyWeaponItem.getTotalFireDelayMultiplier(stack, player);
            player.getCooldowns().addCooldown(this, Math.max(5, Math.round(25 * delayMult)));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!isSelected || !(entity instanceof Player player) || level.isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        int shotsLeft = tag.getInt("BurstShotsLeft");
        if (shotsLeft > 0) {
            int timer = tag.getInt("BurstTimer");
            if (timer <= 0) {
                fireProjectile(level, player, stack);
                tag.putInt("BurstShotsLeft", shotsLeft - 1);
                float delayMult = DestinyWeaponItem.getTotalFireDelayMultiplier(stack, player);
                tag.putInt("BurstTimer", Math.max(1, Math.round(4 * delayMult)));
            } else {
                tag.putInt("BurstTimer", timer - 1);
            }
        }
    }

    private void fireProjectile(Level level, Player player, ItemStack gunStack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 1.8F);
        PerfectRetrogradeProjectile projectile = new PerfectRetrogradeProjectile(level, player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        projectile.setWeaponPerks(gunStack);
        float speedMult = DestinyWeaponItem.getTotalSpeedMultiplier(gunStack);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F * speedMult, 0.5F);
        level.addFreshEntity(projectile);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private PerfectRetrogradeItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new PerfectRetrogradeItemRenderer();
                return this.renderer;
            }
        });
    }
}