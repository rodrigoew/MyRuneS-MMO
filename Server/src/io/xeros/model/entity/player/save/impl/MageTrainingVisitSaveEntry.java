package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;import java.util.List;
/** Crash recovery is separate from permanent points and never modifies a character while encoding. */
public final class MageTrainingVisitSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("mage-training-visit-v1");}
 public String encode(Player p,String key){return p.getMageTrainingArena().encodeVisit();}
 public boolean decode(Player p,String key,String text){return p.getMageTrainingArena().decodeVisit(text);}
 public void login(Player p){p.getMageTrainingArena().safeLogin();}
}
