package io.xeros.content.items.pouch;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
/** The pre-2023 bag holds sixty of each of five precious gems. Legacy contents remain withdrawable. */
public class GemBag extends Pouch {
    public static final int GEM_BAG_ID=12020;
    private static final Set<Integer> ALLOWED=Set.of(1623,1621,1619,1617,1631);
    public GemBag(Player player){this.player=player;}
    public static boolean accepts(int id){return ALLOWED.contains(id);}
    public int remaining(int id){return accepts(id)?Math.max(0,Math.min(60-countItems(id),300-items.stream().mapToInt(GameItem::getAmount).sum())):0;}
    public void storeFresh(int id,int amount){
        if(amount<=0||amount>remaining(id))throw new IllegalArgumentException("Gem bag capacity");
        for(GameItem item:items)if(item.getId()==id+1){item.setAmount(item.getAmount()+amount);return;}
        items.add(new GameItem(id+1,amount));
    }
    private boolean permitted(){return player.getItems().playerHasItem(GEM_BAG_ID)&&configurationPermitted()&&!player.getLock().cannotInteract(player);}
    public void fillBag(){for(int id:new int[]{1623,1621,1619,1617,1631})addItemToGemBag(id,player.getItems().getInventoryCount(id));}
    public void addItemToGemBag(int id,int requested){
        if(!permitted()||requested<=0||!ALLOWED.contains(id))return;
        int amount=Math.min(requested,Math.min(player.getItems().getInventoryCount(id),remaining(id)));
        if(amount<=0)return;
        if(!ProductionBatch.exchange(player,new int[][]{{GEM_BAG_ID,0},{id,amount}},new int[0][]))return;
        player.interruptActions();
        for(GameItem item:items)if(item.getId()==id+1){item.setAmount(item.getAmount()+amount);return;}
        // Existing saves use inventory encoding (actual ID + 1).
        items.add(new GameItem(id+1,amount));
    }
    public void withdrawAll(){if(permitted()){player.interruptActions();ContainerStorage.withdraw(player,items);}}
    @Override public void withdrawItems(){withdrawAll();}
    public void check(){
        if(!permitted())return;player.getPA().resetQuestInterface();player.getPA().sendFrame126("Gem bag",8144);
        int frame=8149,total=0;for(GameItem item:items){total+=item.getAmount();player.getPA().sendFrame126(item.getAmount()+" x "+ItemAssistant.getItemName(item.getId()-1),frame++);}
        player.getPA().sendFrame126(total+" gems; up to 60 of each supported type",8147);player.getPA().openQuestInterface();
    }
}
