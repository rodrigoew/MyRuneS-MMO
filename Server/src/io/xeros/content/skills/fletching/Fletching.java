package io.xeros.content.skills.fletching;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import io.xeros.Server;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.content.skills.Skill;
import io.xeros.content.skills.slayer.SlayerUnlock;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import io.xeros.util.Misc;

public class Fletching {

	public static final String BROAD_ARROW_MESSAGE = "You need to unlock the ability to craft broad ammo through Slayer Rewards.";
	
	private static final Set<FletchableGem> GEMS = Collections.unmodifiableSet(EnumSet.allOf(FletchableGem.class));

	private static final Set<FletchableUnfinishedBolt> UNFINISHED_BOLTS = Collections.unmodifiableSet(EnumSet.allOf(FletchableUnfinishedBolt.class));

	private static final Set<FletchableBow> BOWS = Collections.unmodifiableSet(EnumSet.allOf(FletchableBow.class));

	private static final Set<FletchableCrossbow> CROSSBOWS = Collections.unmodifiableSet(EnumSet.allOf(FletchableCrossbow.class));

	public static final Set<FletchableArrow> ARROWS = Collections.unmodifiableSet(EnumSet.allOf(FletchableArrow.class));

	private static final Set<FletchableJavelin> JAVELINS = Collections.unmodifiableSet(EnumSet.allOf(FletchableJavelin.class));

	private static final Set<FletchableDart> DARTS = Collections.unmodifiableSet(EnumSet.allOf(FletchableDart.class));

	private static final Set<FletchableBolt> BOLTS = Collections.unmodifiableSet(EnumSet.allOf(FletchableBolt.class));

	/**
	 * An unmodifiable {@link Set} of {@link FletchableLogGroup} elements
	 */
	private static final Set<FletchableLogGroup> FLETCHABLE_LOG_GROUP = Collections.unmodifiableSet(EnumSet.allOf(FletchableLogGroup.class));
	/**
	 * An array of values that represent the amount of some selection
	 */
	private static final int[] FLETCHABLE_AMOUNTS = { 1, 5, 10, -1 };

	/**
	 * The {@link Player} that will be fletching
	 */
	private final Player player;

	/**
	 * An Optional of type {@link FletchableLogGroup} that will keep track of what group the player has selected for fletching.
	 */
	private Optional<FletchableLogGroup> selectedGroup = Optional.empty();

	/**
	 * The {@link FletchableLog} that the player has selected to fletch
	 */
	private Optional<FletchableLog> selectedFletchable = Optional.empty();

	/**
	 * Creates a new single class to manage fletching operations related to the {@code fletching} skill.
	 * 
	 * @param player the player that will be fletching
	 */
	public Fletching(final Player player) {
		this.player = player;
	}

	public boolean combine(int use, int used) {
		selectedGroup=Optional.empty();selectedFletchable=Optional.empty();
		return FletchingRecipes.itemOnItem(player,use,used);
	}

	public void select(int buttonId) {
		selectedGroup.ifPresent(group -> {
			for (FletchableLog fletchable : group.getFletchables()) {
				int index = Misc.linearSearch(fletchable.getButtonIds(), buttonId);
				if (index != -1) {
					int amount = FLETCHABLE_AMOUNTS[index];
					selectedFletchable = Optional.of(fletchable);
					if (amount == -1) {
						player.getPA().sendEnterAmount(0);
						break;
					}
					fletchLog(fletchable, amount);
					break;
				}
			}
		});
	}

	/**
	 * Attempts to cut a log with a knife in hopes to make some secondary item
	 *
	 */
	public void fletchLog(FletchableLog fletchable, int amount) {
        selectedGroup=Optional.empty();selectedFletchable=Optional.empty();
        for(FletchingRecipes.Recipe r:FletchingRecipes.recipes())if(r.first==fletchable.getItemId()&&r.product==fletchable.getProduct()){
            FletchingRecipes.make(player,r,amount);return;
        }
	}

	public void fletchGem(int use, int used) {
		FletchingRecipes.itemOnItem(player,use,used);
	}

