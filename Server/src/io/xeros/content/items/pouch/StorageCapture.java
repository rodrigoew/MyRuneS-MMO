package io.xeros.content.items.pouch;
import io.xeros.model.entity.player.Player;
import java.util.*;
import java.util.function.BooleanSupplier;
/** Reserve first, commit second: automatic storage never changes a failed inventory addition. */
public final class StorageCapture {
 private final Player p;private final boolean[] opened=new boolean[3];private int suppressed;
 public StorageCapture(Player p){this.p=p;}
 private static int index(int bag){return bag==13226?0:bag==12020?1:bag==12019?2:-1;}
 public boolean isOpen(int bag){int n=index(bag);return n>=0&&opened[n];}
 public static boolean isBag(int id){return index(id)>=0;}
 public void destroyed(int bag){
  int n=index(bag);if(n<0||p.getItems().getInventoryCount(bag)>1||p.getItems().bankContains(bag))return;
  opened[n]=false;if(bag==13226)p.getHerbSack().getItems().clear();else if(bag==12020)p.getGemBag().getItems().clear();else p.getMotherlode().clearCoal();
 }
 public void toggle(int bag){int n=index(bag);if(n<0||!p.getItems().playerHasItem(bag)||p.isDead||p.getLock().cannotInteract(p))return;opened[n]=!opened[n];p.sendMessage("Automatic storage is now "+(opened[n]?"open":"closed")+".");}
 public String encode(){return(opened[0]?"1":"0")+","+(opened[1]?"1":"0")+","+(opened[2]?"1":"0");}
 public boolean decode(String value){if(!value.matches("[01],[01],[01]"))return false;for(int i=0;i<3;i++)opened[i]=value.charAt(i*2)=='1';return true;}
 public boolean without(BooleanSupplier operation){suppressed++;try{return operation.getAsBoolean();}finally{suppressed--;}}
 public static int bagFor(int id){return HerbSack.accepts(id)?13226:GemBag.accepts(id)?12020:id==453?12019:-1;}
 public int capacity(int id){return capacity(id,p.playerItems);}
 public int capacity(int id,int[] inventory){
  if(suppressed>0||p.isBanking||p.isShopping||p.isDead||p.getLock().cannotInteract(p))return 0;
  int bag=bagFor(id);if(!isOpen(bag))return 0;boolean present=false;for(int stored:inventory)if(stored==bag+1){present=true;break;}if(!present)return 0;
  return bag==13226?p.getHerbSack().remaining(id):bag==12020?p.getGemBag().remaining(id):Math.max(0,p.getMotherlode().coalCapacity()-p.getMotherlode().coal());
 }
 public int reserve(int id,int amount,int[] inventory,Map<Integer,Integer> reserved){
  int bag=bagFor(id),room=capacity(id,inventory)-reserved.getOrDefault(id,0);
  int totalRoom=bag==13226?420-p.getHerbSack().getItems().stream().mapToInt(i->i.getAmount()).sum():bag==12020?300-p.getGemBag().getItems().stream().mapToInt(i->i.getAmount()).sum():Integer.MAX_VALUE;
  for(Map.Entry<Integer,Integer> entry:reserved.entrySet())if(bagFor(entry.getKey())==bag)totalRoom-=entry.getValue();
  int captured=Math.max(0,Math.min(amount,Math.min(room,totalRoom)));reserved.merge(id,captured,Integer::sum);return captured;
 }
 public void store(int id,int amount){if(amount<=0)return;int bag=bagFor(id);if(bag==13226)p.getHerbSack().storeFresh(id,amount);else if(bag==12020)p.getGemBag().storeFresh(id,amount);else if(bag==12019)p.getMotherlode().storeCoal(amount);else throw new IllegalArgumentException();}
}
