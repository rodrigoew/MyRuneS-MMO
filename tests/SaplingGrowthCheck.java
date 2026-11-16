package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.farming.*;
import io.xeros.content.skills.WaterFilling;
import io.xeros.content.items.UseItem;
import io.xeros.content.dialogue.DialogueAction;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import io.xeros.model.items.bank.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.definitions.ItemDef;
import java.util.*;import java.util.concurrent.atomic.AtomicLong;import java.lang.reflect.*;
public class SaplingGrowthCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();p.setLoginName("saplingqa");p.setDisplayName("Saplingqa");return p;}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static long bank(Player p,int id){long n=0;for(BankTab tab:p.getBank().getBankTab())for(BankItem item:tab.getItems())if(item.getId()==id+1)n+=item.getAmount();return n;}
 static int slot(Player p,int id){for(int i=0;i<p.playerItems.length;i++)if(p.playerItems[i]==id+1)return i;throw new AssertionError("Missing item "+id);}
 static void cycles(){
  int[][] reference={{5312,5358,5364,5370,15},{5313,5359,5365,5371,30},{5314,5360,5366,5372,45},{5315,5361,5367,5373,60},{5316,5362,5368,5374,75},{5283,5480,5488,5496,27},{5284,5481,5489,5497,33},{5285,5482,5490,5498,39},{5286,5483,5491,5499,42},{5287,5484,5492,5500,51},{5288,5485,5493,5501,57},{5289,5486,5494,5502,68}};int index=0;
  for(SaplingGrowth.Recipe recipe:SaplingGrowth.recipes()){
   int[] row=reference[index++];ok(recipe.seed==row[0]&&recipe.dry==row[1]&&recipe.watered==row[2]&&recipe.crop.seed==row[3]&&recipe.crop.level==row[4],"Seedling reference "+recipe.crop);
   for(int id:row)if(id>1000)ok(ItemDef.getDefinitions().containsKey(id)&&!ItemDef.forId(id).getName().startsWith("unknown"),"Missing nursery definition "+id);
   Player p=actor();AtomicLong time=new AtomicLong(1_000_000);SaplingGrowth nursery=new SaplingGrowth(p,time::get);give(p,recipe.seed,2);give(p,5354,2);give(p,5325,1);give(p,5340,1);int xp=p.playerXP[19];
   p.playerLevel[19]=recipe.crop.level-1;ok(!nursery.make(recipe,2),"Seedling level gate");tick(4);ok(count(p,recipe.seed)==2&&count(p,5354)==2&&count(p,recipe.dry)==0,"Rejected seedling consumed inputs");
   p.playerLevel[19]=99;ok(nursery.make(recipe,2),"Seedling make");tick(1);ok(count(p,recipe.dry)==0,"Seedling early timer");tick(3);
   ok(count(p,recipe.seed)==0&&count(p,5354)==0&&count(p,recipe.dry)==2&&count(p,5325)==1&&p.playerXP[19]==xp,"Seed pot inputs/output/XP");clear(p);
   p.playerLevel[19]=1;ok(nursery.water(recipe,5340,2),"Water after boost wears off");tick(2);ok(count(p,recipe.watered)==0,"Water early timer");tick(4);
   ok(count(p,recipe.dry)==0&&count(p,recipe.watered)==2&&count(p,5338)==1&&count(p,5340)==0&&p.playerXP[19]==xp,"Two watering doses/XP");
   long deadline=nursery.deadline(recipe);ok(deadline==1_200_000,"Global five-minute boundary");time.set(deadline-1);ok(!nursery.process()&&count(p,recipe.watered)==2,"Premature sapling growth");time.incrementAndGet();
   ok(nursery.process()&&count(p,recipe.crop.seed)==2&&count(p,recipe.watered)==0&&nursery.deadline(recipe)==0&&p.playerXP[19]==xp,"Saplings at boundary");ok(!nursery.process()&&count(p,recipe.crop.seed)==2,"Repeated growth");clear(p);
  }
  ok(index==12,"Not all twelve seed-pot recipes covered");
  System.out.println("SAPLINGS_TWELVE_FULL_SEED_POT_WATER_CAN_DOSES_LEVELS_BOUNDARIES_AND_NO_XP_CYCLES_OK");
 }
 static void safety(){
  SaplingGrowth.Recipe recipe=SaplingGrowth.recipes().get(0);Player p=actor();AtomicLong clock=new AtomicLong(1000);SaplingGrowth nursery=new SaplingGrowth(p,clock::get);
  give(p,recipe.seed,1);give(p,5354,1);int xp=p.playerXP[19];ok(!nursery.make(recipe,1),"Missing trowel");tick(2);ok(count(p,recipe.seed)==1&&count(p,5354)==1,"Missing tool loss");give(p,5325,1);
  nursery.make(recipe,1);p.interruptActions();tick(2);ok(count(p,recipe.seed)==1,"Seed pot continued after cancel");nursery.make(recipe,1);tick(2);clear(p);
  give(p,5331,1);ok(nursery.itemOnItem(recipe.dry,5331),"Empty can route");tick(3);ok(count(p,recipe.watered)==0&&count(p,recipe.dry)==1,"Empty can watered seedling");give(p,5333,1);
  nursery.water(recipe,5333,1);p.heightLevel=1;tick(3);ok(count(p,5333)==1&&count(p,recipe.dry)==1,"Wrong-floor water");p.heightLevel=0;
  nursery.water(recipe,5333,1);p.absX++;tick(3);ok(count(p,5333)==1&&count(p,recipe.dry)==1,"Moved water");p.absX--;
  while(p.getItems().freeSlots()>0)give(p,1931,1);nursery.water(recipe,5333,1);tick(3);ok(count(p,recipe.watered)==1&&count(p,5331)==2&&p.getItems().freeSlots()==0,"Full-bag one-dose exchange");
  clock.set(nursery.deadline(recipe));ok(nursery.process()&&count(p,recipe.crop.seed)==1&&p.getItems().freeSlots()==0&&p.playerXP[19]==xp,"Full bag growth/XP");clear(p);
  p=actor();give(p,recipe.dry,1);give(p,25025,1);nursery=new SaplingGrowth(p,clock::get);nursery.water(recipe,25025,1);tick(3);ok(count(p,25025)==1&&count(p,recipe.watered)==1,"Legacy magic-can tool preservation");clear(p);
  System.out.println("SAPLINGS_MISSING_TOOLS_EMPTY_CAN_CANCEL_ORIGIN_FLOOR_FULL_BAG_LAST_DOSE_AND_MAGIC_CAN_OK");
 }
 static void banks(){
  AtomicLong clock=new AtomicLong(1000);
  for(SaplingGrowth.Recipe recipe:SaplingGrowth.recipes()){
   Player p=actor();SaplingGrowth nursery=new SaplingGrowth(p,clock::get);for(int i=0;i<9;i++)p.getBank().getBankTab()[i].add(new BankItem(recipe.watered+1,i+1));give(p,recipe.watered,3);nursery.decode(recipe.crop.seed+"=1000");int xp=p.playerXP[19];
   ok(nursery.process()&&bank(p,recipe.crop.seed)==45&&bank(p,recipe.watered)==0&&count(p,recipe.crop.seed)==3&&p.playerXP[19]==xp,"Nine tabs + inventory "+recipe.crop);clear(p);
  }
  SaplingGrowth.Recipe recipe=SaplingGrowth.recipes().get(0);Player p=actor();SaplingGrowth nursery=new SaplingGrowth(p,clock::get);BankTab tab=p.getBank().getBankTab()[0];tab.add(new BankItem(recipe.watered+1,3));int capacity=p.getBank().getBankCapacity();for(int i=0;i<capacity-1;i++)tab.add(new BankItem(20000+i,1));nursery.decode(recipe.crop.seed+"=1000");ok(nursery.process()&&bank(p,recipe.crop.seed)==3&&p.getBank().getItemCount()==capacity,"Full bank growth");
  p=actor();nursery=new SaplingGrowth(p,clock::get);tab=p.getBank().getBankTab()[0];BankItem full=new BankItem(recipe.crop.seed+1,Integer.MAX_VALUE);tab.add(full);tab.add(new BankItem(recipe.watered+1,1));give(p,recipe.watered,1);give(p,recipe.dry,1);give(p,5340,1);nursery.decode(recipe.crop.seed+"=1000");
  ok(!nursery.process()&&bank(p,recipe.watered)==1&&count(p,recipe.watered)==1&&nursery.deadline(recipe)==1000,"Overflow consumed seedlings");ok(!nursery.water(recipe,5340,1)&&count(p,recipe.dry)==1,"New water reset blocked mature batch");full.setAmount(Integer.MAX_VALUE-1);
  ok(nursery.process()&&bank(p,recipe.crop.seed)==Integer.MAX_VALUE&&bank(p,recipe.watered)==0&&count(p,recipe.crop.seed)==1,"Recover after bank stack room");clear(p);
  p=actor();nursery=new SaplingGrowth(p,clock::get);give(p,recipe.watered,1);ok(!nursery.process()&&nursery.deadline(recipe)==300000,"Legacy watered seedling discovery");clock.set(300000);ok(nursery.process()&&count(p,recipe.crop.seed)==1,"Legacy seedling growth");clear(p);
  System.out.println("SAPLINGS_TWELVE_TYPES_NINE_BANK_TABS_FULL_BANK_OVERFLOW_DEFER_RESUME_AND_LEGACY_ITEMS_OK");
 }
 static void save()throws Exception{
  Player p=actor();SaplingGrowth.Recipe recipe=SaplingGrowth.recipes().get(0);give(p,recipe.watered,2);p.getBank().getBankTab()[4].add(new BankItem(recipe.watered+1,3));p.getFarming().getSaplings().decode(recipe.crop.seed+"="+(System.currentTimeMillis()-1000));p.setIndex(1);p.isActive=true;p.saveCharacter=true;PlayerHandler.players[1]=p;
  try{int xp=p.playerXP[19];ok(PlayerSave.saveGameInstant(p),"Save watered seedlings");Player q=actor();ok(PlayerSave.loadGame(q,"saplingqa",p.playerPass,true).toString().equals("SUCCESS"),"Load seedlings");ok(q.getFarming().getSaplings().deadline(recipe)>0&&count(q,recipe.watered)==2&&bank(q,recipe.watered)==3,"Pending save state");PlayerSave.login(q);ok(count(q,recipe.crop.seed)==2&&bank(q,recipe.crop.seed)==3&&q.getFarming().getSaplings().deadline(recipe)==0&&q.playerXP[19]==xp,"Offline nursery on login");PlayerSave.login(q);ok(count(q,recipe.crop.seed)==2&&bank(q,recipe.crop.seed)==3,"Repeat login grew duplicates");}
  finally{PlayerHandler.players[1]=null;clear(p);}
  AtomicLong time=new AtomicLong(1000);SaplingGrowth nursery=new SaplingGrowth(actor(),time::get);ok(nursery.decode("5370=1200"),"Decode valid deadline");String before=nursery.encode();for(String bad:new String[]{"5370=-1","99999=1200","5370=1200,5370=1500","5370=1=2","5370=999999999999999999999999","5370=0","5370=x"})ok(!nursery.decode(bad)&&nursery.encode().equals(before),"Malformed nursery state "+bad);
  ok(SaplingGrowth.nextBoundary(Long.MAX_VALUE)==Long.MAX_VALUE&&SaplingGrowth.nextBoundary(300000)==600000,"Clock overflow/boundary");
  System.out.println("SAPLINGS_REAL_CHARACTER_SAVE_LOAD_OFFLINE_LOGIN_NO_DUPLICATE_AND_MALFORMED_STATE_OK");
 }
 static WorldObject waterSource()throws Exception{
  Field regions=RegionProvider.class.getDeclaredField("regions"),objects=Region.class.getDeclaredField("worldObjects");regions.setAccessible(true);objects.setAccessible(true);Set<Integer> candidates=Set.of(873,874,1763,3640,20776,24004);
  for(Region region:((Map<Integer,Region>)regions.get(RegionProvider.getGlobal())).values())for(WorldObject o:(List<WorldObject>)objects.get(region))if(o.height==0&&candidates.contains(o.id)&&WaterFilling.source(o.id))return o;
  throw new AssertionError("No ordinary water source in map");
 }
 static void waterSources()throws Exception{
  WorldObject source=waterSource();
  for(int[] pair:new int[][]{{1925,1929},{1935,1937},{1923,1921},{229,227},{5331,5340},{5335,5340}}){
   Player p=actor();p.absX=source.x;p.absY=source.y-1;give(p,pair[0],2);int xp=p.playerXP[19];UseItem.ItemonObject(p,source.id,source.x,source.y,pair[0]);tick(2);ok(count(p,pair[1])==0,"Water before timer");tick(4);ok(count(p,pair[0])==0&&count(p,pair[1])==2&&p.playerXP[19]==xp,"Native water container "+pair[0]);clear(p);
  }
  Player p=actor();p.absX=source.x;p.absY=source.y-1;give(p,5331,1);give(p,5339,1);give(p,5334,1);WaterFilling.use(p,5331,source.id,source.x,source.y);tick(9);ok(count(p,5340)==3,"Mixed empty/partial cans");clear(p);
  p=actor();p.absX=source.x;p.absY=source.y-1;give(p,1925,1);WaterFilling.use(p,1925,source.id,source.x,source.y);p.heightLevel=1;tick(3);ok(count(p,1925)==1&&count(p,1929)==0,"Water wrong floor after start");p.heightLevel=0;WaterFilling.use(p,1925,source.id,source.x,source.y);p.absX++;tick(3);ok(count(p,1925)==1,"Water moved");clear(p);
  System.out.println("NURSERY_SIX_NATIVE_WATER_CONTAINERS_MIXED_CANS_TIMERS_NO_XP_AND_CANCELLATION_OK source="+source.id+" "+source.x+","+source.y);
 }
 static void routes(){
  Player p=actor();SaplingGrowth.Recipe recipe=SaplingGrowth.recipes().get(0);give(p,recipe.seed,1);give(p,5354,1);give(p,5325,1);give(p,5340,1);
  UseItem.ItemonItem(p,recipe.seed,5354,slot(p,recipe.seed),slot(p,5354));ok(p.getDialogueBuilder()!=null,"Nursery item menu");p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);tick(2);ok(count(p,recipe.dry)==1,"Nursery menu make");
  UseItem.ItemonItem(p,5340,recipe.dry,slot(p,5340),slot(p,recipe.dry));tick(3);ok(count(p,recipe.watered)==1&&count(p,5339)==1&&p.getFarming().getSaplings().deadline(recipe)>System.currentTimeMillis(),"Reverse can-on-seedling route");clear(p);
  System.out.println("SAPLINGS_REAL_ITEM_ON_ITEM_QUANTITY_MENU_AND_REVERSE_WATER_CAN_ROUTE_OK");
 }
 static void shop(){
  io.xeros.model.definitions.ShopDef shop=io.xeros.model.definitions.ShopDef.get(16);
  for(SaplingGrowth.Recipe recipe:SaplingGrowth.recipes())ok(shop.getItems().stream().anyMatch(item->item.getId()==recipe.seed+1),"Tree seed missing from shop "+recipe.seed);
  Player p=actor();give(p,995,100000);p.getShops().openShop(16);List<io.xeros.model.items.GameItem> stock=io.xeros.model.world.ShopHandler.getShopItems(16);int slot=-1;for(int i=0;i<stock.size();i++)if(stock.get(i).getId()==5313)slot=i;
  ok(slot>=0&&p.getShops().buyItem(5312,slot,1)&&count(p,5312)==1&&count(p,995)<100000,"Acorn purchase and payment");clear(p);
  System.out.println("SAPLINGS_TWELVE_TREE_SEEDS_IN_FARMING_SHOP_REAL_PURCHASE_AND_COINS_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;cycles();safety();banks();save();waterSources();routes();shop();System.out.println("SAPLING_GROWTH_ALL_CHECKS_OK");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
