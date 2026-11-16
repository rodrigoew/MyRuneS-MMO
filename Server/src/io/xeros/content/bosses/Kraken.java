package io.xeros.content.bosses;

import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.npc.NPCSpawning;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Coordinate;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Right;

/**
 * 
 * @author Grant_ | www.rune-server.ee/members/grant_ | 12/5/19
 *
 */
public class Kraken {

	public static final int KRAKEN_ID = 494;
	private static final int MAX_DAMAGE = 30;

	public static void init(Player player) {
        if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null)return;
        boolean owner=player.getRights().getPrimary()==Right.OWNER;
        if(!owner&&(player.getSlayer().getTask().isEmpty()||player.getSlayer().getTaskAmount()<=0
                ||!player.getSlayer().getTask().get().getPrimaryName().toLowerCase(java.util.Locale.ROOT).contains("kraken"))){
            player.sendMessage("Você precisa de uma tarefa ativa de Kraken para entrar.");return;
        }
        if(!owner&&player.getPA().getLevelForXP(player.playerXP[18])<87){
            player.sendMessage("Você precisa de nível 87 de Slayer para enfrentar Kraken.");return;
        }

		KrakenInstance instance = new KrakenInstance(player, Boundary.KRAKEN_BOSS_ROOM);
		player.getPA().movePlayer(new Coordinate(2280, 10022, instance.getHeight()));
		NPC npc = NPCSpawning.spawnNpc(player, KRAKEN_ID, 2279, 10035, instance.getHeight(), -1, MAX_DAMAGE, true, false);
		npc.getBehaviour().setRespawnWhenPlayerOwned(true);
		instance.add(npc);
		instance.add(player);
	}

}
