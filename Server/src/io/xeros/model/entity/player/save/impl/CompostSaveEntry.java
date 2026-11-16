package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;
public final class CompostSaveEntry implements PlayerSaveEntry {
    public List<String> getKeys(Player p){return List.of("compost-bins-v1");}
    public boolean decode(Player p,String key,String value){return p.getFarming().getBins().decode(value);}
    public String encode(Player p,String key){return p.getFarming().getBins().encode();}
    public void login(Player p){p.getFarming().getBins().sendConfigs();}
}
