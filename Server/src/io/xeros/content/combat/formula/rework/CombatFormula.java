package io.xeros.content.combat.formula.rework;

import io.xeros.content.combat.Damage;
import io.xeros.content.skills.Skill;
import io.xeros.model.Items;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;

/**
 * @author Arthur Behesnilian 11:59 AM
 */
public interface CombatFormula {
    /** Probability of a strictly greater attack roll. Avoid integer overflow and invalid probabilities. */
    static double rollAccuracy(double attack,double defence){
        if(!Double.isFinite(attack)||!Double.isFinite(defence))return 0;
        double a=Math.max(0,attack),d=Math.max(0,defence);
        double chance=a>d?1.0-0.5*((d+2.0)/(a+1.0)):0.5*(a/(d+1.0));
        return Math.max(0,Math.min(1,chance));
    }


    default double getSalveAmuletMultiplier(Player attacker, NPC defender) {
        if (attacker.getItems().isWearingItem(Items.SALVE_AMULETEI, Player.playerAmulet)) {
            if (defender.isUndead()) return 0.20;
        } else if (attacker.getItems().isWearingItem(Items.SALVE_AMULET_E, Player.playerAmulet)) {
            if (defender.isUndead()) return 0.20;
        } else if (attacker.getItems().isWearingItem(Items.SALVE_AMULET, Player.playerAmulet)) {
            if (defender.isUndead()) return 0.15;
        }
        return 0.0;
    }


    /**
     * Gets the accuracy for the attacker against the defender
     * @param attacker The attacking entity
     * @param defender The defending entity
     * @return The accuracy roll
     */
    default double getAccuracy(Entity attacker, Entity defender) {
        return getAccuracy(attacker, defender, 1.0, 1.0);
    }

    /**
     * Gets the accuracy for the attacker against the defender
     * @param attacker The attacking entity
     * @param defender The defending entity
     * @param specialAttackMultiplier The special attack multiplier
     * @param defenceMultiplier The defence multiplier
     * @return The accuracy roll
     */
    double getAccuracy(Entity attacker, Entity defender, double specialAttackMultiplier, double defenceMultiplier);

    /**
     * Gets the max hit for the attacker against the defender
     * @param attacker The attacking entity
     * @param defender The defending entity
     * @return The max hit roll
     */
    default int getMaxHit(Entity attacker, Entity defender) {
        return getMaxHit(attacker, defender, 1.0, 1.0);
    }

    /**
     * Gets the max hit for the attacker against the defender
     * @param attacker The attacking entity
     * @param defender The defending entity
     * @param specialAttackMultiplier The special attack multiplier
     * @param specialPassiveMultiplier The special passive multiplier
     * @return The max hit roll
     */
    int getMaxHit(Entity attacker, Entity defender, double specialAttackMultiplier, double specialPassiveMultiplier);

}
