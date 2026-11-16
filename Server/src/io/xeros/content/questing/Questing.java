package io.xeros.content.questing;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.xeros.content.questing.LearningTheRopes.LearningTheRopesQuest;
import io.xeros.content.questing.hftd.HftdQuest;
import io.xeros.content.questing.MonkeyMadness.MonkeyMadnessQuest;
import io.xeros.model.Items;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.GameItem;

public class Questing {

    private static final List<Integer> QUEST_LIST_INTERFACE_IDS = java.util.stream.IntStream.range(50615,50635).boxed().collect(java.util.stream.Collectors.toList());

    private final Player player;
    private final List<Quest> questList;
    private final ClassicQuestResources resources;

    public Questing(Player player) {
        this.player = player;
        resources=new ClassicQuestResources(player);
        questList = Collections.unmodifiableList(Lists.newArrayList(
                new LearningTheRopesQuest(player),
                new HftdQuest(player),
                new MonkeyMadnessQuest(player),
                new DruidicRitual(player),
                new EaglesPeak(player),
                new CooksAssistant(player),new SheepShearer(player),new RestlessGhost(player),new RuneMysteries(player)
        ));
    }

    public void updateQuestList() {
        for(int id:QUEST_LIST_INTERFACE_IDS)player.getPA().sendString("",id);
        for (int index = 0; index < questList.size(); index++) {
            Quest quest = questList.get(index);
            player.getPA().sendString(getQuestLineColor(quest) + quest.getDisplayName(), QUEST_LIST_INTERFACE_IDS.get(index));
            player.getPA().sendString(getQuestLineColor(quest)+quest.getDisplayName(),46937+index);
        }
    }

    private String getQuestLineColor(Quest quest) {
        return quest.getStage() == 0 ? "@red@" : quest.getStage() == quest.getCompletionStage() ? "@gre@" : "@yel@";
    }

    private void openQuestJournal(Quest quest) {
        player.getPA().openQuestInterface(quest.getDisplayName(), quest.getJournalText(quest.getStage()));
    }

    protected void openQuestCompletedJournal(Quest quest) {
        openQuestCompletedJournal(quest,true);
    }

    protected void openQuestCompletedJournal(Quest quest,boolean announce) {
        List<String> awarded = quest.getCompletedRewardsList();
        Preconditions.checkState(awarded.size() > 0 && awarded.size() < 7, "Need 1-5 lines for awarded information.");
        player.getPA().sendString("Missao concluida: " + quest.getDisplayName() + "!", 12144);
        for (int index = 0; index < awarded.size(); index++)
            player.getPA().sendString(awarded.get(index), 12150 + index);
        for (int index = awarded.size(); index < 6; index++)
            player.getPA().sendString("", 12150 + index);
        player.getPA().sendString("", 12146);
        player.getPA().sendString("", 12147);
        player.getPA().showInterface(12140);
        if(announce)player.sendMessage("Parabens! Voce concluiu '" + quest.getDisplayName() + "'!");
    }

    public boolean clickLegacyButton(int id){int index=id-46937;if(index<0||index>=20)return false;if(index<questList.size())openQuestJournal(questList.get(index));return true;}
    public boolean clickButton(int buttonId) {
        for (int index = 0; index < QUEST_LIST_INTERFACE_IDS.size(); index++) {
            if (buttonId == QUEST_LIST_INTERFACE_IDS.get(index)) {
                if (questList.size() > index) {
                    openQuestJournal(questList.get(index));
                }

                return true;
            }
        }
        return false;
    }

    public boolean handleObjectClick(WorldObject object, int option) {
        if(resources.object(object,option))return true;
        for (Quest quest : questList) {
            if (quest.handleObjectClick(object, option)) {
                return true;
            }
        }
        return false;
    }


    public boolean handleItemClick(int itemId) {
        for (Quest quest : questList) {
            if (quest.handleItemClick(itemId)) {
                return true;
            }
        }
        return false;
    }
    public boolean handleNpcClick(NPC npc, int option) {
        if(resources.npc(npc,option))return true;
        for (Quest quest : questList) {
            if (quest.handleNpcClick(npc, option)) {
                return true;
            }
        }
        return false;
    }

    public void handleNpcKilled(NPC npc) {
        questList.forEach(quest -> quest.handleNpcKilled(npc));
    }
    public void handleHelpTabActionButton(int button){
        questList.forEach(quest -> quest.handleHelpTabActionButton(button));
    }
    public void exchangeItemForPoints(Player player){
        questList.forEach(quest -> quest.exchangeItemForPoints(player));
    }
    public void updateQuestProgressOnLoad(String key, int value) {
        for (Quest quest : questList) {
            if (quest.getName().equalsIgnoreCase(key)) {
                quest.setStage(value);
                return;
            }
        }
    }

    public List<Quest> getQuestList() {
        return questList;
    }
    public EaglesPeak getEaglesPeak(){for(Quest quest:questList)if(quest instanceof EaglesPeak)return (EaglesPeak)quest;throw new IllegalStateException("Eagles' Peak is missing from the quest list");}
    public int getQuestPoints(){return questList.stream().filter(Quest::isQuestCompleted).mapToInt(Quest::getQuestPoints).sum();}
    public ClassicQuestResources resources(){return resources;}
    public void processClassic(){resources.process();for(Quest q:questList)if(q instanceof RestlessGhost)((RestlessGhost)q).process();}
    public void logoutClassic(){for(Quest q:questList)if(q instanceof RestlessGhost)((RestlessGhost)q).logout();}
    public boolean handleItemOnNpc(NPC n,int item,int slot){if(resources.itemOnNpc(n,item,slot))return true;for(Quest q:questList)if(q.handleItemOnNpc(n,item,slot))return true;return false;}
    public boolean handleItemOnObject(int item,int object,int x,int y){if(resources.use(item,object,x,y))return true;for(Quest quest:questList)if(quest.handleItemOnObject(item,object,x,y))return true;return false;}
}
