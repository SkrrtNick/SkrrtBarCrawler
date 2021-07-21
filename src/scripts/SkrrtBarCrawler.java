package scripts;


import dax.api_lib.DaxWalker;
import org.tribot.api.General;
import org.tribot.api2007.Game;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.Starting;
import scripts.data.Bars;
import scripts.data.Constants;
import scripts.event.BarEvent;
import scripts.event.EndCrawlEvent;
import scripts.event.InitialiseEvent;
import scripts.event.StartCrawlEvent;
import scripts.skrrt_api.events.Core;
import scripts.skrrt_api.util.functions.Logging;
import scripts.skrrt_api.util.functions.Traversing;
import scripts.skrrt_api.util.numbers.Randomisation;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class SkrrtBarCrawler extends Script implements Starting, PaintInfo, Painting {

    ArrayList<BarEvent> bars = new ArrayList<>();
    StartCrawlEvent startCrawlEvent = new StartCrawlEvent(this);
    EndCrawlEvent endCrawlEvent = new EndCrawlEvent(this);
    InitialiseEvent initialiseEvent = new InitialiseEvent(this);

    @ScriptManifest(name = "SkrrtBarCrawl", authors = {"SkrrtNick"}, category = "Tools")

    private final FluffeesPaint SkrrtPaint = new FluffeesPaint(this, FluffeesPaint.PaintLocations.BOTTOM_LEFT_PLAY_SCREEN, new Color[]{new Color(255, 251, 255)}, "Trebuchet MS", new Color[]{new Color(0, 0, 0, 124)},
            new Color[]{new Color(179, 0, 0)}, 1, false, 5, 3, 0);


    @Override
    public void run() {
        Randomisation.setMouseSpeed();
        bars.add(new BarEvent(this, Bars.BARTENDER_FLYING_HORSE_INN));
        bars.add(new BarEvent(this, Bars.BARTENDER_BLUE_MOON));
        bars.add(new BarEvent(this, Bars.BLURBERRY));
        bars.add(new BarEvent(this, Bars.BARTENDER_DEAD_MANS_CHEST));
        bars.add(new BarEvent(this, Bars.BARTENDER_DRAGON_INN));
        bars.add(new BarEvent(this, Bars.BARTENDER_FORESTERS_ARMS));
        bars.add(new BarEvent(this, Bars.BARTENDER_JOLLY_BOAR));
        bars.add(new BarEvent(this, Bars.KAYLEE));
        bars.add(new BarEvent(this, Bars.BARTENDER_RUSTY_ANCHOR));
        bars.add(new BarEvent(this, Bars.ZAMBO));
        bars.add(new BarEvent(this, Bars.BARTENDER_FLYING_HORSE_INN));
        Collections.shuffle(bars);

        if (Game.getSetting(Constants.BARCRAWL_SETTING) != 2) {
            try {
                if (!initialiseEvent.isComplete()) {
                    initialiseEvent.execute();
                }
                if (startCrawlEvent.isPendingOperation()) {
                    startCrawlEvent.execute();
                }
                for (BarEvent bar : bars) {
                    while (bar.isPendingOperation()) {
                        bar.execute();
                        General.sleep(20, 40);
                    }
                }
                if (endCrawlEvent.isPendingOperation()) {
                    Logging.message("SkrrtBarCrawler", "Turning in quest");
                    endCrawlEvent.execute();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Logging.message("SkrrtBarCrawler", "Bar crawl is already completed!");
        }
    }


    @Override
    public void onStart() {
        Traversing.setDaxKey(false);
        Core.setStatus("Initialising");
        Logging.message("SkrrtBarCrawler", "Welcome to Skrrt Bar Crawler, happy drinking!");
        DaxWalker.setGlobalWalkingCondition(Traversing.stamina);
    }

    @Override
    public String[] getPaintInfo() {
        return new String[]{"SkrrtBarCrawler V1.8b", "Time ran: " + SkrrtPaint.getRuntimeString(), "Status: " + Core.getStatus()};
    }


    @Override
    public void onPaint(Graphics graphics) {
        SkrrtPaint.paint(graphics);
    }


}