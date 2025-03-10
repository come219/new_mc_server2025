package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.world.entity.npc.EntityVillager;
import org.bukkit.craftbukkit.entity.CraftVillager.CraftReputationType;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.VillagerReputationChangeEvent;
// CraftBukkit end

public class Reputation {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DISCARD_THRESHOLD = 2;
    private final Map<UUID, Reputation.a> gossips = Maps.newHashMap();

    // CraftBukkit start - store reference to villager entity
    private final EntityVillager villager;

    public Reputation(EntityVillager villager) {
        this.villager = villager;
    }
    // CraftBukkit end

    @VisibleForDebug
    public Map<UUID, Object2IntMap<ReputationType>> getGossipEntries() {
        Map<UUID, Object2IntMap<ReputationType>> map = Maps.newHashMap();

        this.gossips.keySet().forEach((uuid) -> {
            Reputation.a reputation_a = (Reputation.a) this.gossips.get(uuid);

            map.put(uuid, reputation_a.entries);
        });
        return map;
    }

    public void decay() {
        Iterator<Map.Entry<UUID, Reputation.a>> iterator = this.gossips.entrySet().iterator(); // CraftBukkit - iterate over entries instead of values to access entity UUID

        while (iterator.hasNext()) {
            // CraftBukkit start - pass villager and entity UUID to decay method
            Map.Entry<UUID, Reputation.a> reputation_a = iterator.next();

            reputation_a.getValue().decay(villager, reputation_a.getKey());
            if (reputation_a.getValue().isEmpty()) {
                iterator.remove();
            }
            // CraftBukkit end
        }

    }

    private Stream<Reputation.b> unpack() {
        return this.gossips.entrySet().stream().flatMap((entry) -> {
            return ((Reputation.a) entry.getValue()).unpack((UUID) entry.getKey());
        });
    }

    private Collection<Reputation.b> selectGossipsForTransfer(RandomSource randomsource, int i) {
        List<Reputation.b> list = this.unpack().toList();

        if (list.isEmpty()) {
            return Collections.emptyList();
        } else {
            int[] aint = new int[list.size()];
            int j = 0;

            for (int k = 0; k < list.size(); ++k) {
                Reputation.b reputation_b = (Reputation.b) list.get(k);

                j += Math.abs(reputation_b.weightedValue());
                aint[k] = j - 1;
            }

            Set<Reputation.b> set = Sets.newIdentityHashSet();

            for (int l = 0; l < i; ++l) {
                int i1 = randomsource.nextInt(j);
                int j1 = Arrays.binarySearch(aint, i1);

                set.add((Reputation.b) list.get(j1 < 0 ? -j1 - 1 : j1));
            }

            return set;
        }
    }

    private Reputation.a getOrCreate(UUID uuid) {
        return (Reputation.a) this.gossips.computeIfAbsent(uuid, (uuid1) -> {
            return new Reputation.a();
        });
    }

    public void transferFrom(Reputation reputation, RandomSource randomsource, int i) {
        Collection<Reputation.b> collection = reputation.selectGossipsForTransfer(randomsource, i);

        collection.forEach((reputation_b) -> {
            int j = reputation_b.value - reputation_b.type.decayPerTransfer;

            if (j >= 2) {
                // CraftBukkit start - redirect to a method which fires an event before setting value
                this.set(reputation_b.target, reputation_b.type, Reputation.mergeValuesForTransfer(getReputation(reputation_b.target, Predicate.isEqual(reputation_b.type), false), j), Villager.ReputationEvent.GOSSIP);
                //this.getOrCreate(reputation_b.target).entries.mergeInt(reputation_b.type, j, Reputation::mergeValuesForTransfer);
                // CraftBukkit end
            }

        });
    }

    public int getReputation(UUID uuid, Predicate<ReputationType> predicate) {
        // CraftBukkit start - add getReputation overload with additional parameter
        return getReputation(uuid, predicate, true);
    }

