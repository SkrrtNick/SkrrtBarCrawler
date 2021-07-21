package scripts.event;

import org.tribot.api2007.Game;
import org.tribot.script.Script;
import scripts.data.Bars;
import scripts.data.Constants;
import scripts.skrrt_api.events.BotEvent;
import scripts.skrrt_api.events.Core;
import scripts.skrrt_api.util.functions.Interaction;
import scripts.skrrt_api.util.functions.Sleep;

import java.io.IOException;


public class StartCrawlEvent extends BotEvent {

    public StartCrawlEvent(Script script) {
        super(script);
    }

    @Override
    public String toString() {
        return "Starting Barcrawl";
    }


    @Override
    public void step() throws InterruptedException, IOException {
        Core.setStatus("Starting Barcrawl");
        if(Interaction.handleQuestNPC(Bars.START.getNpc(), Bars.START.getArea(), Constants.START_DIALOGUE)){
            Sleep.until(()->!isPendingOperation());
        }
        if(!isPendingOperation()){
            setComplete();
        }
    }

    public boolean isPendingOperation() {
        return (Game.getSetting(Constants.BARCRAWL_SETTING) == 0);
    }

}


