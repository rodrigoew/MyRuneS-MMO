package io.xeros.content.skills.runecrafting;
import io.xeros.model.entity.player.Player;
public class Pouches {
 public static void migrateLegacy(Player p){
  boolean colossal=p.getItems().playerHasItem(26784)||p.getItems().bankContains(26784);
  boolean large=p.getItems().playerHasItem(5512)||p.getItems().bankContains(5512);
  if(colossal&&!large){p.runeEssencePouch[4]=p.runeEssencePouch[2];p.pureEssencePouch[4]=p.pureEssencePouch[2];p.runeEssencePouch[2]=0;p.pureEssencePouch[2]=0;}
 }
 public enum Pouch {
  SMALL_POUCH(5509,0,3,1), MEDIUM_POUCH(5510,1,6,25), LARGE_POUCH(5512,2,9,50),
  GIANT_POUCH(5514,3,12,75), COLOSSAL_POUCH(26784,4,40,85);
  public final int pouchId,index,capacity,level;
  Pouch(int id,int index,int capacity,int level){this.pouchId=id;this.index=index;this.capacity=capacity;this.level=level;}
  public static Pouch forId(int id){for(Pouch p:values())if(p.pouchId==id)return p;return null;}
  public static Pouch forIndex(int index){for(Pouch p:values())if(p.index==index)return p;return null;}
 }
 private static boolean allowed(Player p,Pouch pouch){
  return pouch!=null&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null&&!p.getLock().cannotInteract(p)&&p.teleTimer<=0&&!p.getInterfaceEvent().isActive()&&p.getItems().playerHasItem(pouch.pouchId);
 }
 public static void fill(Player p,Pouch pouch,int ignoredLegacyIndex){
  if(!allowed(p,pouch))return;
  if(p.getLevelForXP(p.playerXP[20])<pouch.level){p.sendMessage("Your Runecrafting level is too low for this pouch.");return;}
  int index=pouch.index,room=pouch.capacity-p.getRuneEssencePouch(index)-p.getPureEssencePouch(index),filled=0;
  // Old saves may contain the former custom capacity; emptying preserves those items.
  for(int id:new int[]{1436,7936}){
   int amount=Math.min(Math.max(0,room),p.getItems().getInventoryCount(id));
   if(amount==0)continue;p.getItems().deleteItem2(id,amount);
   if(id==1436)p.setRuneEssencePouch(index,p.getRuneEssencePouch(index)+amount);
   else p.setPureEssencePouch(index,p.getPureEssencePouch(index)+amount);
   room-=amount;filled+=amount;
  }
  p.sendMessage(filled>0?"You fill your pouch with "+filled+" essence.":"Your pouch is full or you have no essence.");
 }
 public static void empty(Player p,int index){
  if(!allowed(p,Pouch.forIndex(index)))return;
  while(p.getRuneEssencePouch(index)>0&&p.getItems().freeSlots()>0){p.getItems().addItem(1436,1);p.setRuneEssencePouch(index,p.getRuneEssencePouch(index)-1);}
  while(p.getPureEssencePouch(index)>0&&p.getItems().freeSlots()>0){p.getItems().addItem(7936,1);p.setPureEssencePouch(index,p.getPureEssencePouch(index)-1);}
 }
 public static void check(Player p,int index){
  if(allowed(p,Pouch.forIndex(index)))p.sendMessage("Your pouch contains "+p.getRuneEssencePouch(index)+" rune essence and "+p.getPureEssencePouch(index)+" pure essence.");
 }
}
