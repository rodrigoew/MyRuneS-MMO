package io.xeros.content.minigames.tob;

import io.xeros.content.instances.InstancedArea;
import io.xeros.content.minigames.tob.instance.TobInstance;
import io.xeros.model.entity.Entity;
import io.xeros.model.entity.npc.NPC;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;

public class TobBoss extends NPC {
    private boolean deathCredited;
    protected boolean eligible(Player p){return p!=null&&getInstance()!=null&&!getInstance().isDisposed()&&p.getInstance()==getInstance()&&getInstance().getPlayers().contains(p)&&!p.isDead&&!p.isDisconnected()&&p.getHealth().getCurrentHealth()>0&&p.heightLevel==heightLevel&&!p.getAttributes().getBoolean(TobInstance.TOB_DEAD_ATTR_KEY);}
    @Override public boolean canBeDamaged(Entity e){return !isDead()&&e!=null&&e.isPlayer()&&eligible(e.asPlayer());}
    @Override public boolean canBeAttacked(Entity e){return canBeDamaged(e);}


    public TobBoss(int npcId, Position position, InstancedArea instancedArea) {
        super(npcId, position);
        instancedArea.add(this);
        getBehaviour().setRespawn(false);
        getBehaviour().setAggressive(true);
    }

    public void onDeath() {
        if(deathCredited||!isDead()||getHealth().getCurrentHealth()>0)return;deathCredited=true;
        Entity killer = calculateKiller();
        if (getInstance() != null) {
            getInstance().getPlayers().forEach(plr -> {
                int points = 4;
                if (killer != null && killer.equals(plr)) {
                    points += 2;
                }
                ((TobInstance) plr.getInstance()).getMvpPoints().award(plr, points);
                ((TobInstance) plr.getInstance()).getFoodRewards().award(plr, points);
            });
        }
    }

}
