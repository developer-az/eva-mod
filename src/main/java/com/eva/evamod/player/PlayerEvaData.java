package com.eva.evamod.player;

import com.eva.evamod.ModVersions;
import com.eva.evamod.adventure.AdventureProgress;
import com.eva.evamod.mail.MailMessage;
import com.eva.evamod.quest.Errand;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * Per-player Eva state. Schema-versioned for forward-compatible saves.
 * Keep mutations cheap — this is read/written on login and dialogue, never per-tick.
 */
public class PlayerEvaData {
    public static final Codec<PlayerEvaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", 0).forGetter(d -> d.schemaVersion),
            HouseIndexEntry.CODEC.listOf().optionalFieldOf("houses", List.of()).forGetter(d -> d.houses),
            BlockPos.CODEC.listOf().optionalFieldOf("located", List.of()).forGetter(d -> d.locatedSkips),
            Errand.CODEC.optionalFieldOf("errand").forGetter(d -> Optional.ofNullable(d.activeErrand)),
            MailMessage.LIST_CODEC.optionalFieldOf("mail", List.of()).forGetter(d -> d.mail),
            Codec.STRING.listOf().optionalFieldOf("heartEvents", List.of()).forGetter(d -> new ArrayList<>(d.seenHeartEvents)),
            Codec.LONG.optionalFieldOf("lastMailDay", -1L).forGetter(d -> d.lastMailDay),
            Codec.INT.optionalFieldOf("errandsDone", 0).forGetter(d -> d.errandsCompleted),
            Codec.BOOL.optionalFieldOf("gotGuide", false).forGetter(d -> d.receivedGuideBook),
            Codec.STRING.optionalFieldOf("petEntityUuid").forGetter(d -> Optional.ofNullable(d.petEntityUuid)),
            Codec.STRING.optionalFieldOf("petKind").forGetter(d -> Optional.ofNullable(d.petKind)),
            Codec.INT.optionalFieldOf("petsAwakened", 0).forGetter(d -> d.petsAwakened),
            AdventureProgress.LIST_CODEC.optionalFieldOf("adventures", List.of()).forGetter(d -> d.adventures),
            Codec.STRING.listOf().optionalFieldOf("landmarks", List.of()).forGetter(d -> d.landmarks)
    ).apply(instance, PlayerEvaData::fromCodec));

    private static final int SKIP_RADIUS_SQR = 20 * 20;
    private static final int MAX_LOCATED = 128;
    private static final int MAX_MAIL = 24;

    private int schemaVersion;
    private final List<HouseIndexEntry> houses;
    private final List<BlockPos> locatedSkips;
    private Errand activeErrand;
    private final List<MailMessage> mail;
    private final Set<String> seenHeartEvents;
    private long lastMailDay;
    private int errandsCompleted;
    private boolean receivedGuideBook;
    private @Nullable String petEntityUuid;
    private @Nullable String petKind;
    private int petsAwakened;
    private final List<AdventureProgress> adventures;
    private final List<String> landmarks;

    public PlayerEvaData() {
        this(ModVersions.PLAYER_SCHEMA, List.of(), List.of(), null, List.of(), Set.of(), -1L, 0, false,
                null, null, 0, List.of(), List.of());
    }

    public PlayerEvaData(
            int schemaVersion,
            List<HouseIndexEntry> houses,
            List<BlockPos> locatedSkips,
            Errand activeErrand,
            List<MailMessage> mail,
            Set<String> seenHeartEvents,
            long lastMailDay,
            int errandsCompleted,
            boolean receivedGuideBook,
            @Nullable String petEntityUuid,
            @Nullable String petKind,
            int petsAwakened,
            List<AdventureProgress> adventures,
            List<String> landmarks) {
        this.schemaVersion = schemaVersion;
        this.houses = new ArrayList<>(houses);
        this.locatedSkips = new ArrayList<>(locatedSkips);
        this.activeErrand = activeErrand;
        this.mail = new ArrayList<>(mail);
        this.seenHeartEvents = new HashSet<>(seenHeartEvents);
        this.lastMailDay = lastMailDay;
        this.errandsCompleted = errandsCompleted;
        this.receivedGuideBook = receivedGuideBook;
        this.petEntityUuid = petEntityUuid;
        this.petKind = petKind;
        this.petsAwakened = Math.max(0, petsAwakened);
        this.adventures = new ArrayList<>(adventures);
        this.landmarks = new ArrayList<>(landmarks);
    }

    private static PlayerEvaData fromCodec(
            int schema,
            List<HouseIndexEntry> houses,
            List<BlockPos> located,
            Optional<Errand> errand,
            List<MailMessage> mail,
            List<String> heartEvents,
            long lastMailDay,
            int errandsDone,
            boolean gotGuide,
            Optional<String> petEntityUuid,
            Optional<String> petKind,
            int petsAwakened,
            List<AdventureProgress> adventures,
            List<String> landmarks) {
        return new PlayerEvaData(
                schema, houses, located, errand.orElse(null), mail, new HashSet<>(heartEvents),
                lastMailDay, errandsDone, gotGuide,
                petEntityUuid.orElse(null), petKind.orElse(null), petsAwakened,
                adventures, landmarks);
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public boolean receivedGuideBook() {
        return receivedGuideBook;
    }

    public void setReceivedGuideBook(boolean receivedGuideBook) {
        this.receivedGuideBook = receivedGuideBook;
    }

    public List<HouseIndexEntry> houses() {
        return houses;
    }

    public List<BlockPos> locatedSkips() {
        return locatedSkips;
    }

    public Errand activeErrand() {
        return activeErrand;
    }

    public void setActiveErrand(Errand errand) {
        this.activeErrand = errand;
    }

    public void clearErrand() {
        this.activeErrand = null;
    }

    public void completeErrand() {
        if (activeErrand != null) {
            errandsCompleted++;
            activeErrand = null;
        }
    }

    public int errandsCompleted() {
        return errandsCompleted;
    }

    public List<MailMessage> mail() {
        return mail;
    }

    public long lastMailDay() {
        return lastMailDay;
    }

    public void setLastMailDay(long day) {
        this.lastMailDay = day;
    }

    public void addMail(MailMessage message) {
        mail.addFirst(message);
        while (mail.size() > MAX_MAIL) {
            mail.removeLast();
        }
    }

    public int unreadMailCount() {
        int n = 0;
        for (MailMessage m : mail) {
            if (!m.read()) {
                n++;
            }
        }
        return n;
    }

    public void markAllMailRead() {
        for (int i = 0; i < mail.size(); i++) {
            MailMessage m = mail.get(i);
            if (!m.read()) {
                mail.set(i, m.markRead());
            }
        }
    }

    public boolean hasSeenHeartEvent(String key) {
        return seenHeartEvents.contains(key);
    }

    public void markHeartEvent(String key) {
        seenHeartEvents.add(key);
    }

    public boolean isLocateSkipped(BlockPos pos) {
        for (BlockPos skip : locatedSkips) {
            if (skip.distSqr(pos) <= SKIP_RADIUS_SQR) {
                return true;
            }
        }
        return false;
    }

    public void rememberLocated(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        locatedSkips.removeIf(existing -> existing.distSqr(immutable) <= SKIP_RADIUS_SQR);
        locatedSkips.add(immutable);
        while (locatedSkips.size() > MAX_LOCATED) {
            locatedSkips.removeFirst();
        }
    }

    public void resetLocated() {
        locatedSkips.clear();
    }

    public boolean meetNpc(HouseIndexEntry entry) {
        for (int i = 0; i < houses.size(); i++) {
            HouseIndexEntry existing = houses.get(i);
            if (existing.homePos().distSqr(entry.homePos()) <= SKIP_RADIUS_SQR
                    || existing.npcName().equalsIgnoreCase(entry.npcName())) {
                houses.set(i, entry);
                return false;
            }
        }
        houses.add(entry);
        return true;
    }

    public boolean hasPet() {
        return petEntityUuid != null && !petEntityUuid.isBlank();
    }

    public Optional<UUID> petEntityUuid() {
        if (petEntityUuid == null || petEntityUuid.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(petEntityUuid));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public Optional<String> petKind() {
        return Optional.ofNullable(petKind);
    }

    public int petsAwakened() {
        return petsAwakened;
    }

    public void setPet(UUID entityId, String kind) {
        this.petEntityUuid = entityId == null ? null : entityId.toString();
        this.petKind = kind;
    }

    public void clearPet() {
        this.petEntityUuid = null;
        this.petKind = null;
    }

    public void incrementPetsAwakened() {
        this.petsAwakened++;
    }

    public List<AdventureProgress> adventures() {
        return adventures;
    }

    public @Nullable AdventureProgress findAdventure(String id) {
        for (AdventureProgress p : adventures) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public void upsertAdventure(AdventureProgress progress) {
        for (int i = 0; i < adventures.size(); i++) {
            if (adventures.get(i).id().equals(progress.id())) {
                adventures.set(i, progress);
                return;
            }
        }
        adventures.add(progress);
    }

    public boolean isAdventureComplete(String id) {
        AdventureProgress p = findAdventure(id);
        return p != null && p.completed();
    }

    public int adventuresCompleted() {
        int n = 0;
        for (AdventureProgress p : adventures) {
            if (p.completed()) {
                n++;
            }
        }
        return n;
    }

    public List<String> landmarks() {
        return landmarks;
    }

    public boolean discoverLandmark(String id) {
        if (landmarks.contains(id)) {
            return false;
        }
        landmarks.add(id);
        return true;
    }

    public PlayerEvaData copy() {
        return new PlayerEvaData(
                schemaVersion, houses, locatedSkips, activeErrand, mail, seenHeartEvents,
                lastMailDay, errandsCompleted, receivedGuideBook,
                petEntityUuid, petKind, petsAwakened, adventures, landmarks);
    }
}
