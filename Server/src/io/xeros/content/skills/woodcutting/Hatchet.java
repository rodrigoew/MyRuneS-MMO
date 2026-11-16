package io.xeros.content.skills.woodcutting;

import io.xeros.model.entity.player.Player;

public enum Hatchet {
	BRONZE(1351, 1, 879, 1.0), 
	IRON(1349, 1, 877, 1.0), 
	STEEL(1353, 6, 875, .9), 
	BLACK(1361, 11, 873, .9),
	MITHRIL(1355, 21, 871, .80), 
	ADAMANT(1357, 31, 869, .65), 
	RUNE(1359, 41, 867, .55), 
	DRAGON(6739, 61, 2846, .45), 
	INFERNAL_AXE(13241, 61, 2117, .45), 
	THIRD_AGE(20011, 61, 7264, .45),
	ELDER_MULTI_AXE(23582, 81, 2062, .30),
    INFERNAL_EMPTY(13242,61,2846,.45),
    CRYSTAL(23673,71,8324,.40),
    CRYSTAL_EMPTY(23675,61,2846,.45),
    GILDED(23279,41,8303,.55);

	private final int itemId;
    private final int levelRequired;
    private final int animation;
	private final double chopSpeed;

	/**
	 * Constructs a new {@link Hatchet} used to cut down trees.
	 * 
	 * @param itemId the item identification value of the hatchet
	 * @param levelRequired the level required for use
	 * @param animation the animation displayed during use
	 * @param chopSpeed the effectiveness of the hatchet when determining a log has been cut
	 */
    Hatchet(int itemId, int levelRequired, int animation, double chopSpeed) {
		this.itemId = itemId;
		this.levelRequired = levelRequired;
		this.animation = animation;
		this.chopSpeed = chopSpeed;
	}

	/**
	 * The item id associated with the hatchet.
	 * 
	 * @return the item id
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * The level required to operate the hatchet whether its in your inventory or in your equipment.
	 * 
	 * @return the level required for operation
	 */
	public int getLevelRequired() {
		return levelRequired;
	}

	/**
	 * The animation displayed when the hatchet is being operated
	 * 
	 * @return the hatchet animation
	 */
	public int getAnimation() {
		return animation;
	}

	/**
	 * The speed at which this axe effects log cut time
	 * 
	 * @return the chop speed of the hatchet
	 */
	public double getChopSpeed() {
		return chopSpeed;
	}
    public int tier(Player p){
        switch(this){
            case BRONZE:return 0;case IRON:return 1;case STEEL:return 2;case BLACK:return 3;
            case MITHRIL:return 4;case ADAMANT:return 5;case RUNE:case GILDED:return 6;
            case CRYSTAL:return p.getWoodcuttingTools().crystal()>0?8:7;
            case ELDER_MULTI_AXE:return 9;default:return 7;
        }
    }
    private int priority(Player p){
        return tier(p)*10+(this==INFERNAL_AXE&&p.getWoodcuttingTools().infernal()>0?2:0)
            +(p.getItems().isWearingItem(itemId,Player.playerWeapon)?1:0);
    }

	/**
	 * Determines the best hatchet the player has in their inventory, or equipment.
	 * 
	 * @param player the player we're trying to find the best axe for
	 * @return null if the player doesn't have a hatchet they can operate, otherwise the best hatchet on their person.
	 */
	public static Hatchet getBest(Player player) {
		Hatchet hatchet = null;
		for (Hatchet h : values()) {
			if ((player.getItems().playerHasItem(h.itemId) || player.getItems().isWearingItem(h.itemId,Player.playerWeapon)) && player.playerLevel[Player.playerWoodcutting] >= h.levelRequired) {
				if (hatchet == null) {
					hatchet = h;
					continue;
				}
				if (hatchet.priority(player) < h.priority(player)) {
					hatchet = h;
				}
			}
		}
		return hatchet;
	}
}
