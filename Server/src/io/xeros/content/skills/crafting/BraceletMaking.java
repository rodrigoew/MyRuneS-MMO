package io.xeros.content.skills.crafting;

import io.xeros.model.Items;
import io.xeros.model.entity.player.Player;

public class BraceletMaking extends CraftingData {
	
	
 public static void craftBraceletDialogue(Player p){JewelleryRecipes.page(p,"Bracelets",0);}

 public static void craftBracelet(Player p,int button){
  for(braceletData recipe:braceletData.values())if(button>=0&&recipe.getButtonId(button)==button){
   CraftingBatch.start(p,recipe.getAmount(button),recipe.getLevel(),recipe.getXP(),recipe.getProduct(),899,3,
    new int[][]{{Items.BRACELET_MOULD,0},{Items.GOLD_BAR,1},{recipe.getGem(),1}},()->{p.braceletDialogue=false;});return;
  }
 }
}
