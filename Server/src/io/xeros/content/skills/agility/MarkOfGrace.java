package io.xeros.content.skills.agility;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;
/** Solo lap reward: completing the whole course is required before a mark roll. */
public final class MarkOfGrace {
 private MarkOfGrace(){}
 public static void completedLap(Player p,String course){
  if(course==null||!course.startsWith("Rooftop"))return;
  if(System.currentTimeMillis()-p.lastMarkDropped<180_000L||!Misc.hasOneOutOf(4))return;
  p.getItems().addItemUnderAnyCircumstance(11849,1);
  p.lastMarkDropped=System.currentTimeMillis();
  p.sendMessage("Você recebeu uma Mark of grace ao concluir o percurso.");
 }
}
