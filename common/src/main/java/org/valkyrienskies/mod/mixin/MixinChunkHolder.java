package org.valkyrienskies.mod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkHolder.LevelChangeListener;
import net.minecraft.server.level.ChunkHolder.PlayerProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {
    @Shadow
    private int oldTicketLevel;
    @Shadow
    private int queueLevel;

    @Shadow
    public abstract ChunkPos getPos();

    @Inject(method = "<init>", at = @At("TAIL"))
    void replaceOldTicketLevel(ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor,
        LevelLightEngine levelLightEngine, LevelChangeListener levelChangeListener, PlayerProvider playerProvider,
        CallbackInfo ci) {
        if (valkyrienskies$isChunkInShipyard(chunkPos.x, chunkPos.z)) {
            oldTicketLevel = 43; // ? 43
            queueLevel = 43; // ? 43
        }
    }

    @WrapOperation(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkLevel;isLoaded(I)Z"))
    boolean modifyIsLoaded(int i, Operation<Boolean> original) {
        ChunkPos cp = this.getPos();
        if (valkyrienskies$isChunkInShipyard(cp.x, cp.z)) {
            return i < 43;
        } else {
            return original.call(i);
        }
    }

    @Unique
    boolean valkyrienskies$isChunkInShipyard(long x, long z) {
        long cx = x / 256;
        long cz = z / 256;
        return (-7000 <= cx ? cx < 7001 : false) & (3000 <= cz ? cz < 7001 : false);
    }

    @Unique
    boolean valkyrienskies$isChunkInShipyard(long l) {
        ChunkPos cp = new ChunkPos(l);
        return valkyrienskies$isChunkInShipyard(cp.x, cp.z);
    }
}
