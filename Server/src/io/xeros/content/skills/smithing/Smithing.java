package io.xeros.content.skills.smithing;

import io.xeros.Server;
import io.xeros.content.SkillcapePerks;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.content.achievement_diary.impl.VarrockDiaryEntry;
import io.xeros.content.achievement_diary.impl.WildernessDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import io.xeros.util.Misc;

public class Smithing {

	private final Player c;
	private final int[] SMELT_BARS = { 2349, 2351, 2355, 2353, 2357, 2359, 2361, 2363 };
	private final int[] SMELT_FRAME = { 2405, 2406, 2407, 2409, 2410, 2411, 2412, 2413 };
	private final int[] BAR_REQS = { 1, 15, 20, 30, 40, 50, 70, 85 };
	private final int[] ORE_1 = { 438, 440, -1, 440, 444, 447, 449, 451 };
	private final int[] ORE_2 = { 436, -1, -1, -1, -1, -1, -1, -1 };
	private final int[] SMELT_EXP = { 6, 13, -1, 18, 23, 30, 38, 50 };
	private int exp;
	private int oreId;
	private int oreId2;
	private int barId;

	public Smithing(final Player c) {
		this.c = c;
	}

	public boolean canSmelt(final int barType) {
		for (int j = 0; j < SMELT_BARS.length; j++) {
			if (barType == SMELT_BARS[j]) {
				// c.sendMessage("" + c.playerLevel + " bar: " + BAR_REQS[j]);
				return c.playerLevel[Player.playerSmithing] >= BAR_REQS[j];
			}
		}
		return false;
	}


	public int getExp(final int barType) {
		for (int j = 0; j < SMELT_BARS.length; j++) {
			if (barType == SMELT_BARS[j]) {
				return SMELT_EXP[j];
			}
		}
		return 0;
	}

	public int getOre(final int barType) {
		for (int j = 0; j < SMELT_BARS.length; j++) {
			if (barType == SMELT_BARS[j]) {
				// c.sendMessage("" + ORE_1[j]);
				return ORE_1[j];
			}
		}
		return 0;
	}

	public int getOre2(final int barType) {
		for (int j = 0; j < SMELT_BARS.length; j++) {
			if (barType == SMELT_BARS[j]) {
				// c.sendMessage("" + ORE_2[j]);
				return ORE_2[j];
			}
		}
		return 0;
	}

	public boolean hasOres(final int barType) {
		if (getOre2(barType) > 0) {
			return c.getItems().playerHasItem(getOre(barType)) && c.getItems().playerHasItem(getOre2(barType));
		} else {
			return c.getItems().playerHasItem(getOre(barType));
		}
	}

    public static void readInput(final int ignoredLevel, final String type, final Player c, final int amount) {
        try { int product=Integer.parseInt(type);if(c.getSmithingInt().permits(product))AnvilRecipes.make(c,product,amount); }
        catch(NumberFormatException e){c.sendMessage("Unknown smithing product.");}
    }

	public void sendSmelting() {
		for (int j = 0; j < SMELT_FRAME.length; j++) {
			c.getPA().sendFrame246(SMELT_FRAME[j], 150, SMELT_BARS[j]);
		}
		c.getPA().sendChatboxInterface(2400);
		c.smeltInterface = true;
	}

	public void smelt(final int barType) {
		if (c.smeltAmount > 0) {
			c.getPA().closeAllWindows();
			if (hasOres(barType)) {
				c.getItems().deleteItem(oreId, c.getItems().getInventoryItemSlot(oreId), 1);
				if (oreId2 > 0) {
					c.getItems().deleteItem(oreId2, c.getItems().getInventoryItemSlot(oreId2), 1);
				}
				c.getItems().addItem(barId, 1);
				c.getPA().addSkillXPMultiplied(exp, Player.playerSmithing, true);
				c.getPA().refreshSkill(Player.playerSmithing);
				c.smeltAmount--;
			} else {
				c.sendMessage("You do not have the required ores to smelt this.");
				c.getPA().removeAllWindows();
			}
		} else {
			c.getPA().resetVariables();
		}
	}
}
