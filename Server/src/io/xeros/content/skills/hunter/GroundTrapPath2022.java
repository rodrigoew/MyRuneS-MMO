package io.xeros.content.skills.hunter;

import io.xeros.model.Direction;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Position;
import java.util.*;

/** Short, clipped approaches can go around a player or a rock instead of stalling against it. */
final class GroundTrapPath2022 {
    private GroundTrapPath2022() { }
    private static long key(int x,int y) { return ((long)x<<32)|(y&0xffffffffL); }
    private static final class Step {
        final int x,y,first,depth;
        Step(int x,int y,int first,int depth){this.x=x;this.y=y;this.first=first;this.depth=depth;}
    }
    static Direction next(NPC npc,int targetX,int targetY) {
        if(npc.absX==targetX && npc.absY==targetY) return Direction.NONE;
        int startX=npc.absX,startY=npc.absY,height=npc.heightLevel;
        ArrayDeque<Step> queue=new ArrayDeque<>();Set<Long> visited=new HashSet<>();
        queue.add(new Step(startX,startY,-1,0));visited.add(key(startX,startY));
        while(!queue.isEmpty()) {
            Step step=queue.removeFirst();
            if(step.depth>=10)continue;
            // Prefer a direct step without giving up a legal detour when it is blocked.
            List<Integer> directions=new ArrayList<>(8);for(int direction=0;direction<8;direction++)directions.add(direction);
            directions.sort(Comparator.<Integer>comparingInt(direction->{Direction d=Direction.get(direction);
                return Math.max(Math.abs(step.x+d.x()-targetX),Math.abs(step.y+d.y()-targetY));})
                    .thenComparingInt(direction->{Direction d=Direction.get(direction);
                        return Math.abs(step.x+d.x()-targetX)+Math.abs(step.y+d.y()-targetY);}));
            for(int direction:directions) {
                Direction move=Direction.get(direction);int x=step.x+move.x(),y=step.y+move.y();
                if(Math.abs(x-startX)>5||Math.abs(y-startY)>5||visited.contains(key(x,y)))continue;
                if(!legal(npc,new Position(step.x,step.y,height),direction))continue;
                int first=step.first<0?direction:step.first;
                if(x==targetX&&y==targetY)return Direction.get(first);
                visited.add(key(x,y));queue.addLast(new Step(x,y,first,step.depth+1));
            }
        }
        return Direction.NONE;
    }
    private static boolean legal(NPC npc,Position from,int direction) {
        Direction move=Direction.get(direction);int x=from.getX()+move.x(),y=from.getY()+move.y(),height=from.getHeight();
        RegionProvider provider=npc.getRegionProvider();
        if(provider.get(x,y).getTerrainFlags(x,y,height)<0)return false;
        Boolean flight=GroundTrapFlight2022.canStep(npc,from,move);
        if(flight!=null)return flight;
        return provider.canMove(from.getX(),from.getY(),height,direction,true)
                && provider.canMove(x,y,height,7-direction,false);
    }
}
