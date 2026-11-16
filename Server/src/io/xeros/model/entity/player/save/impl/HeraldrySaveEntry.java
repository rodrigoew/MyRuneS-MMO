package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;
public final class HeraldrySaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("player-heraldry-v1");}
 public String encode(Player p,String key){return p.getHouse().workshop().heraldry().encode();}
 public boolean decode(Player p,String key,String value){return p.getHouse().workshop().heraldry().decode(value);}
 public void login(Player p){}
}
