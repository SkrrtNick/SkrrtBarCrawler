package scripts.api.util.functions;

import org.tribot.api2007.Game;
import org.tribot.api2007.types.RSItem;
import scripts.api.events.banking.BankItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inventory07 extends org.tribot.api2007.Inventory {

    public static int getFreeSlots() {
        return 28 - Inventory07.getAll().length;
    }

    public static boolean isEmpty() {
        return Inventory07.getAll().length == 0;
    }

    public static boolean hasRequired(ArrayList<BankItem> bankItems){
        for(BankItem bankItem : bankItems){
            if(bankItem.requirementsMet()){
                continue;
            }
            if((bankItem.getGameSetting()!=0 && bankItem.getNeededUntilSetting() > Game.getSetting(bankItem.getGameSetting())) && bankItem.isTradeable()){
                if(bankItem.getId()>0){
                    if(Inventory07.getCount(bankItem.getId())==0){
                        Logging.debug("We don't have ("+bankItem.getId()+")");
                        return false;
                    }
                } else {
                    if(Inventory07.getCount(bankItem.getIds()) == 0){
                        Logging.debug("We don't have ("+ Arrays.toString(bankItem.getIds()) +")");
                        return false;
                    }
                }

            }
        } return true;
    }

    public static int getCount(List<Integer> ids){

            return Inventory07.getCount(Arrays.stream(ids.toArray(Integer[]::new)).mapToInt(Integer::intValue).toArray());

    }

    public static RSItem[] find(ArrayList<Integer> ids){
        for(Integer id:ids){
            if(Inventory07.find(id).length > 0){
                return Inventory07.find(id);
            }
        } return new RSItem[0];
    }
}
