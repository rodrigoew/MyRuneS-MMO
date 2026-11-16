package io.xeros.content.combat.magic;
import io.xeros.content.combat.CombatItems;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.npc.NPC;
/** Magic uses the 15% imbued task bonus; only imbued Salve amulets affect magic. */
public final class MagicTargetEquipment {
 private MagicTargetEquipment(){}
 public static double salve(Player p,NPC n){if(!n.isUndead())return 0;switch(p.playerEquipment[Player.playerAmulet]){case 12018:case 25278:case 26782:return 0.20;case 12017:case 25250:case 26763:return 0.15;default:return 0;}}
 public static double bonus(Player p,NPC n){
  double salve=salve(p,n);if(salve>0)return salve;
  int hat=p.playerEquipment[Player.playerHat];
  boolean imbued=CombatItems.SLAYER_HELMETS_IMBUED.contains(hat)||hat==25177||hat==26674||hat>=11774&&hat<=11784||hat>=25266&&hat<=25276||hat>=26771&&hat<=26781;
  return imbued&&p.getSlayer().isTaskNpc(n)?0.15:0;
 }
}
