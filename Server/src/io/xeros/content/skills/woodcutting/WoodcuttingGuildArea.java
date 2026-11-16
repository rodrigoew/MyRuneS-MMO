package io.xeros.content.skills.woodcutting;
import io.xeros.model.collisionmap.RegionProvider;
import java.util.*;

/** Connected floor inside the installed Guild's fence and both Redwood platforms. */
public final class WoodcuttingGuildArea {
    private WoodcuttingGuildArea(){}
    private static List<Set<Integer>> floors;
    private static int key(int x,int y){return x*20000+y;}
    public static void initialize(){
        if(floors!=null)return;
        List<Set<Integer>> result=new ArrayList<>();RegionProvider regions=RegionProvider.getGlobal();
        int[][] directions={{0,1,1,6},{1,0,4,3},{0,-1,6,1},{-1,0,3,4}};
        for(int h=0;h<=2;h++){
            Set<Integer> seen=new HashSet<>();ArrayDeque<int[]> queue=new ArrayDeque<>();
            int sx=h==0?1617:1567,sy=h==0?3492:3483;queue.add(new int[]{sx,sy});seen.add(key(sx,sy));
            while(!queue.isEmpty()){int[] p=queue.removeFirst();for(int[] d:directions){
                int x=p[0]+d[0],y=p[1]+d[1];
                if(x<1540||x>1670||y<3450||y>3550||seen.contains(key(x,y)))continue;
                if(!regions.canMove(p[0],p[1],h,d[2])||!regions.canMove(x,y,h,d[3]))continue;
                seen.add(key(x,y));queue.add(new int[]{x,y});
            }}
            if(seen.contains(key(1661,3498))||seen.size()!=(h==0?1646:60))
                throw new IllegalStateException("Woodcutting Guild floor does not match installed map, height="+h);
            result.add(Collections.unmodifiableSet(seen));
        }floors=Collections.unmodifiableList(result);
    }
    public static boolean contains(int x,int y,int height){
        if(height<0||height>2||x<1563||x>1657||y<3472||y>3518)return false;
        initialize();return floors.get(height).contains(key(x,y));
    }
    public static int tiles(int height){initialize();return height<0||height>2?0:floors.get(height).size();}
}
