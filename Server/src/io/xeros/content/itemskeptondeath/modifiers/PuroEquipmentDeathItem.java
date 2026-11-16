package io.xeros.content.itemskeptondeath.modifiers;
import io.xeros.content.itemskeptondeath.DeathItemModifier;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import java.util.*;
/** These untradeable Puro-Puro rewards are destroyed on an unsafe death. */
public final class PuroEquipmentDeathItem implements DeathItemModifier {
    public static final Set<Integer> ITEMS=Set.of(11258,11259);
    public Set<Integer> getItemIds(){return ITEMS;}
    public void modify(Player p,GameItem item,boolean kept,List<GameItem> keptItems,List<GameItem> lostItems){keptItems.remove(item);lostItems.remove(item);}
}
