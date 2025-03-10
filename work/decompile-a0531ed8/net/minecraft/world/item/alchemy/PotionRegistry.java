package net.minecraft.world.item.alchemy;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class PotionRegistry implements FeatureElement {

    public static final Codec<Holder<PotionRegistry>> CODEC = BuiltInRegistries.POTION.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<PotionRegistry>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.POTION);
    private final String name;
    private final List<MobEffect> effects;
    private FeatureFlagSet requiredFeatures;

    public PotionRegistry(String s, MobEffect... amobeffect) {
        this.requiredFeatures = FeatureFlags.VANILLA_SET;
        this.name = s;
        this.effects = List.of(amobeffect);
    }

    public PotionRegistry requiredFeatures(FeatureFlag... afeatureflag) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public List<MobEffect> getEffects() {
        return this.effects;
    }

    public String name() {
        return this.name;
    }

    public boolean hasInstantEffects() {
        Iterator iterator = this.effects.iterator();

        MobEffect mobeffect;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            mobeffect = (MobEffect) iterator.next();
        } while (!((MobEffectList) mobeffect.getEffect().value()).isInstantenous());

        return true;
    }
}
