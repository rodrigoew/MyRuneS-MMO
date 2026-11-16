package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.SkillingOutfit;
import io.xeros.content.skills.mining.motherlode.MotherlodeMine;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

/** Three shared limestone stages from the 2022 cache, not a one-ore generic vein. */
public final class LimestoneMining {
    private LimestoneMining(){}
    public static boolean rock(int id){return id>=11382&&id<=11385;}
    public static int threshold(int level,int boost){
        int effective=Math.max(1,Math.min(99,level))+Math.max(0,boost);
        // Historical 2022 crowdsourced coefficients; not a Jagex-confirmed formula.
        return Math.max(0,Math.min(256,1+(int)Math.floor((95*(99d-effective)+350*(effective-1d))/98d+.5)));
    }
    private static WorldObject current(Player p,int x,int y,int h){
        for(int id=11382;id<=11385;id++){
            WorldObject o=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,h).orElse(null);
            if(o!=null&&(o.type==10||o.type==11))return o;
        }return null;
    }
    private static boolean near(Player p,WorldObject o){
        var size=o.getObjectSize();
        return Math.max(Math.max(o.x-p.absX,p.absX-o.x-size.getX()+1),Math.max(o.y-p.absY,p.absY-o.y-size.getY()+1))<=1;
    }
    public static boolean click(Player p,int id,int x,int y){
        if(!rock(id))return false;
        start(p,id,x,y,Misc::trueRand);return true;
    }
    public static boolean prospect(Player p,int id,int x,int y){
        if(!rock(id))return false;WorldObject o=current(p,x,y,p.heightLevel);
        if(o!=null&&o.id==id&&near(p,o)&&p.getUtilityMagic().canInteract())
            p.sendMessage(id==11385?"There is no limestone left in this pile.":"This pile contains limestone.");
        return true;
    }
    public static boolean start(Player p,int id,int x,int y,IntUnaryOperator random){
        WorldObject o=current(p,x,y,p.heightLevel);
        if(!rock(id)||id==11385||o==null||o.id!=id||!near(p,o)||!p.getUtilityMagic().canInteract())return false;
        if(p.playerLevel[14]<10){p.sendMessage("You need Mining level 10 to mine limestone.");return false;}
        Pickaxe pick=Pickaxe.getBestPickaxe(p);
        if(pick==null){p.sendMessage("You need a suitable pickaxe.");return false;}
        if(p.getItems().freeSlots()==0){p.sendMessage("Your inventory is full.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        long token=p.productionActionToken;int ox=p.absX,oy=p.absY,h=p.heightLevel;
        p.playerSkilling[14]=true;p.facePosition(x,y);p.startAnimation(pick.getAnimation());
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
            int until=VolcanicAsh.delay(pick,random);
            public void execute(){
                WorldObject live=current(p,x,y,h);Pickaxe tool=Pickaxe.getBestPickaxe(p);
                if(token!=p.productionActionToken||!p.playerSkilling[14]||p.isDisconnected()||p.getSession()==null
                    ||!p.getUtilityMagic().canInteract()||p.absX!=ox||p.absY!=oy||p.heightLevel!=h
                    ||!p.isWalkingQueueEmpty()||p.playerLevel[14]<10||tool==null||live==null||live.id==11385
                    ||p.getItems().freeSlots()==0){stop();return;}
                if(--until>0)return;until=VolcanicAsh.delay(tool,random);p.startAnimation(tool.getAnimation());
                int gem=MotherlodeMine.preGem(random.applyAsInt(PreciousRockMining.gemTableDenominator(p)));
                if(gem!=-2){if(gem>0&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{gem,1}}))stop();return;}
                if(random.applyAsInt(256)>=threshold(p.playerLevel[14],CoreRockMining.invisibleBoost(p)))return;
                if(!award(p)){stop();return;}
                p.getMiningAccessories().consumeCrystal(tool,random);
                if(CoreRockMining.cape(p)&&random.applyAsInt(100)<5)award(p);
                if(CoreRockMining.armour(p)>=1&&random.applyAsInt(100)<10)award(p);
                if(p.getMiningTools().ringBonus(CoreRockMining.Rock.CLAY,random))award(p);
                int petChance=741600-25*Math.max(1,Math.min(99,p.getPA().getLevelForXP(p.playerXP[14])));
                if(random.applyAsInt(petChance)==0&&p.getItems().getItemCount(13321,false)==0&&p.petSummonId!=7439){
                    p.getItems().addItemUnderAnyCircumstance(13321,1);p.getCollectionLog().handleDrop(p,5,13321,1);
                }
                advance(p,live);
                p.sendMessage("You mine some limestone.");
                if(live.id==11384)stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[14]=false;p.startAnimation(65535);}}
        });return true;
    }
    private static boolean award(Player p){
        if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{3211,1}}))return false;
        p.getPA().addSkillXPMultiplied(26.5*SkillingOutfit.PROSPECTOR.multiplier(p),14,true);return true;
    }
    private static void advance(Player p,WorldObject live){
        var region=p.getRegionProvider().get(live.x,live.y);
        GlobalObject old=Server.getGlobalObjects().get(live.id,live.x,live.y,live.height);
        if(old!=null&&old.getInstance()==p.getInstance())Server.getGlobalObjects().remove(old);
        else region.removeObject(live.id,live.x,live.y,live.height,live.type,live.face);
        GlobalObject next=new GlobalObject(live.id+1,live.x,live.y,live.height,live.face,live.type,live.id==11384?9:-1,11382);
        next.setInstance(p.getInstance());region.removeWorldObject(next);Server.getGlobalObjects().add(next);
    }
}
