package io.xeros.content.questing;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.GameItem;
import io.xeros.model.world.objects.GlobalObject;
import java.util.*;
import java.util.function.LongSupplier;

/** Quest transport and personal cave unlocks. Vine growth uses elapsed real time, including logout. */
public final class EagleTransport2022 {
    public enum Nest {
        PEAK_DESERT(1487,2020,4958,3,true), PEAK_JUNGLE(1488,2023,4961,3,true), PEAK_POLAR(1489,2026,4963,3,true),
        DESERT(1487,3422,9571,0,false), JUNGLE(1488,2516,9322,0,false), POLAR(1489,2727,10217,0,false);
        public final int npc,x,y,height;public final boolean peak;
        Nest(int npc,int x,int y,int height,boolean peak){this.npc=npc;this.x=x;this.y=y;this.height=height;this.peak=peak;}
        Nest other(){for(Nest nest:values())if(nest.npc==npc&&nest.peak!=peak)return nest;throw new IllegalStateException();}
        boolean contains(int x,int y,int h){return h==height&&x>=this.x&&x<this.x+3&&y>=this.y&&y<this.y+3;}
    }
    private static final Map<NPC,EagleTransport2022> OWNERS=new IdentityHashMap<>();
    private static EagleTransport2022 rockOwner;
    private final Player player;
    private final LongSupplier clock;
    private final EnumMap<Nest,NPC> birds=new EnumMap<>(Nest.class);
    private long vinePlantedAt;
    private final EnumMap<Nest,Long> nextRope=new EnumMap<>(Nest.class);
    private boolean desertOpen,notified;
    private int sentVine=-1;
    private GlobalObject boulder;
    public EagleTransport2022(Player player){this(player,System::currentTimeMillis);}
    public EagleTransport2022(Player player,LongSupplier clock){this.player=player;this.clock=clock;}
    public String encode(){return "1,"+vinePlantedAt+","+(desertOpen?1:0);}
    public void restore(String record){
        if(record==null||!record.matches("1,[0-9]{1,16},[01]"))return;
        String[] fields=record.split(",");long planted=Long.parseLong(fields[1]);if(planted>clock.getAsLong())return;
        vinePlantedAt=planted;desertOpen=fields[2].equals("1");sentVine=-1;notified=false;
    }
    public int vineStage(){if(vinePlantedAt==0)return 0;long age=Math.max(0,clock.getAsLong()-vinePlantedAt);return age<120000?1:age<2400000?2:3;}
    private boolean ready(){return player.getUtilityMagic().canInteract()&&player.getInstance()==null&&player.getHealth().getCurrentHealth()>0;}
    private boolean near(int x,int y,int h,int distance){return ready()&&player.heightLevel==h&&Math.max(Math.abs(player.absX-x),Math.abs(player.absY-y))<=distance;}
    private boolean complete(){return player.getQuesting().getEaglesPeak().isQuestCompleted();}
    private boolean clear(int x,int y,int h){RegionProvider r=player.getRegionProvider();return r.get(x,y).getTerrainFlags(x,y,h)>=0&&(r.getClipping(x,y,h)&0x1280100)==0;}
    private boolean live(NPC n){return n!=null&&n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n&&!n.isDead()&&!n.needRespawn&&!n.isInvisible()&&n.getInstance()==null;}
    public static boolean visible(Player viewer,NPC npc){EagleTransport2022 owner=OWNERS.get(npc);return owner==null||owner.player==viewer;}
    private void remove(NPC n){if(n!=null){OWNERS.remove(n,this);if(n.getIndex()>0&&NPCHandler.npcs[n.getIndex()]==n)n.unregisterInstant();}}
    public void process(){
        int stage=vineStage();if(stage!=sentVine){player.getPA().sendFrame87Masked(935,7,stage);sentVine=stage;}
        if(stage==3&&!notified){notified=true;player.sendMessage("The vine at the jungle eagle's cave is strong enough to climb now.");}
        for(Nest nest:Nest.values()){
            boolean wanted=complete()&&player.getInstance()==null&&player.heightLevel==nest.height&&Math.max(Math.abs(player.absX-nest.x),Math.abs(player.absY-nest.y))<=28;
            NPC current=birds.get(nest);
            if(!wanted||current!=null&&!live(current)){remove(current);birds.remove(nest);current=null;}
            if(wanted&&current==null){
                NPC bird=new NPC(nest.npc,new Position(nest.x,nest.y,nest.height)){
                    @Override public boolean canBeAttacked(Entity entity){return false;}
                    @Override public boolean canBeDamaged(Entity entity){return false;}
                };
                bird.randomWalk=false;bird.walkingType=0;bird.getBehaviour().setWalkHome(false);bird.getBehaviour().setAggressive(false);bird.getBehaviour().setRespawn(false);OWNERS.put(bird,this);birds.put(nest,bird);
            }
        }
        updateBoulder();
        // The original polar and desert eyries provide spare ropes. Keep one
        // private timed floor spawn near the exit, with a respawn delay.
        boolean desert=player.heightLevel==0&&player.absX>=3408&&player.absX<=3445&&player.absY>=9555&&player.absY<=9590;
        boolean polar=player.heightLevel==0&&player.absX>=2700&&player.absX<=2745&&player.absY>=10200&&player.absY<=10235;
        Nest ropeNest=desert?Nest.DESERT:Nest.POLAR;
        if((desert||polar)&&clock.getAsLong()>=nextRope.getOrDefault(ropeNest,0L)){int x=desert?3420:2709,y=desert?9562:10210;nextRope.put(ropeNest,clock.getAsLong()+60000);if(clear(x,y,0)&&Server.itemHandler.getGroundItem(player,954,x,y,0)==null)Server.itemHandler.createPrivateTimedGroundItem(player,new GameItem(954,1),new Position(x,y,0),200);}
    }
    private void updateBoulder(){
        boolean wanted=!desertOpen&&player.getInstance()==null&&player.heightLevel==0&&Math.max(Math.abs(player.absX-3405),Math.abs(player.absY-3160))<=20;
        if(!wanted){clearBoulder();return;}
        if(boulder==null&&(rockOwner==null||rockOwner==this)){
            rockOwner=this;
            // Reuse a native pushable boulder model for this server's cave lock;
            // the historical source does not establish its original scenery ID.
            boulder=new GlobalObject(5842,3404,3159,0,0,10,-1);Server.getGlobalObjects().add(boulder);
        }
    }
    private void clearBoulder(){if(boulder!=null){Server.getGlobalObjects().removeIfCurrent(boulder);boulder=null;}if(rockOwner==this)rockOwner=null;}
    public void logout(){for(NPC n:birds.values())remove(n);birds.clear();clearBoulder();sentVine=-1;}
    private boolean occupiedByNest(int x,int y,int h){for(Nest nest:Nest.values())if(nest.contains(x,y,h))return true;return false;}
    public Position arrival(Nest nest){
        List<Position> choices=new ArrayList<>();
        for(int x=nest.x-1;x<=nest.x+3;x++)for(int y=nest.y-1;y<=nest.y+3;y++)if(!occupiedByNest(x,y,nest.height)&&clear(x,y,nest.height))choices.add(new Position(x,y,nest.height));
        return choices.stream().min(Comparator.comparingInt(t->Math.abs(t.getX()-(nest.x-1))+Math.abs(t.getY()-nest.y))).orElse(null);
    }
    /** Called before the legacy large-NPC following code can choose an unrelated destination. */
    public boolean itemOnNpc(NPC npc,int item,int slot){
        if(npc==null||npc.getNpcId()<1487||npc.getNpcId()>1489||item!=954)return false;
        if(!ready()||!complete()||!live(npc)||OWNERS.get(npc)!=this||slot<0||slot>=player.playerItems.length||player.playerItems[slot]!=955||player.playerItemsN[slot]<1||!near(npc.absX,npc.absY,npc.heightLevel,16))return true;
        Nest from=null;for(Map.Entry<Nest,NPC> entry:birds.entrySet())if(entry.getValue()==npc){from=entry.getKey();break;}if(from==null)return true;
        player.stopMovement();player.getPA().resetFollow();player.clickNpcType=0;
        if(!near(npc.absX,npc.absY,npc.heightLevel,4)){
            List<Position> approaches=new ArrayList<>();for(int dx=-3;dx<=3;dx++)for(int dy=-3;dy<=3;dy++){int x=npc.absX+dx,y=npc.absY+dy;if(!npc.insideOf(x,y)&&clear(x,y,npc.heightLevel))approaches.add(new Position(x,y,npc.heightLevel));}
            approaches.sort(Comparator.comparingInt(t->Math.abs(t.getX()-player.absX)+Math.abs(t.getY()-player.absY)));
            for(Position tile:approaches)if(PathFinder.getPathFinder().accessable(player,tile.getX(),tile.getY())){PathFinder.getPathFinder().findRoute(player,tile.getX(),tile.getY(),true,1,1);player.setTickable(new io.xeros.model.tickable.impl.WalkToTickable(player,new Position(npc.absX-3,npc.absY-3,npc.heightLevel),7,7,p->itemOnNpc(npc,item,slot)));return true;}
            player.sendMessage("You cannot reach that eagle from here.");return true;
        }
        final Nest start=from,target=from.other();final Position landing=arrival(target);if(landing==null){player.sendMessage("There is no safe place to land at that nest.");return true;}
        player.facePosition(npc.absX,npc.absY);
        ProductionBatch.start(player,21,1,0,1,65535,3,new int[][]{{954,0}},new int[0][],()->{
            player.getPA().movePlayer(landing.getX(),landing.getY(),landing.getHeight());player.sendMessage("The eagle carries you to the other nest.");
        },()->ready()&&complete()&&birds.get(start)==npc&&live(npc)&&near(npc.absX,npc.absY,npc.heightLevel,4)&&player.playerItems[slot]==955&&clear(landing.getX(),landing.getY(),landing.getHeight()));return true;
    }
    private boolean live(WorldObject o){return o!=null&&player.heightLevel==o.height&&player.getRegionProvider().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,o.height).filter(o::equals).isPresent();}
    private boolean liveVine(){return player.getRegionProvider().get(2512,2927).getWorldObject(19993,2512,2927,0).isPresent();}
    private void passage(WorldObject o,int skill,int level,int x,int y,int h){
        if(!near(o.x,o.y,o.height,4)||!live(o))return;
        ProductionBatch.start(player,skill,level,0,1,828,3,new int[0][],new int[0][],()->player.getPA().movePlayer(x,y,h),()->ready()&&live(o)&&clear(x,y,h));
    }
    public boolean itemOnObject(int item,int object,int x,int y){
        if(x!=2512||y!=2927||object!=19993&&object!=19985&&object!=19986&&object!=19987&&object!=19988)return false;
        if(!near(x,y,0,3)||!liveVine())return true;
        if(!complete()){player.sendMessage("Complete Eagles' Peak before training this vine.");return true;}
        if(item!=10029&&item!=4189&&item!=6281&&item!=6283&&item!=6285){player.sendMessage("A teasing stick, garden cane or thatching spar could support this vine.");return true;}
        if(vinePlantedAt!=0){player.sendMessage("The vine already has a support.");return true;}
        ProductionBatch.start(player,21,1,0,1,827,2,new int[][]{{item,1}},new int[0][],()->{vinePlantedAt=clock.getAsLong();sentVine=-1;player.sendMessage("You support the young vine. It needs time to grow up the cliff.");},()->ready()&&complete()&&vinePlantedAt==0&&liveVine()&&near(x,y,0,3));return true;
    }
    public boolean object(WorldObject o,int option){
        if(o==null)return false;
        if(o.id==5842&&o.x==3404&&o.y==3159&&o.height==0){
            if(option==3&&boulder!=null&&!desertOpen&&near(o.x,o.y,0,3)&&live(o))ProductionBatch.start(player,2,45,0,1,839,3,new int[0][],new int[0][],()->{desertOpen=true;clearBoulder();player.sendMessage("You push the boulder away from the cave entrance.");},()->ready()&&!desertOpen&&live(o));return true;
        }
        if(o.x==3405&&o.y==3160&&o.id==19798){if(option==1){if(!desertOpen){player.sendMessage("The boulder must be pushed aside from outside; this needs Strength level 45.");return true;}passage(o,2,1,3420,9562,0);}return true;}
        if((o.id==19791&&o.x==3419&&o.y==9560)||(o.id==19792&&o.x==3427&&o.y==9561)){if(option==1){if(!desertOpen){player.sendMessage("The exit is blocked. Fly back and clear the boulder from outside.");return true;}passage(o,2,1,3405,3159,0);}return true;}
        if((o.id==19993||o.id>=19985&&o.id<=19989)&&o.x==2512&&o.y==2927||o.id==19753&&o.x==2511&&o.y==2927){if(option==1){if(!complete()||vineStage()!=3){player.sendMessage("The vine is not strong enough to climb yet.");return true;}passage(o,16,1,2526,9325,0);}return true;}
        if((o.id==19759&&o.x==2527&&o.y==9324)||(o.id==19760&&o.x==2527&&o.y==9317)){if(option==1)passage(o,16,1,2513,2926,0);return true;}
        if(o.id==19762&&o.x==2745&&o.y==3830&&o.height==1){if(option==1)passage(o,16,35,2709,10210,0);return true;}
        if((o.id==19763&&o.x==2707&&o.y==10209)||(o.id==19764&&o.x==2705&&o.y==10204)){if(option==1)passage(o,16,1,2744,3830,1);return true;}
        if((o.id==19846&&o.x==2741||o.id==19847&&(o.x==2742||o.x==2743))&&o.y==3830&&o.height==1){if(option==1)passage(o,16,35,player.absX<=2742?2744:2740,3830,1);return true;}
        return false;
    }
}
