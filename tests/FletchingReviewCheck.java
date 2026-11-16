package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.*;
import io.xeros.content.skills.crafting.Pottery;
import io.xeros.content.skills.fletching.*;
import io.xeros.content.skills.slayer.SlayerUnlock;
import io.xeros.content.dialogue.*;
import io.xeros.content.items.UseItem;
import io.xeros.model.entity.player.Player;
import io.xeros.model.definitions.ItemDef;
import java.util.*;
public class FletchingReviewCheck{
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){return AttackDefencePlayableCheck.actor();}
 static void give(Player p,int id,int n){SkillsAuditCheck.item(p,id,n);}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void select(Player p){ok(p.getDialogueBuilder()!=null,"Missing recipe menu");p.getDialogueBuilder().getCurrent().handleAction(p,DialogueAction.OPTION_1);}
 static void valid(int id){ItemDef d=ItemDef.forId(id);ok(ItemDef.getDefinitions().containsKey(id)&&d!=null&&!d.isNoted()&&d.getName()!=null&&!d.getName().equalsIgnoreCase("null"),"Missing item definition "+id);}
 static void recipes(){
  for(FletchingRecipes.Recipe r:FletchingRecipes.recipes()){
   valid(r.first);valid(r.product);if(r.second>0)valid(r.second);if(r.tool>0)valid(r.tool);
   Player p=actor();give(p,r.first,2);if(r.second>0)give(p,r.second,2);if(r.tool>0)give(p,r.tool,1);int old=p.playerXP[9];p.playerLevel[9]=r.level-1;
   FletchingRecipes.make(p,r,1);tick(r.ticks);ok(count(p,r.product)==0,"Recipe level gate "+r.product);clear(p);p.playerLevel[9]=99;
   FletchingRecipes.make(p,r,1);tick(r.ticks);ok(count(p,r.product)==r.quantity,"Recipe result "+r.product);ExpandedSkillsCheck.xp(p,9,old,r.xp);if(r.tool>0)ok(count(p,r.tool)==1,"Tool consumed "+r.product);clear(p);
   p=actor();give(p,r.first,2);if(r.second>0)give(p,r.second,2);if(r.tool>0)give(p,r.tool,1);FletchingRecipes.make(p,r,1);p.interruptActions();tick(r.ticks);ok(count(p,r.product)==0,"Cancelled recipe completed "+r.product);clear(p);
   if(r.tool>0){p=actor();give(p,r.first,2);if(r.second>0)give(p,r.second,2);FletchingRecipes.make(p,r,1);tick(r.ticks);ok(count(p,r.product)==0&&count(p,r.first)>=2,"Missing tool consumed material "+r.product);clear(p);}
  }
  // Independent 2022 quantities and levels for every arrow-shaft tier.
  int[][] refs={{1511,1,15,5},{1521,15,30,10},{1519,30,45,15},{1517,45,60,20},{1515,60,75,25},{1513,75,90,30},{19669,90,105,35}};
  for(int[] v:refs){FletchingRecipes.Recipe r=FletchingRecipes.recipes().stream().filter(q->q.first==v[0]&&q.product==52).findFirst().orElseThrow();ok(r.level==v[1]&&r.quantity==v[2]&&r.xp==v[3],"Shaft independent reference "+v[0]);}
  ok(FletchableCrossbow.ADAMANR_CROSSBOW.getItem()==9463&&FletchableCrossbow.ADAMANR_CROSSBOW.getProduct()==9183&&FletchableCrossbow.ADAMANR_CROSSBOW.getLevelRequired()==61&&FletchableCrossbow.RUNE_CROSSBOW.getExperience()==50,"Adamant/rune independent crossbow reference");
  ok(FletchableGem.OPAL.getExperience()==1.5&&FletchableGem.SAPHIRE.getExperience()==4&&FletchableGem.ONYX.getAmount()==24&&FletchableGem.ONYX.getExperience()==9.4,"Independent gem tips reference");
  System.out.println("FLETCHING_ALL_COMPONENT_RECIPES_TOOLS_LEVELS_XP_OUTPUT_CANCELLATION_AND_INDEPENDENT_2022_REFERENCES_OK "+FletchingRecipes.recipes().size());
 }
 static void bolts(){
  int n=0;for(FletchableUnfinishedBolt r:FletchableUnfinishedBolt.values()){
   Player p=actor();p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);give(p,r.getUnfinished(),10);give(p,314,10);int old=p.playerXP[9];p.getFletching().fletchUnfinishedBolt(r.getUnfinished());ok(count(p,r.getBolt())==10&&count(p,r.getUnfinished())==0&&count(p,314)==0,"Feather bolts "+r);ExpandedSkillsCheck.xp(p,9,old,r.getExperience()*10);clear(p);n++;
  }
  for(FletchableBolt r:FletchableBolt.values()){
   Player p=actor();p.getSlayer().getUnlocks().add(SlayerUnlock.BROADER_FLETCHING);give(p,r.getUnfinished(),10);give(p,r.getTip(),10);int old=p.playerXP[9];p.getFletching().fletchBolt(r.getUnfinished(),r.getTip());tick(2);ok(count(p,r.getBolt())==10&&count(p,r.getUnfinished())==0&&count(p,r.getTip())==0,"10-bolt output "+r);ExpandedSkillsCheck.xp(p,9,old,r.getExperience()*10);clear(p);n++;
   p=actor();give(p,r.getUnfinished(),10);give(p,r.getTip(),10);old=p.playerXP[9];p.playerLevel[9]=r.getLevel()-1;p.getFletching().fletchBolt(r.getUnfinished(),r.getTip());tick(2);ok(count(p,r.getBolt())==0&&p.playerXP[9]==old,"Bolt level "+r);clear(p);
  }
  ok(FletchableBolt.SAPPHIRE.getLevel()==56&&FletchableBolt.SAPPHIRE.getExperience()==4.7&&FletchableBolt.OPAL_DRAGON.getLevel()==84&&FletchableUnfinishedBolt.BRONZE.getExperience()==.5&&FletchableUnfinishedBolt.BROAD.getExperience()==3,"Bolt independent reference");
  Player p=actor();give(p,11876,10);give(p,314,10);p.getFletching().fletchUnfinishedBolt(11876);ok(count(p,11876)==10&&count(p,11875)==0,"Broad unlock bypass");clear(p);
  p=actor();give(p,877,100);give(p,45,100);give(p,879,1);give(p,1931,25);ok(p.getItems().freeSlots()==0,"Full fixture");p.getFletching().fletchBolt(877,45);tick(2);ok(count(p,879)==11&&count(p,877)==90&&count(p,45)==90,"Existing output stack in full inventory");clear(p);
  System.out.println("FLETCHING_ALL_FEATHERED_AND_TIPPED_BOLTS_TEN_PER_BATCH_XP_LEVELS_BROAD_UNLOCK_FULL_INVENTORY_OK "+n);
 }
 static void routes(){
  Player p=actor();give(p,19669,1);give(p,946,1);UseItem.ItemonItem(p,946,19669,p.getItems().getInventoryItemSlot(946),p.getItems().getInventoryItemSlot(19669));select(p);select(p);tick(3);ok(count(p,52)==105&&count(p,19669)==0,"Redwood actual knife route");clear(p);
  p=actor();give(p,9440,1);give(p,9420,1);give(p,2347,1);UseItem.ItemonItem(p,9420,9440,p.getItems().getInventoryItemSlot(9420),p.getItems().getInventoryItemSlot(9440));select(p);select(p);tick(2);ok(count(p,9454)==1,"Reverse crossbow assembly route");give(p,9438,1);p.getFletching().fletchUnstrungCross(9454);tick(3);ok(count(p,9174)==1,"Crossbow complete chain");clear(p);
  p=actor();give(p,1609,1);give(p,1755,1);UseItem.ItemonItem(p,1609,1755,p.getItems().getInventoryItemSlot(1609),p.getItems().getInventoryItemSlot(1755));select(p);select(p);tick(5);ok(count(p,45)==12&&count(p,1609)==0,"Cut gem route");clear(p);
  p=actor();give(p,11237,15);give(p,53,15);give(p,1761,1);p.getFletching().fletchArrow(11237);Pottery.make(p,Pottery.recipes().get(0),false,1);tick(3);ok(count(p,11212)==0&&count(p,11237)==15&&count(p,1787)==1,"Fletching concurrently completed after another skill");clear(p);
  System.out.println("FLETCHING_REAL_ITEM_ROUTES_REDWOOD_CROSSBOW_CHAIN_GEM_TIPS_AND_CROSS_SKILL_CANCELLATION_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;recipes();bolts();routes();System.out.println("FLETCHING_REVIEW_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
