package io.xeros.solo;

import io.xeros.*;
import io.xeros.content.skills.agility.*;
import io.xeros.content.skills.agility.impl.*;
import io.xeros.model.cycleevent.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionOne;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** Runs in the private QA world. Exercises actual course handlers and scheduled movement. */
public final class AgilityPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void at(Player p,int x,int y,int h){p.getPA().movePlayer(x,y,h);p.getNextPlayerMovement();}
 static void settle(Player p,int n){for(int i=0;i<n;i++){p.getNextPlayerMovement();SkillsAuditCheck.tick(1);}}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static boolean invoke(Class<?> type,Object handler,Player p,int id)throws Exception{return (Boolean)type.getMethod("execute",Player.class,int.class).invoke(handler,p,id);}
 static final String[] NAMES={"Draynor","Alkharid","Varrock","Canafis","Falador","Seers","Pollnivneach","Rellekka","Ardougne"};
 static final String[] ARRAYS={"DRAYNOR","ALKHARID","VARROCK","CANAFIS","FALADOR","SEERS","POLLNIVNEACH","RELLEKKA","ARDOUGNE"};
 static final int[] XP={120,180,238,240,440,570,890,780,793};
 static final int[][] STARTS={{3103,3279},{3273,3192},{3219,3414},{3506,3492},{3036,3341},{2729,3487},{3351,2964},{2625,3675},{2673,3298}};
 static void rooftops()throws Exception{
  for(int k=0;k<NAMES.length;k++){
   Class<?> type=Class.forName("io.xeros.content.skills.agility.impl.rooftop.Rooftop"+NAMES[k]);Object h=type.getConstructor().newInstance();int[] ids=(int[])type.getField(ARRAYS[k]+"_OBJECTS").get(null);
   Player p=actor();at(p,STARTS[k][0],STARTS[k][1],0);int xp=p.playerXP[16];p.playerLevel[16]=(k+1)*10-1;
   ok(invoke(type,h,p,ids[0]),"Known obstacle not consumed below level "+NAMES[k]);settle(p,8);
   ok(p.heightLevel==0&&!p.getAgilityHandler().agilityProgress[0]&&p.playerXP[16]==xp,"Below-level course entry "+NAMES[k]);
   // Dispatcher must not let a rejected rooftop click fall through into another handler.
   ObjectOptionOne.handleOption(p,ids[0],STARTS[k][0],STARTS[k][1]);settle(p,8);ok(p.heightLevel==0&&p.playerXP[16]==xp,"Dispatcher level bypass "+NAMES[k]);
   p.playerLevel[16]=99;
   ok(!invoke(type,h,p,-123),"Unrelated object consumed "+NAMES[k]);
   // Final obstacle alone gives no lap XP. Start afresh after the exit animation.
   invoke(type,h,p,ids[ids.length-1]);settle(p,150);ok(p.playerXP[16]==xp,"Skipped lap rewarded "+NAMES[k]);
   clear(p);at(p,STARTS[k][0],STARTS[k][1],0);
   for(int i=0;i<ids.length;i++){
    p.lastObstacleFail=0;
    if(NAMES[k].equals("Varrock")&&i==1)at(p,3214,3414,3);
    invoke(type,h,p,ids[i]);settle(p,100);
    if(i<ids.length-1)ok(p.getAgilityHandler().agilityProgress[i],"Missing step "+NAMES[k]+"/"+i+" at "+p.absX+","+p.absY+","+p.heightLevel);
   }
   ok(p.playerXP[16]-xp==XP[k],"Lap XP "+NAMES[k]+" expected="+XP[k]+" got="+(p.playerXP[16]-xp));
   ok(p.heightLevel==0,"Course failed to exit to ground "+NAMES[k]+" at "+p.absX+","+p.absY+","+p.heightLevel);
   p.lastObstacleFail=0;invoke(type,h,p,ids[ids.length-1]);settle(p,150);ok(p.playerXP[16]-xp==XP[k],"Repeated final lap rewarded "+NAMES[k]);
   clear(p);System.out.println("AGILITY_ROOFTOP_"+NAMES[k]+"_LEVEL_DISPATCH_STEPS_MOVEMENT_XP_EXIT_NO_SKIPPED_OR_DUPLICATE_LAP_PASS");
  }
 }
 static void groundCourses(){
  Player p=actor();int xp=p.playerXP[16];GnomeAgility g=new GnomeAgility();int[][] seq={{23145,2474,3436,0},{23134,2473,3427,0},{23559,2473,3423,1},{23557,2477,3420,2},{23560,2486,3420,2},{23135,2485,3425,0},{23138,2484,3430,0}};
  for(int[]s:seq){at(p,s[1],s[2],s[3]);g.gnomeCourse(p,s[0]);settle(p,30);}
  ok(p.playerXP[16]-xp==86&&p.playerXpTenths[16]==5,"Gnome XP expected86.5 got="+(p.playerXP[16]-xp)+" tenths="+p.playerXpTenths[16]);
  ok(p.absY==3437&&p.heightLevel==0,"Gnome exit");xp=p.playerXP[16];g.gnomeCourse(p,23138);settle(p,20);ok(p.playerXP[16]==xp,"Gnome duplicate");clear(p);
  p=actor();xp=p.playerXP[16];BarbarianAgility b=new BarbarianAgility();seq=new int[][]{{23131,2551,3554,0},{23144,2551,3546,0},{20211,2541,3546,0},{23547,2536,3547,1},{42487,2532,3547,1},{1948,2541,3553,0}};
  for(int[]s:seq){at(p,s[1],s[2],s[3]);b.barbarianCourse(p,s[0]);settle(p,30);}
  ok(p.playerXP[16]-xp==154&&p.heightLevel==0,"Barbarian complete lap");clear(p);
  p=actor();xp=p.playerXP[16];WildernessAgility wild=new WildernessAgility();seq=new int[][]{{23137,3004,3937,0},{23132,3005,3953,0},{23556,3001,3960,0},{23542,3002,3945,0},{23640,2994,3937,0}};
  for(int[]s:seq){at(p,s[1],s[2],s[3]);wild.wildernessCourse(p,s[0]);settle(p,35);}
  ok(p.playerXP[16]-xp==571&&p.absY==3933,"Wilderness contiguous progress/full lap");clear(p);
  p=actor();p.getAgilityHandler().agilityProgress[5]=true;xp=p.playerXP[16];p.getAgilityHandler().lapFinished(p,5,154,8000);ok(p.playerXP[16]==xp,"Ground course skipped prerequisites rewarded");clear(p);
  System.out.println("AGILITY_GNOME_FRACTIONAL_XP_BARBARIAN_WILDERNESS_REAL_HANDLER_MOVEMENT_AND_PREREQUISITES_PASS");
 }
 static void lifecycle()throws Exception{
  for(int[]dest:new int[][]{{2474,3438},{3104,3279},{3272,3196},{3222,3414},{2551,3555},{3505,3487},{3036,3340},{2729,3487},{3351,2960},{2625,3676},{2672,3296},{3003,3934}}){
   Player walker=actor();at(walker,dest[0],dest[1],0);boolean moved=false;
   for(int[]d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){walker.resetWalkingQueue();PathFinder.getPathFinder().findRoute(walker,dest[0]+d[0],dest[1]+d[1],false,0,0);walker.getNextPlayerMovement();if(walker.absX!=dest[0]||walker.absY!=dest[1]){moved=true;break;}}
   ok(moved,"Travel landing has no walkable exit at "+dest[0]+","+dest[1]);clear(walker);
  }
  System.out.println("AGILITY_TWELVE_TRAVEL_LANDINGS_NATIVE_MAP_WALKABLE_EXIT_PASS");
  Player menu=actor();for(int page=0;page<3;page++)AgilityTravel.open(menu,page);menu.playerLevel[16]=1;ok(!AgilityTravel.travel(menu,90,2673,3295,false)&&menu.teleTimer==0,"Travel menu ignored level");clear(menu);
  for(int variant=0;variant<3;variant++){
   Player p=actor();if(variant==0)AgilityHandler.delayEmote(p,"JUMP",3100,3200,0,2);else if(variant==1)AgilityHandler.delayEmote(p,3067,3100,3200,0,2);else AgilityHandler.delayFade(p,new String("CLIMB_UP"),3100,3200,0,"Inicio","Fim",2);
   p.setDisconnected(true);settle(p,8);ok(!CycleEventHandler.getSingleton().isAlive(p),"Disconnected delayed event remains "+variant);clear(p);
  }
  Player p=actor();p.playerLevel[16]=70;at(p,3447,3575,1);new Shortcuts().agilityShortcuts(p,16537);settle(p,4);ok(p.heightLevel==1,"Level70 bypassed71 chain");p.playerLevel[16]=71;new Shortcuts().agilityShortcuts(p,16537);settle(p,4);ok(p.heightLevel==2,"Level71 chain denied");new Shortcuts().agilityShortcuts(p,16538);settle(p,4);ok(p.heightLevel==1,"Chain return");clear(p);
  p=actor();p.getAgilityHandler().beginCourse("RooftopDraynor");p.getAgilityHandler().agilityProgress[0]=true;Field roll=AgilityHandler.class.getDeclaredField("obstacleRoll");roll.setAccessible(true);Object previous=roll.get(null);try{roll.set(null,(IntUnaryOperator)n->1);ok(AgilityHandler.failObstacle(p,3094,3274,0),"Forced fall");settle(p,4);ok(p.heightLevel==0&&p.absX==3094&&!p.getAgilityHandler().completedThrough(0),"Fall failed to land/reset");}finally{roll.set(null,previous);}clear(p);
  System.out.println("AGILITY_THREE_TRAVEL_PAGES_LEVEL_GATE_DISCONNECT_CLEANUP_ALL_THREE_DELAYS_CHAIN_70_71_RETURN_AND_FALL_PROGRESS_RESET_PASS");
 }
 public static void main(String[]args)throws Exception{
  Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;
  Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());
  Field roll=AgilityHandler.class.getDeclaredField("obstacleRoll");roll.setAccessible(true);Object previous=roll.get(null);try{roll.set(null,(IntUnaryOperator)n->0);rooftops();groundCourses();lifecycle();}finally{roll.set(null,previous);}
  System.out.println("AGILITY_PLAYABLE_V52_CHECKS_PASS");System.exit(0);
 }
}
