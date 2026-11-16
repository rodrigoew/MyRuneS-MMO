package io.xeros.content.minigames.pest_control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.xeros.content.achievement_diary.impl.WesternDiaryEntry;
import io.xeros.content.skills.Skill;
import io.xeros.model.Items;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.mode.ModeType;
import io.xeros.model.items.GameItem;
import io.xeros.util.Misc;

public class PestControlRewards {
	/**
	 * The player that will be managing their pest control rewards
	 */
	private final Player player;

	/**
	 * The current reward the player has
	 */
	private Reward reward;

	/**
	 * The level required to purchase experience
	 */
	static final int REQUIRED_LEVEL = 40;

	/**
	 * Creats a new class for the signified player. A new class is created for each player because there are members of the class that are required for each individual player.
	 * 
	 * @param player the player this class is being created for.
	 */
	public PestControlRewards(Player player) {
		this.player = player;
	}

	/**
	 * Displays the reward interface
	 */
	public void showInterface() {
		player.getPA().sendFrame70(0, -100, 37100);
		player.getPA().sendString("Selecione uma recompensa", 37003);
		player.getPA().sendFrame126(Misc.insertCommas(Integer.toString(player.pcPoints)) + " Pts", 37007);
		player.getPA().showInterface(37000);
	}

	public boolean click(int buttonId) {
		if (buttonId == 144138) {
            if(!player.getUtilityMagic().canInteract())return true;
			if (reward == null) {
				player.sendMessage("Selecione uma recompensa antes de confirmar.");
			} else {
				reward.purchase(player);
				player.getPA().sendFrame126(Misc.insertCommas(Integer.toString(player.pcPoints)) + " Pts", 37007);
			}
			return true;
		}
		for (RewardButton button : RewardButton.values()) {
			if (button.buttonId == buttonId) {
				reward = button.reward;
				player.getPA().sendFrame70(button.xOffset, button.yOffset, 37100);
				player.getPA().sendString(reward.getCost() + " " + (reward.getCost() == 1 ? "point" : "points"), 37003);
				return true;

			}
		}
		return false;
	}

	public enum RewardButton {
		ATTACK_EXPERIENCE_1(144150, 0, 0, new ExperienceReward(1, Skill.ATTACK.getId(), 10_000)),
		ATTACK_EXPERIENCE_10(144151, 0, 0, new ExperienceReward(10, Skill.ATTACK.getId(), 10_000)),
		ATTACK_EXPERIENCE_100(144152, 0, 0, new ExperienceReward(100, Skill.ATTACK.getId(), 10_000)),
		DEFENCE_EXPERIENCE_1(144155, 0, 40, new ExperienceReward(1, Skill.DEFENCE.getId(), 10_000)),
		DEFENCE_EXPERIENCE_10(144156, 0, 40, new ExperienceReward(10, Skill.DEFENCE.getId(), 10_000)),
		DEFENCE_EXPERIENCE_100(144157, 0, 40, new ExperienceReward(100, Skill.DEFENCE.getId(), 10_000)),
		MAGIC_EXPERIENCE_1(144160, 0, 80, new ExperienceReward(1, Skill.MAGIC.getId(), 10_000)),
		MAGIC_EXPERIENCE_10(144161, 0, 80, new ExperienceReward(10, Skill.MAGIC.getId(), 10_000)),
		MAGIC_EXPERIENCE_100(144162, 0, 80, new ExperienceReward(100, Skill.MAGIC.getId(), 10_000)),
		PRAYER_EXPERIENCE_1(144165, 0, 120, new ExperienceReward(1, Skill.PRAYER.getId(), 5_000)),
		PRAYER_EXPERIENCE_10(144166, 0, 120, new ExperienceReward(10, Skill.PRAYER.getId(), 5_000)),
		PRAYER_EXPERIENCE_100(144167, 0, 120, new ExperienceReward(100, Skill.PRAYER.getId(), 5_000)),
		STRENGTH_EXPERIENCE_1(144170, 210, 0, new ExperienceReward(1, Skill.STRENGTH.getId(), 10_000)),
		STRENGTH_EXPERIENCE_10(144171, 210, 0, new ExperienceReward(10, Skill.STRENGTH.getId(), 10_000)),
		STRENGTH_EXPERIENCE_100(144172, 210, 0, new ExperienceReward(100, Skill.STRENGTH.getId(), 10_000)),
		RANGE_EXPERIENCE_1(144175, 210, 40, new ExperienceReward(1, Skill.RANGED.getId(), 10_000)),
		RANGE_EXPERIENCE_10(144176, 210, 40, new ExperienceReward(10, Skill.RANGED.getId(), 10_000)),
		RANGE_EXPERIENCE_100(144177, 210, 40, new ExperienceReward(100, Skill.RANGED.getId(), 10_000)),
		HITPOINTS_EXPERIENCE_1(144180, 210, 80, new ExperienceReward(1, Skill.HITPOINTS.getId(), 10_000)),
		HITPOINTS_EXPERIENCE_10(144181, 210, 80, new ExperienceReward(10, Skill.HITPOINTS.getId(), 10_000)),
		HITPOINTS_EXPERIENCE_100(144182, 210, 80, new ExperienceReward(100, Skill.HITPOINTS.getId(), 10_000)),

