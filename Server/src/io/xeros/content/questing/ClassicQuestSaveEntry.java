package io.xeros.content.questing;
import java.util.*;import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;
public final class ClassicQuestSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return Collections.singletonList("classic_quest_resources");}
 public boolean decode(Player p,String key,String value){p.getQuesting().resources().restore(value);return true;}
 public String encode(Player p,String key){return p.getQuesting().resources().encode();}
 public void login(Player p){}
}
