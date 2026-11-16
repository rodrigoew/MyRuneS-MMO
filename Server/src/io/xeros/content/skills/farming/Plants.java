package io.xeros.content.skills.farming;

import java.util.HashMap;
import java.util.Map;

import io.xeros.model.Items;

public enum Plants {

	APPLE_TREE(Items.APPLE_SAPLING, Items.COOKING_APPLE, 8, 8 + 13, 8 + 19, 8 + 25, 8 + 26, 27, 960, SeedType.FRUIT_TREE, 22.0, 1199.5, 8.5, 6),
	BANANA_TREE(Items.BANANA_SAPLING, Items.BANANA, 35, 35 + 13, 35 + 19, 35 + 25, 35 + 26, 33, 960, SeedType.FRUIT_TREE, 28.0, 1750.5, 10.5, 6),
	ORANGE_TREE(Items.ORANGE_SAPLING, Items.ORANGE, 72, 85, 91, 97, 98, 39, 960, SeedType.FRUIT_TREE, 35.5, 2470.2, 13.5, 6),

	GUAM(5291, 199, 4, 173, 170, 9, 80, SeedType.HERB, 11, 12.5, 4), //24
	MARENTILL(5292, 201, 11, 173, 170, 14, 80, SeedType.HERB, 13.5, 15, 4), //24
	TARROMIN(5293, 203, 18, 173, 170, 19, 80, SeedType.HERB, 16, 18, 4), //42
	HARRALANDER(5294, 205, 25, 173, 170, 26, 80, SeedType.HERB, 21.5, 24, 4), //89
	RANARR(5295, 207, 32, 173, 170, 32, 80, SeedType.HERB, 26.5, 30.5, 4), //35957
	TOADFLAX(5296, Items.GRIMY_TOADFLAX, 39, 173, 170, 38, 80, SeedType.HERB, 34, 38.5, 4), //5296
	IRIT(5297, 209, 46, 173, 170, 43, 80, SeedType.HERB, 43, 48.5, 4), //89
	AVANTOE(5298, 211, 53, 173, 170, 50, 80, SeedType.HERB, 54.5, 61.5, 4), //805
	KWUARM(5299, 213, 68, 173, 170, 56, 80, SeedType.HERB, 69, 78, 4), //1425
	SNAPDRAGON(5300, Items.GRIMY_SNAPDRAGON, 75, 173, 170, 62, 80, SeedType.HERB, 87.5, 98.5, 4), //43585
	CADANTINE(5301,	215, 82, 173, 170, 67, 80, SeedType.HERB, 106.5, 120, 4), //285
	LANTADYME(5302, Items.GRIMY_LANTADYME, 89, 173, 170, 73, 80, SeedType.HERB, 134.5, 151.5, 4), //967
	DWARF_WEED(5303, 217, 96, 173, 170, 79, 80, SeedType.HERB, 170.5, 192, 4), //268
	TORSTOL(5304, 219, 103, 173, 170, 85, 80, SeedType.HERB, 199.5, 224.5, 4), //19813

	POTATO(5318, 1942, 6, 0, 0, 1, 40, SeedType.ALLOTMENT, 8, 9, 4),
	ONION(5319, 1957, 13, 0, 0, 5, 40, SeedType.ALLOTMENT, 9.5, 10.5, 4),
	CABBAGE(5324, 1967, 20, 0, 0, 7, 40, SeedType.ALLOTMENT, 10, 11.5, 4),
	TOMATO(5322, 1982, 27, 0, 0, 12, 40, SeedType.ALLOTMENT, 12.5, 14, 4),
	SWEETCORN(5320, 5986, 34, 0,0, 20, 60, SeedType.ALLOTMENT, 17, 19, 6),
	STRAWBERRY(5323, 5504, 43, 0, 0, 31, 60, SeedType.ALLOTMENT, 26, 29, 6),
	WATERMELON(5321, 5982, 52, 0, 0, 47, 80, SeedType.ALLOTMENT, 48.5, 54.5, 8),

