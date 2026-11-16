package io.xeros.content.skills.hunter.trap.impl;

import com.google.common.collect.ImmutableSet;
import io.xeros.content.skills.hunter.GroundTrapSpecies2022;
import io.xeros.content.skills.hunter.trap.Trap;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Compatibility facade for Stand Up's Hunter API; all trap variants share the validated lifecycle. */
public final class BoxTrap extends Trap {
    public static final ImmutableSet<Integer> NPC_IDS=ImmutableSet.of(1505,2910,2911,2912);
    public BoxTrap(Player player) { super(player,TrapType.BOX_TRAP); }
    public enum BoxTrapData {
        FERRET(GroundTrapSpecies2022.FERRET,10),
        CHINCHOMPA(GroundTrapSpecies2022.CHINCHOMPA,12),
        CARNIVOROUS_CHINCHOMPA(GroundTrapSpecies2022.CARNIVOROUS_CHINCHOMPA,12),
        BLACK_CHINCHOMPA(GroundTrapSpecies2022.BLACK_CHINCHOMPA,12);
        private final GroundTrapSpecies2022 species;
        private final int respawn;
        BoxTrapData(GroundTrapSpecies2022 species,int respawn) { this.species=species;this.respawn=respawn; }
        public int getNpcId() { return species.npc; }
        public int getRespawn() { return respawn; }
        public static Optional<BoxTrapData> getBoxTrapDataByNpcId(int id) { return Arrays.stream(values()).filter(d->d.species.npc==id).findFirst(); }
        
    }
}
