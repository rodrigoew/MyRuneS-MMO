package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.Skill;
import io.xeros.content.skills.hunter.trap.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.npc.pets.PetInsurance2022;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.*;
import io.xeros.model.tickable.impl.WalkToTickable;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;

/** One lifecycle for bird snares and box traps. World slots and approached animals have exclusive owners. */
public final class GroundTrapHunting {
    public static final Random RANDOM=new Random();
    private static final Map<Long,Site> SITES=new HashMap<>();
    private static final Map<NPC,Site> APPROACHING=new IdentityHashMap<>();
    private enum Phase { SETTING, OPEN, LURING, CATCHING, FULL, FAILED, CLOSED }
    private GroundTrapHunting() { }

    private static long key(int x,int y,int height) { return ((long)height<<28)|((long)x<<14)|y; }
    private static boolean bird(Trap trap) { return trap.getType().getItemId()==10006; }
    private static boolean ready(Player p) {
        return p.getUtilityMagic().canInteract() && p.getTeleportToX()==-1 && p.getTeleportToY()==-1
                && !p.getMovementState().isLocked() && !p.getInterfaceEvent().isActive()
                && !p.getBankPin().requiresUnlock() && !Server.getMultiplayerSessionListener().inAnySession(p);
    }
    private static boolean global(Player p) {
        return p.getInstance()==null && p.getRegionProvider()==RegionProvider.getGlobal()
                && p.heightLevel>=0 && p.heightLevel<=3;
    }
    private static boolean boxUnlocked(Player p) {
        // Stage 7 is Nickolaus's box-trapping lesson, before returning the ferret to Charlie.
        return p.getQuesting().getQuestList().stream().anyMatch(quest->quest.getName().equals("Eagles' Peak")&&quest.getStage()>=7);
    }
    private static boolean level(Player p,Trap trap) {
        if(p.playerLevel[21]<(bird(trap)?1:27)) {
            p.sendMessage("You need Hunter level "+(bird(trap)?1:27)+" to lay this trap."); return false;
        }
        if(!bird(trap) && !boxUnlocked(p)) {
            p.sendMessage("Nickolaus must teach you box trapping during Eagles' Peak first."); return false;
        }
        return true;
    }
    private static boolean placement(Player p,int x,int y,int height,Site own) {
        if(!global(p)) return false;
        Site existing=SITES.get(key(x,y,height));
        if(existing!=null && existing!=own) return false;
        Region region=p.getRegionProvider().get(x,y);
        int flags=region.getTerrainFlags(x,y,height);
        if(flags<0 || (flags&4)!=0 || (region.getClip(x,y,height)&0x1280100)!=0) return false;
        if(Server.getGlobalObjects().anyExists(x,y,height)) return false;
        // Keep native interactive scenery, including currently invisible tracking objects, intact.
        return region.worldObjectsSnapshot().stream().noneMatch(o->o.x==x&&o.y==y&&o.height==height&&o.type>=9&&o.type<=21);
    }

    public static boolean lay(Player p,Trap trap) { return lay(p,trap,null); }
    private static boolean lay(Player p,Trap trap,GroundItem ground) {
        if(trap==null || trap.getPlayer()!=p || !ready(p) || !level(p,trap)) return false;
        if(ground==null && !p.getItems().playerHasItem(trap.getType().getItemId())) return false;
        if(Hunter.activeTrapCount(p)>=Hunter.getMaximumTraps(p)) {
            p.sendMessage("You cannot lay any more traps with your current Hunter level."); return false;
        }
        if(!placement(p,p.absX,p.absY,p.heightLevel,null)) {
            p.sendMessage("You cannot lay a trap here."); return false;
        }
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.startAnimation(5208);
        Site site=new Site(p,trap,ground);
        SITES.put(site.key,site);
        Hunter.GLOBAL_TRAPS.computeIfAbsent(p,unused->new TrapProcessor()).getTraps().add(trap);
        site.start(); return true;
    }

