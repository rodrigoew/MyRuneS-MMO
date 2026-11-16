package io.xeros.content.bosses.nex.attacks;
import io.xeros.content.bosses.nex.NexCombat;
import io.xeros.content.instances.InstancedArea;


import io.xeros.content.combat.Hitmark;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

public class BloodSacrifice {
    public BloodSacrifice(NPC npc, Player target) {
        BloodSacrifice(npc, target);
    }

    void BloodSacrifice(NPC npc, Player target) {
        if(target==null)return;
        final InstancedArea arena=target.getInstance();
        CycleEventHandler.getSingleton().addEvent(new Object(), new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                if(!NexCombat.active(target,arena)){container.stop();return;}
                if (!Boundary.isIn(target, Boundary.Nex)) {
                    container.stop();return;
                }
                if (container.getTotalTicks() == 1) {
                    target.gfx100(374);
                    target.sendMessage("You have been chosen as a sacrifice, RUN!");
                }
                if(container.getTotalTicks() == 7 && target.getPosition().getAbsDistance(npc.getPosition()) < 7) {
                    target.playerLevel[5] = Math.max(0,(int)Math.floor(target.playerLevel[5]*0.67));
                    target.getPA().refreshSkill(5);
                    int damage = Misc.random(30, 50);
                    if (target.protectingMagic()) {
                        damage = (damage / 2);
                    }
                    npc.appendHeal(damage, Hitmark.HEAL_PURPLE);
                    target.appendDamage(damage, (damage > 0 ? Hitmark.HIT : Hitmark.MISS));
                }
                if (container.getTotalTicks() > 7) {
                    container.stop();
                }
            }
        }, 1);
    }
}
