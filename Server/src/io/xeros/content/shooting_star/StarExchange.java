package io.xeros.content.shooting_star;
import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.ShopHandler;
import java.util.*;

/** Dusuri's original stardust exchange; no coins or other shop currencies. */
public final class StarExchange {
 public static final int SHOP=221,DUST=25527,FRAGMENT=25547;
 private static final Map<Integer,Integer> PRICES=Map.of(25539,2000,FRAGMENT,3000,25537,300,25533,150);
 private StarExchange(){}
 private static boolean ready(Player p){return !p.isDead&&!p.isDisconnected()&&p.getSession()!=null&&!p.isBanking&&!p.getLock().cannotInteract(p)&&p.getGemBag().configurationPermitted()&&!Server.getMultiplayerSessionListener().inAnySession(p);}
 private static boolean near(Player p){return ready(p)&&p.getInstance()==null&&p.heightLevel==0&&p.distanceToPoint(3022,3342)<=2&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->n.getNpcId()==10631&&!n.isDead()&&n.getHeight()==0&&n.getX()==3022&&n.getY()==3342);}
 public static int price(int id){return PRICES.getOrDefault(id,0);}
 private static int slot(int item){for(int i=0;i<ShopHandler.ShopItems[SHOP].length;i++)if(ShopHandler.ShopItems[SHOP][i]==item+1)return i;return -1;}
 public static void open(Player p){if(near(p)){p.getPA().removeAllWindows();p.getShops().openShop(SHOP);}}
 public static boolean npc(Player p,NPC npc,int option){if(npc==null||npc.getNpcId()!=10631)return false;if(npc.getIndex()<0||npc.getIndex()>=NPCHandler.npcs.length||NPCHandler.npcs[npc.getIndex()]!=npc||!near(p))return true;
  if(option==2){open(p);return true;}if(option!=1)return false;
  new DialogueBuilder(p).option("Dusuri — Shooting Stars",new DialogueOption("Trade stardust",StarExchange::open),new DialogueOption("My discoveries and stardust",q->{q.getPA().removeAllWindows();q.sendMessage("Stars discovered: "+q.getStarPlayerState().discoveries()+". Stardust mined: "+q.getStarPlayerState().dust()+".");}),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
 }
 private static void refresh(Player p){p.getItems().sendInventoryInterface(3823);p.getShops().resetShop(SHOP);p.getShops().updatePlayerShop();}
 public static boolean buy(Player p,int item,int fromSlot,int requested){
  if(!near(p)||p.myShopId!=SHOP||!p.isShopping||requested<=0||price(item)==0||fromSlot<0||fromSlot>=ShopHandler.ShopItems[SHOP].length||fromSlot!=slot(item))return false;
  int amount=Math.min(requested,ShopHandler.ShopItemsN[SHOP][fromSlot]),bought=0;
  while(bought<amount&&ProductionBatch.exchange(p,new int[][]{{DUST,price(item)}},new int[][]{{item,1}})){ShopHandler.ShopItemsN[SHOP][fromSlot]--;bought++;}
  if(bought==0)p.sendMessage("You need enough stardust, stock and inventory space.");refresh(p);return bought>0;
 }
 public static boolean sell(Player p,int item,int fromSlot,int requested){int target=slot(item),cost=price(item)*4/5;
  if(!near(p)||p.myShopId!=SHOP||!p.isShopping||cost<=0||target<0||requested<=0||fromSlot<0||fromSlot>=p.playerItems.length||p.playerItems[fromSlot]!=item+1)return false;
  int amount=Math.min(requested,Math.min(p.getItems().getInventoryCount(item),Math.min(Integer.MAX_VALUE/cost,Integer.MAX_VALUE-ShopHandler.ShopItemsN[SHOP][target])));
  if(amount<=0||!ProductionBatch.exchange(p,new int[][]{{item,amount}},new int[][]{{DUST,cost*amount}}))return false;ShopHandler.ShopItemsN[SHOP][target]+=amount;refresh(p);return true;
 }
 public static boolean dye(Player p,int first,int second){if(first!=FRAGMENT&&second!=FRAGMENT)return false;int piece=first==FRAGMENT?second:first;
  int[] normal={12013,12014,12015,12016},gold={25549,25551,25553,25555};
  for(int i=0;i<normal.length;i++)if(piece==normal[i]){if(ready(p)&&!p.isShopping&&ProductionBatch.exchange(p,new int[][]{{FRAGMENT,1},{piece,1}},new int[][]{{gold[i],1}}))p.sendMessage("The star fragment transforms your prospector equipment into gold.");return true;}return false;
 }
}
