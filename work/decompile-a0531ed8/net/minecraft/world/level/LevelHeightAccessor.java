package net.minecraft.world.level;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;

public interface LevelHeightAccessor {

    int getHeight();

    int getMinY();

    default int getMaxY() {
        return this.getMinY() + this.getHeight() - 1;
    }

    default int getSectionsCount() {
        return this.getMaxSectionY() - this.getMinSectionY() + 1;
    }

    default int getMinSectionY() {
        return SectionPosition.blockToSectionCoord(this.getMinY());
    }

    default int getMaxSectionY() {
        return SectionPosition.blockToSectionCoord(this.getMaxY());
    }

    default boolean isInsideBuildHeight(int i) {
        return i >= this.getMinY() && i <= this.getMaxY();
    }

    default boolean isOutsideBuildHeight(BlockPosition blockposition) {
        return this.isOutsideBuildHeight(blockposition.getY());
    }

    default boolean isOutsideBuildHeight(int i) {
        return i < this.getMinY() || i > this.getMaxY();
    }

    default int getSectionIndex(int i) {
        return this.getSectionIndexFromSectionY(SectionPosition.blockToSectionCoord(i));
    }

    default int getSectionIndexFromSectionY(int i) {
        return i - this.getMinSectionY();
    }

    default int getSectionYFromSectionIndex(int i) {
        return i + this.getMinSectionY();
    }

    static LevelHeightAccessor create(final int i, final int j) {
        return new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return j;
            }

            @Override
            public int getMinY() {
                return i;
            }
        };
    }
}
