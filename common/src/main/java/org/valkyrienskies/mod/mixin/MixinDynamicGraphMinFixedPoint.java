package org.valkyrienskies.mod.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import net.minecraft.world.level.lighting.LeveledPriorityQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DynamicGraphMinFixedPoint.class)
public abstract class MixinDynamicGraphMinFixedPoint {

    @Final
    @Shadow
    private LeveledPriorityQueue priorityQueue;
    @Final
    @Shadow
    private Long2ByteMap computedLevels;
    @Final
    @Shadow
    protected int levelCount;
    @Shadow
    private volatile boolean hasWork;

    @Shadow
    protected abstract int getLevel(long var1);
    @Shadow
    protected abstract void setLevel(long var1, int var3);
    @Shadow
    protected abstract void checkNeighborsAfterUpdate(long var1, int var3, boolean var4);
    @Shadow
    protected abstract void checkEdge(long l, long m, int i, boolean bl);
    @Shadow
    protected abstract boolean isSource(long l);
    @Shadow
    protected abstract int getComputedLevel(long var1, long var3, int var5);
    @Shadow
    protected abstract int computeLevelFromNeighbor(long var1, long var3, int var5);
    @Shadow
    private void checkEdge(long l, long m, int i, int j, int k, boolean bl) { };

    /*@WrapOperation(method = "checkNode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/DynamicGraphMinFixedPoint;checkEdge(JJIZ)V"))
    private void preventCascadeInShipyards(DynamicGraphMinFixedPoint instance, long l, long m, int i, boolean bl,
        Operation<Void> original) {
        ChunkPos pos = new ChunkPos(l);
        if (valkyrienskies$isChunkInShipyard(pos.x, pos.z)) {
            original.call(instance, l, 34L, i, bl);
        } else {
            original.call(instance, l, m, i, bl);
        }
    }*/

    @WrapMethod(method = "runUpdates")
    protected final int runUpdatesLimitedLevel(int i, Operation<Integer> original) {
        if (this.priorityQueue.isEmpty()) {
            return i;
        }
        while (!this.priorityQueue.isEmpty() && i > 0) {
            --i;
            long l = this.priorityQueue.removeFirstLong();

            int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

            int j = Mth.clamp(this.getLevel(l), 0, levelCount_ - 1);
            int k = this.computedLevels.remove(l) & 0xFF;
            if (k < j) {
                this.setLevel(l, k);
                this.checkNeighborsAfterUpdate(l, k, true);
                continue;
            }
            if (k <= j) continue;
            this.setLevel(l, levelCount_ - 1);

            if (k != levelCount_ - 1) {
                this.priorityQueue.enqueue(l, valkyrienskies$calculatePriorityWithCustomLevelCount(levelCount_ - 1, k, levelCount_));
                this.computedLevels.put(l, (byte)k);
            }
            this.checkNeighborsAfterUpdate(l, j, false);
        }
        this.hasWork = !this.priorityQueue.isEmpty();
        return i;
    }

    @WrapMethod(method = "removeFromQueue")
    protected void removeFromQueue(long l, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

        int i = this.computedLevels.remove(l) & 0xFF;
        if (i == 255) {
            return;
        }
        int j = this.getLevel(l);
        int k = valkyrienskies$calculatePriorityWithCustomLevelCount(j, i, levelCount_);

        this.priorityQueue.dequeue(l, k, levelCount_);
        this.hasWork = !this.priorityQueue.isEmpty();
    }

    @Unique
    private int valkyrienskies$calculatePriorityWithCustomLevelCount(int i, int j, int levelCount_) {
        return Math.min(Math.min(i, j), levelCount_ - 1);
    }

    @WrapMethod(method = "checkNode")
    protected void checkNode1(long l, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

        this.checkEdge(l, l, levelCount_ - 1, false);
    }

    @WrapMethod(method = "checkEdge(JJIIIZ)V")
    protected void checkEdge1(long l, long m, int i, int j, int k, boolean bl, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(l)) ? 43 : this.levelCount;

        boolean bl2;
        if (this.isSource(m)) {
            return;
        }
        i = Mth.clamp(i, 0, levelCount_ - 1);
        j = Mth.clamp(j, 0, levelCount_ - 1);
        boolean bl3 = bl2 = k == 255;
        if (bl2) {
            k = j;
        }
        int n = bl ? Math.min(k, i) : Mth.clamp(this.getComputedLevel(m, l, i), 0, levelCount_ - 1);
        int o = valkyrienskies$calculatePriorityWithCustomLevelCount(j, k, levelCount_);
        if (j != n) {
            int p = valkyrienskies$calculatePriorityWithCustomLevelCount(j, n, levelCount_);
            if (o != p && !bl2) {
                this.priorityQueue.dequeue(m, o, p);
            }
            this.priorityQueue.enqueue(m, p);
            this.computedLevels.put(m, (byte)n);
        } else if (!bl2) {
            this.priorityQueue.dequeue(m, o, levelCount_);
            this.computedLevels.remove(m);
        }
    }

    @WrapMethod(method = "checkNeighbor")
    protected final void checkNeighbor(long l, long m, int i, boolean bl, Operation<Void> original) {
        int levelCount_ = (valkyrienskies$isChunkInShipyard(m)) ? 43 : this.levelCount;

        int j = this.computedLevels.get(m) & 0xFF;
        int k = Mth.clamp(this.computeLevelFromNeighbor(l, m, i), 0, levelCount_ - 1);
        if (bl) {
            this.checkEdge(l, m, k, this.getLevel(m), j, bl);
        } else {
            boolean bl2 = j == 255;
            int n = bl2 ? Mth.clamp(this.getLevel(m), 0, levelCount_ - 1) : j;
            if (k == n) {
                this.checkEdge(l, m, levelCount_ - 1, bl2 ? n : this.getLevel(m), j, bl);
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
