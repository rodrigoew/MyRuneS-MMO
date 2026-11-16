package io.xeros.content.shooting_star;

import io.xeros.Server;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** One shared, finite star; pre-2023 quantity-based layers, not timed seven-minute layers.
 * Historical data: Wiki rev14354288. Landing anchors reference OpenRune (ISC).
 * All mutation runs on the game thread, including the three-health-per-tick ceiling. */
public final class ShootingStarsWorld {
 public static final ShootingStarsWorld INSTANCE=new ShootingStarsWorld();
 public static final class Discovery {
  public final String player,place;public final int size;
  Discovery(String player,String place,int size){this.player=player;this.place=place;this.size=size;}
 }
 private StarLocations.Site site,plannedSite;private StarLayer layer;private GlobalObject shown;
 private WorldObject displaced;private final Map<String,GlobalObject> restored=new HashMap<>();
 private final Deque<Discovery> history=new ArrayDeque<>();
 private int health,usedThisTick;private long generation,cycle,nextLanding;private boolean initialized,discovered;
 private ShootingStarsWorld(){}
 public StarLocations.Site site(){return site;}public StarLayer layer(){return layer;}
 public int health(){return health;}public long generation(){return generation;}public long cycle(){return cycle;}
 public long nextLanding(){return nextLanding;}public List<Discovery> history(){return new ArrayList<>(history);}
 public StarLocations.Site plannedSite(){return plannedSite;}
 public void initialize(){if(initialized)return;initialized=true;nextLanding=cycle+2000+Misc.trueRand(3001);plannedSite=choose(Misc::trueRand);
  Server.getEventHandler().submit(new Event<ShootingStarsWorld>("shooting-stars-world",this,1){public void execute(){tick(Misc::trueRand);}});
 }
 public void tick(IntUnaryOperator random){cycle++;usedThisTick=0;if(site!=null&&health==0){generation++;if(layer.size==1)clear();else{layer=StarLayer.size(layer.size-1);health=layer.health;publish();}}if(nextLanding>0&&cycle>=nextLanding)natural(random);}
 private StarLocations.Site choose(IntUnaryOperator random){
  List<StarLocations.Site> choices=new ArrayList<>(StarLocations.ALL);choices.removeIf(s->s.key.equals("DAEYALT_ESSENCE_MINE"));
  StarLocations.Site next=choices.get(random.applyAsInt(choices.size()));
  if(site!=null&&site==next)next=StarLocations.find("DAEYALT_ESSENCE_MINE");
  return next;
 }
 public void natural(IntUnaryOperator random){
  StarLocations.Site next=plannedSite==null?choose(random):plannedSite;
  spawn(next,StarLayer.naturalSize(random.applyAsInt(100)));
  // 600 ms ticks: 105–135 minutes, the historical two-hour cycle with variation.
  nextLanding=cycle+10500+random.applyAsInt(3001);plannedSite=choose(random);
 }
 public long[] forecastWindow(int precisionMinutes){
  if(precisionMinutes!=24&&precisionMinutes!=9&&precisionMinutes!=2)throw new IllegalArgumentException("Telescope precision");
  initialize();long width=precisionMinutes*100L;long begin=nextLanding/width*width;
  return new long[]{Math.max(0,begin-cycle),Math.max(0,begin+width-cycle)};
 }
 private static String time(long ticks){long seconds=ticks*3/5;return seconds/60+"m "+seconds%60+"s";}
 public String forecast(int precisionMinutes){
  long[] window=forecastWindow(precisionMinutes);
  return "The next star will fall in "+plannedSite.region+". Estimated arrival: "+time(window[0])+" to "+time(window[1])+".";
 }
 public void spawn(StarLocations.Site next,int size){
  if(next==null||!StarLocations.ALL.contains(next)||StarLayer.size(size)==null)throw new IllegalArgumentException("Unknown star site or size");
  clear();site=next;layer=StarLayer.size(size);health=layer.health;discovered=false;usedThisTick=0;generation++;
  Region region=RegionProvider.getGlobal().get(site.x,site.y);
  GlobalObject previous=restored.remove(site.key);if(previous!=null){Server.getGlobalObjects().remove(previous);region.removeWorldObject(previous);}
  displaced=region.worldObjectsSnapshot().stream().filter(o->o.x==site.x&&o.y==site.y&&o.height==site.height&&(o.type==10||o.type==11)).findFirst().orElse(null);
  if(previous!=null)displaced=new WorldObject(previous.getObjectId(),site.x,site.y,site.height,previous.getType(),previous.getFace());
  if(displaced!=null){region.removeObject(displaced.id,displaced.x,displaced.y,displaced.height,displaced.type,displaced.face);region.removeWorldObject(new GlobalObject(displaced.id,displaced.x,displaced.y,displaced.height,displaced.face,displaced.type,-1,-1));}
  publish();sync();nextLanding=cycle+12000;plannedSite=choose(Misc::trueRand);
 }
 private void publish(){if(shown!=null)removeShown();shown=new GlobalObject(layer.objectId,site.x,site.y,site.height,0,10,-1,-1);Server.getGlobalObjects().add(shown);}
 private void removeShown(){Server.getGlobalObjects().remove(shown);RegionProvider.getGlobal().get(shown.getX(),shown.getY()).removeWorldObject(shown);shown=null;}
 public void clear(){
  if(site==null)return;StarLocations.Site old=site;if(shown!=null)removeShown();
  if(displaced!=null){GlobalObject back=new GlobalObject(displaced.id,displaced.x,displaced.y,displaced.height,displaced.face,displaced.type,-1,-1);Server.getGlobalObjects().add(back);restored.put(old.key,back);}
  // Collision masks are bitsets, not reference counts: removal must preserve nearby objects.
  Set<Region> regions=new HashSet<>();for(int x:new int[]{old.x-8,old.x,old.x+8})for(int y:new int[]{old.y-8,old.y,old.y+8})regions.add(RegionProvider.getGlobal().get(x,y));
  for(Region region:regions)for(WorldObject o:region.worldObjectsSnapshot())if(o.height==old.height&&Math.abs(o.x-old.x)<=8&&Math.abs(o.y-old.y)<=8)region.addObject(o.id,o.x,o.y,o.height,o.type,o.face);
  site=null;layer=null;health=0;displaced=null;generation++;sync();
 }
 private void sync(){ShootingStar.spawned=site!=null;ShootingStar.lastX=site==null?-1:site.x;ShootingStar.lastY=site==null?-1:site.y;}
 public boolean matches(int id,int x,int y,int height){return site!=null&&layer.objectId==id&&site.x==x&&site.y==y&&site.height==height&&RegionProvider.getGlobal().get(x,y).isWorldObject(id,x,y,height);}
 public boolean near(Player p){return site!=null&&p.getInstance()==null&&p.heightLevel==site.height&&Math.max(Math.max(site.x-p.absX,p.absX-site.x-1),Math.max(site.y-p.absY,p.absY-site.y-1))<=1;}
 public void discover(Player p){if(discovered||!near(p))return;discovered=true;p.getStarPlayerState().discover();String name=p.getDisplayName();if(name==null||name.isEmpty())name=p.getLoginName();history.addFirst(new Discovery(name,site.name,layer.size));while(history.size()>5)history.removeLast();p.sendMessage("You are the first to discover this size "+layer.size+" crashed star!");}
 // Publish exhaustion on the following tick, so all miners on the exhausting tick receive loot.
 public void harvested(){if(site!=null&&usedThisTick++<3)health=Math.max(0,health-1);}
}
