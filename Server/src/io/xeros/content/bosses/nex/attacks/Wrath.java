package io.xeros.content.bosses.nex.attacks;
import io.xeros.content.bosses.nex.NexCombat;
import io.xeros.content.instances.InstancedArea;


import io.xeros.content.combat.Hitmark;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.util.List;

public class Wrath {
    public Wrath(NPC npc, List<Player> targets) {
        Wrath(npc, targets);
    }

    void Wrath(NPC npc, List<Player> targets) {
        if(targets==null||targets.isEmpty())return;
        final InstancedArea arena=targets.get(0).getInstance();

        CycleEventHandler.getSingleton().addEvent(new Object(), new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                if (container.getTotalTicks() > 5) {
                    for (Player player : targets) {
                        if(NexCombat.active(player,arena) && player.getPosition().getAbsDistance(npc.getPosition()) < 5) {
                            player.appendDamage(Misc.random(10, 40), Hitmark.HIT);
                        }
                    }
                    container.stop();
                }
            }
        }, 1);
    }


}
