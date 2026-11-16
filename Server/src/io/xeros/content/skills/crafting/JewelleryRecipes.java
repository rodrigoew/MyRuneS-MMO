package io.xeros.content.skills.crafting;
import io.xeros.content.dialogue.*;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import java.util.*;
public final class JewelleryRecipes {
 public static final class Recipe {
  public final int product,display,bar,gem,mould,level;public final double xp;public final String category;
  Recipe(int p,int d,int b,int g,int m,int l,double x,String c){product=p;display=d;bar=b;gem=g;mould=m;level=l;xp=x;category=c;}
 }
 private static final List<Recipe> RECIPES=new ArrayList<>();
 private static void add(int p,int d,int b,int g,int m,int l,double x,String c){RECIPES.add(new Recipe(p,d,b,g,m,l,x,c));}
 static {
        add(1635,1635,2357,0,1592,5,15,"Rings");
        add(1637,1637,2357,1607,1592,20,40,"Rings");
        add(1639,1639,2357,1605,1592,27,55,"Rings");
        add(1641,1641,2357,1603,1592,34,70,"Rings");
        add(1643,1643,2357,1601,1592,43,85,"Rings");
        add(1645,1645,2357,1615,1592,55,100,"Rings");
        add(6575,6575,2357,6573,1592,67,115,"Rings");
        add(19538,19538,2357,19493,1592,89,150,"Rings");
        add(1654,1654,2357,0,1597,6,20,"Necklaces");
        add(1656,1656,2357,1607,1597,22,55,"Necklaces");
        add(1658,1658,2357,1605,1597,29,60,"Necklaces");
        add(1660,1660,2357,1603,1597,40,75,"Necklaces");
        add(1662,1662,2357,1601,1597,56,90,"Necklaces");
        add(1664,1664,2357,1615,1597,72,105,"Necklaces");
        add(6577,6577,2357,6573,1597,82,120,"Necklaces");
        add(19535,19535,2357,19493,1597,92,165,"Necklaces");
        add(1673,1692,2357,0,1595,8,30,"Amulets");
        add(1675,1694,2357,1607,1595,24,65,"Amulets");
        add(1677,1696,2357,1605,1595,31,70,"Amulets");
        add(1679,1698,2357,1603,1595,50,85,"Amulets");
        add(1681,1700,2357,1601,1595,70,100,"Amulets");
        add(1683,1702,2357,1615,1595,80,150,"Amulets");
        add(6579,6585,2357,6573,1595,90,165,"Amulets");
        add(19501,19541,2357,19493,1595,98,200,"Amulets");
        add(11069,11069,2357,0,11065,7,25,"Bracelets");
        add(11072,11072,2357,1607,11065,23,60,"Bracelets");
        add(11076,11076,2357,1605,11065,30,65,"Bracelets");
        add(11085,11085,2357,1603,11065,42,80,"Bracelets");
        add(11092,11092,2357,1601,11065,58,95,"Bracelets");
        add(11115,11115,2357,1615,11065,74,110,"Bracelets");
        add(11130,11130,2357,6573,11065,84,125,"Bracelets");
        add(19532,19532,2357,19493,11065,95,180,"Bracelets");
        add(21081,21081,2355,1609,1592,1,10,"Rings");
        add(21090,21090,2355,1609,1597,16,35,"Necklaces");
        add(21099,21099,2355,1609,1595,27,55,"Amulets");
        add(21117,21117,2355,1609,11065,22,45,"Bracelets");
        add(21084,21084,2355,1611,1592,13,32,"Rings");
        add(21093,21093,2355,1611,1597,25,54,"Necklaces");
        add(21102,21102,2355,1611,1595,34,70,"Amulets");
        add(21120,21120,2355,1611,11065,29,60,"Bracelets");
        add(21087,21087,2355,1613,1592,16,35,"Rings");
        add(21096,21096,2355,1613,1597,32,70,"Necklaces");
        add(21105,21105,2355,1613,1595,45,80,"Amulets");
        add(21123,21123,2355,1613,11065,38,75,"Bracelets");
        add(1714,1714,2355,0,1599,16,50,"Silver items");
        add(1720,1720,2355,0,1594,17,50,"Silver items");
        add(5525,5525,2355,0,5523,23,52.5,"Silver items");
        add(2961,2961,2355,0,2976,18,50,"Silver items");
 }
 public static List<Recipe> recipes(){return Collections.unmodifiableList(RECIPES);}
 public static boolean make(Player p,int product,int quantity){
  for(Recipe r:RECIPES)if(r.product==product||r.display==product){
   int[][] materials=r.gem==0?new int[][]{{r.mould,0},{r.bar,1}}:new int[][]{{r.mould,0},{r.bar,1},{r.gem,1}};
   return CraftingBatch.start(p,quantity,r.level,r.xp,r.product,899,3,materials,()->{});
  }
  return false;
 }
 public static void menu(Player p){
  new DialogueBuilder(p).option("Jewellery",
   new DialogueOption("Rings",q->page(q,"Rings",0)),new DialogueOption("Necklaces",q->page(q,"Necklaces",0)),
   new DialogueOption("Amulets",q->page(q,"Amulets",0)),new DialogueOption("Bracelets",q->page(q,"Bracelets",0)),
   new DialogueOption("Silver items",q->page(q,"Silver items",0))).send();
 }
 public static void page(Player p,String category,int offset){
  List<Recipe> list=new ArrayList<>();for(Recipe r:RECIPES)if(r.category.equals(category))list.add(r);
  List<DialogueOption> options=new ArrayList<>();
  for(int i=offset;i<Math.min(list.size(),offset+4);i++){Recipe r=list.get(i);options.add(new DialogueOption(ItemAssistant.getItemName(r.product)+" (level "+r.level+")",q->quantity(q,r)));}
  options.add(new DialogueOption(offset+4<list.size()?"More...":"Back",q->{if(offset+4<list.size())page(q,category,offset+4);else menu(q);}));
  new DialogueBuilder(p).option(category,options.toArray(new DialogueOption[0])).send();
 }
 private static void quantity(Player p,Recipe recipe){
  new DialogueBuilder(p).option("How many?",new DialogueOption("Make 1",q->make(q,recipe.product,1)),
   new DialogueOption("Make 5",q->make(q,recipe.product,5)),new DialogueOption("Make all",q->make(q,recipe.product,28))).send();
 }
}
