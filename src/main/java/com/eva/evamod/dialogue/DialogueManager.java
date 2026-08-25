package com.eva.evamod.dialogue;

import com.eva.evamod.calendar.SeasonCalendar;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcPersonality;
import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.friendship.Hearts;
import com.eva.evamod.memory.NpcMemory;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.quest.Errand;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/**
 * Dialogue database — mood, personality, season, festival, birthday, heart events,
 * gossip, and adventure tips so conversations stay fresh.
 */
public final class DialogueManager {

    public enum Context {
        GREETING,
        SMALL_TALK,
        GIFT_FAVORITE,
        GIFT_LIKED,
        GIFT_MEH,
        GIFT_JUNK,
        GIFT_ALREADY,
        GIFT_BIRTHDAY,
        WOKEN,
        HEART_EVENT,
        ERRAND_OFFER,
        ERRAND_PROGRESS,
        ERRAND_COMPLETE,
        FESTIVAL,
        BIRTHDAY,
        ADVENTURE_TIP,
        ASK_DAY,
        COMPLIMENT,
        COMPLIMENT_AGAIN,
        HOME_TOUR,
        STORY
    }

    private static final String[] FIRST_MEETING = {
            "Oh! A new face around here. I'm %NAME%, the local %JOB%.",
            "A traveler! We don't get many. The name's %NAME% - I work as a %JOB%.",
            "Hello, stranger. %NAME%'s the name. If you ever need a %JOB%, that's me.",
            "Welcome to my little corner of the %BIOME%. I'm %NAME%.",
            "Ooh, a visitor! I'm %NAME%. Wipe your boots, mind the flowers, and welcome!",
            "You're not from around here, are you? I'm %NAME%. Pleasure!",
            "Fresh footprints on my path! I'm %NAME%. Stick around — %SEASON% is a fine season to make friends."
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
            "Oh, hello! I was just thinking about lunch.",
            "%SEASON% %DAY% already. Time moves oddly out here."
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

    private static final String[] GIFT_BIRTHDAY_LINES = {
            "A birthday gift?! %ITEM% on MY day? %PLAYER%, you've made this year unforgettable!",
            "You remembered my birthday AND brought %ITEM%? I might actually cry.",
            "Birthday AND %ITEM%! The stars aligned. Thank you, thank you!"
    };

    private static final String[] BIRTHDAY_GREET = {
            "It's my birthday today! %BIRTHDAY%! Don't make it weird... unless you brought cake.",
            "Guess what day it is? Mine. Birthday. Just saying.",
            "I've been humming birthday tunes since dawn. %BIRTHDAY% vibes only."
    };

    private static final String[] FESTIVAL_LINES = {
            "Happy %FESTIVAL%! The whole hamlet feels alive today.",
            "%FESTIVAL% is my favorite excuse to talk to everyone. Including you!",
            "Did you see the plaza? %FESTIVAL% decorations... well, imaginary ones. We make do."
    };

    private static final String[] ERRAND_OFFER_LINES = {
            "Hey %PLAYER% — could you bring me %COUNT%x %NEED%? I'd owe you one.",
            "Help wanted: I need %COUNT%x %NEED%. Think you can swing by later?",
            "If you're adventuring anyway, grab %COUNT%x %NEED% for me? Pretty please."
    };

    private static final String[] ERRAND_PROGRESS_LINES = {
            "Still need %COUNT%x %NEED%, %PLAYER%. No rush — well, a little rush.",
            "Any luck finding %NEED%? I need %COUNT% of them.",
            "My errand stands: %COUNT%x %NEED%. You're my hero-in-progress."
    };

    private static final String[] ERRAND_COMPLETE_LINES = {
            "You actually brought it! %NEED%! You're a legend, %PLAYER%.",
            "Errand complete! Take this friendship bump — you've earned it.",
            "Perfect timing. %NEED% received. I won't forget this favor."
    };

    private static final String[] ADVENTURE_TIPS = {
            "Cartographer tip: solitary houses hide in every major biome — towns are rarer and worth the hike.",
            "If you ever get lost, /evamod town finds hamlets without needing vanilla locate.",
            "I heard a miner muttering about deep caves west of here. Or east. Directions aren't my job.",
            "Bring a bedroll? Nah — every house has a bed. Towns have several. Handy.",
            "Festival days make everyone chatty. Check /evamod calendar so you don't miss one.",
            "Birthdays are written in the stars — check your journal. Gifts that day hit different.",
            "Curious folk leave rumors. Follow a few and the world opens up."
    };

    private static final String[] ASK_DAY_LINES = {
            "%SEASON% %DAY%. I measured the morning by how warm the kettle felt.",
            "Today? Busy in a gentle way. The %BIOME% is humming.",
            "Ask me again at dusk — mornings and evenings are different people.",
            "I woke up thinking about %FAVORITE%. That usually means a good day.",
            "The sky looks like chapter three of a long story. That's my weather report."
    };

    private static final String[] COMPLIMENT_LINES = {
            "Oh! You — stop. No, don't stop. That was lovely, %PLAYER%.",
            "Compliments? In THIS economy? I'll take it.",
            "You're going to make me rearrange my whole afternoon around smiling.",
            "Keep talking like that and I'll start charging rent for standing here.",
            "Thank you. I tucked that kind word next to my favorite button."
    };

    private static final String[] COMPLIMENT_AGAIN_LINES = {
            "One compliment a day keeps the ego manageable. Tomorrow, promise.",
            "You're sweet, but my blush quota is full until dawn.",
            "Save some praise for the bees. They work harder than I do."
    };

    private static final String[] HOME_TOUR_LINES = {
            "Welcome to my %BIOME% home! Bed in back, kettle up front, pride everywhere.",
            "Tour time: door (obviously), work corner for a %JOB%, and a bed that knows my name.",
            "Mind the carpet runner — it's decorative AND a pathfinding suggestion.",
            "I planted the flowers myself. Mostly. The wind helped. Rude wind.",
            "If you ever need a roof in a storm, this one has held through worse moods than mine."
    };

    private static final String[] STORY_LINES = {
            "Once, a traveler left a map with no labels. I still chase those empty circles.",
            "There's a rumor of a town plaza that throws festivals just by existing. I've seen it — almost.",
            "My favorite story ends with soup. All the best ones do.",
            "I met a stuffed animal that glowed like a lantern. Don't tell it I said 'stuffed.'",
            "If you keep helping neighbors, the world starts helping you back. Quietly. Happily."
    };

    private static final Map<Integer, String[]> HEART_EVENTS = Map.of(
            2, new String[]{
                    "We've talked enough that I trust you a little. That's rare for me.",
                    "Two hearts already? Look at us, becoming neighbors."},
            4, new String[]{
                    "I saved you a seat by the window. Figuratively. Emotionally. It's yours.",
                    "Four hearts! Want to hear a secret? I name the pumpkins."},
            6, new String[]{
                    "You're one of my closest friends in the %BIOME%, %PLAYER%. Don't vanish on me.",
                    "Six hearts. I'd lend you my spare boots. They're only slightly muddy."},
            8, new String[]{
                    "I wrote your name in my little book of Important People. In ink!",
                    "Eight hearts. If I ever throw a feast, you're at the head of the table."},
            10, new String[]{
                    "Ten hearts. You're family now. Messy, wonderful, chosen family.",
                    "Maximum hearts! Whatever happens out there, you have a home here."}
    );

    private static final Map<NpcPersonality, String[]> PERSONALITY_TALK = new EnumMap<>(NpcPersonality.class);
    private static final Map<NpcJob, String[]> JOB_SMALL_TALK = new EnumMap<>(NpcJob.class);
    private static final Map<NpcVariant, String[]> BIOME_SMALL_TALK = new EnumMap<>(NpcVariant.class);
    private static final Map<SeasonCalendar.Season, String[]> SEASON_TALK = new EnumMap<>(SeasonCalendar.Season.class);

    static {
        PERSONALITY_TALK.put(NpcPersonality.CHEERFUL, new String[]{
                "Isn't today just the BEST day? I mean, look at it!",
                "I hummed a little song while working today. Want to hear it? Too late, humming again!",
                "Smiling is free, you know. I stock up every morning.",
                "Every sunrise here is my favorite sunrise.",
                "I waved at a bee earlier. It waved back. Probably."});
        PERSONALITY_TALK.put(NpcPersonality.SHY, new String[]{
                "Oh, um. Hello. Sorry, I'm not great at... this.",
                "I practiced saying hi in the mirror this morning. It went better than this.",
                "You can stay, just... maybe don't stand SO close?",
                "I like quiet days. Today is nice and quiet. Well, it was.",
                "If I speak softly, it's not you — it's just my volume knob."});
        PERSONALITY_TALK.put(NpcPersonality.DRAMATIC, new String[]{
                "The wind! The sky! The DRAMA of it all!",
                "I have seen a thousand sunsets and each one has CHANGED me.",
                "My life is a saga, %PLAYER%, and you have just entered chapter twelve.",
                "One day they will write songs about my work. Beautiful, tragic songs.",
                "%SEASON% arrives like a curtain rising. Enter: me."});
        PERSONALITY_TALK.put(NpcPersonality.GRUMPY, new String[]{
                "Back in my day, creepers kept a respectful distance.",
                "My knees ache. That means rain. Or visitors. Apparently both.",
                "I'm not grumpy. This is just my face.",
                "Everything was better yesterday. It always is.",
                "If you're selling optimism, I'm not buying."});
        PERSONALITY_TALK.put(NpcPersonality.SLEEPY, new String[]{
                "*yawn* Sorry, you were saying?",
                "I had a nap planned for right about... now, actually.",
                "Five beds. If I were rich, I'd own five beds. One for each mood.",
                "Some people count emeralds. I count sheep. Professionally.",
                "Wake me for %FESTIVAL%. Or cake. Cake works."});
        PERSONALITY_TALK.put(NpcPersonality.CURIOUS, new String[]{
                "Say, what's it like out past the %BIOME%? Tell me everything.",
                "I found a weird rock today. It's in my pocket. Want to see? ...Maybe later.",
                "Do you think fish know they're wet? I think about this a lot.",
                "Every chest I see, I wonder what's inside. Every. Single. One.",
                "Bring me rumors. I'll trade you better rumors."});
        PERSONALITY_TALK.put(NpcPersonality.KIND, new String[]{
                "If you need a spare loaf or a kind word, my door is open.",
                "Everyone deserves a warm hello. Even the creepers. Figuratively.",
                "I left soup on for whoever looks tired. That includes you, %PLAYER%.",
                "Kindness compounds faster than emeralds. I keep receipts."});
        PERSONALITY_TALK.put(NpcPersonality.WITTY, new String[]{
                "I'd tell you a joke about construction, but I'm still working on it.",
                "My humor is free. My trades are not. Balance, %PLAYER%.",
                "They say I have a sharp tongue. Good — dull ones make poor tools.",
                "If sarcasm were a crop, I'd be the richest farmer in the %BIOME%."});
        PERSONALITY_TALK.put(NpcPersonality.MYSTERIOUS, new String[]{
                "Some paths only appear when the moon is... never mind.",
                "I keep three secrets. You've earned approximately zero-point-four.",
                "The fog knows my name. I prefer it that way.",
                "Ask me later. Or don't. Timing is everything."});
        PERSONALITY_TALK.put(NpcPersonality.BRAVE, new String[]{
                "Fear is just excitement wearing a heavier coat.",
                "I once stared down a skeleton. It blinked first. Probably.",
                "If trouble comes, stand behind me — or beside me. Standing matters.",
                "Courage isn't loud. It's showing up when the path gets steep."});
        JOB_SMALL_TALK.put(NpcJob.FARMER, new String[]{
                "The crops practically grow themselves this season.",
                "A good harvest starts with good soil, I always say.",
                "Rabbits got into the carrots again. Nature's little thieves.",
                "I name my pumpkins. Don't tell anyone. The big one is Gerald.",
                "%SEASON% soil has a personality. Today's is stubborn."});
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
                "X marks the spot. Unless I sneezed while drawing. Then Y does.",
                "Towns show up rarer on my sketches — worth circling in red."});
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
        JOB_SMALL_TALK.put(NpcJob.BAKER, new String[]{
                "The smell of fresh bread solves most arguments. Scientifically.",
                "Kneading dough is meditation with flour on your nose.",
                "I burned a loaf once. Never again. The smoke told on me.",
                "Sweet or savory? Wrong question. Both. Always both.",
                "Wheat, patience, and a friendly oven — that's the whole recipe."});
        JOB_SMALL_TALK.put(NpcJob.BEEKEEPER, new String[]{
                "The bees and I have a treaty: I give flowers, they give gold.",
                "Never swat near a hive. Diplomacy first.",
                "Honeycomb geometry puts architects to shame.",
                "If you hear buzzing, smile. They're working harder than we are.",
                "A calm beekeeper makes calm bees. Mostly."});
        JOB_SMALL_TALK.put(NpcJob.STORYTELLER, new String[]{
                "Every traveler leaves a chapter. You're mid-paragraph, %PLAYER%.",
                "I collect endings. Beginnings are easier to find.",
                "Pull up a seat — the best stories start with 'you won't believe this.'",
                "Books are quiet friends. Lectures are louder ones.",
                "Once upon a time is my favorite spell."});
        JOB_SMALL_TALK.put(NpcJob.ARCHAEOLOGIST, new String[]{
                "Brush gently. History bruises easily.",
                "Pottery shards gossip if you listen long enough.",
                "Suspicious sand is just shy treasure.",
                "I dug up a mystery yesterday. Still unnamed. Still wonderful.",
                "The past isn't gone — it's underfoot, waiting."});

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
        BIOME_SMALL_TALK.put(NpcVariant.OCEAN, new String[]{
                "Salt air cures almost everything except homesickness.",
                "The tide brings news — bottles, driftwood, rumors.",
                "Watch the horizon. Something always arrives eventually."});
        BIOME_SMALL_TALK.put(NpcVariant.CHERRY, new String[]{
                "Petals fall like confetti that never needs cleaning up. Almost.",
                "Cherry blossom season makes everyone softer. Even me.",
                "Pink skies, pink paths, pink moods. Welcome."});
        BIOME_SMALL_TALK.put(NpcVariant.BADLANDS, new String[]{
                "The mesas keep score of every sunset.",
                "Red dust gets everywhere. Including your stories.",
                "Water is wealth out here. Shade is luxury."});
        BIOME_SMALL_TALK.put(NpcVariant.MUSHROOM, new String[]{
                "Don't step on the big ones. They have feelings. Allegedly.",
                "Mycelium networks gossip faster than villagers.",
                "Night glows gently here. No torches required — almost."});
        BIOME_SMALL_TALK.put(NpcVariant.DARK, new String[]{
                "The canopy drinks the daylight. We learned to sip what's left.",
                "Roots run deep. So do old stories.",
                "If you hear a twig snap, it was probably just the forest saying hello."});
        BIOME_SMALL_TALK.put(NpcVariant.MOUNTAIN, new String[]{
                "Thin air, thick views. Fair trade.",
                "Peaks teach patience — and stronger calves.",
                "The wind up here edits every sentence. Shorter is better."});
        SEASON_TALK.put(SeasonCalendar.Season.SPRING, new String[]{
                "Spring air makes even grumps plant flowers. Allegedly.",
                "Shoots are pushing up. Hope always looks like green sprouts.",
                "Mud season is character-building. That's what I tell my boots."});
        SEASON_TALK.put(SeasonCalendar.Season.SUMMER, new String[]{
                "Long days, short tempers, tall sunflowers.",
                "Summer laziness is a professional skill. I'm certified.",
                "If you melt, leave a note. We'll sweep you up at dusk."});
        SEASON_TALK.put(SeasonCalendar.Season.FALL, new String[]{
                "Fall smells like woodsmoke and second chances.",
                "Leaves fall. So do plans. We rake both.",
                "Harvest tables should always have an empty plate for travelers."});
        SEASON_TALK.put(SeasonCalendar.Season.WINTER, new String[]{
                "Winter teaches thrift and thicker socks.",
                "Snow makes every path a suggestion.",
                "Come in close — cold stories need warm listeners."});
    }

    public static String pickLine(BiomeNpc npc, NpcMemory.Record record, String playerName,
                                  Context context, long day, RandomSource random) {
        return pickLine(npc, record, playerName, context, day, random, null, null);
    }

    public static String pickLine(BiomeNpc npc, NpcMemory.Record record, String playerName,
                                  Context context, long day, RandomSource random,
                                  ServerPlayer player, Errand activeErrand) {
        String line = switch (context) {
            case GREETING -> pickGreeting(npc, record, day, random, player);
            case SMALL_TALK -> pickSmallTalk(npc, record, day, random, player);
            case GIFT_FAVORITE -> pick(GIFT_FAVORITE_LINES, random);
            case GIFT_LIKED -> pick(GIFT_LIKED_LINES, random);
            case GIFT_MEH -> pick(GIFT_MEH_LINES, random);
            case GIFT_JUNK -> pick(GIFT_JUNK_LINES, random);
            case GIFT_ALREADY -> pick(GIFT_ALREADY_LINES, random);
            case GIFT_BIRTHDAY -> pick(GIFT_BIRTHDAY_LINES, random);
            case WOKEN -> pick(WOKEN_LINES, random);
            case HEART_EVENT -> pickHeartEvent(record, random);
            case ERRAND_OFFER -> pick(ERRAND_OFFER_LINES, random);
            case ERRAND_PROGRESS -> pick(ERRAND_PROGRESS_LINES, random);
            case ERRAND_COMPLETE -> pick(ERRAND_COMPLETE_LINES, random);
            case FESTIVAL -> pick(FESTIVAL_LINES, random);
            case BIRTHDAY -> pick(BIRTHDAY_GREET, random);
            case ADVENTURE_TIP -> pick(ADVENTURE_TIPS, random);
            case ASK_DAY -> pick(ASK_DAY_LINES, random);
            case COMPLIMENT -> pick(COMPLIMENT_LINES, random);
            case COMPLIMENT_AGAIN -> pick(COMPLIMENT_AGAIN_LINES, random);
            case HOME_TOUR -> pick(HOME_TOUR_LINES, random);
            case STORY -> pick(STORY_LINES, random);
        };
        return substitute(line, npc, playerName, record, day, activeErrand);
    }

    private static String pickHeartEvent(NpcMemory.Record record, RandomSource random) {
        int hearts = Hearts.fromReputation(record.reputation);
        String[] pool = HEART_EVENTS.get(hearts);
        if (pool == null) {
            pool = HEART_EVENTS.get(2);
        }
        return pick(pool, random);
    }

    private static String pickGreeting(BiomeNpc npc, NpcMemory.Record record, long day,
                                       RandomSource random, ServerPlayer player) {
        if (record.isStranger() || record.talks == 0) {
            return pick(FIRST_MEETING, random);
        }
        if (record.lastHitDay == day && record.reputation < 10) {
            return pick(HIT_TODAY, random);
        }
        if (record.lastHitDay >= 0 && day - record.lastHitDay <= 3 && record.reputation < 0) {
            return pick(HIT_RECENTLY, random);
        }
        if (npc.isBirthday(day)) {
            return pick(BIRTHDAY_GREET, random);
        }
        if (SeasonCalendar.isFestival(day) && record.moodTier() >= NpcMemory.MOOD_NEUTRAL) {
            return pick(FESTIVAL_LINES, random);
        }
        long daysSinceTalk = record.lastTalkDay >= 0 ? day - record.lastTalkDay : 0;
        if (daysSinceTalk >= 5 && record.moodTier() >= NpcMemory.MOOD_FRIENDLY) {
            return pick(LONG_TIME_NO_SEE, random);
        }
        if (record.moodTier() >= NpcMemory.MOOD_NEUTRAL) {
            if (npc.level().isRaining() && random.nextInt(3) == 0) {
                return pick(RAINY_DAY, random);
            }
            if (npc.level().isDarkOutside() && random.nextInt(3) == 0) {
                return pick(NIGHT_LINES, random);
            }
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

    private static String pickSmallTalk(BiomeNpc npc, NpcMemory.Record record, long day,
                                        RandomSource random, ServerPlayer player) {
        if (record.moodTier() == NpcMemory.MOOD_HOSTILE) {
            return pick(HOSTILE, random);
        }
        // Gossip about journal friends
        if (player != null && random.nextInt(5) == 0) {
            PlayerEvaData data = player.getData(com.eva.evamod.registry.ModAttachments.PLAYER_DATA);
            List<HouseIndexEntry> others = new ArrayList<>();
            for (HouseIndexEntry e : data.houses()) {
                if (!e.npcName().equalsIgnoreCase(npc.getNpcName())) {
                    others.add(e);
                }
            }
            if (!others.isEmpty()) {
                HouseIndexEntry other = others.get(random.nextInt(others.size()));
                return "Have you seen " + other.npcName() + " lately? That " + other.personality().toLowerCase(Locale.ROOT)
                        + " " + other.job().toLowerCase(Locale.ROOT) + " owes me a story.";
            }
        }
        if (record.moodTier() >= NpcMemory.MOOD_CLOSE_FRIEND && random.nextInt(4) == 0) {
            List<String> callbacks = new ArrayList<>();
            if (record.trades > 0) {
                callbacks.add("We've traded " + record.trades + " times now, %PLAYER%. Practically business partners!");
            }
            if (record.gifts > 0) {
                callbacks.add("That's " + record.gifts + " gifts you've given me. I keep count. Happily.");
            }
            callbacks.add("Remember when we first met? Day %FIRSTMET%. I remember everything.");
            callbacks.add("We're at %HEARTS%. Don't let it go to your head. Or do.");
            return callbacks.get(random.nextInt(callbacks.size()));
        }
        if (random.nextInt(6) == 0) {
            return pick(ADVENTURE_TIPS, random);
        }
        if (random.nextInt(4) == 0) {
            return pick(SEASON_TALK.get(SeasonCalendar.season(day)), random);
        }
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
                                     NpcMemory.Record record, long day, Errand errand) {
        long daysSinceTalk = record.lastTalkDay >= 0 ? Math.max(0, day - record.lastTalkDay) : 0;
        int hearts = Hearts.fromReputation(record.reputation);
        String need = errand != null ? errand.itemDisplayName() : "supplies";
        String count = errand != null ? Integer.toString(errand.count()) : "a few";
        return line
                .replace("%NAME%", npc.getNpcName())
                .replace("%JOB%", npc.getJob().getDisplayName().toLowerCase(Locale.ROOT))
                .replace("%BIOME%", npc.getVariant().getDisplayName().toLowerCase(Locale.ROOT))
                .replace("%PLAYER%", playerName)
                .replace("%DAYS%", Long.toString(daysSinceTalk))
                .replace("%FIRSTMET%", Long.toString(Math.max(0, record.firstMetDay)))
                .replace("%FAVORITE%", npc.getFavoriteGiftName())
                .replace("%ITEM%", npc.getLastGiftName())
                .replace("%SEASON%", SeasonCalendar.season(day).getDisplayName())
                .replace("%DAY%", Integer.toString(SeasonCalendar.dayInSeason(day)))
                .replace("%FESTIVAL%", SeasonCalendar.festivalName(day))
                .replace("%BIRTHDAY%", npc.getBirthdayLabel())
                .replace("%HEARTS%", Hearts.shortLabel(hearts))
                .replace("%NEED%", need)
                .replace("%COUNT%", count);
    }

    private DialogueManager() {
    }
}
