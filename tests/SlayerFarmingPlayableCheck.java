package io.xeros.solo;

import io.xeros.*;
import io.xeros.content.skills.slayer.*;
import io.xeros.content.skills.farming.*;
import io.xeros.model.definitions.NpcDef;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.content.dialogue.*;
import java.lang.reflect.*;
import java.util.*;

public final class SlayerFarmingPlayableCheck {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.setIndex(1);p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void give(Player p,int id,int n){ok(p.getItems().addItem(id,n),"Fixture "+id);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static long xp(Player p,int id){return p.playerXP[id]*10L+p.playerXpTenths[id];}
 static void advance(int n)throws Exception{Field f=Server.class.getDeclaredField("tickCount");f.setAccessible(true);for(int i=0;i<n;i++){f.setLong(null,Server.getTickCount()+1);SkillsAuditCheck.tick(1);}}
 static NPC npc(int id,int x,int y,int h){return new NPC(0,id,NpcDef.forId(id)){{absX=x;absY=y;heightLevel=h;}};}
 static Task task(int id){return new Task(new String[]{NpcDef.forId(id).getName().toLowerCase(Locale.ROOT)},1,100,1,3,new String[]{"Local de teste"},new int[]{3200,3200,0});}
 static void assign(Player p,Task t,int master,int amount){p.getSlayer().setTask(Optional.of(t));p.getSlayer().setMaster(master);p.getSlayer().setTaskAmount(amount);}
 static void masters()throws Exception{
  Map<Integer,Integer> levels=Map.of(401,1,402,40,405,99,6797,101,7663,100,603,80,8761,90,5870,90,8605,90,8623,75);
  for(SlayerMaster m:SlayerMaster.MASTERS){ok(m.getLevel()==levels.get(m.getId()),"JSON levelRequired ignored "+m.getId());Player p=actor();p.getSlayer().setCerberusRoute(true);p.getSlayer().createNewTask(m.getId(),false);ok(p.getSlayer().getTask().isPresent()&&p.getSlayer().getTaskAmount()>0,"Master cannot assign "+m.getId());clear(p);
   if(m.getLevel()>1){p=actor();Arrays.fill(p.playerXP,0);Arrays.fill(p.playerLevel,1);p.playerXP[3]=1154;p.playerLevel[3]=10;p.getSlayer().createNewTask(m.getId(),false);ok(!p.getSlayer().getTask().isPresent(),"Low combat master bypass "+m.getId());clear(p);}
  }
  Player p=actor();Task active=task(448);assign(p,active,405,9);p.getSlayer().setConsecutiveTasks(17);Task[] available=SlayerMaster.get(401).orElseThrow().getAvailable(),old=available.clone();try{Arrays.fill(available,active);p.getSlayer().setRemoved(new String[]{active.getPrimaryName()});p.getSlayer().createNewTask(401,false);ok(p.getSlayer().getTask().orElse(null)==active&&p.getSlayer().getConsecutiveTasks()==17&&p.getSlayer().getTaskAmount()==9,"Failed easy reassignment lost streak/task");}finally{System.arraycopy(old,0,available,0,old.length);}clear(p);
  System.out.println("SLAYER_TEN_MASTERS_JSON_COMBAT_REQUIREMENTS_ASSIGNMENT_LOW_LEVEL_REFUSAL_AND_FAILED_EASY_SWITCH_PRESERVES_STREAK_PASS");
 }
 static void kills()throws Exception{
  Player p=actor();NPC n=npc(448,3200,3200,0);assign(p,task(448),405,2);long before=xp(p,18);p.getSlayer().killTaskMonster(n);ok(p.getSlayer().getTaskAmount()==1&&xp(p,18)-before==1000,"Normal kill count/XP");p.getSlayer().setConsecutiveTasks(9);int points=p.getSlayer().getPoints();p.getSlayer().killTaskMonster(n);ok(!p.getSlayer().getTask().isPresent()&&p.getSlayer().getConsecutiveTasks()==10&&p.getSlayer().getPoints()-points==SlayerMaster.get(405).orElseThrow().getPointReward(0)*5,"Finish/streak/points");long end=xp(p,18);p.getSlayer().killTaskMonster(n);ok(xp(p,18)==end,"Completed task rewarded twice");clear(p);
  p=actor();assign(p,task(448),8623,3);p.setKonarSlayerLocation("catacombs");p.absX=1650;p.absY=10050;long start=xp(p,18);n=npc(448,3200,3200,0);ok(KonarSlayer.checkLocation(p)&&!KonarSlayer.checkLocation(p,n),"Konar fixture boundaries");p.getSlayer().killTaskMonster(n);ok(p.getSlayer().getTaskAmount()==3&&xp(p,18)==start,"Konar checked player instead of NPC");p.absX=3200;p.absY=3200;n=npc(448,1650,10050,0);p.getSlayer().killTaskMonster(n);ok(p.getSlayer().getTaskAmount()==2&&xp(p,18)>start,"Konar valid NPC outside player boundary");clear(p);
  p=actor();assign(p,task(448),7663,2);p.getSlayer().killTaskMonster(npc(448,3200,3200,0));ok(p.getSlayer().getTaskAmount()==2,"Wilderness task counted safe NPC");p.getSlayer().killTaskMonster(npc(448,3100,3700,0));ok(p.getSlayer().getTaskAmount()==1,"Wilderness NPC not counted");clear(p);
  for(int i=0;i<Slayer.SUPERIOR_NPCS.length;i++){
   p=actor();int base=Slayer.SUPERIOR_COUNTERPARTS[i];assign(p,task(base),405,3);n=npc(Slayer.SUPERIOR_NPCS[i],3200,3200,0);n.spawnedBy=1;p.getSlayer().superiorSpawned=true;long old=xp(p,18);ok(p.getSlayer().isTaskNpc(n),"Superior base match "+n.getNpcId());p.getSlayer().killTaskMonster(n);ok(p.getSlayer().getTaskAmount()==2&&xp(p,18)-old==10000&&!p.getSlayer().superiorSpawned,"Superior count/local10xXP/clear "+n.getNpcId());clear(p);
  }
  p=actor();assign(p,task(448),405,3);p.getSlayer().superiorSpawned=true;long oldXp=xp(p,18);p.getSlayer().handleSuperiorExp(npc(7390,3200,3200,0));ok(xp(p,18)==oldXp&&p.getSlayer().superiorSpawned,"Unrelated superior bonus");n=npc(7388,3200,3200,0);n.spawnedBy=2;p.getSlayer().handleSuperiorExp(n);ok(xp(p,18)==oldXp,"Other player's superior bonus");clear(p);
  System.out.println("SLAYER_NORMAL_KILL_FINISH_MILESTONE_NO_DUPLICATE_KONAR_NPC_POSITION_WILDERNESS_AND_23_SUPERIOR_PAIRS_PASS");
 }
 static void bossInput()throws Exception{
  Player p=actor();Task original=task(448);assign(p,original,6797,20);SelectTaskLengthDialogue d=new SelectTaskLengthDialogue(p,original);Method factory=SelectTaskLengthDialogue.class.getDeclaredMethod("getAmountInputHandler");factory.setAccessible(true);Object handler=factory.invoke(d);Class<?> input=Class.forName("io.xeros.model.AmountInput");Method accept=input.getDeclaredMethods()[0];assign(p,task(406),6797,20);accept.invoke(handler,p,30);ok(p.getSlayer().getTaskAmount()==20,"Old boss prompt changed new task");Method set=Slayer.class.getDeclaredMethod("setAmountToSlay",int.class);set.setAccessible(true);set.invoke(p.getSlayer(),999);ok(p.getSlayer().getTaskAmount()==20,"Boss internal amount bound");set.invoke(p.getSlayer(),5);ok(p.getSlayer().getTaskAmount()==5,"Valid boss amount denied");clear(p);
  ok(new Task(new String[]{"cave_crawler"},1,1,1,2,new String[]{"test"},new int[]{1,1,0}).matches("Cave Crawler"),"Task case/underscore normalization");
  System.out.println("SLAYER_BOSS_INPUT_STALE_DIALOGUE_AND_AMOUNT_BOUNDS_TASK_NAME_NORMALIZATION_PASS");
 }
 static Plant crop(Player p,Plants data,FarmingPatches patch){p.absX=patch.bottomLeft.getX()-1;p.absY=patch.bottomLeft.getY();p.heightLevel=0;Plant c=new Plant(patch.ordinal(),data.ordinal());c.time=System.currentTimeMillis();p.getFarming().insert(c);return c;}
 static void fruit()throws Exception{
  int tested=0;for(Plants data:Plants.values())if(data.type==SeedType.FRUIT_TREE){Player p=actor();Plant c=crop(p,data,FarmingPatches.CATHERBY_FRUIT_TREE);c.stage=(byte)data.stages;c.checkedHealth=true;c.time=System.currentTimeMillis()-30L*86400000;long before=xp(p,19);c.click(p,1);ok(c.fruitRemaining==5&&count(p,data.harvest)==1&&xp(p,19)>before,"Fruit pick "+data);c.process(p);ok(c.fruitRemaining==5,"Old plant clock instantly regrew fruit "+data);c.time=System.currentTimeMillis()-45*60000L-1000;c.process(p);ok(c.fruitRemaining==6,"Fruit replenishment "+data);c.stumped=true;c.fruitRemaining=0;c.time=System.currentTimeMillis()-86400000;c.process(p);ok(c.fruitRemaining==0,"Stump grew fruit "+data);clear(p);tested++;}
  Player p=actor();Plant c=crop(p,Plants.OAK_TREE,FarmingPatches.LUMBRIDGE_TREE);c.stage=(byte)Plants.OAK_TREE.stages;long before=xp(p,19);p.getFarming().remove(c);ok(!c.checkHealth(p)&&xp(p,19)==before,"Removed tree check health XP");clear(p);
  p=actor();c=crop(p,Plants.OAK_TREE,FarmingPatches.LUMBRIDGE_TREE);c.stage=(byte)Plants.OAK_TREE.stages;c.checkedHealth=true;c.stumped=true;give(p,952,1);c.clearWithSpade(p);p.getFarming().remove(c);advance(4);ok(count(p,6043)==0,"Stale tree minted roots");clear(p);
  System.out.println("FARMING_"+tested+"_FRUIT_TREES_PICK_CLOCK_REGEN_STUMPS_STALE_HEALTH_AND_DELAYED_ROOTS_NO_DUPLICATION_PASS");
 }
 static void farmingGuards()throws Exception{
  for(int mode=0;mode<6;mode++){Player p=actor();Plant c=crop(p,Plants.POTATO,FarmingPatches.CATHERBY_ALLOTMENT_NORTH);give(p,5340,1);give(p,2347,1);long before=xp(p,19);c.water(p,2347);ok(count(p,2347)==1&&!c.isWatered(),"Invalid watering item converted");switch(mode){case 0:p.isBanking=true;break;case 1:p.isShopping=true;break;case 2:p.teleTimer=1;break;case 3:p.setDisconnected(true);break;case 4:p.setMovementState(new PlayerMovementStateBuilder().setLocked(true).createPlayerMovementState());break;case 5:p.isDead=true;break;}c.water(p,5340);ok(!c.isWatered()&&count(p,5340)==1&&xp(p,19)==before,"Water invalid state "+mode);clear(p);}
  Player p=actor();Plant herb=FarmingCoreReviewCheck.plant(p,Plants.GUAM,0);herb.stage=4;herb.harvest(p);p.isBanking=true;advance(4);ok(count(p,199)==0,"Harvest continued in bank");clear(p);
  p=actor();herb=crop(p,Plants.GUAM,FarmingPatches.CATHERBY_HERB);herb.magicCan=true;herb.time=System.currentTimeMillis()-(Plants.GUAM.minutes+1)*60000L;p.isBanking=true;herb.process(p);ok(herb.stage==Plants.GUAM.stages,"Bank blocked passive growth");clear(p);
  p=actor();SaplingGrowth.Recipe recipe=SaplingGrowth.recipes().get(0);give(p,recipe.seed,1);give(p,5354,1);give(p,5325,1);give(p,recipe.dry,1);give(p,5340,1);p.isBanking=true;ok(!p.getFarming().getSaplings().make(recipe,1)&&!p.getFarming().getSaplings().water(recipe,5340,1)&&count(p,recipe.seed)==1&&count(p,recipe.dry)==1,"Bank seed pot/watering");clear(p);
  System.out.println("FARMING_SIX_ACTION_GUARDS_INVALID_WATERING_ITEM_BANK_HARVEST_STOP_PASSIVE_GROWTH_AND_SAPLING_REFUSAL_PASS");
 }
 static void servicesAndSave()throws Exception{
  NPC lep=Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).filter(n->n.getNpcId()==0).findFirst().orElseThrow();Player p=actor();p.absX=lep.absX;p.absY=lep.absY;p.heightLevel=lep.heightLevel;FarmingServices services=p.getFarming().services();for(int id:new int[]{952,5341,5325,5343,5329}){give(p,id,1);ok(services.deposit(lep,id),"Tool deposit "+id);}String tools=services.encode();p.isBanking=true;ok(!services.withdraw(lep,952)&&services.encode().equals(tools),"Bank storage withdrawal");p.isBanking=false;
  Method menu=FarmingServices.class.getDeclaredMethod("menu",NPC.class,int.class);menu.setAccessible(true);for(int page:new int[]{0,1,2,999}){menu.invoke(services,lep,page);Object current=p.getDialogueBuilder().getCurrent();Field f=current.getClass().getDeclaredField("options");f.setAccessible(true);DialogueOption[] options=(DialogueOption[])f.get(current);ok(options.length<=5&&options.length>=2,"Tool pagination option count "+page);if(page>0)ok(Arrays.stream(options).anyMatch(o->o.getTitle().equals("Página anterior")),"Missing Previous page");}
  p.setLoginName("skills54qa");p.setDisplayName("Skills54qa");p.saveCharacter=true;p.isActive=true;Task t=SlayerMaster.get("cave crawler").orElseThrow();assign(p,t,401,12);p.getSlayer().setPoints(432);p.getSlayer().setConsecutiveTasks(19);Plant tree=crop(p,Plants.APPLE_TREE,FarmingPatches.CATHERBY_FRUIT_TREE);tree.stage=(byte)Plants.APPLE_TREE.stages;tree.checkedHealth=true;tree.paidProtection=true;tree.fruitRemaining=4;tree.time=System.currentTimeMillis();PlayerHandler.players[1]=p;ok(PlayerSave.saveGameInstant(p),"Character save");p.getFarming().save();Player q=actor();q.setLoginName("skills54qa");ok(PlayerSave.loadGame(q,"skills54qa","IsolatedAudit",true).toString().equals("SUCCESS"),"Character load");q.getFarming().load();Plant saved=q.getFarming().getPlantedPatch(FarmingPatches.CATHERBY_FRUIT_TREE.bottomLeft.getX(),FarmingPatches.CATHERBY_FRUIT_TREE.bottomLeft.getY());ok(q.getSlayer().getTaskAmount()==12&&q.getSlayer().getMaster()==401&&q.getSlayer().getPoints()==432&&q.getSlayer().getConsecutiveTasks()==19&&q.getFarming().services().encode().equals(tools)&&saved!=null&&saved.checkedHealth&&saved.paidProtection&&saved.fruitRemaining==4&&saved.time==tree.time,"Slayer/tools/fruit-clock persistence");PlayerHandler.players[1]=null;clear(p);clear(q);
  System.out.println("FARMING_TOOL_PAGINATION_PREVIOUS_CLAMP_FIVE_OPTIONS_AND_SLAYER_TASK_POINTS_STREAK_TOOLS_FRUIT_CLOCK_ACTUAL_SAVE_RELOAD_PASS");
 }
 public static void main(String[]args)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());masters();kills();bossInput();System.out.println("SLAYER_PLAYABLE_V54_PASS");fruit();farmingGuards();servicesAndSave();System.out.println("FARMING_PLAYABLE_V54_PASS");System.exit(0);}
}
