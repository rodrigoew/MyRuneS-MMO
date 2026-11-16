package io.xeros.content.skills.construction;
import java.util.*;
/** Native furniture facts; implementation and scene handling are independent. */
public final class HouseFurnishing {
 public final String key;public final int group,level;public final double xp;public final int[] models;public final int[][] materials;public final boolean plant;
 private HouseFurnishing(String key,int group,int level,double xp,boolean plant,int[] models,int[][] materials){this.key=key;this.group=group;this.level=level;this.xp=xp;this.plant=plant;this.models=models;this.materials=materials;}
 private static final Map<String,HouseFurnishing> DATA=new LinkedHashMap<>();
 static void row(String key,int group,int level,double xp,boolean plant,int[] models,int[][] materials){if(DATA.put(key,new HouseFurnishing(key,group,level,xp,plant,models,materials))!=null)throw new IllegalStateException(key);}
 static {
  row("EXIT_PORTAL",15361,1,100.0,false,new int[]{4525},new int[][]{{2353,10}});
  row("DECORATIVE_ROCK",15361,5,100.0,false,new int[]{4526},new int[][]{{3420,5}});
  row("POND",15361,10,100.0,false,new int[]{4527},new int[][]{{1761,5}});
  row("IMP_STATUE",15361,15,150.0,false,new int[]{4528},new int[][]{{3420,5},{1761,5}});
  row("BIG_DEAD_TREE",15362,5,31.0,true,new int[]{4531},new int[][]{{8417,1}});
  row("BIG_NICE_TREE",15362,10,44.0,true,new int[]{4532},new int[][]{{8419,1}});
  row("BIG_OAK_TREE",15362,15,70.0,true,new int[]{4533},new int[][]{{8421,1}});
  row("BIG_WILLOW_TREE",15362,30,100.0,true,new int[]{4534},new int[][]{{8423,1}});
  row("BIG_MAPLE_TREE",15362,45,122.0,true,new int[]{4535},new int[][]{{8425,1}});
  row("BIG_YEW_TREE",15362,60,141.0,true,new int[]{4536},new int[][]{{8427,1}});
  row("BIG_MAGIC_TREE",15362,75,223.0,true,new int[]{4537},new int[][]{{8429,1}});
  row("SMALL_DEAD_TREE",15363,5,31.0,true,new int[]{4538},new int[][]{{8417,1}});
  row("SMALL_NICE_TREE",15363,10,44.0,true,new int[]{4539},new int[][]{{8419,1}});
  row("SMALL_OAK_TREE",15363,15,70.0,true,new int[]{4540},new int[][]{{8421,1}});
  row("SMALL_WILLOW_TREE",15363,30,100.0,true,new int[]{4541},new int[][]{{8423,1}});
  row("SMALL_MAPLE_TREE",15363,45,122.0,true,new int[]{5126},new int[][]{{8425,1}});
  row("SMALL_YEW_TREE",15363,60,141.0,true,new int[]{5121},new int[][]{{8427,1}});
  row("SMALL_MAGIC_TREE",15363,75,223.0,true,new int[]{5127},new int[][]{{8429,1}});
  row("FERN",15364,1,31.0,true,new int[]{5136},new int[][]{{8431,1}});
  row("BUSH",15364,6,70.0,true,new int[]{5129},new int[][]{{8433,1}});
  row("TALL_PLANT",15364,12,100.0,true,new int[]{5130},new int[][]{{8435,1}});
  row("SHORT_PLANT",15365,1,31.0,true,new int[]{5131},new int[][]{{8431,1}});
  row("LARGE_LEAF_BUSH",15365,6,70.0,true,new int[]{5132},new int[][]{{8433,1}});
  row("HUGE_PLANT",15365,12,100.0,true,new int[]{5133},new int[][]{{8435,1}});
  row("PLANT",15366,1,31.0,true,new int[]{5134},new int[][]{{8431,1}});
  row("SMALLER_FERN",15366,6,70.0,true,new int[]{5135},new int[][]{{8433,1}});
  row("SMALL_FERN",15366,12,100.0,true,new int[]{5136},new int[][]{{8435,1}});
  row("DOCK_LEAF",15367,1,31.0,true,new int[]{5137},new int[][]{{8431,1}});
  row("THISTLE",15367,6,70.0,true,new int[]{5138},new int[][]{{8433,1}});
  row("REEDS",15367,12,100.0,true,new int[]{5139},new int[][]{{8435,1}});
  row("CRUDE_WOODEN_CHAIR",4515,1,58.0,false,new int[]{6752},new int[][]{{960,2},{4819,2}});
  row("WOODEN_CHAIR",4515,8,87.0,false,new int[]{6753},new int[][]{{960,3},{4819,3}});
  row("ROCKING_CHAIR",4515,14,87.0,false,new int[]{6754},new int[][]{{960,3},{4819,3}});
  row("OAK_CHAIR",4515,19,120.0,false,new int[]{6755},new int[][]{{8778,2}});
  row("OAK_ARMCHAIR",4515,26,180.0,false,new int[]{6756},new int[][]{{8778,3}});
  row("TEAK_ARMCHAIR",4515,35,180.0,false,new int[]{6757},new int[][]{{8780,2}});
  row("MAHOGANY_ARMCHAIR",4515,50,280.0,false,new int[]{6758},new int[][]{{8782,2}});
  row("BROWN_RUG",4518,2,30.0,false,new int[]{6761,6760,6759},new int[][]{{8790,2}});
  row("RUG",4518,13,60.0,false,new int[]{6764,6763,6762},new int[][]{{8790,4}});
  row("OPULENT_RUG",4518,65,360.0,false,new int[]{6767,6766,6765},new int[][]{{8790,4},{8784,1}});
  row("WOODEN_BOOKCASE",4521,4,115.0,false,new int[]{6768},new int[][]{{960,4},{4819,4}});
  row("OAK_BOOKCASE",4521,29,180.0,false,new int[]{6769},new int[][]{{8778,3}});
  row("MAHOGANY_BOOKCASE",4521,40,420.0,false,new int[]{6770},new int[][]{{8782,3}});
  row("CLAY_FIREPLACE",4523,3,30.0,false,new int[]{6780},new int[][]{{1761,3}});
  row("STONE_FIREPLACE",4523,33,40.0,false,new int[]{6782},new int[][]{{3420,2}});
  row("MARBLE_FIREPLACE",4523,63,500.0,false,new int[]{6784},new int[][]{{8786,1}});
  row("TORN_CURTAINS",4524,2,132.0,false,new int[]{6774},new int[][]{{960,3},{8790,3},{4819,3}});
  row("CURTAINS",4524,18,225.0,false,new int[]{6775},new int[][]{{8778,3},{8790,3}});
  row("OPULENT_CURTAINS",4524,40,315.0,false,new int[]{6776},new int[][]{{8780,3},{8790,3}});
  row("FIREPIT",15398,5,40.0,false,new int[]{13528},new int[][]{{2353,1},{1761,2}});
  row("FIREPIT_WITH_HOOK",15398,11,60.0,false,new int[]{13529},new int[][]{{2353,2},{1761,2}});
  row("FIREPIT_WITH_POT",15398,17,80.0,false,new int[]{13531},new int[][]{{2353,3},{1761,2}});
  row("SMALL_OVEN",15398,24,80.0,false,new int[]{13533},new int[][]{{2353,4}});
  row("LARGE_OVEN",15398,29,100.0,false,new int[]{13536},new int[][]{{2353,5}});
  row("STEEL_RANGE",15398,34,120.0,false,new int[]{13539},new int[][]{{2353,6}});
  row("FANCY_RANGE",15398,42,160.0,false,new int[]{13542},new int[][]{{2353,8}});
  row("WOODEN_SHELVES_1",15399,6,87.0,false,new int[]{13545,13552},new int[][]{{960,3},{4819,3}});
  row("WOODEN_SHELVES_2",15399,12,147.0,false,new int[]{13546,13553},new int[][]{{960,3},{4819,3},{1761,6}});
  row("WOODEN_SHELVES_3",15399,23,147.0,false,new int[]{13547,13554},new int[][]{{960,3},{4819,3},{1761,6}});
  row("OAK_SHELVES_1",15399,34,240.0,false,new int[]{13548,13555},new int[][]{{8778,3},{1761,6}});
  row("OAK_SHELVES_2",15399,45,240.0,false,new int[]{13549,13556},new int[][]{{8778,3},{1761,6}});
  row("TEAK_SHELVES_1",15399,56,330.0,false,new int[]{13550,13557},new int[][]{{8780,3},{1761,6}});
  row("TEAK_SHELVES_2",15399,67,930.0,false,new int[]{13551,13558},new int[][]{{8780,3},{8784,2},{1761,6}});
  row("BEER_BARREL",15401,7,87.0,false,new int[]{13568},new int[][]{{960,3},{4819,3}});
  row("CIDER_BARREL",15401,12,91.0,false,new int[]{13569},new int[][]{{960,3},{4819,3},{5763,8}});
  row("ASGARNIAN_ALE_BARREL",15401,18,184.0,false,new int[]{13570},new int[][]{{8778,3},{1905,8}});
  row("GREENMANS_ALE_BARREL",15401,26,184.0,false,new int[]{13571},new int[][]{{8778,3},{1909,8}});
  row("DRAGON_BITTER_BARREL",15401,36,224.0,false,new int[]{13572},new int[][]{{8778,3},{1911,8},{2353,2}});
  row("CHEFS_DELIGHT_BARREL",15401,48,224.0,false,new int[]{13573},new int[][]{{8778,3},{5755,8},{2353,2}});
  row("CAT_BLANKET",15402,5,15.0,false,new int[]{13574},new int[][]{{8790,2}});
  row("CAT_BASKET",15402,19,58.0,false,new int[]{13575},new int[][]{{960,2},{4819,2}});
  row("CAT_CUSHIONED_BASKET",15402,33,58.0,false,new int[]{13576},new int[][]{{960,2},{4819,2},{1737,2}});
  row("WOODEN_LARDER",15403,9,228.0,false,new int[]{13565},new int[][]{{960,8},{4819,8}});
  row("OAK_LARDER",15403,33,480.0,false,new int[]{13566},new int[][]{{8778,8}});
  row("TEAK_LARDER",15403,43,750.0,false,new int[]{13567},new int[][]{{8780,8},{8790,2}});
  row("PUMP_AND_DRAIN",15404,7,100.0,false,new int[]{13559},new int[][]{{2353,5}});
  row("PUMP_AND_TUB",15404,27,200.0,false,new int[]{13561},new int[][]{{2353,10}});
  row("SINK",15404,47,300.0,false,new int[]{13563},new int[][]{{2353,15}});
  row("WOODEN_KITCHEN_TABLE",15405,12,87.0,false,new int[]{13577},new int[][]{{960,3},{4819,3}});
  row("OAK_KITCHEN_TABLE",15405,32,180.0,false,new int[]{13578},new int[][]{{8778,3}});
  row("TEAK_KITCHEN_TABLE",15405,52,270.0,false,new int[]{13579},new int[][]{{8780,3}});
  row("GOLD_SINK",15404,47,11144.0,false,new int[]{42852},new int[][]{{26266,10},{8782,5},{8784,5}});
  HouseWorkshopData.register();
  HouseChapelData.register();
  HouseStudyData.register();
  HouseExpansionData.register();
 }
 public int cookingLevel(){switch(key){case "BEER_BARREL":return 5;case "CIDER_BARREL":return 14;case "ASGARNIAN_ALE_BARREL":return 24;case "GREENMANS_ALE_BARREL":return 29;case "DRAGON_BITTER_BARREL":return 39;case "CHEFS_DELIGHT_BARREL":return 44;default:return 0;}}
 public static HouseFurnishing get(String key){return DATA.get(key);}
 public static Collection<HouseFurnishing> all(){return Collections.unmodifiableCollection(DATA.values());}
 public static int group(int root){if(root==15300)return 15299;if(root==15301||root==15267)return 4523;if(root==15302||root==15263)return 4524;if(root>=15264&&root<=15266)return 4518;if(root==15273||root==15274)return 15272;if(root==13728||root==13732||root==13729||root==13730||root==13731||root==27070)return 13733;if(root==4516||root==4517)return 4515;if(root==4519||root==4520)return 4518;if(root==15400)return 15399;if(root==15440)return 15439;if(root==15442)return 15441;if(root>=15444&&root<=15447)return 15443;return root;}
 public static int slot(int root){return root==4516||root==4517||root==15300?root:group(root);}
 public int model(int root){int index=group==15272?root-15272:group==4518?(root>=15264&&root<=15266?root-15264:root-4518):group==15399?root-15399:group==15439||group==15441||group==15443?root-group:0;return models[Math.max(0,Math.min(index,models.length-1))];}
 public String prerequisite(){String extra=HouseExpansionData.prerequisite(key);return extra==null?HouseWorkshopData.prerequisite(key):extra;}
 public String name(){String s=key.toLowerCase().replace('_',' ');return Character.toUpperCase(s.charAt(0))+s.substring(1);}
}
