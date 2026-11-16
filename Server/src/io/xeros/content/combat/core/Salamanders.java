package io.xeros.content.combat.core;
import io.xeros.model.entity.player.Player;import io.xeros.model.CombatType;
/** 2022 lizards: Harralander/Guam/Marrentill/Tarromin ammunition and three combat styles. */
public final class Salamanders {
 private Salamanders(){}public static boolean weapon(int id){return id>=10146&&id<=10149;}
 public static int ammo(int id){switch(id){case 10149:return 10142;case 10146:return 10143;case 10147:return 10144;case 10148:return 10145;default:return -1;}}
 public static int maxMagic(Player p){int id=p.playerEquipment[3],bonus=id==10149?49:id==10146?59:id==10147?77:92;return Math.max(0,(p.playerLevel[6]*(bonus+64)+320)/640);}
 public static Boolean configure(Player p){int id=p.playerEquipment[3];if(!weapon(id)||p.usingClickCast)return null;if(p.playerEquipment[13]!=ammo(id)||p.playerEquipmentN[13]<=0){p.sendMessage("Equipe o tar compativel com esta salamander.");return false;}int style=p.getCombatConfigs().getAttackStyle();p.usingMagic=style==2;p.usingBow=style==1;p.usingMelee=style==0;p.usingArrows=style==1;p.autocasting=false;p.autocastId=-1;p.setSpellId(style==2?101:-1);return true;}
 public static void consume(Player p){if(weapon(p.playerEquipment[3])&&!p.usingClickCast&&p.attacking.getCombatType()!=CombatType.RANGE){int n=p.playerEquipmentN[13]-1;p.getItems().setEquipment(n>0?p.playerEquipment[13]:-1,Math.max(0,n),13,false);}}
}
