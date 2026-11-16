package io.xeros.content.shooting_star;
import io.xeros.Server;
import io.xeros.content.skills.*;
import io.xeros.content.skills.mining.*;
import io.xeros.content.skills.mining.motherlode.MotherlodeMine;
import io.xeros.content.achievement.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

public final class StarMining {
 private StarMining(){}
 private static final ShootingStarsWorld world=ShootingStarsWorld.INSTANCE;
 private static boolean ready(Player p){return !p.isDead&&!p.isDisconnected()&&p.getSession()!=null&&!p.isBanking&&!p.isShopping&&!p.getLock().cannotInteract(p)&&!Server.getMultiplayerSessionListener().inAnySession(p);}
 private static boolean room(Player p){return p.getItems().getInventoryCount(25527)<Integer.MAX_VALUE&&(p.getItems().freeSlots()>0||p.getItems().getInventoryCount(25527)>0);}
 public static boolean click(Player p,int id,int x,int y,int h){if(StarLayer.object(id)==null)return false;if(h==p.heightLevel)start(p,id,x,y,Misc::trueRand);return true;}
 public static boolean start(Player p,int id,int x,int y,IntUnaryOperator random){
  if(!ready(p)||!world.matches(id,x,y,p.heightLevel)||!world.near(p))return false;
  world.discover(p);StarLayer layer=world.layer();
  if(p.playerLevel[14]<layer.level()){p.sendMessage("You need Mining level "+layer.level()+" to mine this size "+layer.size+" star.");return false;}
  Pickaxe pick=Pickaxe.getBestPickaxe(p);if(pick==null){p.sendMessage("You need a suitable pickaxe.");return false;}
  if(!room(p)){p.sendMessage("You need space for stardust.");return false;}
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
  final long token=p.productionActionToken,generation=world.generation();final int ox=p.absX,oy=p.absY,h=p.heightLevel;
  p.playerSkilling[14]=true;p.startAnimation(pick.getAnimation());p.facePosition(x,y);
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
   private int until=VolcanicAsh.delay(pick,random);
   public void execute(){
    Pickaxe current=Pickaxe.getBestPickaxe(p);
    if(token!=p.productionActionToken||!p.playerSkilling[14]||!ready(p)||p.absX!=ox||p.absY!=oy||p.heightLevel!=h||world.generation()!=generation||!world.matches(id,x,y,h)||!world.near(p)||p.playerLevel[14]<layer.level()||current==null||!room(p)){stop();return;}
    if(--until>0)return;until=VolcanicAsh.delay(current,random);p.startAnimation(current.getAnimation());
    int boost=0;for(int ring:new int[]{25539,25541,25543,25545})if(p.getItems().isWearingItem(ring,Player.playerRing)){boost=4;break;}
    int gem=MotherlodeMine.preGem(random.applyAsInt(PreciousRockMining.gemTableDenominator(p)));
    if(gem!=-2){if(gem>0&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{gem,1}}))stop();return;}
    if(random.applyAsInt(256)>=layer.threshold(p.playerLevel[14],boost))return;
    if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{25527,1}})){stop();return;}
    int dust=1;if(random.applyAsInt(100)<layer.extraDustPercent()&&room(p)&&ProductionBatch.exchange(p,new int[0][],new int[][]{{25527,1}}))dust++;
    p.getStarPlayerState().mined(dust);p.getMiningAccessories().consumeCrystal(current,random);
    p.getPA().addSkillXPMultiplied(layer.xp*SkillingOutfit.PROSPECTOR.multiplier(p),14,true);Achievements.increase(p,AchievementType.MINE,1);
    int base=p.getPA().getLevelForXP(p.playerXP[14]);
    if(random.applyAsInt(layer.petDenominator(base))==0&&p.getItems().getItemCount(13321,false)==0&&p.petSummonId!=7439){p.getCollectionLog().handleDrop(p,5,13321,1);p.getItems().addItemUnderAnyCircumstance(13321,1);p.sendMessage("A rock golem has formed from the crashed star!");}
    if(world.site().key.equals("TRAHAEARN_MINE")&&random.applyAsInt(127)==0)p.getItems().addItemUnderAnyCircumstance(23962,1);
    world.harvested();if(world.generation()!=generation){p.sendMessage(world.site()==null?"The last of the star crumbles away.":"You have mined through this layer. The star is now size "+world.layer().size+".");stop();}
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[14]=false;p.startAnimation(65535);}}
  });return true;
 }
 public static boolean prospect(Player p,int id,int x,int y){if(StarLayer.object(id)==null)return false;if(ready(p)&&world.matches(id,x,y,p.heightLevel)&&world.near(p))p.sendMessage("This is a size "+world.layer().size+" star. You need Mining level "+world.layer().level()+". This layer contains "+world.health()+" more pieces of stardust.");return true;}
 public static boolean board(Player p,int id,int x,int y){if(id!=41230)return false;if(!ready(p)||p.getInstance()!=null||Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))>2||!p.getRegionProvider().get(x,y).isWorldObject(id,x,y,p.heightLevel))return true;
  java.util.List<String> lines=new java.util.ArrayList<>();
  lines.add("Stars discovered by you: "+p.getStarPlayerState().discoveries());
  lines.add("Stardust mined by you: "+p.getStarPlayerState().dust());lines.add("");
  for(ShootingStarsWorld.Discovery d:world.history()){lines.add(d.player+" - size "+d.size);lines.add(d.place);lines.add("");}
  p.getPA().openQuestInterface("Shooting Star Noticeboard",lines);return true;
 }
}
