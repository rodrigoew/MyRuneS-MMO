package io.xeros.content.combat.magic;
import io.xeros.model.entity.player.Player;
import java.util.function.IntUnaryOperator;
/** Verified 2022 combat-only savings. Called only after affordability and cast checks succeed. */
public final class RuneSavingWeapons {
 private RuneSavingWeapons(){}
 public static boolean saves(Player p,IntUnaryOperator random){
  switch(p.playerEquipment[Player.playerWeapon]){
   case 11791:case 12902:case 12904:case 22296:case 24144:return random.applyAsInt(7)==0;
   case 21006:return random.applyAsInt(100)<15;
   default:return false;
  }
 }
}
