package io.xeros.content.skills.hunter;

import io.xeros.model.Direction;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Position;

/** Hunter birds can cross water and rocks; ground creatures and player collision keep their usual rules. */
public final class GroundTrapFlight2022 {
    private GroundTrapFlight2022() { }

    /** Null leaves the ordinary NPC pathfinder in charge. */
    public static Boolean canStep(NPC npc, Position from, Direction direction) {
        GroundTrapSpecies2022 species=GroundTrapSpecies2022.npc(npc.getNpcId());
        if(species==null || !species.bird() || npc.heightLevel!=0 || from.getHeight()!=0 || npc.getInstance()!=null
                || npc.getRegionProvider()!=RegionProvider.getGlobal()) return null;
        int x=from.getX()+direction.x(), y=from.getY()+direction.y();
        int flags=npc.getRegionProvider().get(x,y).getTerrainFlags(x,y,from.getHeight());
        return flags>=0 && (flags&4)==0;
    }
}
