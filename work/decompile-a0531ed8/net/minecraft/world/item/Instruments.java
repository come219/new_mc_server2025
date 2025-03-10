package net.minecraft.world.item;

import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;

public interface Instruments {

    int GOAT_HORN_RANGE_BLOCKS = 256;
    float GOAT_HORN_DURATION = 7.0F;
    ResourceKey<Instrument> PONDER_GOAT_HORN = create("ponder_goat_horn");
    ResourceKey<Instrument> SING_GOAT_HORN = create("sing_goat_horn");
    ResourceKey<Instrument> SEEK_GOAT_HORN = create("seek_goat_horn");
    ResourceKey<Instrument> FEEL_GOAT_HORN = create("feel_goat_horn");
    ResourceKey<Instrument> ADMIRE_GOAT_HORN = create("admire_goat_horn");
    ResourceKey<Instrument> CALL_GOAT_HORN = create("call_goat_horn");
    ResourceKey<Instrument> YEARN_GOAT_HORN = create("yearn_goat_horn");
    ResourceKey<Instrument> DREAM_GOAT_HORN = create("dream_goat_horn");

    private static ResourceKey<Instrument> create(String s) {
        return ResourceKey.create(Registries.INSTRUMENT, MinecraftKey.withDefaultNamespace(s));
    }

    static void bootstrap(BootstrapContext<Instrument> bootstrapcontext) {
        register(bootstrapcontext, Instruments.PONDER_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(0), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.SING_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(1), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.SEEK_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(2), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.FEEL_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(3), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.ADMIRE_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(4), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.CALL_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(5), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.YEARN_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(6), 7.0F, 256.0F);
        register(bootstrapcontext, Instruments.DREAM_GOAT_HORN, (Holder) SoundEffects.GOAT_HORN_SOUND_VARIANTS.get(7), 7.0F, 256.0F);
    }

    static void register(BootstrapContext<Instrument> bootstrapcontext, ResourceKey<Instrument> resourcekey, Holder<SoundEffect> holder, float f, float f1) {
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("instrument", resourcekey.location()));

        bootstrapcontext.register(resourcekey, new Instrument(holder, f, f1, ichatmutablecomponent));
    }
}