	public void fletchUnfinishedBolt(int boltId) {
        for(FletchableUnfinishedBolt b:UNFINISHED_BOLTS)if(b.getUnfinished()==boltId){
            if(b==FletchableUnfinishedBolt.BROAD&&!player.getSlayer().getUnlocks().contains(SlayerUnlock.BROADER_FLETCHING)){player.sendMessage(BROAD_ARROW_MESSAGE);return;}
            FletchingAmmo.once(player,b.getLevel(),b.getExperience(),314,b.getUnfinished(),b.getBolt(),10);

            return;
        }
	}

	public void fletchHeadlessArrows() {
        FletchingAmmo.once(player,1,1,52,314,53,15);
	}

	public void fletchUnstrung(int bowId) {
        for(FletchableBow b:BOWS)if(b.getItem()==bowId){
            io.xeros.content.skills.ProductionBatch.start(player,9,b.getLevelRequired(),b.getExperience(),28,b.getAnimation(),3,
                new int[][]{{b.getItem(),1},{1777,1}},new int[][]{{b.getProduct(),1}},()->{
                    io.xeros.content.achievement_diary.DiaryProduction.fletch(player,b.getProduct());
                    if(b.getProduct()==853&&io.xeros.model.entity.player.Boundary.isIn(player,io.xeros.model.entity.player.Boundary.SEERS_BOUNDARY))player.getDiaryManager().getKandarinDiary().progress(io.xeros.content.achievement_diary.impl.KandarinDiaryEntry.STRING_MAPLE_SHORT);
                    if(b.getProduct()==857)io.xeros.content.dailytasks.DailyTasks.increase(player,io.xeros.content.dailytasks.DailyTasks.PossibleTasks.YEW_SHORTBOWS);
                },()->player.getUtilityMagic().canInteract());return;
        }
	}

	public void fletchUnstrungCross(int crossbowId) {
        for(FletchableCrossbow b:CROSSBOWS)if(b.getItem()==crossbowId){
            io.xeros.content.skills.ProductionBatch.start(player,9,b.getLevelRequired(),b.getExperience(),28,b.getAnimation(),3,
                new int[][]{{b.getItem(),1},{9438,1}},new int[][]{{b.getProduct(),1}},null,()->player.getUtilityMagic().canInteract());return;
        }
	}

	public void fletchArrow(int arrowId) {
        for(FletchableArrow a:ARROWS)if(a.getId()==arrowId){
            if(a==FletchableArrow.BROAD&&!player.getSlayer().getUnlocks().contains(SlayerUnlock.BROADER_FLETCHING)){player.sendMessage(BROAD_ARROW_MESSAGE);return;}
            FletchingAmmo.start(player,a.getLevelRequired(),a.getExperience(),53,a.getId(),a.getReward(),15,10,()->a!=FletchableArrow.BROAD||player.getSlayer().getUnlocks().contains(SlayerUnlock.BROADER_FLETCHING));return;
        }
	}
	
	public void fletchJavelin(int arrowId) {
        for(FletchableJavelin a:JAVELINS)if(a.getId()==arrowId){
            FletchingAmmo.start(player,a.getLevelRequired(),a.getExperience(),19584,a.getId(),a.getReward(),15,10,()->true);return;
        }
	}

	public void fletchDart(int dartId) {
        for(FletchableDart a:DARTS)if(a.getId()==dartId){
            FletchingAmmo.start(player,a.getLevelRequired(),a.getExperience(),314,a.getId(),a.getReward(),10,10,()->true);return;
        }
	}

	public boolean fletchBolt(int boltId, int tipId) {
        for(FletchableBolt b:BOLTS)if(b.getUnfinished()==boltId&&b.getTip()==tipId){
            if(b==FletchableBolt.BROAD_AMETHYST&&!player.getSlayer().getUnlocks().contains(SlayerUnlock.BROADER_FLETCHING)){player.sendMessage(BROAD_ARROW_MESSAGE);return true;}
            FletchingAmmo.start(player,b.getLevel(),b.getExperience(),boltId,tipId,b.getBolt(),10,10,()->b!=FletchableBolt.BROAD_AMETHYST||player.getSlayer().getUnlocks().contains(SlayerUnlock.BROADER_FLETCHING));return true;
        }
        return false;
	}

	public Optional<FletchableLog> getSelectedFletchable() {
		return selectedFletchable;
	}

}
