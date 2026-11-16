package io.xeros.content.minigames.fight_cave;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.KaramjaDiaryEntry;
import io.xeros.content.event.eventcalendar.EventChallenge;
import io.xeros.content.minigames.SoloActivityRewards;
import io.xeros.model.cycleevent.*;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.items.GameItem;
import io.xeros.util.Misc;
import java.util.*;

public class FightCave {
 public static final int FIRE_CAPE=6570,TOKKUL=6529,JAD_HEALER=3128;
 public static final Position EXIT=new Position(2438,5168,0);
 private static final int[] REWARD_ITEMS={6571,6528,11128,6523,6524,6525,6526,6527,6568,6523,6524,6525,6526,6527,6568};
 private final Player player;
 private final Set<NPC> waveNpcs=Collections.newSetFromMap(new IdentityHashMap<>());
 private final List<NPC> healers=new ArrayList<>();
 private int killsRemaining,generation;
 private boolean active,spawnPending,completed,rewardPending,healersSpawned;
 public FightCave(Player player){this.player=player;}
 private boolean validType(){return player.fightCavesWaveType>=1&&player.fightCavesWaveType<=3;}
 private boolean inArena(){return player.heightLevel==player.getIndex()*4&&(Boundary.FIGHT_CAVE.in(player)||player.getTeleportToX()>0&&Boundary.isIn(new Position(player.getTeleportToX(),player.getTeleportToY(),player.heightLevel),Boundary.FIGHT_CAVE));}
 private boolean alive(){return !player.isDead&&player.getHealth().getCurrentHealth()>0&&!player.isDisconnected()&&player.getSession()!=null;}
 public void create(int type){
  if(type<1||type>3||active||player.getInstance()!=null||!player.getUtilityMagic().canInteract()||player.getIndex()<=0)return;
  cancel(false);generation++;completed=false;active=true;
  player.getPA().removeAllWindows();player.getPA().movePlayer(2413,5117,player.getIndex()*4);
  player.fightCavesWaveType=type;player.waveId=0;player.fightCaveLeaveTimer=System.currentTimeMillis();
  player.sendMessage("Bem-vindo às Fight Caves. A primeira onda começará em instantes.");spawn();
 }
 public void spawn(){
  if(!validType()||!alive()||!inArena()){if(active)cancel(true);return;}
  int[][] waves=Wave.getWaveForType(player);
  if(player.waveId<0||player.waveId>=waves.length){cancel(true);player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);return;}
  if(spawnPending||!waveNpcs.isEmpty())return;
  active=true;completed=false;spawnPending=true;
  final int run=generation,wave=player.waveId;
  CycleEventHandler.getSingleton().addEvent(this,new CycleEvent(){
   public void execute(CycleEventContainer event){
    if(run!=generation||!active||!alive()||!inArena()||player.waveId!=wave){spawnPending=false;event.stop();if(run==generation)cancel(true);return;}
    spawnPending=false;
    for(int id:waves[wave]){NPC n=spawnNpc(id,0,0);if(n==null){event.stop();cancel(true);player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);player.sendMessage("Não foi possível criar a onda. Entre novamente nas Fight Caves.");return;}waveNpcs.add(n);}
    killsRemaining=waveNpcs.size();player.sendMessage("Fight Caves: onda "+(wave+1)+" de "+waves.length+".");event.stop();
    if(waves[wave].length==1&&waves[wave][0]==Wave.TZTOK_JAD)monitorJad(run);
   }
  },16);
 }
 private NPC spawnNpc(int id,int x,int y){
  if(y==0){int[] pos=Wave.SPAWN_DATA[Misc.random(Wave.SPAWN_DATA.length-1)];x=pos[0];y=pos[1];}
  return NPCSpawning.spawnNpcOld(player,id,x,y,player.getIndex()*4,1,Wave.getHp(id),Wave.getMax(id),Wave.getAtk(id),Wave.getDef(id),true,false);
 }
 public void onNpcDeath(NPC npc){
  if(!active||!alive()||!inArena()||npc==null||npc.spawnedBy!=player.getIndex()||npc.heightLevel!=player.heightLevel||!npc.isDead()||!waveNpcs.remove(npc))return;
  if(npc.getNpcId()==Wave.TZ_KEK){for(int i=0;i<2;i++){NPC child=spawnNpc(Wave.TZ_KEK_SPAWN,npc.absX+i,npc.absY);if(child!=null)waveNpcs.add(child);}}
  killsRemaining=waveNpcs.size();
  if(killsRemaining==0){
   player.waveId++;
   if(player.waveId==Wave.getWaveForType(player).length){
    if(npc.getNpcId()!=Wave.TZTOK_JAD){cancel(true);return;}
    completed=true;player.getNpcDeathTracker().add(npc.getName(),npc.getDefinition().getCombatLevel(),0);stop();
   }else{PlayerSave.saveGame(player);spawn();}
  }
 }
 private void monitorJad(int run){
  healersSpawned=false;
  CycleEventHandler.getSingleton().addEvent(this,new CycleEvent(){
   public void execute(CycleEventContainer event){
    NPC jad=waveNpcs.stream().filter(n->n.getNpcId()==Wave.TZTOK_JAD).findFirst().orElse(null);
    if(run!=generation||!active||!alive()||!inArena()||jad==null||jad.isDead()){event.stop();return;}
    if(!healersSpawned&&jad.getHealth().getCurrentHealth()<=jad.getHealth().getMaximumHealth()/2){
     healersSpawned=true;
     for(int i=0;i<4;i++){NPC h=NPCSpawning.spawnNpcOld(player,JAD_HEALER,jad.absX+(i%2==0?-2:2),jad.absY+(i<2?-2:2),player.heightLevel,0,40,14,90,60,false,false);if(h!=null){h.getBehaviour().setAggressive(false);healers.add(h);}}
     player.sendMessage("Os healers chegaram para curar TzTok-Jad. Ataque-os para interromper a cura.");
    }
    if(event.getTotalTicks()%4==0)for(NPC h:healers)if(!h.isDead()&&!h.isUnregister()&&!h.underAttack&&h.getPlayerAttackingIndex()==0)jad.getHealth().increase(5);
   }
  },1);
 }
 public void stop(){
  if(!active||!completed||!validType())return;
  int type=player.fightCavesWaveType;rewardPending=true;reward();player.waveInfo[type-1]++;
  cancel(true);player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);player.nextChat=0;player.setRunEnergy(100,true);
  player.sendMessage("Parabéns! Você concluiu as Fight Caves e recebeu sua recompensa.");PlayerSave.saveGame(player);
 }
 public void reward(){
  if(!rewardPending||!completed||!validType())return;rewardPending=false;
  int type=player.fightCavesWaveType;List<GameItem> prizes=new ArrayList<>();prizes.add(new GameItem(FIRE_CAPE,type==3?2:1));
  if(type==3){prizes.add(new GameItem(REWARD_ITEMS[Misc.random(REWARD_ITEMS.length-1)]));player.getDiaryManager().getKaramjaDiary().progress(KaramjaDiaryEntry.COMPLETE_FIGHT_CAVES);player.getEventCalendar().progress(EventChallenge.COMPLETE_A_63_WAVE_FIGHT_CAVES);}
  prizes.add(new GameItem(TOKKUL,10000*type+Misc.random(5000)));SoloActivityRewards.give(player,prizes,EXIT);Achievements.increase(player,AchievementType.FIGHT_CAVES_ROUNDS,1);
 }
 public void leaveGame(){if(!active&&!validType())return;cancel(true);player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);player.sendMessage("Você saiu das Fight Caves. Para tentar novamente, comece uma nova partida.");PlayerSave.saveGame(player);}
 public void handleDeath(){int wave=player.waveId+1;cancel(true);player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);player.getHealth().reset();player.getHealth().removeAllStatuses();player.sendMessage("Você morreu na onda "+wave+". Seus equipamentos foram preservados.");player.nextChat=0;}
 public void suspend(){cancel(false);}
 public void abandon(){cancel(true);}
 private void cancel(boolean resetProgress){generation++;active=false;spawnPending=false;completed=false;rewardPending=false;CycleEventHandler.getSingleton().stopEvents(this);killAllSpawns();waveNpcs.clear();healers.clear();healersSpawned=false;killsRemaining=0;if(resetProgress){player.waveId=0;player.fightCavesWaveType=0;}}
 public void killAllSpawns(){for(NPC n:NPCHandler.npcs)if(n!=null&&n.spawnedBy==player.getIndex()&&(NPCHandler.isFightCaveNpc(n)||n.getNpcId()==JAD_HEALER)&&Boundary.FIGHT_CAVE.in(n))n.unregister();}
 public int getKillsRemaining(){return killsRemaining;}
 public void setKillsRemaining(int n){killsRemaining=Math.max(0,n);}
	public void gamble() {
		if (!player.getItems().playerHasItem(FIRE_CAPE)) {
			player.sendMessage("You do not have a firecape.");
			return;
		}
		player.getItems().deleteItem(FIRE_CAPE, 1);
		
		if (Misc.random(100) == 67) {
			 if (player.getItems().getItemCount(13225, true) == 0 && player.petSummonId != 13225) {
				 PlayerHandler.executeGlobalMessage("[@red@PET@bla@] @cr20@<col=255> " + player.getDisplayName() + "</col> received a pet from <col=255>TzTok-Jad</col>.");
				 player.getItems().addItemUnderAnyCircumstance(13225, 1);
				 player.getDH().sendDialogues(74, 2180);
			 }
		} else {
			player.getDH().sendDialogues(73, 2180);
		}
	}

}
