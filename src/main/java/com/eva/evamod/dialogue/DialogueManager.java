package com.eva.evamod.dialogue;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcPersonality;
import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.memory.NpcMemory;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.RandomSource;

/**
 * The dialogue database. Every line an NPC can say lives here, organized by
 * context, mood and personality. Lines are picked server-side using the NPC's
 * memory of the player, then sent to the client as plain text.
 */
public final class DialogueManager {

    /** What situation the NPC is reacting to. */
    public enum Context {
        GREETING,
        SMALL_TALK,
        GIFT_FAVORITE,
        GIFT_LIKED,
        GIFT_MEH,
        GIFT_JUNK,
        GIFT_ALREADY,
        WOKEN
    }

    // ------------------------------------------------------------------
    // Greetings by relationship
    // ------------------------------------------------------------------

    private static final String[] FIRST_MEETING = {
            "Oh! A new face around here. I'm %NAME%, the local %JOB%.",
            "A traveler! We don't get many. The name's %NAME% - I work as a %JOB%.",
            "Hello, stranger. %NAME%'s the name. If you ever need a %JOB%, that's me.",
            "Welcome to my little corner of the %BIOME%. I'm %NAME%.",
            "Ooh, a visitor! I'm %NAME%. Wipe your boots, mind the flowers, and welcome!",
            "You're not from around here, are you? I'm %NAME%. Pleasure!"
    };

    private static final String[] HIT_TODAY = {
            "OW! What is WRONG with you?!",
            "You just hit me! I won't forget this.",
            "Keep those fists to yourself, or we're done talking.",
            "That hurt! And here I thought we could be civil.",
            "Rude! Absolutely, thoroughly rude!"
    };

    private static final String[] HIT_RECENTLY = {
            "You hit me the other day. I haven't forgotten, you know.",
            "My bruise from your last visit is still healing. What do you want?",
            "Back again? Last time you used your fists instead of words.",
            "I remember what you did. Don't expect any favors."
    };

    private static final String[] HOSTILE = {
            "Oh. It's you. What do you want NOW?",
            "After everything you've done, you still show your face here?",
            "I have nothing nice to say to you.",
            "The nerve of some people, honestly.",
            "Hmph. The %BIOME% was quieter before you showed up."
    };

    private static final String[] ANNOYED = {
            "Hmph. Hello, I suppose.",
            "You again. Let's keep this short.",
            "I'm watching you, %PLAYER%.",
            "Make it quick, I'm busy."
    };

    private static final String[] NEUTRAL = {
            "Hello there, %PLAYER%.",
            "Good day. Fine weather for a %JOB%, isn't it?",
            "Ah, %PLAYER%. What brings you by?",
            "Need something? My stock changes every day, mind you.",
            "Oh, hello! I was just thinking about lunch."
    };

    private static final String[] FRIENDLY = {
            "%PLAYER%! Good to see a friendly face.",
            "Ah, my favorite customer! Well... top five, at least.",
            "Always a pleasure, %PLAYER%. Come to chat or to trade?",
            "You're becoming a regular around here, %PLAYER%!",
            "Just the person I was hoping to see today!"
    };

    private static final String[] CLOSE_FRIEND = {
            "%PLAYER%, my friend! You always brighten my day.",
            "If it isn't my favorite person in the whole %BIOME%!",
            "For you, %PLAYER%? Anything. I might even have a special deal today.",
            "Old friends are the best friends, %PLAYER%. Welcome back!",
            "There you are! The house feels cozier when you visit."
    };

    private static final String[] LONG_TIME_NO_SEE = {
            "It's been %DAYS% days, %PLAYER%! I was starting to worry.",
            "%DAYS% whole days since your last visit. The %BIOME% missed you. Well, I did.",
            "Look who finally remembered I exist! %DAYS% days, %PLAYER%."
    };

    // ------------------------------------------------------------------
    // Situational
    // ------------------------------------------------------------------

