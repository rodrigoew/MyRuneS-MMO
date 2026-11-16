package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;
public class WineSaveEntry implements PlayerSaveEntry {
    public List<String> getKeys(Player player){return List.of("wine-fermentation-v1");}
    public boolean decode(Player player,String key,String value){return player.getWineFermentation().decode(value);}
    public String encode(Player player,String key){return player.getWineFermentation().encode();}
    public void login(Player player){player.getWineFermentation().process();}
}
