package io.xeros.content.skills.prayer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import io.xeros.Configuration;
import io.xeros.Server;
import io.xeros.content.event.eventcalendar.EventChallenge;
import io.xeros.content.skills.Skill;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.EquipmentSet;
import io.xeros.util.Misc;

/**
 * A class that manages a single player training the {@code Skill.PRAYER} skill.
 * 
 * @author Jason MacKeigan
 * @date Mar 10, 2015, 2015, 2:48:38 AM
 */
public class Prayer {

	public static final int RESTORE_PRAYER_BONES = 1;
	public static final int RESTORE_PRAYER_BIG_BONES = 2;
	public static final int RESTORE_PRAYER_BABY_DRAG_WYRM = 3;
	public static final int RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH = 4;
	public static final int RESTORE_PRAYER_SUPER_DRAGON = 5;

	/**
	 * The current bone being used on the altar
	 */
	private Optional<Bone> altarBone = Optional.empty();

	/**
	 * The time that must pass before two bones can be buried consecutively.
	 */
	private static final int BURY_DELAY = 1_200;

	/**
	 * A set of all bones that cannot be modified at any time to ensure consistency
	 */
	private static final Set<Bone> BONES = Collections.unmodifiableSet(EnumSet.allOf(Bone.class));

	/**
	 * The player that will be training the {@code Skill.PRAYER} skill.
	 */
	private final Player player;

	/**
	 * Tracks the time in milliseconds of the last bury or use of bone on an altar
	 */
	private long lastAction;

	/**
	 * Creates a new class that will manage training the prayer skill for an individual player.
	 * 
	 * @param player the player training the skill
	 */
	public Prayer(Player player) {
		this.player = player;
	}

	/**
	 * Attempts to bury a single bone into the dirt
	 * 
	 * @param bone the bone being burried
	 */
	public void bury(Bone bone) {
        if(player.isDead||player.getHealth().getCurrentHealth()<=0||player.getLock().cannotInteract(player)||bone==Bone.SUPERIOR&&player.getLevelForXP(player.playerXP[5])<70)return;
		player.getPA().stopSkilling();
		if (!player.getItems().playerHasItem(bone.getItemId())) {
			return;
		}
		if (System.currentTimeMillis()-lastAction < BURY_DELAY) {
			return;
		}
		if (bone.getItemId() == 536) {
			player.getEventCalendar().progress(EventChallenge.BURY_X_DRAGON_BONES);
		}
		ItemDef definition = ItemDef.forId(bone.getItemId());
		player.sendMessage("You bury the " + (definition == null ? "bone" : definition.getName()) + ".");
		player.getPA().addSkillXPMultiplied(bone.getExperience() * (Boundary.isIn(player, Boundary.LAVA_DRAGON_ISLE) && bone.getItemId() == 11943 ? 4 : 1), Skill.PRAYER.getId(), true);
		if (!PrayerPreservation.preserve(player)) player.getItems().deleteItem2(bone.getItemId(), 1);
		else player.sendMessage("Your zealot's robes preserve the bones.");
		player.startAnimation(827);
		player.getPA().sendSound(2738);
		lastAction=System.currentTimeMillis();
		onBonesBuriedOrCrushed(bone, false);
	}

    public boolean scatter(int item){
        Ashes ash=Ashes.forItem(item);if(ash==null)return false;
        if(player.isDead||player.getHealth().getCurrentHealth()<=0||player.getLock().cannotInteract(player)||System.currentTimeMillis()-lastAction<BURY_DELAY||!player.getItems().playerHasItem(item))return true;
        player.interruptActions();player.getPA().stopSkilling();player.getItems().deleteItem2(item,1);player.getPA().addSkillXPMultiplied(ash.xp,5,true);player.startAnimation(827);lastAction=System.currentTimeMillis();return true;
    }
    private java.util.function.DoubleSupplier altarRoll=Math::random;
    public void alter(final int amount,int objectX,int objectY){
        if(amount<=0||!altarBone.isPresent()||player.isDead||player.getHealth().getCurrentHealth()<=0||player.getLock().cannotInteract(player)||Math.abs(player.absX-objectX)>3||Math.abs(player.absY-objectY)>3)return;
        Bone bone=altarBone.get();
        if(bone==Bone.SUPERIOR&&player.getLevelForXP(player.playerXP[5])<70)return;
        if(!player.getItems().playerHasItem(bone.getItemId())||System.currentTimeMillis()-lastAction<BURY_DELAY)return;
        player.interruptActions();player.getPA().stopSkilling();player.boneOnAltar=false;
        final long token=player.productionActionToken;final int x=player.absX,y=player.absY,h=player.heightLevel;
        final boolean chaos=Boundary.WILDY_BONE_ALTAR.in(player);
        final double multiplier=chaos?3.5:3;
        boolean saved=sacrifice(bone,objectX,objectY,multiplier,chaos);
        Server.getEventHandler().submit(new Event<Player>("skilling",player,3){
            int remaining=amount-(saved?0:1);
            public void execute(){
                if(remaining<=0||token!=player.productionActionToken||player.isDisconnected()||player.getSession()==null||player.isDead||player.getHealth().getCurrentHealth()<=0||player.getLock().cannotInteract(player)||player.absX!=x||player.absY!=y||player.heightLevel!=h||!player.getItems().playerHasItem(bone.getItemId())){stop();return;}
                if(!sacrifice(bone,objectX,objectY,multiplier,chaos))remaining--;
            }
        });
    }
    private boolean sacrifice(Bone bone,int x,int y,double multiplier,boolean chaos){
        boolean chaosSaved=chaos&&altarRoll.getAsDouble()<.5;
        boolean robeSaved=!chaosSaved&&PrayerPreservation.preserve(player);
        boolean saved=chaosSaved||robeSaved;
        if(!saved)player.getItems().deleteItem2(bone.getItemId(),1);
        player.getPA().stillGfx(624,x,y,player.heightLevel,1);player.startAnimation(3705);
        double xp=bone.getExperience()*multiplier;
        player.getPA().addSkillXPMultiplied(xp,5,true);lastAction=System.currentTimeMillis();
        if(chaosSaved)player.sendMessage("The god of chaos preserves your sacrifice.");
        else if(robeSaved)player.sendMessage("Your zealot's robes preserve the bones.");
        return saved;
    }

	public void onBonesBuriedOrCrushed(Bone bone, boolean crushed) {
		if (crushed && Boundary.CATACOMBS.in(player) || EquipmentSet.isWearing(player, EquipmentSet.DRAGONBONE_NECKLACE)) {
			player.restore(Skill.PRAYER, bone.getPrayerRestore());
		}
	}

	/**
	 * The bone last used on the altar
	 * 
	 * @return the bone on the altar
	 */
	public Optional<Bone> getAltarBone() {
		return altarBone;
	}

	/**
	 * Modifies the last bone used on the altar to the parameter
	 * 
	 * @param altarBone the bone on the altar
	 */
	public void setAltarBone(Optional<Bone> altarBone) {
		this.altarBone = altarBone;
	}

	/**
	 * Determines if the {@code itemId} matches any of the {@link Bone} itemId values.
	 * 
	 * @param itemId the item id we're comparing
	 * @return {@code true} if a bone exists with a matching itemId.
	 */
	public static Optional<Bone> isOperableBone(int itemId) {
		return BONES.stream().filter(bone -> bone.getItemId() == itemId).findFirst();
	}
}
