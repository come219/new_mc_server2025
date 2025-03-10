package net.minecraft.server.level;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum ParticleStatus implements OptionEnum {

    ALL(0, "options.particles.all"), DECREASED(1, "options.particles.decreased"), MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID = ByIdMap.continuous(ParticleStatus::getId, values(), ByIdMap.a.WRAP);
    private final int id;
    private final String key;

    private ParticleStatus(final int i, final String s) {
        this.id = i;
        this.key = s;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ParticleStatus byId(int i) {
        return (ParticleStatus) ParticleStatus.BY_ID.apply(i);
    }
}
