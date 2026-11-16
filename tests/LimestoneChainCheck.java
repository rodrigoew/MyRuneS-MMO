package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.crafting.LimestoneCrafting;
import io.xeros.content.skills.smithing.Quicklime;
import io.xeros.content.skills.mining.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.entity.player.*;
import java.util.*;
import java.util.function.IntUnaryOperator;

public final class LimestoneChainCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static IntUnaryOperator ore(){return b->b==256?0:b-1;}
 static Player actor(WorldObject o){Player p=MiningGuildCheck.actor(o.x-1,o.y);p.heightLevel=o.height;give(p,1275,1);return p;}
 static List<WorldObject> nodes(int id)throws Exception{
  var f=RegionProvider.class.getDeclaredField("regions");f.setAccessible(true);
  Map<String,WorldObject> all=new LinkedHashMap<>();
  for(Region r:((Map<Integer,Region>)f.get(RegionProvider.getGlobal())).values())for(WorldObject o:r.worldObjectsSnapshot())
   if(o.id==id)all.putIfAbsent(o.x+":"+o.y+":"+o.height+":"+o.type,o);
  return new ArrayList<>(all.values());
 }
 static int live(Player p,WorldObject o){for(int id=11382;id<=11385;id++)if(p.getRegionProvider().get(o.x,o.y).getWorldObject(id,o.x,o.y,o.height).isPresent())return id;return -1;}
 static void reset(){for(int i=0;i<9;i++)Server.getGlobalObjects().pulse();}
 static void xp(Player p,int skill,int old,double... amounts){Player q=AttackDefencePlayableCheck.actor();int before=q.playerXP[skill];for(double amount:amounts)q.getPA().addSkillXPMultiplied(amount,skill,true);ok(p.playerXP[skill]-old==q.playerXP[skill]-before,"Exact XP "+skill+" actual="+(p.playerXP[skill]-old)+" expected="+(q.playerXP[skill]-before));}
 static void cycles(List<WorldObject> rows){
  ok(rows.size()>=10,"Native limestone locations "+rows.size());Set<String> regions=new TreeSet<>();
  for(WorldObject o:rows){
   Player p=actor(o);int old=p.playerXP[14],clip=p.getRegionProvider().getClipping(o.x,o.y,o.height);
   ok(LimestoneMining.start(p,11382,o.x,o.y,ore()),"Start "+o);
   tick(2);ok(count(p,3211)==0&&live(p,o)==11382,"No early ore");
   tick(1);ok(count(p,3211)==1&&live(p,o)==11383,"First stage");
   Player second=actor(o);ok(!LimestoneMining.start(second,11382,o.x,o.y,ore()),"Stale first-stage packet");
   ok(LimestoneMining.start(second,11383,o.x,o.y,ore()),"Second player current stage");
   SkillsAuditCheck.clear(p);tick(3);SkillsAuditCheck.clear(second);
   ok(count(second,3211)==1&&live(p,o)==11384,"Second player shares depletion");
   ok(LimestoneMining.start(p,11384,o.x,o.y,ore()),"Restart third stage");tick(3);SkillsAuditCheck.clear(p);
   ok(count(p,3211)==2&&live(p,o)==11385,"Third primary ore empties");
   xp(p,14,old,26.5,26.5);
   ok(!LimestoneMining.start(actor(o),11385,o.x,o.y,ore()),"Empty rejected");
   for(int i=0;i<8;i++)Server.getGlobalObjects().pulse();
   ok(live(p,o)==11385,"No early respawn");Server.getGlobalObjects().pulse();
   ok(live(p,o)==11382,"Exact nine tick respawn");
   ok(p.getRegionProvider().getClipping(o.x,o.y,o.height)==clip,"Clipping restore "+o);
   regions.add((o.x>>6)+":"+(o.y>>6));
  }
  System.out.println("LIMESTONE_ALL_NATIVE_LOCATIONS_THREE_SHARED_STAGES_TWO_PLAYERS_STALE_PACKET_REJECTION_EXACT_XP_NINE_TICK_RESPAWN_COLLISION_OK count="+rows.size()+" regions="+regions);
 }
 static void miningGuards(WorldObject o){
  for(int mode=0;mode<10;mode++){
   Player p=actor(o);int old=p.playerXP[14];
   if(mode==0){p.playerLevel[14]=9;ok(!LimestoneMining.start(p,11382,o.x,o.y,ore()),"Mining10");continue;}
   if(mode==1){p.absX-=10;ok(!LimestoneMining.start(p,11382,o.x,o.y,ore()),"Remote");continue;}
   if(mode==2){p.heightLevel++;ok(!LimestoneMining.start(p,11382,o.x,o.y,ore()),"Height");continue;}
   ok(LimestoneMining.start(p,11382,o.x,o.y,ore()),"Guard start");
   if(mode==3)p.absX++;if(mode==4)p.interruptActions();if(mode==5)p.getItems().deleteItem2(1275,1);
   if(mode==6)p.isDead=true;if(mode==7)p.playerLevel[14]=9;if(mode==8)p.heightLevel++;
   if(mode==9)while(p.getItems().freeSlots()>0)give(p,229,1);
   tick(6);SkillsAuditCheck.clear(p);ok(count(p,3211)==0&&p.playerXP[14]==old&&live(actor(o),o)==11382,"Guard no reward/depletion "+mode);
  }
  Player pre=actor(o);int old=pre.playerXP[14];
  LimestoneMining.start(pre,11382,o.x,o.y,b->b==32768?70:b-1);tick(3);SkillsAuditCheck.clear(pre);
  ok(count(pre,1623)==1&&count(pre,3211)==0&&pre.playerXP[14]==old&&live(pre,o)==11382,"Gem pre-roll not resource/XP/depletion");
  Player bonus=actor(o);bonus.playerEquipment[Player.playerChest]=13104;bonus.playerEquipment[Player.playerCape]=9792;
  bonus.playerEquipment[Player.playerRing]=25541;ok(bonus.getMiningTools().decode("5000,1,0"),"Charged ring");
  old=bonus.playerXP[14];LimestoneMining.start(bonus,11382,o.x,o.y,b->b==256||b==100||b==10?0:b-1);
  tick(3);SkillsAuditCheck.clear(bonus);
  ok(count(bonus,3211)==4&&live(bonus,o)==11383&&bonus.getMiningTools().charges(25541)==0,"Three independent bonuses only consume one rock stage and ring charge");
  xp(bonus,14,old,26.5,26.5,26.5,26.5);ok(LimestoneMining.start(bonus,11383,o.x,o.y,ore()),"Finish bonus rock");tick(6);SkillsAuditCheck.clear(bonus);reset();
  Player gloves=actor(o);gloves.playerEquipment[Player.playerHands]=21392;LimestoneMining.start(gloves,11382,o.x,o.y,ore());tick(9);SkillsAuditCheck.clear(gloves);ok(live(gloves,o)==11385&&count(gloves,3211)==3,"2022 gloves do not prevent limestone depletion");reset();
  System.out.println("LIMESTONE_LEVEL10_DISTANCE_HEIGHT_MOVEMENT_CANCEL_TOOL_DEATH_LEVEL_DRAIN_FULL_INV_GEM_PRE_ROLL_CAPE_ARMOUR_RING_BONUSES_NO_GLOVE_PRESERVATION_OK");
 }
 static void bricks(){
  for(int lvl:new int[]{11,12,39,40,99})for(int roll:new int[]{0,83}){
   Player p=AttackDefencePlayableCheck.actor();p.playerLevel[12]=lvl;give(p,1755,1);give(p,3211,1);int old=p.playerXP[12];
   if(lvl==11){ok(!LimestoneCrafting.chisel(p,b->roll)&&count(p,3211)==1&&p.playerXP[12]==old,"Crafting12");continue;}
   boolean failed=lvl<40&&roll==0;ok(LimestoneCrafting.chisel(p,b->roll),"Chisel");
   ok(count(p,failed?968:3420)==1&&count(p,1755)==1&&count(p,3211)==0,"Brick/failure/tool");xp(p,12,old,failed?1.5:6);
  }
  Player full=AttackDefencePlayableCheck.actor();give(full,1755,1);give(full,3211,1);while(full.getItems().freeSlots()>0)give(full,229,1);
  ok(LimestoneCrafting.chisel(full,b->83)&&count(full,3420)==1&&full.getItems().freeSlots()==0,"Full inv atomic replacement");
  Player absent=AttackDefencePlayableCheck.actor();give(absent,3211,1);ok(!LimestoneCrafting.chisel(absent,b->83)&&count(absent,3211)==1,"No tool");
  System.out.println("LIMESTONE_CRAFTING_LEVEL12_SUCCESS6XP_FAILURE_ROCK1_POINT5XP_NO_FAILURE_LEVEL40_TOOL_RETAINED_FULL_INV_ATOMIC_REPLACEMENT_OK");
 }
 static void lime()throws Exception{
  WorldObject f=null;for(int id:new int[]{24009,16469,26814,11666,40949}){var rows=nodes(id);if(!rows.isEmpty()){f=rows.get(0);break;}}
  if(f==null){var field=RegionProvider.class.getDeclaredField("regions");field.setAccessible(true);outer:for(Region r:((Map<Integer,Region>)field.get(RegionProvider.getGlobal())).values())for(WorldObject o:r.worldObjectsSnapshot())if("Furnace".equalsIgnoreCase(ObjectDef.getObjectDef(o.id).name)){f=o;break outer;}}
  ok(f!=null,"Native furnace");
  for(int gloves:new int[]{-1,1059,7462,21392,11076}){
   Player p=actor(f);p.getHealth().setMaximumHealth(99);p.getHealth().setCurrentHealth(99);p.playerEquipment[Player.playerHands]=gloves;give(p,3211,1);int old=p.playerXP[13];
   ok(Quicklime.start(p,f.id,f.x,f.y),"Furnace start");tick(2);ok(count(p,3213)==0,"Three tick furnace");tick(1);SkillsAuditCheck.clear(p);
   ok(count(p,3213)==1&&count(p,3211)==0&&p.playerXP[13]==old,"Quicklime zero Smithing XP");
   ok(p.getHealth().getCurrentHealth()==(Quicklime.protectedHands(p)?99:91),"Eight damage only unprotected hands "+gloves);
  }
  Player moving=actor(f);give(moving,3211,1);Quicklime.start(moving,f.id,f.x,f.y);moving.absX++;tick(3);SkillsAuditCheck.clear(moving);ok(count(moving,3211)==1&&count(moving,3213)==0,"Moving cancels smelting");
  Player grinding=AttackDefencePlayableCheck.actor();give(grinding,233,1);give(grinding,3213,1);give(grinding,1931,1);Quicklime.grind(grinding,233,3213);
  ok(count(grinding,3214)==1&&count(grinding,3213)==0&&count(grinding,1931)==0&&count(grinding,233)==1,"Pot of quicklime atomic/tool retained");
  System.out.println("LIMESTONE_NATIVE_FURNACE_THREE_TICKS_GLOVE_WHITELIST_EIGHT_DAMAGE_UNPROTECTED_NO_SMITH_XP_MOVEMENT_CANCEL_AND_GRIND_POT_TOOL_RETAINED_OK furnace="+f);
 }
 public static void main(String[] args){try{
  Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;io.xeros.model.cycleevent.CycleEventHandler.getSingleton().stopEvents(Boolean.FALSE);
  List<WorldObject> rows=nodes(11382);for(WorldObject o:rows)System.out.println("LIMESTONE_NATIVE_NODE "+o);
  for(int id=11382;id<=11385;id++){var d=ObjectDef.getObjectDef(id);System.out.println("LIMESTONE_CACHE_MODEL "+id+" name="+d.name+" actions="+Arrays.toString(d.actions));}
  cycles(rows);miningGuards(rows.get(0));bricks();lime();System.out.println("LIMESTONE_CHAIN_ALL_CHECKS_PASSED");System.exit(0);
 }catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
