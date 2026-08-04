package com.eva.evamod.calendar;

/**
 * Stardew-inspired calendar: four 28-day seasons cycling forever from world day 0.
 * Festival day is the 15th of each season (community gathering flavor).
 */
public final class SeasonCalendar {
    public static final int DAYS_PER_SEASON = 28;
    public static final int DAYS_PER_YEAR = DAYS_PER_SEASON * 4;
    public static final int FESTIVAL_DAY = 15;

    public enum Season {
        SPRING("Spring"),
        SUMMER("Summer"),
        FALL("Fall"),
        WINTER("Winter");

        private final String displayName;

        Season(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private SeasonCalendar() {
    }

    public static long dayOfYear(long worldDay) {
        return Math.floorMod(Math.max(0L, worldDay), DAYS_PER_YEAR);
    }

    public static Season season(long worldDay) {
        int index = (int) (dayOfYear(worldDay) / DAYS_PER_SEASON);
        return Season.values()[index];
    }

    public static int dayInSeason(long worldDay) {
        return (int) (dayOfYear(worldDay) % DAYS_PER_SEASON) + 1;
    }

    public static boolean isFestival(long worldDay) {
        return dayInSeason(worldDay) == FESTIVAL_DAY;
    }

    public static String festivalName(long worldDay) {
        return switch (season(worldDay)) {
            case SPRING -> "Flower Festival";
            case SUMMER -> "Sunfair";
            case FALL -> "Harvest Feast";
            case WINTER -> "Frostlight Gathering";
        };
    }

    public static String formatDate(long worldDay) {
        return season(worldDay).getDisplayName() + " " + dayInSeason(worldDay);
    }

    /** Birthday is day-of-year 0..111 derived from a stable hash (NPC UUID). */
    public static int birthdayDayOfYear(int hash) {
        return Math.floorMod(hash, DAYS_PER_YEAR);
    }

    public static boolean isBirthday(long worldDay, int birthdayDayOfYear) {
        return dayOfYear(worldDay) == Math.floorMod(birthdayDayOfYear, DAYS_PER_YEAR);
    }

    public static String formatBirthday(int birthdayDayOfYear) {
        int day = Math.floorMod(birthdayDayOfYear, DAYS_PER_YEAR);
        Season season = Season.values()[day / DAYS_PER_SEASON];
        int dayIn = (day % DAYS_PER_SEASON) + 1;
        return season.getDisplayName() + " " + dayIn;
    }
}