    public int getReputation(UUID uuid, Predicate<ReputationType> predicate, boolean weighted) {
        // CraftBukkit end
        Reputation.a reputation_a = (Reputation.a) this.gossips.get(uuid);

        // CraftBukkit start - handle weighted parameter
        return reputation_a != null ? (weighted ? reputation_a.weightedValue(predicate) : reputation_a.unweightedValue(predicate)) : 0;
        // CraftBukkit end
    }

    public long getCountForType(ReputationType reputationtype, DoublePredicate doublepredicate) {
        return this.gossips.values().stream().filter((reputation_a) -> {
            return doublepredicate.test((double) (reputation_a.entries.getOrDefault(reputationtype, 0) * reputationtype.weight));
        }).count();
    }

    public void add(UUID uuid, ReputationType reputationtype, int i) {
        // CraftBukkit start - add change reason parameter
        add(uuid, reputationtype, i, Villager.ReputationEvent.UNSPECIFIED);
    }

    public void add(UUID uuid, ReputationType reputationtype, int i, Villager.ReputationEvent changeReason) {
        // CraftBukkit end
        Reputation.a reputation_a = this.getOrCreate(uuid);

        int oldValue = reputation_a.entries.getInt(reputationtype); // CraftBukkit - store old value
        reputation_a.entries.mergeInt(reputationtype, i, (j, k) -> {
            return this.mergeValuesForAddition(reputationtype, j, k);
        });
        // CraftBukkit start - fire reputation change event
        int newValue = reputation_a.entries.getInt(reputationtype);
        newValue = Math.max(0, Math.min(newValue, reputationtype.max));
        reputation_a.entries.replace(reputationtype, oldValue); // restore old value until the event completed processing
        VillagerReputationChangeEvent event = CraftEventFactory.callVillagerReputationChangeEvent((Villager) villager.getBukkitEntity(), uuid, changeReason, CraftReputationType.minecraftToBukkit(reputationtype), oldValue, newValue, reputationtype.max);
        if (!event.isCancelled()) {
            reputation_a.entries.replace(reputationtype, event.getNewValue());
            reputation_a.makeSureValueIsntTooLowOrTooHigh(reputationtype);
        }
        // CraftBukkit end
        if (reputation_a.isEmpty()) {
            this.gossips.remove(uuid);
        }

    }

    // CraftBukkit start
    public void set(UUID uuid, ReputationType reputationType, int i, Villager.ReputationEvent changeReason) {
        int addAmount = i - getReputation(uuid, Predicate.isEqual(reputationType), false);
        if (addAmount == 0) {
            return;
        }
        this.add(uuid, reputationType, addAmount, changeReason);
    }
    // CraftBukkit end

    // CraftBukkit start - add change reason parameter
    public void remove(UUID uuid, ReputationType reputationtype, int i, Villager.ReputationEvent changeReason) {
        this.add(uuid, reputationtype, -i, changeReason);
    }
    // CraftBukkit end

    public void remove(UUID uuid, ReputationType reputationtype, Villager.ReputationEvent changeReason) { // CraftBukkit - add change reason parameter
        Reputation.a reputation_a = (Reputation.a) this.gossips.get(uuid);

        if (reputation_a != null) {
            // CraftBukkit start - redirect - set to 0 instead
            set(uuid, reputationtype, 0, changeReason);
            //reputation_a.remove(reputationtype);
            // CraftBukkit end
            if (reputation_a.isEmpty()) {
                this.gossips.remove(uuid);
            }
        }

    }

    public void remove(ReputationType reputationtype, Villager.ReputationEvent changeReason) { // CraftBukkit - add change reason parameter
        // CraftBukkit start - replace the logic to call the other remove instead
        Set<UUID> uuids = Sets.newHashSet(this.gossips.keySet());
        for (UUID uuid : uuids) {
            remove(uuid, reputationtype, changeReason);
        }
        if (true) {
            return;
        }
        // CraftBukkit end
        Iterator<Reputation.a> iterator = this.gossips.values().iterator();

        while (iterator.hasNext()) {
            Reputation.a reputation_a = (Reputation.a) iterator.next();

            reputation_a.remove(reputationtype);
            if (reputation_a.isEmpty()) {
                iterator.remove();
            }
        }

    }

