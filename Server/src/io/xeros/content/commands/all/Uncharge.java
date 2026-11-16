package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;import io.xeros.model.entity.player.Player;
public class Uncharge extends Command{public void execute(Player p,String name,String input){if(!Boolean.getBoolean("solo.mode"))return;try{String[] a=input.trim().split(" +");int id=Integer.parseInt(a[0]);int slot=a.length>1?Integer.parseInt(a[1])-1:-1;io.xeros.content.combat.magic.SoloEquipmentCharges.uncharge(p,id,slot);}catch(NumberFormatException e){p.sendMessage("Use ::uncharge ID [posicao 1 a 28], com a arma na mochila.");}}}
