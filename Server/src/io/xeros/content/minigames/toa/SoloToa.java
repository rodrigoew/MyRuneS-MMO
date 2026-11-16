package io.xeros.content.minigames.toa;
import io.xeros.*;
import io.xeros.content.instances.*;
import io.xeros.content.minigames.SoloActivityRewards;
import io.xeros.content.combat.Hitmark;
import io.xeros.content.combat.npc.*;
import io.xeros.model.*;
import io.xeros.model.cycleevent.*;
import io.xeros.model.definitions.*;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.items.GameItem;
import io.xeros.util.Misc;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** A solo boss circuit using the ToA models in the installed 2022 cache. */
public final class SoloToa extends InstancedArea {
 public static final Position EXIT=new Position(3359,9128,0);
 public static final int[] IDS={11719,11730,11778,11789,11756};
 public static final String[] NAMES={"Kephri","Zebak","Ba-Ba","Akkha","Tumeken's Warden"};
 private static final int[] SIZES={5,9,5,3,5},HP={350,500,450,450,550},LEVEL={341,346,350,350,340};
 private static final Position[] BOSSES={new Position(3550,5408),new Position(3928,5275),new Position(3803,5405),new Position(3686,5408,1),new Position(3803,5153,1)};
 private static final Position[] ENTRIES={new Position(3549,5403),new Position(3928,5270),new Position(3804,5400),new Position(3685,5403,1),new Position(3804,5148,1)};
 public static final int[] UNIQUES={27226,27226,27226,27229,27229,27229,27232,27232,27232,26219,26219,26219,26219,25975,25975,25975,25975,25985,25985,25985,27275};
 private final Player player;
 private RaidBoss boss;
 private int stage;
 private boolean finished;
 private IntUnaryOperator lootRoll=b->Misc.random(b-1);
 private SoloToa(Player p){super(InstanceConfiguration.CLOSE_ON_EMPTY,ToaConstants.ALL_BOUNDARIES);player=p;stage=Math.max(0,Math.min(4,p.toaSoloStage));}
 public static boolean enter(Player p){
  if(!Boolean.getBoolean("solo.mode")||!p.getUtilityMagic().canInteract()||p.getInstance()!=null)return false;
  for(int i=0;i<IDS.length;i++)NpcDef.registerIfMissing(IDS[i],NAMES[i],LEVEL[i],SIZES[i]);NpcDef.registerIfMissing(11762,"Tumeken's Warden",340,5);
  SoloToa raid=new SoloToa(p);raid.add(p);raid.spawnStage();p.sendMessage("Tombs of Amascut: circuito solo de bosses. Puzzles e invocations completos não fazem parte desta modalidade.");return true;
 }
 private boolean active(){return !finished&&!isDisposed()&&player.getInstance()==this&&getPlayers().contains(player)&&!player.isDead&&!player.isDisconnected()&&player.getSession()!=null&&player.getHealth().getCurrentHealth()>0;}
 private void spawnStage(){if(!active())return;player.interruptActions();player.attacking.reset();player.underAttackByNpc=0;player.underAttackByPlayer=0;player.moveTo(resolve(ENTRIES[stage]));boss=new RaidBoss(stage);player.sendMessage("Tombs of Amascut: confronto "+(stage+1)+"/5 — "+NAMES[stage]+".");}
 private void defeated(RaidBoss dead){
  if(!active()||dead!=boss||!dead.isDead()||dead.getHealth().getCurrentHealth()>0||dead.credited)return;dead.credited=true;
  player.getNpcDeathTracker().add(NAMES[stage],LEVEL[stage],0);player.toaSoloStage=stage+1;
  if(stage==4){finished=true;player.toaCompletions++;player.toaSoloStage=0;List<GameItem> prizes=new ArrayList<>(List.of(new GameItem(995,250000),new GameItem(565,100)));if(lootRoll.applyAsInt(10)==0)prizes.add(new GameItem(UNIQUES[lootRoll.applyAsInt(UNIQUES.length)]));SoloActivityRewards.give(player,prizes,EXIT);remove(player);player.moveTo(EXIT);player.getToaContainer().displayRewardInterface(prizes);player.sendMessage("Tombs of Amascut concluída. Total: "+player.toaCompletions+".");PlayerSave.saveGame(player);return;}
  stage++;PlayerSave.saveGame(player);
  CycleEventHandler.getSingleton().addEvent(this,new CycleEvent(){public void execute(CycleEventContainer e){if(active())spawnStage();e.stop();}},5);
 }
 @Override public boolean handleDeath(Player p){if(p!=player)return false;p.toaSoloStage=0;remove(p);p.moveTo(EXIT);p.getHealth().reset();p.sendMessage("Sua tentativa em Tombs of Amascut terminou. Seus equipamentos foram preservados.");return true;}
 @Override public void onDispose(){CycleEventHandler.getSingleton().stopEvents(this);}
 public final class RaidBoss extends NPC {
  private final int encounter;private int phase,attacks;private boolean credited;
  RaidBoss(int index){super(IDS[index],SoloToa.this.resolve(BOSSES[index]));encounter=index;SoloToa.this.add(this);setDefaultNpcStats(NPCSpawning.getStats(HP[index],180,100));getBehaviour().setRespawn(false);getBehaviour().setAggressive(true);getBehaviour().setWalkHome(false);walkingType=-1;setAttacks();}
  private void setAttacks(){for(io.xeros.model.entity.npc.stats.NpcCombatSkill skill:io.xeros.model.entity.npc.stats.NpcCombatSkill.values())getCombatDefinition().setLevel(skill,skill.name().equals("DEFENCE")?100:180);CombatType type=encounter==2?CombatType.MELEE:encounter==0?CombatType.RANGE:attacks%2==0?CombatType.MAGE:CombatType.RANGE;setNpcAutoAttacks(List.of(new NPCAutoAttackBuilder().setAnimation(new Animation(encounter==0?9575:encounter==1?9624:encounter==2?9743:encounter==3?(type==CombatType.MAGE?9774:9772):(type==CombatType.MAGE?9661:9660))).setCombatType(type).setIgnoreProjectileClipping(true).setDistanceRequiredForAttack(18).setAttackDelay(5).setHitDelay(1).setMaxHit(24+phase*5).setOnAttack(a->special()).createNPCAutoAttack()));maxHit=24+phase*5;}
  @Override public boolean canBeDamaged(Entity e){return active()&&boss==this&&!isDead()&&e==player&&heightLevel==player.heightLevel;}
  @Override public boolean canBeAttacked(Entity e){return canBeDamaged(e);}
  @Override public void setDead(boolean dead){if(dead&&boss==this&&active()&&((encounter==0&&phase==0)||(encounter==4&&phase<2))){phase++;getHealth().reset();if(encounter==4&&phase==2)requestTransform(11762);setAttacks();player.sendMessage(encounter==0?"A carapaça de Kephri foi quebrada!":"Wardens: fase "+(phase+1)+".");return;}super.setDead(dead);}
  @Override public int getDeathAnimation(){return encounter==0?9582:encounter==1?9634:encounter==4?9662:super.getDeathAnimation();}
  @Override public void onDeath(){defeated(this);}
  @Override public void process(){if(!active()||boss!=this||isUnregister())return;super.process();}
  private void special(){if(!active()||boss!=this)return;attacks++;if(encounter==3&&attacks%3==0)setAttacks();if(attacks%4!=0)return;final Position target=player.getPosition();player.getPA().stillGfx(131,target.getX(),target.getY(),0,0);player.sendMessage("Ataque de área: saia da posição marcada!");CycleEventHandler.getSingleton().addEvent(this,new CycleEvent(){public void execute(CycleEventContainer e){if(active()&&boss==RaidBoss.this&&!isDead()&&player.heightLevel==heightLevel&&player.getPosition().equals(target))player.appendDamage(18+Misc.random(12),Hitmark.HIT);e.stop();}},3);}
 }
}
