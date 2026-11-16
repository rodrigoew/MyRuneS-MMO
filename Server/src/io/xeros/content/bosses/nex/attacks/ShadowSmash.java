package io.xeros.content.bosses.nex.attacks;
import io.xeros.content.bosses.nex.NexCombat;
import io.xeros.content.instances.InstancedArea;


import io.xeros.content.combat.Hitmark;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.util.List;

public class ShadowSmash {
    public ShadowSmash(List<Player> targets) {
        ShadowSmash(targets);
    }

    void ShadowSmash(List<Player> targets) {
        if(targets==null||targets.isEmpty())return;
        final InstancedArea arena=targets.get(0).getInstance();

        for (Player player : targets) {
            if (NexCombat.active(player,arena)) {
                player.gfx0(381);
                int dmg = Misc.random(50);
                if (player.protectingMagic()) {
                    dmg = (dmg / 2);
                }

                player.appendDamage(dmg, (dmg > 0 ? Hitmark.HIT : Hitmark.MISS));
            }
        }
    }
}