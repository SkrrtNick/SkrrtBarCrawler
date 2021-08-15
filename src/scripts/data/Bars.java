package scripts.data;

import org.tribot.api2007.Game;
import org.tribot.api2007.types.RSArea;
import scripts.api.npc.NpcID;

import java.math.BigInteger;

public enum Bars {
    START("Barbarian Guard",50,Constants.BARBARIAN_GUARD_AREA,Constants.NOT_STARTED_BIT, NpcID.BARBARIAN_GUARD_5227,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_BLUE_MOON("Blue Moon",50,Constants.BARTENDER_BLUE_MOON_AREA,Constants.BLUE_MOON_INDEX,NpcID.BARTENDER_1312,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_JOLLY_BOAR("Jolly Boar",10,Constants.BARTENDER_JOLLY_BOAR_AREA, Constants.JOLLY_BOAR_INDEX,NpcID.BARTENDER_1310,Constants.BARCRAWL_DIALOGUE2),
    KAYLEE("Rising Sun",70,Constants.KAYLEE_AREA,Constants.RISING_SUN_INDEX,NpcID.KAYLEE,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_RUSTY_ANCHOR("Rusty Anchor",8,Constants.BARTENDER_RUSTY_ANCHOR_AREA, Constants.RUSTY_ANCHOR_INDEX,NpcID.BARTENDER_1313,Constants.BARCRAWL_DIALOGUE),
    ZAMBO("Karamja Spirits",7,Constants.ZAMBO_AREA,Constants.ZAMBO_INDEX,NpcID.ZAMBO,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_DEAD_MANS_CHEST("Dead Mans Chest",15,Constants.BARTENDER_DEAD_MANS_CHEST_AREA,Constants.DEAD_MANS_CHEST_INDEX,NpcID.BARTENDER_1314,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_FLYING_HORSE_INN("Flying Horse Inn",15,Constants.BARTENDER_FLYING_HORSE_INN_AREA,Constants.FLYING_HORSE_INDEX,NpcID.BARTENDER_1319,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_FORESTERS_ARMS("Forester Arms",18,Constants.BARTENDER_FORESTERS_ARMS_AREA,Constants.FORESTER_ARMS_INDEX,NpcID.BARTENDER_1318,Constants.BARCRAWL_DIALOGUE),
    BLURBERRY("The Grand Tree",10,Constants.BLURBERRY_AREA,Constants.BLURBERRY_INDEX,NpcID.BLURBERRY,Constants.BARCRAWL_DIALOGUE),
    BARTENDER_DRAGON_INN("Dragon Inn",12,Constants.BARTENDER_DRAGON_INN_AREA,Constants.DRAGON_INN_INDEX,NpcID.BARTENDER_1320,Constants.BARCRAWL_DIALOGUE);

    private String name;
    private int coins;
    private RSArea area;
    private int bit;
    private int npc;
    private String dialogue;

    Bars(String name, int coins, RSArea area, int bit, int npc, String dialogue) {
        this.name = name;
        this.npc = npc;
        this.coins = coins;
        this.area = area;
        this.bit = bit;
        this.dialogue = dialogue;
    }


    public String getName() {
        return name;
    }

    public int getCoins() {
        return coins;
    }

    public RSArea getArea() {
        return area;
    }

    public int getBit() {
        return bit;
    }

    public int getNpc() {
        return npc;
    }

    public String getDialogue() {
        return dialogue;
    }

    public boolean isCompleted() {
        return BigInteger.valueOf(Game.getSetting(Constants.BARCRAWL_SETTING)).testBit(bit);
    }
}
