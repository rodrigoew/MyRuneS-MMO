package io.xeros.content.skills.smithing;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
public class SmithingInterface {
 private final Player p;
 private int selectedBar,originX,originY,originH;
 public SmithingInterface(Player p){this.p=p;}
 public void showSmithInterface(int bar){
  List<AnvilRecipes.Recipe> list=new ArrayList<>();for(AnvilRecipes.Recipe r:AnvilRecipes.recipes())if(r.bar==bar)list.add(r);
  if(list.size()<25)return;
  selectedBar=bar;originX=p.absX;originY=p.absY;originH=p.heightLevel;
  cell(list.get(0),0,1119,1094);
  cell(list.get(1),0,1120,1091);
  cell(list.get(18),0,1121,1098);
  cell(list.get(3),0,1122,1102);
  cell(list.get(4),0,1123,1107);
  cell(list.get(5),1,1119,1085);
  cell(list.get(2),1,1120,1093);
  cell(list.get(22),1,1121,1099);
  cell(list.get(13),1,1122,1103);
  cell(list.get(9),1,1123,1108);
  cell(list.get(8),2,1119,1087);
  cell(list.get(16),2,1120,1083);
  cell(list.get(23),2,1121,1100);
  cell(list.get(15),2,1122,1104);
  cell(list.get(14),2,1123,1106);
  cell(list.get(10),3,1119,1086);
  cell(list.get(17),3,1120,1092);
  cell(list.get(24),3,1121,1101);
  cell(list.get(19),3,1122,1105);
  cell(list.get(6),3,1123,1096);
  cell(list.get(21),4,1119,1088);
  cell(list.get(20),4,1120,8429);
  cell(list.get(11),4,1121,11461);
  cell(list.get(7),4,1122,13358);
  cell(list.get(12),4,1123,1134);
  p.getPA().showInterface(994);
 }
 public boolean permits(int product){AnvilRecipes.Recipe r=AnvilRecipes.forProduct(product);return p.isInterfaceOpen(994)&&r!=null&&r.bar==selectedBar&&p.heightLevel==originH&&p.absX==originX&&p.absY==originY;}
 private void cell(AnvilRecipes.Recipe r,int slot,int frame,int label){
  p.getPA().sendFrame34(r.product,slot,frame,r.quantity);
  String name=ItemAssistant.getItemName(r.product).replaceFirst("(?i)^(bronze|iron|steel|mithril|adamant|rune) ","");
  if(name.length()>0)name=Character.toUpperCase(name.charAt(0))+name.substring(1);
  p.getPA().sendFrame126((p.playerLevel[13]>=r.level?"@whi@":"@bla@")+name+" ("+r.level+")",label);
  int[][] costs={{1125,1126,1109,1127,1128},{1124,1129,1110,1113,1130},{1116,1118,1111,1114,1131},{1089,1095,1112,1115,1132},{1090,8428,11459,13357,1135}};
  p.getPA().sendFrame126((p.getItems().playerHasItem(r.bar,r.bars)?"@gre@":"@red@")+r.bars+(r.bars==1?" bar":" bars"),costs[slot][frame-1119]);
 }
}
