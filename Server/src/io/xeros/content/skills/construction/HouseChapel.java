package io.xeros.content.skills.construction;

import io.xeros.Server;
import io.xeros.content.instances.DynamicMapScene;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.prayer.Bone;
import io.xeros.content.skills.prayer.PrayerPreservation;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.model.SoundType;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Per-room altar bonuses and independently fuelled native incense burners. */
public final class HouseChapel {
 private static final class Lamp {final String recipe;int remaining;Lamp(String recipe,int remaining){this.recipe=recipe;this.remaining=remaining;}}
 private static IntUnaryOperator random=bound->ThreadLocalRandom.current().nextInt(bound);
 private final PlayerHouse house;private final Player p;
 private final Map<PlayerHouse.Room,Map<Integer,Lamp>> lamps=new IdentityHashMap<>();
 public HouseChapel(PlayerHouse house,Player p){this.house=house;this.p=p;}
 public static int duration(int level,int roll){if(level<1||roll<0||roll>=level)throw new IllegalArgumentException("Lamp duration");return 200+level+roll;}
 private int lampIndex(PlayerHouse.Binding b){
  int[] tile=DynamicMapScene.rotateTile((b.root.x-PlayerHouse.BASE)%8,(b.root.y-PlayerHouse.BASE)%8,(4-b.room.rotation)&3);
  return tile[1]==5&&(tile[0]==1||tile[0]==6)?tile[0]:-1;
 }
 private boolean incense(String key){return key.equals("INCENSE_BURNER")||key.equals("MAHOGANY_BURNER")||key.equals("MARBLE_BURNER");}
 private Lamp lamp(PlayerHouse.Binding b){Map<Integer,Lamp> room=lamps.get(b.room);return room==null?null:room.get(lampIndex(b));}
 public void clear(){lamps.clear();Server.getEventHandler().stop(p,"house-lamps");}
 void removeRoom(PlayerHouse.Room room){lamps.remove(room);}
 void furnitureRemoved(PlayerHouse.Room room,int slot){if(slot==15271)lamps.remove(room);}
 int model(PlayerHouse.Binding b,HouseFurnishing f){
  if(b.room.kind!=PlayerHouse.Kind.CHAPEL)return f.model(b.root.id);
  int style=HouseChapelData.style(b.room.furniture.get(15269));
  if(f.group==15270)return HouseChapelData.altarModel(f.key,style);
  if(f.group==13733)return HouseChapelData.windowModel(f.key,b.root.id,style);
  if(f.group==15275)return HouseChapelData.statueModel(f.key,style);
  if(f.group==15271){Lamp lamp=lamp(b);return f.models[0]+(lamp!=null&&lamp.remaining>0&&lamp.recipe.equals(f.key)?1:0);}
  return f.model(b.root.id);
 }
 public boolean click(WorldObject object,int option){
  PlayerHouse.Binding b=house.furnitureFixture(object);if(b==null||b.room.kind!=PlayerHouse.Kind.CHAPEL)return false;
  HouseFurnishing f=house.furnishing(b);
  if(option==1&&f.group==15276){
   if(!live(b,f.key))return true;
   p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
   boolean organ=f.key.equals("ORGAN");p.startAnimation(organ?3675:3674);
   if(!organ)p.getPA().sendPlayerObjectAnimation(object.x,object.y,f.key.equals("BELLS")?3577:3576,object.type,object.face);
   p.getPA().sendSound(organ?72:f.key.equals("BELLS")?932:2225,organ?SoundType.MUSIC:SoundType.SOUND);
   return true;
  }
  if(option==5&&f.key.equals("GNOME_CHILD_ICON")){
   if(!live(b,f.key))return true;
   p.interruptActions();p.getPA().stopSkilling();
   new DialogueBuilder(p).statement("With smile of Child-like wonder,","and penetrating gaze,","the enigmatic Gnome Child","shall watch you all your days.")
    .statement("The remaining inscription describes the Gnome Child watching","over you through good times and hardship, without intervening.").send();
   return true;
  }
  if(option==1&&f.group==15271){light(b);return true;}
  if(option==1&&f.group==15270){
   int base=p.getLevelForXP(p.playerXP[5]);if(p.playerLevel[5]<base){p.interruptActions();p.playerLevel[5]=base;p.getPA().refreshSkill(5);p.startAnimation(645);p.sendMessage("You recharge your Prayer points.");}else p.sendMessage("Your Prayer is already fully recharged.");return true;
  }
  return false;
 }
 public boolean use(int item,WorldObject object){
  if(!house.inside())return false;PlayerHouse.Binding b=house.furnitureFixture(object);if(b==null||b.room.kind!=PlayerHouse.Kind.CHAPEL)return false;
  HouseFurnishing f=house.furnishing(b);
  if(f.group==15271){if(item==590||item==251)light(b);else p.sendMessage("Use a tinderbox and clean marrentill to light an incense burner.");return true;}
  if(f.group!=15270)return false;
  Bone bone=null;for(Bone value:Bone.values())if(value.getItemId()==item)bone=value;
  if(bone==null){p.sendMessage("Only bones can be offered at this altar.");return true;}
  offer(b,bone,p.getItems().getInventoryCount(item));return true;
 }
 private boolean live(PlayerHouse.Binding b,String recipe){return house.furnitureReady(b)&&recipe.equals(house.furnishing(b).key)&&p.getSession()!=null&&!p.isDisconnected()&&!p.isDead&&p.getHealth().getCurrentHealth()>0&&p.teleTimer==0;}
 private boolean light(PlayerHouse.Binding b){
  HouseFurnishing f=house.furnishing(b);int index=lampIndex(b);if(index<0||!live(b,f.key)||f.group!=15271)return false;
  if(p.playerLevel[11]<30){p.sendMessage("You need Firemaking level 30 to light this.");return false;}
  if(!p.getItems().playerHasItem(590)){p.sendMessage("You need a tinderbox.");return false;}
  boolean incense=incense(f.key);if(incense&&!p.getItems().playerHasItem(251)){p.sendMessage("You need a clean marrentill for this burner.");return false;}
  p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;String recipe=f.key;p.playerSkilling[11]=true;p.startAnimation(3687);
  // The historical duration formula is known; the lighting animation delay is approximate.
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[11]||!live(b,recipe)||p.playerLevel[11]<30||!p.getItems().playerHasItem(590)||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()){stop();return;}
    if(incense&&!ProductionBatch.exchange(p,new int[][]{{251,1}},new int[0][])){stop();return;}
    int level=p.playerLevel[11],ticks=duration(level,random.applyAsInt(level));boolean start=!Server.getEventHandler().isRunning(p,"house-lamps");
    lamps.computeIfAbsent(b.room,key->new HashMap<>()).put(index,new Lamp(recipe,incense?ticks:Integer.MAX_VALUE));house.renderChapel(b.room);
    if(start)timer();p.sendMessage(incense?"You burn the marrentill in the incense burner.":"You light the lamp.");stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[11]=false;p.startAnimation(65535);}}
  });return true;
 }
 private void timer(){
  Server.getEventHandler().submit(new Event<Player>("house-lamps",p,1){public void execute(){
   if(!house.inside()||p.getSession()==null||p.isDisconnected()){clear();stop();return;}
   Iterator<Map.Entry<PlayerHouse.Room,Map<Integer,Lamp>>> rooms=lamps.entrySet().iterator();
   while(rooms.hasNext()){
    var entry=rooms.next();PlayerHouse.Room room=entry.getKey();if(!house.roomRegistered(room)){rooms.remove();continue;}
    boolean changed=false;Iterator<Lamp> values=entry.getValue().values().iterator();while(values.hasNext()){
     Lamp lamp=values.next();if(!lamp.recipe.equals(room.furniture.get(15271))||lamp.remaining!=Integer.MAX_VALUE&&--lamp.remaining<=0){values.remove();changed=true;}
    }
    if(entry.getValue().isEmpty())rooms.remove();if(changed)house.renderChapel(room);
   }
   if(lamps.isEmpty())stop();
  }});
 }
 private int lit(PlayerHouse.Room room){
  Map<Integer,Lamp> states=lamps.get(room);if(states==null)return 0;String key=room.furniture.get(15271);int lit=0;
  for(Lamp lamp:states.values())if(lamp.remaining>0&&lamp.recipe.equals(key)&&incense(key))lit++;
  return Math.min(2,lit);
 }
 private boolean offer(PlayerHouse.Binding b,Bone bone,int amount){
  HouseFurnishing f=house.furnishing(b);if(amount<1||!live(b,f.key)||f.group!=15270||!p.getItems().playerHasItem(bone.getItemId()))return false;
  if(bone==Bone.SUPERIOR&&p.getLevelForXP(p.playerXP[5])<70){p.sendMessage("You need Prayer level 70 to offer superior dragon bones.");return false;}
  p.interruptActions();p.getPA().stopSkilling();long token=p.productionActionToken;String recipe=f.key;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[5]=true;
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){int remaining=Math.min(28,amount);public void execute(){
   if(token!=p.productionActionToken||!p.playerSkilling[5]||!live(b,recipe)||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()||bone==Bone.SUPERIOR&&p.getLevelForXP(p.playerXP[5])<70){stop();return;}
   if(!p.getItems().playerHasItem(bone.getItemId())){stop();return;}
   boolean saved=PrayerPreservation.preserve(p);
   if(!saved&&!ProductionBatch.exchange(p,new int[][]{{bone.getItemId(),1}},new int[0][])){stop();return;}
   int burners=lit(b.room);double xp=bone.getExperience()*(HouseChapelData.altarBonus(recipe)+50*burners)/100d;
   p.getPA().addSkillXPMultiplied(xp,5,true);p.startAnimation(3705);p.getPA().stillGfx(624,b.root.x,b.root.y,p.heightLevel,1);p.sendMessage(burners==2?"The gods are very pleased with your offering.":"The gods are pleased with your offering.");
   if(saved)p.sendMessage("Your zealot's robes preserve the bones.");
   maximumTicks=3;if(!saved&&--remaining<=0)stop();
  }public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[5]=false;p.startAnimation(65535);}}});return true;
 }
}
