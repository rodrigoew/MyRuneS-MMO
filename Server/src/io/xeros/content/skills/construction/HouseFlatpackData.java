package io.xeros.content.skills.construction;
import java.util.*;
/** Obtainable flatpack recipe facts from the historical 2022 references. */
public final class HouseFlatpackData {
 public static final class Recipe {
  public final int item,level,cookingLevel;public final double xp;public final String name,built,key,category;public final int[][] materials;
  Recipe(int item,int level,int cookingLevel,double xp,String name,String built,String key,String category,int[][] materials){this.item=item;this.level=level;this.cookingLevel=cookingLevel;this.xp=xp;this.name=name;this.built=built;this.key=key;this.category=category;this.materials=materials;}
 }
 private static final List<Recipe> DATA=new ArrayList<>();
 private static void row(int id,int level,int cookingLevel,double xp,String name,String built,String key,String category,int[][] materials){DATA.add(new Recipe(id,level,cookingLevel,xp,name,built,key,category,materials));}
 static {
  row(8496,1,0,58.0,"Crude chair (flatpack)","Crude wooden chair","CRUDE_WOODEN_CHAIR","Armchairs",new int[][]{{960,2},{4819,2}});
  row(8498,8,0,87.0,"Wooden chair (flatpack)","Wooden chair","WOODEN_CHAIR","Armchairs",new int[][]{{960,3},{4819,3}});
  row(8500,14,0,87.0,"Rocking chair (flatpack)","Rocking chair","ROCKING_CHAIR","Armchairs",new int[][]{{960,3},{4819,3}});
  row(8502,19,0,120.0,"Oak chair (flatpack)","Oak chair","OAK_CHAIR","Armchairs",new int[][]{{8778,2}});
  row(8504,26,0,180.0,"Oak armchair (flatpack)","Oak armchair","OAK_ARMCHAIR","Armchairs",new int[][]{{8778,3}});
  row(8506,35,0,180.0,"Teak armchair (flatpack)","Teak armchair","TEAK_ARMCHAIR","Armchairs",new int[][]{{8780,2}});
  row(8508,50,0,280.0,"Mahogany armchair (flatpack)","Mahogany armchair","MAHOGANY_ARMCHAIR","Armchairs",new int[][]{{8782,2}});
  row(8510,4,0,115.0,"Bookcase (flatpack)","Wooden bookcase","WOODEN_BOOKCASE","Bookcases",new int[][]{{960,4},{4819,4}});
  row(8512,29,0,180.0,"Oak bookcase (flatpack)","Oak bookcase","OAK_BOOKCASE","Bookcases",new int[][]{{8778,3}});
  row(8514,40,0,420.0,"Mahogany bookcase (flatpack)","Mahogany bookcase","MAHOGANY_BOOKCASE","Bookcases",new int[][]{{8782,3}});
  row(8516,7,5,87.0,"Beer barrel (flatpack)","Beer barrel","BEER_BARREL","Beer barrels",new int[][]{{960,3},{4819,3}});
  row(8518,12,14,91.0,"Cider barrel (flatpack)","Cider barrel","CIDER_BARREL","Beer barrels",new int[][]{{960,3},{5763,8},{4819,3}});
  row(8520,18,24,184.0,"Asgarnian ale (flatpack)","Asgarnian ale (barrel)","ASGARNIAN_ALE_BARREL","Beer barrels",new int[][]{{8778,3},{1905,8}});
  row(8522,26,29,184.0,"Greenman's ale (flatpack)","Greenman's ale (barrel)","GREENMANS_ALE_BARREL","Beer barrels",new int[][]{{8778,3},{1909,8}});
  row(8524,36,39,224.0,"Dragon bitter (flatpack)","Dragon bitter (barrel)","DRAGON_BITTER_BARREL","Beer barrels",new int[][]{{8778,3},{2353,2},{1911,8}});
  row(8526,48,44,224.0,"Chef's delight (flatpack)","Chef's delight (barrel)","CHEFS_DELIGHT_BARREL","Beer barrels",new int[][]{{8778,3},{2353,2},{5755,8}});
  row(8528,12,0,87.0,"Kitchen table (flatpack)","Kitchen table","WOODEN_KITCHEN_TABLE","Kitchen tables",new int[][]{{960,3},{4819,3}});
  row(8530,32,0,180.0,"Oak kitchen table (flatpack)","Oak kitchen table","OAK_KITCHEN_TABLE","Kitchen tables",new int[][]{{8778,3}});
  row(8532,52,0,270.0,"Teak kitchen table (flatpack)","Teak kitchen table","TEAK_KITCHEN_TABLE","Kitchen tables",new int[][]{{8780,3}});
  row(8548,10,0,115.0,"Wood dining table (flatpack)","Wood dining table","WOOD_DINING_TABLE","Dining tables",new int[][]{{960,4},{4819,4}});
  row(8550,22,0,240.0,"Oak dining table (flatpack)","Oak dining table","OAK_DINING_TABLE","Dining tables",new int[][]{{8778,4}});
  row(8552,31,0,360.0,"Carved oak table (flatpack)","Carved oak table","CARVED_OAK_TABLE","Dining tables",new int[][]{{8778,6}});
  row(8554,38,0,360.0,"Teak table (flatpack)","Teak table","TEAK_TABLE","Dining tables",new int[][]{{8780,4}});
  row(8556,45,0,600.0,"Carved teak table (flatpack)","Carved teak table","CARVED_TEAK_TABLE","Dining tables",new int[][]{{8780,6},{8790,4}});
  row(8558,52,0,840.0,"Mahogany table (flatpack)","Mahogany table","MAHOGANY_TABLE","Dining tables",new int[][]{{8782,6}});
  row(8560,72,0,3100.0,"Opulent table (flatpack)","Opulent table","OPULENT_TABLE","Dining tables",new int[][]{{8782,6},{8790,4},{8784,4},{8786,2}});
  row(8562,10,0,115.0,"Wooden bench (flatpack)","Wooden bench","WOODEN_BENCH","Dining benches",new int[][]{{960,4},{4819,4}});
  row(8564,22,0,240.0,"Oak bench (flatpack)","Oak bench","OAK_BENCH","Dining benches",new int[][]{{8778,4}});
  row(8566,31,0,240.0,"Carved oak bench (flatpack)","Carved oak bench","CARVED_OAK_BENCH","Dining benches",new int[][]{{8778,4}});
  row(8568,38,0,360.0,"Teak dining bench (flatpack)","Teak dining bench","TEAK_DINING_BENCH","Dining benches",new int[][]{{8780,4}});
  row(8570,44,0,360.0,"Carved teak bench (flatpack)","Carved teak bench","CARVED_TEAK_BENCH","Dining benches",new int[][]{{8780,4}});
  row(8572,52,0,560.0,"Mahogany bench (flatpack)","Mahogany bench","MAHOGANY_BENCH","Dining benches",new int[][]{{8782,4}});
  row(8574,61,0,1760.0,"Gilded bench (flatpack)","Gilded bench","GILDED_BENCH","Dining benches",new int[][]{{8782,4},{8784,4}});
  row(8576,20,0,117.0,"Wooden bed (flatpack)","Wooden bed","WOODEN_BED","Beds",new int[][]{{960,3},{8790,2},{4819,3}});
  row(8578,30,0,120.0,"Oak bed (flatpack)","Oak bed","OAK_BED","Beds",new int[][]{{8778,3},{8790,2}});
  row(8580,34,0,330.0,"Large oak bed (flatpack)","Large oak bed","LARGE_OAK_BED","Beds",new int[][]{{8778,5},{8790,2}});
  row(8582,40,0,300.0,"Teak bed (flatpack)","Teak bed","TEAK_BED","Beds",new int[][]{{8780,3},{8790,2}});
  row(8584,45,0,480.0,"Large teak bed (flatpack)","Large teak bed","LARGE_TEAK_BED","Beds",new int[][]{{8780,5},{8790,2}});
  row(8586,53,0,450.0,"Four-poster bed (flatpack)","4-poster","4_POSTER","Beds",new int[][]{{8782,3},{8790,2}});
  row(8588,60,0,1330.0,"Gilded four-poster (flatpack)","Gilded 4-poster","GILDED_4_POSTER","Beds",new int[][]{{8782,5},{8790,2},{8784,2}});
  row(8596,21,0,30.0,"Shaving stand (flatpack)","Shaving stand","SHAVING_STAND","Dressers",new int[][]{{960,1},{4819,1},{1775,1}});
  row(8598,29,0,61.0,"Oak shaving stand (flatpack)","Oak shaving stand","OAK_SHAVING_STAND","Dressers",new int[][]{{8778,1},{1775,1}});
  row(8600,37,0,121.0,"Oak dresser (flatpack)","Oak dresser","OAK_DRESSER","Dressers",new int[][]{{8778,2},{1775,1}});
  row(8602,46,0,181.0,"Teak dresser (flatpack)","Teak dresser","TEAK_DRESSER","Dressers",new int[][]{{8780,2},{1775,1}});
  row(8604,56,0,182.0,"Fancy teak dresser (flatpack)","Fancy teak dresser","FANCY_TEAK_DRESSER","Dressers",new int[][]{{8780,2},{1775,2}});
  row(8606,64,0,281.0,"Mahogany dresser (flatpack)","Mahogany dresser","MAHOGANY_DRESSER","Dressers",new int[][]{{8782,2},{1775,1}});
  row(8608,74,0,582.0,"Gilded dresser (flatpack)","Gilded dresser","GILDED_DRESSER","Dressers",new int[][]{{8782,2},{1775,2},{8784,1}});
  row(8610,20,0,58.0,"Shoe box (flatpack)","Shoe box","SHOE_BOX","Wardrobes",new int[][]{{960,2},{4819,2}});
  row(8612,27,0,120.0,"Oak drawers (flatpack)","Oak drawers","OAK_DRAWERS","Wardrobes",new int[][]{{8778,2}});
  row(8614,39,0,180.0,"Oak wardrobe (flatpack)","Oak wardrobe","OAK_WARDROBE","Wardrobes",new int[][]{{8778,3}});
  row(8616,51,0,180.0,"Teak drawers (flatpack)","Teak drawers","TEAK_DRAWERS","Wardrobes",new int[][]{{8780,2}});
  row(8618,63,0,270.0,"Teak wardrobe (flatpack)","Teak wardrobe","TEAK_WARDROBE","Wardrobes",new int[][]{{8780,3}});
  row(8620,75,0,420.0,"Mahogany wardrobe (flatpack)","Mahogany wardrobe","MAHOGANY_WARDROBE","Wardrobes",new int[][]{{8782,3}});
  row(8622,87,0,720.0,"Gilded wardrobe (flatpack)","Gilded wardrobe","GILDED_WARDROBE","Wardrobes",new int[][]{{8782,3},{8784,1}});
  row(8590,25,0,142.0,"Oak clock (flatpack)","Oak clock","OAK_CLOCK","Clocks",new int[][]{{8778,2},{8792,1}});
  row(8592,55,0,202.0,"Teak clock (flatpack)","Teak clock","TEAK_CLOCK","Clocks",new int[][]{{8780,2},{8792,1}});
  row(8594,85,0,602.0,"Gilded clock (flatpack)","Gilded clock","GILDED_CLOCK","Clocks",new int[][]{{8782,2},{8792,1},{8784,1}});
  row(9843,54,0,240.0,"Oak cape rack (flatpack)","Oak cape rack","OAK_CAPE_RACK","Cape racks",new int[][]{{8778,4}});
  row(9844,63,0,360.0,"Teak cape rack (flatpack)","Teak cape rack","TEAK_CAPE_RACK","Cape racks",new int[][]{{8780,4}});
  row(9845,72,0,560.0,"Mahogany cape rack (flatpack)","Mahogany cape rack","MAHOGANY_CAPE_RACK","Cape racks",new int[][]{{8782,4}});
  row(9846,81,0,860.0,"Gilded cape rack (flatpack)","Gilded cape rack","GILDED_CAPE_RACK","Cape racks",new int[][]{{8782,4},{8784,1}});
  row(9847,90,0,500.0,"Marble cape rack (flatpack)","Marble cape rack","MARBLE_CAPE_RACK","Cape racks",new int[][]{{8786,1}});
  row(9848,99,0,1000.0,"Magic cape rack (flatpack)","Magic cape rack","MAGIC_CAPE_RACK","Cape racks",new int[][]{{8788,1}});
  row(9852,42,0,240.0,"Oak magic wardrobe (flatpack)","Oak magic wardrobe","OAK_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8778,4}});
  row(9853,51,0,360.0,"Carved oak magic wardrobe (flatpack)","Carved oak magic wardrobe","CARVED_OAK_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8778,6}});
  row(9854,60,0,360.0,"Teak magic wardrobe (flatpack)","Teak magic wardrobe","TEAK_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8780,4}});
  row(9855,69,0,540.0,"Carved teak magic wardrobe (flatpack)","Carved teak magic wardrobe","CARVED_TEAK_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8780,6}});
  row(9856,78,0,560.0,"Mahogany magic wardrobe (flatpack)","Mahogany magic wardrobe","MAHOGANY_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8782,4}});
  row(9857,87,0,860.0,"Gilded magic wardrobe (flatpack)","Gilded magic wardrobe","GILDED_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8782,4},{8784,1}});
  row(9858,96,0,500.0,"Marble magic wardrobe (flatpack)","Marble magic wardrobe","MARBLE_MAGIC_WARDROBE","Magic wardrobes",new int[][]{{8786,1}});
  row(9859,46,0,180.0,"Oak armour case (flatpack)","Oak armour case","OAK_ARMOUR_CASE","Armour cases",new int[][]{{8778,3}});
  row(9860,64,0,270.0,"Teak armour case (flatpack)","Teak armour case","TEAK_ARMOUR_CASE","Armour cases",new int[][]{{8780,3}});
  row(9861,82,0,420.0,"Mahogany armour case (flatpack)","Mahogany armour case","MAHOGANY_ARMOUR_CASE","Armour cases",new int[][]{{8782,3}});
  row(9862,48,0,120.0,"Oak treasure chest (flatpack)","Oak treasure chest","OAK_TREASURE_CHEST","Treasure chests",new int[][]{{8778,2}});
  row(9863,66,0,180.0,"Teak treasure chest (flatpack)","Teak treasure chest","TEAK_TREASURE_CHEST","Treasure chests",new int[][]{{8780,2}});
  row(9864,84,0,280.0,"M. treasure chest (flatpack)","Mahogany treasure chest","MAHOGANY_TREASURE_CHEST","Treasure chests",new int[][]{{8782,2}});
  row(9865,44,0,120.0,"Oak fancy dress box (flatpack)","Oak fancy dress box","OAK_FANCY_DRESS_BOX","Costume boxes",new int[][]{{8778,2}});
  row(9866,62,0,180.0,"Teak fancy dress box (flatpack)","Teak fancy dress box","TEAK_FANCY_DRESS_BOX","Costume boxes",new int[][]{{8780,2}});
  row(9867,80,0,280.0,"Mahogany fancy dress box (flatpack)","Mahogany fancy dress box","MAHOGANY_FANCY_DRESS_BOX","Costume boxes",new int[][]{{8782,2}});
  row(9849,50,0,120.0,"Oak toy box (flatpack)","Oak toy box","OAK_TOY_BOX","Toy boxes",new int[][]{{8778,2}});
  row(9850,68,0,180.0,"Teak toy box (flatpack)","Teak toy box","TEAK_TOY_BOX","Toy boxes",new int[][]{{8780,2}});
  row(9851,86,0,280.0,"Mahogany toy box (flatpack)","Mahogany toy box","MAHOGANY_TOY_BOX","Toy boxes",new int[][]{{8782,2}});
 }
 public static List<Recipe> all(){return Collections.unmodifiableList(DATA);}
 public static Recipe get(int item){for(Recipe r:DATA)if(r.item==item)return r;return null;}
 public static int limit(String bench){switch(bench){case "WOODEN_WORKBENCH":return 20;case "OAK_WORKBENCH":return 40;case "STEEL_FRAMED_WORKBENCH":return 60;case "BENCH_WITH_VICE":return 80;case "BENCH_WITH_LATHE":return 99;default:return 0;}}
 private HouseFlatpackData(){}
}
