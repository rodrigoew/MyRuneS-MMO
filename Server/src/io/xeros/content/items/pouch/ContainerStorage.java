package io.xeros.content.items.pouch;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.*;
import io.xeros.model.items.bank.BankTransformation;
import java.util.*;
/** Stored IDs retain the legacy inventory encoding (actual ID + 1). */
public final class ContainerStorage {
 private ContainerStorage(){}
 public static boolean bank(Player p,int id,int amount){
  BankTransformation tx=BankTransformation.prepare(p.getBank(),Map.of(),Map.of(id,(long)amount),false);
  if(tx==null)return false;tx.commit();p.getItems().queueBankContainerUpdate();return true;
 }
 public static void withdraw(Player p,List<GameItem> contents){
  p.getStorageCapture().without(()->{
   for(Iterator<GameItem> it=contents.iterator();it.hasNext();){
    GameItem item=it.next();int id=item.getId()-1,amount=p.isBanking?item.getAmount():Math.min(item.getAmount(),p.getItems().freeSlots());
    if(amount<=0)continue;
    boolean success=p.isBanking?bank(p,id,amount):ProductionBatch.exchange(p,new int[0][],new int[][]{{id,amount}});
    if(success){item.setAmount(item.getAmount()-amount);if(item.getAmount()==0)it.remove();}
   }return true;
  });
 }
 public static void check(Player p,List<GameItem> contents,String name,int capacity){
  p.getPA().resetQuestInterface();p.getPA().sendFrame126(name,8144);int frame=8149,total=0;
  for(GameItem item:contents){total+=item.getAmount();p.getPA().sendFrame126(item.getAmount()+" x "+ItemAssistant.getItemName(item.getId()-1),frame++);}
  p.getPA().sendFrame126(total+" / "+capacity,8147);p.getPA().openQuestInterface();
 }
}
