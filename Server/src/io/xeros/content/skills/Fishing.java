package io.xeros.content.skills;

import io.xeros.Server;
import io.xeros.content.SkillcapePerks;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.FaladorDiaryEntry;
import io.xeros.content.achievement_diary.impl.KandarinDiaryEntry;
import io.xeros.content.achievement_diary.impl.KaramjaDiaryEntry;
import io.xeros.content.achievement_diary.impl.WildernessDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.hespori.Hespori;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.items.ItemAssistant;
import io.xeros.util.Misc;

/**
 * Class Fishing Handles: Fishing
 * 
 * @author: PapaDoc START: 22:07 23/12/2010 FINISH: 22:28 23/12/2010
 */

public class Fishing extends SkillHandler {
	
	public static int[] anglerOuftit = { 13258, 13259, 13260, 13261 };

	/**
	 * Fishing data
	 * Id, Level, Equipment, Bait, Raw material, XP, Animation,  
	 */
	public static int[][] data = { 
			{ 1, 1, 303, -1, 317, 10, 621, 321, 15, 30, 12000 }, // SHRIMP
			{ 2, 5, 307, 313, 327, 20, 622, 345, 10, 30, 12000 }, // SARDINE + HERRING
			{ 3, 16, 305, -1, 353, 20, 620, -1, -1, -1, 12000 }, // MACKEREL
			{ 4, 20, 309, 314, 335, 50, 622, 331, 30, 70, 12000 }, // TROUT
			{ 5, 23, 305, -1, 341, 45, 619, 363, 46, 100, 12000 }, // BASS + COD
			{ 6, 25, 307, 313, 349, 60, 622, -1, -1, -1, 12000 }, // PIKE
			{ 7, 35, 311, -1, 359, 80, 618, 371, 50, 100, 12000 }, // TUNA + SWORDIE
			{ 7, 35, 21028, -1, 359, 80, 7401, 371, 50, 100, 12000 }, // TUNA + SWORDIE
			{ 7, 35, 21031, -1, 359, 80, 7402, 371, 50, 100, 12000 }, // TUNA + SWORDIE
			{ 8, 40, 301, -1, 377, 90, 619, -1, -1, -1, 10000 }, // LOBSTER
			{ 9, 62, 303, -1, 7944, 100, 620, -1, -1, -1, 8000 }, // MONKFISH
			{ 10, 76, 311, -1, 383, 110, 618, -1, -1, -1, 6000 }, // SHARK
			{ 10, 76, 21028, -1, 383, 110, 7401, -1, -1, -1, 6000 }, // SHARK
			{ 10, 76, 21031, -1, 383, 110, 7402, -1, -1, -1, 6000 }, // SHARK
			{ 11, 79, 305, -1, 395, 100, 620, 389, 81, 130, 6000 }, // SEA TURTLE
			{ 12, 81, 305, -1, 389, 130, 620, -1, -1, -1, 6000 }, // MANTA RAY
			{ 13, 85, 301, 11940, 11934, 132, 619, -1, -1, -1, 6000 }, // DARK CRAB
			{ 14, 5, 303, -1, 3150, 5, 620, -1, -1, -1, 15000 }, // Karambwanji AFK FISH
			{ 15, 65, 3159, 3150, 3142, 105, 620, -1, -1, -1, 8000 }, // Karambwan
			{ 16, 82, 307, 13431, 13439, 118, 622, -1, -1, -1, 6000 } // Anglerfish
	};
	
