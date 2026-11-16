package io.xeros.model;
import io.xeros.model.entity.player.Player;
/** Compatibility facade for existing saves and fire-tome callers. */
public final class TomeOfFire extends ElementalTomeCharges {
    public TomeOfFire(Player player){super(player,20714,20716,"tome of fire",true);}
    public void replaceTomeOfFire(){replaceEmptyEquipped();}
}
