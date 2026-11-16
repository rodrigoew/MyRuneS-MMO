package io.xeros.content.skills.hunter;

import io.xeros.content.skills.hunter.trap.Trap;
import io.xeros.content.skills.hunter.trap.TrapProcessor;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** Shared Hunter limits and the original public API, originally authored by Stand Up. */
public final class Hunter {
    public static final Map<Player,TrapProcessor> GLOBAL_TRAPS=new HashMap<>();
    private Hunter() { }
    public static int getMaximumTraps(Player player) {
        int level=Math.max(1,Math.min(99,player.playerLevel[21]));
        return level/20+1+(player.getPosition().inWild()?1:0);
    }
    public static int activeTrapCount(Player player) {
        TrapProcessor ground=GLOBAL_TRAPS.get(player);
        return (ground==null?0:ground.getTraps().size())+NetTrapHunting.activeCount(player)+PitfallHunting.activeCount(player)+RabbitSnaring2022.activeCount(player);
    }
    public static boolean lay(Player player,Trap trap) { return GroundTrapHunting.lay(player,trap); }
    public static void abandon(Player player,Trap trap,boolean logout) {
        if(logout) GroundTrapHunting.logout(player); else GroundTrapHunting.abandon(player,trap);
    }
    public static boolean resetTrap(Player player,GlobalObject object) { return GroundTrapHunting.interact(player,object,2); }
    public static boolean pickup(Player player,GlobalObject object) {
        Optional<Trap> trap=getTrap(player,object);
        return trap.isPresent() && trap.get().getState()!=Trap.TrapState.CAUGHT && GroundTrapHunting.interact(player,object,1);
    }
    public static boolean claim(Player player,GlobalObject object) {
        Optional<Trap> trap=getTrap(player,object);
        return trap.isPresent() && trap.get().getState()==Trap.TrapState.CAUGHT && GroundTrapHunting.interact(player,object,1);
    }
    public static Optional<Trap> getTrap(Player player,GlobalObject object) { return GroundTrapHunting.find(player,object); }
}