	private static void clueBottles(Player player, int petChance) {
		int chance =  petChance/40;
		int easyChance = 50;
		int medChance = 100;
		int rewardAmount = 1;
		if (player.fasterCluesScroll) {
			chance = petChance/80;
			easyChance = 25;
			medChance = 50;
		}
		if (Hespori.activeGolparSeed) {
			rewardAmount = 2;
		}
		int bottleRoll = Misc.random(chance);
		if (Misc.random(chance) == 1) {
			player.sendMessage("You catch a clue bottle!");
			if (bottleRoll < easyChance) {
				player.getItems().addItemUnderAnyCircumstance(13648, rewardAmount);
			} else if (bottleRoll >= easyChance && bottleRoll < medChance) {
				player.getItems().addItemUnderAnyCircumstance(13649, rewardAmount);
			} else {
				player.getItems().addItemUnderAnyCircumstance(13650, rewardAmount);
			}
		}
	}
	private static void foeArtefact(Player player, int petChance) {
		int chance = petChance/24;
		int artefactRoll = Misc.random(100);
		if (Misc.random(chance) == 1) {
			if (artefactRoll <65) {//1/300
				player.getItems().addItemUnderAnyCircumstance(11180, 1);//ancient coin foe for 200
				player.sendMessage("You found a coin that can be used in the Fire of Exchange!");
			} else if (artefactRoll >= 65 && artefactRoll < 99) {//1/600
				player.getItems().addItemUnderAnyCircumstance(681, 1);//anicent talisman foe for 300
				player.sendMessage("You found a talisman that can be used in the Fire of Exchange!");
			} else if (artefactRoll > 99){//1/1000
				player.getItems().addItemUnderAnyCircumstance(9034, 1);//golden statuette foe for 500
				PlayerHandler.executeGlobalMessage("@bla@[@red@Fishing@bla@]@blu@ " + player.getDisplayName() + " @red@just found a Golden statuette while fishing!");
			}
		}
	}
    public static void attemptdata(Player player,int method){start(player,method,Misc::trueRand);}
    public static boolean start(Player player,int method,java.util.function.IntUnaryOperator random){
        if(!player.getUtilityMagic().canInteract())return false;
        int[] selected=null;
        for(int[] row:data)if(row[0]==method&&hasFishingEquipmentQuiet(player,row[2]))selected=row;
        if(selected==null){player.sendMessage("Você precisa da ferramenta adequada para pescar aqui.");return false;}
        final int[] recipe=selected.clone();
        if(player.playerLevel[10]<recipe[1]||(recipe[3]>0&&!player.getItems().playerHasItem(recipe[3])))return false;
        final io.xeros.model.entity.npc.NPC spot=player.clickedNpcIndex>0&&player.clickedNpcIndex<io.xeros.model.entity.npc.NPCHandler.npcs.length?io.xeros.model.entity.npc.NPCHandler.npcs[player.clickedNpcIndex]:null;
        if(player.clickedNpcIndex>0&&(spot==null||spot.isDead||spot.heightLevel!=player.heightLevel||player.distance(spot.getPosition())>2))return false;
        final int sx=spot==null?0:spot.absX,sy=spot==null?0:spot.absY;
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();
        final long token=player.productionActionToken;final int x=player.absX,y=player.absY,h=player.heightLevel;
        player.playerSkilling[10]=true;player.stopPlayerSkill=true;player.startAnimation(recipe[6]);
        player.sendMessage("Você começa a pescar.");
        Server.getEventHandler().submit(new Event<Player>("skilling",player,Math.max(1,getTimer(player,method)+5+playerFishingLevel(player))){
            public void execute(){
                if(token!=player.productionActionToken||!player.playerSkilling[10]||!player.stopPlayerSkill||!player.getUtilityMagic().canInteract()
                    ||player.absX!=x||player.absY!=y||player.heightLevel!=h||player.playerLevel[10]<recipe[1]||!hasFishingEquipmentQuiet(player,recipe[2])
                    ||spot!=null&&(spot.isDead||!spot.isRegistered()||spot.absX!=sx||spot.absY!=sy||spot.heightLevel!=h)
                    ||recipe[3]>0&&!player.getItems().playerHasItem(recipe[3])){stop();return;}
                int fish=recipe[4],experience=recipe[5];
                if(recipe[7]>0&&player.playerLevel[10]>=recipe[8]&&random.applyAsInt(2)==1){fish=recipe[7];experience=recipe[9];}
                int baseAmount=method==14?1+Math.max(1,Math.min(99,player.playerLevel[10]))/5:1;
                int amount=baseAmount*((SkillcapePerks.FISHING.isWearing(player)||SkillcapePerks.isWearingMaxCape(player))?2:1);
                int[][] bait=recipe[3]>0?new int[][]{{recipe[3],1}}:new int[0][];
                if(amount>baseAmount&&!ProductionBatch.canExchange(player,bait,new int[][]{{fish,amount}}))amount=baseAmount;
                if(!ProductionBatch.exchange(player,bait,new int[][]{{fish,amount}})){stop();return;}
                player.getPA().addSkillXPMultiplied(experience*SkillingOutfit.ANGLER.multiplier(player),10,true);
                Achievements.increase(player,AchievementType.FISH,1);player.startAnimation(recipe[6]);
                if(fish==383)DailyTasks.increase(player,DailyTasks.PossibleTasks.SHARKS);
                if(fish==389&&Boundary.isIn(player,Boundary.FALADOR_BOUNDARY))player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.FISH_MANTA);
                if(fish==371&&Boundary.isIn(player,Boundary.CATHERBY_BOUNDARY))player.getDiaryManager().getKandarinDiary().progress(KandarinDiaryEntry.FISH_SWORD);
                if(fish==377&&Boundary.isIn(player,Boundary.KARAMJA_BOUNDARY))player.getDiaryManager().getKaramjaDiary().progress(KaramjaDiaryEntry.FISH_LOBSTER_KAR);
                if(fish==3142&&Boundary.isIn(player,Boundary.RESOURCE_AREA_BOUNDARY))player.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.KARAMBWAN);
                catchRewards(player,recipe[10]);
                if(recipe[3]>0&&!player.getItems().playerHasItem(recipe[3]))stop();
            }
            public void stop(){super.stop();if(token==player.productionActionToken){player.playerSkilling[10]=false;player.stopPlayerSkill=false;player.startAnimation(65535);}}
        });return true;
    }
    private static boolean hasFishingEquipmentQuiet(Player p,int item){
        return item<=0||p.getItems().playerHasItem(item)||p.getItems().isWearingItem(item,Player.playerWeapon)
            ||item==311&&(p.getItems().playerHasItem(21028)||p.getItems().isWearingItem(21028,Player.playerWeapon)
                ||p.getItems().playerHasItem(21031)||p.getItems().isWearingItem(21031,Player.playerWeapon)
                ||p.getItems().playerHasItem(10129)||p.getItems().isWearingItem(10129,Player.playerWeapon));
    }
    private static void catchRewards(Player player,int petChance){
							clueBottles(player, petChance);
							foeArtefact(player, petChance);

							if (Misc.random(petChance / 6 ) == 1) {
								player.getItems().addItemUnderAnyCircumstance(anglerOuftit[Misc.random(anglerOuftit.length - 1)], 1);
								player.sendMessage("You notice a angler piece floating in the water and pick it up.");
							}

							if (Misc.hasOneOutOf(25)) {//Fishing caskets
								player.getItems().addItemUnderAnyCircumstance(7956, 1);
								player.sendMessage("@red@You notice a fishing casket floating in the water and manage to fish it out.");
							}

							if (Misc.hasOneOutOf(35)) {//Fishing tomes
								player.getItems().addItemUnderAnyCircumstance(7779, 1);
								player.sendMessage("@red@You spot a fishing tome in the water and try to pull it out.");
							}

							int petRate = player.skillingPetRateScroll ? (int) (petChance * .75) : petChance;
							if (Misc.random(petRate) == 2 && player.getItems().getItemCount(13320, true) == 0 && player.petSummonId != 13320) {
								 PlayerHandler.executeGlobalMessage("[<col=CC0000>News</col>] <col=255>" + player.getDisplayName() + "</col> caught a fish and a <col=CC0000>Heron</col> pet!");
								 player.getItems().addItemUnderAnyCircumstance(13320, 1);
								 player.getCollectionLog().handleDrop(player, 5, 13320, 1);
							 }
    }

	private static int playerFishingLevel(Player c) {
		return (10 - (int) Math.floor(c.playerLevel[Player.playerFishing] / 10));
	}

	private final static int getTimer(Player c, int npcId) {
		switch (npcId) {
		case 1:
			return 2;
		case 2:
			return 3;
		case 3:
			return 4;
		case 4:
			return 4;
		case 5:
			return 4;
		case 6:
			return 5;
		case 7:
			return 5;
		case 8:
			return 5;
		case 9:
			return 5;
		case 10:
			return 5;
		case 11:
			return 9;
		case 12:
			return 9;
		default:
			return -1;
		}
	}

}
