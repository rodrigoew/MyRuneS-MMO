package io.xeros.content.barrows;

import io.xeros.content.collection_log.CollectionLog;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.item.lootable.LootRarity;
import io.xeros.model.Items;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCSpawning;
import io.xeros.model.entity.npc.data.NpcMaxHit;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.items.GameItem;
import io.xeros.model.items.ItemAssistant;
import io.xeros.util.Misc;
import io.xeros.util.discord.Discord;

import java.util.*;

/**
 * Barrows new system
 *
 * @author C.T for koranes.
 */

public class Barrows {
    private static java.util.function.IntUnaryOperator lootRoll=bound->Misc.random(bound-1);
    public static final io.xeros.model.entity.player.Position EXIT=new io.xeros.model.entity.player.Position(3564,3288,0);
    private static boolean inCrypt(Player p){return p.heightLevel==3&&p.absX>=3520&&p.absX<=3585&&p.absY>=9670&&p.absY<=9735;}
    private static boolean inChestRoom(Player p){return p.heightLevel==0&&p.absX>=3520&&p.absX<=3590&&p.absY>=9680&&p.absY<=9730;}


	public final static double BARROWS_CHANCE = 10;//Barrows piece chance
	public final static double AMULET_CHANCE = 100;//Amulet of the damned chance
	public final static double DHAROK_PET_CHANCE = 500;//Dharoks pet chance

	public final static double SLAYER_TIER4_CHANCE = 30;//Tier 4 slayer key chance



	public static void updateBarrowsInterface(Player player) {
		player.getPA().sendFrame126((player.barrowsNpcs[4][1] == 2 ? "@gr3@" : "@red@") + "Dharok", 22046);
		player.getPA().sendFrame126((player.barrowsNpcs[3][1] == 2 ? "@gr3@" : "@red@") + "Guthan", 22047);
		player.getPA().sendFrame126((player.barrowsNpcs[5][1] == 2 ? "@gr3@" : "@red@") + "Ahrim", 22048);
		player.getPA().sendFrame126((player.barrowsNpcs[1][1] == 2 ? "@gr3@" : "@red@") + "Torag", 22049);
		player.getPA().sendFrame126((player.barrowsNpcs[0][1] == 2 ? "@gr3@" : "@red@") + "Verac", 22050);
		player.getPA().sendFrame126((player.barrowsNpcs[2][1] == 2 ? "@gr3@" : "@red@") + "Karil", 22051);
	}

	public static void killedBarrows(Player player, NPC npc) {
		if (player==null || npc == null || !npc.isDead() || npc.getHealth().getCurrentHealth()>0 || npc.spawnedBy!=player.getIndex()) {
			return;
		}
		if (player == null) {
			return;
		}
		for (int o = 0; o < player.barrowsNpcs.length; o++) {
			if (npc.getNpcId() == player.barrowsNpcs[o][0] && player.barrowsNpcs[o][1]==1 && !player.barrowsBrothersKilled[o]) {
				player.barrowsNpcs[o][1] = 2; // 2 for dead
				player.barrowsKillCount++;
				updateBarrowsInterface(player);
				player.barrowsBrothersKilled[o] = true;
			}
		}
	}

	public static void updateSavedBarrowsProgress(Player player) {
        player.barrowsKillCount=0;
		for (int index = 0; index < player.barrowsBrothersKilled.length; index++) {
			if (player.barrowsBrothersKilled[index]) {
				player.barrowsNpcs[index][1] = 2;
				player.barrowsKillCount++;
			}
		}
	}

