package io.xeros.content.questing;
import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.SkillLevel;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.save.PlayerSave;
import java.util.*;
/** Shared transaction and stale-dialogue guards for the classic quest campaigns. */
abstract class ClassicQuest extends Quest {
 ClassicQuest(Player p){super(p);}
 public List<SkillLevel> getStartRequirements(){return Collections.emptyList();}
 public int getQuestPoints(){return 1;}
 protected boolean ready(){return !player.isDead&&!player.isDisconnected()&&player.getHealth().getCurrentHealth()>0&&player.getInstance()==null&&!player.getLock().cannotInteract(player)&&!player.getBankPin().requiresUnlock();}
 protected boolean near(int x,int y,int h){return ready()&&player.heightLevel==h&&Math.max(Math.abs(player.absX-x),Math.abs(player.absY-y))<=3;}
 protected boolean local(NPC n,int x,int y,int h){return n!=null&&n.heightLevel==h&&Math.abs(n.absX-x)<=5&&Math.abs(n.absY-y)<=5;}
 protected boolean has(int id){return player.getItems().playerHasItem(id,1);}
 protected boolean owns(int id){return player.getItems().getItemCount(id,false)>0;}
 protected boolean exchange(int[][] in,int[][] out){return ready()&&ProductionBatch.exchange(player,in,out);}
 public static DialogueBuilder narration(Player p,int id,String...text){List<String> lines=new ArrayList<>();for(String input:text){String line="";for(String word:input.split(" +")){if(!line.isEmpty()&&line.length()+word.length()+1>52){lines.add(line);line="";}line+=(line.isEmpty()?"":" ")+word;}if(!line.isEmpty())lines.add(line);}if(lines.isEmpty())lines.add("");DialogueBuilder d=new DialogueBuilder(p);for(int i=0;i<lines.size();i+=4)d.npc(id,lines.subList(i,Math.min(lines.size(),i+4)).toArray(new String[0]));return d;}
 protected void say(int id,String...text){narration(player,id,text).send();}
 protected void offer(NPC n,int expected,String text,String action,Runnable commit){narration(player,n.getNpcId(),text).option(new DialogueOption(action,p->{if(ready()&&nearbyNpc(n)&&getStage()==expected)commit.run();}),DialogueBuilder.EXIT_DIALOGUE_OPTION).send();}
 protected void checkpoint(){PlayerSave.saveGame(player);}
 protected void step(int expected,int next){if(progressTo(expected,next))checkpoint();}
 protected void finish(int expected,int skill,int xp){if(progressTo(expected,getCompletionStage())){giveQuestCompletionRewards();checkpoint();}}
 protected void exactReward(int skill,int xp){if(!claimRewardsOnce())return;if(xp>0)player.getPA().addSkillXPExact(xp,skill);player.getPA().removeAllWindows();player.getQuesting().openQuestCompletedJournal(this,false);}
 protected boolean replace(int id){if(owns(id)){player.sendMessage("O item ja esta com voce ou no banco.");return false;}if(!exchange(new int[0][],new int[][]{{id,1}}))return false;checkpoint();return true;}
}
