package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;import io.xeros.model.entity.player.Player;public class Charges extends Command{public void execute(Player p,String name,String input){if(!Boolean.getBoolean("solo.mode"))return;io.xeros.content.combat.magic.SoloEquipmentCharges.check(p);}}