	/**
	 * Starts the digging emote and action for the Barrows minigame
	 */
	public static void startDigging(final Player player) {
        if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null||player.heightLevel!=0)return;
		if (Boundary.isWithInArea(player, 3553, 3561, 3294, 3301)) {
			player.getPA().movePlayer(3578, 9706, 3);
		} else if (Boundary.isWithInArea(player, 3550, 3557, 3278, 3287)) {
			player.getPA().movePlayer(3568, 9683, 3);
		} else if (Boundary.isWithInArea(player, 3561, 3568, 3285, 3292)) {
			player.getPA().movePlayer(3557, 9703, 3);
		} else if (Boundary.isWithInArea(player, 3570, 3579, 3293, 3302)) {
			player.getPA().movePlayer(3556, 9718, 3);
		} else if (Boundary.isWithInArea(player, 3571, 3582, 3278, 3285)) {
			player.getPA().movePlayer(3534, 9704, 3);
		} else if (Boundary.isWithInArea(player, 3562, 3569, 3273, 3279)) {
			player.getPA().movePlayer(3546, 9684, 3);
		}
	}

	/**
	 * Coffin identity, Barrows brother identitiy.
	 */
	public static final int[][] COFFIN_AND_BROTHERS =
			{
					{20772, 1677}, //veracs
					{20721, 1676}, //torags
					{20771, 1675}, //karils
					{20722, 1674}, //guthans
					{20720, 1673}, //dh
					{20770, 1672}, //ahrim
			};

	/**
	 * Picking the random coffin
	 **/
	public static int getRandomCoffin() {
		return Misc.random(COFFIN_AND_BROTHERS.length - 1);
	}

	/**
	 * Selects the coffin and shows the interface if coffin id matches random coffin
	 **/
	public static boolean selectCoffin(Player player, int coffinId) {
		if (player.randomCoffin < 0 || player.randomCoffin >= COFFIN_AND_BROTHERS.length) {
			player.randomCoffin = getRandomCoffin();
		}

		if (COFFIN_AND_BROTHERS[player.randomCoffin][0] == coffinId) {
			player.getDH().sendDialogues(163, -1);
			return true;
		}
		return false;
	}

	public static void resetBarrows(Player player) {
		for (int index = 0; index < player.barrowsBrothersKilled.length; index++) {
			player.barrowsBrothersKilled[index] = false;
			player.barrowsNpcs[index][1] = 0;
		}
		player.barrowsKillCount = 0;
		player.randomCoffin = getRandomCoffin();
        player.barrowsTimer=0;
	}

	public final static int[] BARROWS_ITEMS =
			{4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759};

	public static int Runes[] =
			{4740, 558, 560, 565, 4740};

	public static int Resources[] =
			{2364, 452, 13442, 537};

	public static int randomBarrows() {
		return BARROWS_ITEMS[(int) (Math.random() * BARROWS_ITEMS.length)];
	}

	public static int randomRunes() {
		return Runes[(int) (Math.random() * Runes.length)];
	}

	public static int randomResources() {
		return Resources[(int) (Math.random() * Resources.length)];
	}

	/**
	 * Perform actions of the objects in Barrows.
	 */
	public static boolean isBarrowsObject(final Player player, int objectType) {
        boolean known=objectType==20973||java.util.Arrays.stream(COFFIN_AND_BROTHERS).anyMatch(c->c[0]==objectType)||objectType>=20667&&objectType<=20672;
        if(!known)return false;
        if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null||!(objectType==20973?inChestRoom(player):inCrypt(player)))return true;
		switch (objectType) {

			//Chest
			case 20973:
				barrowsChest(player);
				return true;

			//Verac
			case 20672: //steps id
				player.getPA().movePlayer(3556, 3298, 0);
				if (player.barrowsNpcs[0][1] != 2) {
					player.barrowsNpcs[0][1] = 0;
				}
				return true;
			case 20772:

				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[0][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1677))player.barrowsNpcs[0][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

			//Torag
			case 20671: //steps id
				player.getPA().movePlayer(3553, 3283, 0);
				if (player.barrowsNpcs[1][1] != 2) {
					player.barrowsNpcs[1][1] = 0;
				}
				return true;
			case 20721:

				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[1][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1676))player.barrowsNpcs[1][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

			//Karil
			case 20670: //steps id
				player.getPA().movePlayer(3565, 3276, 0);
				if (player.barrowsNpcs[2][1] != 2) {
					player.barrowsNpcs[2][1] = 0;
				}
				return true;

			case 20771:

				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[2][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1675))player.barrowsNpcs[2][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

			//Guthan
			case 20669: //steps id
				player.getPA().movePlayer(3578, 3284, 0);
				if (player.barrowsNpcs[3][1] != 2) {
					player.barrowsNpcs[3][1] = 0;
				}
				return true;
			case 20722:
				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[3][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1674))player.barrowsNpcs[3][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

			//Dharok
			case 20668: //steps id
				player.getPA().movePlayer(3574, 3298, 0);
				if (player.barrowsNpcs[4][1] != 2) {
					player.barrowsNpcs[4][1] = 0;
				}
				return true;
			case 20720:

				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[4][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1673))player.barrowsNpcs[4][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

			//Ahrim
			case 20667: //steps id
				player.getPA().movePlayer(3565, 3290, 0);
				if (player.barrowsNpcs[5][1] != 2) {
					player.barrowsNpcs[5][1] = 0;
				}
				return true;
			case 20770:
				if (selectCoffin(player, objectType)) {
					return true;
				}
				if (player.barrowsNpcs[5][1] == 0) {
					if(spawnBarrowsNpcOnCorrectTile(player, 1672))player.barrowsNpcs[5][1] = 1;
				} else {
					player.sendMessage("Você já procurou neste sarcófago.");
				}
				return true;

		}
		return false;
	}

	/**
	 * Spawn the given npcType on a tile next to the player that the player can walk to.
	 */
	private static boolean spawnBarrowsNpcOnCorrectTile(Player player, int npcType) {
        return NPCSpawning.spawnBarrows(player, npcType, player.getX(), player.getY(), player.getHeight(), true, true, NpcMaxHit.getMaxHit(npcType))!=null;


	}

    private static void barrowsChest(Player player) {
        if(!player.getUtilityMagic().canInteract()||!inChestRoom(player))return;
        if(player.randomCoffin<0||player.randomCoffin>=6)player.randomCoffin=getRandomCoffin();
        updateSavedBarrowsProgress(player);
        if(player.barrowsKillCount<5){player.sendMessage("Derrote os irmãos antes de abrir o baú.");return;}
        if(player.barrowsKillCount==5){
            if(player.barrowsNpcs[player.randomCoffin][1]==0){if(spawnBarrowsNpcOnCorrectTile(player,player.barrowsNpcs[player.randomCoffin][0]))player.barrowsNpcs[player.randomCoffin][1]=1;}
            else player.sendMessage("Derrote o último irmão para receber a recompensa.");
            return;
        }
        ArrayList<GameItem> prizes=new ArrayList<>();
        prizes.add(new GameItem(randomRunes(),Misc.random(10)+50));prizes.add(new GameItem(randomResources(),Misc.random(10)+50));
        int runs=player.getBarrowsRunCompleted()+1;int bonus=0;
        switch(runs){case 10:bonus=350000;break;case 25:bonus=800000;break;case 50:bonus=2000000;break;case 100:bonus=3000000;break;case 250:bonus=5000000;break;case 500:bonus=10000000;break;case 1000:bonus=20000000;break;}
        if(bonus>0)prizes.add(new GameItem(995,bonus));
        if(lootRoll.applyAsInt((int)AMULET_CHANCE)==0)prizes.add(new GameItem(12853));
        if(lootRoll.applyAsInt((int)DHAROK_PET_CHANCE)==0)prizes.add(new GameItem(16015));
        if(lootRoll.applyAsInt((int)SLAYER_TIER4_CHANCE)==0)prizes.add(new GameItem(21053));
        if(lootRoll.applyAsInt((int)BARROWS_CHANCE)==0)prizes.add(new GameItem(BARROWS_ITEMS[lootRoll.applyAsInt(BARROWS_ITEMS.length)]));
        saveBarrowsRunTime(player);
        resetBarrows(player);player.setBarrowsRunCompleted(runs);
        io.xeros.content.minigames.SoloActivityRewards.give(player,prizes,EXIT);
        ArrayList<String> list=new ArrayList<>();
        for(GameItem prize:prizes){list.add(prize.getId()+" "+prize.getAmount());if(prize.getId()==12853||java.util.Arrays.stream(BARROWS_ITEMS).anyMatch(id->id==prize.getId()))player.getCollectionLog().handleDrop(player,CollectionLog.BARROWS_ID,prize.getId(),prize.getAmount());}
        player.sendMessage("Baú concluído. Total de viagens a Barrows: "+runs+".");
        player.getPA().movePlayer(EXIT.getX(),EXIT.getY(),0);
        DailyTasks.increase(player,DailyTasks.PossibleTasks.BARROWS_CHESTS);
        updateBarrowsInterface(player);displayReward(player,list);
        io.xeros.model.entity.player.save.PlayerSave.saveGame(player);
    }

	public static void saveBarrowsRunTime(Player player) {
		if (player.barrowsTimer == 0) {
			return;
		}
		long barrowsRunTime = (System.currentTimeMillis() - player.barrowsTimer) / 1000;
		int oldTime = player.barrowsPersonalRecord;
		boolean broken = false;
		if ((int) barrowsRunTime < player.barrowsPersonalRecord || oldTime == 0) {
			player.barrowsPersonalRecord = (int) barrowsRunTime;
			broken = true;
		}
		if (broken) {
			if (oldTime != 0) {
				player.sendMessage("<col=005f00>You have broken your previous record of " + oldTime + " seconds with " + player.barrowsPersonalRecord + " seconds.");
				io.xeros.content.minigames.SoloActivityRewards.give(player,java.util.List.of(new GameItem(995,150000)),EXIT);//150k for beating old record.
				player.sendMessage("You have received 150k for beating your previous record!");
			} else {
				player.sendMessage("You have completed the Barrows run in @red@" + player.barrowsPersonalRecord + " seconds.");
			}
		} else {
			player.sendMessage("You have completed the Barrows run in @red@" + barrowsRunTime + " seconds.");
		}

	}

	public static void startBarrowsTimer(Player attacker, NPC npc) {
		if (attacker.barrowsKillCount > 0) {
			return;
		}
		boolean brotherFound = false;
		for (int index = 0; index < COFFIN_AND_BROTHERS.length; index++) {
			if (npc.getNpcId() == COFFIN_AND_BROTHERS[index][1]) {
				brotherFound = true;
				break;
			}
		}

		if (!brotherFound) {
			return;
		}
		if (npc.getHealth().getCurrentHealth() < npc.getHealth().getMaximumHealth()) {
			return;
		}
		attacker.barrowsTimer = System.currentTimeMillis();
	}

	public static void resetCoffinStatus(Player player) {
        for(NPC n:io.xeros.model.entity.npc.NPCHandler.npcs)if(n!=null&&n.spawnedBy==player.getIndex()&&!n.isDead()&&java.util.Arrays.stream(COFFIN_AND_BROTHERS).anyMatch(c->c[1]==n.getNpcId()))n.unregister();
		for (int index = 0; index < player.barrowsNpcs.length; index++) {
			if (player.barrowsNpcs[index][1] == 1) {
				player.barrowsNpcs[index][1] = 0;
			}
		}
	}

















	public static void displayReward(Player player, ArrayList<?> list) {
		player.getOutStream().createFrameVarSizeWord(53);
		player.getOutStream().writeWord(6963);
		player.getOutStream().writeWord(list.size());
		for (int i = 0; i < list.size(); i++) {
			String[] args = ((String) list.get(i)).split(" ");
			int item = Integer.parseInt(args[0]);
			int amount = Integer.parseInt(args[1]);
			if (amount > 254) {
				player.getOutStream().writeByte(255);
				player.getOutStream().writeDWord_v2(amount);
			} else {
				player.getOutStream().writeByte(amount);
			}
			player.getOutStream().writeWordBigEndianA(item + 1);
		}
		player.getOutStream().endFrameVarSizeWord();
		player.flushOutStream();
		player.getPA().showInterface(6960);
	}












	private static final Map<LootRarity, List<GameItem>> items = new HashMap<>();



	// Collection log items had to be handled this way.
	static {
		items.put(LootRarity.RARE, Arrays.asList(
				new GameItem(4716, 1),//dh helm
				new GameItem(4720, 1),//dh body
				new GameItem(4722, 1),//dh legs
				new GameItem(4718, 1),//dh axe

				new GameItem(4724, 1),//guthan helm
				new GameItem(4728, 1),//guthan body
				new GameItem(4730, 1),//guthan skirt
				new GameItem(4726, 1),//guthan spear

				new GameItem(4753, 1),//verac helm
				new GameItem(4757, 1),//verac body
				new GameItem(4759, 1),//verac skirt
				new GameItem(4755, 1),//verac flail

				new GameItem(4745, 1),//torag helm
				new GameItem(4749, 1),//torag body
				new GameItem(4751, 1),//torag legs
				new GameItem(4747, 1),//torag hammers

				new GameItem(4732, 1),//karil coif
				new GameItem(4736, 1),//karil body
				new GameItem(4738, 1),//karil skirt
				new GameItem(4734, 1),//karil crossbow

				new GameItem(4708, 1),//ahrim hood
				new GameItem(4712, 1),//ahrim top
				new GameItem(4714, 1),//ahrim bottom
				new GameItem(4710, 1),//ahrim staff

				new GameItem(12853, 1)//Amulet of the damned

		));
	}



	public static ArrayList<GameItem> getRareDrops() {
		ArrayList<GameItem> drops = new ArrayList<>();
		List<GameItem> found = items.get(LootRarity.RARE);
		for(GameItem f : found) {
			boolean foundItem = false;
			for(GameItem drop : drops) {
				if (drop.getId() == f.getId()) {
					foundItem = true;
					break;
				}
			}
			if (!foundItem) {
				drops.add(f);
			}
		}
		return drops;
	}








}
