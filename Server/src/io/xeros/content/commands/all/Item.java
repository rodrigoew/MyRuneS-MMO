package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;
import io.xeros.model.definitions.ItemDef;
public class Item extends Command {
    public void execute(Player p,String name,String input) {
        if(!Boolean.getBoolean("solo.mode")) return;
        try {
            String[] a=input.trim().split("\\s+");
            int id=Integer.parseInt(a[0]),quantity=a.length>1?Integer.parseInt(a[1]):1;
            if(quantity<1 || !ItemDef.getDefinitions().containsKey(id)) {p.sendMessage("Invalid item or quantity.");return;}
            p.getItems().addItem(id,quantity);
        } catch(NumberFormatException e) {p.sendMessage("Usage: ::item ID quantity");}
    }
}