    private static final String[] RAINY_DAY = {
            "Dreadful weather. Good thing I have a roof, unlike some mobs I could mention.",
            "Rain again! The roof is holding. Probably.",
            "Come in, come in, you'll catch a cold out there!"
    };

    private static final String[] NIGHT_LINES = {
            "You're up late, %PLAYER%. The stars are lovely tonight, though.",
            "Shhh... the whole %BIOME% is asleep. Except us, apparently.",
            "A midnight visit? How mysterious of you, %PLAYER%."
    };

    private static final String[] WOKEN_LINES = {
            "Mmmph... %PLAYER%? It's the middle of the night...",
            "*yawn* This had better be important...",
            "I was having the LOVELIEST dream about %FAVORITE%... what is it?",
            "Five more minutes... no, wait, you're still here. Hello.",
            "Do you always wake people up like this? ...At least say hi to the moon for me."
    };

    // ------------------------------------------------------------------
    // Gifts
    // ------------------------------------------------------------------

    private static final String[] GIFT_FAVORITE_LINES = {
            "%ITEM%?! For ME? This is my absolute favorite! How did you know?!",
            "No way. NO WAY. %ITEM%! You wonderful person!",
            "I could cry. %ITEM% is my favorite thing in the whole world.",
            "You remembered that I love %ITEM%! This is the best day all season!"
    };

    private static final String[] GIFT_LIKED_LINES = {
            "Oh, %ITEM%! That's so thoughtful of you, %PLAYER%.",
            "A little something for me? You shouldn't have! ...Do it again sometime.",
            "%ITEM%, how nice! I'll save it for supper.",
            "That's very kind. Most folks only come here to haggle."
    };

    private static final String[] GIFT_MEH_LINES = {
            "Oh. %ITEM%. How... practical. Thank you, I suppose.",
            "Hm, %ITEM%? I'll... find a use for it. Somewhere.",
            "You have interesting taste in gifts, %PLAYER%. But it's the thought that counts!"
    };

    private static final String[] GIFT_JUNK_LINES = {
            "Is... is this %ITEM%? Is this a prank, %PLAYER%?",
            "%ITEM%. You brought me %ITEM%. I'm speechless. Not in the good way.",
            "I'm going to pretend I didn't see that. Take your %ITEM% with you.",
            "My compost heap has better manners than to hand me %ITEM%."
    };

    private static final String[] GIFT_ALREADY_LINES = {
            "Another gift? You already spoiled me today! Save it for tomorrow.",
            "You're too generous, %PLAYER%! One gift a day is plenty.",
            "My pockets are full and my heart is fuller. Tomorrow, okay?"
    };

    // ------------------------------------------------------------------
    // Personality small talk
    // ------------------------------------------------------------------

    private static final Map<NpcPersonality, String[]> PERSONALITY_TALK = new EnumMap<>(NpcPersonality.class);

