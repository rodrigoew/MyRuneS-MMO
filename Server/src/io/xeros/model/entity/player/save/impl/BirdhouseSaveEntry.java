package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;
public class BirdhouseSaveEntry implements PlayerSaveEntry {
    public List<String> getKeys(Player player){return List.of("birdhouses-v1");}
    public boolean decode(Player player,String key,String value){return player.getBirdhouses().decode(value);}
    public String encode(Player player,String key){return player.getBirdhouses().encode();}
    public void login(Player player){player.getBirdhouses().refresh(true);}
}
