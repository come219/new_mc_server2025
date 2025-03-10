package net.minecraft.core;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface IRegistryWritable<T> extends IRegistry<T> {

    Holder.c<T> register(ResourceKey<T> resourcekey, T t0, RegistrationInfo registrationinfo);

    void bindTag(TagKey<T> tagkey, List<Holder<T>> list);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}