    static {
        PERSONALITY_TALK.put(NpcPersonality.CHEERFUL, new String[]{
                "Isn't today just the BEST day? I mean, look at it!",
                "I hummed a little song while working today. Want to hear it? Too late, humming again!",
                "Smiling is free, you know. I stock up every morning.",
                "Every sunrise here is my favorite sunrise."});
        PERSONALITY_TALK.put(NpcPersonality.SHY, new String[]{
                "Oh, um. Hello. Sorry, I'm not great at... this.",
                "I practiced saying hi in the mirror this morning. It went better than this.",
                "You can stay, just... maybe don't stand SO close?",
                "I like quiet days. Today is nice and quiet. Well, it was."});
        PERSONALITY_TALK.put(NpcPersonality.DRAMATIC, new String[]{
                "The wind! The sky! The DRAMA of it all!",
                "I have seen a thousand sunsets and each one has CHANGED me.",
                "My life is a saga, %PLAYER%, and you have just entered chapter twelve.",
                "One day they will write songs about my work. Beautiful, tragic songs."});
        PERSONALITY_TALK.put(NpcPersonality.GRUMPY, new String[]{
                "Back in my day, creepers kept a respectful distance.",
                "My knees ache. That means rain. Or visitors. Apparently both.",
                "I'm not grumpy. This is just my face.",
                "Everything was better yesterday. It always is."});
        PERSONALITY_TALK.put(NpcPersonality.SLEEPY, new String[]{
                "*yawn* Sorry, you were saying?",
                "I had a nap planned for right about... now, actually.",
                "Five beds. If I were rich, I'd own five beds. One for each mood.",
                "Some people count emeralds. I count sheep. Professionally."});
        PERSONALITY_TALK.put(NpcPersonality.CURIOUS, new String[]{
                "Say, what's it like out past the %BIOME%? Tell me everything.",
                "I found a weird rock today. It's in my pocket. Want to see? ...Maybe later.",
                "Do you think fish know they're wet? I think about this a lot.",
                "Every chest I see, I wonder what's inside. Every. Single. One."});
    }

    // ------------------------------------------------------------------
    // Job and biome small talk
    // ------------------------------------------------------------------

    private static final Map<NpcJob, String[]> JOB_SMALL_TALK = new EnumMap<>(NpcJob.class);
    private static final Map<NpcVariant, String[]> BIOME_SMALL_TALK = new EnumMap<>(NpcVariant.class);

