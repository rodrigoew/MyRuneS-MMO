package io.xeros.content.skills.crafting;

import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.Player;

/** Exact ladder pairs; generic ladder IDs elsewhere retain their own destination handlers. */
public final class OrbChargingAccess {
    private static final int[][] LADDERS={
        {16680,3088,3571,3088,9971,0}, {17385,3088,9971,3088,3571,1},
        {17384,2842,3424,2842,9824,0}, {17385,2842,9824,2842,3424,1}
    };
    private OrbChargingAccess(){}
    private static boolean live(Player p,WorldObject target){return p.heightLevel==0&&target.height==0&&p.getRegionProvider().get(target.x,target.y).getWorldObject(target.id,target.x,target.y,0).filter(target::equals).isPresent();}
    public static boolean click(Player p,WorldObject target,int option){
        if(option!=1||target==null)return false;
        for(int[] row:LADDERS)if(target.id==row[0]&&target.x==row[1]&&target.y==row[2]){
            if(!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||!live(p,target)
                ||Math.max(Math.abs(p.absX-target.x),Math.abs(p.absY-target.y))>1)return true;
            ProductionBatch.start(p,16,1,0,1,row[5]==1?828:827,2,new int[0][],new int[0][],()->{
                if(live(p,target))p.getPA().movePlayer(row[3],row[4],0);
            });return true;
        }
        return false;
    }
}
