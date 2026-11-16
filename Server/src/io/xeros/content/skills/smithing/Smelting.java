package io.xeros.content.skills.smithing;

import java.util.Objects;

import io.xeros.Server;
import io.xeros.content.achievement.AchievementType;
import io.xeros.content.achievement.Achievements;
import io.xeros.content.skills.Skill;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ItemAssistant;
import io.xeros.util.Misc;

public class Smelting {

	public enum Bars {
		BRONZE(436, 438, 2349, 1, 6.2, 2405, true, "bronze"),
		IRON(440, -1, 2351, 15, 12.5, 2406, false, "iron"),
		SILVER(442, -1, 2355, 20, 13.7, 2407, false, "silver"),
		STEEL(440, 453, 2353, 30, 17.5, 2409, true, "steel"),
		GOLD(444, -1, 2357, 40, 22.5, 2410, false, "gold"),
		MITHRIL(447, 453, 2359, 50, 30, 2411, true, "mithril"), 
		ADAMANT(449, 453, 2361, 70, 37.5, 2412, true, "adamant"),
		RUNE(451, 453, 2363, 85, 50, 2413, true, "rune");

		private final int ore1;
		private final int ore2;
		private final int bar;
		private final int req;
		private final double exp;
		private final int frame;
		private final boolean twoOres;
		private final String type;

		int getOre1() {
			return ore1;
		}

		int getOre2() {
			return ore2;
		}

		public int getBar() {
			return bar;
		}

		int getReq() {
			return req;
		}

		double getExp() {
			return exp;
		}

		int getFrame() {
			return frame;
		}

		boolean twoOres() {
			return twoOres;
		}

		String getType() {
			return type;
		}

		Bars(int ore, int ore2, int bar, int req, double xp, int frame, boolean two, String type) {
			this.ore1 = ore;
			this.ore2 = ore2;
			this.bar = bar;
			this.req = req;
			this.exp = xp;
			this.frame = frame;
			this.twoOres = two;
			this.type = type;
		}

		static Bars forType(String type) {
			for (Bars bar : values())
				if (bar.getType().equals(type))
					return bar;
			return null;
		}
	}

	/**
	 * Starts Smelting
	 */
 public static void startSmelting(Player p,String type,String amount,String usage){
  Bars bar=Bars.forType(type);int count=getAmount(amount);
  if(bar==null||count<=0||!p.getUtilityMagic().canInteract()||p.playerLevel[13]<bar.req)return;
  if("INFERNAL".equals(usage)){
   if(p.getItems().playerHasItem(13243)||p.getItems().isWearingItem(13243,Player.playerWeapon))produce(p,bar,true);
   return;
  }
  if(!"FURNACE".equals(usage))return;
  p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
  final long token=p.productionActionToken;int x=p.absX,y=p.absY,h=p.heightLevel;p.playerSkilling[13]=true;p.isSmelting=true;
  Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
   int left=count,wait;
   public void execute(){
    if(token!=p.productionActionToken||!p.playerSkilling[13]||!p.getUtilityMagic().canInteract()||p.absX!=x||p.absY!=y||p.heightLevel!=h||p.playerLevel[13]<bar.req){stop();return;}
    if(wait>0){wait--;return;}
    if(!produce(p,bar,false)){stop();return;}p.startAnimation(899);wait=1;if(--left<=0)stop();
   }
   public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[13]=false;p.isSmelting=false;p.startAnimation(65535);}}
  });
 }
 private static boolean produce(Player p,Bars bar,boolean infernal){
  int[][] inputs=bar.ore2>0?new int[][]{{bar.ore1,1},{bar.ore2,secondaryAmount(bar.bar)}}:new int[][]{{bar.ore1,1}};
  boolean success=bar!=Bars.IRON||p.playerEquipment[Player.playerRing]==2568||Misc.random(100)<50;
  if(!io.xeros.content.skills.ProductionBatch.exchange(p,inputs,success?new int[][]{{bar.bar,1}}:new int[0][]))return false;
  if(success){double xp=infernal?bar.exp/2:GoldSmeltingBonus.experience(p,bar.bar,bar.exp);p.getPA().addSkillXPMultiplied(xp,13,true);Achievements.increase(p,AchievementType.SMITH,1);p.getPA().sendSound(2725);}
  else p.sendMessage("O minério é impuro e não produz uma barra.");
  return true;
 }

	private static int secondaryAmount(int bar) {
        switch (bar) {
            case 2353: return 2;
            case 2359: return 4;
            case 2361: return 6;
            case 2363: return 8;
            default: return 1;
        }
    }

 private static int getAmount(String amount){switch(amount==null?"":amount){case "ONE":return 1;case "FIVE":return 5;case "TEN":return 10;case "ALL":return 28;default:return 0;}}
}