				HERB_PACK(144189, 0, 180, new PackReward(30, PackReward.HERB_PACK)), 
				SEED_PACK(144192, 0, 220, new PackReward(15, PackReward.SEED_PACK)),
				MINERAL_PACK(144195, 210, 180, new PackReward(15, PackReward.MINERAL_PACK)), 
				VOID_MACE(144198, 0, 280, new ItemReward(160, new GameItem(8841))), 
				VOID_KNIGHT_ROBE(144201, 0, 320, new ItemReward(175, new GameItem(8840))), 
				VOID_MAGE_HELM(144204, 0, 360, new ItemReward(150, new GameItem(11663))), 
				VOID_MELEE_HELM(144207, 0, 400, new ItemReward(150, new GameItem(11665))), 
				VOID_KNIGHT_TOP(144210, 210, 280, new ItemReward(175, new GameItem(8839))), 
				VOID_KNIGHT_GLOVES(144213, 210, 320, new ItemReward(110, new GameItem(8842))), 
				VOID_RANGE_HELM(144216, 210, 360, new ItemReward(150, new GameItem(11664))
			//	FIGHTER_TORSO(144219, 210, 360, new ItemReward(300, new GameItem(10551))),
				//BARROWS_GLOVES(14422, 210, 360, new ItemReward(80, new GameItem(7462))),
				//FIGHTER_HAT(14422, 210, 360, new ItemReward(60, new GameItem(10548))

		);
		

		/**
		 * The button for this reward
		 */
		private final int buttonId;

		/**
		 * The x and y offset of the button
		 */
		private final int xOffset, yOffset;

		/**
		 * The reward
		 */
		private final Reward reward;

		/**
		 * The button with an id and reward associated with it
		 * 
		 * @param buttonId the button id
		 * @param reward the reward
		 */
        RewardButton(int buttonId, int xOffset, int yOffset, Reward reward) {
			this.buttonId = buttonId;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.reward = reward;
		}

		public Reward getReward() {
			return reward;
		}

	}

	static abstract class Reward {

		/**
		 * The cost of the reward
		 */
		protected final int cost;

		/**
		 * Creates a new reward with an initial cost
		 * 
		 * @param cost the cost of the reward
		 */
		Reward(int cost) {
			this.cost = cost;
		}

		/**
		 * The procedure for purchasing an item
		 */
		abstract void purchase(Player player);

		/**
		 * The cost of the purchase
		 * 
		 * @return the cost in points
		 */
		public int getCost() {
			return cost;
		}
	}

	static class ExperienceReward extends Reward {
		/**
		 * The skill id that will be given the experience
		 */
		private int skillId;

		/**
		 * The default experience for the reward
		 */
		private int experience;

		/**
		 * Creates a new reward with just a cost
		 * 
		 * @param cost the cost of the reward
		 */
		ExperienceReward(int cost) {
			super(cost);
		}

		/**
		 * Creates a new reward with a cost and skillId
		 * 
		 * @param cost the cost of the reward
		 * @param skillId the skill obtaining the experience
		 */
		ExperienceReward(int cost, int skillId, int experience) {
			super(cost);
			this.skillId = skillId;
			this.experience = experience;
			
		}

		@Override
		public void purchase(Player player) {
			if (System.currentTimeMillis() - player.buyPestControlTimer < 5500) {
				return;
			}
			if (player.pcPoints < cost) {
				player.sendMessage("Você não tem pontos de Pest Control suficientes.");
				return;
			}
			if (player.getLevelForXP(player.playerXP[skillId]) < REQUIRED_LEVEL) {
				player.sendMessage("Você precisa de nível " + REQUIRED_LEVEL + " para comprar XP.");
				return;
			}
            boolean lowerRate=player.getMode().getType().getExperienceRate(Skill.ATTACK)!=ModeType.STANDARD.getExperienceRate(Skill.ATTACK);
            if(!player.getPA().addSkillXP((lowerRate?experience/20:experience)*cost,skillId,true)){player.sendMessage("Não foi possível adicionar XP. Seus pontos foram preservados.");return;}
            player.buyPestControlTimer=System.currentTimeMillis();player.pcPoints-=cost;player.getQuestTab().updateInformationTab();
			io.xeros.model.entity.player.save.PlayerSave.saveGame(player);
            player.sendMessage("Você recebeu " + ((lowerRate ? experience / 20 : experience) * cost) + " XP em troca de " + cost + " pontos.");
			if (lowerRate) {
				player.sendMessage("A recompensa de XP segue a taxa do seu modo de jogo.");
			}
		}
	}

	static class ItemReward extends Reward {

		/**
		 * The item received as the reward
		 */
		private GameItem item;

