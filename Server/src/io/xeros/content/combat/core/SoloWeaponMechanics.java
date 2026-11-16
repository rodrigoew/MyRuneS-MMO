package io.xeros.content.combat.core;
import io.xeros.model.entity.player.Player;
/** Equipment rules for the 2022 solo baseline. Pure functions also support exhaustive checks. */
public final class SoloWeaponMechanics {
 private SoloWeaponMechanics(){}
 public static boolean fang(Player p){int id=p.playerEquipment[3];return id==26219||id==27246;}
 public static double fangAccuracy(double attack,double defence){double a=Math.max(0,Math.floor(attack)),d=Math.max(0,Math.floor(defence));double n=Math.min(d,a-1)+1;if(n<=0)return 0;return Math.max(0,Math.min(1,(n-n*(n+1)*(2*n+1)/(6*(a+1)*(a+1)))/(d+1)));}
 public static int fangMinimum(int max){return (int)Math.floor(Math.max(0,max)*.15);}
 public static int fangMaximum(int max,boolean special){return Math.max(0,max)-(special?0:fangMinimum(max));}
 public static int scytheHits(int size){return Math.max(1,Math.min(3,size));}
 public static boolean crystalBow(Player p){int id=p.playerEquipment[3];return id==25867||id==25865||(id>=4212&&id<=4223);}
 public static double crystalBonus(Player p,boolean accuracy){if(!crystalBow(p))return 1;double bonus=0;if(p.playerEquipment[0]==23971)bonus+=.025;if(p.playerEquipment[4]==23975)bonus+=.075;if(p.playerEquipment[7]==23979)bonus+=.05;return 1+bonus*(accuracy?2:1);}
}
