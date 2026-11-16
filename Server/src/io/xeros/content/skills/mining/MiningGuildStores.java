package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import io.xeros.model.world.ShopHandler;
import java.util.*;

/** Guild ore and pickaxe stores with the historical 2022 stock and coin pricing.
 * Stock-price arithmetic and world-phase restocking reference OpenRune (ISC).
 * Integer thousandths avoid floating-point errors at price boundaries. */
public final class MiningGuildStores {
 public static final int HENDOR=222,YARSUL=223,ALECK=23,ARTIMEUS=230,ASYFF=231;
 public static final class Row {
  public final int item,value,initial,restock;
  Row(int item,int value,int initial,int restock){this.item=item;this.value=value;this.initial=initial;this.restock=restock;}
 }
 private static final class Store {
  final int id,npc,x,y,buy,sell,delta;final String name;final List<Row> rows;
  Store(int id,int npc,int x,int y,int buy,String name,Row...rows){this(id,npc,x,y,1000,buy,20,name,rows);}
  Store(int id,int npc,int x,int y,int sell,int buy,int delta,String name,Row...rows){this.id=id;this.npc=npc;this.x=x;this.y=y;this.buy=buy;this.sell=sell;this.delta=delta;this.name=name;this.rows=List.of(rows);}
 }
 private static final Store ORES=new Store(HENDOR,7717,3032,9748,700,"Hendor",
  new Row(436,3,0,15000),new Row(438,3,0,15000),new Row(440,17,0,5000),new Row(447,162,0,2000),new Row(449,400,0,800),new Row(451,3200,0,200),new Row(453,45,0,5000));
 private static final Store PICKS=new Store(YARSUL,7718,3030,9746,600,"Yarsul",
  new Row(1265,1,6,50),new Row(1267,140,5,100),new Row(1269,500,4,200),new Row(1273,1300,3,300),new Row(1271,3200,2,500),new Row(1275,32000,1,700));
 private static final Row[] SUPPLIES={new Row(8794,10,1000,10),new Row(8790,500,1000,100),new Row(4819,2,1000,100),new Row(4820,4,1000,100),new Row(1539,3,1000,100)};
 private static final Row[] PLANTS={new Row(8417,1000,20,100),new Row(8419,2000,20,100),new Row(8421,5000,20,100),new Row(8423,10000,20,100),new Row(8425,15000,20,100),new Row(8427,20000,20,100),new Row(8429,50000,20,100),new Row(8431,1000,20,100),new Row(8433,5000,20,100),new Row(8435,10000,20,100)};
 // StoreLine's restock field is measured in game ticks, not seconds.
 private static final Row[] HUNTER_TOOLS={new Row(10010,20,5,10),new Row(10012,1,100,10),new Row(10025,600,30,50),new Row(10150,4,50,5),new Row(10006,5,50,5),new Row(10008,32,25,10),new Row(10029,50,5,10),new Row(596,4,20,10),new Row(10031,15,10,10),new Row(12740,505,3,10),new Row(12742,3200,3,10),new Row(12744,60000,3,10)};
 private static final Store ALECK_STORE=new Store(ALECK,1501,2567,3083,1200,700,20,"Aleck",HUNTER_TOOLS);
 private static final Store ARTIMEUS_STORE=new Store(ARTIMEUS,1350,3441,2902,1200,700,20,"Artimeus",HUNTER_TOOLS);
 private static final Store ASYFF_STORE=new Store(ASYFF,2887,3281,3398,1300,400,20,"Asyff",new Row(1949,2,0,100),new Row(579,2,3,100),new Row(1023,32,1,100),new Row(958,50,3,100),new Row(948,10,3,100),new Row(1733,1,3,100),new Row(1734,1,100,5),new Row(1059,6,10,100),new Row(1061,6,10,100),new Row(428,5,3,100),new Row(426,5,3,100),new Row(1757,2,1,100),new Row(1013,2,5,100),new Row(1015,2,3,100),new Row(1011,2,2,100),new Row(1007,2,4,100),new Row(1025,2,3,2000),new Row(10088,10,0,20),new Row(10090,13,0,20),new Row(10091,15,0,20),new Row(10089,16,0,20),new Row(10087,20,0,20));
 private static final List<Store> STORES=List.of(ORES,PICKS,
  ALECK_STORE,ARTIMEUS_STORE,ASYFF_STORE,
  new Store(224,3101,3302,3492,1300,500,0,"Construction supplies - Varrock",SUPPLIES),
  new Store(225,3101,1623,3500,1300,500,0,"Construction supplies - Guild",SUPPLIES),
  new Store(226,9140,3315,6116,1300,500,0,"Construction supplies - Prifddinas",SUPPLIES),
  new Store(227,3098,2849,10184,1300,500,30,"Keldagrim Stonemason",new Row(3420,20,1000,100),new Row(8786,250000,20,100),new Row(8784,100000,20,100),new Row(8788,750000,10,100),new Row(26266,8000000,10,100)),
  new Store(228,3102,3017,3374,1000,400,30,"Garden Centre - Falador",PLANTS),
  new Store(229,3103,1247,3735,1000,400,30,"Garden Centre - Farming Guild",PLANTS));
 private static long cycle;
 private MiningGuildStores(){}
 private static Store store(int id){for(Store s:STORES)if(s.id==id)return s;return null;}
 public static int constructionStore(NPC npc){if(npc!=null)for(Store s:STORES)if(s.id>=224&&npc.getNpcId()==s.npc&&npc.getX()==s.x&&npc.getY()==s.y&&npc.getHeight()==0)return s.id;return -1;}
 public static boolean handles(int id){return store(id)!=null;}
 public static List<Row> rows(int id){Store s=store(id);return s==null?List.of():s.rows;}
 public static long cycle(){return cycle;}
 public static int slot(int shop,int item){Store s=store(shop);if(s!=null)for(int i=0;i<s.rows.size();i++)if(s.rows.get(i).item==item)return i;return -1;}
 private static int sellBase(Store s,int item){io.xeros.model.definitions.ItemDef def=io.xeros.model.definitions.ItemDef.forId(item);return s!=null&&(s.id==ALECK||s.id==ARTIMEUS||s.id==ASYFF)&&def.isNoted()?def.getNoteId():item;}
 /** Price is recalculated for each item in a bulk transaction, including overstock. */
 public static int price(int shop,int item,boolean buyingFromShop){
  Store s=store(shop);if(!buyingFromShop)item=sellBase(s,item);int slot=slot(shop,item);if(s==null||slot<0)return -1;Row row=s.rows.get(slot);
  long multiplier=(buyingFromShop?s.sell:s.buy)+(long)s.delta*(row.initial-ShopHandler.ShopItemsN[shop][slot]);
  long fraction=Math.max(buyingFromShop?300:100,multiplier);
  return (int)Math.min(Integer.MAX_VALUE,Math.max(buyingFromShop?1:0,row.value*fraction/1000));
 }
 private static boolean ready(Player p){return !p.isDead&&!p.isDisconnected()&&p.getSession()!=null&&p.getInstance()==null&&!p.isBanking&&!p.getLock().cannotInteract(p)&&p.getGemBag().configurationPermitted()&&!Server.getMultiplayerSessionListener().inAnySession(p);}
 private static boolean near(Player p,Store s){return s!=null&&ready(p)&&p.heightLevel==0&&p.distanceToPoint(s.x,s.y)<=2&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->n.getNpcId()==s.npc&&!n.isDead()&&n.getHeight()==0&&n.getX()==s.x&&n.getY()==s.y);}
 public static void open(Player p,int id){Store s=store(id);if(near(p,s)){p.getPA().removeAllWindows();p.getShops().openShop(id);}}
 public static boolean npc(Player p,NPC npc,int option){
  Store s=npc==null?null:npc.getNpcId()==ORES.npc?ORES:npc.getNpcId()==PICKS.npc?PICKS:npc.getNpcId()==ALECK_STORE.npc?ALECK_STORE:npc.getNpcId()==ARTIMEUS_STORE.npc?ARTIMEUS_STORE:npc.getNpcId()==ASYFF_STORE.npc?ASYFF_STORE:null;if(s==null)return false;
  if(npc.getIndex()<0||npc.getIndex()>=NPCHandler.npcs.length||NPCHandler.npcs[npc.getIndex()]!=npc||npc.getX()!=s.x||npc.getY()!=s.y||!near(p,s))return true;
  if(option==1&&s==ASYFF_STORE)return false;
  if(option==2||option==4&&s==ARTIMEUS_STORE){open(p,s.id);return true;}if(option!=1)return false;
  new DialogueBuilder(p).option(s.name+(s.id==ALECK||s.id==ARTIMEUS?" - Hunter supplies":" - Mining Guild"),new DialogueOption("Trade",q->open(q,s.id)),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
 }
 private static boolean shopping(Player p,Store s){return near(p,s)&&p.myShopId==s.id&&p.isShopping&&(p.isInterfaceOpen(64000)||p.isInterfaceOpen(3824));}
 private static void refresh(Player p,int id){p.getItems().sendInventoryInterface(3823);p.getShops().resetShop(id);p.getShops().updatePlayerShop();}
 public static boolean buy(Player p,int item,int fromSlot,int requested){Store s=store(p.myShopId);int at=slot(p.myShopId,item);
  if(!shopping(p,s)||requested<=0||at<0||fromSlot!=at||ShopHandler.ShopItems[s.id][at]!=item+1)return false;
  int amount=Math.min(requested,ShopHandler.ShopItemsN[s.id][at]),bought=0;
  while(bought<amount&&ProductionBatch.exchange(p,new int[][]{{995,price(s.id,item,true)}},new int[][]{{item,1}})){ShopHandler.ShopItemsN[s.id][at]--;bought++;}
  if(bought==0)p.sendMessage("You need enough coins, stock and inventory space.");refresh(p,s.id);return bought>0;
 }
 public static boolean sell(Player p,int item,int fromSlot,int requested){Store s=store(p.myShopId);int base=sellBase(s,item),at=slot(p.myShopId,base);
  if(!shopping(p,s)||requested<=0||at<0||fromSlot<0||fromSlot>=p.playerItems.length||p.playerItems[fromSlot]!=item+1||ShopHandler.ShopItems[s.id][at]!=base+1)return false;
  int amount=Math.min(requested,p.getItems().getInventoryCount(item)),sold=0;
  while(sold<amount&&ShopHandler.ShopItemsN[s.id][at]<Integer.MAX_VALUE){int coins=price(s.id,item,false);if(!ProductionBatch.exchange(p,new int[][]{{item,1}},coins==0?new int[0][]:new int[][]{{995,coins}}))break;ShopHandler.ShopItemsN[s.id][at]++;sold++;}
  if(sold==0)p.sendMessage("The sale could not be completed.");refresh(p,s.id);return sold>0;
 }
 /** Called once per game tick. Stock normalizes on the shared world phase. */
 public static void pulse(){cycle++;for(Store s:STORES){boolean changed=false;for(int i=0;i<s.rows.size();i++){Row row=s.rows.get(i);int stock=ShopHandler.ShopItemsN[s.id][i];if(stock!=row.initial&&cycle%row.restock==0){ShopHandler.ShopItemsN[s.id][i]+=stock<row.initial?1:-1;changed=true;}}
  if(changed)PlayerHandler.nonNullStream().filter(p->p.isShopping&&p.myShopId==s.id).forEach(p->{p.updateShop=true;p.getShops().resetShop(s.id);});}}
}
