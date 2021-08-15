package scripts.data;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

public class Constants {

    public static final RSArea BARBARIAN_GUARD_AREA = new RSArea(
            new RSTile(2540, 3574, 0),
            new RSTile(2545, 3561, 0));

    public static final String BARCRAWL_DIALOGUE = "I'm doing Alfred Grimhand's Barcrawl.";
    public static final String BARCRAWL_DIALOGUE2 = "I'm doing Alfred Grimhands Barcrawl.";

    //BLUE MOON INN
    public static final RSArea BARTENDER_BLUE_MOON_AREA = new RSArea(
            new RSTile(3222, 3401, 0),
            new RSTile(3226, 3396, 0));

    //JOLLY BOAR
    public static final RSArea BARTENDER_JOLLY_BOAR_AREA = new RSArea(
            new RSTile(3275, 3495, 0),
            new RSTile(3282, 3486, 0));

    //RISING SUN
    public static final RSArea KAYLEE_AREA = new RSArea(
            new RSTile(2954, 3373, 0),
            new RSTile(2959, 3369, 0));

    //RUSTY ANCHOR
    public static final RSArea BARTENDER_RUSTY_ANCHOR_AREA = new RSArea(
            new RSTile(3045, 3258, 0),
            new RSTile(3051, 3256, 0));

    //ZAMBO
    public static final RSArea ZAMBO_AREA = new RSArea(
            new RSTile(2923, 3146, 0),
            new RSTile(2929, 3142, 0));

    //DEAD MANS CHEST
    public static final RSArea BARTENDER_DEAD_MANS_CHEST_AREA = new RSArea(
            new RSTile(2799, 3159, 0),
            new RSTile(2793, 3155, 0));

    //FLYING HORSE INN
    public static final RSArea BARTENDER_FLYING_HORSE_INN_AREA = new RSArea(
            new RSTile(2573, 3325, 0),
            new RSTile(2574, 3319, 0));

    //Foresters Arms
    public static final RSArea BARTENDER_FORESTERS_ARMS_AREA = new RSArea(
            new RSTile(2689, 3497, 0),
            new RSTile(2694, 3491, 0));

    //BLURBERRY
    public static final RSArea BLURBERRY_AREA = new RSArea(
            new RSTile(2481, 3496, 1),
            new RSTile(2483, 3494, 1));

    //DRAGONINN
    public static final RSArea BARTENDER_DRAGON_INN_AREA = new RSArea(
            new RSTile(2553, 3081, 0),
            new RSTile(2557, 3077, 0));

    public static final String[] START_DIALOGUE = {"I want to come through this gate.",
            "Looks can be deceiving, I am in fact a barbarian."};
    public static final String[] END_DIALOGUE = {"Yes please, I want to smash my vials.", "No thank you, I like smashing them."};

    public static final int BARCRAWL_SETTING = 77;
    public static final int NOT_STARTED_BIT = 0;
    public static final int BLUE_MOON_INDEX = 3;
    public static final int JOLLY_BOAR_INDEX = 9;
    public static final int RISING_SUN_INDEX = 11;
    public static final int RUSTY_ANCHOR_INDEX = 12;
    public static final int ZAMBO_INDEX = 10;
    public static final int DEAD_MANS_CHEST_INDEX = 5;
    public static final int FLYING_HORSE_INDEX = 7;
    public static final int FORESTER_ARMS_INDEX = 8;
    public static final int BLURBERRY_INDEX = 4;
    public static final int DRAGON_INN_INDEX = 6;


}
