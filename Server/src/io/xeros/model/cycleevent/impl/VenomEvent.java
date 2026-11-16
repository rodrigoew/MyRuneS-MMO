package io.xeros.model.cycleevent.impl;
import java.util.Optional;import io.xeros.content.combat.Hitmark;import io.xeros.model.cycleevent.Event;import io.xeros.model.entity.*;
/** Venom starts at six damage and increases by two every thirty ticks, capped at twenty. */
public class VenomEvent extends Event<Entity>{
 private int damage;private final Optional<Entity>inflictor;
 public VenomEvent(Entity e,int ignoredInitialDamage,Optional<Entity>inflictor){this(e,6,inflictor,30);}
 public VenomEvent(Entity e,int damage,Optional<Entity>inflictor,int remaining){super("health_status",e,30);this.damage=Math.max(6,Math.min(20,damage));this.inflictor=inflictor;this.ticks=Math.max(1,Math.min(30,remaining));}
 public int getDamage(){return damage;}
 @Override public void update(){if(attachment==null||attachment.getHealth().getCurrentHealth()<=0||!attachment.getHealth().getStatus().isVenomed()){stop();return;}if(ticks<=1&&Health.statusInterfacePaused(attachment))ticks=2;}
 @Override public void execute(){if(attachment==null){stop();return;}Health h=attachment.getHealth();if(h.isNotSusceptibleTo(HealthStatus.VENOM)){h.removeAllStatuses();stop();return;}attachment.appendDamage(null,damage,Hitmark.VENOM);inflictor.ifPresent(inf->attachment.addDamageTaken(inf,damage));damage=Math.min(20,damage+2);}
}
