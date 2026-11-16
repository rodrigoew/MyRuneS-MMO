package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.SkillingOutfit;
import io.xeros.content.achievement.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

/** Ash-specific yields/depletion follow the pre-2023 activity, not ordinary ore drops.
 * Chance coefficients and pickaxe timing reference OpenRune (ISC); see third-party licences. */
public final class VolcanicAsh {
    public static final int PILE=30985,EMPTY=30986,ASH=21622,SODA=1781,RESPAWN_TICKS=50;
    private VolcanicAsh() {}
    public static boolean nativePile(WorldObject object){
        return object.id==PILE&&object.height==0&&(object.type==10||object.type==11)
                &&object.x>=3770&&object.x<=3830&&object.y>=3745&&object.y<=3840;
    }
    public static int amount(int level){return Math.max(1,Math.min(6,1+(level-22)/15));}
    public static int threshold(int visibleLevel,int invisibleBoost){
        int level=Math.max(1,Math.min(99,visibleLevel))+Math.max(0,invisibleBoost);
        return Math.max(0,Math.min(256,1+(int)Math.floor(50d*(99-level)/98d+200d*(level-1)/98d+.5)));
    }
    public static int delay(Pickaxe pick,IntUnaryOperator random){
        switch(pick){case BRONZE:return 8;case IRON:return 7;case STEEL:return 6;
            case BLACK:case MITHRIL:return 5;case ADAMANT:return 4;case RUNE:return 3;
            case CRYSTAL:return random.applyAsInt(4)==0?2:3;
            default:return random.applyAsInt(6)==0?2:3;}
    }
    private static int invisibleBoost(Player p){
        for(int item:new int[]{25539,25541,25543,25545})if(p.getItems().isWearingItem(item,Player.playerRing))return 4;
        return 0;
    }
    public static boolean click(Player p,int object,int x,int y){
        if(object!=PILE&&object!=EMPTY)return false;
        if(object==EMPTY)p.sendMessage("There is no ash left in this pile.");
        else start(p,x,y,Misc::trueRand);
        return true;
    }
    public static boolean start(Player p,int x,int y,IntUnaryOperator random){
        if(!p.getUtilityMagic().canInteract())return false;
        WorldObject pile=RegionProvider.getGlobal().get(x,y).getWorldObject(PILE,x,y,p.heightLevel).orElse(null);
        if(pile==null||!nativePile(pile)||Server.getGlobalObjects().exists(EMPTY,x,y,p.heightLevel))return false;
        io.xeros.model.entity.player.Position size=pile.getObjectSize();
        int dx=Math.max(x-p.absX,p.absX-(x+size.getX()-1)),dy=Math.max(y-p.absY,p.absY-(y+size.getY()-1));
        if(Math.max(dx,dy)>1)return false;
        if(p.playerLevel[14]<22){p.sendMessage("You need Mining level 22 to mine volcanic ash.");return false;}
        Pickaxe pick=Pickaxe.getBestPickaxe(p);
        if(pick==null){p.sendMessage("You need a suitable pickaxe.");return false;}
        if(p.getItems().freeSlots()==0&&p.getItems().getInventoryCount(ASH)==0){p.sendMessage("Your inventory is full.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        final long token=p.productionActionToken;final int originX=p.absX,originY=p.absY,height=p.heightLevel;
        p.playerSkilling[14]=true;p.startAnimation(pick.getAnimation());p.facePosition(x,y);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
            private int until=delay(pick,random);
            public void execute(){
                Pickaxe current=Pickaxe.getBestPickaxe(p);
                if(token!=p.productionActionToken||!p.playerSkilling[14]||p.isDisconnected()||p.getSession()==null
                        ||!p.getUtilityMagic().canInteract()||p.absX!=originX||p.absY!=originY
                        ||p.getRegionProvider().get(x,y).getWorldObject(PILE,x,y,height).isEmpty()||p.heightLevel!=height||p.playerLevel[14]<22||current==null
                        ||Server.getGlobalObjects().exists(EMPTY,x,y,height)){stop();return;}
                if(--until>0)return;
                until=delay(current,random);p.startAnimation(current.getAnimation());
                if(random.applyAsInt(256)>=threshold(p.playerLevel[14],invisibleBoost(p)))return;
                int product=random.applyAsInt(20)==0?SODA:ASH;
                int quantity=product==SODA?1:amount(p.playerLevel[14]);
                if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{product,quantity}})){stop();return;}
                p.getMiningAccessories().consumeCrystal(current,random);
                p.getPA().addSkillXPMultiplied(10*SkillingOutfit.PROSPECTOR.multiplier(p),14,true);
                Achievements.increase(p,AchievementType.MINE,1);
                p.sendMessage(product==SODA?"You mine some soda ash.":"You mine some volcanic ash.");
                // Each successful extraction can exhaust the pile; failed rolls never do.
                if(random.applyAsInt(4)==0){Server.getGlobalObjects().add(new GlobalObject(EMPTY,x,y,height,pile.face,pile.type,RESPAWN_TICKS,PILE));stop();}
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[14]=false;p.startAnimation(65535);}}
        });
        return true;
    }
}
