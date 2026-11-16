package io.xeros.content.skills.hunter;

import io.xeros.Configuration;
import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.woodcutting.HistoricalTreeData;
import io.xeros.model.ForceMovement;
import io.xeros.model.Direction;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.content.combat.Hitmark;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.tickable.impl.WalkToTickable;
import java.util.*;

/** The original two-square pits and their four scenery pieces share native packed states. */
public final class PitfallHunting {
    public enum Species {
        LARUPIA(2908,31,180,10093,10095,5),
        GRAAHK(2909,41,240,10097,10099,7),
        KYATT(2907,55,300,10101,10103,7);
        public final int npc,level,xp,tatty,fur,maxHit;
        Species(int npc,int level,int xp,int tatty,int fur,int maxHit){this.npc=npc;this.level=level;this.xp=xp;this.tatty=tatty;this.fur=fur;this.maxHit=maxHit;}
        public static Species forNpc(int id){for(Species s:values())if(s.npc==id)return s;return null;}
        /** Local interpolation except for the published graahk guarantee above level 85. */
        public int catchChance(int level){return this==GRAAHK&&level>85?100:Math.min(95,60+Math.max(0,Math.min(99,level)-this.level)*35/Math.max(1,99-this.level));}
        /** The 2022 sources do not publish a numerical good-fur curve. */
        public int goodFurChance(int level){return Math.min(45,25+Math.max(0,Math.min(99,level)-this.level)/3);}
    }
    private static final class Site {
        final WorldObject object;final Species species;final int parent,shift;
        Site(int id,int x,int y,int face,int bit,Species species){object=new WorldObject(id,x,y,0,10,face);this.species=species;parent=bit<2968?917:918;shift=(bit-(parent==917?2958:2968))*3;}
        boolean horizontal(){return (object.face&1)!=0;}
        Position edge(boolean low){return new Position(object.x+(horizontal()?(low?-1:2):0),object.y+(horizontal()?0:(low?-1:2)),0);}
        Position pad(boolean low){return new Position(object.x+(horizontal()?(low?-2:2):0),object.y+(horizontal()?0:(low?-2:2)),0);}
    }
    private static final Site[] SITES={
        new Site(19253,2700,3795,3,2958,Species.KYATT),
        new Site(19255,2706,3789,2,2960,Species.KYATT),
        new Site(19254,2700,3785,2,2959,Species.KYATT),
        new Site(19256,2730,3791,3,2961,Species.KYATT),
        new Site(19257,2737,3784,3,2962,Species.KYATT),
        new Site(19258,2730,3780,2,2963,Species.KYATT),
        new Site(19264,2766,3010,2,2969,Species.GRAAHK),
        new Site(19265,2762,3005,2,2970,Species.GRAAHK),
        new Site(19266,2771,3004,1,2971,Species.GRAAHK),
        new Site(19267,2777,3001,2,2972,Species.GRAAHK),
        new Site(19268,2784,3001,1,2973,Species.GRAAHK),
        new Site(19260,2543,2908,2,2965,Species.LARUPIA),
        new Site(19261,2552,2904,1,2966,Species.LARUPIA),
        new Site(19259,2556,2893,0,2964,Species.LARUPIA),
        new Site(19262,2565,2888,1,2967,Species.LARUPIA),
        new Site(19263,2573,2885,2,2968,Species.LARUPIA)
    };
    public static final Random RANDOM=new Random();
    private static final Map<Site,Trap> TRAPS=new IdentityHashMap<>();
    private static final Map<Player,Chase> HUNTERS=new IdentityHashMap<>();
    private static final Map<NPC,Chase> CLAIMS=new IdentityHashMap<>();
    private static final Map<NPC,Site> LAST_SKIPPED=new WeakHashMap<>();
    private PitfallHunting(){}
    private static boolean ready(Player p){return p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0&&!Server.getMultiplayerSessionListener().inAnySession(p);}
    private static boolean live(WorldObject o){return RegionProvider.getGlobal().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,0).filter(o::equals).isPresent();}
    private static boolean registered(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isUnregister()&&!n.isDead()&&!n.isInvisible()&&!n.needRespawn&&n.getInstance()==null&&n.heightLevel==0;}
    private static boolean contact(NPC n,Player p){
        int x=Math.max(n.absX,Math.min(n.absX+1,p.absX)),y=Math.max(n.absY,Math.min(n.absY+1,p.absY));
        // Check walls from the nearest edge, ignoring the creature's own NPC occupancy flags.
        return PathChecker.isMeleePathClear(p,x,y,0,p.absX,p.absY);
    }
    private static int distance(Player p,WorldObject o){return Math.max(Math.max(o.x-p.absX,Math.max(0,p.absX-o.x-1)),Math.max(o.y-p.absY,Math.max(0,p.absY-o.y-1)));}
    private static boolean free(Position p,int size){for(int x=0;x<size;x++)for(int y=0;y<size;y++)if((RegionProvider.getGlobal().getClipping(p.getX()+x,p.getY()+y,0)&0x1280100)!=0)return false;return true;}
    private static boolean stepFree(NPC n,int x,int y,int dx,int dy){
        int direction=NPCClipping.getDirection(x,y,x+dx,y+dy);if(direction<0)return false;
        for(int i=0;i<2;i++)for(int j=0;j<2;j++){
            int tx=x+i+dx,ty=y+j+dy;
            if(!n.getRegionProvider().canMove(x+i,y+j,0,direction,false))return false;
            boolean own=tx>=n.absX&&tx<n.absX+2&&ty>=n.absY&&ty<n.absY+2;
            if(!own&&n.getRegionProvider().isOccupiedByNpc(tx,ty,0))return false;
        }return true;
    }
    /** A two-square creature must clear each edge, not raycast its anchor towards its centre. */
    private static void walk(NPC n,int tx,int ty){
        n.resetWalkingQueue();int sx=n.absX,sy=n.absY;if(sx==tx&&sy==ty)return;
        int dx=Integer.signum(tx-sx),dy=Integer.signum(ty-sy);
        for(int[] d:new int[][]{{dx,dy},{dx,0},{0,dy}})if((d[0]!=0||d[1]!=0)&&stepFree(n,sx,sy,d[0],d[1])){n.setMovement(Direction.fromDeltas(n.getPosition(),new Position(sx+d[0],sy+d[1],0)));return;}
        final int radius=20,size=41;int[][] first=new int[size][size];for(int[] row:first)Arrays.fill(row,-1);
        int[][] dirs={{0,1},{1,0},{0,-1},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};ArrayDeque<int[]> queue=new ArrayDeque<>();queue.add(new int[]{sx,sy});first[radius][radius]=8;
        while(!queue.isEmpty()){
            int[] at=queue.removeFirst();int previous=first[at[0]-sx+radius][at[1]-sy+radius];
            if(at[0]==tx&&at[1]==ty){if(previous<8){int[] d=dirs[previous];n.setMovement(Direction.fromDeltas(n.getPosition(),new Position(sx+d[0],sy+d[1],0)));}return;}
            for(int k=0;k<dirs.length;k++){int[] d=dirs[k];int x=at[0]+d[0],y=at[1]+d[1],ix=x-sx+radius,iy=y-sy+radius;
                if(ix<0||iy<0||ix>=size||iy>=size||first[ix][iy]>=0||!stepFree(n,at[0],at[1],d[0],d[1]))continue;
                first[ix][iy]=previous==8?k:previous;queue.addLast(new int[]{x,y});
            }
        }
    }
    private static boolean healthyOwner(Player p,Site site){return !p.isDisconnected()&&p.getSession()!=null&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0&&Math.max(Math.abs(p.absX-site.object.x),Math.abs(p.absY-site.object.y))<=64;}
    private static Site site(WorldObject object){for(Site s:SITES)if(s.object.equals(object))return s;return null;}
    public static int activeCount(Player p){return (int)TRAPS.values().stream().filter(t->t.owner==p).count();}
    public static void logout(Player p){Chase chase=HUNTERS.get(p);if(chase!=null)chase.close();for(Trap t:new ArrayList<>(TRAPS.values()))if(t.owner==p)t.close();}
    /** Refresh only in pitfall habitats; the low fields of 918 are reused by tracking elsewhere. */
    public static void refresh(Player p){
        if(p.getRegionProvider()!=RegionProvider.getGlobal()||p.heightLevel!=0)return;
        boolean near=false;for(Site s:SITES)if(Math.max(Math.abs(p.absX-s.object.x),Math.abs(p.absY-s.object.y))<64){near=true;break;}if(!near)return;
        int a=0,b=0;for(Site s:SITES){Trap t=TRAPS.get(s);int value=t==null?0:t.state;if(s.parent==917)a|=value<<s.shift;else b|=value<<s.shift;}
        if((p.getVarpState(917)&0x3fffffff)!=a)p.getPA().sendFrame87Masked(917,0x3fffffff,a);
        if((p.getVarpState(918)&0x3ffff)!=b)p.getPA().sendFrame87Masked(918,0x3ffff,b);
    }
    private static int log(Player p){for(HistoricalTreeData.Kind k:HistoricalTreeData.Kind.values())if(k!=HistoricalTreeData.Kind.REDWOOD&&k!=HistoricalTreeData.Kind.HOLLOW&&p.getItems().playerHasItem(k.item))return k.item;return -1;}
    public static boolean click(Player p,WorldObject object,int option){
        Site site=site(object);if(site==null)return false;
        if(!ready(p)||!live(object)||distance(p,object)>1)return true;
        Trap trap=TRAPS.get(site);
        if(trap!=null){
            if(trap.owner!=p){p.sendMessage("Someone else has set this pitfall trap.");return true;}
            if(trap.state==1&&option==1)alignJump(p,trap);
            else if(trap.state!=0&&trap.state!=2&&option==2)trap.dismantle();
            return true;
        }
        if(option!=3)return true;
        if(p.playerLevel[21]<site.species.level){p.sendMessage("You need Hunter level "+site.species.level+" to set this pitfall.");return true;}
        int logs=log(p);if(!p.getItems().playerHasItem(946)||logs<0){p.sendMessage("You need a knife and logs other than redwood to set a pitfall.");return true;}
        if(Hunter.activeTrapCount(p)>=Hunter.getMaximumTraps(p)){p.sendMessage("You have already set the maximum number of Hunter traps.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.attacking.reset();
        Trap created=new Trap(p,site,logs);TRAPS.put(site,created);created.begin();return true;
    }
    /** Tease remains a close-range native NPC action, with ordinary walking to the creature. */
    public static boolean tease(Player p,NPC n){
        Species species=n==null?null:Species.forNpc(n.getNpcId());if(species==null)return false;
        if(!ready(p)||!registered(n))return true;
        if(p.playerLevel[21]<species.level){p.sendMessage("You need Hunter level "+species.level+" to tease this creature.");return true;}
        if(Arrays.stream(SITES).noneMatch(s->s.species==species&&Math.max(Math.abs(n.absX-s.object.x),Math.abs(n.absY-s.object.y))<=32))return true;
        if(!p.getItems().playerHasItem(10029)){p.sendMessage("You need a teasing stick in your inventory.");return true;}
        int d=Math.max(Math.max(n.absX-p.absX,Math.max(0,p.absX-n.absX-1)),Math.max(n.absY-p.absY,Math.max(0,p.absY-n.absY-1)));
        if(d>12)return true;
        if(d>1){PathFinder.getPathFinder().findRoute(p,n.absX,n.absY,true,2,2);p.setTickable(new WalkToTickable(p,n.getPosition(),2,2,q->tease(q,n)));return true;}
        if(!contact(n,p))return true;
        Chase other=CLAIMS.get(n);if(other!=null&&other.owner!=p){p.sendMessage("That creature is already pursuing another hunter.");return true;}
        if(other!=null){p.sendMessage("The creature is already pursuing you.");return true;}
        Chase previous=HUNTERS.get(p);if(previous!=null)previous.close();
        p.interruptActions();p.getPA().removeAllWindows();p.stopMovement();p.attacking.reset();p.facePosition(n.absX,n.absY);p.startAnimation(5236);
        Chase chase=new Chase(p,n,species);HUNTERS.put(p,chase);CLAIMS.put(n,chase);chase.begin();return true;
    }
    private static void alignJump(Player p,Trap t){
        Site s=t.site;boolean low=s.horizontal()?p.absX<s.object.x+1:p.absY<s.object.y+1;Position start=s.edge(low),end=s.edge(!low);
        if(!free(start,1)||!free(end,1)||!free(s.pad(low),2)||!free(s.pad(!low),2))return;
        if(p.absX!=start.getX()||p.absY!=start.getY()){
            PathFinder.getPathFinder().findRoute(p,start.getX(),start.getY(),false,0,0);long token=p.productionActionToken;
            p.setTickable((container,q)->{if(token!=q.productionActionToken||!ready(q)||TRAPS.get(s)!=t||t.state!=1||container.getTicks()>8){container.stop();return;}if(q.absX==start.getX()&&q.absY==start.getY()&&q.isWalkingQueueEmpty()&&q.newWalkCmdSteps==0){container.stop();jump(q,t,low);}});
        }else jump(p,t,low);
    }
    private static void jump(Player p,Trap t,boolean low){
        if(!ready(p)||TRAPS.get(t.site)!=t||t.state!=1)return;
        Site s=t.site;Position start=s.edge(low),end=s.edge(!low);if(p.absX!=start.getX()||p.absY!=start.getY()||!free(end,1))return;
        p.interruptActions();p.stopMovement();p.facePosition(end.getX(),end.getY());long token=p.productionActionToken;
        PlayerMovementState previous=p.getMovementState(),locked=new PlayerMovementState(false,false,true);p.setMovementState(locked);p.startAnimation(3067);p.showForceMovement(end.getX()-start.getX(),end.getY()-start.getY(),2);
        Server.getEventHandler().submit(new Event<Player>("pitfall-player-jump",p,2){public void execute(){
            if(token==p.productionActionToken&&healthyOwner(p,s)&&p.absX==start.getX()&&p.absY==start.getY()&&TRAPS.get(s)==t&&t.state==1){
                p.moveTo(end);Chase chase=HUNTERS.get(p);
                if(chase!=null&&chase.species==s.species){if(LAST_SKIPPED.get(chase.prey)==s)p.sendMessage("The creature refuses to jump the same pit again. Lure it over another pit.");else chase.route(t,low);}
            }stop();}
            public void stop(){super.stop();if(p.getMovementState()==locked)p.setMovementState(previous);if(p.productionActionToken==token)p.startAnimation(65535);}
        });
    }
    private static final class Trap {
        final Player owner;final Site site;final int logs,x,y;final long token;int state,age,fur;boolean closed;
        Trap(Player owner,Site site,int logs){this.owner=owner;this.site=site;this.logs=logs;x=owner.absX;y=owner.absY;token=owner.productionActionToken;}
        void begin(){owner.facePosition(site.object.x,site.object.y);owner.startAnimation(5212);
            Server.getEventHandler().submit(new Event<Trap>("pitfall-trap",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();if(!closed)close();}});
        }
        boolean step(){if(closed||!healthyOwner(owner,site)||!live(site.object))return false;
            if(state!=0)return true;
            if(owner.productionActionToken!=token||owner.absX!=x||owner.absY!=y||!ready(owner)||owner.playerLevel[21]<site.species.level||!owner.getItems().playerHasItem(946))return false;
            if(++age<6)return true;
            if(!ProductionBatch.exchange(owner,new int[][]{{logs,1}},new int[0][]))return false;
            state=1;owner.startAnimation(65535);return true;
        }
        void dismantle(){if(state==2||state==0||closed||!ready(owner))return;
            if(state>=3){if(owner.playerLevel[21]<site.species.level)return;if(!ProductionBatch.exchange(owner,new int[0][],new int[][]{{532,1},{fur,1}}))return;owner.getPA().addSkillXPMultiplied(site.species.xp,21,true);}
            owner.interruptActions();owner.startAnimation(5212);close();
        }
        void close(){if(closed)return;closed=true;TRAPS.remove(site,this);for(Chase chase:new ArrayList<>(HUNTERS.values()))if(chase.trap==this)chase.trap=null;}
    }
    private static final class Chase {
        final Player owner;final NPC prey;final Species species;final boolean oldWalking,oldWalkHome;
        Trap trap;Position near,far;ForceMovement flight;boolean low,success,closed;int age,cooldown;
        Chase(Player owner,NPC prey,Species species){this.owner=owner;this.prey=prey;this.species=species;oldWalking=prey.randomWalk;oldWalkHome=prey.getBehaviour().isWalkHome();prey.randomWalk=false;prey.getBehaviour().setWalkHome(false);prey.resetWalkingQueue();}
        void begin(){Server.getEventHandler().submit(new Event<Chase>("pitfall-chase",this,1){public void execute(){if(!step())stop();}public void stop(){super.stop();close();}});}
        void route(Trap t,boolean low){if(flight!=null||LAST_SKIPPED.get(prey)==t.site)return;trap=t;this.low=low;near=t.site.pad(low);far=t.site.pad(!low);}
        boolean step(){
            if(closed||!registered(prey)||owner.isDisconnected()||owner.getSession()==null||owner.isDead||owner.getHealth().getCurrentHealth()<=0||owner.heightLevel!=0||owner.getRegionProvider()!=RegionProvider.getGlobal()||!owner.getItems().playerHasItem(10029)||owner.playerLevel[21]<species.level||Math.max(Math.abs(owner.absX-prey.absX),Math.abs(owner.absY-prey.absY))>20||++age>200)return false;
            if(flight!=null){prey.resetWalkingQueue();if(flight.isActive())return true;flight=null;
                if(trap==null||TRAPS.get(trap.site)!=trap||!healthyOwner(owner,trap.site))return false;
                if(success){trap.state=low?3:4;trap.fur=RANDOM.nextInt(100)<species.goodFurChance(owner.playerLevel[21])?species.fur:species.tatty;
                    LAST_SKIPPED.remove(prey);prey.setDead(true);prey.applyDead=true;prey.needRespawn=true;prey.setInvisible(true);prey.actionTimer=5;prey.setUpdateRequired(true);owner.sendMessage("The creature falls into your pit. Dismantle the trap to collect its fur, big bones and Hunter experience.");return false;}
                LAST_SKIPPED.put(prey,trap.site);trap=null;owner.sendMessage("The creature clears the pit. Lure it over a different pit before trying this one again.");return true;
            }
            if(trap!=null){
                if(TRAPS.get(trap.site)!=trap||trap.state!=1||!healthyOwner(owner,trap.site)||!across()){trap=null;}
                else if(prey.absX==near.getX()&&prey.absY==near.getY()){
                    if(!free(far,2))return false;
                    success=RANDOM.nextInt(100)<species.catchChance(owner.playerLevel[21]);if(success)trap.state=2;
                    Position end=success?new Position(trap.site.object.x,trap.site.object.y,0):far;
                    prey.resetWalkingQueue();prey.facePosition(end.getX(),end.getY());prey.startAnimation(success?5234:5231);
                    flight=new ForceMovement(prey,2,prey.getPosition(),end,0,60);flight.startForceMovement();return true;
                }else{walk(prey,near.getX(),near.getY());return true;}
            }
            if(cooldown>0)cooldown--;
            int d=Math.max(Math.max(prey.absX-owner.absX,Math.max(0,owner.absX-prey.absX-1)),Math.max(prey.absY-owner.absY,Math.max(0,owner.absY-prey.absY-1)));
            if(d<=1&&contact(prey,owner)){prey.resetWalkingQueue();if(cooldown==0&&!owner.getMovementState().isLocked()){
                prey.facePlayer(owner.getIndex());prey.startAnimation(species==Species.GRAAHK?5229:5228);
                int damage=RANDOM.nextInt(species.maxHit+1);owner.appendDamage(prey,damage,damage==0?Hitmark.MISS:Hitmark.HIT);owner.underAttackByNpc=prey.getIndex();owner.singleCombatDelay2=System.currentTimeMillis();owner.setInCombatDelay(Configuration.IN_COMBAT_TIMER);cooldown=4;
            }}else walk(prey,owner.absX,owner.absY);
            return true;
        }
        boolean across(){Site s=trap.site;return s.horizontal()?(low?owner.absX>=s.object.x+2:owner.absX<s.object.x):(low?owner.absY>=s.object.y+2:owner.absY<s.object.y);}
        void close(){if(closed)return;closed=true;HUNTERS.remove(owner,this);CLAIMS.remove(prey,this);
            if(flight!=null&&flight.isActive()){flight.cancel();if(registered(prey))prey.teleport(near);}
            prey.randomWalk=oldWalking;prey.getBehaviour().setWalkHome(oldWalkHome);prey.resetWalkingQueue();if(trap!=null&&trap.state==2)trap.close();}
    }
}
