package io.xeros.content.bosses.nex.attacks;
import io.xeros.content.bosses.nex.NexCombat;
import io.xeros.content.instances.InstancedArea;


import io.xeros.content.combat.Hitmark;
import io.xeros.model.entity.EntityReference;
import io.xeros.model.entity.player.ClientGameTimer;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class IceBarrage {
    public IceBarrage(Player player, List<Player> players) {
        IceBarrage(player, players);
    }

    void IceBarrage(Player player, List<Player> players) {
        if(player==null||players==null)return;
        final InstancedArea arena=player.getInstance();
        if(!NexCombat.active(player,arena))return;
        for (Player possibleTargets:
                players) {
            if(NexCombat.active(possibleTargets,arena) && player.getPosition().getAbsDistance(possibleTargets.getPosition()) <= 3) {
                possibleTargets.gfx0(369);
                int dam;
                if(possibleTargets.protectingMagic())
                    dam = Misc.random(15);
                else
                    dam = Misc.random(33);
                possibleTargets.appendDamage(dam, (dam > 0 ? Hitmark.HIT : Hitmark.MISS));
                if (possibleTargets.isFreezable()) {
                    int delay = Misc.random(15,30);
                    possibleTargets.frozenBy = EntityReference.getReference(player);
                    possibleTargets.freezeDelay = delay;
                    possibleTargets.freezeTimer = delay;
                    possibleTargets.resetWalkingQueue();
                    possibleTargets.sendMessage("You have been frozen.");
                    possibleTargets.getPA().sendGameTimer(ClientGameTimer.FREEZE, TimeUnit.MILLISECONDS, 600 * delay);
                }
            }

        }
    }
}
