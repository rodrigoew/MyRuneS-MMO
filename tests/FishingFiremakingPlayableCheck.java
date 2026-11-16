package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.*;import io.xeros.content.skills.firemake.*;import io.xeros.model.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import io.xeros.model.entity.npc.*;import io.xeros.model.world.objects.GlobalObject;import java.lang.reflect.*;import java.util.*;
public final class FishingFiremakingPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){return AttackDefencePlayableCheck.actor();}
 static long xp(Player p,int s){return AttackDefencePlayableCheck.xp(p,s);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void block(Player p,int mode){switch(mode){case 0:p.getHealth().setCurrentHealth(0);break;case 1:p.isBanking=true;break;case 2:p.isShopping=true;break;case 3:p.teleTimer=3;break;case 4:p.isDead=true;break;case 5:p.absX++;break;case 6:p.interruptActions();break;}}
 static void fish(){
  int cases=0;for(int[] r:Fishing.data)for(int selection:new int[]{0,1}){
   Player p=actor();if(r[2]>0)give(p,r[2],1);if(r[3]>0)give(p,r[3],1);long before=xp(p,10);ok(Fishing.start(p,r[0],b->selection),"Method start "+r[0]+"/"+r[2]);tick(30);clear(p);
   int item=r[7]>0&&selection==1?r[7]:r[4],amount=r[0]==14?20:1;int actual=count(p,item);
   // Unbaited methods may repeat before thirty ticks; verify one or more exact catch units.
   ok(actual>=amount&&actual%amount==0,"Catch material "+r[0]);int catches=actual/amount;double value=r[7]>0&&selection==1?r[9]:r[5];ok(xp(p,10)-before==Math.round(value*catches*10),"Fishing XP per catch "+r[0]);if(r[3]>0)ok(catches==1&&count(p,r[3])==0,"Last bait conservation "+r[0]);cases++;
  }
  Player p=actor();give(p,309,1);give(p,314,10);ok(!Fishing.start(p,6,b->0),"Pike used fly rod");clear(p);p=actor();p.playerLevel[10]=4;give(p,303,1);ok(!Fishing.start(p,14,b->0),"Karambwanji level5");clear(p);
  for(int level:new int[]{5,9,10,49,50,99}){p=actor();p.playerLevel[10]=level;give(p,303,1);ok(Fishing.start(p,14,b->0),"Ji start");FieldDummy(p,14,1+level/5,5);clear(p);}
  p=actor();give(p,303,1);give(p,3150,1);for(int i=0;p.getItems().freeSlots()>0;i++)give(p,526,1);ok(Fishing.start(p,14,b->0),"Full bag existing ji stack");tick(10);ok(count(p,3150)>1,"Stackable bait fish full bag");clear(p);
  p=actor();give(p,303,1);p.playerEquipment[1]=9798;while(p.getItems().freeSlots()>1)give(p,526,1);ok(Fishing.start(p,1,b->0),"Cape last slot start");tick(10);ok(count(p,317)==1,"Cape must allow one fish in last slot");clear(p);
  for(int mode=0;mode<7;mode++){p=actor();give(p,307,1);give(p,313,2);long before=xp(p,10);ok(Fishing.start(p,2,b->0),"Guard start");block(p,mode);tick(30);ok(count(p,327)==0&&count(p,313)==2&&xp(p,10)==before,"Fishing cancellation "+mode);clear(p);}
  System.out.println("FISHING_"+cases+"_TABLE_PRIMARY_SECONDARY_TOOL_VARIANTS_LAST_BAIT_XP_PIKE_JI_LEVEL_QUANTITIES_FULL_STACK_CAPE_LAST_SLOT_AND_SEVEN_CANCEL_CASES_PASS");
 }
 static void FieldDummy(Player p,int method,int amount,int exp){ // Execute the first scheduled catch at its real delay, then stop.
  long old=xp(p,10);int before=count(p,3150);for(int i=0;i<30&&count(p,3150)==before;i++)tick(1);ok(count(p,3150)-before==amount&&xp(p,10)-old==exp*10,"Ji quantity/XP at level "+p.playerLevel[10]);
 }
 static void spot(){
  Player p=actor();give(p,303,1);NPC n=new NPC(3913,new Position(p.absX+1,p.absY,0));p.clickedNpcIndex=n.getIndex();long before=xp(p,10);ok(Fishing.start(p,1,b->0),"Live spot start");n.absX++;tick(20);ok(count(p,317)==0&&xp(p,10)==before,"Moved spot continued");clear(p);n.unregisterInstant();
  p=actor();give(p,303,1);n=new NPC(3913,new Position(p.absX+1,p.absY,0));p.clickedNpcIndex=n.getIndex();ok(Fishing.start(p,1,b->0),"Spot removal start");n.unregisterInstant();tick(20);ok(count(p,317)==0,"Removed spot continued");clear(p);
  p=actor();give(p,303,1);n=new NPC(3913,new Position(p.absX+10,p.absY,0));p.clickedNpcIndex=n.getIndex();ok(!Fishing.start(p,1,b->0),"Remote spot");clear(p);n.unregisterInstant();
  p=actor();give(p,303,1);ok(Fishing.start(p,1,b->0),"First action");p.interruptActions();give(p,307,1);give(p,313,1);ok(Fishing.start(p,2,b->0),"Replacement action");tick(30);ok(count(p,317)==0&&count(p,327)==1,"Old fish event interfered with new recipe");clear(p);
  System.out.println("FISHING_REAL_REGISTERED_SPOT_MOVEMENT_REMOVAL_DISTANCE_AND_REPLACEMENT_ACTION_PASS");System.out.println("FISHING_PLAYABLE_V60_PASS");
 }
 static int cursor;
 static Player site(){Player p=actor();for(int i=cursor;i<10000;i++){int x=3000+i%100,y=3200+i/100;p.absX=x;p.absY=y;if(p.getRegionProvider().getClipping(x,y,0)==0&&Firemaking.canLight(p,x,y,0)){cursor=i+1;return p;}}throw new AssertionError("No test fire site");}
 static void fires(){
  int cases=0;for(LogData log:LogData.values())for(boolean outfit:new boolean[]{false,true}){Player p=site();give(p,590,1);give(p,log.getlogId(),1);if(outfit){p.playerEquipment[4]=20704;p.playerEquipment[7]=20706;p.playerEquipment[0]=20708;p.playerEquipment[10]=20710;}int x=p.absX,y=p.absY;long before=xp(p,11);Firemaking.lightFire(p,log.getlogId(),new String("tinderbox"));ok(count(p,log.getlogId())==1&&xp(p,11)==before,"Fire cost paid early");tick(3);ok(!Server.getGlobalObjects().exists(5249,x,y,0)&&count(p,log.getlogId())==1,"Early fire");tick(1);ok(Server.getGlobalObjects().exists(5249,x,y,0)&&count(p,log.getlogId())==0&&count(p,590)==1,"Actual fire product and cost");ok(xp(p,11)-before==(long)Math.floor(log.getExperience()*(outfit?1.025:1)*10+1e-7),"Fire XP "+log+"/"+outfit+" got="+(xp(p,11)-before));ok(!p.playerIsFiremaking,"Stuck fire flag");for(int i=0;i<50;i++)Server.getGlobalObjects().pulse();ok(!Server.getGlobalObjects().exists(5249,x,y,0),"Fire never expires");clear(p);cases++;}
  for(int mode=0;mode<7;mode++){Player p=site();give(p,590,1);give(p,1511,1);int x=p.absX,y=p.absY;long before=xp(p,11);Firemaking.lightFire(p,1511,"tinderbox");block(p,mode);tick(4);ok(count(p,1511)==1&&xp(p,11)==before&&!Server.getGlobalObjects().exists(5249,x,y,0)&&!p.playerIsFiremaking,"Cancelled fire material loss "+mode);clear(p);}
  for(int mode=0;mode<4;mode++){Player p=site();give(p,590,1);give(p,1511,1);int x=p.absX,y=p.absY;long before=xp(p,11);Firemaking.lightFire(p,1511,"tinderbox");if(mode==0)p.getItems().deleteItem2(590,1);if(mode==1)p.getItems().deleteItem2(1511,1);if(mode==2)p.playerLevel[11]=0;if(mode==3)Server.getGlobalObjects().add(new GlobalObject(5249,x,y,0,0,10,50,-1));tick(4);ok(xp(p,11)==before&&(mode==1||count(p,1511)==1),"Fire completion recheck "+mode);clear(p);}
  Player p=site();give(p,1511,1);long before=xp(p,11);Firemaking.lightFire(p,1511,"tinderbox");ok(!p.playerIsFiremaking&&count(p,1511)==1,"Missing tinderbox");give(p,590,1);Firemaking.lightFire(p,1511,"invalid");ok(!p.playerIsFiremaking&&xp(p,11)==before,"Invalid fire usage");clear(p);
  p=site();give(p,590,1);give(p,1511,2);p.lastFire=System.currentTimeMillis()-2000;int fx=p.absX,fy=p.absY;Firemaking.lightFire(p,1511,"tinderbox");tick(1);ok(Server.getGlobalObjects().exists(5249,fx,fy,0)&&count(p,1511)==1,"Quick consecutive fire");clear(p);
  p=site();p.heightLevel=1;give(p,590,1);give(p,1511,1);fx=p.absX;fy=p.absY;Firemaking.lightFire(p,1511,"tinderbox");tick(4);ok(Server.getGlobalObjects().exists(5249,fx,fy,1)&&!Server.getGlobalObjects().exists(5249,fx,fy,0),"Fire correct height");clear(p);
  System.out.println("FIREMAKING_"+cases+"_ALL_LOGS_OUTFIT_EXACT_XP_FOUR_TICK_ATOMIC_COMPLETION_REAL_FIRE_EXPIRY_SEVEN_CANCEL_AND_FOUR_RESOURCE_TILE_LEVEL_RECHECKS_PASS");
 }
 static void save()throws Exception{Player p=actor();p.setLoginName("skills60qa");p.setDisplayName("Skills60qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;p.getPA().addSkillXPMultiplied(105,10,true);p.getPA().addSkillXPMultiplied(303.8,11,true);give(p,3142,1);long fish=xp(p,10),fire=xp(p,11);ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills60qa");ok(PlayerSave.loadGame(q,"skills60qa","IsolatedAudit",true).toString().equals("SUCCESS")&&xp(q,10)==fish&&xp(q,11)==fire&&count(q,3142)==1,"Fishing Firemaking save load");clear(p);clear(q);PlayerHandler.players[1]=null;System.out.println("FISHING_FIREMAKING_FRACTIONAL_XP_PRODUCTS_REAL_SAVE_RELOAD_PASS");System.out.println("FIREMAKING_PLAYABLE_V60_PASS");}
 public static void main(String[]a)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field f=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");f.setAccessible(true);f.set(null,Collections.emptyList());fish();spot();fires();save();System.exit(0);}
}
