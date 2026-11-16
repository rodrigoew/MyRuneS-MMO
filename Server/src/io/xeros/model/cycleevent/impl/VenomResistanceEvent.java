package io.xeros.model.cycleevent.impl;
import io.xeros.model.cycleevent.Event;import io.xeros.model.entity.*;
public class VenomResistanceEvent extends Event<Entity>{
 private boolean paused;
 public VenomResistanceEvent(Entity e,int ticks){super("venom_resistance_event",e,ticks);}
 @Override public void update(){if(attachment==null){stop();return;}if(Health.statusInterfacePaused(attachment)){ticks++;paused=true;}else if(paused){paused=false;attachment.getHealth().refreshResistanceTimers();}}
 @Override public void execute(){stop();if(attachment!=null)attachment.getHealth().removeNonsusceptible(HealthStatus.VENOM);}
}
