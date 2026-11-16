package io.xeros.content.combat.magic;
import io.xeros.model.entity.player.Player;
/** Base cast XP is separate from damage XP and is awarded once, including a splash. */
public final class CombatCastExperience {
 private CombatCastExperience(){}
 public static double base(Player p,boolean hit,boolean pvp){
  int i=p.getSpellId();if(i<0||i>=CombatSpellData.MAGIC_SPELLS.length||p.usingSpecial)return 0;
  switch(CombatSpellData.MAGIC_SPELLS[i][0]){
   case 1152:return 5.5;case 1154:return 7.5;case 1156:return 9.5;case 1158:return 11.5;
   case 1160:return 13.5;case 1163:return 16.5;case 1166:return 19.5;case 1169:return 22.5;
   case 1172:return 25.5;case 1175:return 28.5;case 1177:return 31.5;case 1181:return 34.5;
   case 1183:return 36;case 1185:return 37.5;case 1188:return 40;case 1189:return 42.5;
   case 23619:return 44.5;case 23633:return 46.5;case 23603:return 48.2;case 23583:return 50.5;
   case 1153:return 13;case 1157:return 21;case 1161:return 29;
   case 1542:return 76;case 1543:return 83;case 1562:return 90;
   case 1572:return 30;case 1582:return 60;case 1592:return pvp&&!hit?70:89;
   case 1171:return 24.5;case 1539:case 12037:return 30;
   case 1190:case 1191:case 1192:return 35;
   case 12445:return hit?95:83.6;
   case 12939:return 30;case 12987:return 31;case 12901:return 33;case 12861:return 34;
   case 12963:return 36;case 13011:return 37;case 12919:return 39;case 12881:return 40;
   case 12951:return 42;case 12999:return 43;case 12911:return 45;case 12871:return 46;
   case 12975:return 48;case 13023:return 49;case 12929:return 51;case 12891:return 52;
   default:return 0;
  }
 }
}
