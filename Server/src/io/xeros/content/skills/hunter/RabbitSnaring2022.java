package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.*;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** Native rabbit-hole routes, snares and ground-level rabbit graphics.
 * The two-tick actions, three tiles/tick chase, 100-tick trap/hole periods and
 * 1/16 ferret escape roll are local timings/odds pending historical certification.
 * These values are disclosed approximations, not original-server constants. */
public final class RabbitSnaring2022 {
    public static final Random RANDOM=new Random();
    private static final int[][] HOLES={
        {2323,3533},{2328,3524},{2329,3537},{2336,3525},{2337,3535},{2339,3530},
        {2325,3608},{2328,3612},{2329,3604},{2334,3605},{2334,3613},{2338,3608},
        {2308,3633},{2313,3628},{2314,3639},{2320,3627},{2322,3638},{2326,3632}
    };
    private static final Map<Long,Snare> SITES=new HashMap<>();
    private static final Map<Player,Set<Snare>> OWNERS=new IdentityHashMap<>();
    private static final Map<Integer,Chase> RUNS=new HashMap<>();
    private static final Set<Chase> CHASES=Collections.newSetFromMap(new IdentityHashMap<>());
    private RabbitSnaring2022(){}
    private static long key(int x,int y){return ((long)x<<32)|(y&0xffffffffL);}
    private static boolean ready(Player p){return p.getUtilityMagic().canInteract()&&p.getTeleportToX()==-1&&p.getTeleportToY()==-1&&!p.getMovementState().isLocked()&&!p.getInterfaceEvent().isActive()&&!p.getBankPin().requiresUnlock()&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private static boolean unlocked(Player p){return p.playerLevel[21]>=27&&p.getQuesting().getEaglesPeak().getStage()>=8;}
    private static boolean access(Player p){if(p.playerLevel[21]<27){p.sendMessage("You need Hunter level 27 to snare rabbits.");return false;}if(p.getQuesting().getEaglesPeak().getStage()<8){p.sendMessage("Complete Eagles' Peak before hunting rabbits.");return false;}return true;}
    private static int group(int x,int y){for(int i=0;i<HOLES.length;i++)if(HOLES[i][0]==x&&HOLES[i][1]==y)return i/6;return -1;}
    private static boolean near(Player p,int x,int y,int distance){return p.heightLevel==0&&Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))<=distance;}
    private static boolean clear(Player p,int x,int y,Snare own){
        if(SITES.containsKey(key(x,y))&&SITES.get(key(x,y))!=own)return false;
        Region r=p.getRegionProvider().get(x,y);int terrain=r.getTerrainFlags(x,y,0);
        return terrain>=0&&(terrain&4)==0&&(r.getClip(x,y,0)&0x1280100)==0&&!Server.getGlobalObjects().anyExists(x,y,0)
            &&r.worldObjectsSnapshot().stream().noneMatch(o->o.x==x&&o.y==y&&o.height==0&&o.type>=9&&o.type<=21);
    }
    public static int activeCount(Player p){Set<Snare> snares=OWNERS.get(p);return snares==null?0:snares.size();}
    public static void logout(Player p){Set<Snare> snares=OWNERS.get(p);if(snares!=null)for(Snare s:new ArrayList<>(snares))s.close(true);for(Chase c:new ArrayList<>(CHASES))if(c.owner==p)c.close();}
    public static boolean lay(Player p){return lay(p,null);}
    private static boolean lay(Player p,GroundItem ground){
        if(!ready(p)||!access(p))return false;
        if(ground==null&&!p.getItems().playerHasItem(10031))return false;
        int limit=Math.min(Hunter.getMaximumTraps(p),Math.max(1,Math.min(99,p.playerLevel[21]))/20+1);
        if(Hunter.activeTrapCount(p)>=limit){p.sendMessage("You have already set your maximum number of Hunter traps.");return false;}
        if(!clear(p,p.absX,p.absY,null)){p.sendMessage("You cannot lay a snare here.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.newWalkCmdSteps=0;p.startAnimation(5208);
        Snare s=new Snare(p,ground);SITES.put(key(s.x,s.y),s);OWNERS.computeIfAbsent(p,q->Collections.newSetFromMap(new IdentityHashMap<>())).add(s);s.start();return true;
    }
    public static boolean ground(Player p,int item,int x,int y){
        if(item!=10031)return false;if(!ready(p))return true;
        GroundItem floor=Server.itemHandler.getGroundItem(p,item,x,y,0);if(floor==null||!floor.isOwner(p))return true;
        if(p.absX==x&&p.absY==y){lay(p,floor);return true;}
        if(!near(p,x,y,16))return true;
        PathFinder.getPathFinder().findRoute(p,x,y,false,0,0);long token=p.productionActionToken;
        p.setTickable((container,actor)->{if(token!=actor.productionActionToken||!ready(actor)){container.stop();return;}if(actor.newWalkCmdSteps>0||!actor.isWalkingQueueEmpty())return;container.stop();if(actor.absX==x&&actor.absY==y)ground(actor,item,x,y);});return true;
    }
    public static boolean click(Player p,WorldObject o,int option){
        if(o.id==19337||o.id==19338){if(option==1&&o.id==19337)flush(p,o);return true;}
        Snare s=SITES.get(key(o.x,o.y));if(s==null||o.height!=0)return false;
        if(s.owner!=p){p.sendMessage("This snare belongs to someone else.");return true;}
        s.click(o,option);return true;
    }
    public static boolean use(Player p,int item,int object,int x,int y){
        if(object!=19337&&object!=19338)return false;
        if(item==10092){WorldObject o=p.getRegionProvider().get(x,y).getWorldObject(object,x,y,p.heightLevel).orElse(null);if(o!=null&&object==19337)flush(p,o);}return true;
    }
    private static void flush(Player p,WorldObject hole){
        int group=group(hole.x,hole.y);
        if(!ready(p)||!access(p)||group<0||hole.height!=0||hole.type!=10||!near(p,hole.x,hole.y,1)
            ||!p.getRegionProvider().get(hole.x,hole.y).getWorldObject(19337,hole.x,hole.y,0).filter(hole::equals).isPresent())return;
        if(!p.getItems().playerHasItem(10092)){p.sendMessage("You need a ferret to flush out a rabbit.");return;}
        if(RUNS.containsKey(group)){p.sendMessage("A ferret is already flushing this warren.");return;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.newWalkCmdSteps=0;p.facePosition(hole.x,hole.y);p.startAnimation(5208);
        Chase chase=new Chase(p,hole,group);RUNS.put(group,chase);CHASES.add(chase);chase.start();
    }
    /** A rasterized straight route includes every tile crossed between the two hole anchors. */
    static List<Position> route(int x,int y,int tx,int ty){
        List<Position> tiles=new ArrayList<>();int dx=Math.abs(tx-x),dy=-Math.abs(ty-y),sx=x<tx?1:-1,sy=y<ty?1:-1,error=dx+dy;
        while(x!=tx||y!=ty){int twice=error*2;if(twice>=dy){error+=dy;x+=sx;}if(twice<=dx){error+=dx;y+=sy;}tiles.add(new Position(x,y,0));}return tiles;
    }
    private enum State { SETTING,OPEN,FULL,BROKEN,CLOSED }
    private static final class Snare {
        final Player owner;final GroundItem floor;final int x,y;final long token;State state=State.SETTING;GlobalObject object;int age,life=100;boolean placed,pending;
        Snare(Player p,GroundItem floor){owner=p;this.floor=floor;x=p.absX;y=p.absY;token=p.productionActionToken;}
        void start(){Server.getEventHandler().submit(new Event<Snare>("rabbit-snare",this,1){public void execute(){if(!tick())stop();}public void stop(){super.stop();if(state!=State.CLOSED)close(true);}});}
        boolean current(int id){return object!=null&&object.getObjectId()==id&&RegionProvider.getGlobal().get(x,y).getWorldObject(id,x,y,0).filter(o->o.type==10&&o.face==0).isPresent();}
        boolean morph(int id){if(object!=null&&!Server.getGlobalObjects().removeIfCurrent(object))return false;object=new GlobalObject(id,x,y,0,0,10,-1);Server.getGlobalObjects().add(object);return true;}
        boolean tick(){
            if(state==State.CLOSED)return false;
            if(owner.isDisconnected()||owner.getSession()==null||owner.isDead||owner.getHealth().getCurrentHealth()<=0){close(true);return false;}
            if(state==State.SETTING){
                if(token!=owner.productionActionToken||!ready(owner)||!unlocked(owner)||owner.absX!=x||owner.absY!=y){close(false);return false;}if(++age<2)return true;
                if(!clear(owner,x,y,this)){close(false);return false;}
                if(floor==null){if(!ProductionBatch.exchange(owner,new int[][]{{10031,1}},new int[0][])){close(false);return false;}}
                else{if(Server.itemHandler.getGroundItem(owner,10031,x,y,0)!=floor||!floor.isOwner(owner)){close(false);return false;}Server.itemHandler.removeGroundItem(owner,floor,false);if(floor.getAmount()>1)Server.itemHandler.createPrivateTimedGroundItem(owner,new GameItem(10031,floor.getAmount()-1),new Position(x,y,0),100);}
                placed=true;if(!morph(19333)){close(true);return false;}state=State.OPEN;stepOff();return true;
            }
            if(!current(object.getObjectId())){close(true);return false;}
            if(--life<=0){if(state==State.OPEN){if(!morph(19334)){close(true);return false;}state=State.BROKEN;life=100;}else{close(true);return false;}}return true;
        }
        void stepOff(){if(owner.absX!=x||owner.absY!=y||owner.heightLevel!=0||owner.freezeDelay>0)return;for(int[] d:new int[][]{{-1,0},{1,0},{0,-1},{0,1}})if(owner.getRegionProvider().getClipping(x+d[0],y+d[1],0,d[0],d[1])){owner.addToWalkingQueue(x+d[0]-owner.mapRegionX*8,y+d[1]-owner.mapRegionY*8);return;}}
        boolean catches(Chase run,Position tile){return state==State.OPEN&&!pending&&owner==run.owner&&x==tile.getX()&&y==tile.getY()&&unlocked(owner)&&!(owner.absX==x&&owner.absY==y&&owner.heightLevel==0)&&current(19333);}
        void caught(){if(morph(19335)){state=State.FULL;life=100;owner.sendMessage("Your snare has caught a white rabbit.");}else close(true);}
        void click(WorldObject clicked,int option){
            if(!ready(owner)||!near(owner,x,y,1)||!current(clicked.id)||pending||state==State.SETTING||state==State.CLOSED)return;
            if(option==2&&state==State.OPEN){owner.sendMessage("The snare is waiting for a rabbit.");return;}if(option!=1)return;
            State expected=state;int[][] outputs=state==State.FULL?new int[][]{{10031,1},{526,1},{3226,1},{10134,1}}:new int[][]{{10031,1}};
            if(!ProductionBatch.canExchange(owner,new int[0][],outputs)){owner.sendMessage("Leave room for the snare and its catch.");return;}
            owner.interruptActions();owner.getPA().stopSkilling();owner.getPA().removeAllWindows();owner.stopMovement();owner.startAnimation(5207);long action=owner.productionActionToken;int px=owner.absX,py=owner.absY;pending=true;
            Server.getEventHandler().submit(new Event<Snare>("rabbit-snare-check",this,2){public void execute(){if(state==expected&&action==owner.productionActionToken&&ready(owner)&&near(owner,x,y,1)&&owner.absX==px&&owner.absY==py&&current(clicked.id)&&ProductionBatch.exchange(owner,new int[0][],outputs)){if(expected==State.FULL)owner.getPA().addSkillXPMultiplied(144,21,true);close(false);}stop();}public void stop(){super.stop();pending=false;}});
        }
        void close(boolean drop){if(state==State.CLOSED)return;state=State.CLOSED;SITES.remove(key(x,y),this);Set<Snare> own=OWNERS.get(owner);if(own!=null){own.remove(this);if(own.isEmpty())OWNERS.remove(owner);}if(object!=null)Server.getGlobalObjects().removeIfCurrent(object);if(drop&&placed)Server.itemHandler.createPrivateTimedGroundItem(owner,new GameItem(10031,1),new Position(x,y,0),100);placed=false;}
    }
    private static final class Chase {
        final Player owner;final WorldObject clicked;final int group,px,py;final long token;int age,index,phase;boolean closed;WorldObject origin;GlobalObject empty;List<Position> path;Position at;
        Chase(Player p,WorldObject o,int group){owner=p;clicked=o;this.group=group;px=p.absX;py=p.absY;token=p.productionActionToken;}
        void start(){Server.getEventHandler().submit(new Event<Chase>("rabbit-warren",this,1){public void execute(){if(!tick())stop();}public void stop(){super.stop();close();}});}
        boolean tick(){
            if(closed)return false;if(owner.isDisconnected()||owner.isDead||owner.getSession()==null||owner.heightLevel!=0||!unlocked(owner)){close();return false;}
            if(phase==0){
                if(token!=owner.productionActionToken||!ready(owner)||owner.absX!=px||owner.absY!=py||!owner.getItems().playerHasItem(10092)){close();return false;}if(++age<2)return true;
                List<Integer> occupied=new ArrayList<>();for(int i=0;i<6;i++){int[] h=HOLES[group*6+i];if(owner.getRegionProvider().get(h[0],h[1]).getWorldObject(19337,h[0],h[1],0).isPresent()&&!Server.getGlobalObjects().anyExists(h[0],h[1],0))occupied.add(i);}
                if(occupied.isEmpty()){owner.sendMessage("There are no rabbits left in these holes for the moment.");close();return false;}
                int first=occupied.get(RANDOM.nextInt(occupied.size())),second=RANDOM.nextInt(5);if(second>=first)second++;
                int[] a=HOLES[group*6+first],b=HOLES[group*6+second];origin=owner.getRegionProvider().get(a[0],a[1]).getWorldObject(19337,a[0],a[1],0).orElse(null);
                if(origin==null||Server.getGlobalObjects().anyExists(a[0],a[1],0)){close();return false;}
                path=route(a[0],a[1],b[0],b[1]);at=origin.getPosition();
                Region region=RegionProvider.getGlobal().get(origin.x,origin.y);
                region.removeObject(origin.id,origin.x,origin.y,0,origin.type,origin.face);region.removeWorldObject(origin.toGlobalObject());
                empty=new GlobalObject(19338,a[0],a[1],0,origin.face,origin.type,-1);Server.getGlobalObjects().add(empty);
                owner.getPA().stillGfx(943,clicked.x,clicked.y,0,0);owner.getPA().stillGfx(944,a[0],a[1],0,0);
                if(RANDOM.nextInt(16)==0){owner.getItems().deleteItem2(10092,1);owner.sendMessage("Your ferret escapes into the undergrowth.");}
                phase=1;age=0;return true;
            }
            if(phase==1){
                Position from=at;Snare caught=null;
                for(int steps=0;steps<3&&index<path.size();steps++){
                    at=path.get(index++);Snare snare=SITES.get(key(at.getX(),at.getY()));if(snare!=null&&snare.catches(this,at)){caught=snare;break;}
                }
                if(!from.equals(at))new Projectile(from,at,0,new ProjectileBase(945,0,30,0,0,0,0,0,0)).send(null);
                if(caught!=null){caught.caught();phase=2;age=0;RUNS.remove(group,this);}
                else if(index==path.size()){owner.getPA().stillGfx(946,at.getX(),at.getY(),0,0);phase=2;age=0;RUNS.remove(group,this);}
                return true;
            }
            if(++age>=100){close();return false;}return true;
        }
        void close(){if(closed)return;closed=true;RUNS.remove(group,this);CHASES.remove(this);if(empty!=null&&Server.getGlobalObjects().removeIfCurrent(empty)){
            Region r=RegionProvider.getGlobal().get(origin.x,origin.y);r.addObject(origin.id,origin.x,origin.y,0,origin.type,origin.face);r.addWorldObject(origin);Server.getGlobalObjects().updateObject(empty,origin.id);
        }}
    }
}
