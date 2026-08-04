package com.eva.evamod.player;

import com.eva.evamod.mail.MailMessage;
import com.eva.evamod.quest.Errand;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;

/** Per-player house index, locate skips, errands, mail, and heart-event flags. */
public class PlayerEvaData {
    public static final Codec<PlayerEvaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HouseIndexEntry.CODEC.listOf().optionalFieldOf("houses", List.of()).forGetter(d -> d.houses),
            BlockPos.CODEC.listOf().optionalFieldOf("located", List.of()).forGetter(d -> d.locatedSkips),
            Errand.CODEC.optionalFieldOf("errand").forGetter(d -> Optional.ofNullable(d.activeErrand)),
            MailMessage.LIST_CODEC.optionalFieldOf("mail", List.of()).forGetter(d -> d.mail),
            Codec.STRING.listOf().optionalFieldOf("heartEvents", List.of()).forGetter(d -> new ArrayList<>(d.seenHeartEvents)),
            Codec.LONG.optionalFieldOf("lastMailDay", -1L).forGetter(d -> d.lastMailDay),
            Codec.INT.optionalFieldOf("errandsDone", 0).forGetter(d -> d.errandsCompleted)
    ).apply(instance, PlayerEvaData::fromCodec));

    private static final int SKIP_RADIUS_SQR = 20 * 20;
    private static final int MAX_LOCATED = 128;
    private static final int MAX_MAIL = 24;

    private final List<HouseIndexEntry> houses;
    private final List<BlockPos> locatedSkips;
    private Errand activeErrand;
    private final List<MailMessage> mail;
    private final Set<String> seenHeartEvents;
    private long lastMailDay;
    private int errandsCompleted;

    public PlayerEvaData() {
        this(List.of(), List.of(), null, List.of(), Set.of(), -1L, 0);
    }

    public PlayerEvaData(
            List<HouseIndexEntry> houses,
            List<BlockPos> locatedSkips,
            Errand activeErrand,
            List<MailMessage> mail,
            Set<String> seenHeartEvents,
            long lastMailDay,
            int errandsCompleted) {
        this.houses = new ArrayList<>(houses);
        this.locatedSkips = new ArrayList<>(locatedSkips);
        this.activeErrand = activeErrand;
        this.mail = new ArrayList<>(mail);
        this.seenHeartEvents = new HashSet<>(seenHeartEvents);
        this.lastMailDay = lastMailDay;
        this.errandsCompleted = errandsCompleted;
    }

    private static PlayerEvaData fromCodec(
            List<HouseIndexEntry> houses,
            List<BlockPos> located,
            Optional<Errand> errand,
            List<MailMessage> mail,
            List<String> heartEvents,
            long lastMailDay,
            int errandsDone) {
        return new PlayerEvaData(
                houses, located, errand.orElse(null), mail, new HashSet<>(heartEvents), lastMailDay, errandsDone);
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

    public PlayerEvaData copy() {
        return new PlayerEvaData(houses, locatedSkips, activeErrand, mail, seenHeartEvents, lastMailDay, errandsCompleted);
    }
}
