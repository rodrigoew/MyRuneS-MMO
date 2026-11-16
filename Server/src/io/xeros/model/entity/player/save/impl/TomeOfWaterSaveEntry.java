package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;import java.util.List;
public final class TomeOfWaterSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("tome-water-charges-v1");}
 public String encode(Player p,String key){return Integer.toString(p.getTomeOfWater().getTotalCharges());}
 public boolean decode(Player p,String key,String value){try{int n=Integer.parseInt(value);if(n<0||n>20000)return false;p.getTomeOfWater().setTotalCharges(n);return true;}catch(NumberFormatException e){return false;}}
 public void login(Player p){}
}
