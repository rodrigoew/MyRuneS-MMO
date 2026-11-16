package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.GameItem;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** A natural young tree and the adjacent net remain reserved until their owner clears them. */
public final class NetTrapHunting {
    public enum Species {
        SWAMP(2906,29,152,10149,10142,52,360,9341,9342,9257,9343,9003,9004,9005,9158),
        ORANGE(2903,47,224,10146,10143,16,288,8732,8733,8730,8731,8972,8734,8974,8973),
        RED(2904,59,272,10147,10144,0,240,8990,8991,8989,8992,8985,8986,8987,8988),
        BLACK(2905,67,319.2,10148,10145,0,212,9000,9001,8999,9002,8993,8996,8997,8998);
        public final int npc,level,product,bait,low,high,up,setting,bent,net,catching,full,failing,failed;public final double xp;
        Species(int npc,int level,double xp,int product,int bait,int low,int high,int up,int setting,int bent,int net,int catching,int full,int failing,int failed){this.npc=npc;this.level=level;this.xp=xp;this.product=product;this.bait=bait;this.low=low;this.high=high;this.up=up;this.setting=setting;this.bent=bent;this.net=net;this.catching=catching;this.full=full;this.failing=failing;this.failed=failed;}
        public int threshold(int hunter,boolean baited,boolean smoked){hunter=Math.max(1,Math.min(126,hunter));int base=low*(99-hunter)/98+high*(hunter-1)/98+1;return Math.max(0,Math.min(25600,base*100+(baited?768:0)+(smoked?512:0)));}
        public static Species tree(int id){for(Species s:values())if(s.up==id)return s;return null;}
    }
    private static final Set<WorldObject> NATIVE_TREES=Set.of(
        new WorldObject(9341,3532,3447,0,10,2),
        new WorldObject(9341,3536,3451,0,10,1),
        new WorldObject(9341,3538,3445,0,10,3),
        new WorldObject(9341,3548,3437,0,10,1),
        new WorldObject(9341,3548,3442,0,10,3),
        new WorldObject(9341,3550,3449,0,10,3),
        new WorldObject(9341,3553,3450,0,10,3),
        new WorldObject(9341,3553,3453,0,10,1),
        new WorldObject(9341,3554,3441,0,10,2),
        new WorldObject(9341,3557,3438,0,10,3),
        new WorldObject(9341,3557,3444,0,10,1),
        new WorldObject(9341,3562,3434,0,10,0),
        new WorldObject(9341,3677,3403,0,10,2),
        new WorldObject(9341,3677,3417,0,10,3),
        new WorldObject(9341,3680,3413,0,10,1),
        new WorldObject(9341,3681,3409,0,10,0),
        new WorldObject(9341,3682,3406,0,10,0),
        new WorldObject(9341,3683,3402,0,10,3),
        new WorldObject(9341,3684,3412,0,10,0),
        new WorldObject(9341,3694,3412,0,10,3),
        new WorldObject(9341,3697,3406,0,10,3),
        new WorldObject(9341,3697,3410,0,10,0),
        new WorldObject(9341,3700,3401,0,10,1),
        new WorldObject(9341,3702,3405,0,10,2),
        new WorldObject(9341,3704,3402,0,10,0),
        new WorldObject(8732,3403,3100,0,10,1),
        new WorldObject(8732,3404,3092,0,10,3),
        new WorldObject(8732,3404,3134,0,10,3),
        new WorldObject(8732,3406,3134,0,10,2),
        new WorldObject(8732,3407,3088,0,10,1),
        new WorldObject(8732,3407,3093,0,10,0),
        new WorldObject(8732,3410,3081,0,10,1),
        new WorldObject(8732,3412,3075,0,10,0),
        new WorldObject(8732,3414,3078,0,10,1),
        new WorldObject(8732,3415,3073,0,10,2),
        new WorldObject(8990,2447,3225,0,10,0),
        new WorldObject(8990,2449,3228,0,10,2),
        new WorldObject(8990,2451,3225,0,10,3),
        new WorldObject(8990,2453,3219,0,10,0),
        new WorldObject(8990,2463,3251,0,10,1),
        new WorldObject(8990,2467,3242,0,10,1),
        new WorldObject(8990,2472,3238,0,10,0),
        new WorldObject(8990,2475,3236,0,10,1),
        new WorldObject(9000,3294,3669,0,10,1),
        new WorldObject(9000,3294,3672,0,10,1),
        new WorldObject(9000,3295,3677,0,10,2),
        new WorldObject(9000,3296,3666,0,10,0),
        new WorldObject(9000,3298,3674,0,10,3),
        new WorldObject(9000,3299,3670,0,10,3)
    );
    public static final Random RANDOM=new Random();
    private static final Map<Long,Trap> SITES=new HashMap<>();
    private static final Map<Player,Set<Trap>> OWNERS=new IdentityHashMap<>();
    private static final Map<NPC,Trap> CLAIMS=new IdentityHashMap<>();
    private NetTrapHunting(){}
    private static long key(int x,int y){return (long)x<<32|y&0xffffffffL;}
    private static boolean ready(Player p){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private static boolean live(WorldObject o){return RegionProvider.getGlobal().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,0).filter(o::equals).isPresent();}
    private static boolean registered(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isUnregister()&&!n.isDead()&&!n.isInvisible()&&!n.needRespawn&&n.getInstance()==null&&n.heightLevel==0;}
    private static int distance(int x,int y,WorldObject o){Position size=o.getObjectSize();return Math.max(Math.max(o.x-x,Math.max(0,x-o.x-Math.max(1,size.getX())+1)),Math.max(o.y-y,Math.max(0,y-o.y-Math.max(1,size.getY())+1)));}
    public static int activeCount(Player p){Set<Trap> traps=OWNERS.get(p);return traps==null?0:traps.size();}
    public static void logout(Player p){Set<Trap> traps=OWNERS.get(p);if(traps!=null)for(Trap t:new ArrayList<>(traps))t.close(Close.GROUND);}
    /** Living, unnoted salamanders are released rather than put on the floor. */
    public static boolean release(Player p,int id,int slot,int container){
        Species species=null;for(Species s:Species.values())if(s.product==id){species=s;break;}if(species==null)return false;
        if(container!=3214||!p.getItems().isItemInInventorySlot(id,slot)||!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||Server.getMultiplayerSessionListener().inAnySession(p))return true;
        if(p.getBankPin().requiresUnlock()){p.getBankPin().open(2);return true;}
        if(p.getInterfaceEvent().isActive()){p.sendMessage("Please finish what you're doing.");return true;}
        p.getItems().deleteItem(id,slot,1);p.sendMessage("You release the "+io.xeros.model.definitions.ItemDef.forId(id).getName().toLowerCase()+".");return true;
    }
    private enum Close { NONE,GROUND }
    /** A pending installation already occupies one of the shared Hunter trap slots. */
    public static boolean click(Player p,WorldObject o,int option){
        if(o==null||o.height!=0)return false;Trap t=SITES.get(key(o.x,o.y));
        if(t!=null){if(!ready(p)||distance(p.absX,p.absY,o)>1||!t.current(o))return true;if(t.owner!=p){p.sendMessage("Someone else has set this net trap.");return true;}
            if(option==2){p.sendMessage(t.caught?"A salamander has been caught in the net.":"The net trap is waiting."+(t.baited?" It has bait.":"")+(t.smoked?" It has been smoked.":""));return true;}
            if(option==1){if(t.caught)t.collect();else if(t.placed)t.dismantle();}return true;
        }
        Species species=Species.tree(o.id);if(species==null)return false;
        if(option!=1||!ready(p)||o.type!=10||!live(o)||distance(p.absX,p.absY,o)>1)return true;
        if(!NATIVE_TREES.contains(o))return true;
        if(p.playerLevel[21]<species.level){p.sendMessage("You need Hunter level "+species.level+" to set this net trap.");return true;}
        if(!p.getItems().playerHasItem(303)||!p.getItems().playerHasItem(954)){p.sendMessage("You need a small fishing net and a rope to set this trap.");return true;}
        if(Hunter.activeTrapCount(p)>=Hunter.getMaximumTraps(p)){p.sendMessage("You have already set the maximum number of traps for your Hunter level.");return true;}
        int face=o.face&3,dx=face==1?1:face==3?-1:0,dy=face==0?1:face==2?-1:0;int nx=o.x+dx,ny=o.y+dy;
        if(SITES.containsKey(key(nx,ny))||(p.getRegionProvider().getClipping(nx,ny,0)&0x1280100)!=0){p.sendMessage("There is no room for the net beside this tree.");return true;}
        // The second square may contain ground decoration, but never another solid object.
        if(p.getRegionProvider().get(nx,ny).worldObjectsSnapshot().stream().anyMatch(v->v.x==nx&&v.y==ny&&v.height==0&&v.type==10)){p.sendMessage("There is no room for the net beside this tree.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.attacking.reset();
        t=new Trap(p,o,species,nx,ny);OWNERS.computeIfAbsent(p,q->Collections.newSetFromMap(new IdentityHashMap<>())).add(t);SITES.put(key(o.x,o.y),t);SITES.put(key(nx,ny),t);t.begin();return true;
    }
    public static boolean use(Player p,int item,int id,int x,int y){Trap t=SITES.get(key(x,y));if(t==null)return false;
        if(t.owner!=p||!ready(p)||!t.placed||t.caught||t.transition>0||!t.current(new WorldObject(id,x,y,0,t.base.type,t.base.face))||distance(p.absX,p.absY,t.base)>2)return true;
        if(item==594){if(!HunterTorch.canSmoke(p))return true;p.interruptActions();p.startAnimation(5213);t.smoked=true;p.sendMessage("You smoke the net trap.");return true;}
        if(item==t.species.bait){if(!t.baited&&ProductionBatch.exchange(p,new int[][]{{item,1}},new int[0][])){p.interruptActions();p.startAnimation(5217);t.baited=true;p.sendMessage("You add the salamander's preferred tar to the trap.");}return true;}
        p.sendMessage("That is not suitable bait for this net trap.");return true;
    }
    private static final class Trap {
        final Player owner;final WorldObject base;final Species species;final int nx,ny,ax,ay,x,y;final long token;
        GlobalObject tree,net,whole;NPC target;boolean oldWalking,oldWalkHome,placed,caught,closed,baited,smoked;int age,transition;
        Trap(Player owner,WorldObject base,Species species,int nx,int ny){this.owner=owner;this.base=base;this.species=species;this.nx=nx;this.ny=ny;ax=Math.min(nx,base.x);ay=Math.min(ny,base.y);x=owner.absX;y=owner.absY;token=owner.productionActionToken;}
        void begin(){owner.facePosition(base.x,base.y);owner.startAnimation(5215);if(!replaceTree(species.setting)){close(Close.NONE);return;}Server.playerHandler.sendObjectAnimation(tree,5266);
            Server.getEventHandler().submit(new Event<Trap>("net-trap",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();if(!closed)close(Close.GROUND);}});
        }
        boolean current(GlobalObject object){return object!=null&&live(new WorldObject(object.getObjectId(),object.getX(),object.getY(),0,object.getType(),object.getFace()));}
        boolean current(WorldObject o){return o.height==0&&o.type==10&&((tree!=null&&o.id==tree.getObjectId()&&o.x==base.x&&o.y==base.y&&o.face==base.face&&current(tree))||(net!=null&&o.id==net.getObjectId()&&o.x==nx&&o.y==ny&&o.face==base.face&&current(net))||(whole!=null&&o.id==whole.getObjectId()&&o.x==ax&&o.y==ay&&o.face==base.face&&current(whole)));}
        boolean replaceTree(int id){Region region=RegionProvider.getGlobal().get(base.x,base.y);if(tree==null){if(!live(base))return false;region.removeObject(base.id,base.x,base.y,0,base.type,base.face);region.removeWorldObject(base.toGlobalObject());}else if(!Server.getGlobalObjects().removeIfCurrent(tree))return false;tree=new GlobalObject(id,base.x,base.y,0,base.face,10,-1);Server.getGlobalObjects().add(tree);return true;}
        boolean replaceWhole(int id){if(whole!=null){if(!Server.getGlobalObjects().removeIfCurrent(whole))return false;}else {if(!current(tree)||!current(net))return false;Server.getGlobalObjects().removeIfCurrent(tree);Server.getGlobalObjects().removeIfCurrent(net);tree=net=null;}whole=new GlobalObject(id,ax,ay,0,base.face,10,-1);Server.getGlobalObjects().add(whole);return true;}
        boolean step(){if(closed)return false;age++;
            if(owner.isDisconnected()||owner.getSession()==null||owner.getRegionProvider()!=RegionProvider.getGlobal()||owner.heightLevel!=0||owner.isDead||Math.max(Math.abs(owner.absX-base.x),Math.abs(owner.absY-base.y))>64)return false;
            if(!placed){if(token!=owner.productionActionToken||owner.absX!=x||owner.absY!=y||!ready(owner)||owner.playerLevel[21]<species.level||!current(tree))return false;if(age<3)return true;
                if(!ProductionBatch.exchange(owner,new int[][]{{303,1},{954,1}},new int[0][]))return false;placed=true;
                if(!replaceTree(species.bent))return false;net=new GlobalObject(species.net,nx,ny,0,base.face,10,-1);Server.getGlobalObjects().add(net);owner.startAnimation(65535);return true;}
            if(transition>0){if(whole==null||!current(whole))return false;if(--transition>0)return true;if(caught)return replaceWhole(species.full);dropTools();return false;}
            if(caught)return current(whole);
            if(!current(tree)||!current(net))return false;
            if(age>=100){owner.sendMessage("The net trap has collapsed.");return false;}
            if(target==null){if(age%4==0)claim();return true;}
            if(!registered(target)||CLAIMS.get(target)!=this||owner.playerLevel[21]<species.level){release();return true;}
            int d=Math.max(Math.abs(target.absX-nx),Math.abs(target.absY-ny));if(d==0){if(owner.absX==nx&&owner.absY==ny)return true;boolean success=RANDOM.nextInt(25600)<species.threshold(owner.playerLevel[21],baited,smoked);if(!replaceWhole(success?species.catching:species.failing))return false;
                if(success){target.resetWalkingQueue();target.setDead(true);target.applyDead=true;target.needRespawn=true;target.setInvisible(true);target.actionTimer=5;target.setUpdateRequired(true);caught=true;owner.sendMessage("A salamander has been caught in your net.");}else owner.sendMessage("The salamander slips past your net.");release();transition=3;return true;}
            NPCDumbPathFinder.walkTowards(target,nx,ny);return true;
        }
        void claim(){NPC best=null;int distance=9;for(NPC n:NPCHandler.npcs)if(registered(n)&&n.getNpcId()==species.npc&&!CLAIMS.containsKey(n)){int d=Math.max(Math.abs(n.absX-nx),Math.abs(n.absY-ny));if(d<distance){best=n;distance=d;}}if(best==null)return;target=best;oldWalking=best.randomWalk;oldWalkHome=best.getBehaviour().isWalkHome();best.randomWalk=false;best.getBehaviour().setWalkHome(false);CLAIMS.put(best,this);}
        void release(){if(target!=null&&CLAIMS.get(target)==this){CLAIMS.remove(target);target.randomWalk=oldWalking;target.getBehaviour().setWalkHome(oldWalkHome);}target=null;}
        void collect(){if(!caught||transition>0||!current(whole)||!ready(owner)||owner.playerLevel[21]<species.level)return;owner.interruptActions();owner.getPA().stopSkilling();if(!ProductionBatch.exchange(owner,new int[0][],new int[][]{{303,1},{954,1},{species.product,1}}))return;owner.startAnimation(5207);owner.getPA().addSkillXPMultiplied(species.xp,21,true);placed=false;close(Close.NONE);}
        void dismantle(){if(transition>0)return;if(!ProductionBatch.exchange(owner,new int[0][],new int[][]{{303,1},{954,1}}))return;owner.interruptActions();owner.startAnimation(5207);placed=false;close(Close.NONE);}
        void dropTools(){if(!placed)return;placed=false;for(int id:new int[]{303,954})Server.itemHandler.createPrivateTimedGroundItem(owner,new GameItem(id,1),new Position(nx,ny,0),100);}
        void close(Close reason){if(closed)return;closed=true;release();Set<Trap> traps=OWNERS.get(owner);if(traps!=null){traps.remove(this);if(traps.isEmpty())OWNERS.remove(owner);}SITES.remove(key(base.x,base.y),this);SITES.remove(key(nx,ny),this);
            boolean restore=false;if(whole!=null)restore=Server.getGlobalObjects().removeIfCurrent(whole);if(tree!=null)restore=Server.getGlobalObjects().removeIfCurrent(tree)||restore;if(net!=null)Server.getGlobalObjects().removeIfCurrent(net);
            if(restore){Region region=RegionProvider.getGlobal().get(base.x,base.y);region.addObject(base.id,base.x,base.y,0,base.type,base.face);region.addWorldObject(base);Server.getGlobalObjects().updateObject(base.toGlobalObject(),base.id);}
            if(reason!=Close.NONE)dropTools();
        }
    }
}
