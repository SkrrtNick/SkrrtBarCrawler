package scripts.skrrt_api.events;

import lombok.Getter;
import lombok.Setter;

public class Core {
    @Getter @Setter
    public static String status;
    @Getter @Setter
    public static String log;
    @Getter @Setter
    public static double playerSeed = 0;
    @Getter @Setter
    public static boolean isRunning;
    @Getter @Setter
    public static String profileDirectory;
    @Getter @Setter
    public static long startTime;

}