    static {
        JOB_SMALL_TALK.put(NpcJob.FARMER, new String[]{
                "The crops practically grow themselves this season.",
                "A good harvest starts with good soil, I always say.",
                "Rabbits got into the carrots again. Nature's little thieves.",
                "I name my pumpkins. Don't tell anyone. The big one is Gerald."});
        JOB_SMALL_TALK.put(NpcJob.SHEPHERD, new String[]{
                "Wool prices are steady, thank goodness.",
                "Sheep are better listeners than most people.",
                "Lost a lamb to a wolf last winter. Still stings.",
                "I can tell my sheep apart by their chewing sounds. It's a gift."});
        JOB_SMALL_TALK.put(NpcJob.FISHERMAN, new String[]{
                "The fish bite best just after rain.",
                "One day I'll catch something with a name tag. Imagine that.",
                "Patience is the whole job, really. Patience and string.",
                "The one that got away? Gets bigger every time I tell it."});
        JOB_SMALL_TALK.put(NpcJob.LUMBERJACK, new String[]{
                "Plant two for every one you fell. That's the rule.",
                "You can tell a tree's whole life from its rings.",
                "My axe arm is stronger than my hand-shake arm. Noticeably.",
                "I apologize to every tree first. It feels right."});
        JOB_SMALL_TALK.put(NpcJob.TRAPPER, new String[]{
                "Tracks in the snow this morning. Big ones.",
                "A good trapper takes only what the land can spare.",
                "Rabbit stew tonight. Again. I'm not complaining.",
                "The foxes and I have an understanding. Mostly."});
        JOB_SMALL_TALK.put(NpcJob.HERBALIST, new String[]{
                "Every weed is a remedy someone hasn't discovered yet.",
                "Smell that? Sweet berries. Best perfume there is.",
                "Chew glow berries before a night walk. Trust me.",
                "I talk to my plants. The ones I talk to grow taller. Coincidence?"});
        JOB_SMALL_TALK.put(NpcJob.MINER, new String[]{
                "The deeper you dig, the louder the silence gets.",
                "Iron's honest work. Diamonds are just luck with better marketing.",
                "Always carry a spare torch. Always.",
                "I once mistook glowstone dust for gold dust. Bright times."});
        JOB_SMALL_TALK.put(NpcJob.CARTOGRAPHER, new String[]{
                "Every map is a story that hasn't been walked yet.",
                "North is a suggestion until you're lost. Then it's a religion.",
                "I once mapped a cave so deep the ink got cold.",
                "X marks the spot. Unless I sneezed while drawing. Then Y does."});
        JOB_SMALL_TALK.put(NpcJob.GLASSBLOWER, new String[]{
                "Sand plus fire plus patience equals a window. Marvelous, no?",
                "The desert gives us sand; I give it back as art.",
                "Never sneeze while blowing glass. Learned that the hard way.",
                "My greatest work? A bottle so clear I lost it for a week."});
        JOB_SMALL_TALK.put(NpcJob.NOMAD, new String[]{
                "Home is wherever I hang my hat. And I have many hats.",
                "The road teaches more than any book.",
                "I've traded with folk from deserts to glaciers. You meet all sorts.",
                "Settling down was the strangest journey of all."});

        BIOME_SMALL_TALK.put(NpcVariant.PLAINS, new String[]{
                "Nothing beats a plains sunset. Nothing.",
                "Flat land, honest people. That's the plains for you.",
                "The bees around here make the sweetest honey. Don't tell the herbalist I said that."});
        BIOME_SMALL_TALK.put(NpcVariant.DESERT, new String[]{
                "Hot days, cold nights. The desert keeps you humble.",
                "Water first, gold second. Desert rule number one.",
                "I once saw rain here. I still talk about it. Clearly."});
        BIOME_SMALL_TALK.put(NpcVariant.TAIGA, new String[]{
                "The pines whisper if you stand still long enough.",
                "Foxes stole my breakfast again. Clever little things.",
                "Nothing smells better than the taiga after rain. Nothing."});
        BIOME_SMALL_TALK.put(NpcVariant.SNOWY, new String[]{
                "Cold? This is a warm spell, friend.",
                "Snow hides everything except your footprints.",
                "I judge everyone by their coat. Yours is... adequate."});
        BIOME_SMALL_TALK.put(NpcVariant.SAVANNA, new String[]{
                "The acacias have seen more sunrises than any of us.",
                "Dry season's coming. You can smell it.",
                "The grass sings when the wind is right. Truly."});
        BIOME_SMALL_TALK.put(NpcVariant.JUNGLE, new String[]{
                "The jungle never sleeps. Neither do the parrots, sadly.",
                "Watch your step out there - the vines watch back.",
                "A parrot learned my name last week. Now it mocks me daily."});
        BIOME_SMALL_TALK.put(NpcVariant.SWAMP, new String[]{
                "The bog bubbles more than usual today. Interesting.",
                "Frogsong all night long. You get used to it. Mostly.",
                "The mist here keeps secrets. So do I."});
    }

    // ------------------------------------------------------------------
    // Selection
    // ------------------------------------------------------------------

    public static String pickLine(BiomeNpc npc, NpcMemory.Record record, String playerName,
                                  Context context, long day, RandomSource random) {
        String line = switch (context) {
            case GREETING -> pickGreeting(npc, record, day, random);
            case SMALL_TALK -> pickSmallTalk(npc, record, random);
            case GIFT_FAVORITE -> pick(GIFT_FAVORITE_LINES, random);
            case GIFT_LIKED -> pick(GIFT_LIKED_LINES, random);
            case GIFT_MEH -> pick(GIFT_MEH_LINES, random);
            case GIFT_JUNK -> pick(GIFT_JUNK_LINES, random);
            case GIFT_ALREADY -> pick(GIFT_ALREADY_LINES, random);
            case WOKEN -> pick(WOKEN_LINES, random);
        };
        return substitute(line, npc, playerName, record, day);
    }

