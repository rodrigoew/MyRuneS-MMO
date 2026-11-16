package io.xeros.content.combat.magic;
import io.xeros.model.entity.player.Player;import io.xeros.content.skills.ProductionBatch;
/** Charges are attached to each physical weapon, including moves and saved bank stacks. */
public final class SoloEquipmentCharges {
 public static final int SHADOW=27275,SHADOW_SPELL=100,SCYTHE=22325;
 private SoloEquipmentCharges(){}
 public static boolean scythe(int id){return id==SCYTHE||id==25736||id==25739;}
 public static boolean shadowSpell(Player p){return p.playerEquipment[3]==SHADOW&&p.getSpellId()==SHADOW_SPELL&&!p.usingClickCast;}
 public static int shadowFactor(Player p){return p.getInstance() instanceof io.xeros.content.minigames.toa.SoloToa?4:3;}
 public static int shadowDamageBonus(Player p,int bonus){return Math.min(100,bonus*shadowFactor(p));}
 public static int charged(int id){switch(id){case 27277:return 27275;case 22486:return 22325;case 25738:return 25736;case 25741:return 25739;case 25862:return 25865;case 22481:return 22323;case 25733:return 25731;case 11908:return 11907;case 22290:return 22288;case 12900:return 12899;case 22294:return 22292;default:return id;}}
 public static int empty(int id){switch(id){case 27275:return 27277;case 22325:return 22486;case 25736:return 25738;case 25739:return 25741;case 25865:return 25862;case 22323:return 22481;case 25731:return 25733;case 11905:case 11907:return 11908;case 22288:return 22290;case 12899:return 12900;case 22292:return 22294;default:return -1;}}
 private static int[][] materials(int id){switch(EquipmentChargeLedger.family(charged(id))){case 1:return new int[][]{{566,2},{562,5}};case 2:return new int[][]{{565,300},{22446,1}};case 3:return new int[][]{{23962,1}};case 4:return new int[][]{{565,3}};case 5:return new int[][]{{560,1},{562,1},{554,5},{995,10}};case 6:return new int[][]{{560,1},{562,1},{554,5},{995,10},{12934,1}};default:return new int[0][];}}
 public static boolean useItem(Player p,int a,int b){return useItem(p,a,b,-1,-1);}
 public static boolean useItem(Player p,int a,int b,int sa,int sb){for(int[] cost:materials(a))if(cost[0]==b){charge(p,a,sa);return true;}for(int[] cost:materials(b))if(cost[0]==a){charge(p,b,sb);return true;}return false;}
 public static void charge(Player p,int weapon){charge(p,weapon,-1);}
 public static void charge(Player p,int weapon,int slot){
  if(materials(weapon).length==0)return;EquipmentChargeLedger ledger=p.getEquipmentChargeLedger();String key=ledger.choose(weapon,slot);
  if(key==null){int f=EquipmentChargeLedger.family(charged(weapon));for(int i=0;i<28;i++){int id=p.playerItems[i]-1;if(EquipmentChargeLedger.family(charged(id))==f){weapon=id;key="i"+i;break;}}if(key==null&&EquipmentChargeLedger.family(p.playerEquipment[3])==f){weapon=p.playerEquipment[3];key="e3";}}
  if(key==null){p.sendMessage("Tenha a arma na mochila ou equipada.");return;}
  int current=ledger.at(key),target=charged(weapon),unit=scythe(target)||target==25865?100:1,cap=target==11905||target==11907||target==12899?2500:20000,n=(cap-current)/unit;int[][] costs=materials(weapon);for(int[] c:costs)n=Math.min(n,p.getItems().getInventoryCount(c[0])/c[1]);if(n<=0){p.sendMessage("Materiais insuficientes ou arma no limite de cargas.");check(p);return;}
  java.util.List<int[]> inputs=new java.util.ArrayList<>();for(int[] c:costs)inputs.add(new int[]{c[0],c[1]*n});
  if(target==weapon){if(ProductionBatch.exchange(p,inputs.toArray(new int[0][]),new int[0][]))ledger.set(key,current+n*unit);}else{
   if(!key.startsWith("i")){p.sendMessage("Retire a arma para carrega-la.");return;}inputs.add(new int[]{weapon,1});if(!ProductionBatch.canExchange(p,inputs.toArray(new int[0][]),new int[][]{{target,1}}))return;inputs.remove(inputs.size()-1);int selected=Integer.parseInt(key.substring(1));int[] before=p.playerItems.clone();
   try(EquipmentChargeLedger.Transaction movement=ledger.transaction()){p.getItems().deleteItem(weapon,selected,1);if(!ProductionBatch.exchange(p,inputs.toArray(new int[0][]),new int[][]{{target,1}}))throw new IllegalStateException("Charge exchange changed");for(int i=0;i<28;i++)if(p.playerItems[i]==target+1&&before[i]!=target+1){ledger.set("i"+i,current+n*unit);break;}}
  }check(p);
 }
 public static void uncharge(Player p,int id,int slot){EquipmentChargeLedger ledger=p.getEquipmentChargeLedger();String key=ledger.choose(id,slot);int empty=empty(id);if(key==null||empty<0)return;if(!key.startsWith("i")){p.sendMessage("Retire a arma para remover cargas.");return;}int n=ledger.at(key);java.util.List<int[]> outputs=new java.util.ArrayList<>();outputs.add(new int[]{empty,1});for(int[] c:materials(id)){int amount=!scythe(id)&&id!=25865?n*c[1]:scythe(id)&&c[0]==565?n*3:n/100*c[1];if(amount>0)outputs.add(new int[]{c[0],amount});}if(!ProductionBatch.canExchange(p,new int[][]{{id,1}},outputs.toArray(new int[0][])))return;
  try(EquipmentChargeLedger.Transaction movement=ledger.transaction()){ledger.set(key,0);p.getItems().deleteItem(id,Integer.parseInt(key.substring(1)),1);if(!ProductionBatch.exchange(p,new int[0][],outputs.toArray(new int[0][])))throw new IllegalStateException("Uncharge exchange changed");}p.sendMessage("Cargas removidas. Fracoes de vial/shard nao sao recuperaveis.");check(p);
 }
 public static boolean option(Player p,int id,int slot,int option){int target=charged(id),family=EquipmentChargeLedger.family(target);if(materials(id).length==0)return false;if(id!=target&&((family==1||family==4)&&option==2||family==2&&option==3)){charge(p,id,slot);return true;}if(id==target&&option==2){check(p);return true;}if(id==target&&(family==1||family==4)&&option==3){charge(p,id,slot);return true;}if(id==target&&((family==1||family==4)&&option==5||(family==2||family==3||family==5||family==6)&&option==3)){uncharge(p,id,slot);return true;}return false;}
 public static int poweredSpell(int id){switch(EquipmentChargeLedger.family(id)){case 1:return 100;case 4:return 98;case 5:return 52;case 6:return 53;default:return -1;}}
 public static void configure(Player p){int spell=poweredSpell(p.playerEquipment[3]);if(!p.usingClickCast&&spell>=0){p.usingMagic=true;p.autocasting=true;p.autocastId=spell;p.setSpellId(spell);}}
 public static void check(Player p){p.getEquipmentChargeLedger().check();}
 public static boolean ready(Player p){int id=p.playerEquipment[3];if(p.usingClickCast)return true;if(id==11905){p.getEquipmentChargeLedger().equipped();p.getItems().setEquipment(11907,1,3,false);p.getEquipmentChargeLedger().set("e3",2500);id=11907;}int family=EquipmentChargeLedger.family(id);if(family>0&&p.getEquipmentChargeLedger().equipped()<=0){p.sendMessage("Arma sem cargas. Use os materiais na arma ou ::charge ID.");return false;}if(id!=charged(id)&&(poweredSpell(charged(id))>=0||id==25862)){p.sendMessage("Carregue a arma antes de atacar.");return false;}return true;}
 public static void consume(Player p){int f=EquipmentChargeLedger.family(p.playerEquipment[3]);if(f>0&&(f==2&&!p.usingMagic||f==3&&!p.usingMagic||!p.usingClickCast&&p.usingMagic&&poweredSpell(p.playerEquipment[3])==p.getSpellId()))p.getEquipmentChargeLedger().consume();}
}
