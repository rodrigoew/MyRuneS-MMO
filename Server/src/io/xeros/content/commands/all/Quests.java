package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;import io.xeros.model.entity.player.Player;import java.util.Optional;
public class Quests extends Command {
 public void execute(Player p,String name,String input){if(p.isDead||p.getLock().cannotInteract(p))return;p.setSidebarInterface(2,46120);p.infoTabSelected=3;io.xeros.new_quest_tab.AchievementTab.loadInformation(p);p.getQuesting().updateQuestList();p.sendMessage("Abra a aba de missoes e role a lista abaixo dos diarios. Clique em uma missao para ver seu diario.");}
 public Optional<String> getDescription(){return Optional.of("Abre missoes, progresso e diarios.");}
}
