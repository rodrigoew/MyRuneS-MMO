package io.xeros.content.skills.herblore;

import io.xeros.model.definitions.ItemDef;

import java.util.Arrays;

public enum Herb {
	GUAM(199, 249, 3, 2.5),
	MARRENTIL(201, 251, 5, 3.8),
	TARROMIN(203, 253, 11, 5),
	HARRALANDER(205, 255, 20, 6.3),
	RANARR(207, 257, 25, 7.5),
	TOADFLAX(3049, 2998, 30, 8),
	IRIT(209, 259, 40, 8.8),
	AVANTOE(211, 261, 48, 10),
	KWUARM(213, 263, 54, 11.3),
	SNAPDRAGON(3051, 3000, 59, 11.8),
	CADANTINE(215, 265, 65, 12.5),
	LANTADYME(2485, 2481, 67, 13.1),
	DWARF_WEED(217, 267, 70, 13.8),
	TORSTOL(219, 269, 75, 15);

	public static Herb forNotedOrUnNotedGrimyHerb(int itemId) {
		ItemDef def = ItemDef.forId(itemId);
		return forGrimyHerb(def.getUnNotedIdIfNoted());
	}

	public static Herb forGrimyHerb(int itemId) {
		return Arrays.stream(Herb.values()).filter(it -> it.getGrimy() == itemId).findFirst().orElse(null);
	}

	/**
	 * The item identification value of the grimy herb
	 */
	private final int grimy;

	/**
	 * The item identification value of the clean herb
	 */
	private final int clean;

	/**
	 * The level required to clean the herb
	 */
	private final int level;

	/**
	 * The experience gained from cleaning the herb
	 */
	private final double experience;

	/**
	 * An object to represent a set of herbs both clean and grimy, both used in herblore.
	 *
	 * @param grimy the id of the grimy herb
	 * @param clean the id of the clean herb
	 * @param level the level required
	 * @param experience the experience gained from cleaning
	 */
    Herb(int grimy, int clean, int level, double experience) {
		this.grimy = grimy;
		this.clean = clean;
		this.level = level;
		this.experience = experience;
	}

	/**
	 * The grimy herb item id
	 *
	 * @return the id
	 */
	public int getGrimy() {
		return grimy;
	}

	/**
	 * The clean herb item id
	 *
	 * @return the id
	 */
	public int getClean() {
		return clean;
	}

	/**
	 * The level required to clean
	 *
	 * @return level required
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * The experience gained from cleaning
	 *
	 * @return the experience
	 */
	public double getExperience() {
		return experience;
	}
}
