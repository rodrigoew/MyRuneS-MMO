package io.xeros.solo;
import io.xeros.*;
import io.xeros.content.skills.*;
import io.xeros.content.skills.smithing.*;
import io.xeros.content.skills.crafting.*;
import io.xeros.content.skills.cooking.*;
import io.xeros.content.skills.herblore.*;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.GameItem;
import java.util.*;
public class ProductionReviewCheck {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static Player actor(){Player p=SkillsAuditCheck.actor();p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void give(Player p,int id,int qty){SkillsAuditCheck.item(p,id,qty);ok(count(p,id)>=qty,"Cannot give fixture "+id+" "+ItemDef.forId(id).getName());}
 static int count(Player p,int id){return SkillsAuditCheck.count(p,id);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static void clear(Player p){SkillsAuditCheck.clear(p);}
 static void xp(Player p,int skill,int before,double base){Player q=actor();int old=q.playerXP[skill];q.getPA().addSkillXPMultiplied(base,skill,false);ok(p.playerXP[skill]-before==q.playerXP[skill]-old,"XP mismatch skill="+skill+" base="+base);}
 static void valid(int id){ItemDef d=ItemDef.forId(id);ok(ItemDef.getDefinitions().containsKey(id)&&d!=null&&d.getName()!=null&&!d.getName().isBlank()&&!d.getName().equalsIgnoreCase("null")&&!d.isNoted(),"Invalid recipe item "+id+" "+(d==null?"missing":d.getName()));}
 static void anvils(){
  int n=0;for(AnvilRecipes.Recipe r:AnvilRecipes.recipes()){
   valid(r.product);Player p=actor();give(p,r.bar,r.bars*3);int old=p.playerXP[13];
   AnvilRecipes.make(p,r.product,1);tick(5);ok(count(p,r.product)==0&&count(p,r.bar)==r.bars*3,"Missing hammer accepted "+r.product);clear(p);
   give(p,2347,1);p.playerLevel[13]=r.level-1;AnvilRecipes.make(p,r.product,1);tick(5);ok(count(p,r.product)==0,"Level gate "+r.product);clear(p);
   p.playerLevel[13]=99;ok(AnvilRecipes.make(p,r.product,2),"Recipe rejected");tick(10);
   ok(count(p,r.product)==r.quantity*2&&count(p,r.bar)==r.bars,"Anvil quantities "+r.product);xp(p,13,old,r.xp*2);clear(p);n++;
  }
  Player a=actor(),b=actor();give(a,2347,1);give(a,2349,2);give(b,2347,1);give(b,2363,5);AnvilRecipes.make(a,1205,2);AnvilRecipes.make(b,1127,1);tick(10);ok(count(a,1205)==2&&count(b,1127)==1&&count(a,1127)==0,"Cross-player recipe contamination");clear(a);clear(b);
  a=actor();give(a,2347,1);give(a,2349,1);AnvilRecipes.make(a,1205,1);a.absX++;tick(5);ok(count(a,2349)==1&&count(a,1205)==0,"Anvil movement");clear(a);
  a=actor();give(a,2347,1);give(a,2349,1);AnvilRecipes.make(a,1205,1);a.getPA().stopSkilling();tick(5);ok(count(a,2349)==1&&count(a,1205)==0,"Anvil cancelled");clear(a);
  ok(AnvilRecipes.forProduct(4819).quantity==15&&AnvilRecipes.forProduct(19570).quantity==5&&AnvilRecipes.forProduct(1205).xp==12.5&&AnvilRecipes.forProduct(1127).level==99,"Independent anvil reference");
  System.out.println("PRODUCTION_ANVIL_ALL_RECIPES_TOOLS_LEVEL_MATERIALS_QUANTITY_XP_MOVEMENT_CANCEL_ISOLATION_OK "+n);
 }
 static void jewellery(){
  for(int[] ref:new int[][]{{21081,1,10},{21084,13,32},{21087,16,35},{21090,16,35},{21093,25,54},{21096,32,70},{21099,27,55},{21102,34,70},{21105,45,80},{21117,22,45},{21120,29,60},{21123,38,75}}){JewelleryRecipes.Recipe recipe=JewelleryRecipes.recipes().stream().filter(r->r.product==ref[0]).findFirst().orElseThrow();ok(recipe.level==ref[1]&&recipe.xp==ref[2],"Silver jewellery reference "+ref[0]);}
  int n=0;for(JewelleryRecipes.Recipe r:JewelleryRecipes.recipes()){
   valid(r.product);Player p=actor();give(p,r.bar,2);if(r.gem>0)give(p,r.gem,2);int old=p.playerXP[12];
   JewelleryRecipes.make(p,r.product,1);tick(3);ok(count(p,r.product)==0,"Missing mould accepted "+r.product);clear(p);
   give(p,r.mould,1);p.playerLevel[12]=r.level-1;JewelleryRecipes.make(p,r.product,1);tick(3);ok(count(p,r.product)==0,"Jewellery level "+r.product);clear(p);
   p.playerLevel[12]=99;JewelleryRecipes.make(p,r.product,2);tick(6);ok(count(p,r.product)==2&&count(p,r.bar)==0&&(r.gem==0||count(p,r.gem)==0)&&count(p,r.mould)==1,"Jewellery materials/output "+r.product);xp(p,12,old,r.xp*2);clear(p);n++;
  }
  System.out.println("PRODUCTION_JEWELLERY_ALL_RECIPES_MOULDS_LEVEL_MATERIALS_OUTPUT_XP_OK "+n);
 }
 static void cooking(){
  int n=0;for(CookingRecipes.Bake r:CookingRecipes.bakes()){
   valid(r.raw);valid(r.cooked);valid(r.burnt);Player p=actor();give(p,r.raw,1);int old=p.playerXP[7];p.playerLevel[7]=r.level-1;
   CookingRecipes.startBake(p,r,2728,1);tick(2);ok(count(p,r.raw)==1,"Cooking level "+r.raw);clear(p);p.playerLevel[7]=99;
   if(r.rangeOnly){CookingRecipes.startBake(p,r,2732,1);tick(2);ok(count(p,r.raw)==1,"Range-only on fire "+r.raw);clear(p);}
   ok(Cooking.cookThisFood(p,r.raw,2728),"Cooking real entry route "+r.raw);tick(2);ok(count(p,r.raw)==0&&count(p,r.cooked)==1,"Cooking output "+r.raw);if(r.extra>0)ok(count(p,r.extra)==1,"Baking container "+r.raw);xp(p,7,old,r.xp);clear(p);n++;
  }
  System.out.println("PRODUCTION_COOKING_ALL_BAKES_LEVEL_RANGE_CONSUMPTION_CONTAINERS_XP_OK "+n);n=0;
  for(CookingRecipes.Preparation r:CookingRecipes.preparations()){
   valid(r.product);Player p=actor();give(p,r.first,1);give(p,r.second,1);if(r.tool)give(p,1923,1);int old=p.playerXP[7];
   CookingRecipes.prepare(p,r);tick(2);ok(count(p,r.product)==1,"Preparation "+r.first+"+"+r.second+" -> "+r.product);if(r.extra>0)ok(count(p,r.extra)==1,"Preparation container "+r.product);xp(p,7,old,r.xp);clear(p);n++;
  }
  for(int water:new int[]{1921,1929,1937})for(int out:new int[]{2307,1953,2283,1863}){
   Player p=actor();give(p,1933,1);give(p,water,1);CookingRecipes.dough(p,water,out);tick(2);ok(count(p,out)==1&&count(p,1931)==1&&count(p,water)==0,"Dough/container "+out);clear(p);
  }
  Player p=actor();give(p,317,1);Cooking.cookThisFood(p,317,5249);p.absY++;tick(3);ok(count(p,317)==1&&count(p,315)==0,"Legacy fish movement");clear(p);
  p=actor();give(p,317,1);Cooking.cookThisFood(p,317,5249);p.stopPlayerSkill=false;tick(3);ok(count(p,317)==1,"Legacy fish cancellation");clear(p);
  p=actor();give(p,1889,1);give(p,995,27);for(int i=0;i<27;i++)give(p,1205,1);int old=p.playerXP[7];CookingRecipes.startBake(p,CookingRecipes.forRaw(1889),2728,1);tick(2);ok(count(p,1889)==1&&count(p,1891)==0&&p.playerXP[7]==old,"Full bag lost cake tin");clear(p);
  System.out.println("PRODUCTION_COOKING_PREPARATIONS_DOUGH_CONTAINERS_FULL_BAG_MOVEMENT_CANCEL_OK "+n);
 }
 static void herblore(){
  int n=0;for(PotionData.FinishedPotions r:PotionData.FinishedPotions.values()){
   valid(r.getResult().getId());Player p=actor();give(p,r.getPrimary().getId(),r.getPrimary().getAmount());for(GameItem item:r.getIngredients())give(p,item.getId(),item.getAmount());int old=p.playerXP[15];
   p.playerLevel[15]=r.getLevel()-1;p.getHerblore().mix(r.getPrimary().getId());tick(2);ok(count(p,r.getResult().getId())==0,"Potion level "+r);clear(p);
   p.playerLevel[15]=99;ok(p.getHerblore().itemOnItem(r.getIngredients()[0].getId(),r.getPrimary().getId()),"Potion real entry route "+r);tick(2);
   ok(count(p,r.getResult().getId())==r.getResult().getAmount()&&count(p,r.getPrimary().getId())==0,"Potion result/primary "+r);for(GameItem ingredient:r.getIngredients())ok(count(p,ingredient.getId())==0,"Potion ingredient "+r);xp(p,15,old,r.getExperience());clear(p);n++;
  }
  ok(PotionData.FinishedPotions.ATTACK.getResult().getId()==121&&PotionData.FinishedPotions.PRAYER.getExperience()==87.5&&PotionData.FinishedPotions.STAMINA.getPrimary().getId()==3018&&PotionData.FinishedPotions.STAMINA.getResult().getId()==12627,"Independent dose and XP references");
  for(PotionData.UnfinishedPotions r:PotionData.UnfinishedPotions.values()){
   Player p=actor();give(p,227,1);give(p,r.getHerb().getId(),1);ok(p.getHerblore().itemOnItem(227,r.getHerb().getId()),"Water-on-herb route");tick(2);ok(count(p,r.getPotion().getId())==1&&count(p,227)==0,"Unfinished "+r);clear(p);
  }
  Player p=actor();give(p,91,2);give(p,221,2);p.getHerblore().mix(91);p.getHerblore().mix(91);tick(2);ok(count(p,121)==1&&count(p,91)==1,"Repeated potion request doubled processing");clear(p);
  p=actor();give(p,91,1);give(p,221,1);p.getHerblore().mix(91);p.absX++;tick(2);ok(count(p,91)==1&&count(p,121)==0,"Potion movement");clear(p);
  System.out.println("PRODUCTION_HERBLORE_ALL_POTIONS_DOSES_XP_LEVEL_MATERIALS_ORIENTATION_UNFINISHED_MOVEMENT_DUPLICATE_OK "+n);
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","5");Server.startServerless();Configuration.BONUS_SKILL=-1;anvils();jewellery();cooking();herblore();System.out.println("PRODUCTION_REVIEW_ALL_CHECKS_OK");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