	MARIGOLD(5096, 6010, 8, 0, 0, 2, 20, SeedType.FLOWER, 8.5, 47, 4),
	ROSEMARY(5097, 6014, 13, 0, 0, 11, 20, SeedType.FLOWER, 12, 66.5, 4),
	NASTURTIUM(5098, 6012, 18, 0, 0, 24, 20, SeedType.FLOWER, 19.5, 111, 4),
	WOAD(5099, 5738, 23, 0, 0, 25, 20, SeedType.FLOWER, 20.5, 115.5, 4),
	LIMPWURT(5100, 225, 28, 0, 0, 26, 20, SeedType.FLOWER, 21.5, 120, 4),//156
    OAK_TREE(5370, 1521, 8, 72, 136, 14, 12, 15, 160, SeedType.TREE, 14, 467.3, 0, 4),
    WILLOW_TREE(5371, 1519, 15, 79, 143, 23, 21, 30, 240, SeedType.TREE, 25, 1456.5, 0, 6),
    MAPLE_TREE(5372, 1517, 24, 88, 152, 34, 32, 45, 320, SeedType.TREE, 45, 3403.4, 0, 8),
    YEW_TREE(5373, 1515, 35, 99, 163, 47, 45, 60, 400, SeedType.TREE, 81, 7069.9, 0, 10),
    MAGIC_TREE(5374, 1513, 48, 112, 176, 62, 60, 75, 480, SeedType.TREE, 145.5, 13768.3, 0, 12),
    CURRY_TREE(5499, 5970, 99, 112, 118, 124, 125, 42, 960, SeedType.FRUIT_TREE, 40, 2906.9, 15, 6),
    PINEAPPLE_TREE(5500, 2114, 136, 149, 155, 161, 162, 51, 960, SeedType.FRUIT_TREE, 57, 4605.7, 21.5, 6),
    PAPAYA_TREE(5501, 5972, 163, 176, 182, 188, 189, 57, 960, SeedType.FRUIT_TREE, 72, 6146.4, 27, 6),
    PALM_TREE(5502, 5974, 200, 213, 219, 225, 226, 68, 960, SeedType.FRUIT_TREE, 110.5, 10150.1, 41.5, 6),
    // Append new crops: existing saves store enum ordinals.
    WHITE_LILY(22887,22932,37,0,0,58,20,SeedType.FLOWER,42,250,4),
    SNAPE_GRASS(22879,231,63,0,0,61,70,SeedType.ALLOTMENT,82,82,7),
	//WHITE_LILY(14589, 14583, 37, 0, 0, 52, 7, SeedType.FLOWER, 300, 20547, 4)
	;

	public final int seed;
	public final int harvest;
	public final int healthy;
	public final int diseased;
	public final int dead;

	/**
	 * The Experience gained when checking a Tree
	 */
	public final double checkHealthExperience;
	/**
	 * The configuration to check the health of a tree
	 */
	public final int checkHealth;

	/**
	 * The configuration to see a tree stump
	 */
	public final int treeStump;

	public final int level;
	public final int minutes;// no longer used but going to keep here
	public final byte stages;
	public final double plantExperience;
	public final double harvestExperience;
	public final SeedType type;

	private static final Map<Integer, Plants> plantsBySeed = new HashMap<Integer, Plants>();
	
	static {
		for (Plants plant : Plants.values()) {
			plantsBySeed.put(plant.seed, plant);
		}
	}
	
	public static Plants getPlantForSeed(int seed) {
		return plantsBySeed.get(seed);
	}
	
	public static boolean isSeed(int id) {
		return plantsBySeed.containsKey(id);
	}

	Plants(int seed, int harvest, int config, int diseased, int dead, int level, int minutes, SeedType type,
			double plantExperience, double harvestExperience, int stages) {
		this.seed = seed;
		this.harvest = harvest;
		healthy = config;
		this.level = level;
		this.diseased = diseased;
		this.dead = dead;
		this.minutes = minutes;
		this.type = type;
		this.checkHealth = 0;
		this.checkHealthExperience = 0;
		this.treeStump = 0;
		this.plantExperience = plantExperience;
		this.harvestExperience = harvestExperience;
		this.stages = ((byte) stages);
	}

	Plants(int seed, int harvest, int config, int diseased, int dead, int treeStump, int checkHealth, int level, int minutes, SeedType type,
		   double plantExperience, double checkHealthExperience, double harvestExperience, int stages) {
		this.seed = seed;
		this.harvest = harvest;
		healthy = config;
		this.level = level;
		this.diseased = diseased;
		this.dead = dead;
		this.treeStump = treeStump;
		this.checkHealth = checkHealth;
		this.checkHealthExperience = checkHealthExperience;
		this.minutes = minutes;
		this.type = type;
		this.plantExperience = plantExperience;
		this.harvestExperience = harvestExperience;
		this.stages = ((byte) stages);
	}

    public int getMinutes() { return Math.max(1, minutes); }
}
