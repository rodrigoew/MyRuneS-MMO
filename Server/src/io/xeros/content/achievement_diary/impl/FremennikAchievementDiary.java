package io.xeros.content.achievement_diary.impl;

import java.util.EnumSet;
import java.util.Set;

import io.xeros.content.achievement_diary.StatefulAchievementDiary;
import io.xeros.model.entity.player.Player;

import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.CHOP_OAK_FREM;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.DAGANNOTH_KINGS;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.FILL_BUCKET;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_ARMADYL;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_BANDOS;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_DAGANNOTH_KINGS;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_KURASK;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_ROCK_CRAB;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_SARADOMIN;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_SPIRITUAL_MAGE;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.KILL_ZAMORAK;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.MINE_COAL_FREM;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.MIX_SUPER_DEFENCE;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.PRAY_AT_ALTAR;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.TRAVEL_JATIZSOT;
import static io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.TRAVEL_NEITIZNOT;

public final class FremennikAchievementDiary extends StatefulAchievementDiary<FremennikDiaryEntry> {
	
	public static final Set<FremennikDiaryEntry> EASY_TASKS = EnumSet.of(KILL_ROCK_CRAB, FILL_BUCKET, CHOP_OAK_FREM, TRAVEL_JATIZSOT, TRAVEL_NEITIZNOT);
	
	public static final Set<FremennikDiaryEntry> MEDIUM_TASKS = EnumSet.of(MINE_COAL_FREM, KILL_DAGANNOTH_KINGS, KILL_SPIRITUAL_MAGE);
	
	public static final Set<FremennikDiaryEntry> HARD_TASKS = EnumSet.of(PRAY_AT_ALTAR, MIX_SUPER_DEFENCE , KILL_KURASK, DAGANNOTH_KINGS);
	
	public static final Set<FremennikDiaryEntry> ELITE_TASKS = EnumSet.of(/*CRAFT_ASTRAL, */KILL_BANDOS, KILL_ARMADYL, KILL_ZAMORAK, KILL_SARADOMIN);
	
	public static final String NAME = "Fremennik area";

	public FremennikAchievementDiary(Player player) {
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
	
	int REWARD =  13129;
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
	public Set<FremennikDiaryEntry> getEasy() {
		return EASY_TASKS;
	}

	@Override
	public Set<FremennikDiaryEntry> getMedium() {
		return MEDIUM_TASKS;
	}

	@Override
	public Set<FremennikDiaryEntry> getHard() {
		return HARD_TASKS;
	}

	@Override
	public Set<FremennikDiaryEntry> getElite() {
		return ELITE_TASKS;
	}
	
	int frameIndex;
	int amount = frameIndex == 10 || frameIndex == 16 || frameIndex == 20 ? 2 : 1;
	public final void display() {
		Set<FremennikDiaryEntry> all = getAll();
		int[] frames = { 8148, 8149, 8150, 8151, 8152, 8154, 8155, 8156, 8158, 8159, 8160, 8161, 8162, 8164,
				8165, 8166, 8167, 8168, 8169, 8170, 8171, 8172, 8173, 8174, 8175, 8176, 8178, 8179, 8180, 8181,
				8182, 8183, 8184, 8185, 8186, 8187, 8188, 8189, 8190, 8191, 8192, 8193, 8194 };

		player.getPA().resetQuestInterface();
		frameIndex = 0;
		
		player.getPA().sendFrame126("@dre@Fremennik Diary", 8144);
		player.getPA().sendFrame126("", 8145);
		player.getPA().sendFrame126(hasCompleted("EASY") ? "@blu@<str=1>Easy</str>" : "@blu@Easy", 8147);
		player.getPA().sendFrame126(hasCompleted("MEDIUM") ? "@blu@<str=1>Medium</str>" : "@blu@Medium", 8153);
		player.getPA().sendFrame126(hasCompleted("HARD") ? "@blu@<str=1>Hard</str>" : "@blu@Hard", 8157);
		player.getPA().sendFrame126(hasCompleted("ELITE") ? "@blu@<str=1>Elite</str>" : "@blu@Elite", 8163);
		
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
	public int getMaximum(FremennikDiaryEntry achievement) {
		return achievement.getMaximumStages();
	}

}