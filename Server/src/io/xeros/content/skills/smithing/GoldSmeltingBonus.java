package io.xeros.content.skills.smithing;

import io.xeros.model.entity.player.Player;

/** The 2022 gold bonus does not stack, and converted combat max capes lose skillcape perks. */
public final class GoldSmeltingBonus {
    private GoldSmeltingBonus(){}
    public static boolean active(Player player){
        int cape=io.xeros.content.skills.agility.RunEnergy2022.inventoryId(player.playerEquipment[Player.playerCape]);
        return player.playerEquipment[Player.playerHands]==776||cape==9795||cape==9796||cape==13280;
    }
    public static double experience(Player player,int bar,double base){return bar==2357&&active(player)?56.2:base;}
}
