package io.xeros.content.skills.hunter.trap.impl;

import com.google.common.collect.ImmutableSet;
import io.xeros.content.skills.hunter.GroundTrapSpecies2022;
import io.xeros.content.skills.hunter.trap.Trap;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Compatibility facade for Stand Up's Hunter API; all trap variants share the validated lifecycle. */
public final class BirdSnare extends Trap {
    public static final ImmutableSet<Integer> NPC_IDS=ImmutableSet.of(5549,5551,5552,5550,5548);
    public BirdSnare(Player player) { super(player,TrapType.BIRD_SNARE); }
    public enum BirdData {
        CRIMSON_SWIFT(GroundTrapSpecies2022.CRIMSON_SWIFT,10),
        GOLDEN_WARBLER(GroundTrapSpecies2022.GOLDEN_WARBLER,12),
        COPPER_LONGTAIL(GroundTrapSpecies2022.COPPER_LONGTAIL,16),
        CERULEAN_TWITCH(GroundTrapSpecies2022.CERULEAN_TWITCH,18),
        TROPICAL_WAGTAIL(GroundTrapSpecies2022.TROPICAL_WAGTAIL,20);
        private final GroundTrapSpecies2022 species;
        private final int respawn;
        BirdData(GroundTrapSpecies2022 species,int respawn) { this.species=species;this.respawn=respawn; }
        public int getNpcId() { return species.npc; }
        public int getRespawn() { return respawn; }
        public static Optional<BirdData> getBirdDataByNpcId(int id) { return Arrays.stream(values()).filter(d->d.species.npc==id).findFirst(); }
        public static Optional<BirdData> getBirdDataByObjectId(int id) { return Arrays.stream(values()).filter(d->d.species.fullObject==id).findFirst(); }
    }
}
