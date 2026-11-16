package io.xeros.content.skills.smithing;

import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.Player;

/** Additional anvil products exposed by the client's existing More option. */
public final class AnvilExtras {
    private AnvilExtras(){}
    private static boolean live(Player p,WorldObject target,RegionProvider provider,int x,int y,int h){
        return p.getRegionProvider()==provider&&p.absX==x&&p.absY==y&&p.heightLevel==h&&target.height==h
            &&p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()
            &&Math.max(Math.abs(x-target.x),Math.abs(y-target.y))<=1
            &&provider.get(target.x,target.y).getWorldObject(target.id,target.x,target.y,h).filter(target::equals).isPresent();
    }
    public static boolean menu(Player p,int id,int x,int y){
        if(id!=2783&&id!=6150&&id!=2097)return false;
        RegionProvider provider=p.getRegionProvider();WorldObject target=provider.get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);
        int px=p.absX,py=p.absY,h=p.heightLevel;
        if(target==null||!live(p,target,provider,px,py,h))return true;
        p.interruptActions();final long token=p.productionActionToken;
        new DialogueBuilder(p).option("Additional anvil products",
            new DialogueOption("Bronze wire",q->quantity(q,1794,target,provider,px,py,h,token)),
            new DialogueOption("Mith grapple tip",q->quantity(q,9416,target,provider,px,py,h,token))).send();
        return true;
    }
    private static void quantity(Player p,int product,WorldObject target,RegionProvider provider,int x,int y,int h,long token){
        if(token!=p.productionActionToken||!live(p,target,provider,x,y,h))return;
        new DialogueBuilder(p).option("How many?",
            new DialogueOption("Make 1",q->make(q,product,1,target,provider,x,y,h,token)),
            new DialogueOption("Make 5",q->make(q,product,5,target,provider,x,y,h,token)),
            new DialogueOption("Make all",q->make(q,product,28,target,provider,x,y,h,token))).send();
    }
    private static void make(Player p,int product,int amount,WorldObject target,RegionProvider provider,int x,int y,int h,long token){
        if(token!=p.productionActionToken||!live(p,target,provider,x,y,h))return;
        AnvilRecipes.Recipe r=AnvilRecipes.forProduct(product);
        ProductionBatch.start(p,13,r.level,r.xp,amount,898,5,new int[][]{{2347,0},{r.bar,r.bars}},new int[][]{{r.product,r.quantity}},null,()->live(p,target,provider,x,y,h));
    }
}
