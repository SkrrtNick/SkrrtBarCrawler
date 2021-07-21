package scripts.skrrt_api.events;

import org.tribot.api2007.types.RSItemDefinition;

import java.util.function.Supplier;

public class RequisitionItem {

    public final String name;
    public final int qty;
    public final Supplier<Boolean> cond;
    public final boolean noted;
    public RequisitionItem(String name, int qty, boolean noted, Supplier<Boolean> cond) {
        this.name = name;
        this.qty = qty;
        this.noted = noted;
        this.cond = cond;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return qty;
    }

    public boolean getNoted() {
        return noted;
    }

    public Supplier<Boolean> getCondition() {
        return cond;
    }

}