		/**
		 * Creates a new item reward with an initial cost
		 * 
		 * @param cost the cost of the item reward
		 */
		ItemReward(int cost) {
			super(cost);
		}

		/**
		 * Creates a new item reward with an initial cost as well as the item given
		 * 
		 * @param cost the cost of the reward
		 * @param item the item given to the player
		 */
		ItemReward(int cost, GameItem item) {
			super(cost);
			this.item = item;
		}

		@Override
		void purchase(Player player) {
			if (System.currentTimeMillis() - player.buyPestControlTimer < 5500) {
				return;
			}
			if (player.pcPoints < cost) {
				player.sendMessage("Você não tem pontos de Pest Control suficientes.");
				return;
			}
			if (player.getItems().freeSlots() == 0) {
				player.sendMessage("Libere um espaço no inventário para receber a recompensa.");
				return;
			}
			if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[0][],new int[][]{{item.getId(),item.getAmount()}}))return;
            player.pcPoints -= cost;
            player.getQuestTab().updateInformationTab();
            player.buyPestControlTimer = System.currentTimeMillis();
            io.xeros.model.entity.player.save.PlayerSave.saveGame(player);
			ItemDef itemDef = ItemDef.forId(item.getId());
			String name = itemDef == null ? "a item" : itemDef.getName();
			player.sendMessage("Você recebeu " + name + " em troca de " + cost + " pc pontos.");
		}

	}

	static class PackReward extends Reward {

		/**
		 * An array of items that are obtainable from the herb pack
		 */
		static final GameItem[] HERB_PACK = { new GameItem(Items.RESOURCE_BOXLARGE, 1), new GameItem(200, 30), new GameItem(202, 30), new GameItem(204, 30), new GameItem(206, 24), new GameItem(208, 15),
				new GameItem(210, 21), new GameItem(212, 18), new GameItem(214, 15), new GameItem(216, 15), new GameItem(218, 15), new GameItem(220, 10) };

		/**
		 * An array of items that are obtainable from the mineral pack
		 */
		static final GameItem[] MINERAL_PACK = { new GameItem(Items.RESOURCE_BOXLARGE, 1), new GameItem(437, 50), new GameItem(439, 50), new GameItem(441, 40), new GameItem(445, 35), new GameItem(454, 30),
				new GameItem(448, 25), new GameItem(450, 10), new GameItem(452, 5) };

		static final GameItem[] SEED_PACK = { new GameItem(Items.RESOURCE_BOXLARGE, 1), new GameItem(5291, 15), new GameItem(5292, 15), new GameItem(5293, 15), new GameItem(5294, 15), new GameItem(5295, 5),
				new GameItem(5296, 5), new GameItem(5297, 10), new GameItem(5298, 10), new GameItem(5299, 7), new GameItem(5300, 5), new GameItem(5301, 6), new GameItem(5302, 4),
				new GameItem(5303, 4), new GameItem(5304, 3), };

		/**
		 * The pack of items
		 */
		private GameItem[] pack;

		/**
		 * Creates a new reward with a determined cost
		 * 
		 * @param cost the cost of the reward
		 */
		PackReward(int cost) {
			super(cost);
		}

		/**
		 * Creates a new reward with an initial cost and pack of items
		 * 
		 * @param cost the cost of the reward
		 * @param pack the pack fo items received from the reward
		 */
		PackReward(int cost, GameItem[] pack) {
			super(cost);
			this.pack = pack;
		}

		@Override
		void purchase(Player player) {
			if (System.currentTimeMillis() - player.buyPestControlTimer < 5000) {
				return;
			}
			if (player.pcPoints < cost) {
				player.sendMessage("Você não tem pontos de Pest Control suficientes.");
				return;
			}
			if (player.getItems().freeSlots() < 5) {
				player.sendMessage("Libere pelo menos 5 espaços para comprar este pacote.");
				return;
			
			}
            int amount=4+Misc.random(1);
            List<GameItem> pool=new ArrayList<>(Arrays.asList(pack));pool.removeIf(i->i.getId()==Items.RESOURCE_BOXLARGE);
            List<int[]> outputs=new ArrayList<>();outputs.add(new int[]{Items.RESOURCE_BOXLARGE,1});
            while(amount-->0){GameItem item=pool.remove(Misc.random(pool.size()-1));outputs.add(new int[]{item.getId(),1+Misc.random(item.getAmount()-1)});}
            if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[0][],outputs.toArray(new int[0][])))return;
            player.pcPoints-=cost;player.buyPestControlTimer=System.currentTimeMillis();player.getQuestTab().updateInformationTab();
            if(pack==HERB_PACK)player.getDiaryManager().getWesternDiary().progress(WesternDiaryEntry.HERB_PACK);
            if(pack==SEED_PACK)player.getDiaryManager().getWesternDiary().progress(WesternDiaryEntry.SEED_PACK);
            io.xeros.model.entity.player.save.PlayerSave.saveGame(player);

		}
	}

}
