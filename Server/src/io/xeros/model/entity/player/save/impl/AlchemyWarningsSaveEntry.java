package io.xeros.model.entity.player.save.impl;

import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;

public final class AlchemyWarningsSaveEntry implements PlayerSaveEntry {
    public List<String> getKeys(Player p){return List.of("alchemy-warnings-v1");}
    public String encode(Player p,String key){return p.getAlchemy().encodeWarnings();}
    public boolean decode(Player p,String key,String value){return p.getAlchemy().decodeWarnings(value);}
    public void login(Player p){}
}
