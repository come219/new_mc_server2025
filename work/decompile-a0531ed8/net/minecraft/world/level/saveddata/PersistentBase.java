package net.minecraft.world.level.saveddata;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;

public abstract class PersistentBase {

    private boolean dirty;

    public PersistentBase() {}

    public abstract NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a);

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public NBTTagCompound save(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.put("data", this.save(new NBTTagCompound(), holderlookup_a));
        GameProfileSerializer.addCurrentDataVersion(nbttagcompound);
        this.setDirty(false);
        return nbttagcompound;
    }

    public static record a<T extends PersistentBase>(Supplier<T> constructor, BiFunction<NBTTagCompound, HolderLookup.a, T> deserializer, DataFixTypes type) {

    }
}
