package io.xeros.content.combat.magic;

import io.xeros.model.entity.player.Player;

/** Lunar utility spell: 2022 cost, level, XP, duration and recast delay. */
public final class MagicImbue {
    private MagicImbue() {}
    public static boolean cast(Player player) {
        if (player.playerMagicBook != 2 || player.playerLevel[6] < 82) {
            player.sendMessage("You need level 82 Magic and the Lunar spellbook.");return false;
        }
        if (!player.getUtilityMagic().canInteract() || !player.getUtilityMagic().ready()) return false;
        long now=System.currentTimeMillis();
        if(now<player.magicImbueUntil){player.sendMessage("Magic Imbue is still active.");return false;}
        if(!NonCombatSpellData.consumeRunesOnCast(player,new int[]{9075,554,555},new int[]{2,7,7}))return false;
        player.getUtilityMagic().committedCast(3);
        player.magicImbueUntil=now+12600;
        player.getPA().addSkillXPMultiplied(86,6,true);player.startAnimation(4413);player.gfx100(722);
        player.sendMessage("You can combine runes without a talisman for 12.6 seconds.");return true;
    }
}
