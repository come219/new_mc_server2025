package net.minecraft.world.phys;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;

public class MovingObjectPositionBlock extends MovingObjectPosition {

    private final EnumDirection direction;
    private final BlockPosition blockPos;
    private final boolean miss;
    private final boolean inside;
    private final boolean worldBorderHit;

    public static MovingObjectPositionBlock miss(Vec3D vec3d, EnumDirection enumdirection, BlockPosition blockposition) {
        return new MovingObjectPositionBlock(true, vec3d, enumdirection, blockposition, false, false);
    }

    public MovingObjectPositionBlock(Vec3D vec3d, EnumDirection enumdirection, BlockPosition blockposition, boolean flag) {
        this(false, vec3d, enumdirection, blockposition, flag, false);
    }

    public MovingObjectPositionBlock(Vec3D vec3d, EnumDirection enumdirection, BlockPosition blockposition, boolean flag, boolean flag1) {
        this(false, vec3d, enumdirection, blockposition, flag, flag1);
    }

    private MovingObjectPositionBlock(boolean flag, Vec3D vec3d, EnumDirection enumdirection, BlockPosition blockposition, boolean flag1, boolean flag2) {
        super(vec3d);
        this.miss = flag;
        this.direction = enumdirection;
        this.blockPos = blockposition;
        this.inside = flag1;
        this.worldBorderHit = flag2;
    }

    public MovingObjectPositionBlock withDirection(EnumDirection enumdirection) {
        return new MovingObjectPositionBlock(this.miss, this.location, enumdirection, this.blockPos, this.inside, this.worldBorderHit);
    }

    public MovingObjectPositionBlock withPosition(BlockPosition blockposition) {
        return new MovingObjectPositionBlock(this.miss, this.location, this.direction, blockposition, this.inside, this.worldBorderHit);
    }

    public MovingObjectPositionBlock hitBorder() {
        return new MovingObjectPositionBlock(this.miss, this.location, this.direction, this.blockPos, this.inside, true);
    }

    public BlockPosition getBlockPos() {
        return this.blockPos;
    }

    public EnumDirection getDirection() {
        return this.direction;
    }

    @Override
    public MovingObjectPosition.EnumMovingObjectType getType() {
        return this.miss ? MovingObjectPosition.EnumMovingObjectType.MISS : MovingObjectPosition.EnumMovingObjectType.BLOCK;
    }

    public boolean isInside() {
        return this.inside;
    }

    public boolean isWorldBorderHit() {
        return this.worldBorderHit;
    }
}
