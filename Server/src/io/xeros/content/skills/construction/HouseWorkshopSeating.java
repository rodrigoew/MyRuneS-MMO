package io.xeros.content.skills.construction;
import io.xeros.Server;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;

/** Transient seating shared by workbenches and clockmaker's benches. */
public final class HouseWorkshopSeating {
 private final PlayerHouse house;private final Player p;
 private PlayerHouse.Binding seat;private WorldObject stool;private String recipe;
 private int standing,idle;private boolean arrived;
 public HouseWorkshopSeating(PlayerHouse house,Player p){this.house=house;this.p=p;}
 public boolean at(PlayerHouse.Binding b){
  return seat!=null&&seat.room==b.room&&seat.root.x==b.root.x&&seat.root.y==b.root.y&&stool!=null&&house.furnishing(b)!=null&&recipe.equals(house.furnishing(b).key);
 }
 private boolean live(){
  return seat!=null&&house.inside()&&house.roomRegistered(seat.room)&&!p.isDisconnected()&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&p.teleTimer==0&&house.furnishing(seat)!=null&&recipe.equals(house.furnishing(seat).key)
   &&p.heightLevel==stool.height&&p.absX==stool.x&&p.absY==stool.y&&p.isWalkingQueueEmpty()&&house.furnitureReady(seat);
 }
 public boolean ready(PlayerHouse.Binding b){return at(b)&&live();}
 public boolean seated(){return live()&&arrived;}
 public int craftAnimation(){return idle==4108?4110:4109;}
 public void clear(){
  if(seat!=null&&p.playerStandIndex==idle){p.playerStandIndex=standing;p.getPA().requestUpdates();}
  seat=null;stool=null;recipe=null;arrived=false;Server.getEventHandler().stop(p,"house-seat");
 }
 public boolean sit(PlayerHouse.Binding b){
  if(at(b)&&(live()||p.getTeleportToX()==stool.x&&p.getTeleportToY()==stool.y))return true;
  int group=HouseFurnishing.group(b.root.id);if(group!=15439&&group!=15441)return false;
  clear();WorldObject next=house.furniturePart(b.room,group,group+1);if(next==null||!house.furnitureReady(b))return false;
  seat=b;stool=next;recipe=house.furnishing(b).key;standing=p.playerStandIndex;idle=next.id==6807?4108:4107;
  p.moveTo(next.getPosition());p.startAnimation(4103);p.facePosition(b.root.x,b.root.y);p.playerStandIndex=idle;p.getPA().requestUpdates();
  Server.getEventHandler().submit(new Event<Player>("house-seat",p,1){
   private boolean first=true;
   public void execute(){
    if(seat!=b){stop();return;}
    if(first&&p.getTeleportToX()==next.x&&p.getTeleportToY()==next.y){first=false;return;}
    if(!live()){clear();stop();return;}arrived=true;
   }
  });return true;
 }
}
