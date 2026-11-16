package io.xeros.content.itemskeptondeath.modifiers;
import io.xeros.content.itemskeptondeath.DeathItemModifier;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import java.util.*;
public final class BorrowedHunterEquipmentDeathItem implements DeathItemModifier {
    public static final Set<Integer> ITEMS=Set.of(10023,10024);
    public Set<Integer> getItemIds(){return ITEMS;}
    public void modify(Player p,GameItem item,boolean kept,List<GameItem> keptItems,List<GameItem> lostItems){keptItems.remove(item);lostItems.remove(item);p.getFalconry().unsafeDeath();}
}
