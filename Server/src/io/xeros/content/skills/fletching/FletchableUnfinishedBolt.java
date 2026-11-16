package io.xeros.content.skills.fletching;

import io.xeros.model.Items;

public enum FletchableUnfinishedBolt {
    BRONZE(9375,877,9,0.5),
    BLURITE(9376,9139,24,1),
    IRON(9377,9140,39,1.5),
    SILVER(9382,9145,43,2.5),
    STEEL(9378,9141,46,3.5),
    MITHRIL(9379,9142,54,5),
    ADAMANT(9380,9143,61,7),
    RUNE(9381,9144,69,10),
    DRAGON(21930,21905,84,12),
    BROAD(11876,11875,55,3);

	private final int unfinished, bolt, level;
    private final double experience;

	FletchableUnfinishedBolt(int unfinished, int bolt, int level, double experience) {
		this.unfinished = unfinished;
		this.bolt = bolt;
		this.level = level;
		this.experience = experience;
	}

	public int getUnfinished() {
		return unfinished;
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
