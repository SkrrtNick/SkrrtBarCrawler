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

public class BarEvent extends BotEvent {
    public Bars bar;
    public int npcID;

    public BarEvent(Script script, Bars bar) {
        super(script);
        this.bar = bar;
        this.npcID = bar.getNpc();
    }

    @Override
    public void step() throws InterruptedException, IOException {
        Core.setStatus(bar.toString());
        if(Interaction.handleQuestNPC(bar.getNpc(), bar.getArea(), bar.getDialogue())){
            Sleep.until(()->bar.isCompleted());
        }
        if(bar.isCompleted()){
            setComplete();
        }
    }

    public boolean isPendingOperation() {
        return !bar.isCompleted();
    }

}
