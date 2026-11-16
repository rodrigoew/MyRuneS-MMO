package io.xeros.solo;
import io.xeros.*;import io.xeros.content.combat.formula.rework.*;import io.xeros.content.combat.core.*;import io.xeros.content.combat.melee.*;import io.xeros.content.combat.range.*;import io.xeros.content.skills.prayer.*;import io.xeros.model.*;import io.xeros.model.definitions.*;import io.xeros.model.entity.*;import io.xeros.model.entity.npc.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.player.save.PlayerSave;import io.xeros.model.cycleevent.*;import io.xeros.model.cycleevent.impl.*;import java.util.*;import java.lang.reflect.*;
public final class RangedPrayerPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){Player p=AttackDefencePlayableCheck.actor();p.rigour=p.augury=true;p.lastSpear=0;p.switchDelay=0;return p;}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static double call(String n,Class<?>[] types,Object...args)throws Exception{return AttackDefencePlayableCheck.invoke(RangeCombatFormula.STANDARD,n,types,args);}
 static long xp(Player p,int s){return AttackDefencePlayableCheck.xp(p,s);}
 static void ranged()throws Exception{
  Player p=actor(),target=actor();p.playerEquipment[3]=861;int cases=0;
  int[] prayers={-1,CombatPrayer.SHARP_EYE,CombatPrayer.HAWK_EYE,CombatPrayer.EAGLE_EYE,CombatPrayer.RIGOUR};double[] factors={1,1.05,1.1,1.15,1.23};
  for(int style=0;style<3;style++)for(int level:new int[]{1,10,50,99,112})for(int i=0;i<prayers.length;i++)for(int bonus:new int[]{-100,-64,0,49,100,150}){p.getCombatConfigs().setAttackStyle(style);p.playerLevel[4]=level;Arrays.fill(p.prayerActive,false);if(prayers[i]>=0)p.prayerActive[prayers[i]]=true;p.playerBonus[Bonus.RANGED_STRENGTH.ordinal()]=bonus;int eff=(int)Math.floor(level*factors[i])+8+(style==0?3:0);int expected=Math.max(0,(int)Math.floor(.5+eff*(bonus+64d)/640));ok(RangeCombatFormula.STANDARD.getMaxHit(p,target)==expected,"Ranged formula "+style+"/"+level+"/"+i+"/"+bonus);cases++;}
  Arrays.fill(p.prayerActive,false);p.playerLevel[4]=99;p.getCombatConfigs().setAttackStyle(0);p.playerEquipment[0]=11664;p.playerEquipment[4]=8839;p.playerEquipment[7]=8840;p.playerEquipment[9]=8842;
  ok(call("getEffectiveRangeLevel",new Class[]{Player.class,Entity.class},p,target)==121,"Void10percent");p.playerEquipment[4]=13072;p.playerEquipment[7]=13073;ok(call("getEffectiveRangeLevel",new Class[]{Player.class,Entity.class},p,target)==123,"EliteVoid12.5percent");
  Arrays.fill(p.playerEquipment,-1);p.playerEquipment[3]=Items.TWISTED_BOW;p.getCombatConfigs().setAttackStyle(0);NPC n=CombatMagicPaymentCheck.target(p);int magic=n.getCombatDefinition().getLevel(io.xeros.model.entity.npc.stats.NpcCombatSkill.MAGIC);double actual=call("applyAttackSpecials",new Class[]{Player.class,Entity.class,double.class,double.class},p,n,10000d,1d);ok(actual==Math.floor(10000*RangeCombatFormula.getTwistedBowAccuracyBoost(magic,false)),"Tbow accuracy adds extra100percent");ok(RangeCombatFormula.STANDARD.getMaxHit(p,target,-2,1)==0,"Negative ranged special");clear(p);clear(target);
  for(int style=0;style<3;style++){p=actor();p.playerEquipment[3]=861;p.getCombatConfigs().setAttackStyle(style);long r=xp(p,4),d=xp(p,1),hp=xp(p,3);HitDispatcher.getHitEntity(p,actor()).addCombatXP(CombatType.RANGE,10);ok(xp(p,4)-r==(style==2?200:400)&&xp(p,1)-d==(style==2?200:0)&&xp(p,3)-hp==133,"Ranged training XP "+style);clear(p);}
  System.out.println("RANGED_"+cases+"_MAX_HITS_THREE_STYLES_FIVE_PRAYERS_VOID_ELITE_TWISTED_ACCURACY_NEGATIVE_SPECIAL_AND_TRAINING_XP_PASS");
 }
 static void ammo()throws Exception{
  for(int style=0;style<3;style++)for(int bow:new int[]{861,11235}){Player p=actor();p.playerEquipment[3]=bow;p.playerEquipmentN[3]=1;p.playerEquipment[13]=892;p.playerEquipmentN[13]=10;p.usingBow=true;p.attackTimer=0;p.getCombatConfigs().setAttackStyle(style);p.playerBonus[Bonus.ATTACK_RANGED.ordinal()]=-64;NPC n=CombatMagicPaymentCheck.target(p);p.attacking.attackEntity(n);int expected=ItemStats.forId(bow).getEquipment().getAttackSpeed()-(style==1?1:0);ok(p.attackTimer==expected,"Single Rapid reduction "+bow+"/"+style+" got="+p.attackTimer+" expected="+expected);ok(p.playerEquipmentN[13]==(bow==11235?8:9),"Real bow shot ammo "+bow+" amount="+p.playerEquipmentN[13]+" style="+style);clear(p);}
  Player p=actor();p.playerEquipment[3]=841;p.playerEquipment[13]=892;ok(!Bow.canUseArrow(p),"Low bow accepts rune arrows");p.playerEquipment[3]=861;ok(Bow.canUseArrow(p),"MSB rejects rune arrows");p.playerEquipment[13]=-1;ok(!Bow.canUseArrow(p),"Empty arrows");p.playerEquipment[13]=892;p.playerEquipmentN[13]=1;p.getItems().deleteArrow();ok(p.playerEquipmentN[13]==0&&p.playerEquipment[13]==-1,"Last arrow clears slot");clear(p);
  System.out.println("RANGED_SIX_ACTUAL_BOW_ATTACKS_RAPID_SINGLE_TICK_REDUCTION_SINGLE_DOUBLE_AMMO_AND_COMPATIBILITY_LAST_ARROW_PASS");System.out.println("RANGED_PLAYABLE_V58_PASS");
 }
 static boolean conflict(int a,int b){
  int[] def={0,5,13},atk={2,7,15},str={1,6,14},rng={3,11,19},mage={4,12,20},over={16,17,18,21,22,23};
  boolean aCombat=a<=7||a==11||a==12||a==13||a==14||a==15||a==19||a==20||a>=25;boolean bCombat=b<=7||b==11||b==12||b==13||b==14||b==15||b==19||b==20||b>=25;
  if(a>=25&&bCombat||b>=25&&aCombat)return true;
  for(int[] group:new int[][]{def,atk,str,rng,mage,over})if(Arrays.stream(group).anyMatch(v->v==a)&&Arrays.stream(group).anyMatch(v->v==b))return true;
  boolean aOff=aCombat&&!Arrays.stream(def).anyMatch(v->v==a),bOff=bCombat&&!Arrays.stream(def).anyMatch(v->v==b);
  return aOff&&bOff&&!(Arrays.stream(atk).anyMatch(v->v==a)&&Arrays.stream(str).anyMatch(v->v==b)||Arrays.stream(str).anyMatch(v->v==a)&&Arrays.stream(atk).anyMatch(v->v==b));
 }
 static void prayers(){
  Player p=actor();int pairs=0;for(int a=0;a<29;a++)for(int b=0;b<29;b++){CombatPrayer.resetPrayers(p);CombatPrayer.activatePrayer(p,a);ok(p.prayerActive[a],"Activate "+a);CombatPrayer.activatePrayer(p,b);if(a==b)ok(!p.prayerActive[a],"Toggle "+a);else ok(p.prayerActive[b]&&p.prayerActive[a]!=conflict(a,b),"Prayer conflict "+a+"->"+b);pairs++;}
  CombatPrayer.resetPrayers(p);CombatPrayer.activatePrayer(p,-1);CombatPrayer.activatePrayer(p,Integer.MAX_VALUE);ok(QuickPrayers.noneActivate(p),"Invalid id");
  for(int i=0;i<29;i++){if(i==0)continue;CombatPrayer.resetPrayers(p);p.playerXP[5]=p.getPA().getXPForLevel(CombatPrayer.PRAYER_LEVEL_REQUIRED[i]-1);CombatPrayer.activatePrayer(p,i);ok(!p.prayerActive[i],"Prayer base level guard "+i);p.playerXP[5]=13034431;}
  p.playerLevel[1]=1;for(int i:new int[]{25,26,27,28}){CombatPrayer.resetPrayers(p);CombatPrayer.activatePrayer(p,i);ok(p.prayerActive[i],"Drained Defence blocks base unlock "+i);}p.playerXP[1]=0;for(int i:new int[]{25,26,27,28}){CombatPrayer.resetPrayers(p);CombatPrayer.activatePrayer(p,i);ok(!p.prayerActive[i],"Base Defence guard "+i);}p.playerXP[1]=13034431;
  CombatPrayer.resetPrayers(p);p.getQuick().getNormal()[1]=true;p.getQuick().getNormal()[2]=true;QuickPrayers.toggle(p);ok(p.prayerActive[1]&&p.prayerActive[2],"Quick multi activation");p.prayerActive[10]=true;QuickPrayers.toggle(p);ok(QuickPrayers.noneActivate(p),"Quick off leaves other prayers");
  p.playerXP[1]=0;ok(QuickPrayers.clickButton(p,174057)&&p.getQuick().getNormal()[24],"Preserve quick has erroneous Defence requirement");clear(p);
  System.out.println("PRAYER_"+pairs+"_ORDERED_ACTIVATION_PAIRS_LEVEL_DEFENCE_BASE_NOT_DRAINED_INVALID_IDS_QUICK_MULTI_ON_ALL_OFF_AND_PRESERVE_SELECTION_PASS");
 }
 static void drain(){
  for(int bonus:new int[]{-100,0,30,100}){Player p=actor();p.prayerActive[18]=true;p.playerBonus[Bonus.PRAYER.ordinal()]=bonus;int before=p.playerLevel[5];for(int i=0;i<300;i++)CombatPrayer.handlePrayerDrain(p);ok(p.playerLevel[5]<before&&p.playerLevel[5]>=0&&Double.isFinite(p.prayerPoint),"Prayer drain bonus "+bonus);clear(p);}
  Player p=actor();p.playerLevel[5]=1;p.prayerActive[26]=true;for(int i=0;i<100;i++)CombatPrayer.handlePrayerDrain(p);ok(p.playerLevel[5]==0&&QuickPrayers.noneActivate(p),"Exhaustion resets prayers");p.prayerPoint=0;CombatPrayer.handlePrayerDrain(p);ok(p.playerLevel[5]==0,"Inactive drain");p.playerLevel[5]=99;p.prayerPoint=Double.NaN;p.prayerActive[18]=true;CombatPrayer.handlePrayerDrain(p);ok(Double.isFinite(p.prayerPoint),"Bad saved drain counter");clear(p);
  p=actor();p.prayerActive[8]=true;p.playerLevel[0]=50;p.playerLevel[2]=110;EventHandler h=new EventHandler();h.submit(new SkillRestorationEvent(p));for(int i=0;i<49;i++)h.process();ok(p.playerLevel[0]==50,"Rapid early");h.process();ok(p.playerLevel[0]==51&&p.playerLevel[2]==110,"Rapid drained only");for(int i=0;i<50;i++)h.process();ok(p.playerLevel[0]==52&&p.playerLevel[2]==109,"Rapid normal cycle");clear(p);
  System.out.println("PRAYER_DRAIN_NEGATIVE_ZERO_POSITIVE_BONUSES_EXHAUSTION_INVALID_COUNTER_AND_RAPID_RESTORE_ACTUAL50_100_TICK_DRAINED_ONLY_PASS");
 }
 static void save()throws Exception{Player p=actor();p.setLoginName("skills58qa");p.setDisplayName("Skills58qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;p.playerLevel[5]=37;p.getPA().addSkillXPMultiplied(40,4,true);p.getPA().addSkillXPMultiplied(72,5,true);p.prayerPoint=.4;long r=xp(p,4),pr=xp(p,5);ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills58qa");ok(PlayerSave.loadGame(q,"skills58qa","IsolatedAudit",true).toString().equals("SUCCESS")&&xp(q,4)==r&&xp(q,5)==pr&&q.playerLevel[5]==37&&q.rigour&&q.augury&&q.prayerPoint==.4,"Ranged Prayer save load");clear(p);clear(q);PlayerHandler.players[1]=null;System.out.println("RANGED_PRAYER_XP_CURRENT_PRAYER_SCROLL_UNLOCKS_DRAIN_FRACTION_REAL_SAVE_RELOAD_PASS");}
 public static void main(String[]a)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field f=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");f.setAccessible(true);f.set(null,Collections.emptyList());ranged();ammo();prayers();drain();save();System.out.println("PRAYER_PLAYABLE_V58_PASS");System.exit(0);}
}




