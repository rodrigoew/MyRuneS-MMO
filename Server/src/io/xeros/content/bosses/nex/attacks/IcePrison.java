package io.xeros.content.bosses.nex.attacks;
import io.xeros.content.bosses.nex.NexCombat;
import io.xeros.content.instances.InstancedArea;


import io.xeros.Server;
import io.xeros.content.combat.Hitmark;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;

import java.util.List;

public class IcePrison {
    public IcePrison(List<Player> targets) {
        IcePrison(targets);
    }

    void IcePrison(List<Player> targets) {
        if(targets==null||targets.isEmpty())return;
        final InstancedArea arena=targets.get(0).getInstance();

        Player target = targets.get(Misc.random(targets.size() - 1));
        if(!NexCombat.active(target,arena))return;
        Boundary iceBounds = new Boundary(target.getX() - 1, target.absY - 2, target.getX() + 2, target.getY() + 1);

        for(int y = -3; y < 4; y++) {
            addIce(new GlobalObject(42943, target.absX + -3, target.absY + y, target.getHeight(), 0, 10, 6),arena);
            addIce(new GlobalObject(42943, target.absX + 3, target.absY + y, target.getHeight(), 0, 10, 6),arena);
            addIce(new GlobalObject(42943, target.absX + y, target.absY + -3, target.getHeight(), 0, 10, 6),arena);
            addIce(new GlobalObject(42943, target.absX + y, target.absY + 3, target.getHeight(), 0, 10, 6),arena);

        }

        CycleEventHandler.getSingleton().addEvent(new Object(), new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                if(container.getTotalTicks() > 2) {
                    for (Player player :
                            targets) {
                        if (NexCombat.active(player,arena) && iceBounds.in(player)) {
                            player.appendDamage(Misc.random(20, 75), Hitmark.HIT);
                        }
                    }
                    container.stop();
                }
            }
        }, 1);

    }
    private static void addIce(GlobalObject object,InstancedArea arena){object.setInstance(arena);Server.getGlobalObjects().add(object);}
}