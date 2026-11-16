package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;import io.xeros.model.entity.player.Player;
public final class House extends Command {
    @Override public void execute(Player p,String name,String input){if(Boolean.getBoolean("solo.mode"))p.getHouse().command(input);}
}
