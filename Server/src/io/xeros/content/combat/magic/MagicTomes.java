package io.xeros.content.combat.magic;

import io.xeros.model.entity.player.Player;

/** Frozen 2022 elemental tome rules. Queries never consume charges. */
public final class MagicTomes {
    private MagicTomes() {}
    private static int spell(Player p){int i=p.getSpellId();return i>=0&&i<CombatSpellData.MAGIC_SPELLS.length&&!p.usingSpecial?CombatSpellData.MAGIC_SPELLS[i][0]:-1;}
    public static boolean fireSpell(int id){switch(id){case 1158:case 1169:case 1181:case 1189:case 23583:return true;default:return false;}}
    public static boolean waterSpell(int id){switch(id){case 1154:case 1163:case 1175:case 1185:case 23633:return true;default:return false;}}
    public static boolean curseSpell(int id){switch(id){case 1153:case 1157:case 1161:case 1542:case 1543:case 1562:return true;default:return false;}}
    public static boolean bindingSpell(int id){return id==1572||id==1582||id==1592;}
    public static boolean chargedFire(Player p){return p.playerEquipment[Player.playerShield]==20714&&p.getTomeOfFire().hasCharges();}
    public static boolean chargedWater(Player p){return p.playerEquipment[Player.playerShield]==25574&&p.getTomeOfWater().hasCharges();}
    public static double damageMultiplier(Player p){int id=spell(p);return chargedFire(p)&&fireSpell(id)?1.5:chargedWater(p)&&waterSpell(id)?1.2:1.0;}
    public static double accuracyMultiplier(Player p){int id=spell(p);return chargedWater(p)&&(waterSpell(id)||curseSpell(id)||bindingSpell(id))?1.2:1.0;}
    public static double curseMultiplier(Player p){return chargedWater(p)&&curseSpell(spell(p))?1.5:1.0;}
    /** Called once after all damage/accuracy rolls, before clearing the casting spell. Includes splashes. */
    public static void commitCast(Player p){int id=spell(p);if(chargedFire(p)&&fireSpell(id))p.getTomeOfFire().consumeCharge();else if(chargedWater(p)&&(waterSpell(id)||curseSpell(id)||bindingSpell(id)))p.getTomeOfWater().consumeCharge();}
}
