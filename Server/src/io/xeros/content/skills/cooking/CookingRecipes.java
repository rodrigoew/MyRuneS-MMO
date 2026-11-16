package io.xeros.content.skills.cooking;
import io.xeros.Server;
import io.xeros.content.skills.*;
import io.xeros.content.SkillcapePerks;
import io.xeros.content.dialogue.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
public final class CookingRecipes {
 public static final class Bake {
  public final int raw,cooked,burnt,level,stop,extra;public final double xp;public final boolean rangeOnly;
  Bake(int a,int b,int c,int l,double x,int s,boolean r,int e){raw=a;cooked=b;burnt=c;level=l;xp=x;stop=s;rangeOnly=r;extra=e;}
 }
 public static final class Preparation {
  public final int first,second,product,level,extra;public final double xp;public final boolean tool;
  Preparation(int a,int b,int c,int l,double x,int e,boolean t){first=a;second=b;product=c;level=l;xp=x;extra=e;tool=t;}
 }
 private static final List<Bake> BAKES=new ArrayList<>();private static final List<Preparation> PREPS=new ArrayList<>();
 static {
  BAKES.add(new Bake(2307,2309,2311,1,40,38,true,0));
  BAKES.add(new Bake(1863,1865,1867,58,40,99,true,0));
  BAKES.add(new Bake(2287,2289,2305,35,143,68,true,0));
  BAKES.add(new Bake(1889,1891,1903,40,180,74,true,1887));
  BAKES.add(new Bake(2321,2325,2329,10,78,45,true,0));
  BAKES.add(new Bake(2319,2327,2329,20,110,54,true,0));
  BAKES.add(new Bake(2317,2323,2329,30,130,70,true,0));
  BAKES.add(new Bake(7168,7170,2329,29,128,99,true,0));
  BAKES.add(new Bake(7176,7178,2329,34,138,99,true,0));
  BAKES.add(new Bake(7186,7188,2329,47,164,99,true,0));
  BAKES.add(new Bake(19656,19662,2329,52,180,84,true,0));
  BAKES.add(new Bake(21684,21690,2329,60,200,89,true,0));
  BAKES.add(new Bake(7196,7198,2329,70,210,99,true,0));
  BAKES.add(new Bake(22789,22795,2329,73,220,99,true,0));
  BAKES.add(new Bake(7206,7208,2329,85,240,99,true,0));
  BAKES.add(new Bake(7216,7218,2329,95,260,99,true,0));
  BAKES.add(new Bake(2001,2003,2005,25,117,58,false,0));
  BAKES.add(new Bake(2009,2011,2013,60,280,99,false,0));
  BAKES.add(new Bake(1942,6701,6699,7,15,41,true,0));
  BAKES.add(new Bake(5986,5988,5990,28,104,53,true,0));
  BAKES.add(new Bake(7076,7078,7090,13,50,99,true,0));
  BAKES.add(new Bake(1871,7084,7092,42,60,99,true,0));
  BAKES.add(new Bake(7080,7082,7094,46,60,99,true,0));
  PREPS.add(new Preparation(1953,2313,2315,1,0,0,false));
  PREPS.add(new Preparation(1881,1865,1885,1,40,1923,false));
  PREPS.add(new Preparation(2315,1951,2321,10,0,0,false));
  PREPS.add(new Preparation(2315,2142,2319,20,0,0,false));
  PREPS.add(new Preparation(2315,2140,2319,20,0,0,false));
  PREPS.add(new Preparation(2315,1955,2317,30,0,0,false));
  PREPS.add(new Preparation(2315,6032,7164,29,0,0,false));
  PREPS.add(new Preparation(7164,1929,7166,29,0,1925,false));
  PREPS.add(new Preparation(7166,434,7168,29,0,0,false));
  PREPS.add(new Preparation(2315,1982,7172,34,0,0,false));
  PREPS.add(new Preparation(7172,1957,7174,34,0,0,false));
  PREPS.add(new Preparation(7174,1965,7176,34,0,0,false));
  PREPS.add(new Preparation(2315,333,7182,47,0,0,false));
  PREPS.add(new Preparation(7182,339,7184,47,0,0,false));
  PREPS.add(new Preparation(7184,1942,7186,47,0,0,false));
  PREPS.add(new Preparation(2315,329,7192,70,0,0,false));
  PREPS.add(new Preparation(7192,361,7194,70,0,0,false));
  PREPS.add(new Preparation(7194,1942,7196,70,0,0,false));
  PREPS.add(new Preparation(2315,2136,7202,85,0,0,false));
  PREPS.add(new Preparation(7202,2876,7204,85,0,0,false));
  PREPS.add(new Preparation(7204,3226,7206,85,0,0,false));
  PREPS.add(new Preparation(2315,5504,7212,95,0,0,false));
  PREPS.add(new Preparation(7212,5982,7214,95,0,0,false));
  PREPS.add(new Preparation(7214,1955,7216,95,0,0,false));
  PREPS.add(new Preparation(2315,19653,19656,52,0,0,false));
  PREPS.add(new Preparation(2315,21626,21684,60,0,0,false));
  PREPS.add(new Preparation(2315,22929,22789,73,0,0,false));
  PREPS.add(new Preparation(2283,1982,2285,35,0,0,false));
  PREPS.add(new Preparation(2285,1985,2287,35,0,0,false));
  PREPS.add(new Preparation(2289,2142,2293,45,26,0,false));
  PREPS.add(new Preparation(2289,2140,2293,45,26,0,false));
  PREPS.add(new Preparation(2289,319,2297,55,39,0,false));
  PREPS.add(new Preparation(2289,2116,2301,65,45,0,false));
  PREPS.add(new Preparation(2289,2118,2301,65,45,0,false));
  PREPS.add(new Preparation(2291,2291,2289,1,0,0,false));
  PREPS.add(new Preparation(2295,2295,2293,1,0,0,false));
  PREPS.add(new Preparation(2299,2299,2297,1,0,0,false));
  PREPS.add(new Preparation(2303,2303,2301,1,0,0,false));
  PREPS.add(new Preparation(1891,1973,1897,50,30,0,false));
  PREPS.add(new Preparation(1891,1975,1897,50,30,0,false));
  PREPS.add(new Preparation(1921,1942,1997,25,0,0,false));
  PREPS.add(new Preparation(1921,2142,1999,25,0,0,false));
  PREPS.add(new Preparation(1997,2142,2001,25,0,0,false));
  PREPS.add(new Preparation(1921,2140,1999,25,0,0,false));
  PREPS.add(new Preparation(1997,2140,2001,25,0,0,false));
  PREPS.add(new Preparation(1999,1942,2001,25,0,0,false));
  PREPS.add(new Preparation(2001,2007,2009,60,0,0,false));
  PREPS.add(new Preparation(6701,6697,6703,39,40,0,false));
  PREPS.add(new Preparation(6703,1985,6705,47,40,0,false));
  PREPS.add(new Preparation(6703,7062,7054,41,15,1923,false));
  PREPS.add(new Preparation(6703,7064,7056,51,45,1923,false));
  PREPS.add(new Preparation(6703,7066,7058,64,55,1923,false));
  PREPS.add(new Preparation(6703,7068,7060,68,10,1923,false));
  PREPS.add(new Preparation(946,1982,1869,1,0,0,true));
  PREPS.add(new Preparation(946,2142,7070,1,0,0,true));
  PREPS.add(new Preparation(946,1957,1871,1,0,0,true));
  PREPS.add(new Preparation(946,361,7086,1,0,0,true));
  PREPS.add(new Preparation(946,6004,7080,1,0,0,true));
  PREPS.add(new Preparation(7078,1869,7064,23,0,1923,false));
  PREPS.add(new Preparation(7082,7084,7066,57,0,1923,false));
  PREPS.add(new Preparation(7086,5988,7068,67,0,0,false));
 }
 public static List<Bake> bakes(){return Collections.unmodifiableList(BAKES);}
 public static List<Preparation> preparations(){return Collections.unmodifiableList(PREPS);}
 public static Bake forRaw(int raw){for(Bake b:BAKES)if(b.raw==raw)return b;return null;}
 public static boolean isFire(int object){return object==2732||object==5249||object==26185||object==13528;}
 public static boolean select(Player p,int raw,int object){
  Bake b=forRaw(raw);if(b==null)return false;if(!io.xeros.content.questing.CooksAssistant.rangeAllowed(p,object))return true;if(!p.getUtilityMagic().canInteract())return true;
  if(p.playerLevel[7]<b.level){p.sendMessage("You need Cooking level "+b.level+".");return true;}
  if(b.rangeOnly&&isFire(object)){p.sendMessage("You need a cooking range for this food.");return true;}
  if(!p.getItems().playerHasItem(raw))return true;
  p.interruptActions();p.getPA().stopSkilling();p.playerSkillProp[7][0]=raw;p.playerSkillProp[7][5]=object;
  if(p.getItems().getInventoryCount(raw)==1)startBake(p,b,object,1);
  else {p.getPA().sendChatboxInterface(1743);p.getPA().sendFrame246(13716,190,raw);p.getPA().sendFrame126(ItemAssistant.getItemName(raw),13717);}
  return true;
 }
 public static boolean amount(Player p,int quantity){Bake b=forRaw(p.playerSkillProp[7][0]);if(b==null)return false;startBake(p,b,p.playerSkillProp[7][5],quantity);return true;}
 public static boolean startBake(Player p,Bake b,int object,int quantity){
  if(!io.xeros.content.questing.CooksAssistant.rangeAllowed(p,object))return false;
  if(b==null||!BAKES.contains(b)||!p.getUtilityMagic().canInteract()||quantity<=0||p.playerLevel[7]<b.level||b.rangeOnly&&isFire(object)||p.isDead||p.getLock().cannotInteract(p))return false;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();final long token=p.productionActionToken;final int x=p.absX,y=p.absY,h=p.heightLevel;final int[] left={Math.min(quantity,28)};
  p.startAnimation(isFire(object)?897:896);
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
   public void execute(){
    if(token!=p.productionActionToken||!p.getUtilityMagic().canInteract()||p.isDisconnected()||p.getSession()==null||p.isDead||p.getLock().cannotInteract(p)||p.absX!=x||p.absY!=y||p.heightLevel!=h||p.playerLevel[7]<b.level){stop();return;}
    int burnStop=io.xeros.content.questing.CooksAssistant.burnLevel(p,b.raw,object,b.stop);
    boolean success=SkillcapePerks.COOKING.isWearing(p)||SkillcapePerks.isWearingMaxCape(p)||p.playerLevel[7]>=burnStop||Math.random()<Math.min(1,.6+.4*(p.playerLevel[7]-b.level)/(double)Math.max(1,burnStop-b.level));
    int[][] outputs=b.extra==0?new int[][]{{success?b.cooked:b.burnt,1}}:new int[][]{{success?b.cooked:b.burnt,1},{b.extra,1}};
    if(!ProductionBatch.exchange(p,new int[][]{{b.raw,1}},outputs)){stop();return;}
    if(success)p.getPA().addSkillXPMultiplied(b.xp,7,true);
    p.sendMessage(success?"You cook the food.":"You accidentally burn the food.");p.startAnimation(isFire(object)?897:896);maximumTicks=4;
    if(--left[0]<=0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken)p.startAnimation(65535);}
  });return true;
 }
 public static boolean prepare(Player p,Preparation r){
  if(!PREPS.contains(r)||!p.getUtilityMagic().canInteract())return false;
  int[][] inputs=r.tool?new int[][]{{r.first,0},{r.second,1},{1923,1}}:new int[][]{{r.first,1},{r.second,1}};
  int[][] outputs=r.extra==0?new int[][]{{r.product,1}}:new int[][]{{r.product,1},{r.extra,1}};
  return ProductionBatch.start(p,7,r.level,r.xp,1,883,2,inputs,outputs,null,()->p.getUtilityMagic().canInteract());
 }
 public static boolean itemOnItem(Player p,int first,int second){
  if(!p.getUtilityMagic().canInteract())return false;
  int bowlIngredient=first==1923?second:second==1923?first:0;
  if(bowlIngredient==1982||bowlIngredient==2142){
   for(Preparation r:PREPS)if(r.first==946&&r.second==bowlIngredient){prepare(p,r);return true;}
  }
  int spice=first==2007||first==2169?first:second==2007||second==2169?second:0;
  int chopped=spice==first?second:first;
  if(spice!=0&&(chopped==1869||chopped==7070)){
   if(!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||p.getInterfaceEvent().isActive()
      ||p.getBankPin().requiresUnlock()||Server.getMultiplayerSessionListener().inAnySession(p))return true;
   p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
   // Both spice shakers are reusable tools in the 2022 recipes; preparation grants no Cooking XP.
   ProductionBatch.exchange(p,new int[][]{{spice,0},{chopped,1}},new int[][]{{chopped==1869?9994:9996,1}});
   return true;
  }
  for(Preparation r:PREPS)if(first==r.first&&second==r.second||first==r.second&&second==r.first){prepare(p,r);return true;}
  int water=first==1933?second:second==1933?first:0;
  if(water==1929||water==1937||water==1921){
   new DialogueBuilder(p).option("Make dough",new DialogueOption("Bread",q->dough(q,water,2307)),new DialogueOption("Pastry",q->dough(q,water,1953)),new DialogueOption("Pizza base",q->dough(q,water,2283)),new DialogueOption("Pitta",q->dough(q,water,1863))).send();return true;
  }
  if(first==1887||second==1887){int ingredient=first==1887?second:first;if(ingredient==1933||ingredient==1927||ingredient==1944){
   ProductionBatch.start(p,7,40,0,1,883,2,new int[][]{{1887,1},{1933,1},{1927,1},{1944,1}},new int[][]{{1889,1},{1931,1},{1925,1}},null,()->p.getUtilityMagic().canInteract());return true;
  }}
  return false;
 }
 public static boolean dough(Player p,int water,int product){
  if(!p.getUtilityMagic().canInteract())return false;
  if(water!=1929&&water!=1937&&water!=1921)return false;
  int level=product==1863?58:product==2283?35:1;if(product!=1863&&product!=2283&&product!=2307&&product!=1953)return false;
  int empty=water==1929?1925:water==1937?1935:1923;
  return ProductionBatch.start(p,7,level,0,1,883,2,new int[][]{{1933,1},{water,1}},new int[][]{{product,1},{1931,1},{empty,1}},null,()->p.getUtilityMagic().canInteract());
 }
}
