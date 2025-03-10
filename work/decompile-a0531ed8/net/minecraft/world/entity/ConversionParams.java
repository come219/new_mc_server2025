package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.world.scores.ScoreboardTeam;

public record ConversionParams(ConversionType type, boolean keepEquipment, boolean preserveCanPickUpLoot, @Nullable ScoreboardTeam team) {

    public static ConversionParams single(EntityInsentient entityinsentient, boolean flag, boolean flag1) {
        return new ConversionParams(ConversionType.SINGLE, flag, flag1, entityinsentient.getTeam());
    }

    @FunctionalInterface
    public interface a<T extends EntityInsentient> {

        void finalizeConversion(T t0);
    }
}
