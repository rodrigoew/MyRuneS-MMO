package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.prayer.*;import io.xeros.content.skills.construction.*;import io.xeros.model.collisionmap.WorldObject;import io.xeros.model.definitions.*;import io.xeros.model.entity.player.*;import java.lang.reflect.*;import java.util.*;import java.util.function.*;
public final class PrayerPreservationCheck {
 static final int[][] PIECES={{0,25438},{4,25434},{7,25436},{10,25440}};
 static Field random,lastAction,chaosRoll;static Method sacrifice;
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}static int count(Player p,int item){return p.getItems().getInventoryCount(item);}static void bag(Player p){HouseWorkshopCheck.bag(p);}static void give(Player p,int item,int n){HouseWorkshopCheck.give(p,item,n);}static long xp(Player p){return p.playerXP[5]*10L+p.playerXpTenths[5];}
 static void wear(Player p,int mask){Arrays.fill(p.playerEquipment,-1);for(int i=0;i<4;i++)if((mask&(1<<i))!=0)p.playerEquipment[PIECES[i][0]]=PIECES[i][1];}
 static void roll(int r)throws Exception{random.set(null,(IntUnaryOperator)(bound->{ok(bound==80,"Uniform eighty outcomes");return r;}));}
 static void probabilities(){
  Player p=AttackDefencePlayableCheck.actor();int[][] expected={{25438,0,1},{25434,4,7},{25436,7,6},{25440,10,1}};
  for(int[] row:expected){ItemDef def=ItemDef.forId(row[0]);var stats=ItemStats.forId(row[0]);ok(!def.isTradable()&&def.getNoteId()==-1&&def.getRequirements().isEmpty()&&Boolean.TRUE.equals(stats.getEquipable())&&stats.getEquipment().getSlot()==row[1]&&stats.getEquipment().getPrayer()==row[2],"2021 literal item definition/stats "+row[0]);}
  for(int mask=0;mask<16;mask++){wear(p,mask);int saves=0;for(int r=0;r<80;r++)if(PrayerPreservation.saves(p,r))saves++;ok(saves==Integer.bitCount(mask)&&PrayerPreservation.pieces(p)==Integer.bitCount(mask),"All combinations of1/80 per piece "+mask);}
  Arrays.fill(p.playerEquipment,-1);p.playerEquipment[3]=25438;ok(PrayerPreservation.pieces(p)==0,"Wrong equipment slot not counted");System.out.println("PRAYER_PRESERVATION_16_EQUIPMENT_COMBINATIONS_1280_LITERAL_ROLLS_1_25_PERCENT_PER_PIECE_MAX5_PERCENT_FOUR_2021_NATIVE_ITEM_DEFINITIONS_STATS_AND_CORRECT_SLOTS_OK");
 }
 static void burialAndAltars()throws Exception{
  Player p=AttackDefencePlayableCheck.actor();int cases=0;
  for(int mask=0;mask<16;mask++)for(int r=0;r<80;r++){
   wear(p,mask);bag(p);give(p,536,1);roll(r);lastAction.set(p.getPrayer(),0L);long before=xp(p);p.getPrayer().bury(Bone.DRAG);ok(count(p,536)==(r<Integer.bitCount(mask)?1:0)&&xp(p)==before+720,"Actual burial consumption/base72XP "+mask+"/"+r);cases++;
  }
  ok(cases==1280,"All actual burial rolls");int chaos=0,total=0;
  for(int mask=0;mask<16;mask++)for(int c=0;c<2;c++)for(int r=0;r<80;r++){
   wear(p,mask);bag(p);give(p,536,1);roll(r);final double result=c==0?.25:.75;chaosRoll.set(p.getPrayer(),(DoubleSupplier)()->result);long before=xp(p);boolean saved=(Boolean)sacrifice.invoke(p.getPrayer(),Bone.DRAG,2950,3820,3.5,true);boolean expected=c==0||r<Integer.bitCount(mask);ok(saved==expected&&count(p,536)==(expected?1:0)&&xp(p)==before+2520,"Chaos and robes independent conditional rolls "+mask+"/"+c+"/"+r);if(mask==15&&saved)chaos++;total++;
  }
  ok(total==2560&&chaos==84,"Full robes84of160=52.5% actual chaos rolls");
  for(int halo:new int[]{12637,12638,12639}){wear(p,0);p.playerEquipment[0]=halo;bag(p);give(p,536,1);long before=xp(p);sacrifice.invoke(p.getPrayer(),Bone.DRAG,2950,3820,3.5,false);ok(count(p,536)==0&&xp(p)==before+2520,"Halo no artificial5% Prayer XP "+halo);}
  wear(p,15);roll(0);bag(p);give(p,25769,1);lastAction.set(p.getPrayer(),0L);long before=xp(p);p.getPrayer().scatter(25769);ok(count(p,25769)==0&&xp(p)>before,"2022 Zealot excludes scattering ashes");
  System.out.println("PRAYER_PRESERVATION_1280_ACTUAL_BURIALS_2560_ACTUAL_CHAOS_SACRIFICES_FULL_SET84_OF160_52_5_PERCENT_BASE_XP_UNCHANGED_THREE_HALOS_NO_ARTIFICIAL_XP_AND_ASHES_EXCLUDED_OK");
 }
 static void chapel()throws Exception{
  Player p=HouseChapelCheck.fixture("GILDED_ALTAR",null,null,null,0);WorldObject altar=HouseChapelCheck.model(p,13197);HouseChapelCheck.at(p,altar);int tests=0;
  for(int mask=0;mask<16;mask++)for(int r=0;r<80;r++){
   wear(p,mask);bag(p);give(p,536,1);roll(r);long before=xp(p);p.getHouse().chapel().use(536,altar);SkillsAuditCheck.tick(1);ok(count(p,536)==(r<Integer.bitCount(mask)?1:0)&&xp(p)==before+1800,"Actual POH saved bone with normal250% XP "+mask+"/"+r);p.interruptActions();tests++;
  }
  wear(p,15);bag(p);give(p,536,1);roll(0);long before=xp(p);p.getHouse().chapel().use(536,altar);SkillsAuditCheck.tick(7);ok(count(p,536)==1&&xp(p)==before+5400,"Saved bone can be offered repeatedly automatically");p.interruptActions();SkillsAuditCheck.tick(3);ok(count(p,536)==1&&xp(p)==before+5400,"Interrupted saved bone no ghost XP");p.getHouse().exit();ok(tests==1280,"All POH rolls");System.out.println("PRAYER_PRESERVATION_1280_ACTUAL_POH_ALTAR_ROLLS_BASE_MULTIPLIER_XP_SAVED_BONE_AUTO_REPEATED_EVERY_THREE_TICKS_AND_INTERRUPTION_NO_GHOST_XP_OK");
 }
 public static void main(String[] args){Object old=null;try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());random=PrayerPreservation.class.getDeclaredField("random");random.setAccessible(true);old=random.get(null);lastAction=Prayer.class.getDeclaredField("lastAction");lastAction.setAccessible(true);chaosRoll=Prayer.class.getDeclaredField("altarRoll");chaosRoll.setAccessible(true);sacrifice=Prayer.class.getDeclaredMethod("sacrifice",Bone.class,int.class,int.class,double.class,boolean.class);sacrifice.setAccessible(true);probabilities();burialAndAltars();chapel();random.set(null,old);System.out.println("PRAYER_PRESERVATION_ALL_CHECKS_PASSED");System.exit(0);}catch(Throwable t){try{if(random!=null&&old!=null)random.set(null,old);}catch(Exception ignored){}t.printStackTrace();System.exit(1);}}
}
