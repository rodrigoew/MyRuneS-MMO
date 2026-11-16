package io.xeros.content.skills.construction;
import java.util.*;
/** Native heraldic products and 2022 recipe facts, including adamant equipment. */
public final class HouseHeraldryData {
 public enum Crest {
ARRAV("Arrav"),ASGARNIA("Asgarnia"),DORGESHUUN("Dorgeshuun"),DRAGON("Dragon"),FAIRY("Fairy"),GUTHIX("Guthix"),HAM("H.A.M."),HORSE("Horse"),JOGRE("Jogre"),KANDARIN("Kandarin"),MISTHALIN("Misthalin"),MONEY("Money"),SARADOMIN("Saradomin"),SKULL("Skull"),VARROCK("Varrock"),ZAMORAK("Zamorak");
  public final String label;Crest(String label){this.label=label;}
 }
 public static final class Product {
  public final int item,kind,base,level,constructionLevel,ticks;public final double xp;public final Crest crest;public final int[][] materials;
  Product(int item,Crest crest,int kind,int base,int level,double xp,int con,int ticks,int[][] materials){this.item=item;this.crest=crest;this.kind=kind;this.base=base;this.level=level;this.xp=xp;this.constructionLevel=con;this.ticks=ticks;this.materials=materials;}
 }
 private static final List<Product> DATA=new ArrayList<>();
 private static void row(int item,Crest crest,int kind,int base,int level,double xp,int con,int ticks,int[][] materials){DATA.add(new Product(item,crest,kind,base,level,xp,con,ticks,materials));}
 static {
  row(8682,Crest.ARRAV,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8684,Crest.ASGARNIA,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8686,Crest.DORGESHUUN,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8688,Crest.DRAGON,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8690,Crest.FAIRY,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8692,Crest.GUTHIX,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8694,Crest.HAM,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8696,Crest.HORSE,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8698,Crest.JOGRE,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8700,Crest.KANDARIN,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8702,Crest.MISTHALIN,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8704,Crest.MONEY,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8706,Crest.SARADOMIN,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8708,Crest.SKULL,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8710,Crest.VARROCK,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(8712,Crest.ZAMORAK,0,1157,38,37.0,16,6,new int[][]{{1157,1}});
  row(22159,Crest.ARRAV,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22161,Crest.ASGARNIA,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22163,Crest.DORGESHUUN,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22165,Crest.DRAGON,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22167,Crest.FAIRY,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22169,Crest.GUTHIX,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22171,Crest.HAM,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22173,Crest.HORSE,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22175,Crest.JOGRE,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22177,Crest.KANDARIN,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22179,Crest.MISTHALIN,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22181,Crest.MONEY,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22183,Crest.SARADOMIN,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22185,Crest.SKULL,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22187,Crest.VARROCK,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(22189,Crest.ZAMORAK,0,1161,38,37.0,16,6,new int[][]{{1161,1}});
  row(8464,Crest.ARRAV,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8466,Crest.ASGARNIA,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8468,Crest.DORGESHUUN,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8470,Crest.DRAGON,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8472,Crest.FAIRY,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8474,Crest.GUTHIX,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8476,Crest.HAM,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8478,Crest.HORSE,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8480,Crest.JOGRE,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8482,Crest.KANDARIN,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8484,Crest.MISTHALIN,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8486,Crest.MONEY,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8488,Crest.SARADOMIN,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8490,Crest.SKULL,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8492,Crest.VARROCK,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8494,Crest.ZAMORAK,0,1163,38,37.0,16,6,new int[][]{{1163,1}});
  row(8746,Crest.ARRAV,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8748,Crest.ASGARNIA,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8750,Crest.DORGESHUUN,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8752,Crest.DRAGON,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8754,Crest.FAIRY,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8756,Crest.GUTHIX,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8758,Crest.HAM,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8760,Crest.HORSE,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8762,Crest.JOGRE,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8764,Crest.KANDARIN,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8766,Crest.MISTHALIN,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8768,Crest.MONEY,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8770,Crest.SARADOMIN,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8772,Crest.SKULL,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8774,Crest.VARROCK,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(8776,Crest.ZAMORAK,1,1193,43,40.0,16,6,new int[][]{{1193,1}});
  row(22127,Crest.ARRAV,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22129,Crest.ASGARNIA,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22131,Crest.DORGESHUUN,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22133,Crest.DRAGON,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22135,Crest.FAIRY,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22137,Crest.GUTHIX,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22139,Crest.HAM,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22141,Crest.HORSE,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22143,Crest.JOGRE,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22145,Crest.KANDARIN,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22147,Crest.MISTHALIN,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22149,Crest.MONEY,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22151,Crest.SARADOMIN,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22153,Crest.SKULL,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22155,Crest.VARROCK,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(22157,Crest.ZAMORAK,1,1199,43,40.0,16,6,new int[][]{{1199,1}});
  row(8714,Crest.ARRAV,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8716,Crest.ASGARNIA,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8718,Crest.DORGESHUUN,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8720,Crest.DRAGON,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8722,Crest.FAIRY,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8724,Crest.GUTHIX,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8726,Crest.HAM,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8728,Crest.HORSE,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8730,Crest.JOGRE,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8732,Crest.KANDARIN,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8734,Crest.MISTHALIN,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8736,Crest.MONEY,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8738,Crest.SARADOMIN,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8740,Crest.SKULL,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8742,Crest.VARROCK,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8744,Crest.ZAMORAK,1,1201,43,40.0,16,6,new int[][]{{1201,1}});
  row(8650,Crest.ARRAV,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8652,Crest.ASGARNIA,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8654,Crest.DORGESHUUN,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8656,Crest.DRAGON,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8658,Crest.FAIRY,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8660,Crest.GUTHIX,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8662,Crest.HAM,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8664,Crest.HORSE,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8666,Crest.JOGRE,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8668,Crest.KANDARIN,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8670,Crest.MISTHALIN,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8672,Crest.MONEY,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8674,Crest.SARADOMIN,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8676,Crest.SKULL,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8678,Crest.VARROCK,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
  row(8680,Crest.ZAMORAK,2,0,48,42.5,16,6,new int[][]{{8790,1},{960,1}});
 }
 public static List<Product> all(){return Collections.unmodifiableList(DATA);}
 public static Product get(int id){for(Product p:DATA)if(p.item==id)return p;return null;}
 public static String protectingGod(int item){Product p=get(item);return p!=null&&p.kind<2&&(p.crest==Crest.SARADOMIN||p.crest==Crest.ZAMORAK)?p.crest.name():"";}
 private HouseHeraldryData(){}
}
