package scripts.api.util.functions;


import dax.api_lib.DaxWalker;
import dax.api_lib.WebWalkerServerApi;
import dax.api_lib.models.DaxCredentials;
import dax.api_lib.models.DaxCredentialsProvider;
import dax.shared.helpers.BankHelper;
import dax.walker_engine.WalkingCondition;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Game;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.DPathNavigator;
import scripts.entityselector.Entities;
import scripts.entityselector.finders.prefabs.NpcEntity;
import scripts.entityselector.finders.prefabs.ObjectEntity;
import scripts.api.util.antiban.Antiban;
import scripts.api.items.ItemCollections;
import scripts.api.util.numbers.Reactions;


public class Traversing {
    private static final int[] GRAND_EXCHANGE_TELEPORTS = {11988, 11986, 11984, 11982, 11980, 8007, 1706, 1708, 1710, 1712, 11976, 11978};
    private static final RSArea GRAND_EXCHANGE = new RSArea(new RSTile(3160, 3494, 0), new RSTile(3169, 3485, 0));
    private static int attempt = 0;

    public static void walkToBank() {
        if (!BankHelper.isInBank()) {
            Antiban.activateRun();
            DaxWalker.walkToBank();
            Sleep.until(Banking07::isInBank);
        }
    }

    public static boolean walkTo(RSArea area) {
        RSTile tile = area.getRandomTile();
        if (!area.contains(Player.getPosition())) {
            if (DaxWalker.walkTo(tile, () -> {
                Antiban.activateRun();
                if (area.contains(Player07.getPosition())) {
                    return WalkingCondition.State.EXIT_OUT_WALKER_SUCCESS;
                }
                return WalkingCondition.State.CONTINUE_WALKER;
            })) {
                Logging.message("Walker", "Navigating to " + area.getRandomTile());
                Sleep.until(()->area.contains(Player07.getPosition()));
                attempt = 0;
            } else {
                attempt++;
                Logging.debug("Walker failed to produce a path, attempt: " + attempt);
                if (attempt == 3) {
                    Logging.debug("Walker failed to produce a path 3 times, using DPathNavigator");
                    DPathNavigator path = new DPathNavigator();
                    path.traverse(tile);
                    Sleep.until(()->area.contains(Player07.getPosition()));
                }
            }
            Sleep.until(()->area.contains(Player07.getPosition()));
        }
        return area.contains(Player.getPosition());
    }

    public static void setDaxKey(boolean publicKey) {
        WebWalkerServerApi.getInstance().setDaxCredentialsProvider(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                if (publicKey) {
                    return new DaxCredentials("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY");
                } else {
                    return new DaxCredentials("REDACTED", "REDACTED");
                }
            }
        });
    }

    public static boolean drinkStamina() {
        if (Inventory07.getCount(ItemCollections.getStaminaPotions()) == 0) {
            return false;
        } else {
            if (Game.getRunEnergy() < General.random(1, 20)) {
                int run = Game.getRunEnergy();
                Interaction.selectItem(ItemCollections.getStaminaPotions(), "Drink");
                Sleep.until(()->Game.getRunEnergy() > run);
                return true;
            }
        }
        return false;
    }

    public static WalkingCondition stamina = () -> {
        drinkStamina();
        Antiban.activateRun();
        return WalkingCondition.State.CONTINUE_WALKER;
    };

}
