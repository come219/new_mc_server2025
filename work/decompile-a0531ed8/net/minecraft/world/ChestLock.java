package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public record ChestLock(CriterionConditionItem predicate) {

    public static final ChestLock NO_LOCK = new ChestLock(CriterionConditionItem.a.item().build());
    public static final Codec<ChestLock> CODEC = CriterionConditionItem.CODEC.xmap(ChestLock::new, ChestLock::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack itemstack) {
        return this.predicate.test(itemstack);
    }

    public void addToTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        if (this != ChestLock.NO_LOCK) {
            DataResult<NBTBase> dataresult = ChestLock.CODEC.encode(this, holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), new NBTTagCompound());

            dataresult.result().ifPresent((nbtbase) -> {
                nbttagcompound.put("lock", nbtbase);
            });
        }

    }

    public static ChestLock fromTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        if (nbttagcompound.contains("lock", 10)) {
            DataResult<Pair<ChestLock, NBTBase>> dataresult = ChestLock.CODEC.decode(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound.get("lock"));

            if (dataresult.isSuccess()) {
                return (ChestLock) ((Pair) dataresult.getOrThrow()).getFirst();
            }
        }

        return ChestLock.NO_LOCK;
    }
}
