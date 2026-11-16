package io.xeros.content.skills.herblore;

import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ContainerUpdate;
import io.xeros.content.combat.Hitmark;
import java.util.*;

/** Independent timers for each affected stat; saved in game ticks (offline pauses). */
public final class DivinePotions {
    public enum Kind {
        COMBAT(23685,0,1,2),ATTACK(23697,0),STRENGTH(23709,2),DEFENCE(23721,1),
        RANGING(23733,4),MAGIC(23745,6),BATTLEMAGE(24623,1,6),BASTION(24635,1,4);
        public final int base;public final int[] skills;
        Kind(int b,int... s){base=b;skills=s;}
        public int item(int doses){return base+(4-doses)*3;}
    }
    private final Player player;
    private final int[] remaining=new int[7],targets=new int[7];
    public DivinePotions(Player p){player=p;}
    public static Kind kind(int item){for(Kind k:Kind.values())for(int d=1;d<=4;d++)if(k.item(d)==item)return k;return null;}
    public boolean canDrink(){return !player.isDead&&!player.getLock().cannotInteract(player)&&player.getHealth().getCurrentHealth()>10;}
    public boolean drink(int item,int slot){
        Kind k=kind(item);if(k==null)return false;
        if(slot<0||slot>=player.playerItems.length||player.playerItems[slot]!=item+1||player.playerItemsN[slot]!=1||!canDrink())return false;
        int dose=4-(item-k.base)/3,next=dose==1?(player.breakVials?-1:229):k.item(dose-1);
        player.interruptActions();player.playerItems[slot]=next+1;player.playerItemsN[slot]=next<0?0:1;
        for(int skill:k.skills){
            int base=player.getLevelForXP(player.playerXP[skill]),boost=skill==6?4:skill==4?4+base/10:5+(base*15)/100;
            targets[skill]=base+boost;remaining[skill]=500;
            if(player.playerLevel[skill]<targets[skill])player.playerLevel[skill]=Math.min(targets[skill],player.playerLevel[skill]+boost);
            player.getPA().refreshSkill(skill);
        }
        player.startAnimation(829);player.appendDamage(10,Hitmark.HIT);player.getPA().refreshSkill(3);
        player.getItems().addContainerUpdate(ContainerUpdate.INVENTORY);return true;
    }
    public boolean preventsDecay(int skill){
        if(skill<0||skill>=remaining.length)return false;
        int base=player.getLevelForXP(player.playerXP[skill]);
        return remaining[skill]>0&&player.playerLevel[skill]>base&&player.playerLevel[skill]<=targets[skill];
    }
    public int remaining(int skill){return remaining[skill];}
    public void process(){
        if(player.isDead||player.getHealth().getCurrentHealth()<=0){clear();return;}
        for(int i=0;i<remaining.length;i++)if(remaining[i]>0&&--remaining[i]==0){
            player.playerLevel[i]=Math.min(player.playerLevel[i],player.getLevelForXP(player.playerXP[i]));targets[i]=0;player.getPA().refreshSkill(i);
            player.sendMessage("Your divine "+io.xeros.content.skills.Skill.forId(i)+" effect has worn off.");
        }
    }
    public void clear(){for(int i=0;i<remaining.length;i++)if(remaining[i]>0){remaining[i]=0;targets[i]=0;player.playerLevel[i]=Math.min(player.playerLevel[i],player.getLevelForXP(player.playerXP[i]));player.getPA().refreshSkill(i);}}
    public String encode(){StringJoiner j=new StringJoiner(",");for(int i=0;i<remaining.length;i++){j.add(Integer.toString(remaining[i]));j.add(Integer.toString(targets[i]));}return j.toString();}
    public boolean decode(String data){
        try{String[] parts=data.split(",",-1);if(parts.length!=14)return false;int[] ticks=new int[7],levels=new int[7];
            for(int i=0;i<7;i++){ticks[i]=Integer.parseInt(parts[i*2]);levels[i]=Integer.parseInt(parts[i*2+1]);if(ticks[i]<0||ticks[i]>500||levels[i]<0||levels[i]>150||(ticks[i]>0&&(i==3||i==5||levels[i]==0)))return false;}
            System.arraycopy(ticks,0,remaining,0,7);System.arraycopy(levels,0,targets,0,7);return true;
        }catch(RuntimeException e){return false;}
    }
}
