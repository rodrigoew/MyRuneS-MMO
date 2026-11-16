package io.xeros.model.entity.player;

import io.xeros.content.skills.runecrafting.ouriana.OurianaBanker;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.packets.npcoptions.*;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionFour;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionOne;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionThree;
import io.xeros.model.entity.player.packets.objectoptions.ObjectOptionTwo;

public class ActionHandler {

	private final Player c;

	public ActionHandler(Player Client) {
		this.c = Client;
	}

	public void firstClickObject(int objectType, int obX, int obY) {
		ObjectOptionOne.handleOption(c, objectType, obX, obY);
	}

	public void secondClickObject(int objectType, int obX, int obY) {
		ObjectOptionTwo.handleOption(c, objectType, obX, obY);
	}

	public void thirdClickObject(int objectType, int obX, int obY) {
		ObjectOptionThree.handleOption(c, objectType, obX, obY);
	}

	public void fourthClickObject(int objectType, int obX, int obY) {
		ObjectOptionFour.handleOption(c, objectType, obX, obY);
	}

	public void firstClickNpc(NPC npc) {
        if(io.xeros.content.skills.hunter.HerbiRecovery2022.npc(c,npc,1))return;
		NpcOptionOne.handleOption(c, npc.getNpcId());
		NpcOptions.handle(c, npc, 1);
	}

	public void secondClickNpc(NPC npc) {
        if(io.xeros.content.skills.hunter.HerbiRecovery2022.npc(c,npc,2))return;
		NpcOptionTwo.handleOption(c, npc.getNpcId());
		NpcOptions.handle(c, npc, 2);
	}

	public void thirdClickNpc(NPC npc) {
        if(io.xeros.model.entity.npc.pets.BabyChinchompaMorph2022.click(c,npc))return;
		NpcOptionThree.handleOption(c, npc.getNpcId());
		NpcOptions.handle(c, npc, 3);
	}

	public void fourthClickNpc(NPC npc) {
		NpcOptionFour.handleOption(c, npc.getNpcId());
		NpcOptions.handle(c, npc, 4);
	}

}
