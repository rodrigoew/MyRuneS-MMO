package io.xeros.content.skills.herblore;
import io.xeros.model.entity.player.Player;import io.xeros.content.skills.agility.RunEnergy2022;
/** 2022 restoration amounts. The wrench changes Prayer's percentage only, never other skills. */
public final class PotionRestoration2022 {
 private PotionRestoration2022(){}
 public static boolean holyWrench(Player p){int cape=RunEnergy2022.inventoryId(p.playerEquipment[Player.playerCape]);return (p.playerEquipment[Player.playerRing]==13202||p.playerEquipment[Player.playerRing]==25252||p.playerEquipment[Player.playerRing]==26764)||cape==9759||cape==9760||cape==13280||p.getItems().getInventoryCount(6714)>0||p.getItems().getInventoryCount(9759)>0||p.getItems().getInventoryCount(9760)>0||p.getItems().getInventoryCount(13280)>0;}
 public static int prayerAmount(Player p,int percent,int flat){return flat+p.getLevelForXP(p.playerXP[5])*(percent+(holyWrench(p)?2:0))/100;}
 public static void restorePrayer(Player p,int percent,int flat){restore(p,5,prayerAmount(p,percent,flat));}
 public static void restore(Player p,int skill,int amount){int base=p.getLevelForXP(p.playerXP[skill]);if(p.playerLevel[skill]>=base)return;p.playerLevel[skill]=Math.min(base,p.playerLevel[skill]+amount);p.getPA().refreshSkill(skill);}
 public static void superRestore(Player p,boolean sanfew){int percent=sanfew?30:25,flat=sanfew?4:8;for(int skill=0;skill<p.playerLevel.length;skill++){if(skill==3)continue;int amount=skill==5?prayerAmount(p,percent,flat):flat+p.getLevelForXP(p.playerXP[skill])*percent/100;restore(p,skill,amount);}}
 public static void boost(Player p,int skill,int percent,int flat){int increase=flat+p.getLevelForXP(p.playerXP[skill])*percent/100;int cap=p.getLevelForXP(p.playerXP[skill])+increase;if(p.playerLevel[skill]>=cap)return;p.playerLevel[skill]=Math.min(cap,p.playerLevel[skill]+increase);p.getPA().refreshSkill(skill);}
}
