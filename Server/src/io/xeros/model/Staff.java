package io.xeros.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public enum Staff {
    AIR(Lists.newArrayList(Items.STAFF_OF_AIR, Items.AIR_BATTLESTAFF, Items.MYSTIC_AIR_STAFF, Items.MIST_BATTLESTAFF, Items.MYSTIC_MIST_STAFF, Items.DUST_BATTLESTAFF, Items.SMOKE_BATTLESTAFF, Items.MYSTIC_DUST_STAFF,
            Items.MYSTIC_SMOKE_STAFF),
            Lists.newArrayList(Items.AIR_RUNE)),
    WATER(Lists.newArrayList(Items.STAFF_OF_WATER, Items.WATER_BATTLESTAFF, Items.MYSTIC_WATER_STAFF, Items.MIST_BATTLESTAFF, Items.MYSTIC_MIST_STAFF, 12795,12796, Items.MUD_BATTLESTAFF, Items.STEAM_BATTLESTAFF, Items.MYSTIC_MUD_STAFF,
            Items.MYSTIC_STEAM_STAFF, Items.KODAI_WAND),
            Lists.newArrayList(Items.WATER_RUNE)),
    EARTH(Lists.newArrayList(Items.STAFF_OF_EARTH, Items.EARTH_BATTLESTAFF, Items.MYSTIC_EARTH_STAFF,21198,21200, Items.MUD_BATTLESTAFF, Items.LAVA_BATTLESTAFF, Items.DUST_BATTLESTAFF,
            Items.MYSTIC_MUD_STAFF, Items.MYSTIC_LAVA_STAFF, Items.MYSTIC_DUST_STAFF),
            Lists.newArrayList(Items.EARTH_RUNE)),
    FIRE(Lists.newArrayList(Items.STAFF_OF_FIRE, Items.FIRE_BATTLESTAFF, Items.MYSTIC_FIRE_STAFF,12795,12796,21198,21200, Items.LAVA_BATTLESTAFF, Items.STEAM_BATTLESTAFF, Items.SMOKE_BATTLESTAFF,
            Items.MYSTIC_LAVA_STAFF, Items.MYSTIC_STEAM_STAFF, Items.MYSTIC_STEAM_STAFF, Items.MYSTIC_SMOKE_STAFF),
            Lists.newArrayList(Items.FIRE_RUNE)),
    ;

    private final List<Integer> staffs;
    private final List<Integer> runesProvided;

    Staff(List<Integer> staffs, List<Integer> runesProvided) {
        this.staffs = Collections.unmodifiableList(staffs);
        this.runesProvided = Collections.unmodifiableList(runesProvided);
    }

    public static List<Integer> getRunesProvidedBy(io.xeros.model.entity.player.Player player){
        List<Integer> runes=getRunesProvidedBy(player.playerEquipment[3]);
        if(player.playerEquipment[5]==Items.TOME_OF_FIRE&&player.getTomeOfFire().hasCharges()&&!runes.contains(Items.FIRE_RUNE))runes.add(Items.FIRE_RUNE);
        if(player.playerEquipment[5]==25574&&player.getTomeOfWater().hasCharges()&&!runes.contains(Items.WATER_RUNE))runes.add(Items.WATER_RUNE);
        return runes;
    }
    public static List<Integer> getRunesProvidedBy(int weapon) {
        List<Integer> runes = new ArrayList<>();
        Arrays.stream(Staff.values()).forEach(staff -> {
            if (staff.staffs.contains(weapon)) {
                runes.addAll(staff.runesProvided);
            }
        });
        return runes;
    }
}
