package io.xeros.content.items.pouch;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import java.util.*;
/** Fourteen pre-2023 grimy herbs, thirty each. Legacy clean herbs remain withdrawable. */
public final class HerbSack extends Pouch {
 public static final int HERB_SACK_ID=13226;
 public static final int[] HERBS={199,201,203,205,207,3049,209,211,213,3051,215,2485,217,219};
 public HerbSack(Player player){this.player=player;}
 public static boolean accepts(int id){for(int herb:HERBS)if(herb==id)return true;return false;}
 public boolean levelPermitted(){return player.getPA().getLevelForXP(player.playerXP[15])>=58;}
 private boolean permitted(){return player.getItems().playerHasItem(HERB_SACK_ID)&&configurationPermitted()&&!player.getLock().cannotInteract(player);}
 public int remaining(int id){return accepts(id)&&levelPermitted()?Math.max(0,Math.min(30-countItems(id),420-items.stream().mapToInt(GameItem::getAmount).sum())):0;}
 public void storeFresh(int id,int amount){
  if(amount<=0||amount>remaining(id))throw new IllegalArgumentException("Herb sack capacity");
  for(GameItem item:items)if(item.getId()==id+1){item.setAmount(item.getAmount()+amount);return;}
  items.add(new GameItem(id+1,amount));
 }
 public void fillSack(){for(int id:HERBS)addItemToHerbSack(id,player.getItems().getInventoryCount(id));}
 public void addItemToHerbSack(int id,int requested){
  if(!permitted()||requested<=0||!accepts(id))return;
  if(!levelPermitted()){player.sendMessage("You need base Herblore level 58 to fill the herb sack.");return;}
  int amount=Math.min(requested,Math.min(remaining(id),player.getItems().getInventoryCount(id)));
  if(amount>0&&ProductionBatch.exchange(player,new int[][]{{HERB_SACK_ID,0},{id,amount}},new int[0][])){player.interruptActions();storeFresh(id,amount);}
 }
 public void withdrawAll(){if(permitted()){player.interruptActions();ContainerStorage.withdraw(player,items);}}
 @Override public void withdrawItems(){withdrawAll();}
 public void check(){if(permitted())ContainerStorage.check(player,items,"Herb sack",420);}
}
