package xyz.memothelemo.deathchest.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.deathchest.DeathChestMod;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    public abstract @Nullable BlockEntity getBlockEntity(BlockPos blockPos);

    @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true)
    public void dc$rejectRemoveBlockEntityForDeathChests(BlockPos blockPos, CallbackInfo ci) {
        if (dc$chestFromBlockPos(blockPos) == null) return;
        if (DeathChestMod.data.getDataForChest(blockPos) != null) {
            LOGGER.debug("[remove_block_entity] {} -> rejected", blockPos);
            ci.cancel();
        }
    }

    @Inject(method = "removeBlock", at = @At("HEAD"), cancellable = true)
    public void dc$rejectRemoveBlockForDeathChests(
        CallbackInfoReturnable<Boolean> cir,
        @Local(argsOnly = true) BlockPos blockPos
    ) {
        if (dc$chestFromBlockPos(blockPos) == null) return;
        if (DeathChestMod.data.getDataForChest(blockPos) != null) {
            LOGGER.debug("[remove_block] {} -> rejected", blockPos);
            cir.setReturnValue(false);
        }
    }

    @Unique
    private @Nullable ChestBlockEntity dc$chestFromBlockPos(BlockPos position) {
        BlockEntity entity = this.getBlockEntity(position);
        if (!(entity instanceof ChestBlockEntity)) return null;
        return (ChestBlockEntity) entity;
    }
}