    public static boolean ground(Player p,int item,int x,int y) {
        if(item!=10006 && item!=10008) return false;
        if(!ready(p) || !global(p)) return true;
        GroundItem ground=Server.itemHandler.getGroundItem(p,item,x,y,p.heightLevel);
        if(ground==null || !ground.isOwner(p)) return true;
        if(p.absX!=x || p.absY!=y) {
            if(p.distanceToPoint(x,y)>16) return true;
            PathFinder.getPathFinder().findRoute(p,x,y,false,0,0);
            long token=p.productionActionToken;
            p.setTickable((container,player)->{
                if(token!=player.productionActionToken || !ready(player)) { container.stop(); return; }
                if(player.newWalkCmdSteps>0 || !player.isWalkingQueueEmpty()) return;
                container.stop();
                if(player.absX==x && player.absY==y) ground(player,item,x,y);
            });
            return true;
        }
        Trap trap=item==10006 ? new io.xeros.content.skills.hunter.trap.impl.BirdSnare(p)
                : new io.xeros.content.skills.hunter.trap.impl.BoxTrap(p);
        lay(p,trap,ground); return true;
    }

    public static Optional<Trap> find(Player p,GlobalObject object) {
        Site site=SITES.get(key(object.getX(),object.getY(),object.getHeight()));
        return site!=null&&site.owner==p&&site.object!=null&&site.object.getObjectId()==object.getObjectId()
                ? Optional.of(site.trap) : Optional.empty();
    }
    public static boolean interact(Player p,GlobalObject object,int option) {
        Site site=SITES.get(key(object.getX(),object.getY(),object.getHeight()));
        return site!=null && site.interact(p,object.getObjectId(),option);
    }
    public static boolean click(Player p,WorldObject object,int option) {
        Site site=SITES.get(key(object.x,object.y,object.height));
        if(site==null) return false;
        site.interact(p,object.id,option); return true;
    }
    public static void lure(Trap trap,NPC npc) {
        Site site=SITES.get(key(trap.getObject().getX(),trap.getObject().getY(),trap.getObject().getHeight()));
        if(site!=null && site.trap==trap) site.lure(npc);
    }
    public static boolean use(Player p,int item,int id,int x,int y) {
        Site site=SITES.get(key(x,y,p.heightLevel));
        if(site==null) return false;
        if(site.owner!=p || !ready(p) || !site.near(p) || !site.current(id) || site.phase!=Phase.OPEN || site.actionPending) return true;
        if(item==594) {
            if(p.playerLevel[21]<39) { p.sendMessage("You need Hunter level 39 to smoke a trap."); return true; }
            if(site.smoked) { p.sendMessage("This trap is already smoked."); return true; }
            site.action(5217,()->{
                if(site.phase==Phase.OPEN && p.playerLevel[21]>=39 && p.getItems().playerHasItem(594)) {
                    site.smoked=true;p.sendMessage("You smoke the trap.");
                }
            }); return true;
        }
        boolean bait=item==9994 || item==9996 || item==2132 || item==2134 || item==2136 || item==2138;
        if(!bird(site.trap) && bait) {
            if(site.bait!=0) { p.sendMessage("This trap is already baited."); return true; }
            site.action(5217,()->{
                if(site.phase!=Phase.OPEN) return;
                int[][] bowl=item==9994||item==9996 ? new int[][]{{1923,1}} : new int[0][];
                if(ProductionBatch.exchange(p,new int[][]{{item,1}},bowl)) {
                    site.bait=item;p.sendMessage("You add bait to the box trap.");
                }
            }); return true;
        }
        return false;
    }
    public static void logout(Player p) {
        TrapProcessor processor=Hunter.GLOBAL_TRAPS.get(p);
        if(processor==null) return;
        for(Trap trap:new ArrayList<>(processor.getTraps())) abandon(p,trap);
    }
    public static void abandon(Player p,Trap trap) {
        if(trap==null) return;
        Site site=SITES.get(key(trap.getObject().getX(),trap.getObject().getY(),trap.getObject().getHeight()));
        if(site!=null && site.owner==p && site.trap==trap) site.close(true);
    }

    private static boolean registered(NPC npc) {
        if(io.xeros.content.questing.EaglesPeak.temporary(npc))return false;
        return npc!=null && npc.getIndex()>0 && NPCHandler.npcs[npc.getIndex()]==npc
                && !npc.isUnregister() && !npc.isDead() && !npc.isInvisible() && !npc.needRespawn
                && npc.getInstance()==null;
    }

