package io.xeros.content.skills.fletching;

public enum FletchableCrossbow {
    BRONZE_CROSSBOW(9454,9174,4442,9,6), BLURITE_CROSSBOW(9456,9176,4442,24,16),
    IRON_CROSSBOW(9457,9177,4442,39,22), STEEL_CROSSBOW(9459,9179,4442,46,27),
    MITHRIL_CROSSBOW(9461,9181,4442,54,32), ADAMANR_CROSSBOW(9463,9183,4442,61,41),
    RUNE_CROSSBOW(9465,9185,4442,69,50), DRAGON_CROSSBOW(21921,21902,4442,78,70);

	/**
	 * The unstrung bow
	 */
	private final int item;
	/**
	 * The finished product
	 */
	private final int product;
	/**
	 * The level required for making a bow
	 */
	private final int levelRequired;
	/**
	 * The experience gained from making the bow
	 */
	private final double experience;

	private final int animation;

	FletchableCrossbow(int item, int product, int animation, int levelRequired, double experience) {
		this.item = item;
		this.product = product;
		this.animation = animation;
		this.levelRequired = levelRequired;
		this.experience = experience;
	}

	/**
	 * The experience gained from crafting a bow
	 * 
	 * @return The experience gained from crafting a bow
	 */
	public double getExperience() {
		return experience;
	}

	/**
	 * The unstrung bows that need stringing
	 * 
	 * @return the bows that need to be strung
	 */
	public int getItem() {
		return item;
	}

	/**
	 * The level that is required for a bow
	 * 
	 * @return The certain level for a required bow
	 */
	public int getLevelRequired() {
		return levelRequired;
	}

	/**
	 * The finished product or "strung bow"
	 * 
	 * @return The finished product
	 */
	public int getProduct() {
		return product;
	}

	public int getAnimation() {
		return animation;
	}

}
