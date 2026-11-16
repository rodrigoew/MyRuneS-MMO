package io.xeros.content.skills.thieving;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import io.xeros.content.SkillcapePerks;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.ArdougneDiaryEntry;
import io.xeros.content.achievement_diary.impl.DesertDiaryEntry;
import io.xeros.content.achievement_diary.impl.FaladorDiaryEntry;
import io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry;
import io.xeros.content.achievement_diary.impl.VarrockDiaryEntry;
import io.xeros.content.achievement_diary.impl.WesternDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.event.eventcalendar.EventChallenge;
import io.xeros.content.skills.Skill;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.Items;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.model.items.GameItem;
import io.xeros.util.Location3D;
import io.xeros.util.Misc;
import org.apache.commons.lang3.RandomUtils;

/**
 * A representation of the thieving skill. Support for both object and npc actions will be supported.
 * 
 * @author Jason MacKeigan
 * @date Feb 15, 2015, 7:12:14 PM
 */
public class Thieving {
	
	private static final int[] rogueOutfit = { 5553, 5554, 5555, 5556, 5557 };

	/**
	 * The managing player of this class
	 */
	private final Player player;

	/**
	 * The last interaction that player made that is recorded in milliseconds
	 */
	private long lastInteraction;
    private long stunnedUntil;
    private NPC knockedTarget;
    private long knockedUntil;
    private int knockoutPicks;
    private java.util.function.DoubleSupplier successRoll = Math::random;
    private boolean allowed(){return player.getUtilityMagic().canInteract()&&!player.getMovementState().isLocked()&&!player.getBankPin().requiresUnlock()&&!player.getInterfaceEvent().isActive();}
    public boolean isStunned() { return System.currentTimeMillis() < stunnedUntil; }
    public io.xeros.model.entity.player.lock.PlayerLock stunLock(io.xeros.model.entity.player.lock.PlayerLock delegate) {
        return new io.xeros.model.entity.player.lock.PlayerLock() {
            public boolean cannotLogout(Player p){return delegate.cannotLogout(p);}
            public boolean cannotInteract(Player p){return isStunned() || delegate.cannotInteract(p);}
            public boolean cannotClickItem(Player p,int item){return delegate.cannotClickItem(p,item);}
            public boolean cannotTeleport(Player p){return isStunned() || delegate.cannotTeleport(p);}
        };
    }

	/**
	 * The constant delay that is required inbetween interactions
	 */
	private static final long INTERACTION_DELAY = 1_500L;

	/**
	 * The stealing animation
	 */
	private static final int ANIMATION = 881;

	/**
	 * Constructs a new {@link Thieving} object that manages interactions between players and stalls, as well as players and non playable characters.
	 * 
	 * @param player the visible player of this class
	 */
	public Thieving(final Player player) {
		this.player = player;
	}

	/**
	 * A method for stealing from a stall
	 * 
	 * @param stall the stall being stolen from
	 * @param location the location of the stall
	 */
	public void steal(Stall stall, Location3D location) {
        if(location==null||stall==null||!allowed()||player.heightLevel!=location.getZ()||Math.abs(player.absX-location.getX())>2||Math.abs(player.absY-location.getY())>2)return;
		if (isStunned() || System.currentTimeMillis() - lastInteraction < INTERACTION_DELAY) {
			//player.sendMessage("You must wait a few more seconds before you can steal again.");
			return;
		}

		if (player.playerLevel[Skill.THIEVING.getId()] < stall.level) {
			player.sendMessage("Você precisa de nível " + stall.level + " em Thieving para furtar esta barraca.");
			return;
		}
		if (Misc.random(200) == 0 && player.getInterfaceEvent().isExecutable()) {
			player.getInterfaceEvent().execute();
			return;
		}
        // Deliver the guaranteed loot before optional tomes/pets can take the last slot.
        if (!ProductionBatch.exchange(player,new int[0][],new int[][]{{stall.item.getId(),stall.item.getAmount()}})) return;
		player.getEventCalendar().progress(EventChallenge.THIEVE_X_STALLS);
		switch (stall) {
		case Food:
			player.getDiaryManager().getVarrockDiary().progress(VarrockDiaryEntry.TEA_STALL);
			break;
		case Crafting:
			if (Boundary.isIn(player, Boundary.ARDOUGNE_BOUNDARY)) {
				player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.STEAL_CAKE);
			}
			break;
		case Magic:
			if (Boundary.isIn(player, Boundary.ARDOUGNE_BOUNDARY)) {
				player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.STEAL_GEM_ARD);
			}
			if (Boundary.isIn(player, Boundary.FALADOR_BOUNDARY)) {
				player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.STEAL_GEM_FAL);
			}
			break;
		case Scimitar:
			break;
		case Fur:
			if (Boundary.isIn(player, Boundary.ARDOUGNE_BOUNDARY)) {
				player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.STEAL_FUR);
			}
			DailyTasks.increase(player, DailyTasks.PossibleTasks.FUR);//Daily tasks
			break;
		case Gold:
		default:
			break;
		}
		player.facePosition(location.getX(), location.getY());
