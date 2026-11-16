package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.construction.*;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import java.lang.reflect.*;import java.util.*;
public final class HouseWorkshopCheck{
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void bag(Player p){Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);}
 static Player house(){Player p=HouseConstructionCheck.house();ok(p.getHouse().addRoom(PlayerHouse.Kind.WORKSHOP,3,2,0),"Workshop cost/level");p.getNextPlayerMovement();bag(p);return p;}
 static WorldObject object(Player p,int id){return p.getRegionProvider().get(8000,8000).worldObjectsSnapshot().stream().filter(o->o.id==id&&o.height==1&&o.x>=8024&&o.x<8032&&o.y>=8016&&o.y<8024).findFirst().orElseThrow(()->new AssertionError("Workshop model "+id));}
 static void click(Player p,WorldObject o,int action){HouseConstructionCheck.at(p,o.x-1,o.y,1);ok(p.getDynamicMapScene().handleClickObject(p,o,action),"Click workshop");}
 static void choose(Player p,int i){HouseConstructionCheck.choice(p,i);}
 static void prepare(Player p,HouseFurnishing f){bag(p);give(p,2347,1);give(p,8794,1);for(int[] in:f.materials)give(p,in[0],in[1]);}
 static void select(Player p,HouseFurnishing f){
  String prior=f.prerequisite();HouseFurnishing previous=HouseFurnishing.get(prior);WorldObject o=object(p,previous==null?f.group:previous.models[0]);click(p,o,previous==null?4:5);
  List<HouseFurnishing> available=new ArrayList<>();for(var row:HouseFurnishing.all())if(row.group==f.group&&Objects.equals(row.prerequisite(),prior))available.add(row);
  int index=available.indexOf(f);ok(index>=0,"Recipe available");for(int i=0;i<index/3;i++)choose(p,3);choose(p,index%3);
 }
 static void ensure(Player p,HouseFurnishing f){if(f.prerequisite()!=null)ensure(p,HouseFurnishing.get(f.prerequisite()));prepare(p,f);select(p,f);tick(5);ok(p.getHouse().encode().contains(f.group+"="+f.key),"Built "+f.key);}
 static void allRecipes(){
  int tested=0;
  for(HouseFurnishing f:HouseFurnishing.all())if(f.group>=15439&&f.group<=15450){
   Player p=house();if(f.prerequisite()!=null)ensure(p,HouseFurnishing.get(f.prerequisite()));prepare(p,f);int xp=p.playerXP[22];select(p,f);tick(4);ok(p.playerXP[22]==xp,"Premature XP "+f.key);for(int[] in:f.materials)ok(count(p,in[0])==in[1],"Premature materials");tick(1);
   ok(p.playerXP[22]==xp+(int)f.xp&&p.getHouse().encode().contains(f.group+"="+f.key),"Five tick historical XP "+f.key);for(int[] in:f.materials)ok(count(p,in[0])==0,"Materials "+f.key);
   for(int model:f.models)if(model>=0)ok(object(p,model)!=null,"All furniture parts");
   WorldObject main=object(p,f.models[0]);click(p,main,4);choose(p,0);ok(!p.getHouse().encode().contains(f.group+"=")&&object(p,f.group)!=null&&p.playerXP[22]==xp+(int)f.xp,"Remove/no refund/no XP "+f.key);p.getHouse().exit();tested++;
  }
  ok(tested==20&&HouseFurnishing.all().size()==207,"20 additional recipes");
  System.out.println("HOUSE_WORKSHOP_ALL20_RECIPES_REAL_8X8_ROOM_FIVE_TICKS_UPGRADE_PREREQUISITES_EXACT_MATERIALS_INCREMENTAL_XP_GROUP_MODELS_REMOVE_AND_NO_REFUND_OK");
 }
 static void literalFacts(){
  for(Object[] r:new Object[][]{{"WOODEN_WORKBENCH",17,143d},{"STEEL_FRAMED_WORKBENCH",46,440d},{"BENCH_WITH_VICE",62,140d},{"BENCH_WITH_LATHE",77,140d},{"CRAFTING_TABLE_2",25,1d},{"CRAFTING_TABLE_3",34,2d},{"TOOL_STORE_4",44,120d},{"ARMOUR_STAND",55,500d},{"BANNER_EASEL",66,510d}}){
   HouseFurnishing f=HouseFurnishing.get((String)r[0]);ok(f.level==(int)r[1]&&f.xp==(double)r[2],"Independent 2022 level/XP "+r[0]);
  }
  ok(Arrays.deepEquals(HouseFurnishing.get("BENCH_WITH_VICE").materials,new int[][]{{8778,2},{2353,1}})&&"STEEL_FRAMED_WORKBENCH".equals(HouseFurnishing.get("BENCH_WITH_VICE").prerequisite()),"Upgrade materials rather than total base cost");
  ok(Arrays.equals(HouseFurnishing.get("TOOL_STORE_5").models,new int[]{6786,6787,6788,6789,6790}),"All five stores remain built");ok(PlayerHouse.Kind.WORKSHOP.level==15&&PlayerHouse.Kind.WORKSHOP.cost==10000,"Historical room");
  System.out.println("HOUSE_WORKSHOP_LITERAL_2022_ROOM_15_10000_UPGRADE1_AND2_XP_VICE140_OAK2_STEEL1_AND_FIVE_CUMULATIVE_TOOL_MODELS_OK");
 }
 static void tools(){
  Player p=house();ensure(p,HouseFurnishing.get("TOOL_STORE_5"));bag(p);int xp=p.playerXP[22];int[][] literal={{8794,2347,1755,1735},{1925,946,952,590},{1757,1785,1733},{1595,1597,1592,1599,11065,5523},{5341,952,5325,5343,5331,5329}};
  for(int tier=0;tier<5;tier++){
   ok(Arrays.equals(literal[tier],HouseWorkshopData.tools(tier)),"Historical tool contents");
   for(int index=0;index<literal[tier].length;index++){click(p,object(p,6786+tier),1);for(int page=0;page<index/3;page++)choose(p,3);int before=count(p,literal[tier][index]);choose(p,index%3);ok(count(p,literal[tier][index])==before+1,"Tool menu "+literal[tier][index]);}
  }
  ok(p.playerXP[22]==xp,"No XP taking tools");int[] inventory=p.playerItems.clone();p.getHouse().exit();p.getNextPlayerMovement();ok(Arrays.equals(inventory,p.playerItems),"Ordinary tools survive house exit");String saved=p.getHouse().encode();ok(p.getHouse().decode(saved)&&p.getHouse().enter(false),"Reenter saved normal house");p.getNextPlayerMovement();
  for(int id:new int[]{6786,6787,6788,6789,6790})ok(object(p,id)!=null,"Normal mode cumulative store");for(int r=1;r<4;r++){p.getHouse().command("build");p.getNextPlayerMovement();ok(p.getHouse().rotateRoom(3,2,r),"Rotation");p.getNextPlayerMovement();for(int id:new int[]{6786,6787,6788,6789,6790})ok(object(p,id)!=null,"All rotated stores");}
  bag(p);give(p,1927,28);int[] ids=p.playerItems.clone();click(p,object(p,6786),1);choose(p,0);ok(Arrays.equals(ids,p.playerItems),"Full bag tool taking atomic");p.getHouse().exit();
  System.out.println("HOUSE_WORKSHOP_FIVE_NATIVE_TOOL_STORES_23_LITERAL_ITEMS_PAGED_MENUS_NO_XP_FULL_BAG_NO_LOSS_SAVE_NORMAL_MODE_ROTATIONS_AND_ITEMS_RETAINED_ON_EXIT_OK");
 }
 static void cancelledUpgrade(){
  Player p=house();HouseFurnishing f=HouseFurnishing.get("CRAFTING_TABLE_2");ensure(p,HouseFurnishing.get("CRAFTING_TABLE_1"));prepare(p,f);int xp=p.playerXP[22];select(p,f);tick(3);p.absX--;tick(2);ok(p.playerXP[22]==xp&&count(p,1775)==1&&object(p,6796)!=null,"Moved upgrade preserves base/materials");
  prepare(p,f);select(p,f);tick(4);p.playerLevel[22]=24;tick(1);ok(p.playerXP[22]==xp&&count(p,1775)==1&&object(p,6796)!=null,"Drained upgrade level retains old furniture");p.playerLevel[22]=99;
  prepare(p,f);select(p,f);tick(4);p.getItems().deleteItem2(1775,1);tick(1);ok(p.playerXP[22]==xp&&object(p,6796)!=null,"Missing upgrade material no XP/base loss");
  prepare(p,f);select(p,f);tick(5);ok(p.playerXP[22]==xp+1&&object(p,6797)!=null,"Retry exact incremental 1 XP");click(p,object(p,6797),5);ok(object(p,6797)!=null,"Upgrade action never removes table");p.getHouse().exit();
  System.out.println("HOUSE_WORKSHOP_UPGRADE_MOVEMENT_LEVEL_DRAIN_MISSING_MATERIAL_CANCEL_KEEP_PREVIOUS_FURNITURE_RETRY_LITERAL1_XP_AND_DISTINCT_UPGRADE_REMOVE_ACTIONS_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());literalFacts();allRecipes();tools();cancelledUpgrade();System.out.println("HOUSE_WORKSHOP_ALL_CHECKS_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
