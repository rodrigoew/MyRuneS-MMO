package io.xeros.content.combat.magic;
import io.xeros.Server;
import io.xeros.content.lootbag.LootingBag;
import io.xeros.model.collisionmap.PathChecker;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.lock.PlayerLock;
import io.xeros.model.items.GroundItem;

/** A cast owns the exact visible ground stack until its projectile arrives. */
public final class TelekineticGrab {
 public static final int WIDGET=1168;
 private static final java.util.Set<Integer> RESTRICTED=java.util.Set.of(4838,6561,6888,7478,8936,8938,11136,11138,11140,11704,13103,13104,13105,13106,13107,13108,13109,13110,13111,13112,13113,13114,13115,13117,13118,13119,13120,13121,13122,13123,13124,13125,13126,13127,13128,13129,13130,13131,13132,13133,13134,13135,13136,13137,13138,13139,13140,13141,13142,13143,13144,22941,22943,22945,22947);
 private static final int[][] COST={{556,1},{563,1}};
 private final Player p;private boolean busy;
 public TelekineticGrab(Player p){this.p=p;}
 public boolean busy(){return busy;}
 private boolean available(){return !p.isDead&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null&&!p.getLock().cannotInteract(p)&&!p.getMovementState().isLocked()&&!p.isBanking&&!p.isShopping&&!p.isFping()&&!p.isNpc&&!p.getInterfaceEvent().isActive()&&!p.teleportingToDistrict&&p.teleTimer==0;}
 private boolean takeable(GroundItem item){
  if(item==null||RESTRICTED.contains(item.getId()))return false;
  if(LootingBag.isLootingBag(item.getId())&&(p.getItems().getItemCount(LootingBag.LOOTING_BAG,true)>0||p.getItems().getItemCount(LootingBag.LOOTING_BAG_OPEN,true)>0))return false;
  return item.getId()!=12791||p.getItems().getItemCount(12791,true)==0;
 }
 public boolean cast(int widget,int itemId,int x,int y){
  if(busy||widget!=WIDGET||p.playerMagicBook!=0||p.playerLevel[6]<33||!available())return false;
  if(p.getBankPin().requiresUnlock()){p.getBankPin().open(2);return false;}
  GroundItem target=Server.itemHandler.getGroundItem(p,itemId,x,y,p.heightLevel);
  if(!takeable(target)||Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))>10)return false;
  if(!PathChecker.raycast(p,p.getPosition(),target.getPosition(),true)){p.sendMessage("You cannot see that item clearly enough.");return false;}
  if(!RunePayment.canExchange(p,COST,new int[][]{{target.getId(),target.getAmount()}})){p.sendMessage("You need the required runes and room for that item.");return false;}
  if(!RunePayment.consume(p,COST))return false;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.attacking.reset();p.stopMovement();
  final int originX=p.absX,originY=p.absY,height=p.heightLevel;final var instance=p.getInstance();
  // Legacy comparison timing; precise 2022 projectile cadence remains separately uncertified.
  int delay=(int)Math.floor(2+Math.hypot(x-originX,y-originY)*.5);
  PlayerLock previous=p.getLock();PlayerLock flight=new PlayerLock(){
   public boolean cannotLogout(Player player){return previous.cannotLogout(player);}
   public boolean cannotInteract(Player player){return true;}
   public boolean cannotClickItem(Player player,int item){return true;}
   public boolean cannotTeleport(Player player){return true;}
  };
  busy=true;p.lock(flight);p.facePosition(x,y);p.startAnimation(723);p.gfx100(142);
  p.getPA().createPlayersProjectile(originX,originY,y-originY,x-originX,50,Math.max(40,delay*30),143,40,0,0,0);
  p.getPA().addSkillXPMultiplied(43,6,true);
  Server.getEventHandler().submit(new Event<Player>("telekinetic-grab",p,delay){
   public void execute(){
    if(!p.isDisconnected()&&p.getSession()!=null&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&p.heightLevel==height&&p.getInstance()==instance&&p.absX==originX&&p.absY==originY){
     GroundItem current=Server.itemHandler.getGroundItem(p,itemId,x,y,height);
     if(current==target&&takeable(current)){
      p.getPA().createPlayersStillGfx(144,x,y,height,0);
      Server.itemHandler.removeGroundItem(p,current,true);
     }else p.sendMessage("The item is no longer there.");
    }
    stop();
   }
   public void stop(){super.stop();busy=false;p.restoreLock(flight,previous);}
  });return true;
 }
}
