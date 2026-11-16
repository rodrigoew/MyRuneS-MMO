package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.*;import io.xeros.content.skills.construction.*;import io.xeros.content.dialogue.*;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import java.util.*;
public final class HouseConstructionCheck{
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id+"/"+n);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void at(Player p,int x,int y,int h){p.absX=x;p.absY=y;p.heightLevel=h;p.mapRegionX=(x>>3)-6;p.mapRegionY=(y>>3)-6;p.currentX=p.getLocalX();p.currentY=p.getLocalY();p.resetWalkingQueue();}
 static Player house(){Player p=MiningGuildCheck.actor(2953,3224);give(p,995,20000);ok(p.getHouse().purchase()&&p.getHouse().roomCount()==2&&p.getHouse().enter(true),"Initial house");p.getNextPlayerMovement();p.checkInstanceCoords();return p;}
 static WorldObject root(Player p,int id,int cellX,int cellY){return p.getRegionProvider().get(8000,8000).worldObjectsSnapshot().stream().filter(o->o.id==id&&o.height==1&&(o.x-8000)/8==cellX&&(o.y-8000)/8==cellY).findFirst().orElseThrow(()->new AssertionError("Root "+id+" cell "+cellX+","+cellY));}
 static void choice(Player p,int index){p.getDialogueBuilder().getCurrent().handleAction(p,new DialogueAction[]{DialogueAction.OPTION_1,DialogueAction.OPTION_2,DialogueAction.OPTION_3,DialogueAction.OPTION_4}[index]);}
 static void select(Player p,WorldObject o,HouseFurnishing f){
  at(p,o.x-1,o.y,1);ok(p.getDynamicMapScene().handleClickObject(p,o,4),"Build action");List<HouseFurnishing> recipes=new ArrayList<>();for(var a:HouseFurnishing.all())if(a.group==f.group)recipes.add(a);int i=recipes.indexOf(f);for(int j=0;j<i/3;j++)choice(p,3);choice(p,i%3);
 }
 static void recipes(){
  Player p=house();ok(p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,3,2,0),"Kitchen");p.getNextPlayerMovement();ok(p.getHouse().addRoom(PlayerHouse.Kind.GARDEN,2,1,0),"Second garden");p.getNextPlayerMovement();int tested=0;
  for(HouseFurnishing f:HouseFurnishing.all()){
   if(Set.of(15260,15261,15262,15268,15298,15299).contains(f.group)||f.group>=15420&&f.group<=15425||f.group>=15439||Set.of(15269,15270,15271,13733,15272,15275,15276).contains(f.group))continue; // Workshop chains have their own upgrade-aware suite.
   Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);give(p,2347,1);give(p,8794,1);give(p,5340,1);for(int[] in:f.materials)give(p,in[0],in[1]);
   int cx=f.group>=15398?3:2,cy=f.group>=15398?2:f.group>=15361?1:3;WorldObject o=root(p,f.group,cx,cy);int xp=p.playerXP[22],farm=p.playerXP[19];
   select(p,o,f);tick(4);ok(p.playerXP[22]==xp,"Premature XP "+f.key);tick(1);
   ok(p.playerXP[22]==xp+(int)f.xp,"Historical furniture XP "+f.key+" "+(p.playerXP[22]-xp));
   for(int[] in:f.materials)ok(p.getItems().getInventoryCount(in[0])==0,"Materials "+f.key+"/"+in[0]);
   if(f.plant)ok(p.playerXP[19]==farm+(int)f.xp&&p.getItems().getInventoryCount(5340)==0&&p.getItems().getInventoryCount(5339)==1,"Plant grants same Farming XP and consumes one water dose "+f.key);
   WorldObject built=p.getRegionProvider().get(o.x,o.y).getWorldObject(f.model(o.id),o.x,o.y,1).orElseThrow(()->new AssertionError("Model "+f.key));
   ok(p.getDynamicMapScene().handleClickObject(p,built,4),"Remove route");choice(p,0);ok(p.playerXP[22]==xp+(int)f.xp,"Remove granted XP");
   if(f.key.equals("EXIT_PORTAL")){p.getHouse().exit();p.getNextPlayerMovement();p=house();ok(p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,3,2,0),"Recreate kitchen");p.getNextPlayerMovement();ok(p.getHouse().addRoom(PlayerHouse.Kind.GARDEN,2,1,0),"Recreate garden");p.getNextPlayerMovement();}
   else ok(p.getRegionProvider().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,1).isPresent(),"Root restored "+f.key);
   SkillsAuditCheck.clear(p);tested++;
  }
  p.getHouse().exit();System.out.println("HOUSE_ALL_"+tested+"_NATIVE_FURNITURE_RECIPES_PAGED_DIALOGUES_FIVE_TICKS_MATERIALS_MODEL_REPLACEMENT_GROUPS_REMOVE_AND_XP_OK");
 }
 static void facts(){
  Object[][] rows={{"CRUDE_WOODEN_CHAIR",1,58d},{"WOODEN_BOOKCASE",4,115d},{"OAK_CHAIR",19,120d},{"OAK_LARDER",33,480d},{"TEAK_LARDER",43,750d},{"GOLD_SINK",47,11144d},{"FERN",1,31d},{"SMALL_YEW_TREE",60,141d}};
  for(Object[] r:rows){HouseFurnishing f=HouseFurnishing.get((String)r[0]);ok(f!=null&&f.level==(int)r[1]&&f.xp==(double)r[2],"Independent literal historical construction fact "+r[0]);}
  HouseFurnishing gold=HouseFurnishing.get("GOLD_SINK");ok(Arrays.deepEquals(gold.materials,new int[][]{{26266,10},{8782,5},{8784,5}})&&gold.models[0]==42852,"2021 gold sink literal materials and model");
  for(Object[] r:new Object[][]{{"BEER_BARREL",5},{"CIDER_BARREL",14},{"ASGARNIAN_ALE_BARREL",24},{"GREENMANS_ALE_BARREL",29},{"DRAGON_BITTER_BARREL",39},{"CHEFS_DELIGHT_BARREL",44}})ok(HouseFurnishing.get((String)r[0]).cookingLevel()==(int)r[1],"Secondary Cooking literal "+r[0]);
  System.out.println("HOUSE_LITERAL_2022_RECIPE_LEVEL_XP_2021_GOLD_SINK_MATERIALS_MODEL_AND_SIX_SECONDARY_COOKING_REQUIREMENTS_OK");
 }
 static void resources(){
  Player p=house();p.playerXP[22]=0;p.playerLevel[22]=1;p.playerXP[19]=0;p.playerLevel[19]=1;Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);give(p,8431,1);give(p,5333,1);
  WorldObject plant=root(p,15364,2,2);select(p,plant,HouseFurnishing.get("FERN"));tick(5);ok(p.playerXP[22]==31&&p.playerXP[19]==31&&count(p,8431)==0&&count(p,5333)==0&&count(p,5331)==1,"Literal Con1 Farm1 plant31 both skills and last can dose");
  Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);give(p,2347,1);give(p,8794,1);give(p,960,2);give(p,4824,2);give(p,4819,2);WorldObject chair=root(p,4515,2,3);select(p,chair,HouseFurnishing.get("CRUDE_WOODEN_CHAIR"));tick(5);ok(count(p,4824)==0&&count(p,4819)==2&&count(p,960)==0&&p.playerXP[22]==89,"Highest nail tier used first and literal chair58 XP");
  p.getHouse().exit();p.getNextPlayerMovement();p=house();give(p,995,5000);ok(p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,3,2,0),"Kitchen for secondary gates");p.getNextPlayerMovement();WorldObject barrel=root(p,15401,3,2);
  for(String key:new String[]{"BEER_BARREL","CIDER_BARREL","ASGARNIAN_ALE_BARREL","GREENMANS_ALE_BARREL","DRAGON_BITTER_BARREL","CHEFS_DELIGHT_BARREL"}){HouseFurnishing f=HouseFurnishing.get(key);Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);give(p,2347,1);give(p,8794,1);for(int[] a:f.materials)give(p,a[0],a[1]);int xp=p.playerXP[22];int[] ids=p.playerItems.clone(),counts=p.playerItemsN.clone();p.playerLevel[7]=f.cookingLevel()-1;select(p,barrel,f);tick(6);ok(p.playerXP[22]==xp&&Arrays.equals(ids,p.playerItems)&&Arrays.equals(counts,p.playerItemsN),"Cooking start gate "+key);p.playerLevel[7]=f.cookingLevel();select(p,barrel,f);p.playerLevel[7]--;tick(6);ok(p.playerXP[22]==xp&&Arrays.equals(ids,p.playerItems)&&Arrays.equals(counts,p.playerItemsN),"Cooking drain guard "+key);}
  p.playerLevel[7]=99;p.getHouse().exit();System.out.println("HOUSE_LITERAL_LOWEST_CONSTRUCTION_AND_FARMING_PLANT31_BOTH_SKILLS_LAST_WATER_DOSE_HIGHEST_NAIL_TIER_CHAIR58_AND_ALL_SIX_COOKING_START_AND_DRAIN_GUARDS_OK");
 }
 static void guards(){
  Player p=MiningGuildCheck.actor(2953,3224);ok(!p.getHouse().purchase(),"Free purchase");give(p,995,1000);ok(p.getHouse().purchase()&&!p.getHouse().purchase()&&p.getItems().getInventoryCount(995)==0,"Purchase atomic once");p.underAttackByNpc=1;ok(!p.getHouse().enter(true),"Combat entry");p.underAttackByNpc=0;ok(p.getHouse().enter(true),"Enter");p.getNextPlayerMovement();p.checkInstanceCoords();
  WorldObject o=root(p,4515,2,3);HouseFurnishing f=HouseFurnishing.get("OAK_CHAIR");give(p,2347,1);give(p,8794,1);give(p,8778,2);int xp=p.playerXP[22];p.playerLevel[22]=1;select(p,o,f);tick(6);ok(p.playerXP[22]==xp&&p.getItems().getInventoryCount(8778)==2,"Level guard");p.playerLevel[22]=99;
  select(p,o,f);p.absX++;tick(6);ok(p.playerXP[22]==xp&&p.getItems().getInventoryCount(8778)==2,"Movement guard");
  select(p,o,f);p.interruptActions();tick(6);ok(p.playerXP[22]==xp&&p.getItems().getInventoryCount(8778)==2,"Interrupt guard");
  select(p,o,f);p.getItems().deleteItem2(8778,1);tick(6);ok(p.playerXP[22]==xp&&p.getItems().getInventoryCount(8778)==1,"Atomic missing material guard");
  give(p,8778,1);select(p,o,f);p.playerLevel[22]=1;tick(6);ok(p.playerXP[22]==xp&&p.getItems().getInventoryCount(8778)==2,"Mid action level drain");
  p.playerLevel[22]=99;ok(!p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,7,7,0)&&!p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,2,2,0),"Disconnected and occupied room");
  give(p,995,10000);ok(p.getHouse().addRoom(PlayerHouse.Kind.KITCHEN,2,4,0),"Connected kitchen");p.getNextPlayerMovement();ok(!p.getHouse().removeRoom(2,3),"Disconnected removal");ok(p.getHouse().rotateRoom(2,3,3),"Rotation");p.getNextPlayerMovement();ok(p.getHouse().removeRoom(2,4),"Leaf removal");p.getNextPlayerMovement();
  for(int[] r:new int[][]{{1,3,24},{15,4,24},{30,5,24},{45,6,24},{50,6,25},{56,6,26},{60,7,26},{62,7,27},{92,7,32},{96,7,33},{99,7,34}})ok(PlayerHouse.dimensions(r[0])==r[1]&&PlayerHouse.maxRooms(r[0])==r[2],"2022 room cap "+r[0]);
  p.getHouse().exit();System.out.println("HOUSE_PURCHASE_COMBAT_OWNER_LEVEL_MATERIAL_MOVE_INTERRUPT_MID_ACTION_DRAIN_LAYOUT_CONNECTIVITY_ROTATION_REMOVAL_AND_2022_LIMITS_OK");
 }
 static void persistence(){
  Player p=house();String before=p.getHouse().encode();Player q=MiningGuildCheck.actor(2953,3224);ok(q.getHouse().decode(before)&&q.getHouse().encode().equals(before),"House roundtrip");
  for(String bad:new String[]{"","0|1","1|2","1|0|2,2,GARDEN,0","1|1|2,2,GARDEN,0","1|1|2,2,GARDEN,0,15361=EXIT_PORTAL|7,7,PARLOUR,0","1|1|2,2,GARDEN,0,15361=EXIT_PORTAL|2,3,PARLOUR,4",before+"|2,3,PARLOUR,0",before.replace("EXIT_PORTAL","OAK_CHAIR"),before.replace("PARLOUR,0","PARLOUR,0,15361=EXIT_PORTAL")}){ok(!q.getHouse().decode(bad)&&q.getHouse().encode().equals(before),"Malformed house save atomic "+bad);}
  p.setLoginName("housepersistqa");p.setDisplayName("Housepersistqa");p.setIndex(1);p.saveCharacter=true;p.isActive=true;PlayerHandler.players[1]=p;ok(PlayerSave.saveGameInstant(p),"House save");q=MiningGuildCheck.actor(2953,3224);q.setLoginName("housepersistqa");ok(PlayerSave.loadGame(q,"housepersistqa","IsolatedAudit",true).toString().equals("SUCCESS")&&q.getHouse().encode().equals(before),"House persisted actual save");at(q,8020,8018,1);q.getHouse().safeLogin();q.getNextPlayerMovement();ok(q.absX==2953&&q.absY==3224&&q.heightLevel==0,"Login cannot leave player in private void");PlayerHandler.players[1]=null;p.getHouse().exit();System.out.println("HOUSE_STRICT_ATOMIC_SAVE_INVALID_LAYOUT_GROUPS_DUPLICATES_VERSION_REAL_CHARACTER_RELOAD_AND_SAFE_LOGIN_EXIT_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;java.lang.reflect.Field boost=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boost.setAccessible(true);boost.set(null,java.util.Collections.emptyList());facts();recipes();resources();guards();persistence();System.out.println("HOUSE_CONSTRUCTION_ALL_CHECKS_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
