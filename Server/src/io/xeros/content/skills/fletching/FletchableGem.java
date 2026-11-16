package io.xeros.content.skills.fletching;

public enum FletchableGem {
	OPAL(1609, 45, 12, 11, 1.5),
	JADE(1611, 9187, 12, 26, 2),
	TOPAZ(1613, 9188, 12, 48, 3.9),
	SAPHIRE(1607, 9189, 12, 56, 4),
	EMERALD(1605, 9190, 12, 58, 5.5),
	RUBY(1603, 9191, 12, 63, 6.3),
	DIAMOND(1601, 9192, 12, 65, 7),
	DRAGONSTONE(1615, 9193, 12, 71, 8.2),
	ONYX(6573, 9194, 24, 73, 9.4);

	private final int gem, tips, amount, level;
	private final double experience;

	FletchableGem(int gem, int tips, int amount, int level, double experience) {
		this.gem = gem;
		this.tips = tips;
		this.amount = amount;
		this.level = level;
		this.experience = experience;
	}

	public int getGem() {
		return gem;
	}

	public int getTips() {
		return tips;
	}

	public int getAmount() {
		return amount;
	}

	public int getLevel() {
		return level;
	}

	public double getExperience() {
		return experience;
	}

}
