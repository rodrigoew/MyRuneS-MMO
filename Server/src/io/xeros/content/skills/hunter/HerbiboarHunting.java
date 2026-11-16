package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.npc.pets.PetInsurance2022;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.GameItem;
import io.xeros.model.tickable.impl.WalkToTickable;
import java.util.*;
import static io.xeros.content.skills.hunter.HerbiboarData2022.*;

/** Personal trails and stunned prey; only received XP, counters and preferences survive logout. */
public final class HerbiboarHunting {
    public static final Random RANDOM=new Random();
    private static final Map<NPC,HerbiboarHunting> OWNERS=new IdentityHashMap<>();
    private final Player p;
    private final Map<Edge,Integer> tracks=new LinkedHashMap<>();
    private final Set<Node> visited=EnumSet.noneOf(Node.class);
    private Start start;private Spot target;private End finish;private NPC prey;
    private HerbiboarRewards2022.Harvest harvest;
    private int count,steps,ticket;private boolean initialized,pending,warning=true,harvested;
    public HerbiboarHunting(Player p){this.p=p;}
    public int count(){return count;}
    public boolean warning(){return warning;}
    public boolean insured(){return p.getPetInsurance().insured(HerbiboarRewards2022.HERBI);}
    public int reclaimTokens(){return p.getPetInsurance().freeReclaims();}
    public NPC prey(){return prey;}
    public boolean ownsHerbi(){return p.getPetInsurance().owns(HerbiboarRewards2022.HERBI);}
    public boolean reclaimHerbi(NPC probita){return p.getPetInsurance().reclaim(HerbiboarRewards2022.HERBI,probita);}
    public String encode(){return "1,"+count+","+(warning?1:0)+","+(insured()?1:0)+","+reclaimTokens();}
    public boolean restore(String text){
        if(!text.matches("1,[0-9]{1,10},[01],[01],[0-9]{1,10}"))return false;
        try{String[] s=text.split(",");int n=Integer.parseInt(s[1]),tokens=Integer.parseInt(s[4]);if(n<0||tokens<0)return false;count=n;warning=s[2].equals("1");p.getPetInsurance().importLegacyHerbi(s[3].equals("1"),tokens);return true;}catch(NumberFormatException e){return false;}
    }
    private boolean area(){return p.getInstance()==null&&p.getRegionProvider()==RegionProvider.getGlobal()&&p.heightLevel==0&&p.absX>=3650&&p.absX<=3766&&p.absY>=3790&&p.absY<=3905;}
    private boolean ready(){return area()&&p.getTeleportToX()==-1&&p.getTeleportToY()==-1&&p.getUtilityMagic().canInteract()&&!p.getMovementState().isLocked()&&!Server.getMultiplayerSessionListener().inAnySession(p)&&!p.getInterfaceEvent().isActive()&&!p.getBankPin().requiresUnlock();}
    private static boolean live(WorldObject o){return RegionProvider.getGlobal().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,0).filter(o::equals).isPresent();}
    private static boolean registered(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isUnregister()&&!n.isDead()&&!n.isInvisible()&&!n.needRespawn;}
    private boolean near(WorldObject o){Position size=o.getObjectSize();return Math.max(Math.max(o.x-p.absX,Math.max(0,p.absX-o.x-Math.max(1,size.getX())+1)),Math.max(o.y-p.absY,Math.max(0,p.absY-o.y-Math.max(1,size.getY())+1)))<=1;}
    public static boolean visible(Player p,NPC n){return n.getNpcId()!=7785&&n.getNpcId()!=7786||OWNERS.get(n)==p.getHerbiboar();}
    public void process(){
        if(p.getTeleportToX()!=-1||p.getTeleportToY()!=-1)return;
        if(!initialized){initialized=true;sendTracks();}
        if((start!=null||prey!=null)&&(!area()||p.isDead||p.getHealth().getCurrentHealth()<=0||p.isDisconnected()))logout();
        if(prey!=null&&!registered(prey))clearPrey();
    }
    public void logout(){ticket++;pending=false;clearTrail();clearPrey();}
    private void clearPrey(){if(prey!=null){OWNERS.remove(prey,this);if(prey.getIndex()>0&&NPCHandler.npcs[prey.getIndex()]==prey)prey.unregisterInstant();prey=null;}harvest=null;harvested=false;p.getPA().createPlayerHints(0,0);}
    private void clearTrail(){start=null;target=null;finish=null;steps=0;tracks.clear();visited.clear();sendTracks();}
    private void sendTracks(){
        int a=0,b=0,c=0;for(Map.Entry<Edge,Integer> e:tracks.entrySet()){Edge edge=e.getKey();if(edge.parent==1617)a|=e.getValue()<<edge.shift;else if(edge.parent==1618)b|=e.getValue()<<edge.shift;else if(edge.parent==1619)c|=e.getValue()<<edge.shift;}
        p.getPA().sendFrame87Masked(1617,0x3fffffff,a);p.getPA().sendFrame87Masked(1618,0x3fffffff,b);p.getPA().sendFrame87Masked(1619,0xfff,c);
        p.getPA().sendFrame87Masked(1620,0xf8000000,(steps>0?0x80000000:0)|(finish==null?0:finish.code<<27));
    }
    private void trace(Node from,Node to){
        tracks.replaceAll((e,v)->v==3?5:v==4?6:v);Node at=from;
        for(Edge edge:HerbiboarData2022.path(from,to)){if(edge.bit>=0)tracks.put(edge,edge.visibleFrom(at));at=edge.other(at);}sendTracks();
    }
    public boolean click(WorldObject o,int option){
        if(o==null)return false;Start origin=STARTS.stream().filter(s->s.object.equals(o)).findFirst().orElse(null);
        Spot spot=SPOTS.stream().filter(s->s.object.equals(o)).findFirst().orElse(null);End end=ENDS.stream().filter(s->s.object.equals(o)).findFirst().orElse(null);
        if(origin==null&&spot==null&&end==null)return false;
        if(!ready()||!live(o)||!near(o)||pending)return true;
        if(origin!=null){
            if(option==2){p.sendMessage("You have caught "+String.format("%,d",count)+" herbiboar"+(count==1?".":"s."));return true;}
            if(option==3){warning=!warning;p.sendMessage("The warning before starting another herbiboar trail is now "+(warning?"enabled.":"disabled."));return true;}
            if(option!=1)return true;
            if(p.playerLevel[21]<80){p.sendMessage("You need Hunter level 80 to start tracking a herbiboar.");return true;}
            if(start!=null&&warning){Start chosen=origin;new DialogueBuilder(p).option("Abandon your current herbiboar trail?",new DialogueOption("Yes, start again",q->{q.getPA().removeAllWindows();if(ready()&&live(o)&&near(o)&&q.playerLevel[21]>=80)begin(chosen);}),new DialogueOption("No, keep my current trail",q->q.getPA().removeAllWindows())).send();return true;}
            begin(origin);return true;
        }
        if(spot!=null){if(option!=1)return true;if(start==null||target!=spot){p.sendMessage("You find no new herbiboar tracks here.");return true;}inspect(spot);return true;}
        if(option==2){p.sendMessage(finish==end?"The tracks lead into this tunnel.":"You find nothing in the tunnel.");return true;}
        if(option==1&&start!=null&&finish==end)kick(end);return true;
    }
    private void perform(WorldObject o,int animation,Runnable action){
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.stopMovement();p.attacking.reset();p.facePosition(o.x,o.y);p.startAnimation(animation);
        int own=++ticket,x=p.absX,y=p.absY;long token=p.productionActionToken;pending=true;
        Server.getEventHandler().submit(new Event<Player>("herbiboar-action",p,3){public void execute(){if(own==ticket&&token==p.productionActionToken&&ready()&&p.absX==x&&p.absY==y&&p.getTeleportToX()==-1&&p.getTeleportToY()==-1&&live(o))action.run();stop();}public void stop(){super.stop();if(own==ticket)pending=false;if(token==p.productionActionToken)p.startAnimation(65535);}});
    }
    private void begin(Start origin){perform(origin.object,5216,()->{
        clearTrail();clearPrey();start=origin;Node[] candidates;
        switch(origin.node){case MIDDLE:candidates=new Node[]{Node.A,Node.C,Node.D,Node.F};break;case LEPRECHAUN:candidates=new Node[]{Node.G,Node.I,Node.J};break;case CAMP_ENTRANCE:candidates=new Node[]{Node.I,Node.J,Node.K};break;case GHOST_MUSHROOM:candidates=new Node[]{Node.I,Node.K};break;default:candidates=new Node[]{Node.H,Node.J};}
        next(origin.node,candidates[RANDOM.nextInt(candidates.length)]);p.sendMessage("You find herbiboar tracks. Follow the footprints and inspect the scenery.");
    });}
    private void next(Node from,Node to){
        String guaranteed=HerbiboarData2022.guaranteed(to,from);List<Spot> choices=HerbiboarData2022.at(to);
        target=guaranteed==null?choices.get(RANDOM.nextInt(choices.size())):choices.stream().filter(s->s.name.equals(guaranteed)).findFirst().orElseThrow();finish=null;trace(from,to);
    }
    private void finish(Node from,End end){target=null;finish=end;trace(from,end.node);sendTracks();p.getPA().createObjectHints(end.object.x,end.object.y,0,2);p.sendMessage("The tracks end at a tunnel. Attack that tunnel to flush out the herbiboar.");}
    private void inspect(Spot spot){perform(spot.object,5216,()->{
        if(target!=spot||start==null)return;steps++;visited.add(spot.node);p.getPA().addSkillXPMultiplied(50,21,true);
        GameItem find=HerbiboarRewards2022.inspection(RANDOM);if(find!=null&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{find.getId(),find.getAmount()}}))Server.itemHandler.createPrivateTimedGroundItem(p,find,p.getPosition(),100);
        if(steps>=5){finish(spot.node,ENDS.stream().min(Comparator.comparingInt(e->Math.abs(e.object.x-spot.object.x)+Math.abs(e.object.y-spot.object.y))).orElseThrow());return;}
        if(RANDOM.nextInt(50)==0){clearTrail();p.getPA().createPlayerHints(0,0);p.sendMessage("The creature has successfully confused you with its tracks, leading you round in circles. You'll need to start again.");return;}
        End nearby=HerbiboarData2022.end(spot.node);if(nearby!=null&&RANDOM.nextInt(4)==0){finish(spot.node,nearby);return;}
        List<Exit> choices=new ArrayList<>();for(Exit exit:spot.exits){Node to=edge(exit.bit).other(spot.node);if(to!=null&&!visited.contains(to))choices.add(exit);}
        if(choices.isEmpty()){finish(spot.node,ENDS.stream().min(Comparator.comparingInt(e->Math.abs(e.object.x-spot.object.x)+Math.abs(e.object.y-spot.object.y))).orElseThrow());return;}
        Edge chosen=edge(choices.get(RANDOM.nextInt(choices.size())).bit);Node to=chosen.other(spot.node);
        if(to.start()){finish(spot.node,HerbiboarData2022.end(to));return;}next(spot.node,to);
    });}
    private Position spawnAt(End end){
        List<Position> tiles=new ArrayList<>();for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++){if(dx==0&&dy==0)continue;Position tile=new Position(end.object.x+dx,end.object.y+dy,0);if((p.getRegionProvider().getClipping(tile.getX(),tile.getY(),0)&0x1280100)==0&&(p.absX!=tile.getX()||p.absY!=tile.getY()))tiles.add(tile);}
        return tiles.stream().min(Comparator.comparingInt(t->Math.abs(t.getX()-p.absX)+Math.abs(t.getY()-p.absY))).orElse(null);
    }
    private void kick(End end){if(spawnAt(end)==null)return;perform(end.object,423,()->{
        if(finish!=end||start==null)return;Position pos=spawnAt(end);if(pos==null)return;clearPrey();
        prey=new NPC(7785,pos);prey.randomWalk=false;prey.walkingType=0;prey.getBehaviour().setWalkHome(false);prey.getBehaviour().setAggressive(false);prey.getBehaviour().setRespawn(false);prey.facePosition(p.absX,p.absY);prey.startAnimation(7687);OWNERS.put(prey,this);harvest=null;harvested=false;
        if(count<Integer.MAX_VALUE)count++;int xp=HerbiboarRewards2022.hunterXp(p.getPA().getLevelForXP(p.playerXP[21]));clearTrail();p.getPA().addSkillXPMultiplied(xp,21,true);p.getPA().createPlayerHints(1,prey.getIndex());p.sendMessage("A herbiboar appears outside the tunnel. Harvest its herbs when you are ready.");
    });}
    public boolean npc(NPC n){
        if(n==null||n.getNpcId()!=7785&&n.getNpcId()!=7786)return false;
        if(n!=prey||OWNERS.get(n)!=this||!registered(n)||harvested||pending||!ready())return true;
        if(p.playerLevel[15]<31){p.sendMessage("You need Herblore level 31 to harvest the herbiboar.");return true;}
        int distance=Math.max(Math.abs(n.absX-p.absX),Math.abs(n.absY-p.absY));if(distance>12)return true;
        if(distance>1){PathFinder.getPathFinder().findRoute(p,n.absX,n.absY,true,1,1);p.setTickable(new WalkToTickable(p,n.getPosition(),1,1,q->npc(n)));return true;}
        if(harvest==null)harvest=HerbiboarRewards2022.harvest(RANDOM,p.playerLevel[15],p.playerEquipment[Player.playerWeapon]==7409);
        if(!ProductionBatch.canExchange(p,new int[0][],harvest.items)){p.sendMessage("You need more inventory space to harvest these herbs.");return true;}
        p.interruptActions();p.getPA().stopSkilling();p.stopMovement();p.facePosition(n.absX,n.absY);p.startAnimation(2274);long token=p.productionActionToken;int own=++ticket,x=p.absX,y=p.absY;pending=true;
        Server.getEventHandler().submit(new Event<Player>("herbiboar-harvest",p,3){public void execute(){if(own==ticket&&token==p.productionActionToken&&ready()&&p.absX==x&&p.absY==y&&p.playerLevel[15]>=31&&prey==n&&OWNERS.get(n)==HerbiboarHunting.this&&registered(n)&&!harvested){
            completeHarvest(n);
        }stop();}public void stop(){super.stop();if(own==ticket)pending=false;if(token==p.productionActionToken)p.startAnimation(65535);}});return true;
    }
    private void completeHarvest(NPC n){
        if(!ProductionBatch.canExchange(p,new int[0][],harvest.items))return;
        int pet=HerbiboarRewards2022.pet(RANDOM)?HerbiboarRewards2022.HERBI:-1;
        PetInsurance2022.RewardCommit commit=p.getPetInsurance().exchange(new int[0][],harvest.items,pet,p.getPosition());
        if(commit==null)return;
        harvested=true;p.getPA().addSkillXPMultiplied(harvest.herbloreXp,15,true);p.getPA().createPlayerHints(0,0);p.sendMessage("You harvest the herbs from the herbiboar.");
        commit.award();flee(n);
    }
    private void flee(NPC old){old.requestTransform(7786);old.startAnimation(7689);
        Server.getEventHandler().submit(new Event<NPC>("herbiboar-flee",old,2){int stage;public void execute(){if(!registered(old)){stop();return;}if(stage++==0){old.startAnimation(7690);return;}OWNERS.remove(old,HerbiboarHunting.this);old.unregisterInstant();if(prey==old){prey=null;harvest=null;harvested=false;}stop();}});
    }
    private void pet(){p.getPetInsurance().award(HerbiboarRewards2022.HERBI,false);}
}
