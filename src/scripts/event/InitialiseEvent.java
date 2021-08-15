package scripts.event;

import org.tribot.api.General;
import org.tribot.api2007.Game;
import org.tribot.script.Script;
import scripts.data.Constants;
import scripts.api.events.BotEvent;
import scripts.api.events.banking.BankEventV2;
import scripts.api.events.grandexchange.GEEvent;
import scripts.api.util.functions.*;
import scripts.api.items.ItemID;
import scripts.api.util.numbers.Prices;


import java.io.IOException;


public class InitialiseEvent extends BotEvent {
    GEEvent geEvent;
    BankEventV2 bankEvent;

    public InitialiseEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script)
                .addReq("Amulet of glory(6~3)", 1)
                .addReq("Ring of wealth (5~1)", 1)
                .addReq("Ring of dueling(8~4)", 1)
                .addReq("Games necklace(8~1)", 1)
                .addReq("Necklace of passage(5~1)", 1)
                .addReq("Stamina potion(4~2)", 2)
                .addReq("Camelot teleport", Integer.MAX_VALUE)
                .addReq("Varrock teleport", Integer.MAX_VALUE)
                .addReq("Barcrawl card", Integer.MAX_VALUE, () -> Game.getSetting(Constants.BARCRAWL_SETTING) != 0)
                .addReq("Coins",Integer.MAX_VALUE);
        geEvent = new GEEvent(script, bankEvent)
                .addReq("Ring of wealth (5~1)", 1, (int) (Prices.getPrices(ItemID.RING_OF_WEALTH_5).get() * 1.5))
                .addReq("Ring of dueling(8~4)", 1,(int)(Prices.getPrices(ItemID.RING_OF_DUELING8).get() * 1.5))
                .addReq("Games necklace(8~1)", 1,(int)(Prices.getPrices(ItemID.GAMES_NECKLACE8).get() * 1.5))
                .addReq("Necklace of passage(5~1)", 1,(int)(Prices.getPrices(ItemID.NECKLACE_OF_PASSAGE5).get() * 1.5))
                .addReq("Stamina potion(4~2)", General.random(1,2),(int)(Prices.getPrices(ItemID.STAMINA_POTION4).get() * 1.5))
                .addReq("Amulet of glory(6~3)", 1,(int)(Prices.getPrices(ItemID.AMULET_OF_GLORY6).get() * 1.5))
                .addReq("Camelot teleport", General.random(1,3), (int)(Prices.getPrices(ItemID.CAMELOT_TELEPORT).get() * 1.5))
                .addReq("Varrock teleport", General.random(1,2), (int)(Prices.getPrices(ItemID.CAMELOT_TELEPORT).get() * 2.5));
    }

    @Override
    public String toString() {
        return "Performing Initial Check";
    }


    @Override
    public void step() throws InterruptedException, IOException {
        if (bankEvent.needCache()) {
            if(!Banking07.isInBank()){
                Traversing.walkToBank();
            }
            if(Banking07.openBank()){
                Sleep.until(Banking07::isBankLoaded);
                Banking07.closeBankTutorial();
                if(!Inventory07.isEmpty()){
                    if(Banking07.depositAll()>0){
                        Sleep.until(Inventory07::isEmpty);
                    }
                }
                bankEvent.updateCache();
            }
        } else if (geEvent.isPendingOperation() && bankEvent.isPendingOperation()) {
            geEvent.execute();
            Logging.message("GEEvent","Completed GE");
            geEvent.reset();
        } else if (bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
            setComplete();
        }
    }

}

