package io.xeros.content.minigames;
import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import io.xeros.model.items.GameItem;
import io.xeros.model.items.ImmutableItem;
import java.util.List;
/** Delivers each prize without silently discarding an item when inventory capacity is insufficient. */
public final class SoloActivityRewards {
 private SoloActivityRewards(){}
 public static void give(Player p,List<GameItem> rewards,Position exit){
  for(GameItem reward:rewards){
   if(ProductionBatch.exchange(p,new int[0][],new int[][]{{reward.getId(),reward.getAmount()}}))continue;
   if(p.getInventory().addToBank(new ImmutableItem(reward.getId(),reward.getAmount()))){p.sendMessage("Recompensa enviada ao banco por falta de espaço.");continue;}
   Server.itemHandler.createPersonalWorldReward(p,reward,exit);
   p.sendMessage("Inventário e banco sem espaço: recolha sua recompensa no chão junto à saída.");
  }
 }
}
