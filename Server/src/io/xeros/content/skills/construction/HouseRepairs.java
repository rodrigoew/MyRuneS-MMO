package io.xeros.content.skills.construction;
import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Workshop salvage repairs; published success and conditional reward tables. */
public final class HouseRepairs {
 public enum Salvage {
  ARROW(687,9,8,180,0,new int[][]{{884,57},{882,24},{886,10},{53,5},{888,4}}),
  STAFF(689,12,14.5,150,0,new int[][]{{1379,57},{1389,26},{1383,5},{1381,4},{1385,4},{1387,4}}),
  SWORD(686,13,25,100,1,new int[][]{{1277,17},{1291,7},{1279,20},{1293,37},{1281,6},{1295,4},{1299,4},{1283,3},{1297,2}}),
  DAMAGED_ARMOUR(697,13,25,75,2,new int[][]{{1115,57},{1117,24},{1119,10},{1125,5},{1121,4}}),
  BROKEN_ARMOUR(698,13,25,70,2,new int[][]{{1081,57},{1067,57},{1087,24},{1075,24},{1083,10},{1069,10},{1089,5},{1077,5},{1085,4},{1071,4}});
  public final int item,skill,low,tier,total;public final double xp;public final int[][] rewards;
  Salvage(int item,int skill,double xp,int low,int tier,int[][] rewards){this.item=item;this.skill=skill;this.xp=xp;this.low=low;this.tier=tier;this.rewards=rewards;int sum=0;for(int[] row:rewards)sum+=row[1];total=sum;}
  public static Salvage get(int id){for(Salvage r:values())if(r.item==id)return r;return null;}
 }
 private static IntUnaryOperator random=bound->ThreadLocalRandom.current().nextInt(bound);
 private final PlayerHouse house;private final Player p;
 public HouseRepairs(PlayerHouse house,Player p){this.house=house;this.p=p;}
 public static int threshold(Salvage r,int level){level=Math.max(1,level);return Math.max(0,Math.min(256,1+(int)Math.floor((r.low*(99d-level)+240d*(level-1))/98d+.5)));}
 public static int reward(Salvage r,int roll){if(roll<0||roll>=r.total)throw new IllegalArgumentException("Repair reward roll");for(int[] row:r.rewards){if(roll<row[1])return row[0];roll-=row[1];}throw new IllegalStateException("Repair rewards");}
 private static int tier(HouseFurnishing f){switch(f.key){case "REPAIR_BENCH":return 0;case "WHETSTONE":return 1;case "ARMOUR_STAND":return 2;default:return -1;}}
 public boolean use(int item,WorldObject object){
  if(!house.inside())return false;PlayerHouse.Binding b=house.furnitureFixture(object);if(b==null||b.room.kind!=PlayerHouse.Kind.WORKSHOP)return false;
  HouseFurnishing f=house.furnishing(b);if(f.group!=15448)return false;
  HouseBarrowsData.Piece barrows=HouseBarrowsData.get(item);
  if(barrows!=null){
   if(tier(f)!=2)p.sendMessage("You need an armour stand to repair Barrows equipment.");
   else quoteBarrows(b,item,barrows);
   return true;
  }
  Salvage r=Salvage.get(item);
  if(r==null){p.sendMessage("That item cannot be repaired here.");return true;}
  if(tier(f)<r.tier){p.sendMessage("You need a better repair facility for this item.");return true;}
  if(!p.getItems().playerHasItem(item)||p.getHealth().getCurrentHealth()<=0||p.teleTimer>0)return true;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;String key=f.key;p.playerSkilling[r.skill]=true;p.startAnimation(898);
  // One queued action prevents repeated packets from consuming the same scrap.
  // The exact historical repair animation duration is not independently certified.
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[r.skill]||!house.furnitureReady(b)||!key.equals(house.furnishing(b).key)||p.isDisconnected()||p.getSession()==null||p.isDead||p.getHealth().getCurrentHealth()<=0||p.teleTimer>0||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||!p.getItems().playerHasItem(item)){stop();return;}
    boolean success=random.applyAsInt(256)<threshold(r,p.playerLevel[r.skill]);int output=success?reward(r,random.applyAsInt(r.total)):-1;
    if(ProductionBatch.exchange(p,new int[][]{{item,1}},success?new int[][]{{output,1}}:new int[0][])){
     if(success){p.getPA().addSkillXPMultiplied(r.xp,r.skill,true);p.sendMessage("You successfully repair the item.");}
     else p.sendMessage("The item breaks beyond repair.");
    }stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[r.skill]=false;p.startAnimation(65535);}}
  });return true;
 }
 private void quoteBarrows(PlayerHouse.Binding b,int item,HouseBarrowsData.Piece piece){
  if(!house.furnitureReady(b)||!p.getItems().playerHasItem(item)||p.isDisconnected()||p.getHealth().getCurrentHealth()<=0||p.teleTimer>0)return;
  int charges=HouseBarrowsData.defaultCharges(piece,item);
  if(charges==1000){p.sendMessage("This equipment does not need repairing.");return;}
  int quoted=HouseBarrowsData.price(piece,charges,p.playerLevel[13]);String key=house.furnishing(b).key;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
  new DialogueBuilder(p).option("Repair "+piece.name+"?",new DialogueOption("Repair for "+String.format(java.util.Locale.ROOT,"%,d",quoted)+" coins",q->{
   q.getPA().removeAllWindows();
   if(!house.furnitureReady(b)||!key.equals(house.furnishing(b).key)||p.isDisconnected()||p.getHealth().getCurrentHealth()<=0||p.teleTimer>0||!p.getItems().playerHasItem(item))return;
   if(quoted!=HouseBarrowsData.price(piece,charges,p.playerLevel[13])){p.sendMessage("Your Smithing level changed. Check the updated price.");quoteBarrows(b,item,piece);return;}
   if(p.getItems().getInventoryCount(995)<quoted){p.sendMessage("You do not have enough coins for this repair.");return;}
   int[][] inputs=quoted==0?new int[][]{{item,1}}:new int[][]{{item,1},{995,quoted}};
   if(ProductionBatch.exchange(p,inputs,new int[][]{{piece.states[0],1}})){
    p.startAnimation(898);p.getPA().addSkillXPMultiplied(25,13,true);p.sendMessage("You repair the equipment.");
   }
  }),new DialogueOption("Cancel",q->q.getPA().removeAllWindows())).send();
 }
}
