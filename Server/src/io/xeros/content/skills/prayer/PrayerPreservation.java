package io.xeros.content.skills.prayer;
import io.xeros.model.entity.player.Player;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
/** The four 2021 Zealot pieces each spare remains on one of eighty outcomes. */
public final class PrayerPreservation {
 private static IntUnaryOperator random=bound->ThreadLocalRandom.current().nextInt(bound);
 private static final int[][] PIECES={{0,25438},{4,25434},{7,25436},{10,25440}};
 public static int pieces(Player p){int n=0;for(int[] row:PIECES)if(p.playerEquipment[row[0]]==row[1])n++;return n;}
 public static boolean saves(Player p,int roll){if(roll<0||roll>=80)throw new IllegalArgumentException("Zealot roll");return roll<pieces(p);}
 public static boolean preserve(Player p){return pieces(p)>0&&saves(p,random.applyAsInt(80));}
 private PrayerPreservation(){}
}
