package io.xeros.content.skills.crafting;

import io.xeros.content.achievement_diary.impl.MorytaniaDiaryEntry;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;

public class LeatherMaking extends CraftingData {

	public static void craftLeatherDialogue(final Player c, final int itemUsed, final int usedWith) {
		for (final leatherData l : leatherData.values()) {
			final int leather = (itemUsed == 1733 ? usedWith : itemUsed);
			if (leather == l.getLeather()) {
				if(l.getLeather()==1743){
                    c.getPA().sendChatboxInterface(8880);c.getPA().itemOnInterface(8884,180,1131);c.leatherType=leather;c.craftDialogue=true;return;
                }
                if (l.getLeather() == 1741) {
					c.getPA().showInterface(2311);
					c.leatherType = leather;
					c.craftDialogue = true;
					return;
				}
				String[] name = { "Body", "Chaps", "Bandana", "Boots", "Vamb", };
				if (l.getLeather() == 6289) {
					c.getPA().sendChatboxInterface(8938);
					c.getPA().itemOnInterface(8941, 180, 6322);
					c.getPA().itemOnInterface(8942, 180, 6324);
					c.getPA().itemOnInterface(8943, 180, 6326);
					c.getPA().itemOnInterface(8944, 180, 6328);
					c.getPA().itemOnInterface(8945, 180, 6330);
					for (int i = 0; i < name.length; i++) {
						c.getPA().sendFrame126(name[i], 8949 + (i * 4));
					}
					c.leatherType = leather;
					c.craftDialogue = true;
					return;
				}
			}
		}
		for (final leatherDialogueData d : leatherDialogueData.values()) {
			final int leather = (itemUsed == 1733 ? usedWith : itemUsed);
			String[] name = { "Vamb", "Chaps", "Body", };
			if (leather == d.getLeather()) {
				c.getPA().sendChatboxInterface(8880);
				c.getPA().itemOnInterface(8883, 180, d.getVamb());
				c.getPA().itemOnInterface(8884, 180, d.getChaps());
				c.getPA().itemOnInterface(8885, 180, d.getBody());
				for (int i = 0; i < name.length; i++) {
					c.getPA().sendFrame126(name[i], 8889 + (i * 4));
				}
				c.leatherType = leather;
				c.craftDialogue = true;
				return;
			}
		}
	}

 public static void craftLeather(Player p,int button) {
  for(leatherData recipe:leatherData.values())if(button>=0 && recipe.getButtonId(button)==button && p.leatherType==recipe.getLeather()) {
   CraftingBatch.start(p,recipe.getAmount(button),recipe.getLevel(),recipe.getXP(),recipe.getProduct(),1249,5,
    new int[][]{{1733,0},{1734,1},{recipe.getLeather(),recipe.getHideAmount()}},()->{
     if(recipe.getProduct()==2503 && Boundary.isIn(p,Boundary.CANIFIS_BOUNDARY))p.getDiaryManager().getMorytaniaDiary().progress(MorytaniaDiaryEntry.DHIDE_BODY);
    });return;
  }
 }
}