    private static String pickGreeting(BiomeNpc npc, NpcMemory.Record record, long day, RandomSource random) {
        if (record.isStranger() || record.talks == 0) {
            return pick(FIRST_MEETING, random);
        }
        if (record.lastHitDay == day && record.reputation < 10) {
            return pick(HIT_TODAY, random);
        }
        if (record.lastHitDay >= 0 && day - record.lastHitDay <= 3 && record.reputation < 0) {
            return pick(HIT_RECENTLY, random);
        }
        long daysSinceTalk = record.lastTalkDay >= 0 ? day - record.lastTalkDay : 0;
        if (daysSinceTalk >= 5 && record.moodTier() >= NpcMemory.MOOD_FRIENDLY) {
            return pick(LONG_TIME_NO_SEE, random);
        }
        // Situational flavor for anyone who isn't upset with the player
        if (record.moodTier() >= NpcMemory.MOOD_NEUTRAL) {
            if (npc.level().isRaining() && random.nextInt(3) == 0) {
                return pick(RAINY_DAY, random);
            }
            if (npc.level().isDarkOutside() && random.nextInt(3) == 0) {
                return pick(NIGHT_LINES, random);
            }
            // Gift callback: they remember generosity
            if (record.favoriteGifts > 0 && random.nextInt(5) == 0) {
                return "You know, I still think about that %FAVORITE% you gave me, %PLAYER%.";
            }
        }
        return switch (record.moodTier()) {
            case NpcMemory.MOOD_HOSTILE -> pick(HOSTILE, random);
            case NpcMemory.MOOD_ANNOYED -> pick(ANNOYED, random);
            case NpcMemory.MOOD_FRIENDLY -> pick(FRIENDLY, random);
            case NpcMemory.MOOD_CLOSE_FRIEND -> pick(CLOSE_FRIEND, random);
            default -> pick(NEUTRAL, random);
        };
    }

    private static String pickSmallTalk(BiomeNpc npc, NpcMemory.Record record, RandomSource random) {
        if (record.moodTier() == NpcMemory.MOOD_HOSTILE) {
            return pick(HOSTILE, random);
        }
        // Relationship callbacks sprinkle in occasionally
        if (record.moodTier() >= NpcMemory.MOOD_CLOSE_FRIEND && random.nextInt(4) == 0) {
            List<String> callbacks = new ArrayList<>();
            if (record.trades > 0) {
                callbacks.add("We've traded " + record.trades + " times now, %PLAYER%. Practically business partners!");
            }
            if (record.gifts > 0) {
                callbacks.add("That's " + record.gifts + " gifts you've given me. I keep count. Happily.");
            }
            callbacks.add("Remember when we first met? Day %FIRSTMET%. I remember everything.");
            return callbacks.get(random.nextInt(callbacks.size()));
        }
        // Otherwise: personality, job or biome flavored chatter
        int roll = random.nextInt(3);
        String[] pool = switch (roll) {
            case 0 -> PERSONALITY_TALK.get(npc.getPersonality());
            case 1 -> JOB_SMALL_TALK.get(npc.getJob());
            default -> BIOME_SMALL_TALK.get(npc.getVariant());
        };
        return pick(pool, random);
    }

    private static String pick(String[] pool, RandomSource random) {
        return pool[random.nextInt(pool.length)];
    }

    private static String substitute(String line, BiomeNpc npc, String playerName,
                                     NpcMemory.Record record, long day) {
        long daysSinceTalk = record.lastTalkDay >= 0 ? Math.max(0, day - record.lastTalkDay) : 0;
        return line
                .replace("%NAME%", npc.getNpcName())
                .replace("%JOB%", npc.getJob().getDisplayName().toLowerCase(Locale.ROOT))
                .replace("%BIOME%", npc.getVariant().getDisplayName().toLowerCase(Locale.ROOT))
                .replace("%PLAYER%", playerName)
                .replace("%DAYS%", Long.toString(daysSinceTalk))
                .replace("%FIRSTMET%", Long.toString(Math.max(0, record.firstMetDay)))
                .replace("%FAVORITE%", npc.getFavoriteGiftName())
                .replace("%ITEM%", npc.getLastGiftName());
    }

    private DialogueManager() {
    }
}
