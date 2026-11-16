package io.xeros.content.combat.magic;

import java.util.concurrent.TimeUnit;

import io.xeros.content.combat.Hitmark;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.HealthStatus;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.ClientGameTimer;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;

public class LunarSpells extends MagicRequirements {

	public static void CastingLunarOnPlayer(final Player c, int castingSpellId) {
        if(c.playerMagicBook!=2||!c.getUtilityMagic().canInteract()||!c.getUtilityMagic().ready())return;
        if(c.playerAttackingIndex<0||c.playerAttackingIndex>=PlayerHandler.players.length)return;
        final Player castOnPlayer=PlayerHandler.players[c.playerAttackingIndex];
        if(castOnPlayer==null||castOnPlayer==c||castOnPlayer.isDisconnected()||castOnPlayer.isDead||c.isDead||c.getLock().cannotInteract(c)||castOnPlayer.heightLevel!=c.heightLevel||Math.max(Math.abs(c.absX-castOnPlayer.absX),Math.abs(c.absY-castOnPlayer.absY))>10)return;
        c.facePosition(castOnPlayer.absX,castOnPlayer.absY);c.stopMovement();c.attacking.reset();
		switch(castingSpellId) {
			case 30130:
				if (!castOnPlayer.acceptAid) {
					c.sendMessage("This player is currently not accepting aid.");
					return;
				}
				if (!MagicRequirements.checkMagicReqs(c, 86, true)) {
					return;
				}
                c.getUtilityMagic().committedCast(3);
				c.startAnimation(6293);
				c.gfx0(1060);
				c.getPA().resetQuestInterface();
				c.getPA().sendFrame126("Stats of: "+ castOnPlayer.getDisplayName(), 8144);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Attack Level: "+castOnPlayer.playerLevel[0]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[0])+"", 8147);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Strength Level: "+castOnPlayer.playerLevel[2]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[2])+"", 8148);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Defence Level: "+castOnPlayer.playerLevel[1]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[1])+"", 8149);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Hitpoints Level: "+castOnPlayer.playerLevel[3]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[3])+"", 8150);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Range Level: "+castOnPlayer.playerLevel[4]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[4])+"", 8151);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Prayer Level: "+castOnPlayer.playerLevel[5]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[5])+"", 8152);
				c.getPA().sendFrame126(""+ castOnPlayer.getDisplayName() +"'s Magic Level: "+castOnPlayer.playerLevel[6]+"/"+castOnPlayer.getLevelForXP(castOnPlayer.playerXP[6])+"", 8153);
				c.getPA().openQuestInterface();
				castOnPlayer.gfx0(736);
			break;
			case 30298:
				if (!castOnPlayer.acceptAid) {
					c.sendMessage("This player is currently not accepting aid.");
					return;
				}
				if (System.currentTimeMillis() - c.lastCast < 30000) {
					c.sendMessage("You can only cast vengeance every 30 seconds.");
					return;
				}
				if (castOnPlayer.vengOn) {
					c.sendMessage("This player already have vengeance activated.");
					return;
				}

				if (!MagicRequirements.checkMagicReqs(c, 89, true)) {
					return;
				}
                c.getUtilityMagic().committedCast(3);
				c.getPA().sendGameTimer(ClientGameTimer.VENGEANCE, TimeUnit.SECONDS, 30);
				castOnPlayer.vengOn = true;
				c.lastCast = System.currentTimeMillis();
				castOnPlayer.gfx100(725);
				c.getPA().refreshSkill(6);
				c.startAnimation(4411);
				c.sendMessage("You cast vengeance.");
			break;
			case 30048:
				if (!castOnPlayer.acceptAid) {
					c.sendMessage("This player is currently not accepting aid.");
					return;
				}
				if (!castOnPlayer.getHealth().getStatus().isPoisoned()) {
					c.sendMessage("This player is currently not poisoned.");
					return;
				}
				if (!MagicRequirements.checkMagicReqs(c, 85, true)) {
					return;
				}
                c.getUtilityMagic().committedCast(3);
				castOnPlayer.getHealth().resolveStatus(HealthStatus.POISON, 100);
				castOnPlayer.sendMessage("You have been cured by " + c.getDisplayName() + ".");
				castOnPlayer.gfx100(738);
				c.startAnimation(4411);
			break;
			
			case 30290:
				if (castOnPlayer.getPosition().inClanWars() || castOnPlayer.getPosition().inClanWarsSafe()) {
					c.sendMessage("@cr10@This spell can not be used at the district");
					return;
				}
				if (!castOnPlayer.acceptAid) {
					c.sendMessage("This player is currently not accepting aid.");
					return;
				}
                int available=(int)(c.getHealth().getCurrentHealth()*0.75);
                int missing=castOnPlayer.getHealth().getMaximumHealth()-castOnPlayer.getHealth().getCurrentHealth();
                int transfer=Math.min(available,missing);
                if(transfer<=0||c.getHealth().getCurrentHealth()-transfer<1){c.sendMessage("Não há vida disponível para transferir a esse alvo.");return;}
                if(!MagicRequirements.checkMagicReqs(c,88,true))return;
                c.getUtilityMagic().committedCast(3);
                c.appendDamage(c,transfer,Hitmark.HIT);
                castOnPlayer.getHealth().increase(transfer);
				c.setUpdateRequired(true);

				c.startAnimation(4411);
				castOnPlayer.gfx100(738);
				c.gfx100(727);
			break;
			case 30282: 
				if (castOnPlayer.getPosition().inClanWars() || castOnPlayer.getPosition().inClanWarsSafe()) {
					c.sendMessage("@cr10@This spell can not be used at the district");
					return;
				}
				if (Boundary.isIn(castOnPlayer, Boundary.DUEL_ARENA)) {
					c.sendMessage("You cannot use this spell in the duel arena.");
					return;
				}
				if (!castOnPlayer.acceptAid) {
					c.sendMessage("This player is currently not accepting aid.");
					return;
				}
				if (castOnPlayer.specAmount >= 10) {
					c.sendMessage("This player already have full special energy.");
					return;
				}
				if (c.specAmount < 5) {
					c.sendMessage("You do not have enough special energy to transfer.");
					return;
				}
				if (!MagicRequirements.checkMagicReqs(c, 87, true)) {
					return;
				}
                c.getUtilityMagic().committedCast(3);
				c.startAnimation(4411);
				castOnPlayer.gfx0(734);
				castOnPlayer.specAmount = Math.min(10,castOnPlayer.specAmount+5);
				c.specAmount -= 5;
				c.getItems().updateSpecialBar();
				castOnPlayer.getItems().updateSpecialBar();
				castOnPlayer.sendMessage("Your special energy has been restored by 50%!");
				c.sendMessage("You transfer 50% of your energy to " + castOnPlayer.getDisplayName() + ".");
			break;
		}
	}

	private static void Dream(final Player c) {
		//ticks = 0;
		CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
			@Override
			public void execute(CycleEventContainer event) {
				if (c.getHealth().getCurrentHealth() == c.getLevelForXP(c.playerXP[3])) {
					c.sendMessage("You already have full hitpoints");
					event.stop();
					return;
				}
				if (c.dreamSpellTimer == 0) {
					c.startAnimation(6295);
					c.sendMessage("The sleeping has an effect on your health");
				} else if (c.dreamSpellTimer == 2) {
					c.startAnimation(6296);
				} else if (c.dreamSpellTimer > 2) {
					c.gfx0(1056);
					c.getHealth().increase(5);
					c.getPA().refreshSkill(3);
					if (c.getHealth().getCurrentHealth() == c.getLevelForXP(c.playerXP[3])) {
						c.sendMessage("You wake up from your dream..");
						c.gfx0(-1);
						c.startAnimation(6297);
						c.dreamSpellTimer = 0;
						event.stop();
					}
				}
				c.dreamSpellTimer++;
			}
			@Override
			public void onStopped() {
			}
		}, 2);
	}
}
