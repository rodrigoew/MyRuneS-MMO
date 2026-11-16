package io.xeros.content.combat.magic;
import io.xeros.model.Staff;
import io.xeros.model.entity.player.Player;
import java.util.Arrays;
/** Counts for spell icons/hover text use the same carried sources as server payment. */
public final class MagicRuneDisplay {
 public static final int CONFIG_BASE=22000;
 private static final int[] RUNES={554,555,556,557,558,559,560,561,562,563,564,565,566,9075,21880,4694,4695,4696,4697,4698,4699};
 private static final int[][] COMBOS={{4694,555,554},{4695,556,555},{4696,556,557},{4697,556,554},{4698,555,557},{4699,557,554}};
 private final Player player;private final int[] previous=new int[RUNES.length];
 public MagicRuneDisplay(Player player){this.player=player;Arrays.fill(previous,-1);}
 private static long carried(Player p,int id){return (long)p.getItems().getInventoryCount(id)+p.getRunePouch().availableRunes(id);}
 public static int count(Player p,int id){
  if(io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.FOUNTAIN_OF_RUNE_BOUNDARY))for(int rune:RUNES)if(rune==id)return Integer.MAX_VALUE;
  if(Staff.getRunesProvidedBy(p).contains(id))return Integer.MAX_VALUE;
  long total=carried(p,id);
  for(int[] combo:COMBOS)if(combo[1]==id||combo[2]==id)total+=carried(p,combo[0]);
  if(id==561&&io.xeros.content.skills.crafting.BryophytaStaff.isWearingStaffWithCharge(p))total+=p.bryophytaStaffCharges;
  return (int)Math.min(Integer.MAX_VALUE,total);
 }
 public void refresh(){for(int i=0;i<RUNES.length;i++){int value=count(player,RUNES[i]);if(previous[i]!=value){player.getPA().sendConfig(CONFIG_BASE+i,value);previous[i]=value;}}}
}
