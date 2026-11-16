package io.xeros.content.skills;

import io.xeros.Server;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** A batch owns its recipe and origin; no shared mutable recipe between players. */
public final class ProductionBatch {
    private ProductionBatch() {}
    public static boolean exchange(Player p, int[][] inputs, int[][] outputs) {
        return exchange(p,inputs,outputs,true);
    }
    /** A read-only capacity/material check, using exactly the production transaction rules. */
    public static boolean canExchange(Player p,int[][] inputs,int[][] outputs){
        return exchange(p,inputs,outputs,false);
    }
    private static boolean exchange(Player p,int[][] inputs,int[][] outputs,boolean commit){
        Map<Integer,Integer> required=new LinkedHashMap<>(), consumed=new LinkedHashMap<>();
        for(int[] item:inputs){required.merge(item[0],Math.max(1,item[1]),Integer::sum);if(item[1]>0)consumed.merge(item[0],item[1],Integer::sum);}
        for(Map.Entry<Integer,Integer> item:required.entrySet())if(p.getItems().getInventoryCount(item.getKey())<item.getValue())return false;
        int[] ids=p.playerItems.clone(),counts=p.playerItemsN.clone();
        for(Map.Entry<Integer,Integer> item:consumed.entrySet()){
            int left=item.getValue();for(int i=0;i<ids.length&&left>0;i++)if(ids[i]==item.getKey()+1){int take=Math.min(left,counts[i]);counts[i]-=take;left-=take;if(counts[i]==0)ids[i]=0;}
        }
        Map<Integer,Integer> stored=new HashMap<>();
        for(int[] item:outputs){
            if(item[0]<0||item[1]<=0)return false;
            int remaining=item[1]-p.getStorageCapture().reserve(item[0],item[1],ids,stored);boolean stack=ItemDef.forId(item[0]).isStackable();
            if(stack){for(int i=0;i<ids.length;i++)if(ids[i]==item[0]+1){long total=(long)counts[i]+remaining;if(total>Integer.MAX_VALUE)return false;counts[i]=(int)total;remaining=0;break;}}
            for(int i=0;i<ids.length&&remaining>0;i++)if(ids[i]<=0){ids[i]=item[0]+1;counts[i]=stack?remaining:1;remaining-=counts[i];}
            if(remaining>0){if(commit)p.sendMessage("You need more inventory space.");return false;}
        }
        if(!commit)return true;
        for(Map.Entry<Integer,Integer> item:consumed.entrySet())p.getItems().deleteItem2(item.getKey(),item.getValue());
        for(int[] item:outputs)p.getItems().addItem(item[0],item[1]);
        return true;
    }
    public static boolean start(Player p,int skill,int level,double xp,int quantity,int animation,int ticks,int[][] inputs,int[][] outputs,Runnable made){
        return start(p,skill,level,xp,quantity,animation,ticks,inputs,outputs,made,()->true);
    }
    public static boolean start(Player p,int skill,int level,double xp,int quantity,int animation,int ticks,int[][] inputs,int[][] outputs,Runnable made,java.util.function.BooleanSupplier valid){
        if(quantity<=0||!p.getUtilityMagic().canInteract())return false;
        if(!valid.getAsBoolean())return false;
        if(p.playerLevel[skill]<level){p.sendMessage("You need "+Skill.forId(skill).toString()+" level "+level+".");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        final long token=p.productionActionToken;p.playerSkilling[skill]=true;
        final int x=p.absX,y=p.absY,h=p.heightLevel;final int[] remaining={Math.min(quantity,10000)};
        p.startAnimation(animation);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,ticks){
            public void execute(){
                if(token!=p.productionActionToken||!p.playerSkilling[skill]||p.isDisconnected()||p.getSession()==null||!p.getUtilityMagic().canInteract()||p.absX!=x||p.absY!=y||p.heightLevel!=h||p.playerLevel[skill]<level){stop();return;}
                if(!valid.getAsBoolean()||!exchange(p,inputs,outputs)){stop();return;}
                p.getPA().addSkillXPMultiplied(xp,skill,true);p.startAnimation(animation);if(made!=null)made.run();
                if(--remaining[0]<=0)stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[skill]=false;p.startAnimation(65535);}}
        });
        return true;
    }
}
