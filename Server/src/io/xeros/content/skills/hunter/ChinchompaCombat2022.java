package io.xeros.content.skills.hunter;

import io.xeros.content.combat.Hitmark;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.PlayerHandler;

/** Combat death explodes instead of producing the creature obtained from a Hunter trap. */
public final class ChinchompaCombat2022 {
    private ChinchompaCombat2022() { }
    public static boolean death(NPC npc) {
        GroundTrapSpecies2022 species=GroundTrapSpecies2022.npc(npc.getNpcId());
        if(species==null || !species.chinchompa()) return false;
        if(npc.needRespawn || !npc.isDead() || !npc.applyDead) return true;
        if(npc.getAttributes().getBoolean("HUNTER_CHIN_EXPLODED",false)) return true;
        npc.getAttributes().setBoolean("HUNTER_CHIN_EXPLODED",true);
        PlayerHandler.nonNullStream().filter(p->p.getInstance()==npc.getInstance() && p.heightLevel==npc.heightLevel
                && !p.isDead && !p.isDisconnected() && p.getHealth().getCurrentHealth()>0
                && Math.max(Math.abs(p.absX-npc.absX),Math.abs(p.absY-npc.absY))<=1)
                .forEach(p->p.appendDamage(npc,2,Hitmark.HIT));
        return true;
    }
}
