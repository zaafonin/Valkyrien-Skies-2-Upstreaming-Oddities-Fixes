package org.valkyrienskies.mod.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SectionTracker.class)
public abstract class MixinSectionTracker extends DynamicGraphMinFixedPoint {
    protected MixinSectionTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @WrapMethod(method = "checkNeighborsAfterUpdate")
    protected void checkNeighborsAfterUpdate_(long l, int i, boolean bl, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

        if (bl && i >= levelCount_ - 2) {
            return;
        }
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int m = -1; m <= 1; ++m) {
                    long n = SectionPos.offset(l, j, k, m);
                    if (n == l) continue;
                    this.checkNeighbor(l, n, i, bl);
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
