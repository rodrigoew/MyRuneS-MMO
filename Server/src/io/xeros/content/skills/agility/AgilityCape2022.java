package io.xeros.content.skills.agility;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

/** 2017/2022 skillcape stamina perk; inventory and worn capes share the daily allowance. */
public final class AgilityCape2022 {
    private static Clock clock=Clock.systemUTC();
    private AgilityCape2022(){}
    private static boolean allowed(Player p){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!p.getInterfaceEvent().isActive()&&!p.getBankPin().requiresUnlock()&&!p.isBanking&&!p.isShopping&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private static boolean mastery(Player p,int id){
        if(id==9771||id==9772)return p.getLevelForXP(p.playerXP[16])>=99;
        if(id==13280){for(int xp:p.playerXP)if(p.getLevelForXP(xp)<99)return false;return true;}
        return false;
    }
    private static boolean owns(Player p,int id,boolean worn){
        int canonical=RunEnergy2022.inventoryId(id);
        return mastery(p,canonical)&&(worn?p.playerEquipment[Player.playerCape]==id&&p.playerEquipmentN[Player.playerCape]==1:id==canonical&&p.getItems().getInventoryCount(id)>0);
    }
    public static boolean stamina(Player p,int id,boolean worn){
        if(!allowed(p)||!owns(p,id,worn))return false;
        long day=LocalDate.now(clock).toEpochDay();
        if(p.agilityCapeStaminaDay>=day){p.sendMessage("You have already used your skillcape's stamina boost today.");return false;}
        p.agilityCapeStaminaDay=day;
        p.setRunEnergyUnits(10000,true);
        // A shorter cape effect must not remove an already active longer potion effect.
        p.staminaDelay=Math.max(p.staminaDelay,100L);
        p.getPA().sendGameTimer(ClientGameTimer.STAMINA,TimeUnit.MILLISECONDS,(int)Math.min(Integer.MAX_VALUE,p.staminaDelay*600L));
        p.sendMessage("Your cape restores your run energy and gives you a stamina boost.");
        return true;
    }
    public static boolean boost(Player p,int id){
        int canonical=RunEnergy2022.inventoryId(id);
        if((canonical!=9771&&canonical!=9772)||!allowed(p)||!owns(p,id,true))return false;
        if(p.playerLevel[16]<100){p.playerLevel[16]=100;p.getPA().refreshSkill(16);}
        p.sendMessage("Your cape boosts your Agility.");return true;
    }
    public static void maxFeatures(Player p){
        if(!allowed(p)||!((RunEnergy2022.inventoryId(p.playerEquipment[Player.playerCape])==13280&&owns(p,p.playerEquipment[Player.playerCape],true))||owns(p,13280,false)))return;
        new DialogueBuilder(p).option("Max cape features",new DialogueOption("Stamina Boost",q->{q.getPA().removeAllWindows();if(q.playerEquipment[Player.playerCape]==13342||q.playerEquipment[Player.playerCape]==13280)stamina(q,q.playerEquipment[Player.playerCape],true);else stamina(q,13280,false);}),new DialogueOption("Nevermind",q->q.getPA().removeAllWindows())).send();
    }
    public static boolean near(Player p,NPC n){return n!=null&&n.getNpcId()==5789&&n.getIndex()>=0&&n.getIndex()<NPCHandler.npcs.length&&NPCHandler.npcs[n.getIndex()]==n&&!n.isDead()&&n.getX()==2809&&n.getY()==3193&&n.heightLevel==0&&p.heightLevel==0&&p.getInstance()==null&&p.distanceToPoint(2809,3193)<=2&&allowed(p);}
    public static boolean npc(Player p,NPC n,int option){
        if(n==null||n.getNpcId()!=5789||option!=1)return false;
        if(!near(p,n))return true;
        if(p.getLevelForXP(p.playerXP[16])<99){p.sendMessage("Cap'n Izzy sells an Agility cape and hood to those with 99 Agility.");return true;}
        new DialogueBuilder(p).itemStatement(9771,"An Agility cape and matching hood cost 99,000 coins.")
            .option(new DialogueOption("Buy cape and hood",q->{q.getPA().removeAllWindows();buy(q,n);}),new DialogueOption("Nevermind",q->q.getPA().removeAllWindows())).send();return true;
    }
    public static boolean buy(Player p,NPC n){
        if(!near(p,n)||p.getLevelForXP(p.playerXP[16])<99)return false;
        int maxed=0;for(int xp:p.playerXP)if(p.getLevelForXP(xp)>=99)maxed++;
        int cape=maxed>=2?9772:9771;
        if(!ProductionBatch.exchange(p,new int[][]{{995,99000}},new int[][]{{cape,1},{9773,1}})){p.sendMessage("You need 99,000 coins and space for the cape and hood.");return false;}
        p.sendMessage("Cap'n Izzy gives you an Agility cape and matching hood.");return true;
    }
}
