package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.hunter.birdhouse.*;import io.xeros.content.items.UseItem;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import io.xeros.model.collisionmap.*;import java.util.*;import java.lang.reflect.*;import java.util.function.*;import java.nio.file.*;
public class BirdhousesCheck {
 static void ok(boolean value,String why){if(!value)throw new AssertionError(why);}
 static Player actor(){Player p=AttackDefencePlayableCheck.actor();p.setLoginName("birdqa");p.setDisplayName("Birdqa");return p;}
 static void item(Player p,int id,int quantity){SkillsAuditCheck.item(p,id,quantity);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static Birdhouses clocked(Player p,long[] time)throws Exception{Constructor<Birdhouses> ctor=Birdhouses.class.getDeclaredConstructor(Player.class,LongSupplier.class);ctor.setAccessible(true);return ctor.newInstance(p,(LongSupplier)()->time[0]);}
 static void position(Player p,BirdhouseSpot spot){p.absX=spot.x-1;p.absY=spot.y;p.heightLevel=0;}
 static void xp(Player p,int old,double amount){Player ref=actor();int before=ref.playerXP[21];ref.getPA().addSkillXPMultiplied(amount,21,false);ok(p.playerXP[21]-old==ref.playerXP[21]-before&&p.playerXpTenths[21]==ref.playerXpTenths[21],"Hunter XP "+amount);}
 static void cycles()throws Exception{
  int cycles=0;long now=System.currentTimeMillis();
  for(BirdhouseType type:BirdhouseType.values())for(BirdhouseSpot spot:BirdhouseSpot.values()){
   Player p=actor();position(p,spot);long[] time={now};Birdhouses b=clocked(p,time);item(p,type.log,1);item(p,8792,1);p.playerLevel[12]=type.craftingLevel-1;
   b.craft(type.log,8792);ok(count(p,type.item)==0&&count(p,type.log)==1&&count(p,8792)==1,"Crafting level "+type);p.playerLevel[12]=99;b.craft(type.log,8792);ok(count(p,type.item)==0,"Tools missing");item(p,2347,1);item(p,1755,1);int old=p.playerXP[12];b.craft(type.log,8792);ok(count(p,type.item)==1&&count(p,type.log)==0&&count(p,8792)==0&&p.playerXP[12]>old,"Craft ingredients/XP "+type);
   p.playerLevel[21]=type.hunterLevel-1;ok(!b.place(spot,type)&&count(p,type.item)==1,"Hunter level "+type);p.playerLevel[21]=99;ok(b.place(spot,type)&&count(p,type.item)==0&&b.configuration(spot)==1+type.ordinal()*3,"Placement "+type);ok(!b.place(spot,type),"Overwrite occupied house");
   old=p.playerXP[21];item(p,5305,3);ok(b.addSeeds(spot,5305)==3,"Partial low bait");item(p,5295,3);ok(b.addSeeds(spot,5295)==3,"Mixed high bait");time[0]+=60*60_000;ok(b.birds(spot)==0&&!b.dismantle(spot,true)&&p.playerXP[21]==old,"Unbaited capture");item(p,5311,2);ok(b.addSeeds(spot,5311)==1&&count(p,5311)==1,"Final high bait consumption");ok(b.addSeeds(spot,5311)==0&&count(p,5311)==1,"Overfill bait");
   time[0]+=50*60_000-1;ok(b.birds(spot)==9&&!b.dismantle(spot,true)&&p.playerXP[21]==old,"Early reward");time[0]++;ok(b.birds(spot)==10&&b.configuration(spot)==3+type.ordinal()*3,"Mature state");
   Player q=actor();Birdhouses reload=clocked(q,time);ok(reload.decode(b.encode())&&reload.configuration(spot)==b.configuration(spot)&&reload.birds(spot)==10,"Offline reload");ok(q.getBirdhouses().configuration(spot)==0,"Owner isolation");
   p.playerLevel[21]=1;ok(b.dismantle(spot,true)&&count(p,8792)==1&&b.configuration(spot)==0,"Collect after boost expires");xp(p,old,type.hunterXp);old=p.playerXP[21];ok(!b.dismantle(spot,true)&&p.playerXP[21]==old&&count(p,8792)==1,"Duplicate collect");cycles++;
  }
  System.out.println("BIRDHOUSES_36_CYCLES_NINE_TIERS_FOUR_WORLD_SPOTS_TOOLS_LEVELS_BAIT_TIME_LOOT_XP_ISOLATION_OK "+cycles);
 }
 static void materialsAndState()throws Exception{
  Player p=actor();BirdhouseSpot spot=BirdhouseSpot.MUSHROOM_MEADOW;position(p,spot);long[] time={System.currentTimeMillis()};Birdhouses b=clocked(p,time);item(p,21512,1);b.place(spot,BirdhouseType.REGULAR);item(p,5318,10);ok(!b.useItem(5318,999,spot.x,spot.y)&&count(p,5318)==10,"Unknown object");b.useItem(5318,spot.object,spot.x,spot.y);ok(count(p,5318)==10,"2023-only seed accepted");p.absX-=10;item(p,5305,10);ok(b.addSeeds(spot,5305)==0,"Remote bait");p.absX=spot.x-1;p.heightLevel=1;ok(b.addSeeds(spot,5305)==0,"Wrong height");p.heightLevel=0;b.addSeeds(spot,5305);time[0]+=50*60_000;
  int old=p.playerXP[21];Arrays.fill(p.playerItems,1512);Arrays.fill(p.playerItemsN,1);ok(!b.dismantle(spot,true)&&p.playerXP[21]==old&&b.birds(spot)==10,"Full bag lost reward/state");p.playerItems[0]=0;p.playerItemsN[0]=0;ok(b.dismantle(spot,true)&&count(p,8792)==1,"Full bag recovery");
  p=actor();position(p,spot);b=clocked(p,time);item(p,21512,1);b.place(spot,BirdhouseType.REGULAR);old=p.playerXP[21];ok(b.dismantle(spot,false)&&p.playerXP[21]==old&&count(p,8792)==1,"Early dismantle reward");
  String before=b.encode();ok(!b.decode("0,10,0|-|-|-")&&before.equals(b.encode()),"Invalid state accepted");ok(!b.decode("0,10,1|-|999,10,1|-")&&before.equals(b.encode()),"Partial corrupt state committed");ok(b.decode("0,10,"+Long.MAX_VALUE+"|-|-|-")&&b.birds(spot)==0&&b.remainingMinutes(spot)==50,"Clock rollback overflow");
  System.out.println("BIRDHOUSES_DISTANCE_HEIGHT_INVALID_SEEDS_FULL_BAG_EARLY_DISMANTLE_CORRUPT_STATE_CLOCK_OK");
 }
 static void packetAndSave()throws Exception{
  Player p=actor();item(p,2347,1);item(p,1755,1);item(p,1511,1);item(p,8792,1);
  UseItem.ItemonItem(p,1511,8792,p.getItems().getInventoryItemSlot(1511),p.getItems().getInventoryItemSlot(8792));ok(count(p,21512)==1,"Item-on-item routing");
  BirdhouseSpot spot=BirdhouseSpot.MUSHROOM_MEADOW;position(p,spot);ok(p.getBirdhouses().useItem(21512,spot.object,spot.x,spot.y),"Item-on-object dispatch");
  item(p,5305,10);p.getBirdhouses().useItem(5305,spot.object,spot.x,spot.y);
  p.setIndex(1);PlayerHandler.players[1]=p;p.saveCharacter=true;p.isActive=true;ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();ok(PlayerSave.loadGame(q,"birdqa","IsolatedAudit",true).toString().equals("SUCCESS"),"Character reload");ok(q.getBirdhouses().encode().equals(p.getBirdhouses().encode()),"Birdhouse save entry lost");
  String active=p.getBirdhouses().encode();ok(q.getBirdhouses().decode(active.replaceAll(",10,\\d+",",10,"+(System.currentTimeMillis()-50*60_000))),"Offline seed fixture");position(q,spot);ok(q.getBirdhouses().birds(spot)==10,"Offline completion");PlayerHandler.players[1]=null;
  // Every configured object must be present at its actual coordinate in the loaded map.
  for(BirdhouseSpot s:BirdhouseSpot.values()){Optional<WorldObject> object=RegionProvider.getGlobal().get(s.x,s.y).getWorldObject(s.object,s.x,s.y,0);ok(object.isPresent()&&object.get().type==10,"World spot missing "+s);}
  System.out.println("BIRDHOUSES_ITEM_ON_ITEM_ROUTE_CHARACTER_SAVE_OFFLINE_COMPLETION_REAL_WORLD_OBJECTS_OK");
 }
 static void nests(){
  for(int id:new int[]{5070,5071,5072,5073,5074,19712,19714,19716}){Player p=actor();item(p,id,1);ok(BirdhouseLoot.openNest(p,id)&&count(p,id)==0&&count(p,5075)==1,"Nest search "+id);int n=Arrays.stream(p.playerItemsN).sum();BirdhouseLoot.openNest(p,id);ok(Arrays.stream(p.playerItemsN).sum()==n,"Free nest loot "+id);}
  Player p=actor();item(p,5073,1);for(int i=0;i<27;i++)item(p,1511,1);BirdhouseLoot.openNest(p,5073);ok(count(p,5073)==1&&count(p,5075)==0,"Full bag destroyed nest");
  System.out.println("BIRDHOUSES_EIGHT_NEST_TYPES_OPEN_AND_MATERIAL_FULL_BAG_GATES_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;cycles();materialsAndState();packetAndSave();nests();System.out.println("BIRDHOUSES_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
