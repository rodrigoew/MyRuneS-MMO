package io.xeros.content.skills.woodcutting;
import io.xeros.Server;
import io.xeros.content.skills.Skill;
import io.xeros.content.skills.farming.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;
/** Private farm trees update the owner's patch; never create a global stump. */
public final class FarmTreeChopping {
 public static void start(Player p,Plant plant){
  if(!p.getFarming().canReach(plant.getPatch())||!p.getFarming().contains(plant))return;
  Tree tree;switch(Plants.values()[plant.plant]){
   case OAK_TREE:tree=Tree.OAK;break;case WILLOW_TREE:tree=Tree.WILLOW;break;case MAPLE_TREE:tree=Tree.MAPLE;break;case YEW_TREE:tree=Tree.YEW;break;case MAGIC_TREE:tree=Tree.MAGIC;break;default:return;
  }
  if(p.playerLevel[8]<tree.getLevelRequired()){p.sendMessage("You need a Woodcutting level of "+tree.getLevelRequired()+".");return;}
  Hatchet axe=Hatchet.getBest(p);if(axe==null){p.sendMessage("You need a suitable axe.");return;}
  p.interruptActions();p.getPA().stopSkilling();int x=p.absX,y=p.absY,h=p.heightLevel;
  long token=p.productionActionToken;
  Server.getEventHandler().submit(new Event<Player>("skilling",p,4){
   public void execute(){
    if(token!=p.productionActionToken||!p.getFarming().canReach(plant.getPatch())||p.isDead||p.getLock().cannotInteract(p)||p.isDisconnected()||p.getSession()==null||plant.stumped||plant.dead||!plant.checkedHealth||plant.isDiseased()||p.absX!=x||p.absY!=y||p.heightLevel!=h||Hatchet.getBest(p)==null||p.playerLevel[8]<tree.getLevelRequired()||p.getItems().freeSlots()==0){stop();return;}
    if(p.getFarming().getPlantedPatch(plant.getPatch().bottomLeft.getX(),plant.getPatch().bottomLeft.getY())!=plant){stop();return;}
    p.startAnimation(axe.getAnimation());p.getItems().addItem(tree.getWood(),1);
    p.getPA().addSkillXPMultiplied(tree.getExperience()*LumberjackOutfit.multiplier(id->p.getItems().isWearingItem(id)),Skill.WOODCUTTING.getId(),true);
    if(Misc.random(7)==0){plant.stumped=true;plant.doConfig(p);stop();}
   }
  });
 }
 public static void clearFruit(Player p,Plant plant){
  if(!p.getFarming().canReach(plant.getPatch())||!p.getFarming().contains(plant)||plant.dead||!plant.checkedHealth)return;
  if(Hatchet.getBest(p)==null){p.sendMessage("You need a suitable axe.");return;}
  plant.stumped=true;plant.doConfig(p);p.sendMessage("You chop down the fruit tree. Use a spade to clear the stump.");
 }
}