    public <T> T store(DynamicOps<T> dynamicops) {
        Optional<T> optional = Reputation.b.LIST_CODEC.encodeStart(dynamicops, this.unpack().toList()).resultOrPartial((s) -> { // CraftBukkit - missing generic parameter after decompile
            Reputation.LOGGER.warn("Failed to serialize gossips: {}", s);
        });

        Objects.requireNonNull(dynamicops);
        return optional.orElseGet(dynamicops::emptyList);
    }

    public void update(Dynamic<?> dynamic) {
        Reputation.b.LIST_CODEC.decode(dynamic).resultOrPartial((s) -> {
            Reputation.LOGGER.warn("Failed to deserialize gossips: {}", s);
        }).stream().flatMap((pair) -> {
            return ((List<Reputation.b>) pair.getFirst()).stream(); // CraftBukkit - missing generic parameter after decompile
        }).forEach((reputation_b) -> {
            this.getOrCreate(reputation_b.target).entries.put(reputation_b.type, reputation_b.value);
        });
    }

    private static int mergeValuesForTransfer(int i, int j) {
        return Math.max(i, j);
    }

    private int mergeValuesForAddition(ReputationType reputationtype, int i, int j) {
        int k = i + j;

        return k > reputationtype.max ? Math.max(reputationtype.max, i) : k;
    }

    private static class a {

        final Object2IntMap<ReputationType> entries = new Object2IntOpenHashMap();

        a() {}

        public int weightedValue(Predicate<ReputationType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter((entry) -> {
                return predicate.test((ReputationType) entry.getKey());
            }).mapToInt((entry) -> {
                return entry.getIntValue() * ((ReputationType) entry.getKey()).weight;
            }).sum();
        }

        // CraftBukkit start
        public int unweightedValue(Predicate<ReputationType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter((entry) -> {
                return predicate.test((ReputationType) entry.getKey());
            }).mapToInt((entry) -> {
                return entry.getIntValue();
            }).sum();
        }
        // CraftBukkit end

        public Stream<Reputation.b> unpack(UUID uuid) {
            return this.entries.object2IntEntrySet().stream().map((entry) -> {
                return new Reputation.b(uuid, (ReputationType) entry.getKey(), entry.getIntValue());
            });
        }

        public void decay(EntityVillager villager, UUID uuid) { // CraftBukkit - add villager and entity uuid parameters
            ObjectIterator<Entry<ReputationType>> objectiterator = this.entries.object2IntEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<ReputationType> entry = (Entry) objectiterator.next();
                int i = entry.getIntValue() - ((ReputationType) entry.getKey()).decayPerDay;
                // CraftBukkit start - fire event
                VillagerReputationChangeEvent event = CraftEventFactory.callVillagerReputationChangeEvent((Villager) villager.getBukkitEntity(), uuid, Villager.ReputationEvent.DECAY, CraftReputationType.minecraftToBukkit(entry.getKey()), entry.getIntValue(), i, entry.getKey().max);
                if (event.isCancelled()) {
                    continue;
                } else {
                    i = event.getNewValue();
                }
                // CraftBukkit end

                if (i < 2) {
                    objectiterator.remove();
                } else {
                    entry.setValue(i);
                }
            }

        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(ReputationType reputationtype) {
            int i = this.entries.getInt(reputationtype);

            if (i > reputationtype.max) {
                this.entries.put(reputationtype, reputationtype.max);
            }

            if (i < 2) {
                this.remove(reputationtype);
            }

        }

        public void remove(ReputationType reputationtype) {
            this.entries.removeInt(reputationtype);
        }
    }

    private static record b(UUID target, ReputationType type, int value) {

        public static final Codec<Reputation.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(UUIDUtil.CODEC.fieldOf("Target").forGetter(Reputation.b::target), ReputationType.CODEC.fieldOf("Type").forGetter(Reputation.b::type), ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(Reputation.b::value)).apply(instance, Reputation.b::new);
        });
        public static final Codec<List<Reputation.b>> LIST_CODEC = Reputation.b.CODEC.listOf();

        public int weightedValue() {
            return this.value * this.type.weight;
        }
    }
}
