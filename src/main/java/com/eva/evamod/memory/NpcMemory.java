package com.eva.evamod.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NpcMemory {
    public static final int REP_MIN = -100;
    public static final int REP_MAX = 100;

    public static final int MOOD_HOSTILE = 0;
    public static final int MOOD_ANNOYED = 1;
    public static final int MOOD_NEUTRAL = 2;
    public static final int MOOD_FRIENDLY = 3;
    public static final int MOOD_CLOSE_FRIEND = 4;

    public static class Record {
        public int talks;
        public int hits;
        public int trades;
        public int gifts;
        public int favoriteGifts;
        public int reputation;
        public long firstMetDay = -1;
        public long lastTalkDay = -1;
        public long lastHitDay = -1;
        public long lastSeenDay = -1;
        public long lastGiftDay = -1;

        public boolean isStranger() {
            return firstMetDay < 0;
        }

        public int moodTier() {
            if (reputation >= 40) {
                return MOOD_CLOSE_FRIEND;
            }
            if (reputation >= 10) {
                return MOOD_FRIENDLY;
            }
            if (reputation > -10) {
                return MOOD_NEUTRAL;
            }
            if (reputation > -40) {
                return MOOD_ANNOYED;
            }
            return MOOD_HOSTILE;
        }

        void save(ValueOutput output) {
            output.putInt("Talks", talks);
            output.putInt("Hits", hits);
            output.putInt("Trades", trades);
            output.putInt("Gifts", gifts);
            output.putInt("FavoriteGifts", favoriteGifts);
            output.putInt("Reputation", reputation);
            output.putLong("FirstMetDay", firstMetDay);
            output.putLong("LastTalkDay", lastTalkDay);
            output.putLong("LastHitDay", lastHitDay);
            output.putLong("LastSeenDay", lastSeenDay);
            output.putLong("LastGiftDay", lastGiftDay);
        }

        static Record load(ValueInput input) {
            Record record = new Record();
            record.talks = input.getIntOr("Talks", 0);
            record.hits = input.getIntOr("Hits", 0);
            record.trades = input.getIntOr("Trades", 0);
            record.gifts = input.getIntOr("Gifts", 0);
            record.favoriteGifts = input.getIntOr("FavoriteGifts", 0);
            record.reputation = input.getIntOr("Reputation", 0);
            record.firstMetDay = input.getLongOr("FirstMetDay", -1L);
            record.lastTalkDay = input.getLongOr("LastTalkDay", -1L);
            record.lastHitDay = input.getLongOr("LastHitDay", -1L);
            record.lastSeenDay = input.getLongOr("LastSeenDay", -1L);
            record.lastGiftDay = input.getLongOr("LastGiftDay", -1L);
            return record;
        }
    }

    private final Map<UUID, Record> records = new HashMap<>();

    public Record get(UUID player, long day) {
        Record record = records.computeIfAbsent(player, id -> new Record());
        if (record.lastSeenDay >= 0 && day > record.lastSeenDay) {
            int daysAway = (int) Math.min(20, day - record.lastSeenDay);
            if (record.reputation > 0) {
                record.reputation = Math.max(0, record.reputation - daysAway);
            } else if (record.reputation < 0) {
                record.reputation = Math.min(0, record.reputation + daysAway * 2);
            }
        }
        record.lastSeenDay = day;
        return record;
    }

    public void recordTalk(UUID player, long day) {
        Record record = get(player, day);
        if (record.firstMetDay < 0) {
            record.firstMetDay = day;
        }
        if (record.lastTalkDay != day) {
            record.reputation = Mth.clamp(record.reputation + 2, REP_MIN, REP_MAX);
        }
        record.talks++;
        record.lastTalkDay = day;
    }

    public void recordTrade(UUID player, long day) {
        Record record = get(player, day);
        record.trades++;
        record.reputation = Mth.clamp(record.reputation + 5, REP_MIN, REP_MAX);
    }

    public void recordGift(UUID player, long day, boolean favorite, int repChange) {
        Record record = get(player, day);
        if (record.firstMetDay < 0) {
            record.firstMetDay = day;
        }
        record.gifts++;
        if (favorite) {
            record.favoriteGifts++;
        }
        record.lastGiftDay = day;
        record.reputation = Mth.clamp(record.reputation + repChange, REP_MIN, REP_MAX);
    }

    public void recordHit(UUID player, long day) {
        Record record = get(player, day);
        if (record.firstMetDay < 0) {
            record.firstMetDay = day;
        }
        record.hits++;
        record.lastHitDay = day;
        record.reputation = Mth.clamp(record.reputation - 8, REP_MIN, REP_MAX);
    }

    public void save(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("Records");
        records.forEach((uuid, record) -> {
            ValueOutput entry = list.addChild();
            entry.putString("Player", uuid.toString());
            record.save(entry);
        });
    }

    public void load(ValueInput input) {
        records.clear();
        for (ValueInput entry : input.childrenListOrEmpty("Records")) {
            entry.getString("Player").ifPresent(id -> {
                try {
                    records.put(UUID.fromString(id), Record.load(entry));
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
    }
}
