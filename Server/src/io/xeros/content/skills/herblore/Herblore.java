package io.xeros.content.skills.herblore;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import io.xeros.content.SkillcapePerks;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.achievement_diary.impl.ArdougneDiaryEntry;
import io.xeros.content.achievement_diary.impl.DesertDiaryEntry;
import io.xeros.content.achievement_diary.impl.FremennikDiaryEntry;
import io.xeros.content.achievement_diary.impl.KandarinDiaryEntry;
import io.xeros.content.achievement_diary.impl.KaramjaDiaryEntry;
import io.xeros.content.achievement_diary.impl.VarrockDiaryEntry;
import io.xeros.content.dailytasks.DailyTasks;
import io.xeros.content.skills.Skill;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;
import io.xeros.util.Misc;

public class Herblore {

	/**
	 * A {@link Set} of all {@link Herb} elements from it's respective enumeration.
	 */
	public static final Set<Herb> HERBS = Collections.unmodifiableSet(EnumSet.allOf(Herb.class));

	/**
	 * A {@link Set} of all {@link Potion} elements from it's respective enumeration.
	 */
	private static final Set<PotionData.FinishedPotions> FINISHED = Collections.unmodifiableSet(EnumSet.allOf(PotionData.FinishedPotions.class));

	/**
	 * The player that will be operating this skill
	 */
	private final Player player;

	/**
	 * A class for managing herblore operation
	 * 
	 * @param player the player
	 */
	public Herblore(Player player) {
		this.player = player;
	}

	/**
	 * Cleans a single her
	 * 
	 * @param itemId the herb attempting to be cleaned
	 */
	public void clean(int itemId) {
        if(!player.getUtilityMagic().canInteract())return;
        Herb h=Herb.forGrimyHerb(itemId);if(h==null)return;
        if(player.playerLevel[15]<h.getLevel()){player.sendMessage("You need Herblore level "+h.getLevel()+" to clean this herb.");return;}
        player.interruptActions();player.getPA().stopSkilling();
        if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{h.getGrimy(),1}},new int[][]{{h.getClean(),1}}))return;
        player.getPA().addSkillXPMultiplied(h.getExperience(),15,true);
        if(player.getItems().playerHasItem(h.getGrimy(),1))io.xeros.content.skills.ProductionBatch.start(player,15,h.getLevel(),h.getExperience(),28,65535,2,
            new int[][]{{h.getGrimy(),1}},new int[][]{{h.getClean(),1}},null);
    }

    public boolean mix(int primary) {
        Optional<PotionData.FinishedPotions> potion=FINISHED.stream().filter(p->p.getPrimary().getId()==primary&&containsSecondaries(p)).findFirst();
        if(!potion.isPresent())return false;
        PotionData.FinishedPotions p=potion.get();
        int[][] inputs=new int[p.getIngredients().length+1][];
        inputs[0]=new int[]{p.getPrimary().getId(),p.getPrimary().getAmount()};
        for(int i=0;i<p.getIngredients().length;i++)inputs[i+1]=new int[]{p.getIngredients()[i].getId(),p.getIngredients()[i].getAmount()};
        io.xeros.content.skills.ProductionBatch.start(player,15,p.getLevel(),p.getExperience(),28,363,2,inputs,
            new int[][]{{p.getResult().getId(),p.getResult().getAmount()}},()->{Achievements.increase(player,AchievementType.HERB,1);io.xeros.content.achievement_diary.DiaryProduction.potion(player,p.getResult().getId());});
        return true;
    }
    public boolean makeUnfinishedPotion(Player p,GameItem herb){return unfinished(p,herb.getId(),227);}
    private boolean unfinished(Player p,int herb,int solvent){
        PotionData.UnfinishedPotions recipe=PotionData.UnfinishedPotions.forHerb(herb);
        int product,level;
        if(solvent==22446&&herb==265){product=22443;level=80;}
        else if(solvent==5935&&(herb==2998||herb==259)){product=herb==2998?5942:5951;level=herb==2998?68:79;}
        else if(solvent==227&&recipe!=null){product=recipe.getPotion().getId();level=recipe.getLevelReq();}
        else return false;
        io.xeros.content.skills.ProductionBatch.start(p,15,level,0,28,363,2,new int[][]{{solvent,1},{herb,1}},new int[][]{{product,1}},null);
        return true;
    }
    public boolean itemOnItem(int first,int second){
        if(HerbedTar.mix(player,first,second))return true;
        if(Crushable.crushIngredient(player,first,second))return true;
        if(first==227||first==5935||first==22446){if(unfinished(player,second,first))return true;}
        if(second==227||second==5935||second==22446){if(unfinished(player,first,second))return true;}
        for(PotionData.FinishedPotions p:FINISHED){
            int primary=p.getPrimary().getId();int other=primary==first?second:primary==second?first:-1;
            for(GameItem ingredient:p.getIngredients())if(ingredient.getId()==other){
                if(!mix(primary))player.sendMessage("You do not have all the ingredients required for this potion.");
                return true;
            }
        }
        return false;
    }

	/**
	 * Determines if the player has all of the ingredients required for the potion.
	 * 
	 * @param p the potion we're determining this for
	 * @return {@code true} if we have all of the ingredients, otherwise {@code false}
	 */
	private boolean containsSecondaries(PotionData.FinishedPotions p) {
		int required = p.getIngredients().length;

		for (GameItem ingredient : p.getIngredients()) {
			if (player.getItems().playerHasItem(ingredient.getId(), ingredient.getAmount())) {
				required--;
			}
		}
		return required == 0;
	}

	public void crushItem(int itemid) {

	}
}
