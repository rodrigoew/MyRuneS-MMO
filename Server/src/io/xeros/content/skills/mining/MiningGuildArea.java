package io.xeros.content.skills.mining;
import io.xeros.model.collisionmap.RegionProvider;
import java.util.*;

/** The members cave is the walkable component south of the two closed inner doors. */
public final class MiningGuildArea {
    private MiningGuildArea(){}
    private static Set<Integer> floor;
    private static int key(int x,int y){return x*20000+y;}
    public static void initialize(){
        if(floor!=null)return;
        RegionProvider region=RegionProvider.getGlobal();Set<Integer> next=new HashSet<>();ArrayDeque<int[]> queue=new ArrayDeque<>();queue.add(new int[]{3014,9719});next.add(key(3014,9719));
        int[][] directions={{0,1,1,6},{1,0,4,3},{0,-1,6,1},{-1,0,3,4}};
        while(!queue.isEmpty()){int[] tile=queue.removeFirst();for(int[] direction:directions){int x=tile[0]+direction[0],y=tile[1]+direction[1];
            if(x<2980||x>3070||y<9670||y>9765||next.contains(key(x,y)))continue;
            if(!region.canMove(tile[0],tile[1],0,direction[2])||!region.canMove(x,y,0,direction[3]))continue;
            next.add(key(x,y));queue.add(new int[]{x,y});
        }}
        if(next.contains(key(3019,9739))||!next.contains(key(3017,9724)))throw new IllegalStateException("Mining Guild cave boundaries do not match the installed map.");
        floor=Collections.unmodifiableSet(next);
    }
    public static int floorTiles(){initialize();return floor.size();}
    public static boolean contains(int x,int y,int height){
        if(height!=0||x<2980||x>3070||y<9670||y>9732)return false;initialize();
        if(floor.contains(key(x,y)))return true;
        // A mineral occupies a blocked tile; membership is determined by its accessible face.
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(floor.contains(key(x+dx,y+dy)))return true;
        return false;
    }
}