    private static final class Site {
        final Player owner;
        final Trap trap;
        final int x,y,height;
        final long key,setupToken;
        final GroundItem ground;
        Phase phase=Phase.SETTING;
        GlobalObject object;
        NPC target;
        GroundTrapSpecies2022 species;
        GameItem[] loot;
        Integer capturedPet;
        boolean laid,smoked,actionPending,oldRandom,oldHome,oldWalkHome;
        int life=100,age,transition,bait,ticket,approachDirection;

        Site(Player owner,Trap trap,GroundItem ground) {
            this.owner=owner;this.trap=trap;this.ground=ground;
            x=owner.absX;y=owner.absY;height=owner.heightLevel;
            key=GroundTrapHunting.key(x,y,height);setupToken=owner.productionActionToken;
        }
        void start() {
            Server.getEventHandler().submit(new Event<Site>("ground-hunter-trap",this,1) {
                public void execute() { if(!step()) stop(); }
                public void stop() { super.stop();if(phase!=Phase.CLOSED) close(true); }
            });
        }
        boolean near(Player p) { return p.heightLevel==height&&Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))<=1; }
        boolean onTrap() { return owner.heightLevel==height&&owner.absX==x&&owner.absY==y; }
        boolean current(int id) {
            return object!=null && object.getObjectId()==id && RegionProvider.getGlobal().get(x,y)
                    .getWorldObject(id,x,y,height).filter(o->o.type==10&&o.face==object.getFace()).isPresent();
        }
        boolean morph(int id,int face,Trap.TrapState state) {
            if(object!=null && !Server.getGlobalObjects().removeIfCurrent(object)) return false;
            object=new GlobalObject(id,x,y,height,face,10,-1);
            Server.getGlobalObjects().add(object);trap.synchronize(object,state);return true;
        }
        boolean step() {
            if(phase==Phase.CLOSED) return false;
            if(owner.isDead || owner.getHealth().getCurrentHealth()<=0 || owner.isDisconnected() || owner.getSession()==null) { close(true);return false; }
            if(phase==Phase.SETTING) {
                if(owner.productionActionToken!=setupToken || !ready(owner) || owner.absX!=x || owner.absY!=y
                        || owner.heightLevel!=height || !level(owner,trap)) { close(false);return false; }
                if(++age<2) return true;
                if(!placement(owner,x,y,height,this)) { close(false);return false; }
                if(ground==null) {
                    if(!ProductionBatch.exchange(owner,new int[][]{{trap.getType().getItemId(),1}},new int[0][])) { close(false);return false; }
                } else {
                    if(Server.itemHandler.getGroundItem(owner,ground.getId(),x,y,height)!=ground || !ground.isOwner(owner)) { close(false);return false; }
                    Server.itemHandler.removeGroundItem(owner,ground,false);
                    if(ground.getAmount()>1) Server.itemHandler.createPrivateTimedGroundItem(owner,
                            new GameItem(ground.getId(),ground.getAmount()-1),new Position(x,y,height),100);
                }
                laid=true;
                if(!morph(bird(trap)?9345:9380,0,Trap.TrapState.PENDING)) { close(true);return false; }
                phase=Phase.OPEN;life=100;stepOff();return true;
            }
            if(!current(object.getObjectId())) { close(true);return false; }
            if(--life<=0) { close(true);return false; }
            switch(phase) {
                case OPEN:
                    if(!actionPending && !onTrap() && ++age%3==0) {
                        NPC closest=null;int distance=Integer.MAX_VALUE;
                        for(NPC npc:NPCHandler.npcs) {
                            GroundTrapSpecies2022 candidate=npc==null?null:GroundTrapSpecies2022.npc(npc.getNpcId());
                            if(candidate==null || candidate.bird()!=bird(trap) || owner.playerLevel[21]<candidate.level
                                    || !registered(npc) || npc.heightLevel!=height || npc.underAttack || APPROACHING.containsKey(npc)) continue;
                            int d=Math.max(Math.abs(npc.absX-x),Math.abs(npc.absY-y));
                            if(d<=3 && d<distance) { closest=npc;distance=d; }
                        }
                        if(closest!=null) lure(closest);
                    }
                    break;
                case LURING:
                    if(!registered(target) || target.getNpcId()!=species.npc || target.underAttack
                            || owner.playerLevel[21]<species.level || onTrap() || ++transition>12) {
                        release();phase=Phase.OPEN;trap.synchronize(object,Trap.TrapState.PENDING);break;
                    }
                    if(target.absX==x && target.absY==y) catchAnimal();
                    else {
                        rememberDirection();
                        target.setMovement(GroundTrapPath2022.next(target,x,y));
                    }
                    break;
                case CATCHING:
                    if(--transition<=0) {
                        if(loot!=null) {
                            if(!morph(species.fullObject,bird(trap)?approachDirection:0,Trap.TrapState.CAUGHT)) { close(true);return false; }
                            phase=Phase.FULL;owner.sendMessage("Your trap has caught something.");
                        } else {
                            release();
                            if(!morph(bird(trap)?9344:9385,0,Trap.TrapState.FALLEN)) { close(true);return false; }
                            phase=Phase.FAILED;
                        }
                        life=100;
                    }
                    break;
                default: break;
            }
            return true;
        }
        void stepOff() {
            if(!onTrap() || owner.freezeDelay>0) return;
            for(int[] direction:new int[][]{{-1,0},{1,0},{0,-1},{0,1}})
                if(owner.getRegionProvider().getClipping(x+direction[0],y+direction[1],height,direction[0],direction[1])) {
                    // This event runs after player postProcessing. Packet-style newWalkCmdSteps
                    // would be cleared by the next preProcessing before it ever enters the queue.
                    owner.addToWalkingQueue(x+direction[0]-owner.mapRegionX*8,y+direction[1]-owner.mapRegionY*8);
                    return;
                }
        }
        void lure(NPC npc) {
            GroundTrapSpecies2022 candidate=npc==null?null:GroundTrapSpecies2022.npc(npc.getNpcId());
            if(phase!=Phase.OPEN || actionPending || candidate==null || candidate.bird()!=bird(trap)
                    || !registered(npc) || npc.heightLevel!=height || npc.underAttack || onTrap()
                    || owner.playerLevel[21]<candidate.level || Math.max(Math.abs(npc.absX-x),Math.abs(npc.absY-y))>3
                    || APPROACHING.putIfAbsent(npc,this)!=null) return;
            target=npc;species=candidate;oldRandom=npc.randomWalk;oldHome=npc.walkingHome;
            oldWalkHome=npc.getBehaviour().isWalkHome();npc.getBehaviour().setWalkHome(false);
            rememberDirection();
            npc.randomWalk=false;npc.walkingHome=false;npc.resetWalkingQueue();
            phase=Phase.LURING;transition=0;trap.synchronize(object,Trap.TrapState.TRIGGERED);
        }
        void rememberDirection() {
            int dx=target.absX-x,dy=target.absY-y;
            if(dx!=0 || dy!=0) approachDirection=Math.abs(dx)>Math.abs(dy)?dx>0?1:3:dy<0?2:0;
        }
        void catchAnimal() {
            boolean success=RANDOM.nextInt(25600)<species.threshold(owner.playerLevel[21],smoked,bait);
            if(success) {
                loot=species.reward(RANDOM);trap.caught(species,loot);
                target.resetWalkingQueue();target.setDead(true);target.applyDead=true;target.needRespawn=true;
                target.setInvisible(true);target.actionTimer=5;target.setUpdateRequired(true);
                release();
            } else target.setInvisible(true);
            smoked=false;bait=0;
            int model=success?species.enteringObject(approachDirection):bird(trap)?9346:9381;
            if(!morph(model,bird(trap)?approachDirection:0,Trap.TrapState.TRIGGERED)) { close(true);return; }
            phase=Phase.CATCHING;transition=2;
        }
        void release() {
            if(target!=null && APPROACHING.remove(target,this)) {
                if(target.getIndex()>0 && NPCHandler.npcs[target.getIndex()]==target) {
                    // NPC resetAttack can restore randomWalk during normal processing. The behaviour
                    // flag suspends ordinary wandering while lured and must also be restored before
                    // the respawn factory copies the captured creature's behaviour.
                    target.getBehaviour().setWalkHome(oldWalkHome);
                    target.randomWalk=oldRandom;target.walkingHome=oldHome;
                    if(!target.needRespawn && !target.isDead()) target.setInvisible(false);
                    target.resetWalkingQueue();target.setUpdateRequired(true);
                }
            }
            target=null;
        }
        int[][] outputs(boolean reset) {
            List<int[]> items=new ArrayList<>();
            if(phase==Phase.FULL) for(GameItem item:loot) items.add(new int[]{item.getId(),item.getAmount()});
            if(!reset) items.add(new int[]{trap.getType().getItemId(),1});
            return items.toArray(int[][]::new);
        }
        boolean interact(Player p,int id,int option) {
            if(p!=owner) { p.sendMessage("This trap belongs to someone else."); return false; }
            if(!ready(p) || !global(p) || !near(p) || !current(id) || actionPending) return false;
            if(option==2 && phase==Phase.OPEN) {
                p.sendMessage("The trap is set"+(smoked?", smoked":"")+(bait!=0?" and baited":"")+".");return true;
            }
            if(option!=1 && option!=2 || phase==Phase.LURING || phase==Phase.CATCHING || phase==Phase.SETTING) return false;
            boolean reset=option==2 && !bird(trap) && (phase==Phase.FAILED || phase==Phase.FULL);
            if(option==2 && !reset) return false;
            int[][] outputs=outputs(reset);
            if(!ProductionBatch.canExchange(p,new int[0][],outputs)) {
                p.sendMessage("You need more inventory space to collect your catch and trap.");return false;
            }
            Phase expected=phase;
            action(5207,()->{
                if(phase!=expected) return;
                if(!ProductionBatch.canExchange(p,new int[0][],outputs)) return;
                boolean full=phase==Phase.FULL;
                if(full && capturedPet==null) {
                    capturedPet=species.pet(RANDOM,p.getPA().getLevelForXP(p.playerXP[21]),p.playerXP[21]>=Skill.MAX_EXP)
                            ? species==GroundTrapSpecies2022.CHINCHOMPA?13324:species==GroundTrapSpecies2022.CARNIVOROUS_CHINCHOMPA?13323:13325 : -1;
                }
                int pet=full?capturedPet:-1;
                PetInsurance2022.RewardCommit commit=p.getPetInsurance().exchange(new int[0][],outputs,pet,new Position(x,y,height));
                if(commit==null) return;
                if(full) p.getPA().addSkillXPMultiplied(species.xp,21,true);
                loot=null;capturedPet=null;trap.caught(null,null);
                if(reset) {
                    smoked=false;bait=0;species=null;life=100;
                    if(!morph(9380,0,Trap.TrapState.PENDING)) close(true);
                    else { phase=Phase.OPEN;stepOff(); }
                } else close(false);
                commit.award();
            }); return true;
        }
        void action(int animation,Runnable committed) {
            owner.interruptActions();owner.getPA().stopSkilling();owner.getPA().removeAllWindows();owner.stopMovement();
            owner.facePosition(x,y);owner.startAnimation(animation);
            int own=++ticket,px=owner.absX,py=owner.absY;long token=owner.productionActionToken;actionPending=true;
            Server.getEventHandler().submit(new Event<Site>("ground-hunter-action",this,2) {
                public void execute() {
                    if(phase!=Phase.CLOSED && own==ticket && token==owner.productionActionToken && ready(owner)
                            && owner.absX==px && owner.absY==py && near(owner) && current(object.getObjectId())) committed.run();
                    stop();
                }
                public void stop() {
                    super.stop();if(own==ticket) actionPending=false;
                    if(token==owner.productionActionToken) owner.startAnimation(65535);
                }
            });
        }
        void close(boolean drop) {
            if(phase==Phase.CLOSED) return;
            phase=Phase.CLOSED;release();SITES.remove(key,this);
            if(object!=null) Server.getGlobalObjects().removeIfCurrent(object);
            trap.setAbandoned(true);
            TrapProcessor processor=Hunter.GLOBAL_TRAPS.get(owner);
            if(processor!=null) {
                processor.getTraps().remove(trap);
                if(processor.getTraps().isEmpty()) Hunter.GLOBAL_TRAPS.remove(owner);
            }
            if(drop && laid) Server.itemHandler.createPrivateTimedGroundItem(owner,
                    new GameItem(trap.getType().getItemId(),1),new Position(x,y,height),100);
        }
    }
}
