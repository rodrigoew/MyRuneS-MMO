package io.xeros.content.skills.fletching;

public enum FletchableJavelin {
	BRONZE(19570, 825, 3, 1),
	IRON(19572, 826, 17, 2),
	STEEL(19574, 827, 32, 5),
	MITHRIL(19576, 828, 47, 8),
	ADAMANT(19578, 829, 62, 10),
	RUNE(19580, 830, 77, 12.5),
	AMETHYST(21352, 21318, 84, 13.5),
	DRAGON(19582, 19484, 92, 15);

	/**
	 * The id
	 */
	private final int id;
	/**
	 * The reward;
	 */
	private final int reward;
	/**
	 * The level required.
	 */
	private final int levelRequired;
	/**
	 * The experience granted.
	 */
	private final double experience;

	FletchableJavelin(int id, int reward, int levelRequired, double experience) {
		this.id = id;
		this.reward = reward;
		this.levelRequired = levelRequired;
		this.experience = experience;
	}

	public double getExperience() {
		return experience;
	}

	public int getId() {
		return id;
	}

	public int getLevelRequired() {
		return levelRequired;
	}

	public int getReward() {
		return reward;
	}

}
