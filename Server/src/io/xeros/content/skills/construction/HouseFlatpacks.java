package io.xeros.content.skills.construction;
import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Unnoted flatpack production and installation in an empty owned hotspot. */
public final class HouseFlatpacks {
 public static final int PRODUCTION_TICKS=8;
 private final PlayerHouse house;private final Player p;private final HouseWorkshopSeating seating;
 public HouseFlatpacks(PlayerHouse house,Player p,HouseWorkshopSeating seating){this.house=house;this.p=p;this.seating=seating;}
 private int limit(PlayerHouse.Binding b){HouseFurnishing f=house.furnishing(b);return f==null?0:HouseFlatpackData.limit(f.key);}
 private boolean ready(PlayerHouse.Binding b){return house.furnitureReady(b)&&seating.at(b)&&limit(b)>0;}
 public boolean click(PlayerHouse.Binding b,int option){
  if(option!=1||b.root.id!=15439||limit(b)==0)return false;
  if(seating.sit(b))categories(b,0);return true;
 }
 private List<String> categories(PlayerHouse.Binding b){
  Set<String> names=new LinkedHashSet<>();for(var r:HouseFlatpackData.all())if(r.level<=limit(b))names.add(r.category);return new ArrayList<>(names);
 }
 private void categories(PlayerHouse.Binding b,int page){
  if(!ready(b))return;List<String> rows=categories(b);List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(rows.size(),page*3+3);i++){String category=rows.get(i);options.add(new DialogueOption(category,q->recipes(b,category,0)));}
  if(page*3+3<rows.size())options.add(new DialogueOption("More",q->categories(b,page+1)));
  else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Flatpack furniture",options.toArray(new DialogueOption[0])).send();
 }
 private void recipes(PlayerHouse.Binding b,String category,int page){
  if(!ready(b))return;List<HouseFlatpackData.Recipe> rows=new ArrayList<>();for(var r:HouseFlatpackData.all())if(r.category.equals(category)&&r.level<=limit(b))rows.add(r);List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(rows.size(),page*3+3);i++){var r=rows.get(i);options.add(new DialogueOption(r.name+" (Construction "+r.level+")",q->quantity(b,r)));}
  if(page*3+3<rows.size())options.add(new DialogueOption("More",q->recipes(b,category,page+1)));
  else options.add(new DialogueOption("Back",q->categories(b,0)));
  new DialogueBuilder(p).option(category,options.toArray(new DialogueOption[0])).send();
 }
 private void quantity(PlayerHouse.Binding b,HouseFlatpackData.Recipe r){
  if(!ready(b))return;
  new DialogueBuilder(p).option("How many?",new DialogueOption("Make 1",q->start(b,r,1)),new DialogueOption("Make 5",q->start(b,r,5)),new DialogueOption("Make All",q->start(b,r,10000))).send();
 }
 private boolean start(PlayerHouse.Binding b,HouseFlatpackData.Recipe r,int quantity){
  p.getPA().removeAllWindows();
  if(HouseFlatpackData.get(r.item)!=r||quantity<1||!ready(b)||r.level>limit(b))return false;
  if(p.playerLevel[22]<r.level){p.sendMessage("You need Construction level "+r.level+".");return false;}
  if(p.playerLevel[7]<r.cookingLevel){p.sendMessage("You need Cooking level "+r.cookingLevel+".");return false;}
  if(!house.constructionTools())return false;
  p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;p.playerSkilling[22]=true;p.startAnimation(seating.craftAnimation());
  Server.getEventHandler().submit(new Event<Player>("skilling",p,PRODUCTION_TICKS){
   int remaining=Math.min(quantity,10000);
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[22]||!seating.ready(b)||p.getSession()==null||p.getLock().cannotInteract(p)||p.playerLevel[22]<r.level||p.playerLevel[7]<r.cookingLevel||r.level>limit(b)||!house.constructionTools()){stop();return;}
    int[][] materials=house.materialInputs(r.materials);
    if(materials==null||!ProductionBatch.exchange(p,materials,new int[][]{{r.item,1}})){stop();return;}
    p.getPA().addSkillXPMultiplied(r.xp,22,true);p.startAnimation(seating.craftAnimation());p.sendMessage("You make the "+r.name.toLowerCase()+".");if(--remaining==0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[22]=false;p.startAnimation(65535);}}
  });return true;
 }
 public boolean install(int item,WorldObject object){
  HouseFlatpackData.Recipe r=HouseFlatpackData.get(item);if(r==null)return false;
  if(!house.installFlatpack(r,object))p.sendMessage("Use this flatpack on its empty hotspot in your house's building mode, with a hammer.");
  return true;
 }
}
