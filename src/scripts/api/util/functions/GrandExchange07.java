package scripts.api.util.functions;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.GrandExchange;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.types.RSGEOffer;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSNPC;
import scripts.entityselector.Entities;
import scripts.entityselector.finders.prefabs.InterfaceEntity;
import scripts.entityselector.finders.prefabs.NpcEntity;
import scripts.api.util.numbers.Reactions;

public class GrandExchange07 extends GrandExchange {

private static final int GRAND_EXCHANGE_MASTER = 465;

    public static boolean open() {
        if (Interfaces.isInterfaceSubstantiated(GRAND_EXCHANGE_MASTER)) {
            return true;
        } else {
            RSNPC clerk = Entities.find(NpcEntity::new)
                    .nameEquals("Grand Exchange Clerk")
                    .sortByDistance()
                    .getFirstResult();
            if (clerk != null) {
                if(!clerk.isClickable()){
                    clerk.adjustCameraTo();
                }
                Clicking.click("Exchange Grand Exchange Clerk", clerk);
            }
            Sleep.until(()->Interfaces.isInterfaceSubstantiated(GRAND_EXCHANGE_MASTER),Reactions.getNormal());
        }
        return Interfaces.isInterfaceSubstantiated(GRAND_EXCHANGE_MASTER);
    }

    public static boolean close() {
        RSInterface close = Entities.find(InterfaceEntity::new)
                .inMaster(465)
                .actionContains("Close")
                .getFirstResult();
        if (close != null && !close.isHidden()) {
            return close.click();
        }
        return false;
    }


    public static boolean abort(int ID) {
        RSGEOffer[] offers = GrandExchange.getOffers();
        for (RSGEOffer o : offers) {
            if (o.getItemID() == ID) {
                return o.click("Abort offer");
            }
        }
        return false;
    }

}
