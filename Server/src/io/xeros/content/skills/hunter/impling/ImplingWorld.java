package io.xeros.content.skills.hunter.impling;

import com.google.gson.Gson;
import io.xeros.Server;
import io.xeros.model.Direction;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Position;
import java.nio.file.*;
import java.util.*;

/** Owns the published 2022 spawn coordinates and the lifetime of each produced impling. */
public final class ImplingWorld {
    public static final Random RANDOM=new Random();
    private static Engine engine;
    private ImplingWorld(){}
    private static final class Point {int x,y,species;String tier;}
    private static final class Weight {int species,weight;}
    private static final class Dataset {List<Point> staticPuro,dynamicPuro,surfaceRollers,surfaceLow;Map<String,List<Weight>> tables;}
    private static void require(boolean b,String s){if(!b)throw new IllegalStateException("Impling world: "+s);}
    public static void load()throws Exception{
        Dataset data=new Gson().fromJson(Files.readString(Path.of(Server.getDataDirectory(),"cfg/npc/impling_world_2022.json")),Dataset.class);
        require(data!=null&&data.staticPuro!=null&&data.staticPuro.size()==51&&data.dynamicPuro!=null&&data.dynamicPuro.size()==24&&data.surfaceRollers!=null&&data.surfaceRollers.size()==42&&data.surfaceLow!=null&&data.surfaceLow.size()==11,"missing historical spawn data");
        int[] count=new int[6];for(Point p:data.staticPuro){require(p.species>=0&&p.species<6&&Impling.puro(p.x,p.y,0),"static spawn");count[p.species]++;}
        require(Arrays.equals(count,new int[]{13,10,11,8,5,4}),"static species counts");
        for(String key:new String[]{"low","mid","high","puro_high"}){List<Weight> rows=data.tables.get(key);require(rows!=null&&!rows.isEmpty(),"missing weights "+key);int sum=0;for(Weight w:rows){require(w.species>=0&&w.species<12&&w.species!=9&&w.weight>0,"bad species weight");sum+=w.weight;}require(sum==(key.equals("high")?101:key.equals("puro_high")?301:100),"probability mass "+key);}
        for(Point p:data.dynamicPuro)require(Impling.puro(p.x,p.y,0)&&Set.of("low","mid","puro_high").contains(p.tier),"dynamic Puro spawn");
        if(engine!=null)engine.close();engine=new Engine(data);Engine current=engine;current.start();
        Server.getEventHandler().submit(new Event<Engine>("impling-world",current,1){public void execute(){if(engine!=current||!current.active)stop();else current.pulse();}});
    }
    public static boolean managed(NPC npc){return engine!=null&&engine.owned.containsKey(npc);}
    /** Called only after the capture transaction has committed; an unmanaged cheat spawn uses the fallback. */
    static boolean captured(NPC npc){return engine!=null&&engine.capture(npc);}
    static ImplingData choose(Dataset data,String tier,Random random){List<Weight> rows=data.tables.get(tier);int sum=rows.stream().mapToInt(w->w.weight).sum(),roll=random.nextInt(sum);for(Weight row:rows){if(roll<row.weight)return ImplingData.values()[row.species];roll-=row.weight;}throw new AssertionError("Validated spawn table");}
    /** Floor occupancy is irrelevant to flight; wall edges and diagonal corners still block it. */
    public static boolean canFly(RegionProvider regions,int x,int y,int height,int dx,int dy){
        if(Math.abs(dx)>1||Math.abs(dy)>1||dx==0&&dy==0||x+dx<1||x+dx>16382||y+dy<1||y+dy>16382)return false;
        int source=regions.getClipping(x,y,height),target=regions.getClipping(x+dx,y+dy,height);
        if(dx!=0&&dy!=0){
            if(!canFly(regions,x,y,height,dx,0)||!canFly(regions,x,y,height,0,dy)||!canFly(regions,x+dx,y,height,0,dy)||!canFly(regions,x,y+dy,height,dx,0))return false;
            int to=dx>0?(dy>0?0xe0:0x83):(dy>0?0x38:0x0e),from=dx>0?(dy>0?0x0e:0x38):(dy>0?0x83:0xe0);
            return (target&to)==0&&(source&from)==0;
        }
        int out=dx<0?0x80:dx>0?0x08:dy>0?0x02:0x20,in=dx<0?0x08:dx>0?0x80:dy>0?0x20:0x02;
        return (source&out)==0&&(target&in)==0;
    }
    private static final class Slot {
        final Point anchor;final String tier;final boolean puro,permanent;final ImplingData fixed;
        NPC npc;int phase;long due,nextMove;
        Slot(Point point,String tier,boolean puro,boolean permanent,ImplingData fixed){anchor=point;this.tier=tier;this.puro=puro;this.permanent=permanent;this.fixed=fixed;}
    }
    private static final class Engine {
        final Dataset data;final List<Slot> slots=new ArrayList<>();final Map<NPC,Slot> owned=new IdentityHashMap<>();
        long clock,nextSurface=3000;boolean active=true;
        Engine(Dataset data){this.data=data;}
        void start(){
            for(Point p:data.staticPuro){Slot s=new Slot(p,null,true,true,ImplingData.values()[p.species]);slots.add(s);visible(s,s.fixed);}
            for(Point p:data.dynamicPuro){Slot s=new Slot(p,p.tier,true,true,null);slots.add(s);hidden(s);}
            for(Point p:data.surfaceLow){Slot s=new Slot(p,"low",false,true,null);slots.add(s);hidden(s);}
            surfaceCycle();
        }
        void configure(NPC n){n.randomWalk=false;n.walkingType=0;n.walkingHome=false;n.getBehaviour().setWalkHome(false);n.getBehaviour().setAggressive(false);n.getBehaviour().setRespawn(false);}
        void spawn(Slot s,ImplingData type,boolean invisible){
            int id=s.puro?type.npcId:type.npcIds()[0];s.npc=new NPC(id,new Position(s.anchor.x,s.anchor.y,0));configure(s.npc);s.npc.setInvisible(invisible);owned.put(s.npc,s);s.nextMove=clock+2;
        }
        void hidden(Slot s){removeNpc(s);spawn(s,ImplingData.BABY,true);s.phase=1;s.due=clock+(s.puro||s.permanent?200:100+RANDOM.nextInt(201));}
        void visible(Slot s,ImplingData kind){
            if(s.npc==null)spawn(s,kind,false);else{s.npc.requestTransform(s.puro?kind.npcId:kind.npcIds()[0]);configure(s.npc);s.npc.setInvisible(false);s.npc.setUpdateRequired(true);}
            s.phase=2;s.due=s.fixed==null?clock+3000:Long.MAX_VALUE;
        }
        void removeNpc(Slot s){if(s.npc!=null){owned.remove(s.npc);if(s.npc.getIndex()>0&&NPCHandler.npcs[s.npc.getIndex()]==s.npc)s.npc.unregisterInstant();s.npc=null;}}
        boolean capture(NPC npc){Slot s=owned.get(npc);if(s==null)return false;removeNpc(s);
            if(!s.permanent){slots.remove(s);return true;}
            if(s.fixed!=null){s.phase=0;s.due=clock+(s.fixed==ImplingData.EARTH||s.fixed==ImplingData.ESSENCE?50:7);}else hidden(s);return true;
        }
        void surfaceCycle(){
            for(Iterator<Slot> it=slots.iterator();it.hasNext();){Slot s=it.next();if(!s.permanent){removeNpc(s);it.remove();}}
            for(Point p:data.surfaceRollers)for(int i=0;i<3;i++){int roll=RANDOM.nextInt(100);String tier=roll<14?"low":roll<21?"mid":roll<25?"high":null;if(tier!=null){Slot s=new Slot(p,tier,false,false,null);slots.add(s);hidden(s);}}
        }
        void pulse(){
            clock++;if(clock>=nextSurface){surfaceCycle();nextSurface=clock+3000;}
            for(Slot s:new ArrayList<>(slots)){
                NPC n=s.npc;
                if(n!=null&&(n.getIndex()<=0||NPCHandler.npcs[n.getIndex()]!=n||n.isUnregister())){owned.remove(n);s.npc=null;s.phase=0;s.due=clock+(s.fixed==null?1:s.fixed==ImplingData.EARTH||s.fixed==ImplingData.ESSENCE?50:7);}
                if(clock>=s.due){if(s.phase==0){if(s.fixed!=null)visible(s,s.fixed);else if(s.permanent)hidden(s);else{slots.remove(s);continue;}}else if(s.phase==1)visible(s,choose(data,s.tier,RANDOM));else if(s.permanent)hidden(s);else{removeNpc(s);slots.remove(s);continue;}}
                n=s.npc;if(n==null||n.isDead()||n.needRespawn||n.freezeTimer!=0||Impling.reserved(n)||clock<s.nextMove)continue;
                s.nextMove=clock+2+RANDOM.nextInt(4);int direction=RANDOM.nextInt(8);int[] dx={-1,1,0,0,-1,-1,1,1},dy={0,0,-1,1,-1,1,-1,1};int x=n.absX+dx[direction],y=n.absY+dy[direction];
                if(Math.abs(x-s.anchor.x)>100||Math.abs(y-s.anchor.y)>100||s.puro&&!Impling.puro(x,y,0))continue;
                if(canFly(n.getRegionProvider(),n.absX,n.absY,0,dx[direction],dy[direction]))n.setMovement(Direction.fromDeltas(n.getPosition(),new Position(x,y,0)));
            }
        }
        void close(){active=false;for(Slot s:slots)removeNpc(s);slots.clear();owned.clear();}
    }
}
