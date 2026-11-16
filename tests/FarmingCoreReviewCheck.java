package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.farming.*;
import io.xeros.model.entity.player.Player;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.IntSupplier;

public class FarmingCoreReviewCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.setLoginName("farmcoreqa");return p;}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static GrassyPatch soil(Player p,FarmingPatches patch)throws Exception{Field f=Farming.class.getDeclaredField("patches");f.setAccessible(true);return ((GrassyPatch[])f.get(p.getFarming()))[patch.ordinal()];}
 static void near(Player p,FarmingPatches patch){p.absX=patch.bottomLeft.getX()-1;p.absY=patch.bottomLeft.getY();p.heightLevel=0;}
 static FarmingPatches patch(Plants crop){return crop.type==SeedType.HERB?FarmingPatches.CATHERBY_HERB:crop.type==SeedType.FLOWER?FarmingPatches.CATHERBY_FLOWER:FarmingPatches.CATHERBY_ALLOTMENT_NORTH;}
 static Plant plant(Player p,Plants crop,int tier)throws Exception{
  FarmingPatches patch=patch(crop);near(p,patch);soil(p,patch).stage=3;
  give(p,5343,1);give(p,952,1);give(p,crop.seed,crop.type==SeedType.ALLOTMENT?3:1);
  if(tier>0){give(p,tier==1?6032:tier==2?6034:21483,1);ok(p.getFarming().applyCompost(tier==1?6032:tier==2?6034:21483,patch.bottomLeft.getX(),patch.bottomLeft.getY()),"Compost dispatch");}
  p.getFarming().plant(crop.seed,patch.bottomLeft.getX(),patch.bottomLeft.getY());
  Plant plant=p.getFarming().getPlantedPatch(patch.bottomLeft.getX(),patch.bottomLeft.getY());ok(plant!=null,"Plant "+crop);
  Field f=Plant.class.getDeclaredField("harvestRoll");f.setAccessible(true);f.set(plant,(IntSupplier)()->255);
  return plant;
 }
 static void xp(Player p,int before,int tenths,double... actions){Player reference=actor();int initial=reference.playerXP[19];reference.playerXpTenths[19]=tenths;for(double amount:actions)reference.getPA().addSkillXPMultiplied(amount,19,false);ok(p.playerXP[19]-before==reference.playerXP[19]-initial&&p.playerXpTenths[19]==reference.playerXpTenths[19],"Exact Farming XP "+Arrays.toString(actions));}
 static void tables(){
  double[][] herbs={{9,11,12.5},{14,13.5,15},{19,16,18},{26,21.5,24},{32,26.5,30.5},{38,34,38.5},{43,43,48.5},{50,54.5,61.5},{56,69,78},{62,87.5,98.5},{67,106.5,120},{73,134.5,151.5},{79,170.5,192},{85,199.5,224.5}};
  for(int i=0;i<14;i++){Plants crop=Plants.getPlantForSeed(5291+i);ok(crop.level==herbs[i][0]&&crop.plantExperience==herbs[i][1]&&crop.harvestExperience==herbs[i][2]&&crop.minutes==80&&crop.stages==4,"2022 herb reference "+crop);}
  ok(Plants.SWEETCORN.minutes==60&&Plants.SWEETCORN.stages==6&&Plants.STRAWBERRY.minutes==60&&Plants.WATERMELON.minutes==80,"Allotment growth reference");
  for(Plants crop:Plants.values())if(crop.type==SeedType.FLOWER)ok(crop.minutes==20&&crop.stages==4,"Flower growth "+crop);
  int[] low={25,28,31,36,39,43,46,50,54,57,60,64,67,71};
  for(int i=0;i<14;i++)for(int level=1;level<=99;level++)for(int bonus:new int[]{0,5,10,15}){
   int expected=((int)Math.floor(low[i]+(80-low[i])*(level-1)/98.0))*(100+bonus)/100+1;
   ok(FarmingYield.saveThreshold(Plants.getPlantForSeed(5291+i),level,bonus)==expected,"Harvest-life interpolation");
  }
  for(int level=1;level<=110;level++)for(int roll=0;roll<level;roll++)ok(FarmingYield.flowerAmount(Plants.LIMPWURT,level,roll)==3+roll/10,"Limpwurt level yield");
  System.out.println("FARMING_REFERENCE_26_CROPS_5544_HERB_THRESHOLDS_AND_LIMPWURT_YIELDS_OK");
 }
 static void cycles()throws Exception{
  int tested=0;
  for(Plants crop:Plants.values())if(crop.type==SeedType.HERB||crop.type==SeedType.ALLOTMENT||crop.type==SeedType.FLOWER){
   for(int tier=0;tier<=3;tier++){
    Player p=actor();int before=p.playerXP[19];Plant plant=plant(p,crop,tier);
    if(crop.type==SeedType.HERB)xp(p,before,0,tier==0?0:tier==1?18:tier==2?26:36);
    ok(plant.compost==tier&&plant.harvestLives==3+tier,"Compost lives "+crop);
    plant.magicCan=true;plant.time=System.currentTimeMillis()-(crop.minutes+1)*60000L;plant.process(p);
    ok(plant.stage==crop.stages&&!plant.dead,"Offline maturation "+crop);
    int old=p.playerXP[19],fraction=p.playerXpTenths[19];plant.harvest(p);tick(2);ok(count(p,crop.harvest)==0,"Early harvest "+crop);tick(1);
    if(crop.type==SeedType.FLOWER){
     int amount=count(p,crop.harvest);ok(crop==Plants.LIMPWURT?amount>=3&&amount<=12:amount==(crop==Plants.WOAD?3:1),"Flower output "+crop);
     xp(p,old,fraction,crop.harvestExperience);ok(!p.getFarming().contains(plant),"Flower patch not cleared");
    }else{
     ok(count(p,crop.harvest)==1&&plant.harvestLives==2+tier,"First harvest/life "+crop);
     xp(p,old,fraction,crop.harvestExperience+(crop.type==SeedType.HERB?crop.plantExperience:0));
     tick((2+tier)*3);ok(count(p,crop.harvest)==3+tier&&!p.getFarming().contains(plant),"Minimum yield/depleted patch "+crop);
    }
    ok(soil(p,patch(crop)).compost==0,"Compost leaked into next crop");clear(p);tested++;
   }
  }
  ok(tested==112,"All 28 ordinary crops must be covered");
  System.out.println("FARMING_112_COMPLETE_CROP_COMPOST_OFFLINE_GROWTH_HARVEST_XP_CLEAR_CYCLES_OK count="+tested);
 }
 static void safety()throws Exception{
  Player p=actor();Plant plant=plant(p,Plants.GUAM,0);plant.stage=4;int old=p.playerXP[19];
  plant.harvest(p);p.heightLevel=1;tick(6);ok(count(p,199)==0&&p.playerXP[19]==old,"Wrong floor harvest");p.heightLevel=0;
  plant.harvest(p);p.absX++;tick(6);ok(count(p,199)==0,"Moved harvest");near(p,patch(Plants.GUAM));
  plant.harvest(p);p.interruptActions();tick(6);ok(count(p,199)==0,"Cancelled harvest");
  plant.harvest(p);p.getItems().deleteItem2(952,1);tick(6);ok(count(p,199)==0,"Missing spade mid-harvest");give(p,952,1);
  while(p.getItems().freeSlots()>0)give(p,1931,1);
  plant.harvest(p);tick(3);ok(plant.harvestLives==3&&count(p,199)==0&&p.playerXP[19]==old&&!plant.herbPlantXpAwarded,"Full bag lost crop or XP");
  p.getItems().deleteItem2(1931,1);plant.harvest(p);tick(3);ok(count(p,199)==1&&plant.herbPlantXpAwarded,"Resume after full bag");clear(p);
  p=actor();Plant immature=plant(p,Plants.POTATO,0);give(p,5340,1);immature.water(p,5340);ok(count(p,5340)==0&&count(p,5339)==1,"Watering dose");
  int water=count(p,5339);immature.water(p,5339);ok(count(p,5339)==water,"Duplicate watering consumed dose");
  p.absX=3000;p.absY=3200;ok(!p.getFarming().plant(5318,2805,3465),"Remote planting");
  System.out.println("FARMING_HARVEST_ORIGIN_FLOOR_TOOL_CANCEL_FULL_BAG_RESUME_AND_WATERING_OK");
 }
 static void saves()throws Exception{
  Player p=actor();Plant plant=plant(p,Plants.GUAM,3);plant.stage=4;plant.harvestLives=2;p.getFarming().save();
  Player q=actor();near(q,patch(Plants.GUAM));q.getFarming().load();Plant saved=q.getFarming().getPlantedPatch(2813,3462);
  ok(saved!=null&&saved.compost==3&&saved.harvestLives==2&&!saved.herbPlantXpAwarded,"New crop state save");
  give(q,952,1);saved.harvest(q);tick(3);clear(q);ok(saved.herbPlantXpAwarded,"Planting XP not paid");q.getFarming().save();
  Player reloaded=actor();reloaded.getFarming().load();Plant again=reloaded.getFarming().getPlantedPatch(2813,3462);ok(again!=null&&again.herbPlantXpAwarded&&again.harvested==1,"Repeated planting XP after reload");
  Path file=Paths.get(Server.getSaveDirectory(),"farming","farmcoreqa.txt");
  Files.writeString(file,"[PLANT]\npatch: 2\nplant: "+Plants.GUAM.ordinal()+"\nstage: 4\nharvested: 8\ntime: 1\nEND PLANT\n[PLANT]\npatch: 999\nplant: 999\nEND PLANT\n[PATCH]\npatch: 0\nstage: 3\nEND PATCH\n");
  Player legacy=actor();legacy.getFarming().load();Plant migrated=legacy.getFarming().getPlantedPatch(2813,3462);
  ok(migrated!=null&&migrated.herbPlantXpAwarded&&migrated.compost==0&&migrated.harvestLives==3&&soil(legacy,FarmingPatches.CATHERBY_ALLOTMENT_NORTH).stage==3,"Legacy migration/bad-block recovery");
  p=actor();near(p,patch(Plants.GUAM));give(p,6034,2);int old=p.playerXP[19];p.getFarming().applyCompost(6034,2813,3462);int gained=p.playerXP[19];p.getFarming().applyCompost(6034,2813,3462);ok(count(p,6034)==1&&p.playerXP[19]==gained,"Duplicate compost XP/consumption");p.getFarming().save();q=actor();q.getFarming().load();ok(soil(q,patch(Plants.GUAM)).compost==2,"Empty-soil compost persistence");
  System.out.println("FARMING_LIVES_COMPOST_DEFERRED_XP_LEGACY_SAVE_BAD_BLOCK_AND_REPEAT_PROTECTION_OK");
 }
 static void rake()throws Exception{
  Player p=actor();near(p,FarmingPatches.CATHERBY_HERB);give(p,5341,1);int old=p.playerXP[19];GrassyPatch soil=soil(p,FarmingPatches.CATHERBY_HERB);
  soil.rake(p,2);tick(9);ok(soil.stage==3&&count(p,6055)==3,"Rake three weeds");xp(p,old,0,4,4,4);clear(p);
  p=actor();near(p,FarmingPatches.CATHERBY_HERB);give(p,5341,1);soil=soil(p,FarmingPatches.CATHERBY_HERB);soil.rake(p,2);p.absX++;tick(9);ok(soil.stage==0&&count(p,6055)==0,"Remote rake tick");clear(p);
  System.out.println("FARMING_RAKE_THREE_TIMED_WEEDS_EXACT_XP_AND_MOVEMENT_OK");
 }
 static void ultra(){
  Player p=actor();give(p,6034,2);give(p,21622,4);int old=p.playerXP[19];
  io.xeros.content.items.UseItem.ItemonItem(p,21622,6034,2,0);tick(4);
  ok(count(p,21483)==2&&count(p,21622)==0&&count(p,6034)==0&&p.playerXP[19]==old,"2022 ultracompost recipe/XP");clear(p);
  p=actor();give(p,6034,1);give(p,21622,1);p.getFarming().itemOnItem(6034,21622);tick(4);ok(count(p,6034)==1&&count(p,21622)==1,"Single ash consumed");clear(p);
  System.out.println("FARMING_ULTRACOMPOST_TWO_ASHES_CONVERSION_NO_2024_XP_OR_MISSING_MATERIAL_LOSS_OK");
 }
 static void morphs(){
  for(Plants crop:Plants.values())if(crop.type==SeedType.HERB||crop.type==SeedType.FLOWER||crop.type==SeedType.ALLOTMENT){
   Plant plant=new Plant(patch(crop).ordinal(),crop.ordinal());int object=crop.type==SeedType.HERB?8150:crop.type==SeedType.FLOWER?7847:8550;
   plant.stage=crop.stages;System.out.println("FARMING_MORPH "+crop+"|"+object+"|"+plant.getConfig()+"|harvest");
   for(int stage=1;stage<crop.stages;stage++){
    plant.stage=(byte)stage;plant.disease=(byte)stage;plant.dead=false;System.out.println("FARMING_MORPH "+crop+"|"+object+"|"+plant.getConfig()+"|cure");
    plant.dead=true;System.out.println("FARMING_MORPH "+crop+"|"+object+"|"+plant.getConfig()+"|clear");
   }
  }
 }
 static void flowers()throws Exception{
  Plants[] flowers={Plants.MARIGOLD,Plants.ROSEMARY,Plants.NASTURTIUM,Plants.WHITE_LILY,Plants.WOAD,Plants.LIMPWURT};
  Plants[] crops={Plants.POTATO,Plants.ONION,Plants.TOMATO,Plants.CABBAGE,Plants.SWEETCORN,Plants.WATERMELON,Plants.SNAPE_GRASS};
  int tests=0;
  for(Plants flower:flowers)for(Plants crop:crops){
   Player p=actor();Plant target=new Plant(FarmingPatches.CATHERBY_ALLOTMENT_NORTH.ordinal(),crop.ordinal());p.getFarming().insert(target);
   Plant protection=new Plant(FarmingPatches.CATHERBY_FLOWER.ordinal(),flower.ordinal());protection.stage=4;protection.time=1000;p.getFarming().insert(protection);
   boolean expected=flower==Plants.WHITE_LILY||flower==Plants.MARIGOLD&&(crop==Plants.POTATO||crop==Plants.ONION||crop==Plants.TOMATO)||flower==Plants.ROSEMARY&&crop==Plants.CABBAGE||flower==Plants.NASTURTIUM&&crop==Plants.WATERMELON;
   ok(p.getFarming().flowerProtects(target,2000)==expected,"Flower protection "+flower+" "+crop);
   ok(!p.getFarming().flowerProtects(target,999),"Flower protected earlier growth");
   protection.dead=true;ok(!p.getFarming().flowerProtects(target,2000),"Dead flower protection");protection.dead=false;
   protection.stage=3;ok(!p.getFarming().flowerProtects(target,2000),"Immature flower protection");protection.stage=4;
   protection.patch=FarmingPatches.FALADOR_FLOWER.ordinal();ok(!p.getFarming().flowerProtects(target,2000),"Flower in another area");clear(p);tests++;
  }
  Player p=actor();Plant flower=new Plant(FarmingPatches.CATHERBY_FLOWER.ordinal(),Plants.WHITE_LILY.ordinal());flower.magicCan=true;long planted=System.currentTimeMillis()-4000000L;flower.time=planted;p.getFarming().insert(flower);p.getFarming().sequence();ok(flower.time==planted+1200000L&&flower.stage==4,"Offline flower maturation timestamp");
  ok(Plants.WHITE_LILY.seed==22887&&Plants.WHITE_LILY.harvest==22932&&Plants.WHITE_LILY.level==58&&Plants.WHITE_LILY.plantExperience==42&&Plants.WHITE_LILY.harvestExperience==250,"White lily reference");
  ok(Plants.SNAPE_GRASS.seed==22879&&Plants.SNAPE_GRASS.level==61&&Plants.SNAPE_GRASS.plantExperience==82&&Plants.SNAPE_GRASS.harvestExperience==82&&Plants.SNAPE_GRASS.minutes==70,"Snape grass reference");
  System.out.println("FARMING_42_FLOWER_CROP_PROTECTION_PAIRS_MATURITY_AREA_DEATH_AND_OFFLINE_TIMESTAMPS_OK count="+tests);
 }
 static void disease()throws Exception{
  Plants[] crops={Plants.GUAM,Plants.APPLE_TREE,Plants.MAPLE_TREE,Plants.MAGIC_TREE};
  int[][] expected={{27,14,6,3},{18,9,4,2},{13,7,3,2},{9,5,2,1}};
  for(int i=0;i<4;i++)for(int tier=0;tier<4;tier++)ok(FarmingDisease.threshold(crops[i],tier,false)==expected[i][tier],"Published disease threshold "+crops[i]);
  for(Plants crop:crops){ok(!FarmingDisease.susceptible(crop,0)&&!FarmingDisease.susceptible(crop,crop.stages),"First/mature crop disease");int vulnerable=0;for(int stage=0;stage<=crop.stages;stage++)if(FarmingDisease.susceptible(crop,stage))vulnerable++;ok(vulnerable==(crop.type==SeedType.HERB?3:crop.type==SeedType.FRUIT_TREE?4:crop==Plants.MAPLE_TREE?7:11),"Disease windows "+crop);}
  Player p=actor();Plant plant=plant(p,Plants.GUAM,0);Field roll=Plant.class.getDeclaredField("diseaseRoll");roll.setAccessible(true);roll.set(plant,(IntSupplier)()->0);
  plant.time=System.currentTimeMillis()-21*60000L;plant.process(p);ok(plant.stage==1&&!plant.isDiseased(),"First stage disease");
  plant.time=System.currentTimeMillis()-21*60000L;plant.process(p);ok(plant.stage==1&&plant.isDiseased()&&!plant.dead,"Infection/growth");
  plant.time=System.currentTimeMillis()-21*60000L;plant.process(p);ok(!plant.dead,"Disease used healthy 20-minute death interval");
  plant.time=System.currentTimeMillis()-41*60000L;plant.process(p);ok(plant.dead,"Disease did not use next slower interval");clear(p);
  p=actor();plant=plant(p,Plants.POTATO,0);plant.stage=1;plant.watered=-1;roll.set(plant,(IntSupplier)()->0);plant.time=System.currentTimeMillis()-11*60000L;plant.process(p);ok(plant.isDiseased()&&!plant.isWatered(),"Watering incorrectly grants immunity");clear(p);
  p=actor();GrassyPatch soil=soil(p,FarmingPatches.CATHERBY_HERB);soil.stage=3;Field time=GrassyPatch.class.getDeclaredField("time");time.setAccessible(true);time.set(soil,System.currentTimeMillis()-4*60000L);soil.process(p,2);ok(soil.stage==3,"Weeds before five minutes");time.set(soil,System.currentTimeMillis()-6*60000L);soil.process(p,2);ok(soil.stage==2,"Weeds at five minutes");clear(p);
  System.out.println("FARMING_16_PUBLISHED_DISEASE_ROLLS_WINDOWS_SLOWER_DEATH_WATER_NOT_IMMUNITY_AND_FIVE_MINUTE_WEEDS_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;tables();cycles();safety();saves();rake();ultra();flowers();disease();morphs();System.out.println("FARMING_CORE_REVIEW_ALL_CHECKS_OK");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
