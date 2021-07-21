package scripts.data;

import org.tribot.api.General;

import java.util.ArrayList;

public class Vars {

    public static boolean isRunning = true;
    public static String status = "Logging in";
    public static int i = 0;
    public static ArrayList<String> dialogue = new ArrayList<>();
    public static boolean initialCheck = false;
    public static boolean shouldBuyItems = false;
    public static boolean runningPrep = true;
    public static boolean runningList = false;
    public static final int stamina = General.random(1,3);

}
