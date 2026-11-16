package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.prayer.*;import io.xeros.content.combat.death.NPCDeath;import io.xeros.model.entity.player.*;import io.xeros.model.entity.npc.NPC;import io.xeros.model.definitions.ItemDef;import java.lang.reflect.*;import java.util.*;import java.util.function.DoubleSupplier;
public class PrayerRemainsReviewCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){return AttackDefencePlayableCheck.actor();}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void roll(Prayer p,double value)throws Exception{Field f=Prayer.class.getDeclaredField("altarRoll");f.setAccessible(true);f.set(p,(DoubleSupplier)()->value);}
 static void bones(){
  for(Bone bone:Bone.values()){Player p=actor();give(p,bone.getItemId(),1);int old=p.playerXP[5];p.getPrayer().bury(bone);ok(count(p,bone.getItemId())==0,"Bone not consumed "+bone);ExpandedSkillsCheck.xp(p,5,old,bone.getExperience());old=p.playerXP[5];p.getPrayer().bury(bone);ok(p.playerXP[5]==old,"Duplicate burial "+bone);clear(p);}
  ok(Bone.RAURG.getExperience()==96&&Bone.HYDRA.getExperience()==110,"Historical bone XP");Player p=actor();p.playerXP[5]=0;give(p,22124,1);p.getPrayer().bury(Bone.SUPERIOR);ok(count(p,22124)==1,"Superior bones below base Prayer 70");
  System.out.println("REVIEW_ALL_BONE_TYPES_BURY_CONSUMPTION_XP_DUPLICATE_GATE_AND_SUPERIOR_REQUIREMENT_OK "+Bone.values().length);
 }
 static void ashes(){
  for(Ashes ash:Ashes.values()){Player p=actor();ok(ItemDef.forId(ash.item).getName().toLowerCase().contains("ashes"),"Ash definition");give(p,ash.item,2);int old=p.playerXP[5];ok(p.getPrayer().scatter(ash.item),"Scatter dispatch");ok(count(p,ash.item)==1,"Ash consumed");ExpandedSkillsCheck.xp(p,5,old,ash.xp);old=p.playerXP[5];p.getPrayer().scatter(ash.item);ok(count(p,ash.item)==1&&p.playerXP[5]==old,"Scatter cooldown");ok(!Prayer.isOperableBone(ash.item).isPresent(),"Ash allowed on bone altar");}
  Player p=actor();int old=p.playerXP[5];p.getPrayer().scatter(25778);ok(p.playerXP[5]==old,"Absent ashes granted XP");
  System.out.println("REVIEW_FIVE_ASH_TYPES_SCATTER_XP_RESOURCES_COOLDOWN_ALTAR_EXCLUSION_OK");
 }
 static void altar()throws Exception{
  Player p=actor();p.absX=2950;p.absY=3820;give(p,536,1);p.getPrayer().setAltarBone(Optional.of(Bone.DRAG));roll(p.getPrayer(),0);int old=p.playerXP[5];p.getPrayer().alter(1,2950,3820);ok(count(p,536)==1,"First chaos sacrifice failed to preserve");ExpandedSkillsCheck.xp(p,5,old,72*3.5);p.interruptActions();SkillsAuditCheck.tick(4);ok(count(p,536)==1,"Cancelled altar consumed bone");clear(p);
  p=actor();p.absX=2950;p.absY=3820;give(p,536,2);p.getPrayer().setAltarBone(Optional.of(Bone.DRAG));roll(p.getPrayer(),1);old=p.playerXP[5];p.getPrayer().alter(2,2950,3820);SkillsAuditCheck.tick(3);ok(count(p,536)==0,"Chaos consumed path");ExpandedSkillsCheck.xp(p,5,old,72*7);clear(p);
  p=actor();give(p,536,1);p.getPrayer().setAltarBone(Optional.of(Bone.DRAG));p.getPrayer().alter(1,3010,3210);ok(count(p,536)==1,"Remote altar accepted");
  System.out.println("REVIEW_CHAOS_ALTAR_FIRST_BONE_PRESERVE_CONSUME_3_5_XP_CANCELLATION_DISTANCE_OK");
 }
 static void drops(){
  int[][] ids={{3134,25766},{2005,25769},{240,25772},{124,25775},{5862,25778}};
  Player p=actor();p.setIndex(1);p.mapRegionX=(p.absX >> 3)-6;p.mapRegionY=(p.absY >> 3)-6;p.isActive=true;PlayerHandler.players[1]=p;
  try{for(int[] row:ids){NPC npc=new NPC(row[0],new Position(p.absX,p.absY,0));NPCDeath.dropItemsFor(npc,p,row[0]);ok(Server.itemHandler.getGroundItem(p,row[1],p.absX,p.absY,0)!=null,"Death route missed ashes "+row[0]+" "+npc.getName());}}
  finally{PlayerHandler.players[1]=null;clear(p);}
  System.out.println("REVIEW_REAL_NPC_DEATH_ROUTE_FIVE_TIERS_DEMONIC_ASH_GROUND_DROPS_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;bones();ashes();altar();drops();System.out.println("PRAYER_REMAINS_REVIEW_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}

