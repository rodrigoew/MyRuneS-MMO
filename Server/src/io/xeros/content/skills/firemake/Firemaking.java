package io.xeros.content.skills.firemake;

import io.xeros.Server;
import io.xeros.achievements.AchievementHandler;
import io.xeros.achievements.AchievementList;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.SkillingOutfit;
import io.xeros.content.skills.woodcutting.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;

public class Firemaking {
 public static int[] pyromancerOutfit={20704,20706,20708,20710};
 public static boolean canLight(Player p,int x,int y,int height){
  int clipping=p.getRegionProvider().getClipping(x,y,height);
  return (clipping==0||p.getRegionProvider().isOccupiedByNpc(x,y,height))
    &&!Server.getGlobalObjects().anyExists(x,y,height)&&!p.getPosition().inBank()
    &&!Boundary.isIn(p,Boundary.EDGE_TRADING_AREA)&&!Boundary.isIn(p,Boundary.DUEL_ARENA)
    &&!Boundary.isIn(p,Boundary.HALLOWEEN_ORDER_MINIGAME);
 }
 public static void lightFire(Player p,int logUsed,String usage){
  if(!p.getUtilityMagic().canInteract())return;
  LogData log=LogData.getLogData(p,logUsed);if(log==null)return;
  // Compatibility for the old custom-tree handler. Ordinary trees use WoodcuttingTools directly.
  if("infernal_axe".equals(usage)){
   if(Hatchet.getBest(p)!=Hatchet.INFERNAL_AXE)return;
   for(HistoricalTreeData.Kind kind:HistoricalTreeData.Kind.values())if(kind.item==logUsed){p.getWoodcuttingTools().combust(Hatchet.INFERNAL_AXE,kind,b->0);return;}
   return;
  }
  if(!"tinderbox".equals(usage)||!p.getItems().playerHasItem(590)||!p.getItems().playerHasItem(logUsed)
    ||p.playerLevel[11]<log.getlevelRequirement()||p.playerIsFiremaking||System.currentTimeMillis()-p.lastFire<1800
    ||!canLight(p,p.absX,p.absY,p.heightLevel))return;
  int delay=System.currentTimeMillis()-p.lastFire>3000?4:1;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
  long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;
  p.playerIsFiremaking=true;p.playerSkilling[11]=true;p.lastFire=System.currentTimeMillis();p.startAnimation(733);
  Server.getEventHandler().submit(new Event<Player>("skilling",p,delay){
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[11]||!p.getUtilityMagic().canInteract()
      ||p.absX!=x||p.absY!=y||p.heightLevel!=h||p.playerLevel[11]<log.getlevelRequirement()
      ||!p.getItems().playerHasItem(590)||!canLight(p,x,y,h)
      ||!ProductionBatch.exchange(p,new int[][]{{logUsed,1}},new int[0][])){stop();return;}
    GlobalObject fire=new GlobalObject(5249,x,y,h,0,10,50,-1);fire.setInstance(p.getInstance());Server.getGlobalObjects().add(fire);
    p.getPA().addSkillXPMultiplied(log.getExperience()*SkillingOutfit.PYROMANCER.multiplier(p),11,true);
    Achievements.increase(p,AchievementType.FIRE,1);
    if(logUsed==1521)p.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.BURN_OAK);
    if(logUsed==1511)AchievementHandler.activate(p,AchievementList.BURN_200_LOGS,1);
    if(logUsed==1515)DailyTasks.increase(p,DailyTasks.PossibleTasks.LIGHT_YEW_LOGS);
    if(logUsed==1513)DailyTasks.increase(p,DailyTasks.PossibleTasks.LIGHT_MAGIC_LOGS);
    p.getPA().sendSound(2596);p.sendMessage("Você acende uma fogueira.");rewards(p);
    stop();
    for(int[] step:new int[][]{{-1,0},{1,0},{0,-1},{0,1}})
     if(p.getRegionProvider().getClipping(x+step[0],y+step[1],h,step[0],step[1])){p.getPA().walkTo(step[0],step[1]);break;}
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerIsFiremaking=false;p.playerSkilling[11]=false;p.startAnimation(65535);}else p.playerIsFiremaking=false;}
  });
 }
 private static void rewards(Player p){
  if(Misc.hasOneOutOf(25)){p.getItems().addItemUnderAnyCircumstance(7794,1);p.sendMessage("Você encontra um tomo de Firemaking.");}
  if(Misc.random(2000)==1&&p.getItems().getItemCount(20693,false)==0&&p.petSummonId!=20693){
   p.getItems().addItemUnderAnyCircumstance(20693,1);
   PlayerHandler.executeGlobalMessage("[@red@PET@bla@] "+p.getDisplayNameFormatted()+" recebeu Phoenix!");
   p.getCollectionLog().handleDrop(p,5,20693,1);
  }
 }
}
