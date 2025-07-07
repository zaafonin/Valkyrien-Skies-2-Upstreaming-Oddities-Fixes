package org.valkyrienskies.mod.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkTracker.class)
public abstract class MixinChunkTracker extends DynamicGraphMinFixedPoint {
    protected MixinChunkTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @WrapMethod(method = "checkNeighborsAfterUpdate")
    protected void checkNeighborsAfterUpdate_(long l, int i, boolean bl, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

        if (!bl || i < levelCount_ - 2) {
            ChunkPos chunkPos = new ChunkPos(l);
            int j = chunkPos.x;
            int k = chunkPos.z;

            for(int m = -1; m <= 1; ++m) {
                for(int n = -1; n <= 1; ++n) {
                    long o = ChunkPos.asLong(j + m, k + n);
                    if (o != l) {
                        this.checkNeighbor(l, o, i, bl);
                    }
                }
            }

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
