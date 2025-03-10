package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldown {

    public final Map<MinecraftKey, ItemCooldown.Info> cooldowns = Maps.newHashMap();
    public int tickCount;

    public ItemCooldown() {}

    public boolean isOnCooldown(ItemStack itemstack) {
        return this.getCooldownPercent(itemstack, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(ItemStack itemstack, float f) {
        MinecraftKey minecraftkey = this.getCooldownGroup(itemstack);
        ItemCooldown.Info itemcooldown_info = (ItemCooldown.Info) this.cooldowns.get(minecraftkey);

        if (itemcooldown_info != null) {
            float f1 = (float) (itemcooldown_info.endTime - itemcooldown_info.startTime);
            float f2 = (float) itemcooldown_info.endTime - ((float) this.tickCount + f);

            return MathHelper.clamp(f2 / f1, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Entry<MinecraftKey, ItemCooldown.Info>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<MinecraftKey, ItemCooldown.Info> entry = (Entry) iterator.next();

                if (((ItemCooldown.Info) entry.getValue()).endTime <= this.tickCount) {
                    iterator.remove();
                    this.onCooldownEnded((MinecraftKey) entry.getKey());
                }
            }
        }

    }

    public MinecraftKey getCooldownGroup(ItemStack itemstack) {
        UseCooldown usecooldown = (UseCooldown) itemstack.get(DataComponents.USE_COOLDOWN);
        MinecraftKey minecraftkey = BuiltInRegistries.ITEM.getKey(itemstack.getItem());

        return usecooldown == null ? minecraftkey : (MinecraftKey) usecooldown.cooldownGroup().orElse(minecraftkey);
    }

    public void addCooldown(ItemStack itemstack, int i) {
        this.addCooldown(this.getCooldownGroup(itemstack), i);
    }

    public void addCooldown(MinecraftKey minecraftkey, int i) {
        this.cooldowns.put(minecraftkey, new ItemCooldown.Info(this.tickCount, this.tickCount + i));
        this.onCooldownStarted(minecraftkey, i);
    }

    public void removeCooldown(MinecraftKey minecraftkey) {
        this.cooldowns.remove(minecraftkey);
        this.onCooldownEnded(minecraftkey);
    }

    protected void onCooldownStarted(MinecraftKey minecraftkey, int i) {}

    protected void onCooldownEnded(MinecraftKey minecraftkey) {}

    public static record Info(int startTime, int endTime) {

    }
}
