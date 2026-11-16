package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.mining.*;import io.xeros.model.collisionmap.*;import io.xeros.model.entity.player.*;import io.xeros.model.definitions.*;
import java.util.*;import java.lang.reflect.*;import java.util.function.IntUnaryOperator;
public class PreciousRockCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}static void tick(int n){SkillsAuditCheck.tick(n);}static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void give(Player p,int i,int n){SkillsAuditCheck.item(p,i,n);}static int count(Player p,int i){return SkillsAuditCheck.count(p,i);}
 static Player actor(WorldObject o,int level){Player p=AttackDefencePlayableCheck.actor();p.absX=o.x-1;p.absY=o.y;p.heightLevel=o.height;
  if(PreciousRockMining.amethyst(o.id))for(int[] delta:new int[][]{{-1,0},{1,0},{0,-1},{0,1}}){int x=o.x+delta[0],y=o.y+delta[1];if((p.getRegionProvider().getClipping(x,y,o.height)&0x1280100)==0&&MiningGuildArea.contains(x,y,o.height)){p.absX=x;p.absY=y;break;}}
  if(PreciousRockMining.amethyst(o.id)&&io.xeros.content.skills.mining.MiningGuild.diaryCrystals(o.x,o.y,o.height)){for(var entry:io.xeros.content.achievement_diary.impl.FaladorAchievementDiary.ELITE_TASKS)p.getDiaryManager().getFaladorDiary().setAchievementStage(entry,entry.getMaximumStages(),false);ok(p.getMiningGuild().decodeCaveAccess("1"),"Fixture saved cave permission");} p.playerLevel[14]=level;p.playerXP[14]=p.getPA().getXPForLevel(level);give(p,1275,1);return p;}
 static void restore(){for(int i=0;i<125;i++)Server.getGlobalObjects().pulse();}
 static void xp(Player p,int before,double amount){Player ref=AttackDefencePlayableCheck.actor();int old=ref.playerXP[14];double jacket=p.playerEquipment[Player.playerChest]==13107?1.008:1;ref.getPA().addSkillXPMultiplied(amount*jacket,14,true);ok(p.playerXP[14]-before==ref.playerXP[14]-old,"Mining XP "+amount+" got"+(p.playerXP[14]-before));}
 static int slot(Player p,int id){for(int i=0;i<28;i++)if(p.playerItems[i]==id+1)return i;throw new AssertionError("Item slot");}
 static void equip(Player p,int id,int expected){give(p,id,1);ok(p.getItems().equipItem(id,slot(p,id))&&p.playerEquipment[expected]==id,"Native equipment "+id);}
 static List<WorldObject> nodes()throws Exception{List<WorldObject> out=new ArrayList<>();Field rs=RegionProvider.class.getDeclaredField("regions"),os=Region.class.getDeclaredField("worldObjects");rs.setAccessible(true);os.setAccessible(true);
  for(Region r:((Map<Integer,Region>)rs.get(RegionProvider.getGlobal())).values())for(WorldObject o:(List<WorldObject>)os.get(r))if(o.height==0&&(PreciousRockMining.gem(o.id)&&(o.type==10||o.type==11)||PreciousRockMining.amethyst(o.id)&&o.type==0))out.add(o);
  // This base layers legacy 9030 rocks over some of the 11380/11381 positions.
  // A coordinate holds one visible mining object, not two independent rocks.
  Map<String,WorldObject> unique=new LinkedHashMap<>();for(WorldObject o:out){String key=o.x+","+o.y+","+o.height;WorldObject previous=unique.get(key);if(previous==null||previous.id==9030&&o.id!=9030)unique.put(key,o);}
  System.out.println("PRECIOUS_WORLD_RAW_RECORDS "+out.size()+" UNIQUE_POSITIONS "+unique.size());return new ArrayList<>(unique.values());
 }
 static IntUnaryOperator success(int gem){return bound->bound==256?0:bound==128?gem:bound-1;}
 static void tables(){
  int[] ids={1625,1627,1629,1623,1621,1619,1617},expected={60,30,15,9,5,5,4},actual=new int[7];
  for(int roll=0;roll<128;roll++){int id=PreciousRockMining.gemForRoll(roll),index=-1;for(int i=0;i<ids.length;i++)if(ids[i]==id)index=i;ok(index>=0,"Invalid gem");actual[index]++;}ok(Arrays.equals(actual,expected),"All128 gem distribution");
  ok(Mineral.GEM.getExperience()==65&&Mineral.AMETHYST.getExperience()==240&&Mineral.GEM.getRespawnRate()==99&&Mineral.AMETHYST.getRespawnRate()==125,"Independent XP/timer tables");
  ok(Mineral.forObjectId(30371)==null&&Mineral.forObjectId(30372)==null,"Ruins are not amethyst in this cache");
  for(int level=1;level<=110;level++)for(int boost:new int[]{0,4,7,11})for(boolean gem:new boolean[]{false,true})for(boolean glory:new boolean[]{false,true}){
   double l=Math.min(99,level)+boost,low=gem?28:-18,high=gem?70:10;if(gem&&glory){low*=3;high*=3;}
   int reference=Math.max(0,Math.min(256,1+(int)Math.floor((low*(99-l)+high*(l-1))/98+.5)));
   ok(PreciousRockMining.threshold(level,boost,gem,glory)==reference,"Chance table");
  }
  ok(PreciousRockMining.petDenominator(99,true)==209411&&PreciousRockMining.petDenominator(99,false)==43875,"Pet base rates");
  System.out.println("PRECIOUS_ALL_128_GEM_ROLLS_SEVEN_WEIGHTS_NO_ONYX_DRAGONSTONE_1760_CHANCE_CASES_XP_AND_RESPAWN_TABLES_OK");
 }
 static void cycles(List<WorldObject> nodes){int tested=0;Set<Integer> types=new HashSet<>();
  for(WorldObject o:nodes){Player p=actor(o,99);int xp=p.playerXP[14];boolean gem=PreciousRockMining.gem(o.id);ok(PreciousRockMining.start(p,o.id,o.x,o.y,success(0)),"Native node start "+o+" current="+p.getRegionProvider().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,o.height)+" empty="+Server.getGlobalObjects().exists(gem?Mineral.EMPTY_VEIN:11393,o.x,o.y,o.height));tick(2);ok(p.playerXP[14]==xp,"Before rune action delay");tick(1);clear(p);
   ok(count(p,gem?1625:21347)==1,"Native node material "+o);xp(p,xp,gem?65:240);int empty=gem?Mineral.EMPTY_VEIN:11393;
   ok(Server.getGlobalObjects().exists(empty,o.x,o.y,o.height),"Always deplete without gloves");
   ok(!PreciousRockMining.start(p,o.id,o.x,o.y,success(0)),"Mined empty node");restore();ok(p.getRegionProvider().get(o.x,o.y).getWorldObject(o.id,o.x,o.y,o.height).isPresent(),"Native rock restore");tested++;types.add(o.id);
  }
  ok(types.containsAll(List.of(9030,11380,11381,11388,11389)),"Missing native object types "+types);System.out.println("PRECIOUS_ALL_NATIVE_GEM_AND_AMETHYST_OBJECTS_LEVEL_99_MATERIAL_XP_DEPLETION_AND_RESTORE_OK "+tested);
 }
 static void gemRolls(WorldObject gem){
  int[] ids={1625,1627,1629,1623,1621,1619,1617},rolls={0,60,90,105,114,119,124};
  for(int i=0;i<ids.length;i++){Player p=actor(gem,99);equip(p,13107,Player.playerChest);p.playerEquipment[Player.playerCape]=9792;int xp=p.playerXP[14];PreciousRockMining.start(p,gem.id,gem.x,gem.y,success(rolls[i]));tick(3);clear(p);ok(count(p,ids[i])==1&&count(p,1631)==0&&count(p,6571)==0,"Gem equipment must not double/replace output "+ids[i]);xp(p,xp,65);restore();}
  for(int id:new int[]{1706,1708,1710,1712,11976,11978,10354,10356,10358,10360,11964,11966,19707}){Player p=actor(gem,99);equip(p,id,Player.playerAmulet);ok(PreciousRockMining.chargedGlory(p),"Charged glory identified");int xp=p.playerXP[14];PreciousRockMining.start(p,gem.id,gem.x,gem.y,b->b==256?100:b==128?0:b-1);tick(3);ok(count(p,1625)==1,"Triple success for glory "+id);xp(p,xp,65);clear(p);restore();}
  for(int id:new int[]{1704,10362}){Player p=actor(gem,99);equip(p,id,Player.playerAmulet);ok(!PreciousRockMining.chargedGlory(p),"Uncharged glory boosted");int xp=p.playerXP[14];PreciousRockMining.start(p,gem.id,gem.x,gem.y,b->b==256?100:b-1);tick(12);ok(p.playerXP[14]==xp,"Uncharged glory succeeded at impossible roll");clear(p);}
  System.out.println("PRECIOUS_SEVEN_REAL_GEM_PRODUCTS_NO_CAPE_ARMOUR_DOUBLES_THIRTEEN_CHARGED_TWO_UNCHARGED_NATIVE_GLORIES_OK");
 }
 static void amethyst(WorldObject o){
  for(int result=0;result<10;result++){final int roll=result;Player p=actor(o,99);equip(p,13107,Player.playerChest);int xp=p.playerXP[14];PreciousRockMining.start(p,o.id,o.x,o.y,b->b==256?0:b==10?roll:b-1);tick(3);clear(p);int amount=result==0?2:1;ok(count(p,21347)==amount,"Varrock4 1/10 "+result);xp(p,xp,240*amount);restore();}
  for(int id:new int[]{13104,13105,13106}){Player p=actor(o,99);equip(p,id,Player.playerChest);int xp=p.playerXP[14];PreciousRockMining.start(p,o.id,o.x,o.y,b->b==256?0:b-1);tick(3);clear(p);ok(count(p,21347)==1,"Lower armour doubled amethyst");xp(p,xp,240);restore();}
  for(int result=0;result<4;result++){final int roll=result;Player p=actor(o,99);equip(p,21392,Player.playerHands);PreciousRockMining.start(p,o.id,o.x,o.y,b->b==256?0:b==4?roll:b-1);tick(3);clear(p);ok(count(p,21347)==1,"Expert glove extraction");ok(Server.getGlobalObjects().exists(11393,o.x,o.y,o.height)==(result!=0),"2022 expert glove percentage "+result);restore();}
  Player p=actor(o,99);give(p,1511,27);int xp=p.playerXP[14];ok(!PreciousRockMining.start(p,o.id,o.x,o.y,success(0))&&p.playerXP[14]==xp,"Full inventory start");clear(p);
  p=actor(o,99);PreciousRockMining.start(p,o.id,o.x,o.y,b->b==256?0:b==20?0:b-1);tick(3);clear(p);ok(count(p,21347)==1&&count(p,21341)==1,"Unidentified minerals one in twenty");restore();
  p=actor(o,99);ok(PreciousRockMining.invisibleBoost(p,false)==7,"Guild invisible bonus");equip(p,25539,Player.playerRing);ok(PreciousRockMining.invisibleBoost(p,false)==11,"Guild and ring boost stack");clear(p);
  System.out.println("PRECIOUS_AMETHYST_TEN_VARROCK4_OUTCOMES_LOWER_ARMOURS_FOUR_2022_GLOVE_OUTCOMES_MINERALS_GUILD_RING_STACK_OK");
 }
 static void guards(WorldObject o){
  int required=PreciousRockMining.gem(o.id)?40:92;Player p=actor(o,required-1);ok(!PreciousRockMining.start(p,o.id,o.x,o.y,success(0)),"Level gate");p.playerLevel[14]=99;
  int xp=p.playerXP[14];for(int variant=0;variant<6;variant++){
   PrecousFixture.run(p,o,variant);ok(p.playerXP[14]==xp,"Guard XP "+variant);clear(p);
  }
  ok(!PreciousRockMining.start(p,o.id,o.x+1,o.y,success(0)),"Phantom node");p.absX-=4;ok(!PreciousRockMining.start(p,o.id,o.x,o.y,success(0)),"Remote node");clear(p);
 }
 static void pets(WorldObject o){
  Player p=actor(o,99);p.setLoginName("preciousqa");p.getCollectionLog().loadForPlayer(p);IntUnaryOperator pet=bound->bound==256||bound==128||bound==209411?0:bound-1;
  PreciousRockMining.start(p,o.id,o.x,o.y,pet);tick(3);clear(p);ok(count(p,13321)==1&&count(p,1625)==1,"Native rock golem reward");restore();
  PreciousRockMining.start(p,o.id,o.x,o.y,pet);tick(3);clear(p);ok(count(p,13321)==1&&count(p,1625)==2,"Duplicate pet reward");restore();
  System.out.println("PRECIOUS_PET_BASE_LEVEL_RATE_REWARD_AND_OWNED_PET_DUPLICATE_GUARD_OK");
 }
 static class PrecousFixture {static void run(Player p,WorldObject o,int variant){
  if(!p.getItems().playerHasItem(1275))give(p,1275,1);p.absX=o.x-1;p.absY=o.y;p.heightLevel=o.height;p.isDead=false;p.playerLevel[14]=99;
  PreciousRockMining.start(p,o.id,o.x,o.y,success(0));
  if(variant==0)p.interruptActions();if(variant==1)p.absX++;if(variant==2)p.heightLevel++;if(variant==3)p.isDead=true;if(variant==4)p.getItems().deleteItem2(1275,1);if(variant==5)p.playerLevel[14]=1;
  tick(3);p.isDead=false;p.heightLevel=o.height;
 }}
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;List<WorldObject> nodes=nodes();tables();cycles(nodes);WorldObject gem=nodes.stream().filter(o->o.id==11380).findFirst().orElseThrow(),ame=nodes.stream().filter(o->o.id==11388).findFirst().orElseThrow();gemRolls(gem);amethyst(ame);guards(gem);guards(ame);pets(gem);System.out.println("PRECIOUS_ROCK_REVIEW_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}

