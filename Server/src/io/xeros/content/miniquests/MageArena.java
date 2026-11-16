package io.xeros.content.miniquests;
import io.xeros.content.instances.*;
import io.xeros.content.instances.impl.LegacySoloPlayerInstance;
import io.xeros.content.minigames.SoloActivityRewards;
import io.xeros.content.combat.npc.NPCAutoAttackBuilder;
import io.xeros.content.dialogue.*;
import io.xeros.model.*;
import io.xeros.model.cycleevent.*;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.items.GameItem;
import java.util.*;
public class MageArena {
 public static final Position EXIT=new Position(2538,4716,0);
 private final Player player;private Arena arena;private KolodionForm opponent;
 public MageArena(Player player){this.player=player;}
 public void start(){
  if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null)return;
  if(player.mageArenaCompleted){player.moveTo(EXIT);offerRewards();return;}
  if(player.getPA().getLevelForXP(player.playerXP[6])<60){player.sendMessage("Você precisa de nível 60 de Magic para desafiar Kolodion.");return;}
  player.getPA().removeAllWindows();arena=new Arena();arena.add(player);player.mageArenaStage=Math.max(0,Math.min(4,player.mageArenaStage));player.moveTo(new Position(3105,3934,arena.getHeight()));spawnForm();player.sendMessage("Mage Arena: vença as cinco formas de Kolodion usando Magic.");
 }
 private boolean active(){return arena!=null&&!arena.isDisposed()&&player.getInstance()==arena&&arena.getPlayers().contains(player)&&!player.isDead&&!player.isDisconnected()&&player.getSession()!=null&&player.getHealth().getCurrentHealth()>0;}
 private void spawnForm(){
  if(!active())return;
  boolean resume=opponent!=null&&player.autoRet==1&&player.isWalkingQueueEmpty()&&player.playerAttackingIndex==0
    &&(player.npcAttackingIndex==0||player.npcAttackingIndex==opponent.getIndex())
    &&(player.autocasting||io.xeros.content.combat.magic.SoloEquipmentCharges.poweredSpell(player.playerEquipment[3])>=0);
  player.attacking.reset();player.underAttackByNpc=0;opponent=new KolodionForm(player.mageArenaStage);
  if(resume&&io.xeros.content.combat.magic.SoloEquipmentCharges.ready(player)){
   io.xeros.content.combat.magic.SoloEquipmentCharges.configure(player);
   player.attackEntity(opponent);
  }
 }
 private void defeated(KolodionForm form){
  if(!active()||form!=opponent||form.credited||!form.isDead()||form.getHealth().getCurrentHealth()>0)return;form.credited=true;player.mageArenaStage++;
  if(player.mageArenaStage==5){player.mageArenaCompleted=true;player.mageArenaStage=0;arena.remove(player);player.moveTo(EXIT);player.sendMessage("Você venceu Kolodion e concluiu Mage Arena. Escolha uma god cape e seu primeiro god staff.");PlayerSave.saveGame(player);offerRewards();return;}
  PlayerSave.saveGame(player);CycleEventHandler.getSingleton().addEvent(arena,new CycleEvent(){public void execute(CycleEventContainer e){if(active())spawnForm();e.stop();}},5);
 }
 public void offerRewards(){if(!player.mageArenaCompleted)return;player.start(new DialogueBuilder(player).option(new DialogueOption("Saradomin",p->claimGodGear(0)),new DialogueOption("Guthix",p->claimGodGear(1)),new DialogueOption("Zamorak",p->claimGodGear(2))));}
 public boolean claimGodGear(int god){
  if(!player.mageArenaCompleted){player.sendMessage("Conclua Mage Arena antes de escolher uma god cape.");return false;}
  if(god<0||god>2||!player.getUtilityMagic().canInteract())return false;
  boolean nearby=player.absX>=2490&&player.absX<=2555&&player.absY>=4680&&player.absY<=4745;
  if(!nearby)return false;
  List<GameItem> prizes=new ArrayList<>();prizes.add(new GameItem(2412+god));if(!player.mageArenaStaffClaimed){prizes.add(new GameItem(2415+god));player.mageArenaStaffClaimed=true;}
  SoloActivityRewards.give(player,prizes,EXIT);player.getPA().closeAllWindows();PlayerSave.saveGame(player);return true;
 }
 public final class Arena extends LegacySoloPlayerInstance {
  Arena(){super(InstanceConfiguration.CLOSE_ON_EMPTY,MageArena.this.player,new Boundary(3090,3920,3125,3955));}
  @Override public void onDispose(){CycleEventHandler.getSingleton().stopEvents(this);}
  @Override public boolean handleDeath(Player p){p.mageArenaStage=0;remove(p);p.moveTo(EXIT);p.getHealth().reset();p.sendMessage("Kolodion venceu esta tentativa. Seus equipamentos foram preservados.");return true;}
 }
 public final class KolodionForm extends NPC {
  private boolean credited;
  KolodionForm(int stage){super(1605+stage,new Position(3106,3934,arena.getHeight()));arena.add(this);setDefaultNpcStats(NPCSpawning.getStats(20+stage*20,80,60));getBehaviour().setRespawn(false);getBehaviour().setAggressive(true);for(io.xeros.model.entity.npc.stats.NpcCombatSkill skill:io.xeros.model.entity.npc.stats.NpcCombatSkill.values())getCombatDefinition().setLevel(skill,80);setNpcAutoAttacks(List.of(new NPCAutoAttackBuilder().setAnimation(new Animation(io.xeros.model.entity.npc.data.AttackAnimation.handleEmote(this))).setCombatType(CombatType.MAGE).setMaxHit(12+stage*2).setDistanceRequiredForAttack(15).setHitDelay(1).setAttackDelay(5).createNPCAutoAttack()));}
  @Override public boolean canBeAttacked(Entity e){return active()&&opponent==this&&!isDead()&&e==player&&player.heightLevel==heightLevel;}
  @Override public boolean canBeDamaged(Entity e){return canBeAttacked(e)&&(player.usingMagic||player.attacking.getCombatType()==CombatType.MAGE);}
  @Override public void onDeath(){defeated(this);}
  @Override public void process(){if(active()&&!isUnregister())super.process();}
 }
}
