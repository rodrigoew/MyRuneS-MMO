package io.xeros.content.achievement_diary.impl;

import java.util.EnumSet;
import java.util.Set;

import io.xeros.content.achievement_diary.StatefulAchievementDiary;
import io.xeros.model.entity.player.Player;

public final class MorytaniaAchievementDiary extends StatefulAchievementDiary<MorytaniaDiaryEntry> {
	
	public static final Set<MorytaniaDiaryEntry> EASY_TASKS = EnumSet.of(MorytaniaDiaryEntry.MAZCHNA, MorytaniaDiaryEntry.KILL_BANSHEE, MorytaniaDiaryEntry.KILL_WEREWOLF);
	
	public static final Set<MorytaniaDiaryEntry> MEDIUM_TASKS = EnumSet.of(MorytaniaDiaryEntry.KILL_CAVE_HORROR, MorytaniaDiaryEntry.CLIMB_CHAIN);
	
	public static final Set<MorytaniaDiaryEntry> HARD_TASKS = EnumSet.of(MorytaniaDiaryEntry.TEN_CONSECUTIVE, MorytaniaDiaryEntry.DHIDE_BODY, MorytaniaDiaryEntry.KILL_NECHRYAEL);
	
	public static final Set<MorytaniaDiaryEntry> ELITE_TASKS = EnumSet.of(MorytaniaDiaryEntry.BARROWS_CHEST, MorytaniaDiaryEntry.KILL_ABYSSAL_DEMON);
	
	public static final String NAME = "Morytania area";

	public MorytaniaAchievementDiary(Player player) {
		super(NAME, player);
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
	
	int REWARD =  13112;
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

	@Override
	public Set<MorytaniaDiaryEntry> getEasy() {
		return EASY_TASKS;
	}

	@Override
	public Set<MorytaniaDiaryEntry> getMedium() {
		return MEDIUM_TASKS;
	}

	@Override
	public Set<MorytaniaDiaryEntry> getHard() {
		return HARD_TASKS;
	}

	@Override
	public Set<MorytaniaDiaryEntry> getElite() {
		return ELITE_TASKS;
	}
	
	int frameIndex;
	int amount = frameIndex == 10 || frameIndex == 16 || frameIndex == 20 ? 2 : 1;
	public final void display() {
		Set<MorytaniaDiaryEntry> all = getAll();
		int[] frames = { 8148, 8149, 8150, 8151, 8153, 8154, 8156, 8157, 8158, 8159, 8161, 8162, 8163, 8164,
				8165, 8166, 8167, 8168, 8169, 8170, 8171, 8172, 8173, 8174, 8175, 8176, 8178, 8179, 8180, 8181,
				8182, 8183, 8184, 8185, 8186, 8187, 8188, 8189, 8190, 8191, 8192, 8193, 8194 };

		player.getPA().resetQuestInterface();
		frameIndex = 0;
		
		player.getPA().sendFrame126("@dre@Morytania Diary", 8144);
		player.getPA().sendFrame126("", 8145);
		player.getPA().sendFrame126(hasCompleted("EASY") ? "@blu@<str=1>Easy</str>" : "@blu@Easy", 8147);
		player.getPA().sendFrame126(hasCompleted("MEDIUM") ? "@blu@<str=1>Medium</str>" : "@blu@Medium", 8152);
		player.getPA().sendFrame126(hasCompleted("HARD") ? "@blu@<str=1>Hard</str>" : "@blu@Hard", 8155);
		player.getPA().sendFrame126(hasCompleted("ELITE") ? "@blu@<str=1>Elite</str>" : "@blu@Elite", 8160); //60
		
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
			frameIndex += amount;
		});

		player.getPA().openQuestInterface();
	}

	@Override
	public int getMaximum(MorytaniaDiaryEntry achievement) {
		return achievement.getMaximumStages();
	}

}