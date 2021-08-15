package scripts.event;

import dax.walker_engine.interaction_handling.NPCInteraction;
import org.tribot.api2007.Game;
import org.tribot.script.Script;
import scripts.data.Bars;
import scripts.data.Constants;
import scripts.data.Vars;
import scripts.api.events.BotEvent;
import scripts.api.events.Core;

import scripts.api.util.functions.Interaction;
import scripts.api.util.functions.Sleep;

import java.io.IOException;

import static scripts.data.Vars.*;


public class EndCrawlEvent extends BotEvent {

    public EndCrawlEvent(Script script) {
        super(script);
    }

    @Override
    public String toString() {
        return "Performing Barbarian Guard Step";
    }

    @Override
    public void step() throws InterruptedException, IOException {
        Core.setStatus("Turning in Barcrawl");
        if(Interaction.handleQuestNPC(Bars.START.getNpc(), Bars.START.getArea(), Constants.END_DIALOGUE)){
            Sleep.until(()->!isPendingOperation());
            setComplete();
        }
    }

    public boolean isPendingOperation() {
        return  Game.getSetting(Constants.BARCRAWL_SETTING) == 2 || Game.getSetting(Constants.BARCRAWL_SETTING) == 8185;
    }
}


