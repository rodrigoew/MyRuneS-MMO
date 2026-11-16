package io.xeros.content.skills.cooking;

import io.xeros.Server;
import io.xeros.model.entity.player.Player;
import io.xeros.model.multiplayersession.MultiplayerSessionType;
import io.xeros.model.multiplayersession.duel.DuelSession;
import io.xeros.model.multiplayersession.duel.DuelSessionRules;
import java.util.*;

/** Ordinary ale effects also apply to the temporary player-owned-house variants. */
public final class AleDrinks {
    private AleDrinks() {}
    private static final int[] REGULAR={1917,5763,1905,1909,1911,5755};
    private static final int[] HOUSE={7740,7752,7744,7746,7748,7754};
    private static int index(int id){for(int i=0;i<6;i++)if(REGULAR[i]==id||HOUSE[i]==id)return i;return -1;}
    public static boolean handles(int id){return index(id)>=0;}
    private static void boost(Player p,int skill,int flat,int percent){
        int base=p.getPA().getLevelForXP(p.playerXP[skill]);
        p.playerLevel[skill]=Math.max(p.playerLevel[skill],base+flat+base*percent/100);
        p.getPA().refreshSkill(skill);
    }
    private static void drain(Player p,int skill,int flat,int percent){
        p.playerLevel[skill]=Math.max(0,p.playerLevel[skill]-flat-p.playerLevel[skill]*percent/100);
        p.getPA().refreshSkill(skill);
    }
    public static boolean drink(Player p,int id,int slot){
        int kind=index(id);if(kind<0)return false;
        boolean house=id==HOUSE[kind];
        if(slot<0||slot>=p.playerItems.length||p.playerItems[slot]!=id+1||p.playerItemsN[slot]!=1
            ||p.isDead||p.getHealth().getCurrentHealth()<=0||p.getLock().cannotInteract(p)||p.teleTimer>0
            ||p.isBanking||p.isShopping||p.getPotionTimer().elapsed()<3)return true;
        if(house&&!p.getHouse().inside()){p.sendMessage("Drink this ale inside your house.");return true;}
        if(Server.getMultiplayerSessionListener().inSession(p,MultiplayerSessionType.TRADE))return true;
        DuelSession duel=(DuelSession)Server.getMultiplayerSessionListener().getMultiplayerSession(p,MultiplayerSessionType.DUEL);
        if(duel!=null&&duel.getRules().contains(DuelSessionRules.Rule.NO_DRINKS)){p.sendMessage("Drinks have been disabled for this duel.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPotionTimer().reset();
        p.playerItems[slot]=(house?7742:1919)+1;p.getItems().sendInventoryInterface(3214);
        p.startAnimation(829);p.getPA().sendSound(2401);
        switch(kind){
            case 0:boost(p,2,1,2);drain(p,0,1,6);break;
            case 1:boost(p,19,1,0);drain(p,0,2,0);drain(p,2,2,0);break;
            case 2:boost(p,2,2,0);drain(p,0,2,5);break;
            case 3:boost(p,15,1,0);drain(p,0,3,0);drain(p,1,3,0);drain(p,2,3,0);break;
            case 4:boost(p,2,2,0);drain(p,0,4,0);break;
            case 5:boost(p,7,1,5);drain(p,0,2,0);drain(p,2,2,0);break;
            default:throw new IllegalStateException("Ale type");
        }
        p.getHealth().increase(kind==1||kind==2?2:1);p.getPA().refreshSkill(3);
        p.sendMessage("You drink the ale.");return true;
    }
}
