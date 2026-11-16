package io.xeros.model.cycleevent.impl;

import io.xeros.content.SkillcapePerks;
import io.xeros.content.combat.melee.CombatPrayer;
import io.xeros.model.Items;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;

public class SkillRestorationEvent extends Event<Player> {

	public SkillRestorationEvent(Player attachment) {
		super(attachment, 100);
	}

    private int healthTicks;
    private int restoreTicks;
    private int boostedHealthTicks;
    private boolean rapidHeal;

    @Override
    public void update() {
        boolean rapid = CombatPrayer.isPrayerOn(attachment, CombatPrayer.RAPID_HEAL);
        // Changing Rapid Heal restarts its countdown; it cannot be flicked for instant healing.
        if (rapid != rapidHeal) { healthTicks = 0; rapidHeal = rapid; }
        if (attachment.isDead || attachment.getHealth().getCurrentHealth() <= 0) {
            healthTicks = boostedHealthTicks = 0;
            return;
        }
        // Halfway through the normal cycle, Rapid Restore restores drained stats only.
        restoreTicks = (restoreTicks + 1) % 100;
        if (restoreTicks == 50 && CombatPrayer.isPrayerOn(attachment, CombatPrayer.RAPID_RESTORE)) {
            for (int i = 0; i < attachment.playerLevel.length; i++) {
                if (i == 3 || i == 5) continue;
                if (attachment.playerLevel[i] < attachment.getLevelForXP(attachment.playerXP[i])) {
                    attachment.playerLevel[i]++;
                    attachment.getPA().refreshSkill(i);
                }
            }
        }
        if (++boostedHealthTicks >= 100) {
            boostedHealthTicks = 0;
            if (attachment.getHealth().getCurrentHealth() > attachment.getHealth().getMaximumHealth())
                attachment.getHealth().tick(1);
        }
        if (++healthTicks >= (rapid ? 50 : 100)) {
            healthTicks = 0;
            if (attachment.getHealth().getCurrentHealth() >= attachment.getHealth().getMaximumHealth()) return;
            boolean cape = SkillcapePerks.HITPOINTS.isWearing(attachment) || SkillcapePerks.isWearingMaxCape(attachment);
            int heal = !rapid && cape ? 2 : 1;
            if (attachment.getItems().isWearingItem(Items.REGEN_BRACELET, Player.playerHands)) heal *= 2;
            attachment.getHealth().increase(heal);
        }
    }

	@Override
	public void execute() {
		if (attachment.isDead || attachment.getHealth().getCurrentHealth() <= 0) {
			return;
		}


		for (int index = 0; index < attachment.playerLevel.length; index++) {
			if (index == 3 || index == 5) {
				continue;
			}
            if(attachment.getDivinePotions().preventsDecay(index))continue;
			if ((index == 0 || index == 1 || index == 2) && attachment.hasDivineCombatBoost) {
				continue;
			}
			if ((index == 4) && attachment.hasDivineRangeBoost) {
				continue;
			}
			if ((index == 6) && attachment.hasDivineMagicBoost) {
				continue;
			}
			if ((index == 0 || index == 1 || index == 2
					|| index == 4 || index == 6) && attachment.hasOverloadBoost) {
				continue;
			}
			final int maximum = attachment.getLevelForXP(attachment.playerXP[index]);
			if (attachment.playerLevel[index] < maximum) {
				attachment.playerLevel[index]++;
			} else if (attachment.playerLevel[index] > maximum) {
				attachment.playerLevel[index]--;
			}
			attachment.getPA().refreshSkill(index);
		}
	}

}
