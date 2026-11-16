package io.xeros.content.skills.construction;
/** Additional native room furniture; recipe requirements reuse the validated flatpack catalogue. */
final class HouseExpansionData {
 static void register(){
  flatpack("WOOD_DINING_TABLE",15298,13293);
  flatpack("OAK_DINING_TABLE",15298,13294);
  flatpack("CARVED_OAK_TABLE",15298,13295);
  flatpack("TEAK_TABLE",15298,13296);
  flatpack("CARVED_TEAK_TABLE",15298,13297);
  flatpack("MAHOGANY_TABLE",15298,13298);
  flatpack("OPULENT_TABLE",15298,13299);
  flatpack("WOODEN_BENCH",15299,13300);
  flatpack("OAK_BENCH",15299,13301);
  flatpack("CARVED_OAK_BENCH",15299,13302);
  flatpack("TEAK_DINING_BENCH",15299,13303);
  flatpack("CARVED_TEAK_BENCH",15299,13304);
  flatpack("MAHOGANY_BENCH",15299,13305);
  flatpack("GILDED_BENCH",15299,13306);
  flatpack("WOODEN_BED",15260,13148);
  flatpack("OAK_BED",15260,13149);
  flatpack("LARGE_OAK_BED",15260,13150);
  flatpack("TEAK_BED",15260,13151);
  flatpack("LARGE_TEAK_BED",15260,13152);
  flatpack("4_POSTER",15260,13153);
  flatpack("GILDED_4_POSTER",15260,13154);
  flatpack("SHOE_BOX",15261,13155);
  flatpack("OAK_DRAWERS",15261,13156);
  flatpack("OAK_WARDROBE",15261,13157);
  flatpack("TEAK_DRAWERS",15261,13158);
  flatpack("TEAK_WARDROBE",15261,13159);
  flatpack("MAHOGANY_WARDROBE",15261,13160);
  flatpack("GILDED_WARDROBE",15261,13161);
  flatpack("SHAVING_STAND",15262,13162);
  flatpack("OAK_SHAVING_STAND",15262,13163);
  flatpack("OAK_DRESSER",15262,13164);
  flatpack("TEAK_DRESSER",15262,13165);
  flatpack("FANCY_TEAK_DRESSER",15262,13166);
  flatpack("MAHOGANY_DRESSER",15262,13167);
  flatpack("GILDED_DRESSER",15262,13168);
  flatpack("OAK_CLOCK",15268,13169);
  flatpack("TEAK_CLOCK",15268,13170);
  flatpack("GILDED_CLOCK",15268,13171);
  HouseFurnishing.row("RESTORATION_POOL",29122,65,706,false,new int[]{29237},new int[][]{{3420,5},{1929,5},{566,1000},{559,1000}});
  HouseFurnishing.row("REVITALISATION_POOL",29122,70,850,false,new int[]{29238},new int[][]{{12625,10}});
  HouseFurnishing.row("REJUVENATION_POOL",29122,80,900,false,new int[]{29239},new int[][]{{2434,10}});
  HouseFurnishing.row("FANCY_REJUVENATION_POOL",29122,85,1950,false,new int[]{29240},new int[][]{{3024,10},{8786,2}});
  HouseFurnishing.row("ORNATE_REJUVENATION_POOL",29122,90,3107,false,new int[]{29241},new int[][]{{12905,10},{8784,5},{565,1000}});
 }
 private static void flatpack(String key,int group,int model){for(HouseFlatpackData.Recipe r:HouseFlatpackData.all())if(r.key.equals(key)){HouseFurnishing.row(key,group,r.level,key.equals("OAK_BED")?210:r.xp,false,new int[]{model},r.materials);return;}}
 static String prerequisite(String key){switch(key){case "REVITALISATION_POOL":return "RESTORATION_POOL";case "REJUVENATION_POOL":return "REVITALISATION_POOL";case "FANCY_REJUVENATION_POOL":return "REJUVENATION_POOL";case "ORNATE_REJUVENATION_POOL":return "FANCY_REJUVENATION_POOL";default:return null;}}
}
