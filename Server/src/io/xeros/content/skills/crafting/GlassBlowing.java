package io.xeros.content.skills.crafting;
import io.xeros.model.cycleevent.*;
import io.xeros.model.entity.player.Player;
/** A separate batch for each player, released on completion or interruption. */
public class GlassBlowing extends GlassData {
 public static void glassBlowing(Player p,int button){
  for(glassData recipe:glassData.values())if(button>=0&&recipe.getButtonId(button)==button){
   CraftingBatch.start(p,recipe.getAmount(button),recipe.getLevelReq(),recipe.getXP(),recipe.getNewId(),884,3,
    new int[][]{{1785,0},{1775,1}},()->{});return;
  }
 }
 public static void makeGlass(Player p,int used,int with){
  if(((used==1785&&with==1775)||(used==1775&&with==1785))&&p.getItems().playerHasItem(1785)&&p.getItems().playerHasItem(1775))p.getPA().showInterface(11462);
 }
}
