package com.client.itembonus;

import java.util.Arrays;

public class ItemBonusDefinition {

	private short id;
	private int[] bonuses;

	public int[] getBonuses() {
		return bonuses;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "ItemBonusDefinition [id=" + id + ", bonuses=" + Arrays.toString(bonuses) + "]";
	}
}