package io.xeros.content.bosses.nex;


import io.xeros.content.instances.InstanceConfiguration;
import io.xeros.content.instances.impl.LegacySoloPlayerInstance;
import io.xeros.model.Npcs;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;

public class Nex extends LegacySoloPlayerInstance {

    public static final int KEY = 26_356;
    public static final Position EXIT = new Position(2904,5206,0);
    public static boolean enterSolo(Player p){
        if(!p.getUtilityMagic().canInteract())return false;
        if(p.getInstance()!=null){p.sendMessage("Use ::solo home antes de entrar em outra instância.");return false;}
        Nex arena=new Nex(p,Boundary.Nex);enter(p,arena);return p.getInstance()==arena;
    }

    public Nex(Player player, Boundary boundary) {
        super(InstanceConfiguration.CLOSE_ON_EMPTY, player, boundary);
    }

    public static void enter(Player player, Nex instance) {
        if(instance==null)return;
        if(instance.isDisposed()||instance.player!=player||!player.getUtilityMagic().canInteract()||player.getInstance()!=null){if(player.getInstance()!=instance)instance.dispose();return;}
        try {
            player.interruptActions();player.getPA().stopSkilling();NexCombat.clear(player);
            instance.add(player);
            NexNPC npc = new NexNPC(Npcs.NEX, new Position(2924, 5202, instance.getHeight()), instance);
            System.out.println("npc pos: " + npc.getPosition());
            player.moveTo(new Position(2918, 5203, instance.getHeight()));
        } catch (Exception e) {
            instance.dispose();player.moveTo(EXIT);throw new IllegalStateException("Não foi possível iniciar Nex",e);
        }
    }

    @Override
    public void remove(Player player) {
        NexCombat.clear(player);
        super.remove(player);
    }

    @Override
    public void onDispose() {
        for(Player p:new java.util.ArrayList<>(getPlayers()))NexCombat.clear(p);
    }
}
