package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.combat.formula.rework.*;
import io.xeros.content.combat.core.HitDispatcher;
import io.xeros.content.combat.melee.CombatPrayer;
import io.xeros.content.combat.weapon.*;
import io.xeros.content.skills.slayer.Task;
import io.xeros.model.*;
import io.xeros.model.definitions.NpcDef;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import java.lang.reflect.*;
import java.util.*;

public final class AttackDefencePlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void close(double a,double b,String s){ok(Math.abs(a-b)<1e-10,s+" got="+a+" expected="+b);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.getCombatConfigs().setAttackStyle(0);return p;}
 static double invoke(Object o,String name,Class<?>[]types,Object...args)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return ((Number)m.invoke(o,args)).doubleValue();}
 static long xp(Player p,int skill){return p.playerXP[skill]*10L+p.playerXpTenths[skill];}
 static NPC npc(int id){return new NPC(0,id,NpcDef.forId(id));}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void probabilities(){
  int tested=0;for(int a=0;a<=24;a++)for(int d=0;d<=24;d++){int hits=0;for(int x=0;x<=a;x++)for(int y=0;y<=d;y++)if(x>y)hits++;double expected=hits/((a+1d)*(d+1d));close(CombatFormula.rollAccuracy(a,d),expected,"Enumerated independent probability "+a+"/"+d);tested++;}
  for(double a:new double[]{-100,0,1,100,Integer.MAX_VALUE,Double.MAX_VALUE})for(double d:new double[]{-100,0,1,100,Integer.MAX_VALUE,Double.MAX_VALUE}){double p=CombatFormula.rollAccuracy(a,d);ok(Double.isFinite(p)&&p>=0&&p<=1,"Invalid probability "+a+"/"+d);}
  close(CombatFormula.rollAccuracy(Double.MAX_VALUE,Double.MAX_VALUE),.5,"Double max overflow");close(CombatFormula.rollAccuracy(10,Integer.MAX_VALUE),10d/(2d*(Integer.MAX_VALUE+1d)),"Int max defence overflow");ok(CombatFormula.rollAccuracy(Double.NaN,10)==0&&CombatFormula.rollAccuracy(10,Double.POSITIVE_INFINITY)==0,"Nonfinite values");
  System.out.println("DEFENCE_625_INDEPENDENT_ENUMERATED_ACCURACY_CASES_NEGATIVES_ZERO_INT_DOUBLE_EXTREMES_AND_NONFINITE_GUARDS_PASS");
 }
 static void styles()throws Exception{
  int checked=0;Set<WeaponData> visited=new HashSet<>();
  for(WeaponData data:WeaponData.values()){
   int item=Arrays.stream(data.getItems()).filter(id->WeaponData.forItemId(id)==data).findFirst().orElse(Integer.MIN_VALUE);if(item==Integer.MIN_VALUE)continue;
   Player p=actor();p.playerEquipment[3]=item;p.playerLevel[0]=50;p.playerLevel[1]=50;
   for(int index=0;index<data.getWeaponModes().length;index++){
    p.getCombatConfigs().setAttackStyle(index);AttackStyle style=p.getCombatConfigs().getWeaponMode().getAttackStyle();
    double attack=58+(style==AttackStyle.ACCURATE?3:style==AttackStyle.CONTROLLED?1:0);
    double defence=58+(style==AttackStyle.DEFENSIVE?3:style==AttackStyle.CONTROLLED?1:0);
    close(invoke(MeleeCombatFormula.get(),"getEffectiveAttackLevel",new Class[]{Player.class},p),attack,"Weapon actual Attack style "+data+"/"+index);
    close(invoke(MeleeCombatFormula.get(),"getEffectiveDefenceLevel",new Class[]{Player.class},p),defence,"Weapon Defence style "+data+"/"+index);
    close(invoke(RangeCombatFormula.STANDARD,"getEffectiveDefenceLevel",new Class[]{Player.class},p),defence,"Ranged Defence style "+data+"/"+index);checked++;
   }
   p.getCombatConfigs().setAttackStyle(-100);ok(p.getCombatConfigs().getAttackStyle()==0,"Negative style");p.getCombatConfigs().setAttackStyle(Integer.MAX_VALUE);ok(p.getCombatConfigs().getAttackStyle()==data.getWeaponModes().length-1,"Large style");visited.add(data);clear(p);
  }
  ok(visited.contains(WeaponData.WHIP)&&visited.contains(WeaponData.SPEAR)&&visited.contains(WeaponData.STAFF),"Whip/spear/staff coverage");
  Player p=actor();p.playerLevel[0]=99;p.playerLevel[1]=99;int[] prayers={CombatPrayer.CLARITY_OF_THOUGHT,CombatPrayer.IMPROVED_REFLEXES,CombatPrayer.INCREDIBLE_REFLEXES,CombatPrayer.CHIVALRY,CombatPrayer.PIETY};double[] factors={1.05,1.10,1.15,1.15,1.20};for(int i=0;i<prayers.length;i++){Arrays.fill(p.prayerActive,false);p.prayerActive[prayers[i]]=true;close(invoke(MeleeCombatFormula.get(),"getEffectiveAttackLevel",new Class[]{Player.class},p),Math.floor(99*factors[i])+11,"Prayer Attack rounding "+prayers[i]);}clear(p);
  System.out.println("ATTACK_ACTUAL_WEAPON_MODES_"+checked+"_CASES_"+visited.size()+"_FAMILIES_WHIP_SPEAR_STAFF_STYLE_BONUSES_INVALID_SELECTION_AND_FIVE_PRAYERS_PASS");
 }
 static void masks()throws Exception{
  Player p=actor();NPC chicken=npc(2805);ok(!chicken.isUndead(),"Non-undead fixture");Task t=new Task(new String[]{chicken.getName().toLowerCase(Locale.ROOT)},1,10,1,2,new String[]{"test"},new int[]{3200,3200,0});p.getSlayer().setTask(Optional.of(t));p.getSlayer().setTaskAmount(5);List<Integer> masks=new ArrayList<>();for(int id:MeleeCombatFormula.BLACK_MASKS)masks.add(id);for(int id:MeleeCombatFormula.BLACK_MASKS_I)masks.add(id);
  for(int id:masks){p.playerEquipment[0]=id;close(invoke(MeleeCombatFormula.get(),"specialNpcAttackMultiplier",new Class[]{Player.class,NPC.class},p,chicken),1.1666,"Mask attack duplicated/missing "+id);close(invoke(MeleeCombatFormula.get(),"getNpcMultipliers",new Class[]{Player.class,NPC.class},p,chicken),1.1666,"Mask damage duplicated/missing "+id);p.getSlayer().setTaskAmount(0);close(invoke(MeleeCombatFormula.get(),"specialNpcAttackMultiplier",new Class[]{Player.class,NPC.class},p,chicken),1,"Mask off task "+id);p.getSlayer().setTaskAmount(5);}
  NPC undead=npc(Configuration.UNDEAD_NPCS[0]);p.getSlayer().setTask(Optional.of(new Task(new String[]{undead.getName().toLowerCase(Locale.ROOT)},1,10,1,2,new String[]{"test"},new int[]{3200,3200,0})));p.playerEquipment[0]=MeleeCombatFormula.BLACK_MASKS[0];p.playerEquipment[2]=Items.SALVE_AMULETEI;close(invoke(MeleeCombatFormula.get(),"specialNpcAttackMultiplier",new Class[]{Player.class,NPC.class},p,undead),1.2,"Salve ei stacked with mask");clear(p);
  System.out.println("ATTACK_22_BLACK_MASK_CHARGED_IMBUED_VARIANTS_ONE_BONUS_ONLY_OFF_TASK_NO_BONUS_SALVE_EI_NO_STACK_PASS");
 }
 static void defence()throws Exception{
  Player a=actor(),d=actor();a.playerLevel[0]=50;d.playerLevel[1]=50;d.playerLevel[6]=50;
  int[] weapons={1205,1333,-1};Bonus[] slots={Bonus.DEFENCE_STAB,Bonus.DEFENCE_SLASH,Bonus.DEFENCE_CRUSH};
  for(int i=0;i<weapons.length;i++){a.playerEquipment[3]=weapons[i];a.getCombatConfigs().setAttackStyle(0);Arrays.fill(d.playerBonus,0);d.playerBonus[slots[i].ordinal()]=100;close(invoke(MeleeCombatFormula.get(),"getDefenceRoll",new Class[]{Entity.class,Entity.class},a,d),58*164,"Target stab/slash/crush defence "+slots[i]);}
  Arrays.fill(d.playerBonus,0);a.playerEquipment[3]=-1;a.getCombatConfigs().setAttackStyle(0);
  for(CombatFormula formula:new CombatFormula[]{MeleeCombatFormula.get(),RangeCombatFormula.STANDARD,MagicCombatFormula.STANDARD}){
   double prior=1;for(int level=1;level<=110;level++){d.playerLevel[1]=level;double chance=formula.getAccuracy(a,d);ok(Double.isFinite(chance)&&chance>=0&&chance<=1&&chance<=prior+1e-12,"Defence increasing raised hit chance "+formula.getClass()+"/"+level);prior=chance;}
   for(double mult:new double[]{0,.5,1,2,Double.MAX_VALUE,Double.NaN}){double chance=formula.getAccuracy(a,d,1,mult);ok(Double.isFinite(chance)&&chance>=0&&chance<=1,"Defence scale invalid chance");}
  }
  d.playerLevel[1]=50;d.playerLevel[6]=1;double lowMagic=MagicCombatFormula.STANDARD.getAccuracy(a,d);d.playerLevel[6]=99;ok(MagicCombatFormula.STANDARD.getAccuracy(a,d)<lowMagic,"Magic level did not affect magic defence");
  for(Bonus bonus:new Bonus[]{Bonus.ATTACK_STAB,Bonus.ATTACK_SLASH,Bonus.ATTACK_CRUSH,Bonus.ATTACK_RANGED,Bonus.ATTACK_MAGIC})a.playerBonus[bonus.ordinal()]=-100;
  for(CombatFormula f:new CombatFormula[]{MeleeCombatFormula.get(),RangeCombatFormula.STANDARD,MagicCombatFormula.STANDARD})ok(f.getAccuracy(a,d)==0,"Negative attack roll not clamped");clear(a);clear(d);
  System.out.println("DEFENCE_STAB_SLASH_CRUSH_EQUIPMENT_330_LEVEL_CASES_ALL_THREE_ATTACK_TYPES_SCALE_GUARDS_MAGIC_LEVEL_AND_NEGATIVE_ATTACK_BONUSES_PASS");
 }
 static void xpAndSave()throws Exception{
  for(int index:new int[]{0,1,2}){Player p=actor();p.playerEquipment[3]=4151;p.getCombatConfigs().setAttackStyle(index);long atk=xp(p,0),def=xp(p,1),str=xp(p,2);HitDispatcher.getHitEntity(p,actor()).addCombatXP(CombatType.MELEE,10);ok(xp(p,0)-atk==(index==0?400:index==1?133:0)&&xp(p,1)-def==(index==2?400:index==1?133:0)&&xp(p,2)-str==(index==1?133:0),"Whip training XP "+index);clear(p);}
  Player p=actor();p.setLoginName("skills56qa");p.setDisplayName("Skills56qa");p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;p.playerEquipment[3]=4151;p.getCombatConfigs().setAttackStyle(2);p.getPA().addSkillXPMultiplied(40,0,true);p.getPA().addSkillXPMultiplied(40,1,true);int atk=p.playerXP[0],def=p.playerXP[1];ok(PlayerSave.saveGameInstant(p),"Save");Player q=actor();q.setLoginName("skills56qa");ok(PlayerSave.loadGame(q,"skills56qa","IsolatedAudit",true).toString().equals("SUCCESS")&&q.playerXP[0]==atk&&q.playerXP[1]==def&&q.playerEquipment[3]==4151,"Attack Defence/equipment save reload");PlayerHandler.players[1]=null;clear(p);clear(q);
  System.out.println("ATTACK_DEFENCE_ACCURATE_CONTROLLED_DEFENSIVE_WHIP_XP_AND_REAL_SAVE_RELOAD_PASS");
 }
 public static void main(String[]args)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());styles();masks();System.out.println("ATTACK_PLAYABLE_V56_PASS");probabilities();defence();xpAndSave();System.out.println("DEFENCE_PLAYABLE_V56_PASS");System.exit(0);}
}