/**		if (Misc.random(stall.depletionProbability) == 0) {
			GlobalObject stallObj = Server.getGlobalObjects().get(objectId, location.getX(), location.getY(), location.getZ());
			if (stallObj != null) {
				Server.getGlobalObjects().add(new GlobalObject(4797, location.getX(), location.getY(), location.getZ(), stallObj.getFace(), 10, 8, stallObj.getObjectId()));
			}
		}
 */
		GameItem item = stall.item;
		ItemDef definition = ItemDef.forId(item.getId());
		int petRate = player.skillingPetRateScroll ? (int) (stall.petChance * .75) : stall.petChance;
		if (Misc.random(petRate) == 20 && player.getItems().getItemCount(20663, false) == 0 && player.petSummonId != 20663) {
			 PlayerHandler.executeGlobalMessage("[<col=CC0000>News</col>] <col=255>" + player.getDisplayName() + "</col> now goes hand in hand with a <col=CC0000>Rocky</col> pet!");
			 player.getItems().addItemUnderAnyCircumstance(20663, 1);
			 player.getCollectionLog().handleDrop(player, 5, 20663, 1);
		 }

		if (Misc.hasOneOutOf(20)) {//Thieving tomes
			player.getItems().addItemUnderAnyCircumstance(7785, 1);
			player.sendMessage("@red@You notice an thieving tome hidden under the stall.");
		}

		player.startAnimation(ANIMATION);
		player.getPA().addSkillXPMultiplied(stall.experience, Skill.THIEVING.getId(), true);
		player.sendMessage("Você furtou " + definition.getName() + " da barraca.");
		Achievements.increase(player, AchievementType.THIEV, 1);
		lastInteraction = System.currentTimeMillis();
	}

	public int getRoguesPieces() {
		int pieces = 0;
		for (int aRogueOutfit : rogueOutfit) {
			if (player.getItems().isWearingItem(aRogueOutfit)) {
				pieces++;
			}
		}
		return pieces;
	}

	/**
	 * A method for pick pocketing npc's
	 * 
	 * @param pickpocket the pickpocket type
	 * @param npc the npc being pick pocketed
	 */
	public void steal(Pickpocket pickpocket, NPC npc) {
        steal(pickpocket, npc, successRoll);
    }

    void steal(Pickpocket pickpocket, NPC npc, java.util.function.DoubleSupplier random) {
        if (pickpocket == null || npc == null || npc.isDead() || !allowed() || npc.heightLevel!=player.heightLevel || Math.max(Math.abs(player.absX-npc.absX),Math.abs(player.absY-npc.absY))>2) return;
		if (isStunned() || System.currentTimeMillis() - lastInteraction < INTERACTION_DELAY) {
			//player.sendMessage("You must wait a few more seconds before you can steal again.");
			return;
		}
		if (player.playerLevel[Skill.THIEVING.getId()] < pickpocket.level) {
			player.sendMessage("Você precisa de nível " + pickpocket.level + " em Thieving para furtar este NPC.");
			return;
		}
		if (Misc.random(200) == 0 && player.getInterfaceEvent().isExecutable()) {
			player.getInterfaceEvent().execute();
			return;
		}
        GameItem item=pickpocket.getRandomItem();
        int reward=item==null?0:item.getAmount()*(Misc.random(99)<getRoguesPieces()*20?2:1);
        int[][] output=item==null?new int[0][]:new int[][]{{item.getId(),reward}};
        if (!ProductionBatch.canExchange(player,new int[0][],output)) {
            player.sendMessage("Sua mochila não tem espaço para o loot.");return;
        }
        PickpocketRules rules = PickpocketRules.forTarget(pickpocket);
        boolean knocked = npc==knockedTarget && System.currentTimeMillis()<knockedUntil && knockoutPicks>0;
        if (!knocked && random.getAsDouble() >= io.xeros.content.skills.thieving.GlovesOfSilence.chance(player, rules.chance(player.playerLevel[Skill.THIEVING.getId()]))) {
            io.xeros.content.skills.thieving.GlovesOfSilence.failed(player);
            lastInteraction = System.currentTimeMillis();
            stunnedUntil = lastInteraction + rules.ticks * 600L;
            player.getPA().stopSkilling();
            player.stopMovement();
            player.startAnimation(424);
            player.sendMessage("O furto falhou. Você ficou atordoado.");
            player.appendDamage(npc, rules.lowDamage + Misc.random(rules.highDamage - rules.lowDamage), io.xeros.content.combat.Hitmark.HIT);
            return;
        }

        if (!ProductionBatch.exchange(player,new int[0][],output)) return;
		switch (pickpocket) {
		case FARMER:
			if (Boundary.isIn(player, Boundary.ARDOUGNE_BOUNDARY)) {
				player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.PICKPOCKET_ARD);
			}
			if (Boundary.isIn(player, Boundary.FALADOR_BOUNDARY)) {
				player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.PICKPOCKET_MASTER_FARMER_FAL);
			}
			if (Boundary.isIn(player, Boundary.DRAYNOR_BOUNDARY)) {
				player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.PICKPOCKET_FARMER_DRAY);
			}
			DailyTasks.increase(player, DailyTasks.PossibleTasks.MASTER_FARMERS);//Daily tasks
			break;
		case MAN:
			if (Boundary.isIn(player, Boundary.FALADOR_BOUNDARY)) {
				player.getDiaryManager().getFaladorDiary().progress(FaladorDiaryEntry.PICKPOCKET_MAN);
			}
			if (Boundary.isIn(player, Boundary.LUMRIDGE_BOUNDARY)) {
				player.getDiaryManager().getLumbridgeDraynorDiary().progress(LumbridgeDraynorDiaryEntry.PICKPOCKET_MAN_LUM);
			}
			break;
		case GNOME:
			player.getDiaryManager().getWesternDiary().progress(WesternDiaryEntry.PICKPOCKET_GNOME);
			break;
		case HERO:
			if (Boundary.isIn(player, Boundary.ARDOUGNE_BOUNDARY)) {
				player.getDiaryManager().getArdougneDiary().progress(ArdougneDiaryEntry.PICKPOCKET_HERO);
			}
			break;
		case MENAPHITE_THUG:
			player.getDiaryManager().getDesertDiary().progress(DesertDiaryEntry.PICKPOCKET_THUG);
			break;
		default:
			break;
		
		}

		player.facePosition(npc.getX(), npc.getY());
		player.startAnimation(ANIMATION);
        if(knocked)knockoutPicks--;
        if(item==null)player.sendMessage("Você não encontrou nada útil.");
		int petRate = player.skillingPetRateScroll ? (int) (pickpocket.petChance * .75) : pickpocket.petChance;
		if (Misc.random(petRate) == 20 && player.getItems().getItemCount(20663, false) == 0 && player.petSummonId != 20663) {
			 PlayerHandler.executeGlobalMessage("[<col=CC0000>News</col>] @cr20@ <col=255>" + player.getDisplayName() + "</col> now goes hand in hand with a <col=CC0000>Rocky</col> pet!");
			 player.getItems().addItemUnderAnyCircumstance(20663, 1);
			 player.getCollectionLog().handleDrop(player, 5, 20663, 1);
		 }

		if (Misc.hasOneOutOf(28)) {//Thieving tomes
			player.getItems().addItemUnderAnyCircumstance(7785, 1);
			player.sendMessage("@red@You manage to steal an thieving tome.");
		}

		Achievements.increase(player, AchievementType.THIEV, 1);
		player.getPA().addSkillXPMultiplied(pickpocket.experience, Skill.THIEVING.getId(), true);
		lastInteraction = System.currentTimeMillis();
	}

	private enum Rarity {
		ALWAYS(0), COMMON(5), UNCOMMON(10), RARE(15), VERY_RARE(25);

		/**
		 * The rarity
		 */
		private final int rarity;

		/**
		 * Creates a new rarity
		 * 
		 * @param rarity the rarity
		 */
		Rarity(int rarity) {
			this.rarity = rarity;
		}
	}

	@SuppressWarnings("serial")
    private static Map<Rarity,List<GameItem>> coins(int n){Map<Rarity,List<GameItem>> table=new java.util.EnumMap<>(Rarity.class);table.put(Rarity.ALWAYS,java.util.Collections.singletonList(new GameItem(995,n)));return table;}
    public boolean npc(NPC npc,int option){
        if(npc==null)return false;
        String name=npc.getName().toLowerCase(java.util.Locale.ROOT);
        Pickpocket target=name.equals("knight of ardougne")?Pickpocket.KNIGHT:name.equals("guard")?Pickpocket.GUARD:name.equals("warrior")?Pickpocket.WARRIOR:null;
        if(option==2&&target!=null){steal(target,npc);return true;}
        if(option==3&&name.equals("menaphite thug")){knockout(npc);return true;}
        return false;
    }
    public boolean knockout(NPC npc){
        if(npc==null||!npc.getName().equalsIgnoreCase("Menaphite thug")||npc.isDead()||!allowed()||isStunned()||npc.heightLevel!=player.heightLevel||Math.max(Math.abs(player.absX-npc.absX),Math.abs(player.absY-npc.absY))>2)return false;
        if(System.currentTimeMillis()-lastInteraction<INTERACTION_DELAY)return false;
        if(player.playerLevel[17]<65){player.sendMessage("Você precisa de nível 65 em Roubo.");return false;}
        int weapon=player.playerEquipment[Player.playerWeapon];
        if(!java.util.Set.of(4599,4600,6408,6410,6412,6414,6416,6418,6420).contains(weapon)){player.sendMessage("Equipe um blackjack para nocautear este NPC.");return false;}
        lastInteraction=System.currentTimeMillis();player.startAnimation(401);
        if(successRoll.getAsDouble()>=.75){knockedTarget=null;knockoutPicks=0;stunnedUntil=lastInteraction+2400;player.appendDamage(npc,4,io.xeros.content.combat.Hitmark.HIT);player.sendMessage("O nocaute falhou.");return false;}
        knockedTarget=npc;knockedUntil=lastInteraction+4800;knockoutPicks=2;npc.freezeTimer=Math.max(npc.freezeTimer,8);npc.startAnimation(838);
        player.getPA().addSkillXPMultiplied(55,17,true);player.sendMessage("Nocaute bem-sucedido. Você pode furtar duas vezes antes de ele acordar.");return true;
    }
	public enum Pickpocket {
		WARRIOR(25,26,20000, coins(18)),
        GUARD(40,46.8,20000, coins(30)),
        KNIGHT(55,84.3,20000, coins(50)),
        MAN(1, 8, 20000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.ALWAYS, Arrays.asList(new GameItem(995, 3)));
			}
		}),

		DRUNKEN_DWARF(75, 90, 20000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.ALWAYS, Arrays.asList(
						new GameItem(995, 950),
						new GameItem(Items.COAL),
						new GameItem(Items.IRON_ORE),
						new GameItem(Items.BRONZE_BAR),
						new GameItem(Items.SEAWEED),
						new GameItem(Items.BEER),
						new GameItem(Items.BRONZE_PICKAXE),
						new GameItem(Items.RAW_COD)));

				put(Rarity.VERY_RARE, Arrays.asList(
						new GameItem(995, 7850),
						new GameItem(Items.RUNITE_ORE),
						new GameItem(Items.COAL_NOTED, 25),
						new GameItem(Items.RUNITE_BAR),
						new GameItem(Items.RUNE_PICKAXE)));
			}
		}),

		FARMER(38, 43, 20000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.COMMON, Lists.newArrayList(
						new GameItem(Items.GUAM_SEED, 3), new GameItem(Items.MARRENTILL_SEED, 3),new GameItem(Items.TARROMIN_SEED, 3),
						new GameItem(Items.HARRALANDER_SEED, 3),

						new GameItem(Items.POTATO_SEED, 4),new GameItem(Items.ONION_SEED, 4),new GameItem(Items.CABBAGE_SEED, 4),
						new GameItem(Items.TOMATO_SEED, 4),new GameItem(Items.SWEETCORN_SEED, 4),new GameItem(Items.STRAWBERRY_SEED, 4),
						new GameItem(Items.WATERMELON_SEED, 4),new GameItem(Items.MARIGOLD_SEED, 4),new GameItem(Items.NASTURTIUM_SEED, 4),
						new GameItem(Items.WOAD_SEED, 4),new GameItem(Items.LIMPWURT_SEED, 4)
				));

				put(Rarity.UNCOMMON, Lists.newArrayList(
						new GameItem(Items.RANARR_SEED, 3),new GameItem(Items.TOADFLAX_SEED, 3),
						new GameItem(Items.IRIT_SEED, 3),new GameItem(Items.AVANTOE_SEED, 3),
						new GameItem(Items.KWUARM_SEED, 3),new GameItem(Items.SNAPDRAGON_SEED, 3),
						new GameItem(Items.CADANTINE_SEED, 3),new GameItem(Items.LANTADYME_SEED, 3),
						new GameItem(Items.DWARF_WEED_SEED, 3),new GameItem(Items.TORSTOL_SEED, 3)
				));
			}

		}), MENAPHITE_THUG(65, 137.5, 18000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.ALWAYS, Arrays.asList(new GameItem(995, 60)));
			} 
		}), GNOME(75, 85, 18000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.ALWAYS, Arrays.asList(new GameItem(995, 1200), new GameItem(995, 800), new GameItem(995, 1250)));
				put(Rarity.UNCOMMON, Arrays.asList(new GameItem(444), new GameItem(557), new GameItem(13431, 5)));
			}
		}), HERO(80, 100, 17000, new HashMap<Rarity, List<GameItem>>() {
			{
				put(Rarity.ALWAYS, Arrays.asList(new GameItem(995, 1500), new GameItem(995, 1800), new GameItem(995, 3500)));
				put(Rarity.UNCOMMON, Arrays.asList(new GameItem(560, 2), new GameItem(565), new GameItem(444), new GameItem(1601)));
			}
		});

		/**
		 * The level required to pickpocket
		 */
		private final int level;

		/**
		 * The experience gained from the pick pocket
		 */
		private final double experience;
		
		/**
		 * The chance of receiving a pet
		 */
		private final int petChance;

		/**
		 * The list of possible items received from the pick pocket
		 */
		private Map<Rarity, List<GameItem>> items = new HashMap<>();

		/**
		 * Creates a new pickpocket level requirement and experience gained
		 * 
		 * @param level the level required to steal from
		 * @param experience the experience gained from stealing
		 */
		Pickpocket(int level, double experience, int petChance, Map<Rarity,List<GameItem>> items) {
			this.level = level;
			this.experience = experience;
			this.petChance = petChance;
			this.items = items;
		}

		GameItem getRandomItem() {
            switch(this){case MAN:return new GameItem(995,3);case WARRIOR:return new GameItem(995,18);case GUARD:return new GameItem(995,30);case KNIGHT:return new GameItem(995,50);case MENAPHITE_THUG:return new GameItem(995,60);default:break;}
			if (this == DRUNKEN_DWARF && Misc.trueRand(5_000) == 0) {
				return new GameItem(Items.AMMO_MOULD);
			}

			for (Entry<Rarity, List<GameItem>> entry : items.entrySet()) {
				final Rarity rarity = entry.getKey();

				if (rarity == Rarity.ALWAYS) {
					continue;
				}
				final List<GameItem> items = entry.getValue();

				if (items.isEmpty()) {
					continue;
				}

				if (RandomUtils.nextInt(1, rarity.rarity) == 1) {
					return Misc.getItemFromList(items).randomizedAmount();
				}
			}

			List<GameItem> always = items.getOrDefault(Rarity.ALWAYS, Lists.newArrayList());

			if (!always.isEmpty()) {
				return Misc.getItemFromList(always).randomizedAmount();
			}

			return null;
		}
	}

	public enum Stall {
		Crafting(new GameItem(1893), 1, 16, 20, 15000),
		Silk(new GameItem(950), 25, 30, 10, 13000),
		Silver(new GameItem(2961), 50, 54, 10, 12000),
		Fur(new GameItem(6814), 65, 80, 10, 11000),
		Magic(new GameItem(1613), 90, 100, 10, 10000),
		Food(new GameItem(712), 25, 30, 10, 13000),
		General(new GameItem(2961), 50, 54, 10, 12000),
		Scimitar(new GameItem(1993), 90, 100, 10, 10000),
		Spice(new GameItem(2007), 50, 54, 10, 12000),
		Gold(new GameItem(4692), 95, 110, 10, 9000),
		LZ_GOLD(new GameItem(19473), 99, 120, 10, 8500);

		/**
		 * The item received from the stall
		 */
		private final GameItem item;

		/**
		 * The experience gained in thieving from a single stall thieve
		 */
		private final double experience;

		/**
		 * The probability that the stall will deplete
		 */
		private final int depletionProbability;

		/**
		 * The level required to steal from the stall
		 */
		private final int level;
		
		/**
		 * The chance of receiving a pet
		 */
		private final int petChance;

		/**
		 * Constructs a new {@link Stall} object with a single parameter, {@link GameItem} which is the item received when interacted with.
		 * 
		 * @param item the item received upon interaction
		 */
		Stall(GameItem item, int level, int experience, int depletionProbability, int petChance) {
			this.item = item;
			this.level = level;
			this.experience = experience;
			this.depletionProbability = depletionProbability;
			this.petChance = petChance;
		}
	}

}
