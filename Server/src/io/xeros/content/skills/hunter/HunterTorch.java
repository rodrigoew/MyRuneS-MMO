package io.xeros.content.skills.hunter;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
/** Ordinary torch preparation shared by the Hunter trap activities. */
public final class HunterTorch {
 private HunterTorch(){}
 private static boolean ready(Player p){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked();}
 public static boolean itemOnItem(Player p,int first,int second){
  if(!(first==590&&second==596||first==596&&second==590))return false;
  if(!ready(p)||p.playerLevel[11]<1||!p.getItems().playerHasItem(590))return true;
  if(ProductionBatch.exchange(p,new int[][]{{596,1}},new int[][]{{594,1}})){p.interruptActions();p.getPA().stopSkilling();p.startAnimation(733);p.sendMessage("You light the torch.");}return true;
 }
 public static boolean extinguish(Player p,int id,int slot,int container){
  if(id!=594)return false;
  if(container!=3214||!ready(p)||!p.getItems().isItemInInventorySlot(594,slot))return true;
  if(ProductionBatch.exchange(p,new int[][]{{594,1}},new int[][]{{596,1}})){p.interruptActions();p.getPA().stopSkilling();p.sendMessage("You extinguish the torch.");}return true;
 }
 public static boolean canSmoke(Player p){if(p.playerLevel[21]<39){p.sendMessage("You need Hunter level 39 to smoke a trap.");return false;}return p.getItems().playerHasItem(594);}
}
