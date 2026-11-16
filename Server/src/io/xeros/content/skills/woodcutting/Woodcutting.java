package io.xeros.content.skills.woodcutting;

import io.xeros.Server;
import io.xeros.content.skills.Skill;
import io.xeros.model.entity.player.Player;

public class Woodcutting {

	private static final Woodcutting INSTANCE = new Woodcutting();

	public void chop(Player player, int objectId, int x, int y) {
        if(HistoricalWoodcutting.click(player,objectId,x,y))return;
		if (!player.getUtilityMagic().canInteract()) return;
		Tree tree = Tree.forObject(objectId);
		if (tree == null) return;
		player.facePosition(x, y);
		if (player.playerLevel[Player.playerWoodcutting] < tree.getLevelRequired()) {
			player.sendMessage("You do not have the woodcutting level required to cut this tree down.");
			return;
		}
		Hatchet hatchet = Hatchet.getBest(player);
		if (hatchet == null) {
			player.sendMessage("You must have an axe and the level required to cut this tree down.");
			return;
		}
		if (player.getItems().freeSlots() == 0) {
			player.sendMessage("You must have at least one free inventory space to do this.");
			return;
		}
		int stumpId = tree == Tree.REDWOOD && objectId == 29670 ? 29671 : tree.getStumpId();
		if (Server.getGlobalObjects().exists(stumpId, x, y, player.heightLevel)) {
			player.sendMessage("This tree has been cut down to a stump, you must wait for it to grow.");
			return;
		}
		player.getPA().stopSkilling();
		player.sendMessage("You swing your axe at the tree.");
		player.startAnimation(hatchet.getAnimation());
		Server.getEventHandler().submit(new WoodcuttingEvent(player, tree, hatchet, objectId, x, y));
	}

	public static Woodcutting getInstance() {
		return INSTANCE;
	}

}
