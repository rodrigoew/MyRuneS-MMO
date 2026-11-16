package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.combat.formula.rework.*;
import io.xeros.content.combat.core.HitDispatcher;
import io.xeros.content.combat.melee.CombatPrayer;
import io.xeros.content.combat.weapon.*;
import io.xeros.model.*;
import io.xeros.model.entity.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.cycleevent.EventHandler;
import io.xeros.model.cycleevent.impl.SkillRestorationEvent;
import java.lang.reflect.*;
import java.util.*;
public final class StrengthHitpointsPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){return AttackDefencePlayableCheck.actor();}
 static long xp(Player p,int s){return AttackDefencePlayableCheck.xp(p,s);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static double invoke(String name,Class<?>[] types,Object...args)throws Exception{return AttackDefencePlayableCheck.invoke(MeleeCombatFormula.get(),name,types,args);}
 static void strength()throws Exception{
  Player target=actor();int checked=0;
  for(WeaponData data:WeaponData.values()){
   int id=Arrays.stream(data.getItems()).filter(i->WeaponData.forItemId(i)==data).findFirst().orElse(Integer.MIN_VALUE);if(id==Integer.MIN_VALUE)continue;
   Player p=actor();p.playerEquipment[3]=id;p.playerLevel[2]=50;
   for(int i=0;i<data.getWeaponModes().length;i++){p.getCombatConfigs().setAttackStyle(i);AttackStyle style=p.getCombatConfigs().getWeaponMode().getAttackStyle();double expected=58+(style==AttackStyle.AGGRESSIVE?3:style==AttackStyle.CONTROLLED?1:0);ok(invoke("getEffectiveStrengthLevel",new Class[]{Player.class},p)==expected,"Actual Strength style "+data+"/"+i);checked++;}clear(p);
  }
  Player p=actor();p.playerEquipment[3]=-1;p.getCombatConfigs().setAttackStyle(1);
  int[] prayers={-1,CombatPrayer.BURST_OF_STRENGTH,CombatPrayer.SUPERHUMAN_STRENGTH,CombatPrayer.ULTIMATE_STRENGTH,CombatPrayer.CHIVALRY,CombatPrayer.PIETY};double[] factors={1,1.05,1.10,1.15,1.18,1.23};int hits=0;
  for(int level:new int[]{1,10,50,99,118})for(int i=0;i<prayers.length;i++)for(int bonus:new int[]{-100,-64,0,64,150,300}){p.playerLevel[2]=level;Arrays.fill(p.prayerActive,false);if(prayers[i]>=0)p.prayerActive[prayers[i]]=true;p.playerBonus[Bonus.STRENGTH.ordinal()]=bonus;int expected=Math.max(0,(int)Math.floor(.5+(Math.floor(level*factors[i])+11)*(bonus+64)/640));int got=MeleeCombatFormula.get().getMaxHit(p,target);ok(got==expected,"Strength level/prayer/bonus "+level+"/"+i+"/"+bonus+" got="+got+" expected="+expected);hits++;}
  Arrays.fill(p.prayerActive,false);p.playerLevel[2]=99;p.playerBonus[Bonus.STRENGTH.ordinal()]=100;
  int base=MeleeCombatFormula.get().getMaxHit(p,target);ok(MeleeCombatFormula.get().getMaxHit(p,target,1.5,1)==Math.floor(base*1.5),"Special multiplier");ok(MeleeCombatFormula.get().getMaxHit(p,target,-2,1)==0,"Negative special damage");
  p.playerEquipment[0]=4716;p.playerEquipment[3]=4718;p.playerEquipment[4]=4720;p.playerEquipment[7]=4722;p.getCombatConfigs().setAttackStyle(0);
  p.getHealth().setCurrentHealth(99);double full=invoke("applyPassiveMultiplier",new Class[]{Entity.class,Entity.class,double.class},p,target,100d);p.getHealth().setCurrentHealth(120);ok(invoke("applyPassiveMultiplier",new Class[]{Entity.class,Entity.class,double.class},p,target,100d)==full,"Dharok overheal reduced damage");p.getHealth().setCurrentHealth(1);ok(invoke("applyPassiveMultiplier",new Class[]{Entity.class,Entity.class,double.class},p,target,100d)>full,"Dharok missing health bonus");clear(p);clear(target);
  for(int index:new int[]{0,1,2}){p=actor();p.playerEquipment[3]=-1;p.getCombatConfigs().setAttackStyle(index);long before=xp(p,2);HitDispatcher.getHitEntity(p,actor()).addCombatXP(CombatType.MELEE,10);ok(xp(p,2)-before==(index==1?400:0),"Strength aggressive XP "+index);clear(p);}
  System.out.println("STRENGTH_ACTUAL_WEAPON_MODES_"+checked+"_AND_"+hits+"_LEVEL_PRAYER_EQUIPMENT_MAX_HITS_SPECIAL_DHAROK_OVERHEAL_AND_AGGRESSIVE_XP_PASS");
  System.out.println("STRENGTH_PLAYABLE_V57_PASS");
 }
 static void healthBounds(){
  Player p=actor();Health h=p.getHealth();h.setCurrentHealth(50);h.increase(-10);ok(h.getCurrentHealth()==50,"Negative healing damaged health");h.increase(Integer.MAX_VALUE);ok(h.getCurrentHealth()==99,"Healing integer overflow");h.setCurrentHealth(120);h.increase(10);ok(h.getCurrentHealth()==120,"Normal food removed overheal");h.increase(30,121);ok(h.getCurrentHealth()==121,"Overheal cap");h.reduce(Integer.MAX_VALUE);ok(h.getCurrentHealth()==0,"Overkill health below zero");h.tick(1);ok(h.getCurrentHealth()==0,"Natural tick resurrected");h.setCurrentHealth(-10);ok(h.getCurrentHealth()==0,"Negative health setter");h.reset();h.reduce(-1);ok(h.getCurrentHealth()==99,"Negative damage");clear(p);
  System.out.println("HITPOINTS_NEGATIVE_HEALING_OVERFLOW_OVERHEAL_CAP_OVERKILL_ZERO_DEAD_TICK_AND_RESET_PASS");
 }
 static void ticks(EventHandler handler,int n){for(int i=0;i<n;i++)handler.process();}
 static void regeneration(){
  int cases=0;for(boolean rapid:new boolean[]{false,true})for(int cape:new int[]{-1,9768,9769,13280})for(boolean bracelet:new boolean[]{false,true}){
   Player p=actor();p.getHealth().setCurrentHealth(50);p.prayerActive[CombatPrayer.RAPID_HEAL]=rapid;p.playerEquipment[1]=cape;p.playerEquipment[9]=bracelet?Items.REGEN_BRACELET:-1;p.playerLevel[2]=110;EventHandler handler=new EventHandler();handler.submit(new SkillRestorationEvent(p));int period=rapid?50:100;int amount=(!rapid&&cape!=-1?2:1)*(bracelet?2:1);
   ticks(handler,period-1);ok(p.getHealth().getCurrentHealth()==50,"Early regeneration");ticks(handler,1);ok(p.getHealth().getCurrentHealth()==50+amount,"Regen combo "+rapid+"/"+cape+"/"+bracelet);ticks(handler,100-period);ok(p.playerLevel[2]==109,"Stat decay cadence changed");ok(p.getHealth().getCurrentHealth()==50+(rapid?2:1)*amount,"Minute healing total");p.getHealth().setCurrentHealth(120);ticks(handler,100);ok(p.getHealth().getCurrentHealth()==119,"Overheal decays once per minute");p.getHealth().setCurrentHealth(0);ticks(handler,100);ok(p.getHealth().getCurrentHealth()==0,"Dead regeneration");clear(p);cases++;
  }
  Player p=actor();p.getHealth().setCurrentHealth(50);EventHandler handler=new EventHandler();handler.submit(new SkillRestorationEvent(p));ticks(handler,99);p.prayerActive[CombatPrayer.RAPID_HEAL]=true;ticks(handler,1);ok(p.getHealth().getCurrentHealth()==50,"Flick instant heal");ticks(handler,49);ok(p.getHealth().getCurrentHealth()==51,"Rapid after transition");p.getHealth().setCurrentHealth(98);p.playerEquipment[9]=Items.REGEN_BRACELET;ticks(handler,50);ok(p.getHealth().getCurrentHealth()==99,"Regen exceeds maximum");p.isDead=true;p.getHealth().setCurrentHealth(10);ticks(handler,100);ok(p.getHealth().getCurrentHealth()==10,"Dead flag regen");clear(p);
  System.out.println("HITPOINTS_"+cases+"_REGEN_COMBINATIONS_ACTUAL_EVENT_TICKS_BOUNDARIES_STAT_DECAY_CAPE_PRAYER_NO_STACK_BRACELET_OVERHEAL_DEAD_AND_TOGGLE_PASS");
 }
 static void xpSave()throws Exception{
  for(CombatType type:new CombatType[]{CombatType.MELEE,CombatType.RANGE,CombatType.MAGE}){Player p=actor();long hp=xp(p,3);HitDispatcher d=HitDispatcher.getHitEntity(p,actor());d.addCombatXP(type,10);ok(xp(p,3)-hp==133,"HP XP "+type);hp=xp(p,3);d.addCombatXP(type,0);ok(xp(p,3)==hp,"Miss XP");clear(p);}
  Player p=actor();p.setLoginName("skills57qa");p.setDisplayName("Skills57qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;p.getHealth().setCurrentHealth(57);p.getPA().addSkillXPMultiplied(40,2,true);p.getPA().addSkillXPMultiplied(13.3,3,true);long str=xp(p,2),hp=xp(p,3);ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills57qa");ok(PlayerSave.loadGame(q,"skills57qa","IsolatedAudit",true).toString().equals("SUCCESS"),"Load");ok(xp(q,2)==str&&xp(q,3)==hp&&q.getHealth().getCurrentHealth()==57,"Strength HP XP current health save reload");PlayerHandler.players[1]=null;clear(p);clear(q);
  System.out.println("HITPOINTS_MELEE_RANGED_MAGIC_DAMAGE_AND_MISS_XP_STRENGTH_HP_FRACTIONAL_XP_CURRENT_HEALTH_REAL_SAVE_RELOAD_PASS");
 }
 public static void main(String[]args)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field f=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");f.setAccessible(true);f.set(null,Collections.emptyList());strength();healthBounds();regeneration();xpSave();System.out.println("HITPOINTS_PLAYABLE_V57_PASS");System.exit(0);}
}
