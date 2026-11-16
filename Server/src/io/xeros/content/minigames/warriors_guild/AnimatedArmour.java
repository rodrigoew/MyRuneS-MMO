package io.xeros.content.minigames.warriors_guild;

import io.xeros.Server;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.npc.NPCSpawning;
import io.xeros.model.entity.player.Player;

/**
 * 
 * @author Jason http://www.rune-server.org/members/jason
 * @date Oct 20, 2013
 */
public class AnimatedArmour {

	private static final int SPAWN_X = 2851;
	private static final int SPAWN_Y = 3536;
	
	public static boolean animator_west;

	public enum Armour {

		BRONZE(2450, 1155, 1117, 1075, 5, 10, 2, 20, 20), 
		IRON(2451, 1153, 1115, 1067, 10, 20, 4, 30, 30), 
		STEEL(2452, 1157, 1119, 1069, 15, 40, 6, 50, 50), 
		MITHRIL(2454, 1159, 1121, 1071, 50, 80, 10, 100, 100), 
		ADAMANT(2455, 1161, 1123, 1073, 60, 100, 13, 120, 120), 
		RUNE(2456, 1163, 1127, 1079, 80, 120, 18, 150, 150);

		int npcId, helm, platebody, platelegs, tokens, health, max, attack, defence;

		Armour(int npcId, int helm, int platebody, int platelegs, int tokens, int health, int max, int attack, int defence) {
			this.npcId = npcId;
			this.helm = helm;
			this.platebody = platebody;
			this.platelegs = platelegs;
			this.tokens = tokens;
			this.health = health;
			this.max = max;
			this.attack = attack;
			this.defence = defence;
		}

		public int getNpcId() {
			return npcId;
		}

		public int getHelmId() {
			return helm;
		}

		public int getPlatebodyId() {
			return platebody;
		}

		public int getPlatelegsId() {
			return platelegs;
		}

		public int getAmountOfTokens() {
			return tokens;
		}

		public int getHealth() {
			return health;
		}

		public int getMax() {
			return max;
		}

		public int getAttack() {
			return attack;
		}

		public int getDefence() {
			return defence;
		}
	}

	private static Armour getArmourForItemId(int itemId) {
		for (Armour a : Armour.values())
			if (a.getHelmId() == itemId || a.getPlatebodyId() == itemId || a.getPlatelegsId() == itemId)
				return a;
		return null;
	}

	private static Armour getArmourForNpcId(int npcId) {
		for (Armour a : Armour.values())
			if (a.getNpcId() == npcId)
				return a;
		return null;
	}

	public static boolean isAnimatedArmourNpc(int npcId) {
		for (Armour armour : Armour.values()) {
			if (armour.npcId == npcId) {
				return true;
			}
		}
		return false;
	}

	public static void itemOnAnimator(final Player player, int itemId) {
		if(!player.getUtilityMagic().canInteract()||player.getInstance()!=null||player.heightLevel!=0)return;
        int x = player.absX, y = player.absY;
		
		if (y != 3537 || x!=2851&&x!=2857) {
			player.sendMessage("Aproxime-se do Armour animator.");
			return;
		}
        final boolean west=x==2851;
		if (player.isAnimatedArmourSpawned) {
			player.sendMessage("Você já possui uma Animated Armour ativa.");
			return;
		}
		final Armour armour = getArmourForItemId(itemId);
		if (armour == null) {
			player.sendMessage("Esse item não pode ser usado no Armour animator.");
			return;
		}
		if (!player.getItems().playerHasItem(armour.getPlatebodyId(), 1) || !player.getItems().playerHasItem(armour.getPlatelegsId(), 1)
				|| !player.getItems().playerHasItem(armour.getHelmId(), 1)) {
			player.sendMessage("Você precisa de full helm, platebody e platelegs do mesmo metal.");
			return;
		}
		if(player.animatedArmourEscrow!=-1){player.sendMessage("Recupere sua armadura anterior antes de usar o animator.");return;}
        if(!io.xeros.content.skills.ProductionBatch.exchange(player,new int[][]{{armour.getPlatebodyId(),1},{armour.getPlatelegsId(),1},{armour.getHelmId(),1}},new int[0][]))return;
        player.isAnimatedArmourSpawned=true;player.animatedArmourEscrow=armour.getNpcId();
		player.getPA().walkTo(0, +3);
		CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {

			@Override
			public void execute(CycleEventContainer event) {
				if(player.animatedArmourEscrow!=armour.getNpcId()||!player.isAnimatedArmourSpawned){event.stop();return;}
                if(player.isDisconnected()||player.getSession()==null){event.stop();return;}
                if(player.isDead||player.heightLevel!=0||player.absX<2830||player.absX>2880||player.absY<3520||player.absY>3560){recover(player);event.stop();return;}
                io.xeros.model.entity.npc.NPC npc=NPCSpawning.spawnNpcOld(player,armour.getNpcId(),west?SPAWN_X:2857,SPAWN_Y,0,1,armour.getHealth(),armour.getMax(),armour.getAttack(),armour.getDefence(),true,true);
                if(npc==null)recover(player);
				player.sendMessage("Uma Animated Armour foi invocada...", 255);
				event.stop();
			}

			@Override
			public void onStopped() {

			}
		}, 6);
	}

    public static void recover(Player p){
        Armour a=getArmourForNpcId(p.animatedArmourEscrow);if(a==null){p.animatedArmourEscrow=-1;p.isAnimatedArmourSpawned=false;return;}
        for(io.xeros.model.entity.npc.NPC n:io.xeros.model.entity.npc.NPCHandler.npcs)if(n!=null&&n.spawnedBy==p.getIndex()&&isAnimatedArmourNpc(n.getNpcId()))n.unregister();
        p.animatedArmourEscrow=-1;p.isAnimatedArmourSpawned=false;
        io.xeros.content.minigames.SoloActivityRewards.give(p,java.util.List.of(new io.xeros.model.items.GameItem(a.helm),new io.xeros.model.items.GameItem(a.platebody),new io.xeros.model.items.GameItem(a.platelegs)),new io.xeros.model.entity.player.Position(2855,3537,0));
        p.sendMessage("Sua armadura do Armour animator foi recuperada.");
    }

	public static void dropTokens(Player player, int npcType, int x, int y) {
		Armour npc = getArmourForNpcId(npcType);
		if (npc != null && player.animatedArmourEscrow==npcType) {
                player.animatedArmourEscrow=-1;
				Server.itemHandler.createGroundItem(player, 8851, x, y, player.heightLevel, npc.getAmountOfTokens(), player.getIndex());
				player.isAnimatedArmourSpawned = false;
		}
	}

}
