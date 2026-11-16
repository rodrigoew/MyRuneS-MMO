package io.xeros.content.skills.crafting;

import java.util.HashMap;
import java.util.Map;

import io.xeros.model.entity.player.Player;

public class Enchantment {

	private static final Enchantment SINGLETON = new Enchantment();

	private Enchantment() {
	}

	public enum Enchant {
		SAPPHIRE_RING(1637, 2550, 7, 17.5, 931, 238, 1),
		SAPPHIRE_AMULET(1694, 1727, 7, 17.5, 719, 114, 1),
		SAPPHIRE_NECKLACE(1656, 3853, 7, 17.5, 931, 238, 1),

		EMERALD_RING(1639, 2552, 27, 37, 931, 238, 2),
		EMERALD_AMULET(1696, 1729, 27, 37, 720, 115, 2),
		EMERALD_NECKLACE(1658, 5521, 27, 37, 931, 238, 2),

		RUBY_RING(1641, 2568, 49, 59, 931, 238, 3),
		RUBY_AMULET(1698, 1725, 49, 59, 721, 116, 3),
		RUBY_NECKLACE(1660, 11194, 49, 59, 931, 238, 3),

		DIAMOND_RING(1643, 2570, 57, 67, 931, 238, 4),
		DIAMOND_AMULET(1700, 1731, 57, 67, 719, 153, 4),
		DIAMOND_NECKLACE(1662, 11090, 57, 67, 931, 238, 4),

		DRAGONSTONE_RING(1645, 2572, 68, 78, 931, 238, 5),
		DRAGONSTONE_AMULET(1702, 1712, 68, 78, 720, 154, 5),
		DRAGONSTONE_NECKLACE(1664, 11105, 68, 78, 931, 238, 5),
		DRAGONSTONE_BRACELET(11115, 11126, 68, 78, 931, 238, 5),

		ONYX_RING(6575, 6583, 87, 97, 931, 238, 6),
		ONYX_AMULET(6581, 6585, 87, 97, 721, 452, 6),
		ONYX_NECKLACE(6577, 11128, 87, 97, 931, 238, 6),
		ONYX_BRACELET(11130, 11133, 87, 97, 931, 238, 6),

		ZENYTE_RING(19538, 19550, 93, 110, 931, 238, 7),
		ZENYTE_NECKLACE(19535, 19547, 93, 110, 931, 238, 7),
		ZENYTE_AMULET(19541, 19553, 93, 110, 721, 452, 7),
		ZENYTE_BRACELET(19532, 19544, 93, 110, 931, 238, 7),
        SAPPHIRE_BRACELET(11072, 11074, 7, 17.5, 931, 238, 1),
        EMERALD_BRACELET(11076, 11079, 27, 37, 931, 238, 2),
        RUBY_BRACELET(11085, 11088, 49, 59, 931, 238, 3),
        DIAMOND_BRACELET(11092, 11095, 57, 67, 931, 238, 4),
        OPAL_RING(21081, 21126, 7, 17.5, 931, 238, 1),
        OPAL_NECKLACE(21090, 21143, 7, 17.5, 931, 238, 1),
        OPAL_BRACELET(21117, 21177, 7, 17.5, 931, 238, 1),
        OPAL_AMULET(21108, 21160, 7, 17.5, 719, 114, 1),
        JADE_RING(21084, 21129, 27, 37, 931, 238, 2),
        JADE_NECKLACE(21093, 21146, 27, 37, 931, 238, 2),
        JADE_BRACELET(21120, 21180, 27, 37, 931, 238, 2),
        JADE_AMULET(21111, 21163, 27, 37, 720, 115, 2),
        TOPAZ_RING(21087, 21140, 49, 59, 931, 238, 3),
        TOPAZ_NECKLACE(21096, 21157, 49, 59, 931, 238, 3),
        TOPAZ_BRACELET(21123, 21183, 49, 59, 931, 238, 3),
        TOPAZ_AMULET(21114, 21166, 49, 59, 721, 116, 3);

		int unenchanted, enchanted, levelReq, anim, gfx, reqEnchantmentLevel;
        double xpGiven;

		Enchant(int unenchanted, int enchanted, int levelReq, double xpGiven, int anim, int gfx, int reqEnchantmentLevel) {
			this.unenchanted = unenchanted;
			this.enchanted = enchanted;
			this.levelReq = levelReq;
			this.xpGiven = xpGiven;
			this.anim = anim;
			this.gfx = gfx;
			this.reqEnchantmentLevel = reqEnchantmentLevel;
		}

		public int getUnenchanted() {
			return unenchanted;
		}

		public int getEnchanted() {
			return enchanted;
		}

		public int getLevelReq() {
			return levelReq;
		}

		public double getXp() {
			return xpGiven;
		}

		public int getAnim() {
			return anim;
		}

		public int getGFX() {
			return gfx;
		}

		public int getELevel() {
			return reqEnchantmentLevel;
		}

		private static final Map<Integer, Enchant> enc = new HashMap<Integer, Enchant>();

		public static Enchant forId(int itemID) {
			return enc.get(itemID);
		}

		static {
			for (Enchant en : values()) {
				enc.put(en.getUnenchanted(), en);
			}
		}
	}

    public void enchantItem(Player player,int itemID,int spellID){
        player.getUtilityMagic().enchantJewellery(itemID,player.getItems().getInventoryItemSlot(itemID),spellID);
    }

	public static Enchantment getSingleton() {
		return SINGLETON;
	}

}
