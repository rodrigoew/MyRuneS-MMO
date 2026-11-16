package io.xeros.content.skills.runecrafting;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import io.xeros.Configuration;
import io.xeros.content.Announcement;
import io.xeros.content.achievement_diary.impl.ArdougneDiaryEntry;
import io.xeros.content.achievement_diary.impl.FaladorDiaryEntry;
import io.xeros.content.achievement_diary.impl.KaramjaDiaryEntry;
import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.content.achievement_diary.impl.VarrockDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.skills.SkillHandler;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.util.Misc;
import io.xeros.util.discord.Discord;

public class Runecrafting extends SkillHandler {

	public enum RunecraftingData {
		AIR(25378, 556, 5, new int[] { 1, 11, 22, 33, 44, 55, 66, 77, 88, 99 }, 25000, 20667),
		MIND(25379, 558, 5.5, new int[] { 2, 14, 28, 42, 56, 70, 84, 98 }, 24500, 20669),
		WATER(25376, 555, 6, new int[] { 5, 19, 38, 57, 76, 95 }, 24000, 20671),
		MIST(-1, -1, 8, new int[] { 6 }, 15, -1),
		EARTH(24972, 557, 6.5, new int[] { 9, 26, 52, 78, 104 }, 23500, 20673),
		DUST(-1, -1, 8.3, new int[] { 10 }, 15000, -1),
		MUD(-1, -1, 9.3, new int[] { 13 }, 15000, -1),
		FIRE(24971, 554, 7, new int[] { 14, 35, 70 }, 23000, 20665),
		SMOKE(-1, -1, 8.5, new int[] { 15 }, 15000, -1),
		STEAM(-1, -1, 9.3, new int[] { 19 }, 15000, -1),
		BODY(24973, 559, 7.5, new int[] { 20, 46, 92 }, 22500, 20675),
		LAVA(-1, -1, 10, new int[] { 23 }, 15000, -1),
		COSMIC(24974, 564, 8, new int[] { 27, 59 }, 22000, 20677),
		CHAOS(24976, 562, 8.5, new int[] { 35, 74 }, 21500, 20679),
		ASTRAL(14911, 9075, 8.7, new int[] { 40, 82 }, 21000, 20689),
		NATURE(24975, 561, 9, new int[] { 44, 91 }, 20000, 20681),
		LAW(25034, 563, 9.5, new int[] { 54, 95 }, 19500, 20683),
		DEATH(25035, 560, 10, new int[] { 65, 99 }, 19000, 20685),
		//BLOOD(25380, 565, 17, new int[] { 77, 89, 94 }, 18800, 20691),
		BLOOD(43848, 565, 17, new int[] { 77, 89, 94 }, 18800, 20691),
		WRATH(25380, 21880, 15, new int[] { 95 }, 18800, 20691),
		SOUL(25377, 566, 18, new int[] { 90, 95, 99 }, 18000, 20687);

		private final int objectId;
		private final int runeId;
		private final int petChance;
		private final int petId;
		private final double experience;
		private final int[] multiplier;

		public int getObjectId() {
			return objectId;
		}

		public int getRuneId() {
			return runeId;
		}

		public int getPetChance() {
			return petChance;
		}

		public int getPetId() {
			return petId;
		}

		public double getExperience() {
			return experience;
		}

		public int getLevelRequirement() {
			return multiplier[0];
		}

		RunecraftingData(int objectId, int runeId, double experience, int[] multiplier, int petChance, int petId) {
			this.objectId = objectId;
			this.runeId = runeId;
			this.experience = experience;
			this.multiplier = multiplier;
			this.petChance = petChance;
			this.petId = petId;
		}

		public static RunecraftingData forId(int id) {
			Optional<RunecraftingData> first = Arrays.stream(RunecraftingData.values()).filter(rune -> rune.runeId == id).findFirst();
			return first.orElse(null);
		}
	}

