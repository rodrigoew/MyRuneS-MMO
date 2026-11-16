package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.crafting.*;
import io.xeros.content.skills.cooking.*;
import io.xeros.content.items.UseItem;
import io.xeros.content.dialogue.*;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionOne;
import io.xeros.model.collisionmap.*;
import java.lang.reflect.*;
import java.util.*;
public class StationProductionCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void give(Player p,int i,int n){SkillsAuditCheck.item(p,i,n);}
 static int count(Player p,int i){return SkillsAuditCheck.count(p,i);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void xp(Player p,int s,int old,double base,int repetitions){Player q=actor();int before=q.playerXP[s];for(int i=0;i<repetitions;i++)q.getPA().addSkillXPMultiplied(base,s,false);ok(p.playerXP[s]-old==q.playerXP[s]-before&&p.playerXpTenths[s]==q.playerXpTenths[s],"XP per-action precision "+base);}
 static void select(Player p,DialogueAction a){ok(p.getDialogueBuilder()!=null,"Missing menu");p.getDialogueBuilder().getCurrent().handleAction(p,a);}
 static void pottery(){
  int[][] independent={{1787,1931,1},{1789,2313,7},{1791,1923,8},{5352,5350,19},{4438,4440,25}};int index=0;
  for(Pottery.Recipe r:Pottery.recipes()){
   ok(r.unfired==independent[index][0]&&r.product==independent[index][1]&&r.level==independent[index++][2],"Pottery reference");
   Player p=actor();give(p,1761,2);int old=p.playerXP[12];p.playerLevel[12]=r.level-1;Pottery.make(p,r,false,1);tick(4);ok(count(p,r.unfired)==0&&count(p,1761)==2,"Shaping level gate");clear(p);
   p.playerLevel[12]=99;Pottery.make(p,r,false,2);tick(6);ok(count(p,1761)==0&&count(p,r.unfired)==2,"Shaping resources");xp(p,12,old,r.shapingXp,2);clear(p);
   p=actor();give(p,r.unfired,2);old=p.playerXP[12];Pottery.make(p,r,true,2,()->0);tick(14);ok(count(p,r.unfired)==0&&count(p,r.product)==2,"Firing resources");xp(p,12,old,r.firingXp,2);clear(p);
   p=actor();p.playerLevel[12]=1;give(p,r.unfired,1);old=p.playerXP[12];Pottery.make(p,r,true,1,()->.999);tick(7);ok(count(p,r.unfired)==0&&count(p,r.product)==0&&p.playerXP[12]==old,"Crack consumed XP/product");clear(p);
   p=actor();p.playerLevel[12]=1;give(p,r.unfired,1);Pottery.make(p,r,true,1,()->0);tick(7);ok(count(p,r.product)==1,"Firing incorrectly requires shaping level");clear(p);
  }
  for(int[] pair:new int[][]{{1929,1925},{1937,1935},{1921,1923}}){Player p=actor();give(p,434,1);give(p,pair[0],1);int old=p.playerXP[12];ok(Pottery.soften(p,pair[0],434),"Water combination reverse");tick(2);ok(count(p,1761)==1&&count(p,pair[1])==1&&p.playerXP[12]==old,"Soft clay/container/no XP");clear(p);}
  ok(Pottery.firingSuccess(13)==1&&Pottery.firingSuccess(1)<1,"Firing level-13 threshold");
  Player p=actor();give(p,1761,1);Pottery.make(p,Pottery.recipes().get(0),false,1);p.heightLevel++;tick(3);ok(count(p,1761)==1,"Pottery wrong-floor activity");clear(p);
  System.out.println("POTTERY_FIVE_2022_RECIPES_SHAPING_FIRING_LEVELS_CRACK_NO_XP_SOFT_CLAY_CONTAINERS_MOVEMENT_OK");
 }
 static void dairy(){
  for(DairyChurning.Recipe r:DairyChurning.recipes()){
   Player p=actor();give(p,r.ingredient,2);int old=p.playerXP[7];p.playerLevel[7]=r.level-1;DairyChurning.make(p,r,1);tick(r.ticks);ok(count(p,r.product)==0&&count(p,r.ingredient)==2,"Dairy level");clear(p);
   p.playerLevel[7]=99;DairyChurning.make(p,r,2);tick(r.ticks-1);ok(count(p,r.ingredient)==2,"Dairy early product");tick(r.ticks+1);ok(count(p,r.product)==2&&count(p,r.ingredient)==0,"Dairy quantities");if(r.container>0)ok(count(p,r.container)==2,"Dairy container");xp(p,7,old,r.xp,2);clear(p);
   p=actor();give(p,r.ingredient,1);DairyChurning.make(p,r,1);p.absX++;tick(r.ticks);ok(count(p,r.ingredient)==1,"Dairy movement");clear(p);
  }
  Player p=actor();give(p,1927,1);give(p,1931,27);int old=p.playerXP[7];DairyChurning.make(p,DairyChurning.recipes().get(0),1);tick(8);ok(count(p,1927)==1&&count(p,2130)==0&&p.playerXP[7]==old,"Full inventory item loss/XP");clear(p);
  System.out.println("DAIRY_SIX_RECIPES_LEVELS_QUANTITY_CONTAINERS_XP_TIMERS_MOVEMENT_FULL_INVENTORY_OK");
 }
 static void mapRoutes()throws Exception{
  Field regions=RegionProvider.class.getDeclaredField("regions"),objects=Region.class.getDeclaredField("worldObjects");regions.setAccessible(true);objects.setAccessible(true);Map<String,WorldObject> found=new HashMap<>();
  for(Region region:((Map<Integer,Region>)regions.get(RegionProvider.getGlobal())).values())for(WorldObject o:(List<WorldObject>)objects.get(region)){
   if(o.height!=0)continue;
   if(Pottery.wheel(o.id))found.putIfAbsent("wheel",o);if(Pottery.oven(o.id))found.putIfAbsent("oven",o);if(DairyChurning.churn(o.id))found.putIfAbsent("churn",o);
   ObjectDef d=ObjectDef.getObjectDef(o.id);if(d!=null&&"Furnace".equalsIgnoreCase(d.name))found.putIfAbsent("furnace",o);
  }
  for(String key:new String[]{"wheel","oven","churn","furnace"}){
   WorldObject o=found.get(key);ok(o!=null,"No real map station "+key);Player p=actor();p.absX=o.x;p.absY=o.y-1;
   if(key.equals("wheel")){give(p,1761,1);ObjectOptionOne.handleOption(p,o.id,o.x,o.y);select(p,DialogueAction.OPTION_1);select(p,DialogueAction.OPTION_1);tick(3);ok(count(p,1787)==1,"Real wheel click");}
   if(key.equals("oven")){give(p,1787,1);UseItem.ItemonObject(p,o.id,o.x,o.y,1787);select(p,DialogueAction.OPTION_1);select(p,DialogueAction.OPTION_1);tick(7);ok(count(p,1931)==1,"Real oven item route");}
   if(key.equals("churn")){give(p,1927,1);ObjectOptionOne.handleOption(p,o.id,o.x,o.y);select(p,DialogueAction.OPTION_1);select(p,DialogueAction.OPTION_1);tick(8);ok(count(p,2130)==1,"Real churn click");}
   if(key.equals("furnace")){give(p,2357,1);UseItem.ItemonObject(p,o.id,o.x,o.y,2357);ok(p.getDialogueBuilder()!=null,"Gold bar on real furnace");}
   clear(p);System.out.println("STATION_REAL_MAP_ROUTE_OK "+key+" "+o.id+" "+o.x+","+o.y+","+o.height);
  }
  WorldObject o=found.get("churn");Player p=actor();p.absX=o.x;p.absY=o.y-1;give(p,1927,1);DairyChurning.menu(p,o.x,o.y,1927);p.heightLevel=1;select(p,DialogueAction.OPTION_1);tick(30);ok(count(p,1927)==1,"Dairy menu reused on another floor");clear(p);
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;pottery();dairy();mapRoutes();System.out.println("STATION_PRODUCTION_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
