package scripts.skrrt_api.events.banking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.script.Script;

import dax.api_lib.DaxWalker;
import scripts.skrrt_api.events.BotEvent;
import scripts.skrrt_api.events.Core;
import scripts.skrrt_api.events.inventory.InventoryEvent;
import scripts.skrrt_api.events.RequisitionItem;
import scripts.skrrt_api.util.functions.Banking07;
import scripts.skrrt_api.util.functions.Inventory07;
import scripts.skrrt_api.util.functions.Logging;
import scripts.skrrt_api.util.functions.Sleep;

import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankEventV2 extends BotEvent {

    public HashMap<String, BankCache> bankCacheHashMap = new HashMap<>();

    public LinkedHashMap<String, RequisitionItem> withdrawList = new LinkedHashMap<>();

    public BankEventV2(Script script) {
        super(script);
    }

    String finalItem;

    @Override
    public void step() throws InterruptedException, IOException {
        Core.setStatus("Banking");
        if(GrandExchange.getWindowState() != null) {
            GrandExchange.close();
            Timing.waitCondition( () -> GrandExchange.getWindowState() == null, 4000);
        } else
        if (!Banking.isBankScreenOpen()) {
            RSObject[] bank = Objects.find(20, "Bank booth");
            RSNPC[] banker = NPCs.find("Banker");
            RSObject[] chest = Objects.find(20, "Bank chest", "Open chest");
            if (bank.length < 1 && banker.length < 1 && chest.length < 1) {
                Logging.message("BankEvent","Walking to closest bank");
                DaxWalker.walkToBank();
                Timing.waitCondition( () -> Banking.isInBank() || !Player.getRSPlayer().isMoving(), 20000);
            } else if (Banking.openBank()) {
                Logging.message("BankEvent","Opening Bank");
                Sleep.until(Banking07::isBankLoaded);
                Banking07.closeBankTutorial();
            }
            return;
        }

        if (!Banking.isBankLoaded()) {
            return;
        }

        if (!InventoryEvent.containsOnly(arryOfItemsToWithdraw())) {
            Logging.message("BankEvent","Banking un-required items");
            Banking.depositAllExcept(arryOfItemsToWithdraw());
        }

        for (Map.Entry<String, RequisitionItem> withdrawList : withdrawList.entrySet()) {
            RequisitionItem reqItem = withdrawList.getValue();
            String itemName = reqItem.getName();
            int amount = reqItem.getQuantity();
            Supplier<Boolean> itemCondition = reqItem.getCondition();
            boolean noted = reqItem.getNoted();
            finalItem = "";

            if (!itemCondition.get()) {
                Logging.message("BankEvent","BankEvent: Banking un-required items part2");
                Logging.message("BankEvent","Checking item: " + itemName + " amount: " + amount + " noted: " + noted); //You're not checking finalItem we're checking the itemName
                Banking.deposit(Integer.MAX_VALUE, itemName);
                continue;
            }

            if (itemName.contains("~")) {
                if (finalItem.equals("")) {
                    List<String> expandedItem = expandItemName(itemName);
                    for (String item : expandedItem) {
                        if (contains(item)) {
                            finalItem = item;
                            break;
                        }
                    }
                }
            } else {
                finalItem = itemName;
            }
            RSItem finalRsItem = InventoryEvent.getInventoryItem(finalItem);
            RSItemDefinition finalItemDefinition = null;
            if(finalRsItem != null) {
                finalItemDefinition = finalRsItem.getDefinition();
            }
            Logging.message("BankEvent","Checking item: " + finalItem + " amount: " + amount + " noted: " + noted);

            if (InventoryEvent.contains(finalRsItem) && finalItemDefinition != null && !finalItemDefinition.isNoted() && noted) {
                Banking.deposit(InventoryEvent.getCount(itemName), itemName);
                Timing.waitCondition(() -> !InventoryEvent.contains(itemName), 2000);
            } else if (!contains(finalItem) && InventoryEvent.getInventoryItem(finalItem) == null) {
                Logging.message("BankEvent","Stopping we dont have item '"+itemName+"' in bank");
                setComplete();
            } else if (!InventoryEvent.contains(finalItem)) {
                boolean setStatus = setWithdrawNoted(noted);
                if (!setStatus) {
                    continue;
                } else if (noted) {
                    Logging.message("BankEvent","Withdrawing noted: " + finalItem);
                }
                if (Banking.withdraw(amount, finalItem)) {
                    Timing.waitCondition(() -> InventoryEvent.contains(finalItem), 2000);
                }
            } else if (InventoryEvent.contains(finalItem) && finalRsItem != null) {
                boolean isStackable = finalItemDefinition != null && finalItemDefinition.isStackable();
                int itemCount = isStackable ? finalRsItem.getStack() : InventoryEvent.getCount(finalItem);
                boolean shouldWithdraw = itemCount < amount;
                boolean setWithdrawStatus = setWithdrawNoted(noted);
                BooleanSupplier bankWaitCondition = isStackable ? () -> InventoryEvent.getStackedCount(finalItem) == amount : () -> InventoryEvent.getCount(finalItem) == amount;

                if (shouldWithdraw && Banking.withdraw(amount - itemCount, finalItem)) {
                    Timing.waitCondition(bankWaitCondition, 2000);
                } else if (!shouldWithdraw && Banking.deposit(itemCount - amount, finalItem)) {
                    Timing.waitCondition(bankWaitCondition, 2000);
                }
            }
        }
        Logging.message("BankEvent","Trying to update cache");
        updateCache();
        setComplete();
    }

    public void depositAll() {
        if(Banking.isBankScreenOpen()) {
            if(Inventory.getAll().length > 0) {
                Banking.depositAll();
                Sleep.until(Inventory07::isEmpty);
            }
        }
    }

    public void setWithdrawList(String itemName, int amount, boolean noted, Supplier<Boolean> condition) {
        if (withdrawList.containsKey(itemName)) {
            if (withdrawList.get(itemName).getQuantity() != amount) {
                withdrawList.replace(itemName, new RequisitionItem(itemName, amount, noted, condition));
            }
        } else {
            withdrawList.put(itemName, new RequisitionItem(itemName, amount, noted, condition));
        }
    }

    public BankEventV2 addReq(String itemName, int amount) {
        setWithdrawList(itemName, amount, false, () -> true);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, false, condition);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, boolean noted) {
        setWithdrawList(itemName, amount, noted, () -> true);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, boolean noted, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, noted, condition);
        return this;
    }

    public static boolean setWithdrawNoted(boolean enable) {
        return enable ? setNoted() : setUnNoted();
    }

    public static boolean setNoted() {
        if (Game.getSetting(115) != 1) {
            Interfaces.get(12, 23).click("Note");
        }
        return Game.getSetting(115) == 1;
    }

    public static boolean setUnNoted() {
        if (Game.getSetting(115) == 1) {
            Interfaces.get(12, 21).click("Item");
        }
        return Game.getSetting(115) != 1;
    }

    public String[] arryOfItemsToWithdraw() {
        Logging.message("BankEvent",withdrawList.keySet().toString());
        return withdrawList.keySet().toArray(new String[0]);
    }

    public boolean isPendingOperation() {
        for (Map.Entry<String, RequisitionItem> withdrawList : withdrawList.entrySet()) {
            String itemName = withdrawList.getKey();
            finalItem = "";
            Supplier<Boolean> cond = withdrawList.getValue().getCondition();
            boolean noted = withdrawList.getValue().getNoted();
            if (cond.get()) {
                if (itemName.contains("~")) {
                    List<String> expandedItem = expandItemName(itemName);
                    if (expandedItem.size() > 0) {
                        for (String item : expandedItem) {
                            if (InventoryEvent.contains(item)) {
                                finalItem = item;
                                break;
                            }
                        }
                    }
                } else {
                    finalItem = itemName;
                }
                RSItem item = InventoryEvent.getInventoryItem(finalItem);
                if (noted) {
                    if (item != null) {
                        if (!InventoryEvent.contains(item)) {
                            Logging.message("BankEvent","We dont have item: (a)" + itemName);
                            return true;
                        } else if (!item.getDefinition().isNoted()) {
                            Logging.message("BankEvent","We need noted: " + finalItem);
                            return true;
                        }
                    } else {
                        return true;
                    }

                } else if (!InventoryEvent.contains(item)) {
                    Logging.message("BankEvent","We dont have: (b)" + itemName);
                    return true;
                }
            }
        }
        return false;
    }

    public RSItem getBankItem(String... itemNames) {
        RSItem[] items = Banking.find(itemNames);
        for (RSItem item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public int getCount(String... itemNames) {
        int amount = 0;
        if (getBankItem(itemNames) != null) {
            amount = getBankItem(itemNames).getStack();
        }
        return amount;
    }

    public static boolean contains(String itemName) {
        return Banking.find(itemName).length > 0;
    }

    public void bankEquipment() {
        if (!Banking.isBankScreenOpen()) {
            if (!Banking.isInBank()) {
                DaxWalker.walkToBank();
            } else if (Banking.openBank()) {
                Timing.waitCondition(Banking::isBankScreenOpen, 2000);
            }
        } else {
            if (Banking.depositEquipment()) {
                Timing.waitCondition(() -> Equipment.getItems().length == 0, 2000);
            }
        }
    }

    public static List<String> expandItemName(String name) {
        ArrayList<String> names = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(.*?)([0-9]+)~([0-9]+)(.*?)$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String prepend = matcher.group(1), append = matcher.group(4);
            int start = Integer.parseInt(matcher.group(2)), finish = Integer.parseInt(matcher.group(3)),
                    dir = start > finish ? -1 : 1;
            for (int i = start; i * dir <= finish * dir; i += dir) {
                names.add(prepend + i + append);
            }
        } else {
            pattern = Pattern.compile("^(.*?)\\{(.*?)}(.*?)$");
            matcher = pattern.matcher(name);
            if (matcher.find()) {
                String prepend = matcher.group(1), append = matcher.group(3);
                String[] tings = matcher.group(2).split(";");
                for (String t : tings) {
                    names.add((prepend + t + append).trim());
                }
            } else {
                names.add(name);
            }
        }
        return names;
    }

    public void openBank() {
        if (!Banking.isBankScreenOpen()) {
            RSObject[] bank = Objects.find(20, "Bank booth");
            RSNPC[] banker = NPCs.find("Banker");
            RSObject[] chest = Objects.find(20, "Bank chest", "Open chest");
            if (bank.length < 1 && banker.length < 1 && chest.length < 1) {
                Logging.message("BankEvent","Walking to closest bank");
                DaxWalker.walkToBank();
                Timing.waitCondition( () -> Banking.isInBank() || !Player.getRSPlayer().isMoving(), 20000);
            } else if (Banking.openBank()) {
                Logging.message("BankEvent","Opening bank");
                Sleep.until(Banking07::isBankScreenOpen);
                Banking07.closeBankTutorial();
            }
            updateCache();
        }
    }

    public boolean needCache() {
        return bankCacheHashMap.isEmpty();
    }

    public void updateItem(String itemName, int id, int amount) {
        if(bankCacheHashMap.containsKey(itemName)) {
            bankCacheHashMap.replace(itemName, new BankCache(itemName, id, amount));
        } else {
            bankCacheHashMap.put(itemName, new BankCache(itemName, id, amount));
        }
    }

    public void updateCache() {
        if (!Banking.isBankScreenOpen() && Banking.isBankLoaded()) {
            Logging.message("BankCache","Bank is not open cannot continue");
            return;
        } else {
            RSItem[] bankCache = Banking.getAll();
            for (int i = 0; bankCache.length > i; i++) {
                Logging.message("BankCache","Updating cache: " + bankCache[i].getDefinition().getName() + " ID: " + bankCache[i].getDefinition().getID() + " qty: " + bankCache[i].getStack());
                updateItem(bankCache[i].getDefinition().getName(), bankCache[i].getDefinition().getID(), bankCache[i].getStack());
            }
        }
    }
}