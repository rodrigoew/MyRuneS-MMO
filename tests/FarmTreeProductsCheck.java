package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.farming.*;import io.xeros.model.entity.player.Player;import java.lang.reflect.*;
public class FarmTreeProductsCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.absX=3190;p.absY=3229;return p;}
 static Plant plant(Player p,Plants crop)throws Exception{
  Field f=Farming.class.getDeclaredField("patches");f.setAccessible(true);((GrassyPatch[])f.get(p.getFarming()))[FarmingPatches.LUMBRIDGE_TREE.ordinal()].stage=3;
  give(p,952,1);give(p,crop.seed,1);p.getFarming().plant(crop.seed,3191,3229);Plant tree=p.getFarming().getPlantedPatch(3191,3229);ok(tree!=null&&count(p,5350)==1,"Sapling/pot "+crop);return tree;
 }
 static void roots()throws Exception{
  Plants[] crops={Plants.OAK_TREE,Plants.WILLOW_TREE,Plants.MAPLE_TREE,Plants.YEW_TREE,Plants.MAGIC_TREE};
  int[][] levels={{15,23,31,39},{30,38,46,54},{45,53,61,69},{60,68,76,84},{75,83,91,99}};
  int[] roots={6043,6045,6047,6049,6051};
  for(int i=0;i<crops.length;i++)for(int step=0;step<4;step++){
   Player p=actor();p.playerLevel[19]=levels[i][step];p.playerXP[19]=p.getPA().getXPForLevel(levels[i][step]);Plant tree=plant(p,crops[i]);tree.stage=crops[i].stages;tree.checkedHealth=true;tree.stumped=true;
   ok(tree.rootItem()==roots[i]&&tree.rootAmount(levels[i][step])==step+1&&tree.rootAmount(levels[i][step]+7)==Math.min(4,step+1),"Root threshold "+crops[i]);
   int xp=p.playerXP[19];tree.useItemOnPlant(p,952);tick(2);ok(p.getFarming().contains(tree)&&count(p,roots[i])==0,"Early root reward");tick(1);
   ok(!p.getFarming().contains(tree)&&count(p,roots[i])==step+1&&p.playerXP[19]==xp,"Root output/XP "+crops[i]);tree.clearWithSpade(p);tick(3);ok(count(p,roots[i])==step+1,"Repeated stump reward");clear(p);
  }
  System.out.println("FARMING_ALL_FIVE_ROOT_TYPES_TWENTY_LEVEL_THRESHOLDS_POTS_TIMERS_CLEAR_NO_REPEAT_OK");
 }
 static void safety()throws Exception{
  Player p=actor();Plant tree=plant(p,Plants.MAGIC_TREE);tree.stage=12;tree.checkedHealth=true;tree.stumped=true;
  while(p.getItems().freeSlots()>0)give(p,1931,1);tree.clearWithSpade(p);tick(3);ok(p.getFarming().contains(tree)&&count(p,6051)==0,"Full bag lost stump/roots");
  p.getItems().deleteItem2(1931,4);tree.clearWithSpade(p);p.absX++;tick(3);ok(p.getFarming().contains(tree)&&count(p,6051)==0,"Moved root clear");p.absX--;
  tree.stumped=false;tree.useItemOnPlant(p,952);tick(3);ok(p.getFarming().contains(tree),"Healthy tree dug without axe");tree.dead=true;tree.clearWithSpade(p);tick(3);ok(!p.getFarming().contains(tree)&&count(p,6051)==0,"Dead tree yielded roots");clear(p);
  p=actor();tree=plant(p,Plants.OAK_TREE);tree.stage=4;tree.checkedHealth=true;p.playerEquipment[Player.playerWeapon]=1359;tree.click(p,1);p.interruptActions();tick(8);ok(count(p,1521)==0,"Chopping continued after cancel");clear(p);
  p=actor();tree=plant(p,Plants.OAK_TREE);p.absX=2812;p.absY=3462;Field f=Farming.class.getDeclaredField("patches");f.setAccessible(true);((GrassyPatch[])f.get(p.getFarming()))[2].stage=3;give(p,5325,1);p.getFarming().handleItemOnObject(5350,8151,2813,3462);tick(2);ok(count(p,5350)==0&&count(p,5354)==1&&count(p,5325)==1,"Fill plant pot/tool");clear(p);
  System.out.println("FARMING_ROOTS_FULL_BAG_CANCELLATION_DEAD_CROP_AXE_REQUIREMENT_AND_REFILL_POTS_OK");
 }
 static void shop(){
  io.xeros.model.definitions.ShopDef shop=io.xeros.model.definitions.ShopDef.get(16);
  for(int id:new int[]{952,5325,5350,5354,6036,6032,6034,22887,22879,5370,5371,5372,5373,5374,5496,5497,5498,5499,5500,5501,5502})
   ok(shop.getItems().stream().anyMatch(item->item.getId()==id+1),"Farming shop missing "+id);
  io.xeros.model.entity.npc.NPC npc=java.util.Arrays.stream(io.xeros.model.entity.npc.NPCHandler.npcs).filter(n->n!=null&&n.getNpcId()==732).findFirst().orElseThrow();
  Player p=actor();p.absX=npc.absX;p.absY=npc.absY;p.npcClickIndex=npc.getIndex();io.xeros.model.entity.player.packets.npcoptions.NpcOptionTwo.handleOption(p,732);ok(p.myShopId==16,"Farming NPC shop route");give(p,995,100000);
  java.util.List<io.xeros.model.items.GameItem> stock=io.xeros.model.world.ShopHandler.getShopItems(16);int slot=-1;for(int i=0;i<stock.size();i++)if(stock.get(i).getId()==953)slot=i;
  ok(slot>=0&&p.getShops().buyItem(952,slot,1)&&count(p,952)==1&&count(p,995)<100000,"Farming shop spade purchase/payment");clear(p);
  System.out.println("FARMING_21_ADDED_SHOP_SUPPLIES_REAL_NPC_ROUTE_SPADE_PURCHASE_AND_COINS_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;roots();safety();shop();System.out.println("FARM_TREE_PRODUCTS_ALL_CHECKS_OK");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
