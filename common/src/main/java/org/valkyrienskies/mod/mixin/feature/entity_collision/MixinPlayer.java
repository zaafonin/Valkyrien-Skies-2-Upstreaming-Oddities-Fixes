package org.valkyrienskies.mod.mixin.feature.entity_collision;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(Player.class)
public abstract class MixinPlayer implements IEntityDraggingInformationProvider {
    // Allow players to crouch walk on ships
    @WrapOperation(
        method = "maybeBackOffFromEdge",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"
        )
    )
    private boolean checkCollisionWithShips(Level level, Entity entity, AABB aABB, Operation<Boolean> original) {
        boolean noWorldCollision = original.call(level, entity, aABB);
        if (noWorldCollision) {
            if (entity instanceof final IEntityDraggingInformationProvider dragProvider) {
                Long lastId = dragProvider.getDraggingInformation().getLastShipStoodOn();
                if (lastId != null) {
                    final Ship shipDraggedBy = VSGameUtilsKt.getAllShips(level).getById(lastId);
                    if (shipDraggedBy != null) {
                        // While VS2 handles player-to-ship collisions decently, edge (huh) cases are not reliable.
                        // Because of that we will be more conservative in stepping off the edge.
                        AABB reducedAABB = aABB.inflate(-0.1, 0.0, -0.1);
                        AABB shipAABB = VectorConversionsMCKt.toMinecraft(VectorConversionsMCKt.toJOML(reducedAABB).transform(shipDraggedBy.getWorldToShip()));
                        for (VoxelShape voxelShape : level.getBlockCollisions(entity, shipAABB)) {
                            if (voxelShape.isEmpty()) continue;
                            return false;
                        }
                    }
                }
            }
        }
        return noWorldCollision;
    }
}