	public static void execute(Player player, int objectId) {
        if (objectId <= 0 || !player.getUtilityMagic().canInteract()) return;

		for (RunecraftingData data : RunecraftingData.values()) {

			String name = data.name().toLowerCase().replaceAll("_", " ");
			if (data.getObjectId() == objectId) {
				if (!hasRequiredLevel(player, 20, data.getLevelRequirement(), "runecrafting", "craft these runes")) {
					return;
				}
				if (Misc.random(10) == 0 && player.getInterfaceEvent().isExecutable()) {
					player.getInterfaceEvent().execute();
					return;
				}
				if (!player.getItems().playerHasItem(1436) && !player.getItems().playerHasItem(7936)) {
					player.sendMessage("You need some essence to craft runes!");
					return;
				}
				int multiplier = 1;
				for (int multiply = 0; multiply < data.multiplier.length; multiply++) {
					if (player.playerLevel[20] >= data.multiplier[multiply]) {
						multiplier = multiply + 1;
					}
				}
				boolean basic = data == RunecraftingData.AIR || data == RunecraftingData.MIND || data == RunecraftingData.WATER
                        || data == RunecraftingData.EARTH || data == RunecraftingData.FIRE || data == RunecraftingData.BODY;
                int pure = player.getItems().getInventoryCount(7936);
                int normal = basic ? player.getItems().getInventoryCount(1436) : 0;
                int count = pure + normal;
                if (count == 0) { player.sendMessage("This altar requires pure essence."); return; }
                int multiply = count * multiplier;
                int output = withEyeBonus(player,multiply);
                java.util.List<int[]> ingredients=new java.util.ArrayList<>();
                if(pure>0)ingredients.add(new int[]{7936,pure});
                if(normal>0)ingredients.add(new int[]{1436,normal});
                if(!io.xeros.content.skills.ProductionBatch.exchange(player,ingredients.toArray(new int[0][]),new int[][]{{data.getRuneId(),output}}))return;

				if (name.equals("water")) {
					player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.CRAFT_WATER);
				}
				if (name.equals("death")) {
					player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.CRAFT_DEATH);
				}
				if (name.equals("mind")) {
					if (multiply > 100) {
						player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.CRAFT_MIND);
					}
				}
				if (name.equals("cosmic")) {
					if (multiply > 56) {
						player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.CRAFT_COSMIC);
					}
				}
				if (name.equals("earth")) {
					if (multiply > 150) {
						player.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.ALOT_OF_EARTH);
					}
					player.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.EARTH_RUNES);
				}
				if (name.equals("nature")) {
					if (multiply > 50) {
						player.getDiaryManager().getKaramjaDiary().progress(KaramjaDiaryEntry.CRAFT_NATURES);
					}
					DailyTasks.increase(player, DailyTasks.PossibleTasks.NATURE_RUNES);//Daily tasks
				}

				if (name.equals("law"))
					DailyTasks.increase(player, DailyTasks.PossibleTasks.LAW_RUNES);//Daily tasks



				player.gfx100(186);
				player.startAnimation(791);
				double percentOfXp = data.getExperience() * count * 2.5;
				player.getPA().addSkillXPMultiplied(data.getExperience()*count + (player.getItems().isWearingItem(20008) ? percentOfXp : 0),20,true);

				player.getPA().requestUpdates();
				player.getPA().sendSound(2710);
				petRoll(player, data);
				if (Misc.hasOneOutOf(25)) {//Runecrating casket
					player.getItems().addItemUnderAnyCircumstance(25590, 1);//Runecrating casket
					player.sendMessage("@red@You have managed to receive an runecrating casket.");
					Announcement.announce("@red@"+player.getLoginName()+" @pur@has received an @red@Runecrafting Casket @pur@from @red@grinding Runecrafting.");
					Discord.writeDropsSyncMessage(""+player.getLoginName()+" has received an Runecrafting Casket from grinding Runecrafting.");
				}
			}
		}
	}

    public static int withEyeBonus(Player player,int amount) {
        int pieces=0;
        for(int slot:new int[]{Player.playerHat,Player.playerChest,Player.playerLegs,Player.playerFeet}){
            int worn=player.playerEquipment[slot];
            if(Arrays.stream(Configuration.RAIMENTS_OF_THE_EYE_PEICES).anyMatch(id->id==worn))pieces++;
        }
        double extra=amount*(pieces*.1+(pieces==4?.2:0));int whole=(int)extra;
        return amount+whole+(Misc.random(999)<(extra-whole)*1000?1:0);
    }

	public static void petRoll(Player player, RunecraftingData data) {
		boolean hasGuardian = IntStream.range(20665, 20691).anyMatch(id -> player.getItems().getInventoryCount(id) > 0);
		boolean hasGuardianItem = IntStream.range(20665, 20691).anyMatch(id -> player.getItems().playerHasItem(id));

		if (hasGuardianItem) {
			IntStream.range(20665, 20691).forEach(id -> player.getItems().deleteItem(id, 1));
			player.getItems().addItem(data.getPetId(), 1);
		}
		int petRate = player.skillingPetRateScroll ? (int) (data.getPetChance() * .75) : data.getPetChance();

		if (!hasGuardian) {
			if (Misc.random(petRate/15) == 18) {
				PlayerHandler.executeGlobalMessage("[<col=CC0000>News</col>] @cr20@ <col=255>" + player.getDisplayName() + "</col> successfully crafted a <col=CC0000>Rift guardian</col> pet!");
				player.getItems().addItemUnderAnyCircumstance(data.getPetId(), 1);
				player.getCollectionLog().handleDrop(player, 5, data.getPetId(), 1);
			}
		}
	}
}
