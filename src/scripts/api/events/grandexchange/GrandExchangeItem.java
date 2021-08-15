package scripts.api.events.grandexchange;

import org.tribot.api2007.types.RSItemDefinition;

public class GrandExchangeItem {
    public String itemName;
    public final int qty;
    public int id;
    public final int price;

    public GrandExchangeItem(String itemName, int id, int qty, int price) {
        this.itemName = itemName;
        this.id = id;
        this.qty = qty;
        this.price = price;
    }

    public GrandExchangeItem(int id, int qty, int price) {
        String itemName = RSItemDefinition.get(id).getName();
        if (itemName != null) {
            this.itemName = itemName;
        }
        this.id = id;
        this.qty = qty;
        this.price = price;
    }

    public GrandExchangeItem(String itemName, int qty, int price) {
        this.itemName = itemName;
        this.qty = qty;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQty() {
        return qty;
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }
}
