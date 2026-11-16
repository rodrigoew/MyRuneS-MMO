package io.xeros.content.achievement_diary.impl;

import java.util.EnumSet;
import java.util.Set;

import io.xeros.content.achievement_diary.StatefulAchievementDiary;
import io.xeros.model.entity.player.Player;

public final class KaramjaAchievementDiary extends StatefulAchievementDiary<KaramjaDiaryEntry> {
	
	public static final Set<KaramjaDiaryEntry> EASY_TASKS = EnumSet.of(KaramjaDiaryEntry.PICK_BANANAS, KaramjaDiaryEntry.TELEPORT_TO_KARAMJA, KaramjaDiaryEntry.SAIL_TO_ARDOUGNE, KaramjaDiaryEntry.FISH_LOBSTER_KAR);
	
	public static final Set<KaramjaDiaryEntry> MEDIUM_TASKS = EnumSet.of(KaramjaDiaryEntry.DURADEL, KaramjaDiaryEntry.STEEL_DRAGON, KaramjaDiaryEntry.DANCE_75_TIMES);
	
	public static final Set<KaramjaDiaryEntry> HARD_TASKS = EnumSet.of(KaramjaDiaryEntry.COMPLETE_FIGHT_CAVES, KaramjaDiaryEntry.MINE_GOLD_KAR, KaramjaDiaryEntry.KILL_TZHAAR_XIL);
	
	public static final Set<KaramjaDiaryEntry> ELITE_TASKS = EnumSet.of(KaramjaDiaryEntry.CRAFT_NATURES, KaramjaDiaryEntry.ANTI_VENOM, KaramjaDiaryEntry.EQUIP_FIRE_CAPE);
	
	public static final String NAME = "Karamja area";

	public KaramjaAchievementDiary(Player player) {
		super(NAME, player);
	}
	
	int REWARD = 11136;
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
	public Set<KaramjaDiaryEntry> getEasy() {
		return EASY_TASKS;
	}

	@Override
	public Set<KaramjaDiaryEntry> getMedium() {
		return MEDIUM_TASKS;
	}

	@Override
	public Set<KaramjaDiaryEntry> getHard() {
		return HARD_TASKS;
	}

	@Override
	public Set<KaramjaDiaryEntry> getElite() {
		return ELITE_TASKS;
	}
	
	int frameIndex;
	public final void display() {
		Set<KaramjaDiaryEntry> all = getAll();
		int[] frames = { 8148, 8149, 8150, 8151, 8152, 8154, 8155, 8156, 8158, 8159, 8160, 8162, 8163, 8164,
				8165, 8166, 8167, 8168, 8169, 8170, 8171, 8172, 8173, 8174, 8175, 8176, 8178, 8179, 8180, 8181,
				8182, 8183, 8184, 8185, 8186, 8187, 8188, 8189, 8190, 8191, 8192, 8193, 8194 };

		player.getPA().resetQuestInterface();
		frameIndex = 0;
		
		player.getPA().sendFrame126("@dre@Karamja Diary", 8144);
		player.getPA().sendFrame126("", 8145);
		player.getPA().sendFrame126(hasCompleted("EASY") ? "@blu@<str=1>Easy</str>" : "@blu@Easy", 8147);
		player.getPA().sendFrame126(hasCompleted("MEDIUM") ? "@blu@<str=1>Medium</str>" : "@blu@Medium", 8153);
		player.getPA().sendFrame126(hasCompleted("HARD") ? "@blu@<str=1>Hard</str>" : "@blu@Hard", 8157);
		player.getPA().sendFrame126(hasCompleted("ELITE") ? "@blu@<str=1>Elite</str>" : "@blu@Elite", 8161);
		
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
	public int getMaximum(KaramjaDiaryEntry achievement) {
		return achievement.getMaximumStages();
	}

}