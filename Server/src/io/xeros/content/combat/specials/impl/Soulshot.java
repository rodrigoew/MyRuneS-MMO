package io.xeros.content.combat.specials.impl;
import io.xeros.content.combat.*;import io.xeros.content.combat.specials.Special;import io.xeros.model.entity.*;import io.xeros.model.entity.player.*;import io.xeros.model.entity.npc.stats.*;import io.xeros.model.definitions.ItemStats;
/** Seercull Soulshot / magic longbow Powershot: guaranteed accuracy, ammunition-only maximum. */
public final class Soulshot extends Special {
 public Soulshot(){super(10,1,1,new int[]{6724,859});}
 public static int max(Player p){int ammo=Math.max(0,ItemStats.forId(p.playerEquipment[13]).getEquipment().getRstr());return Math.max(0,((p.playerLevel[4]+10)*(ammo+64)+320)/640);}
 public void activate(Player p,Entity target,Damage hit){p.startAnimation(426);p.attacking.fireRangeProjectile(target);}
 public void hit(Player p,Entity target,Damage hit){if(!hit.isSuccess()||!hit.getEquipment().isPresent()||hit.getEquipment().get()[3]!=6724)return;int drain=Math.min(hit.getAmount(),target.getHealth().getCurrentHealth());if(target.isNPC()){NpcCombatDefinition d=target.asNPC().getCombatDefinition(),base=NpcCombatDefinition.definitions.get(target.asNPC().getNpcId());if(d!=null&&(base==null||d.getLevel(NpcCombatSkill.MAGIC)>=base.getLevel(NpcCombatSkill.MAGIC)))d.setLevel(NpcCombatSkill.MAGIC,Math.max(0,d.getLevel(NpcCombatSkill.MAGIC)-drain));}else{Player q=target.asPlayer();if(q.playerLevel[6]>=q.getPA().getLevelForXP(q.playerXP[6])){q.playerLevel[6]=Math.max(0,q.playerLevel[6]-drain);q.getPA().refreshSkill(6);}}}
}
