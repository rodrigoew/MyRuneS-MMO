package io.xeros.model.cycleevent.impl;
import java.util.Optional;import io.xeros.content.combat.Hitmark;import io.xeros.model.cycleevent.Event;import io.xeros.model.entity.*;
/** 2022 poison: eighteen seconds between hits, five hits at each damage value. */
public class PoisonEvent extends Event<Entity>{
 private int damage,hits;private final Optional<Entity>inflictor;
 public PoisonEvent(Entity e,int damage,Optional<Entity>inflictor){this(e,damage,inflictor,30,0);}
 public PoisonEvent(Entity e,int damage,Optional<Entity>inflictor,int remaining,int hits){super("health_status",e,30);this.damage=Math.max(1,damage);this.inflictor=inflictor;this.ticks=Math.max(1,Math.min(30,remaining));this.hits=Math.max(0,Math.min(4,hits));}
 public int getDamage(){return damage;}public int getHits(){return hits;}
 @Override public void update(){if(attachment==null||attachment.getHealth().getCurrentHealth()<=0||!attachment.getHealth().getStatus().isPoisoned()){stop();return;}if(ticks<=1&&Health.statusInterfacePaused(attachment))ticks=2;}
 @Override public void execute(){if(attachment==null){stop();return;}Health h=attachment.getHealth();if(h.isNotSusceptibleTo(HealthStatus.POISON)){h.removeAllStatuses();stop();return;}attachment.appendDamage(null,damage,Hitmark.POISON);inflictor.ifPresent(inf->attachment.addDamageTaken(inf,damage));if(++hits>=5){damage--;hits=0;}if(damage<=0){h.removeAllStatuses();stop();}}
}
