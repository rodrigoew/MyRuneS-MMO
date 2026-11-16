package io.xeros.content.bosses;
import io.xeros.model.entity.player.Player;import io.xeros.content.skills.ProductionBatch;
/** Atomic boss-component recipes; recipe selection must match both used items. */
public final class SoloBossCrafting {
 private SoloBossCrafting(){}
 public static boolean useItem(Player p,int a,int b){
  int armour=a==27269?b:b==27269?a:-1;if(armour==27226||armour==27229||armour==27232){if(p.playerLevel[12]<90){p.sendMessage("Voce precisa de Crafting 90.");return true;}int plates=armour==27226?1:armour==27229?4:3;make(p,new int[][]{{armour,1},{27269,plates}},armour+9);return true;}
  if(a==2347||b==2347){int gear=a==2347?b:a;int n=gear==11826?1:gear==11828?4:gear==11830?3:0;if(n>0){if(p.playerLevel[12]<90){p.sendMessage("Voce precisa de Crafting 90.");return true;}if(ProductionBatch.exchange(p,new int[][]{{gear,1},{2347,0}},new int[][]{{27269,n}}))p.sendMessage("Armadylean plates recuperadas.");return true;}}
  int kit=a==25742||a==25744?a:b==25742||b==25744?b:-1;int weapon=kit==a?b:a;if(kit>0){int result=weapon==22325?(kit==25742?25736:25739):weapon==22486?(kit==25742?25738:25741):kit==25742&&weapon==22323?25731:kit==25742&&weapon==22481?25733:-1;if(result>0){make(p,new int[][]{{weapon,1},{kit,1}},result);return true;}}
  if((a==22969||a==22971||a==22973)&&(b==22969||b==22971||b==22973)&&a!=b){make(p,new int[][]{{22969,1},{22971,1},{22973,1}},22975);return true;}
  if(a>=13274&&a<=13276&&b>=13274&&b<=13276&&a!=b){make(p,new int[][]{{13274,1},{13275,1},{13276,1}},13263);return true;}
  int[][] recipes={{22966,11889,22978},{24511,24422,24423},{24514,24422,24424},{24517,24422,24425},{21730,11836,21733},{23804,21739,21752},{13274,13275,13263}};
  for(int[] recipe:recipes)if((a==recipe[0]&&b==recipe[1])||(b==recipe[0]&&a==recipe[1])){if(recipe[2]==13263)make(p,new int[][]{{13274,1},{13275,1},{13276,1}},13263);else make(p,new int[][]{{recipe[0],1},{recipe[1],1}},recipe[2]);return true;}
  if((a==22983&&b==2347)||(b==22983&&a==2347)){make(p,new int[][]{{22983,1},{2347,0}},22981);return true;}
  return false;
 }
 private static void make(Player p,int[][] inputs,int result){if(ProductionBatch.exchange(p,inputs,new int[][]{{result,1}}))p.sendMessage("Equipamento criado: "+io.xeros.model.definitions.ItemDef.forId(result).getName()+".");else p.sendMessage("Confira os componentes e o espaco na mochila.");}
}
