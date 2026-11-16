package io.xeros.content.skills.construction;

/** Native workshop identifiers and historical construction requirements. */
public final class HouseWorkshopData {
 private HouseWorkshopData(){}
 static void register(){
  row("WOODEN_WORKBENCH",15439,17,143,new int[]{6791,6806},new int[][]{{960,5},{4819,5}});
  row("OAK_WORKBENCH",15439,32,300,new int[]{6792,6807},new int[][]{{8778,5}});
  row("STEEL_FRAMED_WORKBENCH",15439,46,440,new int[]{6793,6807},new int[][]{{8778,6},{2353,4}});
  row("BENCH_WITH_VICE",15439,62,140,new int[]{6794,6807},new int[][]{{8778,2},{2353,1}});
  row("BENCH_WITH_LATHE",15439,77,140,new int[]{6795,6807},new int[][]{{8778,2},{2353,1}});
  row("CRAFTING_TABLE_1",15441,16,240,new int[]{6796,6807},new int[][]{{8778,4}});
  row("CRAFTING_TABLE_2",15441,25,1,new int[]{6797,6807},new int[][]{{1775,1}});
  row("CRAFTING_TABLE_3",15441,34,2,new int[]{6798,6807},new int[][]{{1775,2}});
  row("CRAFTING_TABLE_4",15441,42,120,new int[]{6799,6807},new int[][]{{8778,2}});
  row("TOOL_STORE_1",15443,15,120,new int[]{6786,-1,-1,-1,-1},new int[][]{{8778,2}});
  row("TOOL_STORE_2",15443,25,120,new int[]{6786,6787,-1,-1,-1},new int[][]{{8778,2}});
  row("TOOL_STORE_3",15443,35,120,new int[]{6786,6787,6788,-1,-1},new int[][]{{8778,2}});
  row("TOOL_STORE_4",15443,44,120,new int[]{6786,6787,6788,6789,-1},new int[][]{{8778,2}});
  row("TOOL_STORE_5",15443,55,120,new int[]{6786,6787,6788,6789,6790},new int[][]{{8778,2}});
  row("REPAIR_BENCH",15448,15,120,new int[]{6800},new int[][]{{8778,2}});
  row("WHETSTONE",15448,35,260,new int[]{6801},new int[][]{{8778,4},{3420,1}});
  row("ARMOUR_STAND",15448,55,500,new int[]{6802},new int[][]{{8778,8},{3420,1}});
  row("PLUMING_STAND",15450,16,120,new int[]{6803},new int[][]{{8778,2}});
  row("SHIELD_EASEL",15450,41,240,new int[]{6804},new int[][]{{8778,4}});
  row("BANNER_EASEL",15450,66,510,new int[]{6805},new int[][]{{8778,8},{8790,2}});
 }
 private static void row(String key,int group,int level,double xp,int[] models,int[][] materials){HouseFurnishing.row(key,group,level,xp,false,models,materials);}
 public static String prerequisite(String key){
  switch(key){
   case "BENCH_WITH_VICE":return "STEEL_FRAMED_WORKBENCH";
   case "BENCH_WITH_LATHE":return "BENCH_WITH_VICE";
   case "CRAFTING_TABLE_2":return "CRAFTING_TABLE_1";
   case "CRAFTING_TABLE_3":return "CRAFTING_TABLE_2";
   case "CRAFTING_TABLE_4":return "CRAFTING_TABLE_3";
   case "TOOL_STORE_2":return "TOOL_STORE_1";
   case "TOOL_STORE_3":return "TOOL_STORE_2";
   case "TOOL_STORE_4":return "TOOL_STORE_3";
   case "TOOL_STORE_5":return "TOOL_STORE_4";
   default:return null;
  }
 }
 public static int[] tools(int tier){
  switch(tier){
   case 0:return new int[]{8794,2347,1755,1735};
   case 1:return new int[]{1925,946,952,590};
   case 2:return new int[]{1757,1785,1733};
   case 3:return new int[]{1595,1597,1592,1599,11065,5523};
   case 4:return new int[]{5341,952,5325,5343,5331,5329};
   default:return new int[0];
  }
 }
}
