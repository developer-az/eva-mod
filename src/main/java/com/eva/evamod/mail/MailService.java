package com.eva.evamod.mail;

import com.eva.evamod.calendar.SeasonCalendar;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

/**
 * Delivers occasional NPC letters: birthday reminders, festival invites, friendship notes.
 */
public final class MailService {
    private MailService() {
    }

    public static void tryDeliver(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        long day = player.level().getOverworldClockTime() / 24000L;
        if (data.lastMailDay() == day) {
            return;
        }
        data.setLastMailDay(day);
        boolean delivered = false;

        if (SeasonCalendar.isFestival(day)) {
            data.addMail(new MailMessage(
                    "Town Plaza",
                    SeasonCalendar.festivalName(day),
                    "Today is " + SeasonCalendar.festivalName(day) + "! Folk will be chatting about it all day. Come say hello.",
                    day,
                    false));
            delivered = true;
        }

        for (HouseIndexEntry entry : data.houses()) {
            int bday = birthdayFromLabel(entry.birthday(), entry.npcName().hashCode());
            // Reminder the day before, and on the day.
            long tomorrow = day + 1;
            if (SeasonCalendar.isBirthday(day, bday)) {
                data.addMail(new MailMessage(
                        entry.npcName(),
                        "It's my birthday!",
                        "Today's my birthday (" + entry.birthday() + "). If you stop by with something nice, I'll remember it forever.",
                        day,
                        false));
                delivered = true;
            } else if (SeasonCalendar.isBirthday(tomorrow, bday) && data.houses().size() <= 12) {
                data.addMail(new MailMessage(
                        "Calendar",
                        entry.npcName() + "'s birthday tomorrow",
                        entry.npcName() + " celebrates on " + entry.birthday()
                                + ". A loved gift would mean the world.",
                        day,
                        false));
                delivered = true;
            } else if (entry.hearts() >= 6 && day % 14 == Math.floorMod(entry.npcName().hashCode(), 14)) {
                data.addMail(new MailMessage(
                        entry.npcName(),
                        "Thinking of you",
                        "Hey — just wanted to say the " + entry.biome().toLowerCase()
                                + " feels friendlier with you around. Visit when you can!",
                        day,
                        false));
                delivered = true;
            }
            if (delivered && data.mail().size() >= 3) {
                break;
            }
        }

        player.setData(ModAttachments.PLAYER_DATA, data.copy());
    }

    private static int birthdayFromLabel(String birthday, int fallbackHash) {
        // Birthday labels are like "Spring 12" — re-derive a stable day-of-year from the NPC name hash.
        return SeasonCalendar.birthdayDayOfYear(fallbackHash);
    }
}
