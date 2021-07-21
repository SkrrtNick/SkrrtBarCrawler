package scripts.skrrt_api.events.grandexchange;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import scripts.skrrt_api.events.BotEvent;
import scripts.skrrt_api.events.Core;
import scripts.skrrt_api.events.banking.BankCache;
import scripts.skrrt_api.events.banking.BankEventV2;
import scripts.skrrt_api.events.inventory.InventoryEvent;
import scripts.skrrt_api.util.functions.*;
import scripts.skrrt_api.util.numbers.Reactions;

import java.io.IOException;
import java.util.Objects;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GEEvent extends BotEvent {

    public HashMap<String, GrandExchangeItem> grandExchangeItemHashMap = new HashMap<>();
    public int itemChild;
    List<String> expandedItems;
    BankEventV2 bankEvent;
    RSArea grandExchangeArea = new RSArea(
            new RSTile[]{
                    new RSTile(3157, 3494, 0),
                    new RSTile(3157, 3487, 0),
                    new RSTile(3162, 3483, 0),
                    new RSTile(3169, 3483, 0),
                    new RSTile(3173, 3487, 0),
                    new RSTile(3173, 3494, 0),
                    new RSTile(3168, 3498, 0),
                    new RSTile(3161, 3498, 0)
            }
    );

    public GEEvent(Script script, BankEventV2 bankEvent) {
        super(script);
        this.bankEvent = bankEvent;

    }

    public GEEvent addReq(String itemName, int id, int qty, int price) {
        setGrandExchangeItemIDList(itemName, id, qty, price);
        return this;
    }

    public boolean isPendingOperation() {
        if (bankEvent.bankCacheHashMap.isEmpty()) {
            Logging.debug("We need to have a cache first");
            return false;
        }
        for (Map.Entry<String, GrandExchangeItem> grandExchangeList : grandExchangeItemHashMap.entrySet()) {
            String itemName = grandExchangeList.getKey();
            if (!itemName.contains("~") && (!bankEvent.bankCacheHashMap.containsKey(itemName) || (bankEvent.bankCacheHashMap.containsKey(itemName) && bankEvent.bankCacheHashMap.get(itemName).getQty() <= 0))) {
                return true;
            }
            if (itemName.contains("~")) {
                expandedItems = expandItemName(itemName);
                for (String item : expandedItems) {
                    if (bankEvent.bankCacheHashMap.containsKey(item) || (bankEvent.bankCacheHashMap.containsKey(item) && bankEvent.bankCacheHashMap.get(item).getQty() <= 0)) {
                        Logging.debug("BankCache contains " + item);
                        break;
                    }
                    if (expandedItems.get(expandedItems.size() - 1).equals(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setGrandExchangeItemIDList(String itemName, int id, int qty, int price) {
        grandExchangeItemHashMap.put(itemName, new GrandExchangeItem(itemName, id, qty, price));
    }

    public void setGrandExchangeItemList(String itemName, int qty, int price) {
        grandExchangeItemHashMap.put(itemName, new GrandExchangeItem(itemName, qty, price));
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!isPendingOperation()) {
            if (!canOpenExchange()) {
                closeGrandExchange();
            } else if (!Banking.isBankScreenOpen()) {
                bankEvent.openBank();
            } else {
                bankEvent.depositAll();
                bankEvent.updateCache();
                setComplete();
            }
        } else if (!grandExchangeArea.contains(me())) {
            Core.setStatus("Moving to the GE");
            Banking07.close();
            Traversing.walkTo(grandExchangeArea);
        } else {
            if (needCoins()) {
                getCoins();
            } else if (Banking.isBankScreenOpen()) {
                Banking.close();
                Timing.waitCondition(() -> !Banking.isBankScreenOpen(), 4000);
            } else if (canOpenExchange()) {
                GrandExchange07.open();
            } else {
                for (Map.Entry<String, GrandExchangeItem> grandExchangeItems : grandExchangeItemHashMap.entrySet()) {
                    int qty;
                    String itemName = grandExchangeItems.getKey();
                    if (itemName.contains("~")) {
                        expandedItems = expandItemName(itemName);
                        for (String item : expandedItems) {
                            if (bankEvent.bankCacheHashMap.get(item) != null) {
                                qty = grandExchangeItems.getValue().getQty() - bankEvent.bankCacheHashMap.get(item).getQty();
                                if (qty <= 0) {
                                    itemName = item;
                                    break;
                                }
                            } else {
                                qty = grandExchangeItems.getValue().getQty();
                            }
                            itemName = expandItemName(itemName).get(0);
                        }
                    }
                    Core.setStatus("Buying " + itemName);
                    String finalItemName = itemName;
                    if (bankEvent.bankCacheHashMap.get(itemName) != null) {
                        qty = grandExchangeItems.getValue().getQty() - bankEvent.bankCacheHashMap.get(itemName).getQty();
                        if (qty <= 0) {
                            continue;
                        }
                    } else {
                        qty = grandExchangeItems.getValue().getQty();
                    }

                    int price = grandExchangeItems.getValue().getPrice();
                    RSGEOffer[] rsgeOffer = GrandExchange.getOffers();
                    if (Arrays.stream(rsgeOffer).anyMatch(i -> i.getStatus() == RSGEOffer.STATUS.COMPLETED) && GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)) {
                        Logging.message("GEEvent", "Collecting items");
                        collectItemsInventory();
                        General.sleep(1000);
                    } else {
                        GrandExchange.WINDOW_STATE state = GrandExchange.getWindowState();
                        if (state != null && state.equals(GrandExchange.WINDOW_STATE.OFFER_WINDOW)) {
                        } else if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)) {
                            if (alreadyHaveOffer(itemName)) {
                                continue;
                            } else if (!getEmptyBox().isPresent()) {
                                if (GrandExchange.collectItems(GrandExchange.COLLECT_METHOD.NOTES)) {
                                    Timing.waitCondition(() -> GrandExchange.getOffers().length < 6, 5000);
                                }
                            } else if (!alreadyHaveOffer(itemName)) {
                                openFirstEmptyBox();
                            }
                        } else if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW)) {
                            while (!alreadyHaveOffer(itemName)) {
                                if (needToChooseItem()) {
                                    if (isItemThere(itemName)) {
                                        clickItemChild();
                                        Timing.waitCondition(() -> !needToChooseItem(), 3000);
                                    } else if (!isItemThere(itemName)) {
                                        RSInterface buyTextBar = Interfaces.get(162, 42);
                                        Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(buyTextBar), 3000);
                                        new TypingEvent(script, itemName, false).setInterruptCondition(() -> !inMakeBuyOfferWindow()).execute();
                                        if (!inMakeBuyOfferWindow()) {
                                            GrandExchange07.goToSelectionWindow();
                                            break;
                                        }
                                        Timing.waitCondition(() -> isItemThere(finalItemName), 4000);
                                    }
                                } else if (isItemCorrect(itemName)) {
                                    if (!isItemPriceCorrect(price)) {
                                        setItemPrice(price);
                                    } else if (!isItemQtyCorrect(qty)) {
                                        setItemQty(qty);
                                    } else {
                                        clickConfirm();
                                        Timing.waitCondition(() -> !alreadyHaveOffer(finalItemName), 3000);
                                    }
                                } General.sleep(20,40);
                            }
                        }
                    }
                }
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

    public void collectItemsInventory() {
        RSInterface collectInterface = Interfaces.get(465, 6, 0);
        RSItem[] beforeItemList = getCurrentInventoryList();
        if (Interfaces.isInterfaceSubstantiated(collectInterface)) {
            if (collectInterface.click("Collect to inventory")) {
                General.sleep(1000);
                updateCache();
            }
        }
    }

    public RSItem[] getCurrentInventoryList() {
        return Inventory.getAll();
    }

    public void updateItem(String itemName, int id, int amount) {
        int oldCache;
        if (bankEvent.bankCacheHashMap.containsKey(itemName)) {
            oldCache = bankEvent.bankCacheHashMap.get(itemName).getQty();
            bankEvent.bankCacheHashMap.replace(itemName, new BankCache(itemName, id, amount + oldCache));
        } else {
            bankEvent.bankCacheHashMap.put(itemName, new BankCache(itemName, id, amount));
        }
    }

    public void updateCache() {
        RSItem[] inventoryItems = Inventory.getAll();
        Arrays.stream(inventoryItems)
                .map(RSItem::getDefinition)
                .filter(Objects::nonNull)
                .forEach(rsItemDefinition -> {
                    final String name = rsItemDefinition.getName();
                    final int id = rsItemDefinition.getUnnotedItemID();

                    final int qty;

                    // find the qty by matching the current RSItem to the RSItem[] inventoryItems by ID
                    qty = Arrays.stream(inventoryItems)
                            .filter(rsItem -> rsItem.getID() == rsItemDefinition.getID()) // find the current RSItem in stream using ID
                            .mapToInt(RSItem::getStack) // map the stack
                            .findFirst() // find it
                            .orElse(0); // return the stack or zero if not found

                    Logging.message("BankCache", "Updating cache: " + name + " ID: " + id + " qty: " + qty);

                    updateItem(name, id, qty);
                });
    }

    public void clickConfirm() {
        RSInterface confirmButton = Interfaces.get(465, 27, 0);
        if (Interfaces.isInterfaceSubstantiated(confirmButton)) {
            confirmButton.click();
            Sleep.until(() -> GrandExchange07.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW));
        }
    }

    public boolean needToChooseItem() {
        RSInterface rsInterface = Interfaces.get(465, 24, 25);
        if (Interfaces.isInterfaceSubstantiated(rsInterface)) {
            return rsInterface.getText().equals("Choose an item...");
        }
        return false;
    }

    public boolean isItemCorrect(String itemName) {
        RSInterface itemNameInterface = Interfaces.get(465, 24, 25);
        if (Interfaces.isInterfaceSubstantiated(itemNameInterface)) {
            return itemNameInterface.getText().equals(itemName);
        }
        return false;
    }

    public boolean isItemPriceCorrect(int price) {
        RSInterface itemPriceText = Interfaces.get(465, 24, 39);
        if (Interfaces.isInterfaceSubstantiated(itemPriceText)) {
            String itemPrice = itemPriceText.getText().replace(" coins", "").replace(",", "");
            return Integer.parseInt(itemPrice) == price;
        }
        return false;
    }

    public void setItemPrice(int price) throws IOException, InterruptedException {
        RSInterface enterPriceButton = Interfaces.get(465, 24, 12);
        RSInterface enterPrice = Interfaces.get(162, 41);
        if (Interfaces.isInterfaceSubstantiated(enterPrice)) {
            String itemPrice = Integer.toString(price);
            new TypingEvent(script, itemPrice, true).setInterruptCondition(() -> !inMakeBuyOfferWindow()).execute();
            if (!inMakeBuyOfferWindow()) {
                GrandExchange07.goToSelectionWindow();
            }
            Timing.waitCondition(() -> isItemPriceCorrect(price), 4000);
        } else if (Interfaces.isInterfaceSubstantiated(enterPriceButton)) {
            if (enterPriceButton.click()) {
                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(enterPrice), 2000);
            }
        }
    }

    public boolean isItemQtyCorrect(int qty) {
        RSInterface itemQtyText = Interfaces.get(465, 24, 32);
        if (Interfaces.isInterfaceSubstantiated(itemQtyText)) {
            return Integer.parseInt(itemQtyText.getText().replace(",", "")) == qty;
        }
        return false;
    }

    public void setItemQty(int qty) throws IOException, InterruptedException {
        RSInterface itemQtyButton = Interfaces.get(465, 24, 7);
        RSInterface enterQty = Interfaces.get(162, 41);
        if (Interfaces.isInterfaceSubstantiated(enterQty)) {
            String itemQty = Integer.toString(qty).replace(",", "");
            new TypingEvent(script, itemQty, true).setInterruptCondition(() -> !inMakeBuyOfferWindow()).execute();
            if (!inMakeBuyOfferWindow()) {
                GrandExchange07.goToSelectionWindow();
            }
        } else if (Interfaces.isInterfaceSubstantiated(itemQtyButton)) {
            if (itemQtyButton.click()) {
                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(enterQty), 3000);
            }
        }
    }

    public boolean clickItemChild() {
        RSInterface itemList = ExchangeBoxes.itemList;
        if (Interfaces.isInterfaceSubstantiated(itemList)) {
            RSInterface itemChild = itemList.getChild(getItemChild());
            if (Interfaces.isInterfaceSubstantiated(itemChild)) {
                return itemChild.click();
            } else {
                General.println("We need to scroll i guess");
                General.sleep(1000);
            }
        }
        return false;
    }

    public boolean isItemThere(String itemName) {
        RSInterface itemList = ExchangeBoxes.itemList;
        if (Interfaces.isInterfaceSubstantiated(itemList)) {
            for (int i = 0; i < 26; i++) {
                if (Interfaces.isInterfaceSubstantiated(itemList.getChild(i))) {
                    String itemBoxName = itemList.getChild(i).getComponentName().replace("<col=ff9040>", "").replace("</col>", "");
                    if (itemBoxName.equals(itemName)) {
                        setItemChild(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean inMakeBuyOfferWindow() {
        Sleep.until(() -> Interfaces.isInterfaceSubstantiated(465, 24, 18), Reactions.getNormal());
        RSInterface buyOffer = Interfaces.get(465, 24, 18);
        if (Interfaces.isInterfaceSubstantiated(buyOffer) && buyOffer.getText().contains("Buy")) {
            return true;
        } else {
            Logging.debug("Buy Offer Interface is Substantiated", Interfaces.isInterfaceSubstantiated(buyOffer));
            Logging.debug("Buy Offer Text is: " + buyOffer.getText());
            return false;
        }
    }

    public int getItemChild() {
        return itemChild;
    }

    public void setItemChild(int i) {
        itemChild = i;
    }

    public void openFirstEmptyBox() {
        RSInterface emptyBox = getFirstEmptyBox().get();
        if (emptyBox.getChild(ExchangeBoxes.buyChild).click()) {
            Timing.waitCondition(() -> GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW), 5000);
        }
    }

    public boolean alreadyHaveOffer(String itemName) {
        return Arrays.stream(ExchangeBoxes.allBoxes).anyMatch(i -> i.getChild(19).getText().equals(itemName));
    }

    public boolean isBoxEmpty(RSInterface box) {
        return Interfaces.isInterfaceSubstantiated(box.getChild(ExchangeBoxes.buyChild));
    }

    public Optional<RSInterface> getEmptyBox() {
        RSInterface[] allBoxes = ExchangeBoxes.allBoxes;
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).filter(i -> Interfaces.isInterfaceSubstantiated(i.getChild(buyChild))).findAny();
    }

    public Optional<RSInterface> getFirstEmptyBox() {
        RSInterface[] allBoxes = ExchangeBoxes.allBoxes;
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).filter(i -> Interfaces.isInterfaceSubstantiated(i.getChild(buyChild))).findFirst();
    }

    public boolean noEmptyBoxes() {
        RSInterface[] allBoxes = ExchangeBoxes.allBoxes;
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).noneMatch(i -> Interfaces.isInterfaceSubstantiated(i.getChild(buyChild)));
    }


    public boolean needCoins() {
        return !InventoryEvent.contains("Coins");
    }

    public void getCoins() throws IOException, InterruptedException {
        new BankEventV2(script).addReq("Coins", Integer.MAX_VALUE).execute();
    }

    public boolean canOpenExchange() {
        return GrandExchange.getWindowState() == null;
    }

    public void closeGrandExchange() {
        if (!canOpenExchange()) {
            GrandExchange.close();
            Timing.waitCondition(() -> canOpenExchange(), 4000);
        }
    }


    public RSPlayer me() {
        return Player.getRSPlayer();
    }

    public GEEvent addReq(String itemName, int qty, int price) {
        grandExchangeItemHashMap.put(itemName, new GrandExchangeItem(itemName, qty, price));
        return this;
    }


}