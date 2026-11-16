package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.save.PlayerSaveEntry;
import java.util.List;
public class DivinePotionSaveEntry implements PlayerSaveEntry{
 public List<String> getKeys(Player p){return List.of("divine-potions-v1");}
 public boolean decode(Player p,String key,String value){return p.getDivinePotions().decode(value);}
 public String encode(Player p,String key){return p.getDivinePotions().encode();}
 public void login(Player p){}
}
