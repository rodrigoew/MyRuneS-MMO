package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;import java.util.List;
public final class MiningToolsSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("mining-tool-charges-v1");}
 public String encode(Player p,String key){return p.getMiningTools().encode();}
 public boolean decode(Player p,String key,String value){return p.getMiningTools().decode(value);}
 public void login(Player p){}
}
