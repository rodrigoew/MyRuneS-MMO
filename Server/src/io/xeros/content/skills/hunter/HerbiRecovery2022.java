package io.xeros.content.skills.hunter;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.pets.PetInsurance2022;
import io.xeros.model.entity.player.Player;
/** Compatibility entry point; Hunter pet insurance and free reclaims share one service. */
public final class HerbiRecovery2022 {
 private HerbiRecovery2022(){}
 static boolean ready(Player player,NPC npc){return PetInsurance2022.nearProbita(player,npc);}
 public static boolean npc(Player player,NPC npc,int option){return PetInsurance2022.npc(player,npc,option);}
}
