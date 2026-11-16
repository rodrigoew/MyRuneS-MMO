package io.xeros.content.skills.crafting;

import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;

public class BattlestaveMaking {
	
	public static void craftBattlestaveDialogue(final Player c, final int itemUsed, final int usedWith) {
		for (final CraftingData.battlestaveDialogueData d : CraftingData.battlestaveDialogueData.values()) {
			final int leather = (itemUsed == 1391 ? usedWith : itemUsed);
			String[] name = { "", "Battlestaff", "" };
			if (leather == d.getOrb()) {
				c.getPA().sendChatboxInterface(8880);
				c.getPA().itemOnInterface(8884, 180, d.getBattlestaff());
				for (int i = 0; i < name.length; i++) {
					c.getPA().sendFrame126(name[i], 8889 + (i * 4));
				}
				c.leatherType = leather;
				c.battlestaffDialogue = true;
				return;
			}
		}
	}

 public static void craftBattlestave(Player p,int button) {
  for(CraftingData.battlestaveData recipe:CraftingData.battlestaveData.values())
   if(button>=0 && recipe.getButtonId(button)==button && p.leatherType==recipe.getOrb()) {
    CraftingBatch.start(p,recipe.getAmount(button),recipe.getLevel(),recipe.getXP(),recipe.getProduct(),1249,2,
     new int[][]{{recipe.getOrb(),1},{1391,1}},()->{});return;
   }
 }
}
