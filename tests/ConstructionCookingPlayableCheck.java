package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.Cooking;
import io.xeros.content.skills.cooking.*;
import io.xeros.content.skills.construction.*;
import io.xeros.content.dialogue.*;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class ConstructionCookingPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void blocked(Player p,int mode){switch(mode){case 0:p.isBanking=true;break;case 1:p.isShopping=true;break;case 2:p.teleTimer=1;break;case 3:p.isDead=true;break;case 4:p.setDisconnected(true);break;case 5:p.getHealth().setCurrentHealth(0);break;case 6:p.setMovementState(new PlayerMovementStateBuilder().setLocked(true).createPlayerMovementState());break;}}
 static void fish(){
  int[][] rows={{317,315,30},{321,319,30},{327,325,40},{345,347,50},{353,355,60},{335,333,70},{341,339,75},{349,351,80},{331,329,90},{359,361,100},{363,365,100},{2138,2140,30},{2132,2142,30},{377,379,120},{371,373,140},{383,385,210},{395,397,212},{389,391,216},{7944,7946,150},{11934,11936,220},{13439,13441,212},{3142,3144,190}};
  for(int[]row:rows){Player p=actor();p.playerEquipment[1]=9801;give(p,row[0],1);int xp=p.playerXP[7];ok(Cooking.cookThisFood(p,row[0],2728),"Fish route "+row[0]);tick(2);ok(count(p,row[0])==0&&count(p,row[1])==1&&p.playerXP[7]-xp==row[2],"Fish product/XP "+row[0]);tick(4);ok(count(p,row[1])==1,"Fish repeated after completion");clear(p);}
  Player p=actor();give(p,317,2);p.playerEquipment[1]=9801;int xp=p.playerXP[7];Cooking.cookThisFood(p,317,2728);Cooking.getAmount(p,0);Cooking.getAmount(p,-1);tick(4);ok(count(p,317)==2&&p.playerXP[7]==xp,"Zero/negative quantity cooked food");Cooking.getAmount(p,1);tick(5);ok(count(p,317)==1&&count(p,315)==1&&p.playerXP[7]==xp+30,"One-fish quantity");clear(p);
  for(int mode=0;mode<7;mode++){p=actor();give(p,317,1);xp=p.playerXP[7];blocked(p,mode);ok(!Cooking.cookThisFood(p,317,2728),"Blocked fish start "+mode);tick(4);ok(count(p,317)==1&&p.playerXP[7]==xp,"Blocked fish mutation "+mode);clear(p);
   p=actor();give(p,317,1);xp=p.playerXP[7];Cooking.cookThisFood(p,317,2728);blocked(p,mode);tick(4);ok(count(p,317)==1&&p.playerXP[7]==xp,"Blocked mid-fish mutation "+mode);clear(p);}
  p=actor();p.playerEquipment[1]=9801;p.playerLevel[7]=84;p.playerSkillProp[7][0]=13439;p.playerSkillProp[7][2]=84;for(int i=0;i<100;i++)ok(Cooking.cookFish(p),"Cooking cape failed with drained Cooking");clear(p);
  System.out.println("COOKING_22_FISH_MEAT_ROUTES_PRODUCTS_XP_NO_REPEAT_ZERO_NEGATIVE_QUANTITY_SEVEN_START_AND_MID_ACTION_GUARDS_CAPE_NO_BURN_PASS");
 }
 static void cookingTransactions(){
  CookingRecipes.Bake cake=CookingRecipes.forRaw(1889);Player p=actor();give(p,1889,1);give(p,2347,27);int xp=p.playerXP[7];CookingRecipes.startBake(p,cake,2728,1);tick(2);ok(count(p,1889)==1&&count(p,1891)==0&&p.playerXP[7]==xp,"Full bag cake lost ingredients/tin/XP");clear(p);
  p=actor();give(p,1889,1);give(p,2347,26);p.playerEquipment[1]=9801;CookingRecipes.startBake(p,cake,2728,1);tick(2);ok(count(p,1889)==0&&count(p,1891)==1&&count(p,1887)==1,"Cake with one spare slot");clear(p);
  for(int mode=0;mode<7;mode++){
   p=actor();give(p,2307,1);xp=p.playerXP[7];CookingRecipes.startBake(p,CookingRecipes.forRaw(2307),2728,1);blocked(p,mode);tick(2);ok(count(p,2307)==1&&p.playerXP[7]==xp,"Baking continued invalid state "+mode);clear(p);
   p=actor();CookingRecipes.Preparation r=CookingRecipes.preparations().get(0);give(p,r.first,1);give(p,r.second,1);CookingRecipes.prepare(p,r);blocked(p,mode);tick(3);ok(count(p,r.first)==1&&count(p,r.product)==0,"Preparation continued invalid state "+mode);clear(p);
  }
  p=actor();DairyChurning.Recipe dairy=DairyChurning.recipes().get(0);give(p,dairy.ingredient,1);DairyChurning.make(p,dairy,1);p.isBanking=true;tick(dairy.ticks);ok(count(p,dairy.ingredient)==1&&count(p,dairy.product)==0,"Dairy in bank");clear(p);
  p=actor();AtomicLong now=new AtomicLong(1000000);WineFermentation wine=new WineFermentation(p,now::get);give(p,1987,2);give(p,1937,2);ok(wine.mix(2),"Wine fixture");tick(2);p.isBanking=true;tick(2);ok(count(p,1995)==1&&count(p,1987)==1,"Wine mixing continued in bank");now.addAndGet(12000);ok(wine.process()&&count(p,1993)==1,"Bank blocked passive wine fermentation");ok(!wine.mix(1)&&count(p,1987)==1,"Bank accepted new wine mix");clear(p);
  System.out.println("COOKING_CAKE_TWO_OUTPUT_CAPACITY_ATOMIC_REFUSAL_SEVEN_BAKE_PREP_GUARDS_DAIRY_AND_ACTIVE_VERSUS_PASSIVE_WINE_PASS");
 }
 static DialogueOption[] options(Player p)throws Exception{Object current=p.getDialogueBuilder().getCurrent();Field f=current.getClass().getDeclaredField("options");f.setAccessible(true);return (DialogueOption[])f.get(current);}
 static void house()throws Exception{
  for(int mode=0;mode<7;mode++){Player p=actor();give(p,995,1000);blocked(p,mode);ok(!p.getHouse().purchase()&&!p.getHouse().owned()&&count(p,995)==1000,"Blocked house purchase "+mode);clear(p);}
  Player p=HouseConstructionCheck.house();give(p,995,1000000);int[][] cells={{3,2},{4,2},{4,3},{3,3},{3,4},{2,4},{1,4},{1,3},{1,2}};int i=0;
  for(PlayerHouse.Kind kind:PlayerHouse.Kind.values()){int x=cells[i][0],y=cells[i++][1],coins=count(p,995);p.playerLevel[22]=kind.level-1;ok(!p.getHouse().addRoom(kind,x,y,0)&&count(p,995)==coins,"Underlevel room "+kind);p.playerLevel[22]=kind.level;ok(p.getHouse().addRoom(kind,x,y,i%4)&&count(p,995)==coins-kind.cost,"Room purchase/cost "+kind);p.getNextPlayerMovement();}
  String encoded=p.getHouse().encode();Player q=actor();ok(q.getHouse().decode(encoded)&&q.getHouse().encode().equals(encoded),"Nine room types persistence");p.getHouse().exit();clear(p);clear(q);
  p=HouseConstructionCheck.house();give(p,2347,1);give(p,8794,1);give(p,8778,2);WorldObject root=HouseConstructionCheck.root(p,4515,2,3);HouseFurnishing f=HouseFurnishing.get("OAK_CHAIR");HouseConstructionCheck.select(p,root,f);int xp=p.playerXP[22];p.isBanking=true;tick(5);ok(count(p,8778)==2&&p.playerXP[22]==xp&&!p.getHouse().encode().contains("=OAK_CHAIR"),"Build consumed during bank");p.isBanking=false;
  HouseConstructionCheck.at(p,root.x-1,root.y,1);p.getDynamicMapScene().handleClickObject(p,root,4);String first=options(p)[0].getTitle();p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_4);DialogueOption[] second=options(p);ok(second.length<=5&&second[second.length-1].getTitle().equals("Página anterior"),"Furniture Previous page missing");p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_5);ok(options(p)[0].getTitle().equals(first),"Furniture Previous page did not return");
  Object stale=p.getDialogueBuilder().getCurrent();ok(p.getHouse().rotateRoom(2,3,1),"Rotate fixture");p.getNextPlayerMovement();((DialogueObject)stale).handleAction(p,DialogueAction.OPTION_1);tick(5);ok(count(p,8778)==2&&p.playerXP[22]==xp,"Old build menu survived rebuilt scene");p.getHouse().exit();clear(p);
  System.out.println("CONSTRUCTION_SEVEN_PURCHASE_GUARDS_NINE_ROOM_TYPES_LEVEL_COST_ROTATION_PERSISTENCE_DELAYED_BUILD_BANK_AND_PREVIOUS_STALE_MENU_PASS");
 }
 static void save()throws Exception{Player p=HouseConstructionCheck.house();p.setLoginName("skills55qa");p.setDisplayName("Skills55qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;give(p,315,3);p.getPA().addSkillXPMultiplied(100,22,true);p.getPA().addSkillXPMultiplied(40,7,true);String house=p.getHouse().encode();int construction=p.playerXP[22],cooking=p.playerXP[7];ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills55qa");ok(PlayerSave.loadGame(q,"skills55qa","IsolatedAudit",true).toString().equals("SUCCESS")&&q.getHouse().encode().equals(house)&&q.playerXP[22]==construction&&q.playerXP[7]==cooking&&count(q,315)==3,"House/Cooking actual save reload");PlayerHandler.players[1]=null;p.getHouse().exit();clear(p);clear(q);System.out.println("CONSTRUCTION_COOKING_HOUSE_LAYOUT_BOTH_XP_AND_FOOD_ACTUAL_SAVE_RELOAD_PASS");}
 public static void main(String[]args)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field b=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");b.setAccessible(true);b.set(null,Collections.emptyList());fish();cookingTransactions();System.out.println("COOKING_PLAYABLE_V55_PASS");house();save();System.out.println("CONSTRUCTION_PLAYABLE_V55_PASS");System.exit(0);}
}
