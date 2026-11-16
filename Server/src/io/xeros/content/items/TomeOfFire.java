package io.xeros.content.items;
import io.xeros.model.entity.player.Player;
/** Legacy dialogue compatibility; page payment and conversion are one transaction. */
public final class TomeOfFire {
 public static final int TOME_EMPTY=20716,TOME=20714,PAGE=20718;
 private static int held(Player p){return p.getItems().getInventoryCount(TOME)>0?TOME:TOME_EMPTY;}
 public static void store(Player p){ElementalTomes.transfer(p,held(p),false,true,Integer.MAX_VALUE);}
 public static void remove(Player p){ElementalTomes.transfer(p,TOME,false,false,Integer.MAX_VALUE);}
}
