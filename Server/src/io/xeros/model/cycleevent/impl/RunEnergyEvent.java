package io.xeros.model.cycleevent.impl;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.content.skills.agility.RunEnergy2022;
/** Runs after actual player movement, once per game tick. */
public class RunEnergyEvent extends Event<Player> {
    public RunEnergyEvent(Player p,int ticks){super(p,ticks);}
    @Override public void execute(){
        Player p=attachment;
        if(p==null||p.isDisconnected()||p.getSession()==null){stop();return;}
        if(p.isDead)return;
        boolean running=!p.didTeleport&&p.dir1!=-1&&p.dir2!=-1;
        int change=running?-RunEnergy2022.drainUnits(RunEnergy2022.weightGrams(p),p.staminaDelay>0,false)
            :RunEnergy2022.restoreUnits(p.playerLevel[16],RunEnergy2022.restorationBonus(p));
        p.setRunEnergyUnits(p.getRunEnergyUnits()+change,true);
        if(p.getRunEnergy()==0&&p.isRunningToggled())p.updateRunningToggled(false);
        if(p.staminaDelay>0)p.staminaDelay--;
        p.refreshWeight();
    }
}
