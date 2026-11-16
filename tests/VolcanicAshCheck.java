package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.mining.*;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import io.xeros.model.definitions.*;import java.util.*;import java.lang.reflect.*;import java.util.function.IntUnaryOperator;
public class VolcanicAshCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static final IntUnaryOperator ASH=bound->bound==20||bound==4||bound==6?1:0;
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static List<WorldObject> nodes()throws Exception{Field regions=RegionProvider.class.getDeclaredField("regions"),objects=Region.class.getDeclaredField("worldObjects");regions.setAccessible(true);objects.setAccessible(true);List<WorldObject> nodes=new ArrayList<>();for(Region region:((Map<Integer,Region>)regions.get(RegionProvider.getGlobal())).values())for(WorldObject o:(List<WorldObject>)objects.get(region))if(VolcanicAsh.nativePile(o))nodes.add(o);ok(nodes.size()==16,"Sixteen real Fossil Island piles");return nodes;}
 static Player actor(WorldObject o,int level){Player p=AttackDefencePlayableCheck.actor();p.absX=o.x-1;p.absY=o.y;p.heightLevel=o.height;p.playerLevel[14]=level;p.playerXP[14]=p.getPA().getXPForLevel(level);give(p,1265,1);return p;}
 static void xp(Player p,int before,double base){Player reference=AttackDefencePlayableCheck.actor();int old=reference.playerXP[14];reference.getPA().addSkillXPMultiplied(base,14,false);ok(p.playerXP[14]-before==reference.playerXP[14]-old,"Mining XP per extraction");}
 static void tables(){
  for(int visible=1;visible<=110;visible++)for(int boost:new int[]{0,4}){int effective=Math.max(1,Math.min(99,visible))+boost;int expected=Math.min(256,1+(4900+150*(effective-1)+49)/98);ok(VolcanicAsh.threshold(visible,boost)==expected,"Chance interpolation "+visible+" "+boost);}
  int[][] bands={{22,1},{36,1},{37,2},{51,2},{52,3},{66,3},{67,4},{81,4},{82,5},{96,5},{97,6},{104,6}};
  for(int[] band:bands)ok(VolcanicAsh.amount(band[0])==band[1],"Ash yield threshold");
  Pickaxe[] picks={Pickaxe.BRONZE,Pickaxe.IRON,Pickaxe.STEEL,Pickaxe.BLACK,Pickaxe.MITHRIL,Pickaxe.ADAMANT,Pickaxe.RUNE};int[] delays={8,7,6,5,5,4,3};for(int i=0;i<picks.length;i++)ok(VolcanicAsh.delay(picks[i],ASH)==delays[i],"Pickaxe action delay");
  for(int roll=0;roll<6;roll++){final int result=roll;ok(VolcanicAsh.delay(Pickaxe.DRAGON,n->result)==(roll==0?2:3),"Dragon skip roll");}
  System.out.println("ASH_220_CHANCE_CASES_TWELVE_YIELD_BOUNDARIES_AND_PICKAXE_DELAYS_OK");
 }
 static void yields(WorldObject o){
  for(int level:new int[]{22,37,52,67,82,97}){
   Player p=actor(o,level);int old=p.playerXP[14];ok(VolcanicAsh.start(p,o.x,o.y,ASH),"Start ash level "+level);tick(7);ok(count(p,21622)==0,"Premature bronze extraction");tick(1);ok(count(p,21622)==VolcanicAsh.amount(level),"Ash quantity "+level);xp(p,old,10);clear(p);
  }
  int soda=0,volcanic=0;
  for(int outcome=0;outcome<20;outcome++){
   final int product=outcome;Player p=actor(o,99);give(p,1275,1);int old=p.playerXP[14];IntUnaryOperator random=bound->bound==20?product:bound==4?1:0;
   VolcanicAsh.start(p,o.x,o.y,random);tick(3);if(outcome==0){ok(count(p,1781)==1&&count(p,21622)==0,"Soda must stay one at level 99");soda++;}else{ok(count(p,21622)==6&&count(p,1781)==0,"19/20 volcanic outcome");volcanic++;}xp(p,old,10);clear(p);
  }
  ok(soda==1&&volcanic==19,"Soda/ash distribution");
  System.out.println("ASH_SIX_LEVEL_YIELDS_TWENTY_PRODUCT_OUTCOMES_AND_FIXED_TEN_XP_OK");
 }
 static void guards(WorldObject o){
  Player p=actor(o,21);ok(!VolcanicAsh.start(p,o.x,o.y,ASH),"Level 21 ash accepted");tick(8);ok(count(p,21622)==0,"Low level produced ash");clear(p);
  p=actor(o,22);p.getItems().deleteItem2(1265,1);ok(!VolcanicAsh.start(p,o.x,o.y,ASH),"Missing pickaxe");clear(p);
  p=actor(o,99);VolcanicAsh.start(p,o.x,o.y,ASH);p.absY++;tick(8);ok(count(p,21622)==0,"Moved mining");clear(p);
  p=actor(o,99);VolcanicAsh.start(p,o.x,o.y,ASH);p.heightLevel++;tick(8);ok(count(p,21622)==0,"Changed-floor mining");clear(p);
  p=actor(o,99);VolcanicAsh.start(p,o.x,o.y,ASH);p.interruptActions();tick(8);ok(count(p,21622)==0,"Cancelled mining");clear(p);
  p=actor(o,99);VolcanicAsh.start(p,o.x,o.y,ASH);p.getItems().deleteItem2(1265,1);tick(8);ok(count(p,21622)==0,"Lost pickaxe mining");clear(p);
  p=actor(o,99);give(p,1275,1);int old=p.playerXP[14];VolcanicAsh.start(p,o.x,o.y,n->n==256?255:0);tick(12);ok(count(p,21622)==0&&count(p,1781)==0&&p.playerXP[14]==old&&!Server.getGlobalObjects().exists(30986,o.x,o.y,o.height),"Failed attempts reward or deplete");clear(p);
  p=actor(o,99);give(p,21622,1);while(p.getItems().freeSlots()>0)give(p,1931,1);old=p.playerXP[14];VolcanicAsh.start(p,o.x,o.y,ASH);tick(8);ok(count(p,21622)==7&&p.getItems().freeSlots()==0,"Full bag existing ash stack");xp(p,old,10);clear(p);
  old=p.playerXP[14];VolcanicAsh.start(p,o.x,o.y,n->0);tick(8);ok(count(p,1781)==0&&p.playerXP[14]==old&&!Server.getGlobalObjects().exists(30986,o.x,o.y,o.height),"Full bag soda lost items/XP/depleted");clear(p);
  p=actor(o,99);give(p,21622,1);for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]==21623)p.playerItemsN[i]=Integer.MAX_VALUE;old=p.playerXP[14];VolcanicAsh.start(p,o.x,o.y,ASH);tick(8);ok(count(p,21622)==Integer.MAX_VALUE&&p.playerXP[14]==old,"Ash stack overflow");clear(p);
  System.out.println("ASH_LEVEL_TOOL_ORIGIN_FLOOR_CANCEL_FAILED_ROLL_FULL_BAG_SODA_AND_OVERFLOW_GUARDS_OK");
 }
 static void rings(WorldObject o){
  for(int id:new int[]{25539,25541,25543,25545}){
   Player p=actor(o,99);give(p,1275,1);give(p,id,1);int slot=-1;for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]==id+1)slot=i;
   ok(ItemDef.getDefinitions().containsKey(id)&&ItemStats.forId(id).getEquipment().getSlot()==Player.playerRing,"Missing ring definition/equipment slot");
   ok(p.getItems().equipItem(id,slot)&&p.playerEquipment[Player.playerRing]==id,"Native equip ring "+id);
   VolcanicAsh.start(p,o.x,o.y,bound->bound==256?201:bound==20||bound==4?1:0);tick(3);ok(count(p,21622)==6,"Ring +4 chance "+id);clear(p);
  }
  Player p=actor(o,99);give(p,1275,1);int old=p.playerXP[14];VolcanicAsh.start(p,o.x,o.y,bound->bound==256?201:1);tick(3);ok(count(p,21622)==0&&p.playerXP[14]==old,"Unboosted threshold must fail roll 201");clear(p);
  System.out.println("ASH_FOUR_CELESTIAL_RING_TYPES_REAL_EQUIP_AND_PLUS_FOUR_INVISIBLE_BOOST_OK");
 }
 static void depletion(WorldObject o){
  for(int result=0;result<4;result++){
   final int deplete=result;Player p=actor(o,99);give(p,1275,1);VolcanicAsh.start(p,o.x,o.y,bound->bound==4?deplete:bound==20?1:0);tick(3);clear(p);
   ok(Server.getGlobalObjects().exists(30986,o.x,o.y,o.height)==(result==0),"1/4 depletion result");
   if(result==0){ok(!VolcanicAsh.start(p,o.x,o.y,ASH),"Mined depleted pile");for(int i=0;i<49;i++)Server.getGlobalObjects().pulse();ok(Server.getGlobalObjects().exists(30986,o.x,o.y,o.height),"Early ash respawn");Server.getGlobalObjects().pulse();ok(!Server.getGlobalObjects().exists(30986,o.x,o.y,o.height)&&RegionProvider.getGlobal().get(o.x,o.y).getWorldObject(30985,o.x,o.y,o.height).isPresent(),"Ash did not restore after 50 ticks");}
  }
  System.out.println("ASH_FOUR_DEPLETION_OUTCOMES_EMPTY_PILE_BLOCK_AND_FIFTY_TICK_NATIVE_RESTORE_OK");
 }
 static void compost(WorldObject o){Player p=actor(o,37);give(p,6034,1);VolcanicAsh.start(p,o.x,o.y,ASH);tick(8);clear(p);ok(count(p,21622)==2,"Natural compost ash");int xp=p.playerXP[19];p.getFarming().itemOnItem(6034,21622);tick(2);ok(count(p,21483)==1&&count(p,6034)==0&&count(p,21622)==0&&p.playerXP[19]==xp,"Mined ash to 2022 ultracompost");clear(p);System.out.println("ASH_REAL_MINING_OUTPUT_TO_ULTRACOMPOST_MATERIAL_CHAIN_OK");}
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;List<WorldObject> nodes=nodes();WorldObject node=nodes.get(0);tables();yields(node);guards(node);rings(node);depletion(node);compost(node);System.out.println("VOLCANIC_ASH_ALL_CHECKS_OK nativeNodes="+nodes.size());System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
