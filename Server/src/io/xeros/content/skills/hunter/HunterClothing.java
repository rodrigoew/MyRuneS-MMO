package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Asyff's twenty 2022 fur recipes. NPC tailoring grants no skill experience. */
public final class HunterClothing {
    public static final class Recipe {
        public final int item,fee,ticks;public final int[][] furs;
        Recipe(int item,int fee,int ticks,int[][]furs){this.item=item;this.fee=fee;this.ticks=ticks;this.furs=furs;}
    }
    // Generated from the frozen 2022 recipe facts, not from custom shop prices.
    public static final Recipe[] RECIPES={
        new Recipe(10065,20,0,new int[][]{{10117,2}}),new Recipe(10067,20,0,new int[][]{{10117,2}}),
        new Recipe(10053,20,0,new int[][]{{10121,2}}),new Recipe(10055,20,0,new int[][]{{10121,2}}),
        new Recipe(10057,20,0,new int[][]{{10119,2}}),new Recipe(10059,20,0,new int[][]{{10119,2}}),
        new Recipe(10061,20,0,new int[][]{{10123,2}}),new Recipe(10063,20,0,new int[][]{{10123,2}}),
        new Recipe(10045,500,0,new int[][]{{10095,1}}),new Recipe(10043,100,0,new int[][]{{10095,1},{10093,1}}),new Recipe(10041,100,0,new int[][]{{10095,1},{10093,1}}),
        new Recipe(10051,750,0,new int[][]{{10099,1}}),new Recipe(10049,150,0,new int[][]{{10099,1},{10097,1}}),new Recipe(10047,150,0,new int[][]{{10099,1},{10097,1}}),
        new Recipe(10039,1000,0,new int[][]{{10103,1}}),new Recipe(10037,200,0,new int[][]{{10103,1},{10101,1}}),new Recipe(10035,200,0,new int[][]{{10103,1},{10101,1}}),
        new Recipe(10075,600,1,new int[][]{{10115,2}}),new Recipe(10069,400,1,new int[][]{{10125,2}}),new Recipe(10071,800,1,new int[][]{{10127,2}})
    };
    private HunterClothing(){}
    public static boolean near(Player p,NPC n){return n!=null&&n.getNpcId()==2887&&n.getIndex()>=0&&n.getIndex()<NPCHandler.npcs.length&&NPCHandler.npcs[n.getIndex()]==n&&!n.isDead()
        &&n.getX()==3281&&n.getY()==3398&&n.heightLevel==0&&p.heightLevel==0&&p.getInstance()==null&&p.distanceToPoint(3281,3398)<=2
        &&p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!p.getInterfaceEvent().isActive()&&!p.getBankPin().requiresUnlock()
        &&!p.isBanking&&!p.isShopping&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    public static boolean npc(Player p,NPC n,int option){
        if(n==null||n.getNpcId()!=2887||option!=3)return false;
        if(near(p,n)){p.getPA().removeAllWindows();page(p,n,0);}return true;
    }
    private static void page(Player p,NPC n,int page){
        if(!near(p,n))return;
        if(page==0)new DialogueBuilder(p).option("Custom Fur Clothing",
            new DialogueOption("Polar camouflage",q->pieces(q,n,0,2)),new DialogueOption("Wood camouflage",q->pieces(q,n,2,2)),
            new DialogueOption("Jungle camouflage",q->pieces(q,n,4,2)),new DialogueOption("Desert camouflage",q->pieces(q,n,6,2)),
            new DialogueOption("More clothing",q->page(q,n,1))).send();
        else new DialogueBuilder(p).option("Custom Fur Clothing",
            new DialogueOption("Larupia clothing",q->pieces(q,n,8,3)),new DialogueOption("Graahk clothing",q->pieces(q,n,11,3)),
            new DialogueOption("Kyatt clothing",q->pieces(q,n,14,3)),new DialogueOption("Gloves and spotted capes",q->pieces(q,n,17,3)),
            new DialogueOption("Camouflage clothing",q->page(q,n,0))).send();
    }
    private static void pieces(Player p,NPC n,int start,int count){
        if(!near(p,n))return;List<DialogueOption> choices=new ArrayList<>();
        for(int i=start;i<start+count;i++){final Recipe r=RECIPES[i];choices.add(new DialogueOption(ItemDef.forId(r.item).getName()+" ("+r.fee+" coins)",q->offer(q,n,r)));}
        choices.add(new DialogueOption("Back",q->page(q,n,start<8?0:1)));
        new DialogueBuilder(p).option("Custom Fur Clothing",choices.toArray(new DialogueOption[0])).send();
    }
    private static void offer(Player p,NPC n,Recipe r){
        if(!near(p,n))return;String fur=ItemDef.forId(r.furs[0][0]).getName();
        if(r.furs.length>1)fur+=" or "+ItemDef.forId(r.furs[1][0]).getName();
        new DialogueBuilder(p).itemStatement(r.item,"Asyff needs "+r.furs[0][1]+" "+fur+" and "+r.fee+" coins.")
            .option(new DialogueOption("Make this item",q->{q.getPA().removeAllWindows();make(q,n,r.item);}),new DialogueOption("Back",q->page(q,n,0))).send();
    }
    public static boolean make(Player p,NPC n,int item){
        if(!near(p,n))return false;Recipe r=Arrays.stream(RECIPES).filter(x->x.item==item).findFirst().orElse(null);if(r==null)return false;
        for(int[]fur:r.furs){int[][]in={{fur[0],fur[1]},{995,r.fee}},out={{r.item,1}};
            if(!ProductionBatch.canExchange(p,in,out))continue;
            if(r.ticks==0){if(!ProductionBatch.exchange(p,in,out))return false;p.sendMessage("Asyff makes your "+ItemDef.forId(item).getName()+".");}
            else {
                p.interruptActions();final long token=p.productionActionToken;
                Server.getEventHandler().submit(new io.xeros.model.cycleevent.Event<Player>(p,r.ticks){public void execute(){
                    if(token==p.productionActionToken&&near(p,n)&&ProductionBatch.exchange(p,in,out))p.sendMessage("Asyff makes your "+ItemDef.forId(item).getName()+".");stop();
                }});
            }
            return true;
        }
        p.sendMessage("Bring the required unnoted furs and coins, and leave space for the clothing.");return false;
    }
}
