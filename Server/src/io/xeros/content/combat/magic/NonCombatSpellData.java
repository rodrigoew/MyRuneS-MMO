package io.xeros.content.combat.magic;

import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.model.Staff;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;

public class NonCombatSpellData extends MagicRequirements {

	public static void attemptDate(Player player,int action){player.getBoneConversion().cast(action);}

    public static boolean consumeRunesOnCast(Player player,int[] runes,int[] amounts){
        int[][] costs=MagicRequirements.runeCosts(runes,amounts);
        if(costs==null||!RunePayment.consume(player,costs)){player.sendMessage("You don't have all the runes required to cast this spell.");return false;}
        return true;
    }

    public static void superHeatItem(Player p,int item){
        p.getUtilityMagic().superheat(item,p.getItems().getInventoryItemSlot(item));
    }
}
