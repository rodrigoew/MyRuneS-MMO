package io.xeros.content.skills.prayer;

import static io.xeros.content.skills.prayer.Prayer.*;

/**
 * A bone is an item that is used for training the prayer skill. A bone can be buried by clicking the item once, or the bone can be used on an alter to gain additional experience.
 * 
 * <p>
 * Each bone has an item id associated as well as a base amount of experience gained when operating that bone.
 * </p>
 * 
 * @author Jason MacKeigan
 * @date Mar 10, 2015, 2015, 3:24:22 AM
 */
public enum Bone {
	REGULAR(526, 4.5, RESTORE_PRAYER_BONES),
	BAT(530, 5.3, RESTORE_PRAYER_BONES),
    BURNT(528,4.5,RESTORE_PRAYER_BONES),
    ZOGRE(4812,22.5,RESTORE_PRAYER_BIG_BONES),
    SHAIKAHAN(3123,25,RESTORE_PRAYER_BIG_BONES),
    FAYRG(4830,84,RESTORE_PRAYER_BIG_BONES),
    OURG(4834,140,RESTORE_PRAYER_BIG_BONES),
	WOLF(2859, 4.5, RESTORE_PRAYER_BONES),
	MONKEY(3183, 5, RESTORE_PRAYER_BONES),

	BIG(532, 15, RESTORE_PRAYER_BIG_BONES),
	JOGRE(3125, 15, RESTORE_PRAYER_BIG_BONES),
	RAURG(4832, 96, RESTORE_PRAYER_BIG_BONES),

	BABY_DRAG(534, 30, RESTORE_PRAYER_BABY_DRAG_WYRM),
	WYRM(22780, 50, RESTORE_PRAYER_BABY_DRAG_WYRM),

	DRAG(536, 72, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),
	DRAKE(22783, 80, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),
	DAG(6729, 125, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),
	WYVERN(6812, 72, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),
	LAVA(11943, 85, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),
	HYDRA(22786, 110, RESTORE_PRAYER_DRAGON_WYVERN_HYDRA_DRAKE_DAGGANOTH),

	SUPERIOR(22124, 150, RESTORE_PRAYER_SUPER_DRAGON),
    SMALL_NINJA(3179,16.0,RESTORE_PRAYER_BIG_BONES),
    MEDIUM_NINJA(3180,18.0,RESTORE_PRAYER_BIG_BONES),
    GORILLA(3181,18.0,RESTORE_PRAYER_BIG_BONES),
    BEARDED_GORILLA(3182,18.0,RESTORE_PRAYER_BIG_BONES),
    SMALL_ZOMBIE(3185,5.0,RESTORE_PRAYER_BONES),
    LARGE_ZOMBIE(3186,5.0,RESTORE_PRAYER_BONES),
    SKELETAL_GORILLA(3187,3.0,RESTORE_PRAYER_BONES),
    BLEACHED(25422,4.5,RESTORE_PRAYER_BONES),
	;
	/**
	 * The item identification value for the bone
	 */
	private final int itemId;

	/**
	 * The experience gained from burying a bone
	 */
	private final double experience;

	private final int prayerRestore;

	/**
	 * Creates a new {@code Bone} object that will be used in training prayer
	 *  @param itemId the item id of the bone
	 * @param experience the experience gained
	 * @param prayerRestore
	 */
    Bone(int itemId, double experience, int prayerRestore) {
		this.itemId = itemId;
		this.experience = experience;
		this.prayerRestore = prayerRestore;
	}

	/**
	 * The item identification value that represents the bone
	 * 
	 * @return the item
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * The base experience gained from operating the bone
	 * 
	 * @return the experience gained in the {@code Skill.PRAYER} skill
	 */
	public double getExperience() {
		return experience;
	}

	public int getPrayerRestore() {
		return prayerRestore;
	}
}