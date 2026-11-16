package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.farming.*;import io.xeros.content.dialogue.DialogueAction;
import io.xeros.content.skills.herblore.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.definitions.ItemDef;import java.util.*;import java.util.concurrent.atomic.AtomicLong;import java.lang.reflect.*;
public class CompostBinsCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(CompostBins.Bin bin){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.absX=bin.x-1;p.absY=bin.y;p.setLoginName("compostqa");p.setDisplayName("Compostqa");return p;}
 static void give(Player p,int i,int n){SkillsAuditCheck.item(p,i,n);}static int count(Player p,int i){return SkillsAuditCheck.count(p,i);}
 static void tick(int n){SkillsAuditCheck.tick(n);}static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void empty(Player p){Arrays.fill(p.playerItems,0);Arrays.fill(p.playerItemsN,0);}
 static void fill(Player p,CompostBins bins,CompostBins.Bin bin,int item,int amount){
  while(amount>0){int n=Math.min(20,amount);give(p,item,n);ok(bins.use(item,bin.object,bin.x,bin.y),"Use produce route");tick(n*2);amount-=n;}
 }
 static void cycles(){int n=0;
  for(CompostBins.Bin bin:CompostBins.Bin.values())for(int kind=1;kind<=4;kind++){
   Player p=actor(bin);AtomicLong clock=new AtomicLong(1000);CompostBins bins=new CompostBins(p,clock::get);int xp=p.playerXP[19];
   int produce=kind==4?1982:kind>=2?5982:6055;
   fill(p,bins,bin,produce,bin.capacity);
   ok(bins.remaining(bin)==bin.capacity&&p.playerXP[19]==xp&&count(p,produce)==0,"Filled capacity/no XP "+bin+kind);
   ok(bins.tier(bin)==0,"Premature decomposition");bins.click(bin.object,bin.x,bin.y,1);long deadline=bins.deadline(bin);
   ok(deadline==1000+(kind==2||kind==3||bin.capacity==30?90:60)*60_000L,"Rotting duration "+bin+kind);
   clock.set(deadline-1);bins.click(bin.object,bin.x,bin.y,1);ok(!bins.opened(bin),"Early open");
   String saved=bins.encode();CompostBins reload=new CompostBins(p,clock::get);ok(reload.decode(saved),"Rotting batch decode");bins=reload;
   clock.incrementAndGet();bins.click(bin.object,bin.x,bin.y,1);ok(bins.opened(bin),"Offline ready/open");
   if(kind==3){give(p,21622,bin.capacity==30?49:24);bins.use(21622,bin.object,bin.x,bin.y);ok(bins.tier(bin)==2,"Too few ash upgraded");give(p,21622,1);bins.use(21622,bin.object,bin.x,bin.y);ok(bins.tier(bin)==3&&count(p,21622)==0&&p.playerXP[19]==xp,"Ash upgrade cost/XP");}
   int product=kind==4?2518:kind==3?21483:kind==2?6034:6032;double base=kind==1||kind==4?4.5:kind==2?8.5:10;
   Player reference=SkillsAuditCheck.actor();int before=reference.playerXP[19];int collected=0;
   while(collected<bin.capacity){int portion=Math.min(20,bin.capacity-collected);if(kind!=4)give(p,1925,portion);bins.click(bin.object,bin.x,bin.y,1);tick(portion*4);
    ok(count(p,product)==portion,"Harvest product "+bin+kind+" got"+count(p,product));
    for(int i=0;i<portion;i++)reference.getPA().addSkillXPMultiplied(base,19,true);
    collected+=portion;ok(bins.remaining(bin)==bin.capacity-collected,"Remaining batch");empty(p);
   }
   ok(p.playerXP[19]-xp==reference.playerXP[19]-before&&p.playerXpTenths[19]==reference.playerXpTenths[19],"Per-action fractional XP "+bin+kind);
   ok(bins.encode().isEmpty()&&bins.morph(bin)==0,"Empty reset");bins.click(bin.object,bin.x,bin.y,1);tick(8);ok(p.playerXP[19]-xp==reference.playerXP[19]-before,"Repeated extraction XP");clear(p);n++;
  }
  ok(n==28,"Seven bins/four products");System.out.println("COMPOST_28_NATIVE_FACILITY_PRODUCT_CYCLES_CAPACITIES_XP_TIMERS_OFFLINE_UPGRADES_AND_RESET_OK");
 }
 static void safety(){
  CompostBins.Bin bin=CompostBins.Bin.CATHERBY;Player p=actor(bin);CompostBins bins=p.getFarming().getBins();give(p,6055,5);int xp=p.playerXP[19];
  bins.use(6055,bin.object,bin.x,bin.y);tick(1);ok(bins.remaining(bin)==0,"Fill before two ticks");p.interruptActions();tick(2);ok(count(p,6055)==5,"Fill after cancel");
  bins.use(6055,bin.object,bin.x,bin.y);p.absX--;tick(2);ok(count(p,6055)==5,"Fill after movement");p.absX++;
  bins.use(6055,bin.object,bin.x,bin.y);p.heightLevel=1;tick(2);ok(count(p,6055)==5,"Fill after floor change");p.heightLevel=0;
  bins.use(6055,bin.object,bin.x,bin.y);p.isDead=true;tick(2);p.isDead=false;ok(count(p,6055)==5,"Fill after death");
  bins.use(6055,bin.object,bin.x,bin.y);p.getItems().deleteItem2(6055,5);tick(2);ok(bins.remaining(bin)==0,"Unavailable item filled");
  give(p,6055,1);bins.use(6055,bin.object,bin.x,bin.y);tick(2);bins.click(bin.object,bin.x,bin.y,1);ok(bins.tier(bin)==0,"Close partly full");
  give(p,5982,1);bins.use(5982,bin.object,bin.x,bin.y);ok(p.getDialogueBuilder()!=null,"Mixing warning missing");tick(4);ok(count(p,5982)==1,"Warning consumed item");
  p.getDialogueBuilder().sendNextDialogue();p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);tick(2);ok(bins.remaining(bin)==2&&count(p,5982)==0,"Mixing confirmation");
  bins.click(bin.object,bin.x,bin.y,2);ok(bins.remaining(bin)==2,"Dump without confirmation");p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_2);ok(bins.remaining(bin)==2,"Dump on keep");
  bins.click(bin.object,bin.x,bin.y,2);p.absX-=4;p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);ok(bins.remaining(bin)==2,"Remote dump");p.absX+=4;
  bins.click(bin.object,bin.x,bin.y,2);p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);ok(bins.remaining(bin)==0,"Dump confirmed");
  for(int id:new int[]{6056,5983,1511,6016,129,5340})ok(CompostBins.quality(id)==0,"Invalid/noted produce "+id);
  give(p,6056,1);bins.use(6056,bin.object,bin.x,bin.y);tick(4);ok(count(p,6056)==1&&p.playerXP[19]==xp,"Noted items accepted");
  ok(!bins.use(6055,bin.object,bin.x+1,bin.y),"Phantom anchor");p.absX-=50;bins.use(6055,bin.object,bin.x,bin.y);tick(2);ok(bins.remaining(bin)==0,"Remote fill");
  Player low=actor(CompostBins.Bin.GUILD);low.playerLevel[19]=44;give(low,6055,1);low.getFarming().getBins().use(6055,34631,1272,3729);tick(2);ok(count(low,6055)==1,"Guild Farming gate");clear(p);clear(low);
  System.out.println("COMPOST_CANCEL_MOVEMENT_FLOOR_DEATH_MISSING_ITEM_MIX_WARNING_DUMP_CONFIRM_LEVEL_AND_NATIVE_GUARDS_OK");
 }
 static void dosesAndCapacity(){
  CompostBins.Bin bin=CompostBins.Bin.FALADOR;
  for(int potion:new int[]{6470,6472,6474,6476}){
   Player p=actor(bin);CompostBins bins=p.getFarming().getBins();ok(bins.decode("FALADOR,15,0,0,1,1,1"),"Finished fixture");int xp=p.playerXP[19];give(p,potion,1);bins.use(potion,bin.object,bin.x,bin.y);
   ok(bins.tier(bin)==2&&count(p,potion)==0&&count(p,CompostBins.nextDose(potion))==1&&p.playerXP[19]==xp,"Potion dose "+potion);clear(p);
   p=actor(bin);give(p,6032,1);give(p,potion,1);xp=p.playerXP[19];ok(p.getFarming().itemOnItem(potion,6032),"Bucket potion route");tick(2);
   ok(count(p,6034)==1&&count(p,6032)==0&&count(p,CompostBins.nextDose(potion))==1&&p.playerXP[19]==xp,"Bucket potion dose "+potion);clear(p);
  }
  Player p=actor(bin);CompostBins bins=p.getFarming().getBins();bins.decode("FALADOR,15,0,0,1,1,1");int xp=p.playerXP[19];bins.click(bin.object,bin.x,bin.y,1);tick(4);ok(bins.remaining(bin)==15&&p.playerXP[19]==xp,"No bucket reward");
  give(p,1925,1);give(p,1511,27);ok(p.getItems().freeSlots()==0,"Full inventory fixture");bins.click(bin.object,bin.x,bin.y,1);tick(4);ok(count(p,6032)==1&&count(p,1925)==0&&bins.remaining(bin)==14,"Full inventory bucket replacement");clear(p);
  p=actor(bin);bins=p.getFarming().getBins();bins.decode("FALADOR,15,0,15,4,1,1");give(p,1511,28);xp=p.playerXP[19];bins.click(bin.object,bin.x,bin.y,1);tick(4);ok(count(p,2518)==0&&bins.remaining(bin)==15&&p.playerXP[19]==xp,"Full bag tomatoes loss");clear(p);
  for(int level:new int[]{21,22}){p=actor(bin);p.playerLevel[15]=level;give(p,97,1);give(p,21622,1);xp=p.playerXP[15];ok(p.getHerblore().itemOnItem(97,21622),"Compost potion recipe dispatch");tick(2);ok(count(p,6472)==(level==22?1:0)&&count(p,97)==(level==22?0:1),"Compost potion level "+level);ok((p.playerXP[15]>xp)==(level==22),"Compost potion XP");clear(p);}
  System.out.println("COMPOST_FOUR_POTION_DOSES_BUCKET_UPGRADES_NO_BUCKET_FULL_BAG_REPLACEMENT_TOMATOES_AND_HERBLORE_RECIPE_OK");
 }
 static void persistence()throws Exception{
  CompostBins.Bin bin=CompostBins.Bin.CATHERBY;Player p=actor(bin);CompostBins bins=p.getFarming().getBins();
  ok(bins.decode("CATHERBY,15,15,0,2,1,0;GUILD,30,0,0,1,1,0;HOSIDIUS,7,0,7,0,0,0"),"Valid all states");String good=bins.encode();
  for(String bad:new String[]{"CATHERBY,16,0,0,0,0,0","CATHERBY,-1,0,0,0,0,0","CATHERBY,15,0,0,7,1,0","CATHERBY,15,0,0,1,-1,0","CATHERBY,15,0,0,1,0,0","CATHERBY,14,0,0,1,1,0","CATHERBY,1,1,1,0,0,0","CATHERBY,15,0,0,1,1,2","UNKNOWN,1,0,0,0,0,0",good+";"+good}){
   ok(!bins.decode(bad)&&bins.encode().equals(good),"Malformed load changed state "+bad);
  }
  p.setIndex(1);p.saveCharacter=true;p.isActive=true;PlayerHandler.players[1]=p;
  try{ok(PlayerSave.saveGameInstant(p),"Compost save");Player q=actor(bin);ok(PlayerSave.loadGame(q,"compostqa",p.playerPass,true).toString().equals("SUCCESS"),"Compost load");ok(q.getFarming().getBins().encode().equals(good),"Persisted bins");
   PlayerSave.login(q);CompostBins loaded=q.getFarming().getBins();loaded.click(bin.object,bin.x,bin.y,1);ok(loaded.opened(bin),"Finished while offline");give(q,1925,1);loaded.use(1925,bin.object,bin.x,bin.y);tick(4);ok(count(q,6034)==1&&loaded.remaining(bin)==14,"Postload bucket");
   q.setIndex(1);q.saveCharacter=true;q.isActive=true;PlayerHandler.players[1]=q;ok(PlayerSave.saveGameInstant(q),"Partly emptied save");Player again=actor(bin);ok(PlayerSave.loadGame(again,"compostqa",q.playerPass,true).toString().equals("SUCCESS"),"Second load");ok(count(again,6034)==1&&again.getFarming().getBins().remaining(bin)==14,"Reload duplicated reward");clear(q);clear(again);
  }finally{PlayerHandler.players[1]=null;}clear(p);
  System.out.println("COMPOST_SEVEN_INDEPENDENT_BATCH_STATES_ATOMIC_MALFORMED_LOAD_PLAYER_SAVE_OFFLINE_AND_NO_DUPLICATION_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;io.xeros.model.cycleevent.CycleEventHandler.getSingleton().stopEvents(Boolean.FALSE);Configuration.BONUS_SKILL_TIMER=Long.MAX_VALUE;cycles();safety();dosesAndCapacity();persistence();System.out.println("COMPOST_BINS_REVIEW_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}

