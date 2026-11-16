package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** A deadfall owns its site, prey and pending reward until it is cleared. */
public final class DeadfallHunting {
    public enum Prey {
        WILD(1349,23,128,10113,20651,2132), BARB_TAILED(1348,33,168,10129,20650,10138),
        PRICKLY(1346,37,204,10105,20648,6006), SABRE_TOOTHED(1347,51,200,10109,20649,2132);
        public final int npc,level,xp,product,caught,bait;
        Prey(int npc,int level,int xp,int product,int caught,int bait){this.npc=npc;this.level=level;this.xp=xp;this.product=product;this.caught=caught;this.bait=bait;}
    }
    private static final Map<Player,Trap> OWNERS=new IdentityHashMap<>();
    private static final Map<Long,Trap> SITES=new HashMap<>();
    private static final Map<NPC,Trap> CLAIMS=new IdentityHashMap<>();
    private static final Random RANDOM=new Random();
    private DeadfallHunting(){}
    private static long key(int x,int y){return ((long)x<<32)|(y&0xffffffffL);}
    private static Prey kind(int npc){for(Prey p:Prey.values())if(p.npc==npc)return p;return null;}
    private static boolean habitat(int x,int y){return (x>=2300&&x<=2370&&y>=3500&&y<=3650)||(x>=2550&&x<=2600&&y>=2880&&y<=2940)||(x>=2700&&x<=2730&&y>=3750&&y<=3790);}
    private static boolean usable(Player p){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0;}
    private static int distance(int x,int y,WorldObject target){Position size=target.getObjectSize();int dx=Math.max(target.x-x,Math.max(0,x-target.x-Math.max(1,size.getX())+1));int dy=Math.max(target.y-y,Math.max(0,y-target.y-Math.max(1,size.getY())+1));return Math.max(dx,dy);}
    private static boolean live(WorldObject target){return RegionProvider.getGlobal().get(target.x,target.y).getWorldObject(target.id,target.x,target.y,target.height).filter(target::equals).isPresent();}
    public static boolean click(Player p,WorldObject object,int option){
        if(object==null||!habitat(object.x,object.y))return false;
        Trap t=SITES.get(key(object.x,object.y));
        if(t!=null){
            if(!usable(p)||distance(p.absX,p.absY,object)>1||!t.current(object))return true;
            if(t.owner!=p){p.sendMessage("Someone else has set this trap.");return true;}
            if(option==2){p.sendMessage(t.caught?"There is a kebbit under the boulder.":"The deadfall is waiting for a kebbit."+(t.baited?" It has bait.":"")+(t.smoked?" It has been smoked.":""));return true;}
            if(option==1){if(t.caught)t.collect();else t.close(true);}
            return true;
        }
        if(object.id!=19215||object.type!=10||object.height!=0)return false;
        if(option!=1||!usable(p)||!live(object)||distance(p.absX,p.absY,object)>1)return true;
        if(!Region.getBaseMapRegion(object.x,object.y).getWorldObject(object.id,object.x,object.y,0).filter(object::equals).isPresent())return true;
        if(OWNERS.containsKey(p)){p.sendMessage("You can only have one deadfall trap at a time.");return true;}
        if(p.playerLevel[21]<23){p.sendMessage("You need Hunter level23 to set a deadfall trap.");return true;}
        if(!p.getItems().playerHasItem(946)||!p.getItems().playerHasItem(1511)){p.sendMessage("You need a knife and logs to set the trap.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        t=new Trap(p,object);OWNERS.put(p,t);SITES.put(key(object.x,object.y),t);t.start();return true;
    }
    public static boolean use(Player p,int item,int object,int x,int y){
        Trap t=SITES.get(key(x,y));if(t==null)return false;
        if(t.owner!=p||!usable(p)||!t.currentId(object)||distance(p.absX,p.absY,t.base)>1||t.caught||!t.placed)return true;
        p.interruptActions();
        if(item==594){if(!HunterTorch.canSmoke(p))return true;t.smoked=true;p.startAnimation(5213);p.sendMessage("You smoke the trap.");return true;}
        Prey prey=t.expected();if(prey!=null&&item==prey.bait){
            if(!t.baited&&ProductionBatch.exchange(p,new int[][]{{item,1}},new int[0][])){t.baited=true;t.baitItem=item;p.sendMessage("You add bait to the trap.");}return true;
        }
        p.sendMessage("That is not suitable bait for this deadfall.");return true;
    }
    /** Local interpolation; the original server's full chance curve is not published. */
    static int catchChance(int level,Prey prey,boolean bait,boolean smoke){return Math.min(95,45+Math.max(0,level-prey.level)*3/4+(bait?3:0)+(smoke?2:0));}
    private static final class Trap {
        final Player owner;final WorldObject base;final int x,y;final long token;
        int age,baitItem;boolean placed,caught,closed,baited,smoked;GlobalObject model;NPC target;Prey prey;boolean oldWalking;
        Trap(Player owner,WorldObject base){this.owner=owner;this.base=base;x=owner.absX;y=owner.absY;token=owner.productionActionToken;}
        void start(){owner.facePosition(base.x,base.y);owner.startAnimation(5212);
            Server.getEventHandler().submit(new Event<Trap>("deadfall",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();if(!closed)close(false);}});
        }
        boolean currentId(int id){return model!=null&&model.getObjectId()==id&&live(new WorldObject(id,base.x,base.y,0,base.type,base.face));}
        boolean current(WorldObject o){return model!=null&&o.id==model.getObjectId()&&o.x==base.x&&o.y==base.y&&o.height==0&&o.type==base.type&&o.face==base.face&&live(o);}
        boolean swap(int id){
            Region r=RegionProvider.getGlobal().get(base.x,base.y);
            if(model==null){if(!live(base))return false;r.removeObject(base.id,base.x,base.y,0,base.type,base.face);r.removeWorldObject(base.toGlobalObject());}
            else if(!Server.getGlobalObjects().removeIfCurrent(model))return false;
            model=new GlobalObject(id,base.x,base.y,0,base.face,base.type,-1);Server.getGlobalObjects().add(model);return true;
        }
        boolean step(){
            if(closed)return false;age++;
            if(owner.isDisconnected()||owner.getSession()==null||owner.getRegionProvider()!=RegionProvider.getGlobal()||owner.heightLevel!=0||owner.isDead||Math.max(Math.abs(owner.absX-base.x),Math.abs(owner.absY-base.y))>64)return false;
            if(!placed){
                if(token!=owner.productionActionToken||owner.absX!=x||owner.absY!=y||!usable(owner)||owner.playerLevel[21]<23||!live(base))return false;
                if(age<6)return true;
                if(!ProductionBatch.exchange(owner,new int[][]{{946,0},{1511,1}},new int[0][]))return false;
                if(!swap(19217))return false;placed=true;owner.startAnimation(65535);return true;
            }
            if(!currentId(model.getObjectId()))return false;
            if(caught)return true;
            if(target==null){if(age%6==0)claim();return true;}
            if((target.isDead()||target.needRespawn)||target.getHeight()!=0||CLAIMS.get(target)!=this||owner.playerLevel[21]<prey.level){release();return true;}
            if(distance(target.getX(),target.getY(),base)<=1){
                boolean success=RANDOM.nextInt(100)<catchChance(owner.playerLevel[21],prey,baited&&baitItem==prey.bait,smoked);
                if(!success){owner.sendMessage("The kebbit escapes your deadfall.");return false;}
                if(!swap(prey.caught))return false;target.setDead(true);caught=true;release();owner.getPA().sendSound(2631);return true;
            }
            Position next=approach(target);NPCDumbPathFinder.walkTowards(target,next.getX(),next.getY());return true;
        }
        Prey expected(){
            if(prey!=null)return prey;NPC n=nearest(false);if(n!=null)return kind(n.getNpcId());
            return base.x>=2700?Prey.SABRE_TOOTHED:base.x>=2550?Prey.BARB_TAILED:base.y>=3590?Prey.PRICKLY:Prey.WILD;
        }
        NPC nearest(boolean available){NPC best=null;int range=13;for(NPC n:NPCHandler.npcs)if(n!=null&&!n.isDead()&&!n.needRespawn&&n.getHeight()==0&&kind(n.getNpcId())!=null&&(!available||!CLAIMS.containsKey(n))){int d=distance(n.getX(),n.getY(),base);if(d<range){range=d;best=n;}}return best;}
        void claim(){NPC n=nearest(true);if(n==null)return;Prey type=kind(n.getNpcId());if(owner.playerLevel[21]<type.level)return;prey=type;target=n;oldWalking=n.randomWalk;n.randomWalk=false;CLAIMS.put(n,this);}
        Position approach(NPC n){Position size=base.getObjectSize();int w=Math.max(1,size.getX()),h=Math.max(1,size.getY());int px=Math.max(base.x,Math.min(base.x+w-1,n.getX())),py=Math.max(base.y,Math.min(base.y+h-1,n.getY()));if(n.getX()<base.x)px=base.x-1;else if(n.getX()>=base.x+w)px=base.x+w;if(n.getY()<base.y)py=base.y-1;else if(n.getY()>=base.y+h)py=base.y+h;return new Position(px,py,0);}
        void release(){if(target!=null&&CLAIMS.get(target)==this){CLAIMS.remove(target);target.randomWalk=oldWalking;}target=null;}
        void collect(){if(!caught||prey==null||!usable(owner)||owner.playerLevel[21]<prey.level)return;
            owner.interruptActions();owner.getPA().stopSkilling();
            if(!ProductionBatch.exchange(owner,new int[0][],new int[][]{{526,1},{prey.product,1}}))return;
            owner.getPA().addSkillXPMultiplied(prey.xp,21,true);owner.startAnimation(827);close(false);
        }
        void close(boolean dismantled){if(closed)return;closed=true;release();OWNERS.remove(owner,this);SITES.remove(key(base.x,base.y),this);
            if(model!=null&&Server.getGlobalObjects().removeIfCurrent(model)){Region r=RegionProvider.getGlobal().get(base.x,base.y);r.addObject(base.id,base.x,base.y,0,base.type,base.face);r.addWorldObject(base);Server.getGlobalObjects().updateObject(base.toGlobalObject(),base.id);}
            if(dismantled)owner.sendMessage("You dismantle your deadfall.");
        }
    }
}
