package io.xeros.content.commands.all;

import io.xeros.content.commands.Command;
import io.xeros.content.skills.Skill;
import io.xeros.model.entity.player.Player;

/** Public skill numbers are one-based; internal IDs remain unchanged. */
public class Addxp extends Command {
    @Override public void execute(Player p, String commandName, String input) {
        if (!Boolean.getBoolean("solo.mode")) return;
        String[] args = input.trim().split("\\s+");
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            for (Skill skill : Skill.values()) p.sendMessage((skill.getId() + 1) + " = " + skill);
            return;
        }
        if (args.length != 2) { p.sendMessage("Use ::addxp SKILL XP or ::addxp list. Example: ::addxp 1 50000"); return; }
        try {
            int number = Integer.parseInt(args[0]);
            long requested = Long.parseLong(args[1]);
            if (number < 1 || number > Skill.length() || requested <= 0) {
                p.sendMessage("Invalid skill or XP. Use ::addxp list; XP must be positive."); return;
            }
            int id = number - 1, before = p.playerXP[id];
            p.getPA().addSkillXPExact((int) Math.min(requested, Skill.MAX_EXP), id);
            int gained = p.playerXP[id] - before;
            if (gained == 0) p.sendMessage("No XP added: check XP locks or the 200M limit.");
            else p.sendMessage("Added " + gained + " XP to " + Skill.forId(id) + ". Total: " + p.playerXP[id] + ".");
        } catch (NumberFormatException e) {
            p.sendMessage("Use whole numbers: ::addxp 1 50000");
        }
    }
}
