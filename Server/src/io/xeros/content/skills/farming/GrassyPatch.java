package io.xeros.content.skills.farming;


import io.xeros.content.skills.Skill;
import io.xeros.model.Animation;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.ImmutableItem;
import io.xeros.util.Misc;

public class GrassyPatch {
	public byte stage = 0;
    public int compost;
	long time = System.currentTimeMillis();

	public void setTime() {
		time = System.currentTimeMillis();
	}

	public boolean isRaked() {
		return stage == 3;
	}

	public void process(Player player, int index) {
		if (stage == 0)
			return;
        long now=System.currentTimeMillis();if(time<=0||time>now){time=now;return;}
        int grown=(int)Math.min(stage,(now-time)/300_000L);
        if(grown>0){stage-=grown;time+=grown*300_000L;player.getFarming().doConfig();}
	}

	public void click(Player player, int option, int index) {
		if (option == 1)
			rake(player, index);
	}

    public void rake(final Player p, final int index) {
        if(!p.getFarming().canReach(FarmingPatches.values()[index]))return;
        if(isRaked()){p.sendMessage("This plot is fully raked. Try planting a seed.");return;}
        if(!p.getItems().playerHasItem(5341)){p.sendMessage("You need a rake to clear this patch.");return;}
        io.xeros.content.skills.ProductionBatch.start(p,Skill.FARMING.getId(),1,4,3-stage,2273,3,
                new int[][]{{5341,0}},new int[][]{{6055,1}},()->{
                    stage++;setTime();doConfig(p);
                    if(isRaked())p.sendMessage("The patch is clear. You can compost it and plant seeds.");
                },()->p.getFarming().canReach(FarmingPatches.values()[index]));
    }

	public static void doConfig(Player player) {
		player.getFarming().doConfig();
	}

	public int getStage() {
		return stage;
	}
}
