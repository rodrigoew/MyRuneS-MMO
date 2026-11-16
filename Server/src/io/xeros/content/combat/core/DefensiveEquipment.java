package io.xeros.content.combat.core;
import io.xeros.Server;import io.xeros.content.combat.Hitmark;import io.xeros.model.*;import io.xeros.model.entity.*;import io.xeros.model.entity.player.*;import io.xeros.content.combat.weapon.*;import java.util.function.IntUnaryOperator;
/** Defensive effects apply to NPC combat hits; poison, recoil and self-inflicted damage are excluded. */
public final class DefensiveEquipment {
 private DefensiveEquipment(){}
 public static boolean bulwark(Player p){return p.playerEquipment[3]==21015;}
 public static boolean blocking(Player p){return bulwark(p)&&p.getCombatConfigs().getAttackStyle()!=0;}
 public static void changed(Player p,boolean wasBlocking){if(wasBlocking||blocking(p))p.bulwarkReadyTick=Server.getTickCount()+8;}
 public static int bulwarkStrength(Player p){if(!bulwark(p)||blocking(p))return 0;double avg=(p.getBonus(Bonus.DEFENCE_STAB)+p.getBonus(Bonus.DEFENCE_SLASH)+p.getBonus(Bonus.DEFENCE_CRUSH)+p.getBonus(Bonus.DEFENCE_RANGED))/4.0;return Math.max(0,(int)Math.floor((avg-314)/3));}
 public static int reduce(Player p,Entity source,int damage,Hitmark mark){return reduce(p,source,damage,mark,b->java.util.concurrent.ThreadLocalRandom.current().nextInt(b));}
 public static int reduce(Player p,Entity source,int damage,Hitmark mark,IntUnaryOperator roll){
  if(damage<=0||source==null||!source.isNPC()||(mark!=Hitmark.HIT&&mark!=Hitmark.MISS))return damage;
  CombatType type=source.asNPC().getAttackType();if(type==null||type==CombatType.SPECIAL)return damage;
  double reduction=0;
  if(type!=CombatType.DRAGON_FIRE&&p.playerEquipment[0]==22326&&p.playerEquipment[4]==22327&&p.playerEquipment[7]==22328){CombatStyle style=type==CombatType.RANGE?CombatStyle.RANGE:type==CombatType.MAGE?CombatStyle.MAGIC:io.xeros.content.combat.CombatConfigs.getCombatStyle(source);Bonus defence=style==CombatStyle.STAB?Bonus.DEFENCE_STAB:style==CombatStyle.SLASH?Bonus.DEFENCE_SLASH:style==CombatStyle.RANGE?Bonus.DEFENCE_RANGED:style==CombatStyle.MAGIC?Bonus.DEFENCE_MAGIC:Bonus.DEFENCE_CRUSH;reduction+=Math.max(0,p.getBonus(defence))/3000.0;}
  if(p.playerEquipment[5]==12817&&roll.applyAsInt(100)<70){reduction+=.25;p.gfx0(321);}
  int result=Math.max(0,(int)Math.floor(damage*(1-Math.min(1,reduction))));
  if(blocking(p)&&Server.getTickCount()>=p.bulwarkReadyTick)result=(int)Math.floor(result*.8);
  return result;
 }
 public static int prayerLoss(Player p,int amount){return Math.max(0,p.playerEquipment[5]==12821?(amount+1)/2:amount);}
}
