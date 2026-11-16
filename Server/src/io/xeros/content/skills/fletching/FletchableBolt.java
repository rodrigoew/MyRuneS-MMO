package io.xeros.content.skills.fletching;

public enum FletchableBolt {
    OPAL(877,45,879,11,1.6),
    JADE(9139,9187,9335,26,2.4),
    PEARL(9140,46,880,41,3.2),
    TOPAZ(9141,9188,9336,48,3.9),
    BARBED(877,47,881,51,9.5),
    SAPPHIRE(9142,9189,9337,56,4.7),
    EMERALD(9142,9190,9338,58,5.5),
    RUBY(9143,9191,9339,63,6.3),
    DIAMOND(9143,9192,9340,65,7),
    DRAGON(9144,9193,9341,71,8.2),
    ONYX(9144,9194,9342,73,9.4),
    BROAD_AMETHYST(11875,21338,21316,76,10.6),
    OPAL_DRAGON(21905,45,21955,84,1.6),
    JADE_DRAGON(21905,9187,21957,84,2.4),
    PEARL_DRAGON(21905,46,21959,84,3.2),
    TOPAZ_DRAGON(21905,9188,21961,84,4),
    SAPPHITRE_DRAGON(21905,9189,21963,84,4.7),
    EMERALD_DRAGON(21905,9190,21965,84,5.5),
    RUBY_DRAGON(21905,9191,21967,84,6.3),
    DIAMOND_DRAGON(21905,9192,21969,84,7),
    DRAGONSTONE_DRAGON(21905,9193,21971,84,8.2),
    ONYX_DRAGON(21905,9194,21973,84,9.4);
	private final int unfinished, tip, bolt, level;
    private final double experience;

	FletchableBolt(int unfinished, int tip, int bolt, int level, double experience) {
		this.unfinished = unfinished;
		this.tip = tip;
		this.bolt = bolt;
		this.level = level;
		this.experience = experience;
	}

	public int getUnfinished() {
		return unfinished;
	}

	public int getTip() {
		return tip;
	}

	public int getBolt() {
		return bolt;
	}

	public int getLevel() {
		return level;
	}

	public double getExperience() {
		return experience;
	}

}
