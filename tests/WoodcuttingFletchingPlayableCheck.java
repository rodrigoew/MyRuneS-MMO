package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.*;import io.xeros.content.skills.woodcutting.*;import io.xeros.content.skills.fletching.*;import io.xeros.content.skills.slayer.SlayerUnlock;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import java.lang.reflect.*;import java.util.*;
public final class WoodcuttingFletchingPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){return AttackDefencePlayableCheck.actor();}
 static long xp(Player p,int skill){return AttackDefencePlayableCheck.xp(p,skill);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void block(Player p,int mode){switch(mode){case 0:p.getHealth().setCurrentHealth(0);break;case 1:p.isBanking=true;break;case 2:p.isShopping=true;break;case 3:p.teleTimer=3;break;case 4:p.isDead=true;break;}}
 static void wood()throws Exception{
  WorldObject o=HistoricalTreeCheck.nodes().stream().filter(q->q.id==10820&&q.height==0).findFirst().orElseThrow();
  for(int mode=0;mode<5;mode++){
   Player p=HistoricalTreeCheck.actor(o);block(p,mode);ok(!HistoricalWoodcutting.start(p,o.id,o.x,o.y,b->b==256?0:b-1),"Blocked chop start "+mode);clear(p);
   p=HistoricalTreeCheck.actor(o);long before=xp(p,8);ok(HistoricalWoodcutting.start(p,o.id,o.x,o.y,b->b==256?0:b-1),"Chop start");block(p,mode);tick(4);ok(count(p,1521)==0&&xp(p,8)==before&&!p.playerSkilling[8],"Blocked callback "+mode);clear(p);
  }
  Player p=actor();p.playerEquipment[0]=1359;ok(Hatchet.getBest(p)==null,"Axe in hat slot accepted");p.playerEquipment[0]=-1;p.playerEquipment[3]=1359;ok(Hatchet.getBest(p)==Hatchet.RUNE,"Weapon axe rejected");p.playerEquipment[3]=-1;give(p,1359,1);ok(Hatchet.getBest(p)==Hatchet.RUNE,"Inventory axe rejected");clear(p);
  for(int mode=0;mode<5;mode++){p=actor();give(p,6739,1);give(p,13233,1);long wc=xp(p,8),fm=xp(p,11);ok(p.getWoodcuttingTools().createInfernal(),"Infernal batch fixture");block(p,mode);tick(3);ok(count(p,6739)==1&&count(p,13233)==1&&count(p,13241)==0&&xp(p,8)==wc&&xp(p,11)==fm,"Unsafe infernal crafting "+mode);clear(p);}
  System.out.println("WOODCUTTING_FIVE_START_CALLBACK_INTERACTION_GUARDS_AXE_INVENTORY_WEAPON_ONLY_AND_FIVE_INFERNAL_CALLBACK_MATERIAL_XP_GUARDS_PASS");System.out.println("WOODCUTTING_PLAYABLE_V59_PASS");
 }
 static void reward(Player p,int first,int second,int output,int n,double value,long before,String label){ok(count(p,output)==n&&count(p,first)==3&&count(p,second)==0,label+" materials/result "+n);ok(xp(p,9)-before==Math.round(value*n*10),label+" exact tenths "+n+" got="+(xp(p,9)-before));clear(p);}
 static void partial(){
  int tested=0;for(int n:new int[]{1,9,10,14,15,16,23}){
   Player p=actor();give(p,52,n+3);give(p,314,n);long before=xp(p,9);int made=Math.min(15,n);p.getFletching().fletchHeadlessArrows();ok(count(p,53)==made&&count(p,52)==n+3-made&&count(p,314)==n-made&&xp(p,9)-before==made*10,"Headless partial "+n);clear(p);tested++;
   for(FletchableArrow a:FletchableArrow.values()){p=actor();p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);give(p,53,n+3);give(p,a.getId(),n);before=xp(p,9);p.getFletching().fletchArrow(a.getId());tick(4);reward(p,53,a.getId(),a.getReward(),n,a.getExperience(),before,"Arrow "+a);tested++;}
   for(FletchableDart a:FletchableDart.values()){p=actor();give(p,314,n+3);give(p,a.getId(),n);before=xp(p,9);p.getFletching().fletchDart(a.getId());tick(6);reward(p,314,a.getId(),a.getReward(),n,a.getExperience(),before,"Dart "+a);tested++;}
   for(FletchableJavelin a:FletchableJavelin.values()){p=actor();give(p,19584,n+3);give(p,a.getId(),n);before=xp(p,9);p.getFletching().fletchJavelin(a.getId());tick(4);reward(p,19584,a.getId(),a.getReward(),n,a.getExperience(),before,"Javelin "+a);tested++;}
   for(FletchableBolt a:FletchableBolt.values()){p=actor();p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);give(p,a.getUnfinished(),n+3);give(p,a.getTip(),n);before=xp(p,9);p.getFletching().fletchBolt(a.getUnfinished(),a.getTip());tick(6);reward(p,a.getUnfinished(),a.getTip(),a.getBolt(),n,a.getExperience(),before,"Tipped bolt "+a);tested++;}
   for(FletchableUnfinishedBolt a:FletchableUnfinishedBolt.values()){p=actor();p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);give(p,314,n+3);give(p,a.getUnfinished(),n);before=xp(p,9);p.getFletching().fletchUnfinishedBolt(a.getUnfinished());made=Math.min(10,n);ok(count(p,a.getBolt())==made&&count(p,a.getUnfinished())==n-made&&count(p,314)==n+3-made&&xp(p,9)-before==Math.round(a.getExperience()*made*10),"Unfinished partial "+a+"/"+n);clear(p);tested++;}
  }
  System.out.println("FLETCHING_"+tested+"_PARTIAL_COMPLETE_AND_REMAINDER_AMMO_CASES_ALL_FAMILIES_EXACT_MATERIAL_COUNTS_AND_FRACTIONAL_XP_PASS");
 }
 static void safety(){
  FletchingRecipes.Recipe r=FletchingRecipes.recipes().stream().filter(q->q.first==1511&&q.product==52).findFirst().orElseThrow();
  for(int mode=0;mode<5;mode++){Player p=actor();give(p,1511,1);give(p,946,1);long before=xp(p,9);ok(FletchingRecipes.make(p,r,1),"Recipe fixture");block(p,mode);tick(3);ok(count(p,1511)==1&&count(p,52)==0&&xp(p,9)==before,"Recipe callback guard "+mode);clear(p);
   p=actor();give(p,314,11);give(p,819,11);before=xp(p,9);p.getFletching().fletchDart(819);block(p,mode);tick(4);ok(count(p,819)==11&&count(p,806)==0&&xp(p,9)==before,"Ammo callback guard "+mode);clear(p);
  }
  Player p=actor();give(p,314,23);give(p,819,23);long before=xp(p,9);p.getFletching().fletchDart(819);tick(2);p.interruptActions();tick(4);ok(count(p,806)==10&&count(p,819)==13&&xp(p,9)-before==180,"Interrupted remainder");clear(p);
  p=actor();give(p,314,10);give(p,819,10);p.playerLevel[9]=9;p.getFletching().fletchDart(819);tick(2);ok(count(p,806)==0&&count(p,819)==10,"Dart level");clear(p);
  p=actor();give(p,53,16);give(p,FletchableArrow.BROAD.getId(),16);p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);p.getFletching().fletchArrow(FletchableArrow.BROAD.getId());p.getSlayer().getUnlocks().remove(SlayerUnlock.BROADER_FLETCHING);tick(4);ok(count(p,FletchableArrow.BROAD.getReward())==0,"Unlock removed mid batch");clear(p);
  p=actor();give(p,314,10);give(p,819,10);give(p,806,Integer.MAX_VALUE-5);before=xp(p,9);p.getFletching().fletchDart(819);tick(2);ok(count(p,819)==10&&count(p,314)==10&&count(p,806)==Integer.MAX_VALUE-5&&xp(p,9)==before,"Stack overflow must be atomic");clear(p);
  System.out.println("FLETCHING_FIVE_RECIPE_AND_AMMO_CALLBACK_GUARDS_CANCEL_REMAINDER_LEVEL_UNLOCK_RECHECK_AND_OUTPUT_OVERFLOW_NO_MATERIAL_OR_XP_LOSS_PASS");
 }
 static void save()throws Exception{Player p=actor();p.setLoginName("skills59qa");p.setDisplayName("Skills59qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;give(p,819,3);give(p,314,3);p.getFletching().fletchDart(819);tick(2);p.getPA().addSkillXPMultiplied(37.5,8,true);long wc=xp(p,8),fl=xp(p,9);ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills59qa");ok(PlayerSave.loadGame(q,"skills59qa","IsolatedAudit",true).toString().equals("SUCCESS")&&xp(q,8)==wc&&xp(q,9)==fl&&count(q,806)==3,"Woodcutting Fletching XP/inventory load");clear(p);clear(q);PlayerHandler.players[1]=null;System.out.println("WOODCUTTING_FLETCHING_FRACTIONAL_XP_PRODUCTS_REAL_SAVE_RELOAD_PASS");}
 public static void main(String[]a)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field f=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");f.setAccessible(true);f.set(null,Collections.emptyList());wood();partial();safety();save();System.out.println("FLETCHING_PLAYABLE_V59_PASS");System.exit(0);}
}
