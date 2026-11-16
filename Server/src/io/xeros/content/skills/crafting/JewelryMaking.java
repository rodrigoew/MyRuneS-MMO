package io.xeros.content.skills.crafting;

import io.xeros.model.Items;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;

public class JewelryMaking extends CraftingData {

    public static void mouldInterface(Player p){JewelleryRecipes.menu(p);}
    public static void mouldItem(Player p,int item,int amount){JewelleryRecipes.make(p,item,amount);}

	public static String getRequiredMessage(String item) {
		if (item.startsWith("A") || item.startsWith("E") || item.startsWith("I") || item.startsWith("O") || item.startsWith("U")) {
			return "You need a Gold bar and an " + item + " to make this.";
		} else {
			return "You need a Gold bar and a " + item + " to make this.";
		}
	}

	public static void stringAmulet(final Player c, final int itemUsed, final int usedWith) {
		if(itemUsed!=1759 && usedWith!=1759)return;
        final int amuletId = (itemUsed == 1759 ? usedWith : itemUsed);
        if(!c.getItems().playerHasItem(1759)||!c.getItems().playerHasItem(amuletId))return;
		for(int[] pair:new int[][]{{21099,21108},{21102,21111},{21105,21114},{1714,1716},{1720,1722}})if(amuletId==pair[0]){
            if(io.xeros.content.skills.ProductionBatch.exchange(c,new int[][]{{1759,1},{amuletId,1}},new int[][]{{pair[1],1}}))c.getPA().addSkillXPMultiplied(4,12,true);
            return;
        }
        for (final amuletData a : amuletData.values()) {
			if (amuletId == a.getAmuletId()) {
				c.getItems().deleteItem(1759, 1);
				c.getItems().deleteItem(amuletId, 1);
				c.getItems().addItem(a.getProduct(), 1);
				c.getPA().addSkillXPMultiplied(4, 12, true);
			}
		}
	}
}
