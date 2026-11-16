package io.xeros.content.skills.thieving;

import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;

/** 2022 gloves: failure reduction, shared player condition, and atomic fur repair. */
public final class GlovesOfSilence {
    public static final int ITEM=10075,FUR=10115;
    private GlovesOfSilence(){}
    public static boolean diaryBonus(Player p){
        return p.getDiaryManager().getArdougneDiary().hasDoneHard()
            ||p.getDiaryManager().getArdougneDiary().hasDoneMedium()&&Boundary.isIn(p,Boundary.ARDOUGNE_BOUNDARY);
    }
    public static boolean active(Player p){return p.playerEquipment[Player.playerHands]==ITEM&&!diaryBonus(p);}
    public static double chance(Player p,double base){
        double failure=1-Math.max(0,Math.min(1,base));
        return 1-failure*(diaryBonus(p)?.90:active(p)?.95:1);
    }
    /** Called once only after a real failed pickpocket, never for refused attempts. */
    public static void failed(Player p){
        if(!active(p))return;
        if(++p.silenceGloveFailures>=62){
            p.silenceGloveFailures=0;p.getItems().setEquipment(-1,0,Player.playerHands,true);
            p.sendMessage("Your gloves of silence fall apart.");
        }else if(p.silenceGloveFailures==61)p.sendMessage("Your gloves of silence are going to fall apart!");
    }
    public static void check(Player p){
        int n=Math.max(0,Math.min(61,p.silenceGloveFailures));
        p.sendMessage(n==0?"Your gloves are new.":n<29?"Your gloves are in good condition.":n<43?"Your gloves are starting to look quite shabby.":n<53?"Your gloves are starting to need repair.":n<61?"Your gloves are in need of repair!":"Your gloves are about to fall apart!");
    }
    public static boolean repair(Player p,int first,int second){
        if(!((first==FUR&&second==ITEM)||(first==ITEM&&second==FUR)))return false;
        if(!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||p.getInterfaceEvent().isActive()
                ||p.getBankPin().requiresUnlock()||p.isBanking||p.isShopping)return true;
        if(p.playerLevel[12]<64){p.sendMessage("You need Crafting level 64 to repair these gloves.");return true;}
        // Zero amounts are required reusable tools in ProductionBatch.
        if(ProductionBatch.exchange(p,new int[][]{{ITEM,0},{FUR,1},{1734,1},{946,0},{1733,0}},new int[0][])){
            p.silenceGloveFailures=0;p.sendMessage("You repair your gloves of silence.");
        }else p.sendMessage("You need dark kebbit fur, thread, a needle and a knife to repair the gloves.");
        return true;
    }
}
