package io.xeros.content.skills;
import io.xeros.model.entity.player.Player;
/** Legacy amount input shares the owner-bound Study transaction. */
public final class TabletCreation {
 public static void createTablet(Player player,int recipe,int amount){if(player!=null)player.getHouse().study().legacyTablet(recipe,amount);}
 private TabletCreation(){}
}
