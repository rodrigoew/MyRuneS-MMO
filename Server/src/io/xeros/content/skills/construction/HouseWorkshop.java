package io.xeros.content.skills.construction;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;

/** Owner-scoped workshop interactions, separate from permanent layout data. */
public final class HouseWorkshop {
 private final PlayerHouse house;private final Player p;
 private final HouseClockmaking clockmaking;
 private final HouseHeraldry heraldry;
 private final HouseFlatpacks flatpacks;private final HouseWorkshopSeating seating;private final HouseRepairs repairs;
 public HouseWorkshop(PlayerHouse house,Player p){this.house=house;this.p=p;heraldry=new HouseHeraldry(house,p);seating=new HouseWorkshopSeating(house,p);clockmaking=new HouseClockmaking(house,p,seating);flatpacks=new HouseFlatpacks(house,p,seating);repairs=new HouseRepairs(house,p);}
 public HouseHeraldry heraldry(){return heraldry;}
 public HouseClockmaking clockmaking(){return clockmaking;}
 public HouseFlatpacks flatpacks(){return flatpacks;}
 public HouseRepairs repairs(){return repairs;}
 public boolean use(int item,WorldObject object){return heraldry.use(item,object)||repairs.use(item,object);}
 public void clear(){seating.clear();}
 public boolean click(WorldObject object,int option){
  PlayerHouse.Binding b=house.furnitureFixture(object);
  if(b==null||b.room.kind!=PlayerHouse.Kind.WORKSHOP)return false;
  HouseFurnishing f=house.furnishing(b);
  if(heraldry.click(b,option))return true;
  if(clockmaking.click(b,option))return true;
  if(flatpacks.click(b,option))return true;
  if(option==1&&f.group==15443&&f.model(b.root.id)>=0){
   supplies(b,HouseWorkshopData.tools(b.root.id-15443),0,f.key);return true;
  }
  return false;
 }
 private void supplies(PlayerHouse.Binding b,int[] items,int page,String recipe){
  if(!house.furnitureReady(b)||!house.furnishing(b).key.equals(recipe)||page<0||page*3>=items.length)return;
  List<DialogueOption> options=new ArrayList<>();
  for(int i=page*3;i<Math.min(items.length,page*3+3);i++){
   int id=items[i];options.add(new DialogueOption(ItemAssistant.getItemName(id),q->{
    q.getPA().removeAllWindows();
    if(house.furnitureReady(b)&&house.furnishing(b).key.equals(recipe))
     ProductionBatch.exchange(p,new int[0][],new int[][]{{id,1}});
   }));
  }
  if(page*3+3<items.length)options.add(new DialogueOption("More",q->supplies(b,items,page+1,recipe)));
  else options.add(new DialogueOption("Leave",q->q.getPA().removeAllWindows()));
  new DialogueBuilder(p).option("Tool store",options.toArray(new DialogueOption[0])).send();
 }
}
