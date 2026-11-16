package io.xeros.content.combat.specials.impl;
import io.xeros.content.combat.Damage;import io.xeros.content.combat.specials.Special;import io.xeros.model.entity.Entity;import io.xeros.model.entity.player.Player;
public class OsmumtensFang extends Special {
 public OsmumtensFang(){super(2.5,1.5,1.0,new int[]{26219,27246});}
 @Override public void activate(Player p,Entity t,Damage d){p.startAnimation(1378);p.gfx100(2128);}
 @Override public void hit(Player p,Entity t,Damage d){}
}
