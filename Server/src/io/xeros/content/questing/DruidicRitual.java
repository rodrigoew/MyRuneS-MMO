package io.xeros.content.questing;

import io.xeros.Server;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.SkillLevel;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.entity.npc.*;
import io.xeros.model.entity.player.*;
import java.util.*;

/** Starter Herblore quest, using the existing Taverley NPCs and dungeon cache. */
public final class DruidicRitual extends Quest {
    private boolean rewarded,gateDisturbed;
    public DruidicRitual(Player p){super(p);}
    public String getName(){return "Druidic Ritual";}
    public List<SkillLevel> getStartRequirements(){return Collections.emptyList();}
    public int getCompletionStage(){return 4;}
    public int getQuestPoints(){return 4;}
    public List<String> getCompletedRewardsList(){return Arrays.asList("4 Quest points","250 Herblore XP","Access to Herblore training");}
    public List<String> getJournalText(int stage){
        switch(stage){
            case 0:return Arrays.asList("Kaqemeex at the stone circle north of Taverley needs help.","This quest unlocks the first Herblore levels.");
            case 1:return Arrays.asList("Sanfew is upstairs in Taverley's herb shop.","Kaqemeex asked me to help with his ritual.");
            case 2:return Arrays.asList("Take raw beef, rat meat, bear meat and chicken", "to the Cauldron of Thunder in Taverley Dungeon.","Use each meat on the cauldron, then visit Sanfew.");
            case 3:return Arrays.asList("Sanfew has the four enchanted meats.","Return to Kaqemeex to learn about Herblore.");
            default:return Arrays.asList("The druids have their ritual ingredients.","I can train Herblore.");
        }
    }
    @Override public void setStage(int stage){if(stage<0||stage>4)return;super.setStage(stage);rewarded=stage==4;}
    public void giveQuestCompletionRewards(){if(isQuestCompleted()&&!rewarded){
        rewarded=true;player.getPA().addSkillXPExact(250,15);
        // Level-up dialogue closes interfaces; restore the quest journal after granting XP.
        player.getPA().removeAllWindows();player.getQuesting().openQuestCompletedJournal(this,false);
    }}
    private boolean near(int x,int y,int h){return player.heightLevel==h&&Math.abs(player.absX-x)<=3&&Math.abs(player.absY-y)<=3&&!player.isDead&&!player.getLock().cannotInteract(player);}
    private boolean near(NPC npc){return npc!=null&&!npc.isDead()&&near(npc.absX,npc.absY,npc.heightLevel);}
    private void say(int npc,String...text){new DialogueBuilder(player).npc(npc,text).send();}
    @Override public boolean handleNpcClick(NPC npc,int option){
        int id=npc.getNpcId();if(id!=5044&&id!=5045)return false;
        if(option!=1||!near(npc))return true;
        if(id==5045){
            if(getStage()==0)new DialogueBuilder(player).npc(id,"Our circle needs a purification ritual.","Will you help Sanfew gather its ingredients?").option(
                new DialogueOption("I will help.",p->{if(getStage()==0&&near(npc)){incrementStage();say(id,"Sanfew is upstairs in the herb shop to the south.");}}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();
            else if(getStage()==3)new DialogueBuilder(player).npc(id,"Sanfew has everything he needs.","I can now show you how herbs become useful potions.").option(
                new DialogueOption("Learn Herblore.",p->{if(getStage()==3&&near(npc)){incrementStage();giveQuestCompletionRewards();}}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();
            else if(isQuestCompleted())say(id,"You already know the basics.","Clean grimy herbs and mix them with the correct ingredients.");
            else say(id,"Please speak with Sanfew in the herb shop.");
        }else{
            if(getStage()==0)say(id,"Speak with Kaqemeex at the stone circle first.");
            else if(getStage()==1)new DialogueBuilder(player).npc(id,"Bring beef, rat meat, bear meat and chicken, all raw.","Dip each in the Cauldron of Thunder in the dungeon.").option(
                new DialogueOption("I will prepare the four meats.",p->{if(getStage()==1&&near(npc)){incrementStage();say(id,"Use each raw meat on the cauldron, then return here.");}}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();
            else if(getStage()==2){
                if(hasMeats())new DialogueBuilder(player).npc(id,"The ingredients are ready for the ritual.").option(
                    new DialogueOption("Hand over the enchanted meats.",p->{if(getStage()==2&&near(npc)&&ProductionBatch.exchange(player,new int[][]{{522,1},{523,1},{524,1},{525,1}},new int[0][])){
                        incrementStage();say(id,"Thank you. Kaqemeex will teach you the skill.");
                    }}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();
                else say(id,"I still need all four enchanted meats.","The cauldron is behind the guarded gate in the dungeon.");
            }else say(id,"Kaqemeex can help you with your Herblore training.");
        }
        return true;
    }
    private boolean hasMeats(){for(int i=522;i<=525;i++)if(!player.getItems().playerHasItem(i,1))return false;return true;}
    private void climb(boolean up,int x,int y,int height){ProductionBatch.start(player,16,1,0,1,up?828:827,2,new int[0][],new int[0][],()->player.getPA().movePlayer(x,y,height));}
    @Override public boolean handleItemOnObject(int item,int object,int x,int y){
        if(object!=2142||x!=2893||y!=9831)return false;
        if(!near(x,y,0))return true;
        if(getStage()!=2){player.sendMessage("Speak with the druids about their ritual first.");return true;}
        int product=item==2132?522:item==2134?523:item==2136?524:item==2138?525:0;
        if(product==0){player.sendMessage("The ritual needs four kinds of raw meat.");return true;}
        ProductionBatch.start(player,15,1,0,1,899,3,new int[][]{{item,1}},new int[][]{{product,1}},()->player.sendMessage("The meat turns blue in the thunderous cauldron."));return true;
    }
    @Override public boolean handleObjectClick(WorldObject o,int option){
        if(option!=1)return false;
        if(o.x==2898&&o.y==3428&&(o.id==16671||o.id==16673)){
            if(near(o.x,o.y,o.height))climb(o.height==0,2898,3427,o.height==0?1:0);return true;
        }
        if(o.x==2884&&((o.y==3397&&o.id==16680)||(o.y==9797&&o.id==17385))){
            if(near(o.x,o.y,0))climb(o.y!=3397,2884,o.y==3397?9797:3397,0);return true;
        }
        if(o.x==2898&&(o.y==9831||o.y==9832)&&(o.id==1568||o.id==1569)){
            if(!near(o.x,o.y,0))return true;
            if(player.absX>=2898&&!gateDisturbed){
                boolean guard=false;for(NPC n:NPCHandler.npcs)if(n!=null&&n.getNpcId()==5043&&n.heightLevel==0&&!n.isDeadOrDying()&&Math.abs(n.absX-2899)<=3&&Math.abs(n.absY-9831)<=3){n.attackEntity(player);guard=true;}
                gateDisturbed=true;if(guard){player.sendMessage("The armour guards attack. You can fight them or try the gate again.");return true;}
            }
            player.getPA().movePlayer(player.absX>=2898?2897:2899,Math.max(9831,Math.min(9832,player.absY)),0);return true;
        }
        return false;
    }
}
