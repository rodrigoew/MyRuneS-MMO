package io.xeros.content.skills.construction;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

/** Crafting at the owner's native clockmaker's bench. */
public final class HouseClockmaking {
 public static final class Recipe {
  public final int output,level,table;public final int[][] materials;
  private Recipe(int output,int level,int table,int[][] materials){this.output=output;this.level=level;this.table=table;this.materials=materials;}
 }
 private static Recipe row(int output,int level,int table,int[][] materials){return new Recipe(output,level,table,materials);}
 private static final List<Recipe> RECIPES=Collections.unmodifiableList(Arrays.asList(
  row(2520,10,1,new int[][]{{960,1}}),row(2522,10,1,new int[][]{{960,1}}),
  row(2524,10,1,new int[][]{{960,1}}),row(2526,10,1,new int[][]{{960,1}}),
  row(8792,8,2,new int[][]{{2353,1}}),
  row(7759,13,3,new int[][]{{8792,1},{960,1}}),
  row(7763,18,3,new int[][]{{8792,1},{960,1}}),
  row(7767,33,4,new int[][]{{8792,1},{960,1}}),
  row(7771,85,4,new int[][]{{8792,1},{960,1}}),
  row(2574,23,4,new int[][]{{2353,1}}),
  row(2575,28,4,new int[][]{{2353,1},{8792,1}})
 ));
 private final PlayerHouse house;private final Player p;
 private final HouseWorkshopSeating seating;
 public HouseClockmaking(PlayerHouse house,Player p,HouseWorkshopSeating seating){this.house=house;this.p=p;this.seating=seating;}
 public static List<Recipe> recipes(){return RECIPES;}
 private int table(PlayerHouse.Binding b){HouseFurnishing f=house.furnishing(b);return f!=null&&f.key.startsWith("CRAFTING_TABLE_")?f.key.charAt(f.key.length()-1)-'0':0;}
 public void clear(){seating.clear();}
 public boolean seated(){return seating.seated();}
 public boolean click(PlayerHouse.Binding b,int option){
  if(option!=1||table(b)==0||b.root.id!=15441)return false;
  if(seating.sit(b))menu(b,0);return true;
 }
 private List<Recipe> available(PlayerHouse.Binding b){
  List<Recipe> out=new ArrayList<>();int tier=table(b);for(Recipe r:RECIPES)if(r.table<=tier)out.add(r);return out;
 }
 private void menu(PlayerHouse.Binding b,int page){
  if(!house.furnitureReady(b)||!seating.at(b))return;
  List<Recipe> rows=available(b);List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(rows.size(),page*3+3);i++){
   Recipe r=rows.get(i);options.add(new DialogueOption(ItemAssistant.getItemName(r.output)+" (Crafting "+r.level+")",q->quantity(b,r)));
  }
  if(page*3+3<rows.size())options.add(new DialogueOption("More",q->menu(b,page+1)));
  else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Clockmaking",options.toArray(new DialogueOption[0])).send();
 }
 private void quantity(PlayerHouse.Binding b,Recipe r){
  if(!house.furnitureReady(b)||!seating.at(b))return;
  new DialogueBuilder(p).option("How many?",new DialogueOption("Make 1",q->start(b,r,1)),new DialogueOption("Make 5",q->start(b,r,5)),new DialogueOption("Make All",q->start(b,r,10000))).send();
 }
 private boolean start(PlayerHouse.Binding b,Recipe r,int quantity){
  p.getPA().removeAllWindows();
  if(!RECIPES.contains(r)||quantity<1||!house.furnitureReady(b)||!seating.at(b)||r.table>table(b))return false;
  if(p.playerLevel[12]<r.level){p.sendMessage("You need Crafting level "+r.level+".");return false;}
  boolean ready=seated();p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;
  p.playerSkilling[12]=true;p.startAnimation(seating.craftAnimation());
  Server.getEventHandler().submit(new Event<Player>("skilling",p,ready?3:4){
   int remaining=Math.min(quantity,10000);
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[12]||!seating.ready(b)||p.getSession()==null||p.getLock().cannotInteract(p)||p.playerLevel[12]<r.level||r.table>table(b)){stop();return;}
    if(!ProductionBatch.exchange(p,r.materials,new int[][]{{r.output,1}})){stop();return;}
    p.getPA().addSkillXPMultiplied(15,12,true);p.startAnimation(seating.craftAnimation());p.sendMessage("You make the "+ItemAssistant.getItemName(r.output).toLowerCase()+".");
    maximumTicks=3;if(--remaining==0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[12]=false;p.startAnimation(65535);}}
  });return true;
 }
}
