package io.xeros.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import io.xeros.Server;
import io.xeros.model.cycleevent.impl.PoisonEvent;
import io.xeros.model.cycleevent.impl.PoisonResistanceEvent;
import io.xeros.model.cycleevent.impl.VenomEvent;
import io.xeros.model.cycleevent.impl.VenomResistanceEvent;
import io.xeros.model.entity.player.Player;

public final class Health {

	/**
	 * The entity this health was created for
	 */
	private final Entity entity;

	/**
	 * The amount of health the entity has
	 */
	private int currentHealth;

	/**
	 * The maximum amount of health the entity has access to
	 */
	private int maximumHealth;

	/**
	 * The status of the health
	 */
	private HealthStatus status = HealthStatus.NORMAL;
    private PoisonEvent poisonEvent;
    private VenomEvent venomEvent;
    private PoisonResistanceEvent poisonResistance;
    private VenomResistanceEvent venomResistance;
    private Optional<Entity> statusInflictor=Optional.empty();

	/**
	 * A {@link Collection} of statuses that the entity is not susceptible to
	 */
	private final Collection<HealthStatus> nonsusceptibles = new ArrayList<>();

	/**
	 * A new {@link Health} object to manage health operations
	 * 
	 * @param entity the single entity this is created for
	 */
	public Health(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Determines if the health of the entity is not susceptible to a possible status change
	 * 
	 * @param status the possible new status change
	 * @return {@code true} if the entity is not susceptible to the status
	 */
	public boolean isNotSusceptibleTo(HealthStatus status) {
		return nonsusceptibles.contains(status);
	}

	/**
	 * Proposes that the health of the entity be changed to another
	 * 
	 * @param status the potential new status of the entity
	 */
	public void proposeStatus(HealthStatus status, int damage, Optional<Entity> inflictor) {
		if (!entity.susceptibleTo(status)) {
			return;
		}

		if (isNotSusceptibleTo(status)) {
			return;
		}

		if (this.status == status) {
			return;
		}

		if (this.status.isVenomed() && status.isPoisoned()) {
			return;
		}

		if (Server.getEventHandler().isRunning(entity, "health_status")) {
			Server.getEventHandler().stop(entity, "health_status");
		}

		if (status.isVenomed()) {
			venomEvent=new VenomEvent(entity, damage, inflictor);Server.getEventHandler().submit(venomEvent);
		}

		if (status.isPoisoned()) {
			poisonEvent=new PoisonEvent(entity, damage, inflictor);Server.getEventHandler().submit(poisonEvent);
		}

		this.status = status;
        statusInflictor=inflictor;

		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.sendMessage("You have been effected by " + status.toString() + ".");
			player.getPA().requestUpdates();
			player.getPA().updatePoisonStatus();
		}
	}

