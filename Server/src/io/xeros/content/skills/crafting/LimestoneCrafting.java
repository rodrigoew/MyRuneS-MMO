package io.xeros.content.skills.crafting;

import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

/** Instant chisel action. Failure interpolation is an estimate between documented 12/40 endpoints. */
public final class LimestoneCrafting {
    private LimestoneCrafting(){}
    public static int failureThreshold(int level){return Math.max(0,Math.min(28,40-level));}
    public static boolean itemOnItem(Player p,int a,int b){
        if(!((a==3211&&b==1755)||(b==3211&&a==1755)))return false;
        chisel(p,Misc::trueRand);return true;
    }
    public static boolean chisel(Player p,IntUnaryOperator random){
        if(p.isDead||p.getLock().cannotInteract(p)||p.isBanking||p.isShopping)return false;
        if(p.playerLevel[12]<12){p.sendMessage("You need Crafting level 12 to shape limestone.");return false;}
        boolean failed=random.applyAsInt(84)<failureThreshold(p.playerLevel[12]);
        if(!ProductionBatch.exchange(p,new int[][]{{1755,0},{3211,1}},new int[][]{{failed?968:3420,1}}))return false;
        p.interruptActions();p.getPA().stopSkilling();p.startAnimation(886);
        p.getPA().addSkillXPMultiplied(failed?1.5:6,12,true);
        if(failed)p.sendMessage("You are too heavy-handed with the chisel and make a rock.");
        else p.sendMessage("You shape the limestone into a brick.");
        return true;
    }
}
