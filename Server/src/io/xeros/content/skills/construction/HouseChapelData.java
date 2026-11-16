package io.xeros.content.skills.construction;
/** Primary historical chapel recipes; includes the 2021 Gnome Child decoration. */
public final class HouseChapelData {
 static void register(){
  HouseFurnishing.row("GNOME_CHILD_ICON",15269,45,100.0,false,new int[]{40871},new int[][]{{8778,3},{25338,1}});
  HouseFurnishing.row("SARADOMIN_SYMBOL",15269,48,120.0,false,new int[]{13172},new int[][]{{8778,2}});
  HouseFurnishing.row("ZAMORAK_SYMBOL",15269,48,120.0,false,new int[]{13173},new int[][]{{8778,2}});
  HouseFurnishing.row("GUTHIX_SYMBOL",15269,48,120.0,false,new int[]{13174},new int[][]{{8778,2}});
  HouseFurnishing.row("SARADOMIN_ICON",15269,59,960.0,false,new int[]{13175},new int[][]{{8780,4},{8784,2}});
  HouseFurnishing.row("ZAMORAK_ICON",15269,59,960.0,false,new int[]{13176},new int[][]{{8780,4},{8784,2}});
  HouseFurnishing.row("GUTHIX_ICON",15269,59,960.0,false,new int[]{13177},new int[][]{{8780,4},{8784,2}});
  HouseFurnishing.row("BOB_ICON",15269,71,1160.0,false,new int[]{13178},new int[][]{{8782,4},{8784,2}});
  HouseFurnishing.row("OAK_ALTAR",15270,45,240.0,false,new int[]{13179},new int[][]{{8778,4}});
  HouseFurnishing.row("TEAK_ALTAR",15270,50,360.0,false,new int[]{13182},new int[][]{{8780,4}});
  HouseFurnishing.row("CLOTH_ALTAR",15270,56,390.0,false,new int[]{13185},new int[][]{{8780,4},{8790,2}});
  HouseFurnishing.row("MAHOGANY_ALTAR",15270,60,590.0,false,new int[]{13188},new int[][]{{8782,4},{8790,2}});
  HouseFurnishing.row("LIMESTONE_ALTAR",15270,64,910.0,false,new int[]{13191},new int[][]{{8782,6},{8790,2},{3420,2}});
  HouseFurnishing.row("MARBLE_ALTAR",15270,70,1030.0,false,new int[]{13194},new int[][]{{8786,2},{8790,2}});
  HouseFurnishing.row("GILDED_ALTAR",15270,75,2230.0,false,new int[]{13197},new int[][]{{8786,2},{8790,2},{8784,4}});
  HouseFurnishing.row("STEEL_TORCHES",15271,45,40.0,false,new int[]{13202},new int[][]{{2353,2}});
  HouseFurnishing.row("WOODEN_TORCHES",15271,49,58.0,false,new int[]{13200},new int[][]{{960,2},{4819,2}});
  HouseFurnishing.row("STEEL_CANDLESTICKS",15271,53,124.0,false,new int[]{13204},new int[][]{{2353,6},{36,6}});
  HouseFurnishing.row("GOLD_CANDLESTICKS",15271,57,46.0,false,new int[]{13206},new int[][]{{2357,6},{36,6}});
  HouseFurnishing.row("INCENSE_BURNER",15271,61,280.0,false,new int[]{13208},new int[][]{{8778,4},{2353,2}});
  HouseFurnishing.row("MAHOGANY_BURNER",15271,65,600.0,false,new int[]{13210},new int[][]{{8782,4},{2353,2}});
  HouseFurnishing.row("MARBLE_BURNER",15271,69,1040.0,false,new int[]{13212},new int[][]{{8786,2},{2353,2}});
  HouseFurnishing.row("SHUTTERED_WINDOW",13733,49,228.0,false,new int[]{13217,13226,13235,13244,13253,13262,27073},new int[][]{{960,8},{4819,8}});
  HouseFurnishing.row("DECORATIVE_WINDOW",13733,69,4.0,false,new int[]{13218,13227,13236,13245,13254,13263,27074},new int[][]{{1775,8}});
  HouseFurnishing.row("STAINED_GLASS",13733,89,5.0,false,new int[]{13219,13228,13237,13246,13255,13264,27075},new int[][]{{1775,16}});
  HouseFurnishing.row("SMALL_STATUE",15275,49,40.0,false,new int[]{13271},new int[][]{{3420,2}});
  HouseFurnishing.row("MEDIUM_STATUE",15275,69,500.0,false,new int[]{13272},new int[][]{{8786,1}});
  HouseFurnishing.row("LARGE_STATUE",15275,89,1500.0,false,new int[]{13273},new int[][]{{8786,3}});
  HouseFurnishing.row("WINDCHIMES",15276,49,323.0,false,new int[]{13214},new int[][]{{8778,4},{4819,4},{2353,4}});
  HouseFurnishing.row("BELLS",15276,58,480.0,false,new int[]{13215},new int[][]{{8780,4},{2353,6}});
  HouseFurnishing.row("ORGAN",15276,69,680.0,false,new int[]{13216},new int[][]{{8782,4},{2353,6}});
  HouseFurnishing.row("CHAPEL_BROWN_RUG",15272,2,30.0,false,new int[]{6761,6760,6759},new int[][]{{8790,2}});
  HouseFurnishing.row("CHAPEL_RUG",15272,13,60.0,false,new int[]{6764,6763,6762},new int[][]{{8790,4}});
  HouseFurnishing.row("CHAPEL_OPULENT_RUG",15272,65,360.0,false,new int[]{6767,6766,6765},new int[][]{{8790,4},{8784,1}});
 }
 public static int altarBonus(String key){switch(key){case "OAK_ALTAR":return 100;case "TEAK_ALTAR":return 110;case "CLOTH_ALTAR":return 125;case "MAHOGANY_ALTAR":return 150;case "LIMESTONE_ALTAR":return 175;case "MARBLE_ALTAR":return 200;case "GILDED_ALTAR":return 250;default:return 0;}}
 public static int style(String key){if(key==null)return 0;if(key.startsWith("SARADOMIN"))return 0;if(key.startsWith("GUTHIX"))return 1;if(key.startsWith("ZAMORAK"))return 2;if(key.equals("GNOME_CHILD_ICON"))return 4;return 3;}
 public static int altarModel(String key,int style){switch(key){
  case "OAK_ALTAR":return style==4?40872:style==1?13181:style==2?13180:13179;
  case "TEAK_ALTAR":return style==4?40873:style==1?13184:style==2?13183:13182;
  case "CLOTH_ALTAR":return style==4?40874:style==1?13186:style==2?13187:13185;
  case "MAHOGANY_ALTAR":return style==4?40875:style==1?13190:style==2?13189:13188;
  case "LIMESTONE_ALTAR":return style==4?40876:style==1?13193:style==2?13192:13191;
  case "MARBLE_ALTAR":return style==4?40877:style==1?13196:style==2?13195:13194;
  case "GILDED_ALTAR":return style==4?40878:style==1?13199:style==2?13198:13197;
 default:return -1;}}
 public static int windowModel(String key,int root,int style){int[] roots={13733,13728,13732,13729,13730,13731,27070};int index=-1;for(int i=0;i<roots.length;i++)if(roots[i]==root)index=i;if(index<0)return -1;switch(key){
  case "SHUTTERED_WINDOW":return new int[]{13217,13226,13235,13244,13253,13262,27073}[index];
  case "DECORATIVE_WINDOW":switch(style){
   case 0:return new int[]{13222,13231,13240,13249,13258,13267,27078}[index];
   case 1:return new int[]{13220,13229,13238,13247,13256,13265,27076}[index];
   case 2:return new int[]{13224,13233,13242,13251,13260,13269,27080}[index];
   case 3:return new int[]{13218,13227,13236,13245,13254,13263,27074}[index];
   case 4:return new int[]{40879,40881,40883,40885,40899,40901,40903}[index];
  default:return -1;}
  case "STAINED_GLASS":switch(style){
   case 0:return new int[]{13223,13232,13241,13250,13259,13268,27079}[index];
   case 1:return new int[]{13221,13230,13239,13248,13257,13266,27077}[index];
   case 2:return new int[]{13225,13234,13243,13252,13261,13270,27081}[index];
   case 3:return new int[]{13219,13228,13237,13246,13255,13264,27075}[index];
   case 4:return new int[]{40880,40882,40884,40886,40900,40902,40904}[index];
  default:return -1;}
 default:return -1;}}
 public static int statueModel(String key,int style){switch(key){
  case "SMALL_STATUE":return new int[]{13271,13274,13277,13280,40915}[Math.max(0,Math.min(4,style))];
  case "MEDIUM_STATUE":return new int[]{13272,13275,13278,13281,40916}[Math.max(0,Math.min(4,style))];
  case "LARGE_STATUE":return new int[]{13273,13276,13279,13282,40917}[Math.max(0,Math.min(4,style))];
 default:return -1;}}
 private HouseChapelData(){}
}
