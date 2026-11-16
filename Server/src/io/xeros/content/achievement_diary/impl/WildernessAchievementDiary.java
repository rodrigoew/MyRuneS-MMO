package io.xeros.content.achievement_diary.impl;

import java.util.EnumSet;
import java.util.Set;

import io.xeros.content.achievement_diary.StatefulAchievementDiary;
import io.xeros.model.entity.player.Player;

public final class WildernessAchievementDiary extends StatefulAchievementDiary<WildernessDiaryEntry> {
	
	public static final Set<WildernessDiaryEntry> EASY_TASKS = EnumSet.of(WildernessDiaryEntry.LOW_ALCH, WildernessDiaryEntry.WILDERNESS_LEVER, WildernessDiaryEntry.WILDERNESS_ALTAR, WildernessDiaryEntry.KILL_EARTH_WARRIOR, WildernessDiaryEntry.DEMONIC_RUINS, WildernessDiaryEntry.KBD_LAIR, WildernessDiaryEntry.MINE_IRON_WILD, WildernessDiaryEntry.ABYSS_TELEPORT);
	
	public static final Set<WildernessDiaryEntry> MEDIUM_TASKS = EnumSet.of(WildernessDiaryEntry.MINE_MITHRIL_WILD, WildernessDiaryEntry.WILDERNESS_GODWARS, WildernessDiaryEntry.KILL_GREEN_DRAGON, WildernessDiaryEntry.KILL_BLOODVELD, WildernessDiaryEntry.MYSTERIOUS_EMBLEM, WildernessDiaryEntry.SMITH_MITHRIL_AXE, WildernessDiaryEntry.WILDERNESS_AGILITY);
	
	public static final Set<WildernessDiaryEntry> HARD_TASKS = EnumSet.of(WildernessDiaryEntry.CLAWS_OF_GUTHIX, WildernessDiaryEntry.SMITH_ADAMANT_SCIMITAR, WildernessDiaryEntry.CHAOS_ELEMENTAL, WildernessDiaryEntry.CRAZY_ARCHAEOLOGIST, WildernessDiaryEntry.CHAOS_FANATIC, WildernessDiaryEntry.SCORPIA, WildernessDiaryEntry.SPIRITUAL_WARRIOR, WildernessDiaryEntry.KARAMBWAN);
	
	public static final Set<WildernessDiaryEntry> ELITE_TASKS = EnumSet.of(WildernessDiaryEntry.CALLISTO, WildernessDiaryEntry.VENENATIS, WildernessDiaryEntry.VETION, WildernessDiaryEntry.DARK_CRAB, WildernessDiaryEntry.SMITH_RUNE_SCIM_WILD, WildernessDiaryEntry.SPIRITUAL_MAGE, WildernessDiaryEntry.MAGIC_LOG_WILD);
	
	public static final String NAME = "Wilderness area";

	public WildernessAchievementDiary(Player player) {
		super(NAME, player);
	}
	
	int REWARD =  13108;
	public void claimReward() {
        claimRegionalReward(REWARD);
    }
	
	public void npcDialogue(String dialogue) {
		player.getDH().sendNpcChat1(dialogue, player.npcType, "Diary Manager");
		player.nextChat = -1;
	}
	public void addReward(int reward) {
		player.getItems().addItem(reward, 1);
		player.getDH().sendNpcChat1("Here you go, upgraded and ready to be used.", player.npcType, "Diary Manager");
	}
	public void upgradeReward(int reward, int upgrade) {
		player.getItems().replaceItem(player, reward, upgrade);
		player.getDH().sendNpcChat1("Here you go, upgraded and ready.", player.npcType, "Diary Manager");
	}
	public int getCount(int id) {
		return player.getItems().getItemCount(id, false);
	}
	
	public boolean hasCompleted(String difficulty) {
		switch (difficulty) {
		case "EASY":
			return achievements.containsAll(EASY_TASKS);
			
		case "MEDIUM":
			return achievements.containsAll(MEDIUM_TASKS);
			
		case "HARD":
			return achievements.containsAll(HARD_TASKS);
			
		case "ELITE":
			return achievements.containsAll(ELITE_TASKS);
		}
		return achievements.containsAll(EASY_TASKS);
	}

	@Override
	public Set<WildernessDiaryEntry> getEasy() {
		return EASY_TASKS;
	}

	@Override
	public Set<WildernessDiaryEntry> getMedium() {
		return MEDIUM_TASKS;
	}

	@Override
	public Set<WildernessDiaryEntry> getHard() {
		return HARD_TASKS;
	}

	@Override
	public Set<WildernessDiaryEntry> getElite() {
		return ELITE_TASKS;
	}
	
	int frameIndex;
	public final void display() {
		Set<WildernessDiaryEntry> all = getAll();
		int[] frames = { 8148, 8149, 8150, 8151, 8152, 8153, 8154, 8155, 8157, 8158, 8159, 8160, 8161, 8162, 8163,
				8165, 8166, 8167, 8168, 8169, 8170, 8171, 8172, 8174, 8175, 8176, 8177, 8178, 8179, 8180, 8181,
				8182, 8183, 8184, 8185, 8186, 8187, 8188, 8189, 8190, 8191, 8192, 8193, 8194 };

		player.getPA().resetQuestInterface();
		frameIndex = 0;
		
		player.getPA().sendFrame126("@dre@Wilderness Diary", 8144);
		player.getPA().sendFrame126("", 8145);
		player.getPA().sendFrame126(hasCompleted("EASY") ? "@blu@<str=1>Easy</str>" : "@blu@Easy", 8147);
		player.getPA().sendFrame126(hasCompleted("MEDIUM") ? "@blu@<str=1>Medium</str>" : "@blu@Medium", 8156);
		player.getPA().sendFrame126(hasCompleted("HARD") ? "@blu@<str=1>Hard</str>" : "@blu@Hard", 8164);
		player.getPA().sendFrame126(hasCompleted("ELITE") ? "@blu@<str=1>Elite</str>" : "@blu@Elite", 8173);
		
		all.forEach(entry -> {
			String description = entry.getDescription();
			
			/* %stage gets the current stage (e.g. 1)
			 * %maximumstage gets the maximum stage (e.g. 5)
			 * %totalstage gets both of these (e.g. 1/5)
			 */
			description = description.replace("%stagej", Integer.toString(getAbsoluteAchievementStage(entry)));
			description = description.replace("%maximumstage", Integer.toString(getMaximum(entry)));
			description = description.replace("%totalstage", (getAbsoluteAchievementStage(entry)) + "/" + getMaximum(entry));
			
			player.getPA().sendFrame126(hasDone(entry) ? "<str=0>" +description+ "</str>" : description, frames[frameIndex]);
			frameIndex++;
		});

		player.getPA().openQuestInterface();
	}

	@Override
	public int getMaximum(WildernessDiaryEntry achievement) {
		return achievement.getMaximumStages();
	}

}