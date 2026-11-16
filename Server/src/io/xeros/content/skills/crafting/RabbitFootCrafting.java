package io.xeros.content.skills.crafting;

import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;

/** Published two-tick stringing recipe; all materials commit together. */
public final class RabbitFootCrafting {
    private RabbitFootCrafting(){}
    public static boolean itemOnItem(Player p,int first,int second){
        if(!((first==1759&&second==10134)||(first==10134&&second==1759)))return false;
        if(!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||p.getInterfaceEvent().isActive()
                ||p.getBankPin().requiresUnlock()||p.isBanking||p.isShopping)return true;
        ProductionBatch.start(p,12,37,4,1,884,2,new int[][]{{1759,1},{10134,1}},new int[][]{{10132,1}},
            ()->p.sendMessage("You string the rabbit foot onto the wool."),
            ()->p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!p.getInterfaceEvent().isActive()
                &&!p.getBankPin().requiresUnlock()&&!p.isBanking&&!p.isShopping);
        return true;
    }
}
