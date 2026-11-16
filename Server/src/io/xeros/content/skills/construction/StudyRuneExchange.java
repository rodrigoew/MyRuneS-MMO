package io.xeros.content.skills.construction;
import io.xeros.content.combat.magic.RunePayment;
import io.xeros.model.entity.player.Player;
/** Study uses the shared atomic rune/material transaction. */
final class StudyRuneExchange {
 static boolean exchange(Player p,int[][] materials,int output){return RunePayment.exchange(p,materials,new int[][]{{output,1}});}
 private StudyRuneExchange(){}
}