	/**
	 * Attempts to resolve the status specified. If the entity is already not
	 * 
	 * @param status the status we're trying to resolve
	 * @param resistanceDuration the duration of the resistance if provided
	 */
    public static boolean statusInterfacePaused(Entity e){return e instanceof Player&&(((Player)e).getOpenInterface()>0||((Player)e).getStatusChatboxInterface()>0);}
public int basicAntivenomTicks(){
        int next=status==HealthStatus.POISON&&poisonEvent!=null?poisonEvent.getTicks():status==HealthStatus.VENOM&&venomEvent!=null?venomEvent.getTicks():0;
        if(next<=0){int remaining=Math.max(resistanceTicks(HealthStatus.POISON),resistanceTicks(HealthStatus.VENOM));next=remaining>0?(remaining-1)%30+1:30;}
        return 30+Math.max(1,Math.min(30,next));
    }
    public int resistanceTicks(HealthStatus kind){
        if(!nonsusceptibles.contains(kind))return 0;
        io.xeros.model.cycleevent.Event<?> event=kind==HealthStatus.POISON?poisonResistance:venomResistance;
        return event==null?0:Math.max(0,event.getTicks());
    }
    private int grantResistance(HealthStatus kind,int duration){
        duration=Math.max(1,Math.max(duration,resistanceTicks(kind)));
        if(!nonsusceptibles.contains(kind))nonsusceptibles.add(kind);
        if(kind==HealthStatus.POISON){Server.getEventHandler().stop(entity,"poison_resistance_event");poisonResistance=new PoisonResistanceEvent(entity,duration);Server.getEventHandler().submit(poisonResistance);}
        else if(kind==HealthStatus.VENOM){Server.getEventHandler().stop(entity,"venom_resistance_event");venomResistance=new VenomResistanceEvent(entity,duration);Server.getEventHandler().submit(venomResistance);}
        return duration;
    }
    public void resolveStatus(HealthStatus kind,int duration){grantResistance(kind,duration);removeAllStatuses();}
    /** Ordinary antipoison first converts venom into poison at its current damage; another dose cures it. */
    public int antipoison(int duration){
        if(status==HealthStatus.VENOM){int damage=venomEvent==null?6:venomEvent.getDamage();Optional<Entity> inflictor=statusInflictor;removeAllStatuses();proposeStatus(HealthStatus.POISON,damage,inflictor);return resistanceTicks(HealthStatus.POISON);}
        resolveStatus(HealthStatus.POISON,duration);return resistanceTicks(HealthStatus.POISON);
    }
    public void refreshResistanceTimers(){
        if(entity instanceof Player){Player p=(Player)entity;for(HealthStatus k:new HealthStatus[]{HealthStatus.POISON,HealthStatus.VENOM}){int ticks=resistanceTicks(k);if(ticks>0)p.getPA().sendGameTimer(k==HealthStatus.POISON?io.xeros.model.entity.player.ClientGameTimer.ANTIPOISON:io.xeros.model.entity.player.ClientGameTimer.ANTIVENOM,java.util.concurrent.TimeUnit.MILLISECONDS,ticks*600);}}
    }
    public String snapshot(){
        int kind=status==HealthStatus.POISON?1:status==HealthStatus.VENOM?2:0;
        int damage=kind==1&&poisonEvent!=null?poisonEvent.getDamage():kind==2&&venomEvent!=null?venomEvent.getDamage():0;
        int ticks=kind==1&&poisonEvent!=null?poisonEvent.getTicks():kind==2&&venomEvent!=null?venomEvent.getTicks():0;
        int hits=kind==1&&poisonEvent!=null?poisonEvent.getHits():0;
        return kind+","+damage+","+ticks+","+hits+","+resistanceTicks(HealthStatus.POISON)+","+resistanceTicks(HealthStatus.VENOM);
    }
    public static int[] parseSnapshot(String raw){
        int[] v=new int[6];try{String[] parts=raw.split(",");if(parts.length!=6)return v;for(int i=0;i<6;i++)v[i]=Integer.parseInt(parts[i].trim());}catch(RuntimeException ex){return new int[6];}
        v[0]=Math.max(0,Math.min(2,v[0]));v[1]=Math.max(0,Math.min(v[0]==2?20:100,v[1]));v[2]=Math.max(1,Math.min(30,v[2]));v[3]=Math.max(0,Math.min(4,v[3]));for(int i=4;i<6;i++)v[i]=Math.max(0,Math.min(1200,v[i]));if(v[1]==0)v[0]=0;if(v[0]==0){v[1]=0;v[2]=0;v[3]=0;}return v;
    }
    public void restoreSnapshot(int[] raw){
        if(raw==null||raw.length!=6)return;int[]v=parseSnapshot(java.util.Arrays.stream(raw).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(",")));
        removeAllStatuses();removeAllImmunities();
        if(v[0]==1){status=HealthStatus.POISON;poisonEvent=new PoisonEvent(entity,v[1],Optional.empty(),v[2],v[3]);Server.getEventHandler().submit(poisonEvent);}
        else if(v[0]==2){status=HealthStatus.VENOM;venomEvent=new VenomEvent(entity,v[1],Optional.empty(),v[2]);Server.getEventHandler().submit(venomEvent);}
        if(v[4]>0)grantResistance(HealthStatus.POISON,v[4]);if(v[5]>0)grantResistance(HealthStatus.VENOM,v[5]);updateStatus();refreshResistanceTimers();
    }

	private void updateStatus() {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.getPA().updatePoisonStatus();
		}
	}

	/**
	 * Removes and resolves all status effects such as venom or poison.
	 */
	public void removeAllStatuses() {
		status = HealthStatus.NORMAL;
        statusInflictor=Optional.empty();poisonEvent=null;venomEvent=null;
		Server.getEventHandler().stop(entity, "health_status");
		updateStatus();
	}

	/**
	 * Removes all immunities the player may have such as being immune to venom or poison.
	 */
	public void removeAllImmunities() {
		Server.getEventHandler().stop(entity, "poison_resistance_event");
		Server.getEventHandler().stop(entity, "venom_resistance_event");
        nonsusceptibles.clear();poisonResistance=null;venomResistance=null;
		updateStatus();
	}

	/**
	 * Allows us to remove the status from the list of non-susceptible.
	 * 
	 * @param status the status to be removed
	 * @return {@code true} if the status is in the
	 */
	public boolean removeNonsusceptible(HealthStatus status) {
		return nonsusceptibles.remove(status);
	}

	/**
	 * Requests that the health of the entity update some attribute of the player. In this case, the {@link Player} would have his or her health value sent to the client to show in
	 * the orb and on the statistic interface.
	 */
	public void requestUpdate() {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (player.initialized) // Fix for writing packets from outside main
				player.getPA().refreshSkill(Player.playerHitpoints);
		}
	}

	/**
	 * Retrieves the current status of the entities health
	 * 
	 * @return the status of the health
	 */
	public HealthStatus getStatus() {
		return status;
	}

	/**
	 * The amount of health the entity has
	 * 
	 * @return the amount of health
	 */
	public int getCurrentHealth() {
		return currentHealth;
	}

	/**
	 * The absolute maximum health of this npc
	 * 
	 * @param maximumHealth the maximum health
	 */
	public void setMaximumHealth(int maximumHealth) {
		this.maximumHealth = maximumHealth;
	}

	/**
	 * The maximum health the entity can have
	 * 
	 * @return the maximum health
	 */
	public int getMaximumHealth() {
		return maximumHealth;
	}

	/**
	 * Attempts to reduce the entities health amount by a specified amount
	 * 
	 * @param amount the amount the health amount is to be reduced by
	 */
	public void reduce(int amount) {
		if (amount <= 0) {
			return;
		}
		if (amount > this.currentHealth) {
			amount = this.currentHealth;
		}
		this.currentHealth -= amount;
		requestUpdate();
	}

	/**
	 * Attempts to increase the entities health by a specified amount
	 * 
	 * @param amount the amount the health will be increased by
	 */
	public void increase(int amount) {
	    increase(amount, maximumHealth);
	}

	/**
	 * Attempts to increase the entities health by a specified amount
	 *
	 * @param amount the amount the health will be increased by
	 */
	public void increase(int amount, int maxHealth) {
        if (amount <= 0 || maxHealth <= currentHealth) return;
        currentHealth = (int) Math.min((long) maxHealth, (long) currentHealth + amount);
        requestUpdate();
    }

	public void tick(int amount) {
        if (currentHealth <= 0 || amount <= 0) return;
		if (currentHealth > maximumHealth) {
			currentHealth--;
			requestUpdate();
		} else {
			increase(amount);
		}
	}

	public boolean isMaximum() {
		return currentHealth == maximumHealth;
	}

	/**
	 * Modifies the current amount of health to the amount specified in the parameter. <b> It is worth noting that the amount can exceed that of the {@code maximum}. </b>
	 * 
	 * @param currentHealth the new amount of health
	 */
	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = Math.max(0, currentHealth);
		requestUpdate();
	}

	/**
	 * Sets the value of {@code #amount} to that of {@code #maximum} in an effort to revitalize the health of the entity.
	 */
	public void reset() {
		currentHealth = maximumHealth;
		requestUpdate();
	}


	public double getPercentage() {
		return (double) currentHealth / (double) maximumHealth;
	}
}
