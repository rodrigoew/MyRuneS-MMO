package io.xeros.content.achievement_diary;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.xeros.model.entity.player.Player;

public abstract class DifficultyAchievementDiary<T extends Enum<T>> extends AchievementDiary<T> {
	   
    private final Set<EntryDifficulty> claimed = new HashSet<>();
   
    public enum EntryDifficulty {
        EASY,
        MEDIUM,
        HARD,
        ELITE;
    	
    	public static final ImmutableSet<EntryDifficulty> VALUES = Sets.immutableEnumSet(EnumSet.allOf(EntryDifficulty.class));
       
        public static Optional<EntryDifficulty> forString(String claim) {
            return VALUES.stream().filter(diff -> diff.name().equalsIgnoreCase(claim)).findAny();
        }
    }
 
    public DifficultyAchievementDiary(String name, Player player) {
        super(name, player);
    }
   
    public abstract Set<T> getEasy();
    public abstract Set<T> getMedium();
    public abstract Set<T> getHard();
    public abstract Set<T> getElite();
   
    public final boolean hasDone(EntryDifficulty difficulty) {
        if (difficulty == EntryDifficulty.EASY) {
            return hasDone(getEasy());
        }
        if (difficulty == EntryDifficulty.MEDIUM) {
            return hasDone(getMedium());
        }
        if (difficulty == EntryDifficulty.HARD) {
            return hasDone(getHard());
        }
        return hasDone(getElite());
    }
   
    protected final void claimRegionalReward(int first) {
        int[] items=first==11136?new int[]{11136,11138,11140,13103}:new int[]{first,first+1,first+2,first+3};
        int highest=-1;
        for(EntryDifficulty tier:EntryDifficulty.values()){
            if(!hasDone(tier))break;
            if(hasClaimed(tier)){highest=tier.ordinal();continue;}
            if(!deliverReward(items[tier.ordinal()]))return;
            claim(tier);player.getDiaryManager().setDiariesCompleted();
            player.sendMessage("Diario de "+getName()+": recompensa "+tierLabel(tier)+" recebida.");
            io.xeros.model.entity.player.save.PlayerSave.saveGameInstant(player);return;
        }
        if(highest>=0&&player.getItems().getItemCount(items[highest],false)==0){if(deliverReward(items[highest])){player.sendMessage("Sua recompensa foi recuperada.");io.xeros.model.entity.player.save.PlayerSave.saveGameInstant(player);}}
        else player.sendMessage(highest>=0?"Voce ja possui sua recompensa. Confira mochila, equipamento e banco.":"Conclua as tarefas faceis deste diario para receber a primeira recompensa.");
    }
    private boolean deliverReward(int id){
        if(player.isDead||player.isDisconnected()||player.getHealth().getCurrentHealth()<=0||player.getLock().cannotInteract(player))return false;
        if(player.getBankPin().requiresUnlock()){player.sendMessage("Desbloqueie o banco antes de receber sua recompensa.");return false;}
        if(io.xeros.content.skills.ProductionBatch.exchange(player,new int[0][],new int[][]{{id,1}}))return true;
        if(player.getInventory().addToBank(new io.xeros.model.items.ImmutableItem(id,1))){player.sendMessage("Recompensa enviada ao banco por falta de espaco.");return true;}
        player.sendMessage("Libere espaco na mochila ou no banco; a recompensa continua disponivel.");return false;
    }
    public static String tierLabel(EntryDifficulty tier){switch(tier){case EASY:return "facil";case MEDIUM:return "media";case HARD:return "dificil";default:return "elite";}}

    public final boolean claim(EntryDifficulty difficulty) {
        return claimed.add(difficulty);
    }
    public final boolean hasClaimed(EntryDifficulty difficulty) {
        return claimed.contains(difficulty);
    }
   
    public final Set<T> getAll() {
        Set<T> set = new LinkedHashSet<>();
        set.addAll(getEasy());
        set.addAll(getMedium());
        set.addAll(getHard());
        set.addAll(getElite());
        return set;
    }
    
	public Set<EntryDifficulty> getClaimed() {
		return claimed;
	}
   
    public final boolean hasDoneEasy() {
        return hasDone(EntryDifficulty.EASY);
    }
   
    public final boolean hasDoneMedium() {
        return hasDone(EntryDifficulty.MEDIUM);
    }
   
    public final boolean hasDoneHard() {
        return hasDone(EntryDifficulty.HARD);
    }
   
    public final boolean hasDoneElite() {
        return hasDone(EntryDifficulty.ELITE);
    }
   
    public final boolean hasDoneAll() {
        boolean hasDone = hasDoneEasy() && hasDoneMedium() && hasDoneHard() && hasDoneElite();
        return hasDone;
    }
 
}