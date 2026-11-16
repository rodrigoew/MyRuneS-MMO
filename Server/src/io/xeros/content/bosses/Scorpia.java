package io.xeros.content.bosses;
import io.xeros.model.entity.npc.*;import io.xeros.model.cycleevent.*;import java.util.*;
/** Guardians belong to their Scorpia encounter. Freezing or killing them interrupts healing. */
public final class Scorpia {
 public static int stage;private static final Map<NPC,List<NPC>> encounters=new WeakHashMap<>();
 public static void spawnHealer(){NPC n=NPCHandler.getNpc(6615);if(n!=null)spawnHealer(n);}
 public static void spawnHealer(NPC boss){if(boss.isDead()||boss.getHealth().getCurrentHealth()<=0||boss.getHealth().getCurrentHealth()>boss.getHealth().getMaximumHealth()/2||encounters.containsKey(boss))return;
  List<NPC> guardians=new ArrayList<>();encounters.put(boss,guardians);stage=1;
  for(int dx:new int[]{-3,3}){NPC n=NPCSpawning.spawnNpc(6617,boss.getX()+dx,boss.getY(),boss.getHeight(),0,0);if(n==null)continue;n.getBehaviour().setRespawn(false);n.getBehaviour().setAggressive(false);n.getHealth().setMaximumHealth(70);n.getHealth().reset();if(boss.getInstance()!=null)boss.getInstance().add(n);guardians.add(n);}
  CycleEventHandler.getSingleton().addEvent(boss,new CycleEvent(){int idle;public void execute(CycleEventContainer c){if(boss.isDead()||boss.isUnregister()||boss.getHealth().getCurrentHealth()<=0){stopAll(c);return;}boolean active=false,healed=false;for(NPC n:guardians){if(n.isDead()||n.isUnregister()||n.getHealth().getCurrentHealth()<=0)continue;active=true;if(n.freezeTimer>0)continue;if(!n.getPosition().withinDistance(boss.getPosition(),2))n.moveTowards(boss.getX(),boss.getY(),false);else{boss.getHealth().increase(5);n.gfx0(444);healed=true;}}if(!active||(!healed&&++idle>=7)){stopAll(c);return;}if(healed)idle=0;}private void stopAll(CycleEventContainer c){for(NPC n:guardians)if(!n.isUnregister())n.unregister();c.stop();}},4);
 }
}
