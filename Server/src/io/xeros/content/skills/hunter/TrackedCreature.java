package io.xeros.content.skills.hunter;

import io.xeros.Server;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import java.util.*;

/** Shared action ownership and packed trail fields for creatures caught with a noose wand. */
public class TrackedCreature {
    protected static final class Place {
        final int x,y;
        final boolean burrow,target;
        Place(int x,int y,boolean burrow){this(x,y,burrow,!burrow);}
        Place(int x,int y,boolean burrow,boolean target){this.x=x;this.y=y;this.burrow=burrow;this.target=target;}
    }
    protected static final class Edge {
        final int bit,from,to,plantX,plantY,parent,shift;
        Edge(int bit,int from,int to,int plantX,int plantY){this(bit,from,to,plantX,plantY,bit<=2983?919:920,(bit-(bit<=2983?2974:2985))*3);}
        Edge(int bit,int from,int to,int plantX,int plantY,int parent,int shift){
            this.bit=bit;this.from=from;this.to=to;this.plantX=plantX;this.plantY=plantY;this.parent=parent;this.shift=shift;
        }
    }
    protected static final class Step {
        final Edge edge;
        final int to,value,inspectX,inspectY;
        Step(Edge edge,int to){this(edge,to,edge.plantX,edge.plantY);}
        Step(Edge edge,int to,int inspectX,int inspectY){this.edge=edge;this.to=to;this.inspectX=inspectX;this.inspectY=inspectY;value=to==edge.to?4:5;}
    }
    protected static final class Profile {
        final String name;
        final int level,product,animation,projectile,height,counterParent,counterShift;
        final double xp;
        final int[] bounds;
        final Place[] places;
        final Edge[] edges;
        final Set<Integer> startIds,searchIds,catchIds;
        final Map<Integer,List<List<Step>>> routes;
        final Map<Integer,Integer> masks=new LinkedHashMap<>();
        Profile(String name,int level,double xp,int product,int animation,int projectile,int height,int[] bounds,
                Place[] places,Edge[] edges,int[] startIds,int[] searchIds,int[] catchIds,int counterParent,int counterShift,
                Map<Integer,List<List<Step>>> routes){
            this.name=name;this.level=level;this.xp=xp;this.product=product;this.animation=animation;this.projectile=projectile;this.height=height;
            this.bounds=bounds.clone();this.places=places;this.edges=edges;this.startIds=ids(startIds);this.searchIds=ids(searchIds);this.catchIds=ids(catchIds);
            this.counterParent=counterParent;this.counterShift=counterShift;this.routes=Collections.unmodifiableMap(new HashMap<>(routes));
            for(Edge edge:edges)masks.merge(edge.parent,7<<edge.shift,(a,b)->a|b);
            masks.merge(counterParent,3<<counterShift,(a,b)->a|b);
            for(int start=0;start<places.length;start++)if(places[start].burrow&&(!routes.containsKey(start)||routes.get(start).isEmpty()))
                throw new IllegalArgumentException("Missing "+name+" tracking routes at "+places[start].x+","+places[start].y);
        }
        private static Set<Integer> ids(int[] values){Set<Integer> result=new HashSet<>();for(int value:values)result.add(value);return Collections.unmodifiableSet(result);}
    }
    /** Local route reconstruction: links cannot repeat, nodes may be revisited. */
    protected static Map<Integer,List<List<Step>>> routes(Place[] places,Edge[] edges,int minimumSections){
        Map<Integer,List<List<Step>>> result=new LinkedHashMap<>();
        for(int start=0;start<places.length;start++)if(places[start].burrow){
            List<List<Step>> choices=new ArrayList<>();enumerate(places,edges,minimumSections,start,new HashSet<>(),new ArrayList<>(),choices);
            result.put(start,Collections.unmodifiableList(choices));
        }
        return Collections.unmodifiableMap(result);
    }
    private static void enumerate(Place[] places,Edge[] edges,int minimum,int at,Set<Edge> used,List<Step> path,List<List<Step>> result){
        if(path.size()>=minimum&&places[at].target){result.add(Collections.unmodifiableList(new ArrayList<>(path)));return;}
        for(Edge edge:edges){
            int next=edge.from==at?edge.to:edge.to==at?edge.from:-1;
            if(next<0||used.contains(edge))continue;
            used.add(edge);path.add(new Step(edge,next));enumerate(places,edges,minimum,next,used,path,result);
            path.remove(path.size()-1);used.remove(edge);
        }
    }
    private final Player player;
    private final Profile profile;
    private final Random random;
    private List<Step> trail;
    private int shown,action;
    private long pendingToken;
    private boolean initialized,pending;
    protected TrackedCreature(Player player,Profile profile,Random random){this.player=player;this.profile=profile;this.random=random;}
    private boolean habitat(){return player.heightLevel==profile.height&&player.absX>=profile.bounds[0]&&player.absX<=profile.bounds[1]
        &&player.absY>=profile.bounds[2]&&player.absY<=profile.bounds[3]&&player.getInstance()==null&&player.getRegionProvider()==RegionProvider.getGlobal();}
    private boolean available(){return habitat()&&player.getUtilityMagic().canInteract()&&!player.getMovementState().isLocked();}
    public final void process(){
        if(!initialized){initialized=true;sendTracks();}
        if(trail!=null&&(!habitat()||player.isDead||player.isDisconnected()))clear();
    }
    private void clear(){trail=null;shown=0;pending=false;action++;sendTracks();}
    private void sendTracks(){
        Map<Integer,Integer> values=new LinkedHashMap<>();for(int parent:profile.masks.keySet())values.put(parent,0);
        if(trail!=null){
            for(int i=0;i<shown;i++){Step step=trail.get(i);values.merge(step.edge.parent,step.value<<step.edge.shift,(a,b)->a|b);}
            values.merge(profile.counterParent,Math.min(3,Math.max(0,shown-1))<<profile.counterShift,(a,b)->a|b);
        }
        for(Map.Entry<Integer,Integer> entry:profile.masks.entrySet())player.getPA().sendFrame87Masked(entry.getKey(),entry.getValue(),values.get(entry.getKey()));
    }
    private int start(WorldObject object){
        if(!profile.startIds.contains(object.id))return -1;
        for(int i=0;i<profile.places.length;i++){Place place=profile.places[i];if(place.burrow&&object.x==place.x&&object.y==place.y)return i;}
        return -1;
    }
    private boolean scenery(WorldObject object){return object.height==profile.height&&object.x>=profile.bounds[0]&&object.x<=profile.bounds[1]
        &&object.y>=profile.bounds[2]&&object.y<=profile.bounds[3]&&(profile.startIds.contains(object.id)||profile.searchIds.contains(object.id)||profile.catchIds.contains(object.id));}
    private static boolean live(WorldObject object){return RegionProvider.getGlobal().get(object.x,object.y).getWorldObject(object.id,object.x,object.y,object.height).filter(object::equals).isPresent();}
    private boolean near(WorldObject object){
        Position size=object.getObjectSize();int dx=Math.max(object.x-player.absX,Math.max(0,player.absX-object.x-Math.max(1,size.getX())+1));
        int dy=Math.max(object.y-player.absY,Math.max(0,player.absY-object.y-Math.max(1,size.getY())+1));return Math.max(dx,dy)<=1;
    }
    public final boolean click(WorldObject object,int option){
        if(object==null||!scenery(object))return false;
        process();if(!available()||!near(object)||!live(object))return true;
        if(pending&&pendingToken!=player.productionActionToken){pending=false;action++;}
        if(pending)return true;
        int start=start(object);
        if(start>=0&&trail==null){
            if(option!=1)return true;
            if(player.playerLevel[21]<profile.level){player.sendMessage("You need Hunter level "+profile.level+" to track this creature.");return true;}
            search(object,()->{
                List<List<Step>> choices=profile.routes.get(start);trail=choices.get(random.nextInt(choices.size()));
                shown=player.getRingOfPursuit().revealNewTrail()?trail.size():1;
                sendTracks();player.sendMessage("You find kebbit tracks. Follow them and inspect the scenery.");
            });
            return true;
        }
        boolean target=profile.catchIds.contains(object.id)&&trail!=null&&shown==trail.size()
            &&object.x==profile.places[trail.get(shown-1).to].x&&object.y==profile.places[trail.get(shown-1).to].y;
        if(profile.catchIds.contains(object.id)&&option==2){
            if(!target){player.startAnimation(5255);player.sendMessage("You find nothing hiding here.");return true;}
            if(player.playerEquipment[Player.playerWeapon]!=10150){clear();player.sendMessage("Without an equipped noose wand, you scare the kebbit away.");return true;}
            capture(object);return true;
        }
        if(option==1)search(object,()->{
            if(target){player.sendMessage("There is a rustling sound. A creature is hiding here.");return;}
            if(trail!=null&&shown<trail.size()){
                Step next=trail.get(shown);
                if(object.x==next.inspectX&&object.y==next.inspectY){shown++;sendTracks();player.sendMessage("You find more tracks leading away.");return;}
            }
            player.sendMessage("You find no new tracks here.");
        });
        return true;
    }
    private void search(WorldObject object,Runnable result){perform(object,5216,5,false,result);}
    private void capture(WorldObject object){perform(object,profile.animation,3,true,()->{
        clear();
        for(int id:new int[]{526,9986,profile.product})if(!player.getItems().addItem(id,1,false))
            Server.itemHandler.createGroundItem(player,id,player.absX,player.absY,player.heightLevel,1,player.getIndex());
        player.getPA().addSkillXPMultiplied(profile.xp,21,true);player.sendMessage("You catch a "+profile.name+".");
    });}
    private void perform(WorldObject object,int animation,int ticks,boolean capture,Runnable result){
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();player.facePosition(object.x,object.y);player.startAnimation(animation);
        final int ticket=++action,x=player.absX,y=player.absY;final long token=player.productionActionToken;pending=true;pendingToken=token;
        if(capture)player.getPA().createPlayersProjectile(object.x,object.y,y-object.y,x-object.x,0,60,profile.projectile,0,0,-player.getIndex()-1,0);
        Server.getEventHandler().submit(new Event<Player>("noose-tracking",player,ticks){
            public void execute(){
                if(ticket==action&&token==player.productionActionToken&&available()&&player.absX==x&&player.absY==y&&player.playerLevel[21]>=profile.level
                    &&live(object)&&(!capture||player.playerEquipment[Player.playerWeapon]==10150))result.run();
                stop();
            }
            public void stop(){super.stop();if(ticket==action)pending=false;}
        });
    }
}
