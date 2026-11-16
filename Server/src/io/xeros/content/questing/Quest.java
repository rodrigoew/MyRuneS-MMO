package io.xeros.content.questing;


import java.util.List;

import com.google.common.collect.Lists;
import io.xeros.model.Items;
import io.xeros.model.SkillLevel;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;

public abstract class Quest {

    protected final Player player;
    private int stage;
    private boolean rewardClaimed;
    protected final boolean claimRewardsOnce(){if(!isQuestCompleted()||rewardClaimed)return false;rewardClaimed=true;return true;}
    public String getDisplayName(){return getName();}
    protected final boolean nearbyNpc(NPC n){return n!=null&&!n.isDead()&&!n.needRespawn&&!n.isUnregister()&&n.getInstance()==player.getInstance()&&n.getHeight()==player.heightLevel&&player.getPosition().withinDistance(n.getPosition(),3)&&!player.isDead&&!player.isDisconnected()&&!player.getLock().cannotInteract(player);}
    protected final boolean advance(int expected){if(stage!=expected||player.isDead||player.isDisconnected())return false;incrementStage();return true;}

    /** Live progress; load restoration remains setStage so completed rewards cannot replay. */
    protected final boolean progressTo(int expected,int next){
        if(getStage()!=expected||next<=expected||next>getCompletionStage()||player.isDead||player.isDisconnected())return false;
        while(getStage()<next)incrementStage();return true;
    }
    public boolean handleItemOnNpc(NPC npc,int item,int slot){return false;}
    public abstract String getName();

    public abstract List<SkillLevel> getStartRequirements();

    public abstract List<String> getJournalText(int stage);

    public abstract int getCompletionStage();

    public abstract List<String> getCompletedRewardsList();

    public abstract void giveQuestCompletionRewards();
    public int getQuestPoints(){return 0;}
    public boolean handleItemOnObject(int item,int object,int x,int y){return false;}

    public Quest(Player player) {
        this.player = player;
    }

    public List<String> getStartRequirementLines() {
        List<String> stringList = Lists.newArrayList();
        List<SkillLevel> startRequirements = getStartRequirements();
        if (startRequirements != null && startRequirements.size() > 0) {
            stringList.add("To start this quest you need:");
            for (SkillLevel requirement : startRequirements) {
                stringList.add(requirement.getLevel() + " " + requirement.getSkill().toString());
            }
        }
        return stringList;
    }

    public boolean isQuestCompleted() {
        return getStage() == getCompletionStage();
    }

    public boolean handleObjectClick(WorldObject object, int option) {
        return false;
    }
    public boolean handleItemClick(int itemId) {
        return false;
    }
    public void handleHelpTabActionButton(int button) { }
    public void exchangeItemForPoints(Player c) { }

    public boolean handleNpcClick(NPC npc, int option) {
        return false;
    }

    public void handleNpcKilled(NPC npc) { }

    public void incrementStage() {
        if(stage>=getCompletionStage())return;
        stage++;
        player.getQuesting().updateQuestList();
        if (stage == getCompletionStage()) {
            player.getQuesting().openQuestCompletedJournal(this);
        }
    }

    public int getStage() {
        return stage;
    }

    /**
     * Only used on login, use {@link Quest#incrementStage()} for quest progress through actions.
     * @param stage the stage to set
     */
    public void setStage(int stage) {
        this.stage = Math.max(0,Math.min(getCompletionStage(),stage));
        rewardClaimed=this.stage==getCompletionStage();
    }
}
