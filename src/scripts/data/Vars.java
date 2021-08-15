package scripts.data;

import lombok.Getter;
import lombok.Setter;
import org.tribot.api.General;

import java.util.ArrayList;

public class Vars {

    public static String status = "Logging in";
    public static ArrayList<String> dialogue = new ArrayList<>();
    public static boolean runningList = false;
    public static final int stamina = General.random(1,3);

    @Getter @Setter
    private static int beersDrank = 0;
    @Getter @Setter
    private static int crawlsCompleted = 0;

}
