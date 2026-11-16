package io.xeros.content.minigames.raids;
import io.xeros.content.instances.*;
import io.xeros.content.minigames.SoloActivityRewards;
import io.xeros.content.item.lootable.impl.RaidsChestItems;
import io.xeros.content.item.lootable.LootRarity;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.cycleevent.*;
import io.xeros.model.items.GameItem;
import io.xeros.util.Misc;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** Native Chambers rooms and boss scripts, with a solo lifecycle. */
public final class SoloCox extends InstancedArea {
 public static final Position EXIT=new Position(1245,3562,0);
 private final Player player;private final Raids raid=new Raids();private boolean finished;
 private IntUnaryOperator lootRoll=n->Misc.random(n-1);
 private SoloCox(Player p){super(InstanceConfiguration.CLOSE_ON_EMPTY,Boundary.FULL_RAIDS);player=p;}
 public Player owner(){return player;}
 public static boolean enter(Player p){if(!Boolean.getBoolean("solo.mode")||!p.getUtilityMagic().canInteract()||p.getInstance()!=null)return false;SoloCox a=new SoloCox(p);a.add(p);a.raid.startSolo(p,a,Math.max(1,p.coxSoloStage));p.sendMessage("Chambers of Xeric solo: salas e bosses da base. Avanço automático; puzzles simplificados.");a.watch();return true;}
 private boolean active(){return !finished&&!isDisposed()&&player.getInstance()==this&&!player.isDead&&!player.isDisconnected()&&player.getHealth().getCurrentHealth()>0;}
 private void watch(){CycleEventHandler.getSingleton().addEvent(this,new CycleEvent(){public void execute(CycleEventContainer e){if(!active()){e.stop();return;}if(raid.olmDead){complete();e.stop();return;}if(raid.remainingSolo()==0&&raid.currentRoom<11){int next=raid.currentRoom+1;player.coxSoloStage=next;PlayerSave.saveGame(player);raid.nextRoom(player);}}},8);}
 private void complete(){if(!active()||!raid.olmDead)return;finished=true;player.coxSoloStage=0;player.raidCount++;List<GameItem> loot=new ArrayList<>();loot.add(new GameItem(995,150000));loot.add(new GameItem(565,100));if(lootRoll.applyAsInt(25)==0){List<GameItem> pool=RaidsChestItems.getItems().get(LootRarity.RARE);GameItem item=pool.get(lootRoll.applyAsInt(pool.size()));loot.add(new GameItem(item.getId(),item.getAmount()));player.getCollectionLog().handleDrop(player,7554,item.getId(),item.getAmount());}SoloActivityRewards.give(player,loot,EXIT);leave();PlayerSave.saveGame(player);player.sendMessage("Chambers concluída! Recompensas entregues e progresso salvo. Total: "+player.raidCount+".");}
 public void leave(){remove(player);player.moveTo(EXIT);}
 @Override public boolean handleDeath(Player p){if(p!=player)return false;p.coxSoloStage=0;leave();p.getHealth().reset();p.sendMessage("Tentativa de Chambers encerrada. Equipamentos preservados.");PlayerSave.saveGame(p);return true;}
 @Override public void onDispose(){CycleEventHandler.getSingleton().stopEvents(this);raid.clearSolo(player);}
}
