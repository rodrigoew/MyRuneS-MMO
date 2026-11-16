package io.xeros.content.skills.crafting;
import io.xeros.model.entity.player.Player;
import io.xeros.content.dailytasks.DailyTasks;
public class GemCutting {
 public static void cut(Player p,int use,int used){
  if(use!=1755&&used!=1755)return;
  for(Gem g:Gem.values())if(g.getUncut()==use||g.getUncut()==used){
   io.xeros.content.skills.ProductionBatch.start(p,12,g.getLevel(),g.getExperience(),28,886,2,
    new int[][]{{1755,0},{g.getUncut(),1}},new int[][]{{g.getCut(),1}},()->{
     if(g.getCut()==1615)DailyTasks.increase(p,DailyTasks.PossibleTasks.DRAGONSTONES);
    });return;
  }
 }
}
