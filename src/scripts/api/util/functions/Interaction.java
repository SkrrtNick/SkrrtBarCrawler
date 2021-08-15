package scripts.api.util.functions;


import dax.walker_engine.interaction_handling.NPCInteraction;

import org.tribot.api.General;

import org.tribot.api.types.generic.Filter;

import org.tribot.api2007.Game;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.*;

import scripts.api.util.numbers.Reactions;
import scripts.entityselector.Entities;
import scripts.entityselector.finders.prefabs.ItemEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class Interaction {

    public static boolean handleQuestNPC(int ID, RSArea area, String... dialogue) {
        if (!area.contains(Player07.getPosition())) {
            if (Traversing.walkTo(area)) {
                Sleep.until(() -> area.contains(Player07.getPosition()), Reactions.getNormal() * Player07.distanceTo(area.getRandomTile()));
            }
        }
        Filter<RSNPC> npc = Filters.NPCs.idEquals(ID);
        if (npc == null) {
            Traversing.walkTo(area);
            return false;
        } else {
            if (NPCInteraction.clickNpcAndWaitChat(npc, "Talk-to")) {
                Sleep.until(NPCInteraction::isConversationWindowUp);
                if (NPCInteraction.isConversationWindowUp()) {
                    NPCInteraction.handleConversation(dialogue);
                    General.sleep(Reactions.getSemiAFK());
                }
            }
        }
        return !NPCInteraction.isConversationWindowUp();
    }

    public static boolean selectItem(List<Integer> id1, String action) {
        General.sleep(Reactions.getPredictable());
        for (Integer id : id1) {
            int decision = Reactions.getDecision(3);
            if (decision < 100) {
                RSItem[] item1 = (Entities.find(ItemEntity::new)
                        .idEquals(id)
                        .getResults());
                if (item1.length > 0) {
                    Logging.debug("Attempting to click " + item1[0].getDefinition().getName());
                    item1[General.random(0, item1.length - 1)].click(action);
                }
            } else if (decision <= 200) {
                Optional<RSItem> item1 = Arrays.stream(Entities.find(ItemEntity::new)
                        .idEquals(id)
                        .getResults()).min(ItemEntity.getClosestComparator());
                if (item1.isPresent()) {
                    Logging.debug("Attempting to click " + item1.get().getDefinition().getName());
                    item1.get().click(action);
                }
            } else {
                RSItem item1 = Entities.find(ItemEntity::new)
                        .idEquals(id)
                        .getFirstResult();
                if (item1 != null) {
                    Logging.debug("Attempting to " + action + " " + item1.getDefinition().getName());
                    item1.click(action);
                }
            }

        }
        return Game.getItemSelectionState() == 1;
    }
}

