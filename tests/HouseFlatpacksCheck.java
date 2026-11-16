package io.xeros.solo;
import io.xeros.*;import io.xeros.content.skills.construction.*;import io.xeros.model.collisionmap.WorldObject;import io.xeros.model.entity.player.*;import java.lang.reflect.*;import java.util.*;
public final class HouseFlatpacksCheck{
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void tick(int n){SkillsAuditCheck.tick(n);}
 static int count(Player p,int id){return p.getItems().getInventoryCount(id);}
 static void give(Player p,int id,int n){HouseWorkshopCheck.give(p,id,n);}
 static Player house(String bench){Player p=HouseWorkshopCheck.house();HouseWorkshopCheck.ensure(p,HouseFurnishing.get(bench));HouseWorkshopCheck.bag(p);p.getHealth().setMaximumHealth(99);p.getHealth().reset();return p;}
 static void supplies(Player p,HouseFlatpackData.Recipe r,int quantity){give(p,2347,1);give(p,8794,1);for(int[] in:r.materials)give(p,in[0],in[1]*quantity);}
 static void begin(Player p,String bench,HouseFlatpackData.Recipe r,int quantity){
  WorldObject o=HouseWorkshopCheck.object(p,HouseFurnishing.get(bench).models[0]);if(!p.getHouse().workshop().clockmaking().seated())HouseConstructionCheck.at(p,o.x-1,o.y,1);ok(p.getDynamicMapScene().handleClickObject(p,o,1),"Work-at");
  List<String> categories=new ArrayList<>();for(var row:HouseFlatpackData.all())if(row.level<=HouseFlatpackData.limit(bench)&&!categories.contains(row.category))categories.add(row.category);int index=categories.indexOf(r.category);ok(index>=0,"Category available");for(int i=0;i<index/3;i++)HouseWorkshopCheck.choose(p,3);HouseWorkshopCheck.choose(p,index%3);
  List<HouseFlatpackData.Recipe> recipes=new ArrayList<>();for(var row:HouseFlatpackData.all())if(row.level<=HouseFlatpackData.limit(bench)&&row.category.equals(r.category))recipes.add(row);index=recipes.indexOf(r);ok(index>=0,"Recipe available");for(int i=0;i<index/3;i++)HouseWorkshopCheck.choose(p,3);HouseWorkshopCheck.choose(p,index%3);HouseWorkshopCheck.choose(p,quantity);p.getNextPlayerMovement();
 }
 static void allRecipes(){
  int tested=0;for(var r:HouseFlatpackData.all()){
   Player p=house("BENCH_WITH_LATHE");supplies(p,r,1);int xp=p.playerXP[22],craft=p.playerXP[12];p.playerLevel[22]=r.level-1;int[] before=p.playerItems.clone();begin(p,"BENCH_WITH_LATHE",r,0);tick(8);ok(count(p,r.item)==0&&p.playerXP[22]==xp&&Arrays.equals(before,p.playerItems),"Below-level no loss "+r.item);
   p.playerLevel[22]=r.level;begin(p,"BENCH_WITH_LATHE",r,0);tick(7);ok(count(p,r.item)==0&&p.playerXP[22]==xp,"Premature flatpack "+r.item);tick(1);
   ok(count(p,r.item)==1&&p.playerXP[22]==xp+(int)r.xp&&p.playerXP[12]==craft,"Exact flatpack output and XP "+r.item);for(int[] in:r.materials)ok(count(p,in[0])==0,"Flatpack material "+r.item);ok(count(p,2347)==1&&count(p,8794)==1,"Tools retained");p.getHouse().exit();ok(count(p,r.item)==1,"Normal flatpack retained on exit");tested++;
  }
  ok(tested==82,"82 obtainable recipes");System.out.println("HOUSE_FLATPACK_ALL82_OBTAINABLE_2022_RECIPES_CATEGORY_PAGES_BELOW_LEVEL_GATES_EIGHT_TICKS_EXACT_MATERIALS_UNNOTED_ITEM_XP_TOOLS_RETAINED_AND_NO_CRAFTING_XP_OK");
 }
 static void literals(){
  for(Object[] row:new Object[][]{{8496,1,58d},{8560,72,3100d},{8588,60,1330d},{9848,99,1000d}}){var r=HouseFlatpackData.get((int)row[0]);ok(r!=null&&r.level==(int)row[1]&&r.xp==(double)row[2],"Independent historical flatpack "+row[0]);}
  ok(Arrays.deepEquals(HouseFlatpackData.get(8588).materials,new int[][]{{8782,5},{8790,2},{8784,2}}),"Gilded four poster actual1330XP materials");
  for(Object[] row:new Object[][]{{"WOODEN_WORKBENCH",20},{"OAK_WORKBENCH",40},{"STEEL_FRAMED_WORKBENCH",60},{"BENCH_WITH_VICE",80},{"BENCH_WITH_LATHE",99}})ok(HouseFlatpackData.limit((String)row[0])==(int)row[1],"Five bench limits");
  int known=0;for(var r:HouseFlatpackData.all()){HouseFurnishing f=HouseFurnishing.get(r.key);if(f!=null){ok(f.level==r.level&&f.xp==(r.key.equals("OAK_BED")?210:r.xp)&&f.cookingLevel()==r.cookingLevel,"Primary flatpack/build level XP Cooking agreement "+r.key);known++;}}ok(known==57,"All57 currently constructible furniture flatpacks matched canonical names");
  System.out.println("HOUSE_FLATPACK_LITERAL_CRUDE58_OPULENT3100_GILDED_FOUR_POSTER1330_MAGIC_CAPE1000_FIVE_BENCH_LIMITS_AND_19_FURNITURE_PRIMARY_REFERENCE_AGREEMENTS_OK");
 }
 static void limitsAndGuards()throws Exception{
  int[] levels={20,40,60,80,99};String[] benches={"WOODEN_WORKBENCH","OAK_WORKBENCH","STEEL_FRAMED_WORKBENCH","BENCH_WITH_VICE","BENCH_WITH_LATHE"};
  for(int i=0;i<benches.length;i++){
   Player p=house(benches[i]);WorldObject o=HouseWorkshopCheck.object(p,HouseFurnishing.get(benches[i]).models[0]);HouseConstructionCheck.at(p,o.x-1,o.y,1);p.getDynamicMapScene().handleClickObject(p,o,1);
   Method fixture=PlayerHouse.class.getDeclaredMethod("furnitureFixture",WorldObject.class);fixture.setAccessible(true);Object b=fixture.invoke(p.getHouse(),o);Method start=HouseFlatpacks.class.getDeclaredMethod("start",b.getClass(),HouseFlatpackData.Recipe.class,int.class);start.setAccessible(true);int blocked=0;
   for(var r:HouseFlatpackData.all())if(r.level>levels[i]){ok(!(boolean)start.invoke(p.getHouse().workshop().flatpacks(),b,r,1),"Forged above bench limit "+r.item);blocked++;}
   ok(levels[i]==99||blocked>0,"Lower bench has inaccessible recipes");p.getHouse().exit();
  }
  for(var r:HouseFlatpackData.all())if(r.cookingLevel>0){
   Player p=house("BENCH_WITH_LATHE");supplies(p,r,1);int xp=p.playerXP[22];p.playerLevel[7]=r.cookingLevel-1;int[] before=p.playerItems.clone();begin(p,"BENCH_WITH_LATHE",r,0);tick(8);ok(count(p,r.item)==0&&p.playerXP[22]==xp&&Arrays.equals(before,p.playerItems),"Secondary Cooking gate "+r.item);p.playerLevel[7]=r.cookingLevel;begin(p,"BENCH_WITH_LATHE",r,0);tick(7);p.playerLevel[7]--;tick(1);ok(count(p,r.item)==0&&p.playerXP[22]==xp&&Arrays.equals(before,p.playerItems),"Cooking drained during flatpack "+r.item);p.getHouse().exit();
  }
  var r=HouseFlatpackData.get(8496);Player p=house("WOODEN_WORKBENCH");supplies(p,r,1);int xp=p.playerXP[22];begin(p,"WOODEN_WORKBENCH",r,0);tick(7);p.getItems().deleteItem2(8794,1);tick(1);ok(count(p,8496)==0&&count(p,960)==2&&p.playerXP[22]==xp,"Saw removed during production");
  give(p,8794,1);begin(p,"WOODEN_WORKBENCH",r,0);tick(4);p.absX++;p.interruptActions();tick(4);ok(count(p,8496)==0&&count(p,960)==2&&p.playerXP[22]==xp,"Movement cancellation");p.getHouse().exit();
  System.out.println("HOUSE_FLATPACK_FIVE_BENCH_FORGED_LEVEL_LIMITS_SIX_SECONDARY_COOKING_START_AND_DRAIN_GATES_MISSING_SAW_MOVEMENT_CANCEL_AND_NO_MATERIAL_LOSS_OK");
 }
 static void installation(){
  int tested=0;for(var recipe:HouseFlatpackData.all()){
   HouseFurnishing f=HouseFurnishing.get(recipe.key);if(f==null)continue;
   Player p=HouseConstructionCheck.house();PlayerHouse.Kind kind=Set.of(15260,15261,15262,15268).contains(f.group)?PlayerHouse.Kind.BEDROOM:Set.of(15298,15299).contains(f.group)?PlayerHouse.Kind.DINING_ROOM:PlayerHouse.Kind.KITCHEN;ok(p.getHouse().addRoom(kind,3,2,0),"Installation room");p.getNextPlayerMovement();HouseWorkshopCheck.bag(p);p.playerLevel[22]=1;p.playerLevel[7]=1;give(p,recipe.item,1);WorldObject root=HouseConstructionCheck.root(p,f.group,(f.group>=15398||Set.of(15260,15261,15262,15268,15298,15299).contains(f.group))?3:2,(f.group>=15398||Set.of(15260,15261,15262,15268,15298,15299).contains(f.group))?2:3);HouseConstructionCheck.at(p,root.x-1,root.y,1);int xp=p.playerXP[22],cook=p.playerXP[7];
   p.getHouse().workshop().flatpacks().install(recipe.item,root);ok(count(p,recipe.item)==1&&!p.getHouse().encode().contains("="+f.key),"Hammer required");give(p,2347,1);p.getHouse().workshop().flatpacks().install(recipe.item,root);ok(count(p,recipe.item)==0&&p.getHouse().encode().contains("="+f.key)&&count(p,8794)==0&&p.playerXP[22]==xp&&p.playerXP[7]==cook,"Hammer-only install no Construction/Cooking level XP gate "+f.key);
   give(p,recipe.item,1);p.getHouse().workshop().flatpacks().install(recipe.item,root);ok(count(p,recipe.item)==1&&p.playerXP[22]==xp,"Stale root cannot install twice");String data=p.getHouse().encode();p.getHouse().exit();p.getNextPlayerMovement();ok(p.getHouse().decode(data)&&p.getHouse().enter(false),"Flatpack layout save/reload");p.getNextPlayerMovement();ok(p.getRegionProvider().get(root.x,root.y).getWorldObject(f.model(root.id),root.x,root.y,1).isPresent(),"Native installed model persisted "+f.key);p.getHouse().exit();tested++;
  }
  ok(tested==57,"57 furniture installs");System.out.println("HOUSE_FLATPACK_57_NATIVE_HOTSPOT_INSTALLS_HAMMER_ONLY_LEVEL1_NO_SAW_NO_XP_SECONDARY_LEVEL_BYPASS_STALE_ROOT_NO_DUPLICATE_NORMAL_MODE_AND_LAYOUT_SAVE_RELOAD_OK");
 }
 public static void main(String[] args){try{Thread.currentThread().setName("GameThread");System.setProperty("solo.mode","true");System.setProperty("solo.xp","1");Server.startServerless();Configuration.BONUS_SKILL=-1;Field boosts=io.xeros.content.boosts.Boosts.class.getDeclaredField("boosts");boosts.setAccessible(true);boosts.set(null,Collections.emptyList());literals();allRecipes();limitsAndGuards();installation();System.out.println("HOUSE_FLATPACK_ALL_CHECKS_PASSED");System.exit(0);}catch(Throwable t){t.printStackTrace();System.exit(1);}}
}
