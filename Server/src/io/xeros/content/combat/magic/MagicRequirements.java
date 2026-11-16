package io.xeros.content.combat.magic;

import io.xeros.Configuration;
import io.xeros.content.skills.crafting.BryophytaStaff;
import io.xeros.model.Items;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;

public class MagicRequirements extends MagicConfig {

    static int[][] runeCosts(int[] runes,int[] amounts){
        if(runes==null||amounts==null||runes.length!=amounts.length)return null;
        java.util.List<int[]> costs=new java.util.ArrayList<>();
        for(int i=0;i<runes.length;i++){if(amounts[i]<0)return null;if(amounts[i]==0)continue;if(runes[i]<=0)return null;costs.add(new int[]{runes[i],amounts[i]});}
        return costs.toArray(new int[0][]);
    }
    public static boolean hasRunes(Player c,int[] runes,int[] amount){int[][] cost=runeCosts(runes,amount);if(cost!=null&&RunePayment.canExchange(c,cost,new int[0][]))return true;c.sendMessage("You don't have enough required runes to cast this spell!");return false;}
    public static void deleteRunes(Player c,int[] runes,int[] amount){int[][] cost=runeCosts(runes,amount);if(cost!=null)RunePayment.consume(c,cost);}

	public static boolean hasRequiredLevel(Player c, int i) {
		return c.playerLevel[6] >= i;
	}

	public static boolean wearingTomeOfFire(Player player) {
		return player.playerEquipment[Player.playerShield] == Items.TOME_OF_FIRE;
	}

	public static boolean wearingStaff(Player player,int runeId){
        if(io.xeros.model.Staff.getRunesProvidedBy(player).contains(runeId))return true;
        return runeId==Items.NATURE_RUNE&&BryophytaStaff.isWearingStaffWithCharge(player);
    }

	public static boolean checkMagicReqs(Player c, int spell, boolean deleteRunes) {
		if (spell > -1) {

            if(spell>=CombatSpellData.MAGIC_SPELLS.length)return false;
            int[] data=java.util.Arrays.copyOf(CombatSpellData.MAGIC_SPELLS[spell],16);
            int[][] cost=runeCosts(new int[]{data[8],data[10],data[12],data[14]},new int[]{data[9],data[11],data[13],data[15]});
            boolean pay=Configuration.RUNES_REQUIRED&&!Boundary.isIn(c,Boundary.FOUNTAIN_OF_RUNE_BOUNDARY);
            if(cost==null||(pay&&!RunePayment.canExchange(c,cost,new int[0][]))){c.sendMessage("You don't have the required runes to cast this spell.");return false;}



			int staffRequired = CombatSpellData.getStaffNeeded(c, spell);
			if (staffRequired > 0 && Configuration.RUNES_REQUIRED) { // staff required
				if (c.playerEquipment[Player.playerWeapon] != staffRequired) {
					c.sendMessage("You need a " + ItemAssistant.getItemName(staffRequired).toLowerCase() + " to cast this spell.");
					return false;
				}
			}

			if (Configuration.MAGIC_LEVEL_REQUIRED) { // check magic level
				if (c.playerLevel[6] < CombatSpellData.MAGIC_SPELLS[spell][1]) {
					c.sendMessage("You need to have a magic level of " + CombatSpellData.MAGIC_SPELLS[spell][1] + " to cast this spell.");
					return false;
				}
			}
            if(deleteRunes&&pay){
                boolean saved=data[0]>0&&RuneSavingWeapons.saves(c,java.util.concurrent.ThreadLocalRandom.current()::nextInt);
                if(!saved&&!RunePayment.consume(c,cost))return false;
            }

		}
		return true;
	}
}