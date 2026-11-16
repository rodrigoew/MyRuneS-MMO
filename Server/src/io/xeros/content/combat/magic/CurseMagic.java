package io.xeros.content.combat.magic;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.stats.NpcCombatSkill;
import io.xeros.model.entity.npc.stats.NpcCombatDefinition;

/** Stat curses share their target check and successful-hit effect in PvM and PvP. */
public final class CurseMagic {
 private CurseMagic(){}
 private static int id(Player p){int i=p.getSpellId();return i>=0&&i<CombatSpellData.MAGIC_SPELLS.length?CombatSpellData.MAGIC_SPELLS[i][0]:-1;}
 private static int skill(int id){switch(id){case 1153:case 1562:return 0;case 1157:case 1543:return 2;case 1161:case 1542:return 1;default:return -1;}}
 private static NpcCombatSkill npcSkill(int skill){return skill==0?NpcCombatSkill.ATTACK:skill==1?NpcCombatSkill.DEFENCE:NpcCombatSkill.STRENGTH;}
 private static int current(Entity target,int skill){return target.isPlayer()?target.asPlayer().playerLevel[skill]:target.asNPC().getCombatDefinition().getLevel(npcSkill(skill));}
 private static int base(Entity target,int skill){
  if(target.isPlayer())return target.asPlayer().getLevelForXP(target.asPlayer().playerXP[skill]);
  NPC n=target.asNPC();NpcCombatDefinition def=NpcCombatDefinition.definitions.get(n.getNpcId());return def==null?1:def.getLevel(npcSkill(skill));
 }
 public static boolean canCast(Player p,Entity target){int s=skill(id(p));if(s<0||current(target,s)>=base(target,s))return true;p.sendMessage("That target's affected stat is already reduced.");return false;}
 public static void apply(Player p,Entity target){int id=id(p),s=skill(id);if(s<0||current(target,s)<base(target,s))return;double percent=id==1153||id==1157||id==1161?0.05:0.10;int drain=(int)Math.floor(base(target,s)*percent*MagicTomes.curseMultiplier(p));int value=Math.max(0,current(target,s)-drain);
  if(target.isPlayer()){Player q=target.asPlayer();q.playerLevel[s]=value;q.getPA().refreshSkill(s);q.sendMessage("Your "+(s==0?"attack":s==1?"defence":"strength")+" level has been reduced!");}
  else target.asNPC().getCombatDefinition().setLevel(npcSkill(s),value);
 }
}
