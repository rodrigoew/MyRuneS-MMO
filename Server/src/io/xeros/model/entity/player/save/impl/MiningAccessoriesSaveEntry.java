package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;import java.util.List;
public final class MiningAccessoriesSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("mining-accessory-charges-v1");}
 public String encode(Player p,String key){return p.getMiningAccessories().encode();}
 public boolean decode(Player p,String key,String value){return p.getMiningAccessories().decode(value);}
 public void login(Player p){}
}
