package scripts.skrrt_api.events.banking;

import org.tribot.api2007.types.RSItemDefinition;

public class BankItem {
    private int id;
    private int[] ids;
    private int count;
    private boolean tradeable;
    private boolean requirementsMet;
    private int gameSetting;
    private int neededUntilSetting;
    private boolean requiredFor;

    public BankItem(int id, int count, boolean tradeable) {
        this.id = id;
        this.count = count;
        this.tradeable = tradeable;
    }

    public BankItem(int id, int count, boolean tradeable, boolean requirementsMet) {
        this.id = id;
        this.count = count;
        this.tradeable = tradeable;
        this.requirementsMet = requirementsMet;
    }
    public BankItem(int id, int count, boolean tradeable, boolean requirementsMet, boolean requiredFor) {
        this.id = id;
        this.count = count;
        this.tradeable = tradeable;
        this.requirementsMet = requirementsMet;
        this.requiredFor = requiredFor;
    }

    public BankItem(int id, int count, boolean tradeable, int gameSetting, int neededUntil) {
        this.id = id;
        this.count = count;
        this.tradeable = tradeable;
        this.gameSetting = gameSetting;
        this.neededUntilSetting = neededUntil;
    }

    public BankItem(int count, boolean tradeable, int... id) {
        this.ids = id;
        this.count = count;
        this.tradeable = tradeable;
        this.requiredFor = true;
    }
    public BankItem(int count, boolean tradeable,boolean requirementsMet, boolean requiredFor, int... id) {
        this.ids = id;
        this.count = count;
        this.tradeable = tradeable;
        this.requirementsMet = requirementsMet;
        this.requiredFor = requiredFor;
    }

    public int getGameSetting() {
        return gameSetting;
    }

    public int getNeededUntilSetting() {
        return neededUntilSetting;
    }

    public int getId() {
        return id;
    }

    public boolean requirementsMet() {
        return requirementsMet;
    }

    public boolean isRequiredFor() {
        return requiredFor;
    }

    public int getCount() {
        return count;
    }

    public int[] getIds() {
        return ids;
    }

    public boolean isTradeable() {
        return tradeable;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIds(int[] ids) {
        this.ids = ids;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTradeable(boolean tradeable) {
        this.tradeable = tradeable;
    }

    public void setGameSetting(int gameSetting) {
        this.gameSetting = gameSetting;
    }

    public void setNeededUntil(int neededUntil) {
        this.neededUntilSetting = neededUntil;
    }
}
