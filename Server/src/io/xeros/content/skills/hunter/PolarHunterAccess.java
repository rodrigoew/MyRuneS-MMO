package io.xeros.content.skills.hunter;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.entity.player.*;

/** Exact original stair pairs. Landings are outside the two-by-two blocked stair objects. */
public final class PolarHunterAccess {
    private static final int[][] STAIRS={
        {19690,2715,3799,0,2715,3802,1},{19691,2715,3800,1,2715,3798,0},
        {19690,2726,3802,0,2726,3805,1},{19691,2726,3803,1,2726,3801,0}
    };
    private PolarHunterAccess(){}
    private static boolean live(Player p,WorldObject o){return p.heightLevel==o.height&&p.getRegionProvider()==RegionProvider.getGlobal()
        &&p.getInstance()==null&&p.getRegionProvider().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,o.height).filter(o::equals).isPresent();}
    private static boolean near(Player p,WorldObject o){Position size=o.getObjectSize();int dx=Math.max(o.x-p.absX,Math.max(0,p.absX-o.x-Math.max(1,size.getX())+1));int dy=Math.max(o.y-p.absY,Math.max(0,p.absY-o.y-Math.max(1,size.getY())+1));return Math.max(dx,dy)<=1;}
    public static boolean click(Player p,WorldObject o,int option){
        if(o==null)return false;
        for(int[] row:STAIRS)if(o.id==row[0]&&o.x==row[1]&&o.y==row[2]&&o.height==row[3]){
            if(option!=1||!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||!live(p,o)||!near(p,o))return true;
            ProductionBatch.start(p,16,1,0,1,row[6]>row[3]?828:827,2,new int[0][],new int[0][],
                ()->p.getPA().movePlayer(row[4],row[5],row[6]),
                ()->p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&live(p,o)
                    &&(RegionProvider.getGlobal().getClipping(row[4],row[5],row[6])&0x1280100)==0);
            return true;
        }
        return false;
    }
}
