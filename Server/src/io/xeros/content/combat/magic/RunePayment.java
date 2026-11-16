package io.xeros.content.combat.magic;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.Staff;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** One game-thread transaction across inventory, carried pouch and elemental substitutes. */
public final class RunePayment {
 private static final int[][] COMBOS={{4694,555,554},{4695,556,555},{4696,556,557},{4697,556,554},{4698,555,557},{4699,557,554}};
 private static int stored(Player p,int id){return id==561&&io.xeros.content.skills.crafting.BryophytaStaff.isWearingStaffWithCharge(p)?p.bryophytaStaffCharges:0;}
 private static int available(Player p,int id,Map<Integer,Integer> reserved){return (int)Math.min(Integer.MAX_VALUE,(long)p.getItems().getInventoryCount(id)-reserved.getOrDefault(id,0)+p.getRunePouch().availableRunes(id)+stored(p,id));}
 public static boolean exchange(Player p,int[][] materials,int[][] outputs){return evaluate(p,materials,outputs,true,new int[0][]);}
 public static boolean canExchange(Player p,int[][] materials,int[][] outputs){return evaluate(p,materials,outputs,false,new int[0][]);}
 public static boolean consume(Player p,int[][] materials){return exchange(p,materials,new int[0][]);}
 /** Physical ingredients are always taken from inventory, even when the same ID is a rune supplied by a staff. */
 public static boolean exchangeWithInventoryInputs(Player p,int[][] inventoryInputs,int[][] runes,int[][] outputs){return evaluate(p,runes,outputs,true,inventoryInputs);}
 public static boolean canExchangeWithInventoryInputs(Player p,int[][] inventoryInputs,int[][] runes,int[][] outputs){return evaluate(p,runes,outputs,false,inventoryInputs);}
 private static boolean rune(int id){return id>=554&&id<=566||id>=4694&&id<=4699||id==9075||id==21880;}
 private static boolean evaluate(Player p,int[][] materials,int[][] outputs,boolean commit,int[][] inventoryInputs){
  Map<Integer,Integer> need=new LinkedHashMap<>();List<int[]> ordinary=new ArrayList<>();List<Integer> provided=Staff.getRunesProvidedBy(p);
  Map<Integer,Integer> reserved=new LinkedHashMap<>();
  for(int[] row:inventoryInputs){if(row==null||row.length!=2||row[0]<=0||row[1]<=0)return false;long total=(long)reserved.getOrDefault(row[0],0)+row[1];if(total>Integer.MAX_VALUE)return false;reserved.put(row[0],(int)total);ordinary.add(row.clone());}
  for(var row:reserved.entrySet())if(p.getItems().getInventoryCount(row.getKey())<row.getValue())return false;
  boolean freeRunes=io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.FOUNTAIN_OF_RUNE_BOUNDARY);
  for(int[] row:materials){if(row==null||row.length!=2||row[0]<=0||row[1]<=0)return false;if(!rune(row[0])){ordinary.add(row.clone());continue;}if(freeRunes)continue;if(!provided.contains(row[0])){long sum=(long)need.getOrDefault(row[0],0)+row[1];if(sum>Integer.MAX_VALUE)return false;need.put(row[0],(int)sum);}}
  Map<Integer,Integer> cost=new LinkedHashMap<>();if(!plan(p,0,need,cost,new HashSet<>(),reserved))return false;
  Map<Integer,Integer> pouch=new LinkedHashMap<>();int staffCost=0;
  for(var row:cost.entrySet()){int staff=Math.min(row.getValue(),stored(p,row.getKey()));staffCost+=staff;int remaining=row.getValue()-staff;int bag=Math.min(remaining,p.getItems().getInventoryCount(row.getKey())-reserved.getOrDefault(row.getKey(),0));if(bag>0)ordinary.add(new int[]{row.getKey(),bag});if(bag<remaining)pouch.put(row.getKey(),remaining-bag);}
  int[][] physical=ordinary.toArray(new int[0][]);
  if(!ProductionBatch.canExchange(p,physical,outputs))return false;
  if(!commit)return true;
  // Pay stored runes while the pouch is still carried: the physical target itself may be that pouch.
  for(var row:pouch.entrySet())if(!p.getRunePouch().deleteRunesOnCast(row.getKey(),row.getValue()))throw new IllegalStateException("Rune pouch changed during game-thread transaction");
  if(!ProductionBatch.exchange(p,physical,outputs))throw new IllegalStateException("Inventory changed during game-thread rune transaction");
  for(int i=0;i<staffCost;i++)io.xeros.content.skills.crafting.BryophytaStaff.depleteIfUsed(p,561);
  return true;
 }
 private static boolean plan(Player p,int index,Map<Integer,Integer> need,Map<Integer,Integer> cost,Set<String> failed,Map<Integer,Integer> reserved){
  for(var row:need.entrySet()){
   long capacity=available(p,row.getKey(),reserved);for(int i=index;i<COMBOS.length;i++)if(COMBOS[i][1]==row.getKey()||COMBOS[i][2]==row.getKey())capacity+=available(p,COMBOS[i][0],reserved);
   if(capacity<row.getValue())return false;
  }
  if(index==COMBOS.length){for(var row:need.entrySet())if(available(p,row.getKey(),reserved)<row.getValue())return false;for(var row:need.entrySet())if(row.getValue()>0)cost.put(row.getKey(),row.getValue());return true;}
  String state=index+":"+need.toString();if(failed.contains(state))return false;
  int[] combo=COMBOS[index];int a=need.getOrDefault(combo[1],0),b=need.getOrDefault(combo[2],0),max=Math.min(available(p,combo[0],reserved),Math.max(a,b));
  for(int n=max;n>=0;n--){
   Map<Integer,Integer> next=new LinkedHashMap<>(need);if(a>0)next.put(combo[1],Math.max(0,a-n));if(b>0)next.put(combo[2],Math.max(0,b-n));
   if(n>0)cost.put(combo[0],n);else cost.remove(combo[0]);
   if(plan(p,index+1,next,cost,failed,reserved))return true;
  }
  cost.remove(combo[0]);failed.add(state);return false;
 }
 private RunePayment(){}
}
