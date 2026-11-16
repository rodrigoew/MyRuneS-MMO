package io.xeros.content.skills.hunter;

import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import java.util.Random;

/** The 2022 ring shares ten successful activations across a character's rings. */
public final class RingOfPursuit {
    public static final int ID=21126;
    private static final Random RANDOM=new Random();
    private final Player player;
    private int charges=10;
    public RingOfPursuit(Player player){this.player=player;}
    public int charges(){return charges;}
    public void restore(String value){
        try{int loaded=Integer.parseInt(value);charges=loaded>=1&&loaded<=10?loaded:10;}
        catch(NumberFormatException malformed){charges=10;}
    }
    private boolean available(){return player.getUtilityMagic().canInteract()&&!player.getMovementState().isLocked();}
    public boolean revealNewTrail(){
        if(!available()||player.playerEquipment[Player.playerRing]!=ID||player.playerEquipmentN[Player.playerRing]!=1)return false;
        int index=player.getIndex();
        if(index<0||index>=PlayerHandler.players.length||PlayerHandler.players[index]!=player)return false;
        if(RANDOM.nextInt(4)!=0)return false;
        if(--charges==0){
            player.getItems().deleteEquipment(Player.playerRing);charges=10;
            player.sendMessage("Your ring of pursuit reveals the full trail and crumbles to dust.");
        }else player.sendMessage("Your ring of pursuit reveals the full trail. It has "+charges+" charges remaining.");
        return true;
    }
    private void check(){player.sendMessage("You have "+charges+" charge"+(charges==1?"":"s")+" remaining before a ring of pursuit breaks.");}
    public void checkInventory(){if(available()&&player.getItems().playerHasItem(ID))check();}
    public void checkWorn(){if(available()&&player.playerEquipment[Player.playerRing]==ID)check();}
    public void breakInventory(int widget,int slot){
        if(!available()||widget!=3214||slot<0||slot>=player.playerItems.length||player.playerItems[slot]!=ID+1||player.playerItemsN[slot]!=1)return;
        player.getItems().deleteItem(ID,slot,1);charges=10;
        player.sendMessage("You break the ring. Your next ring of pursuit starts with 10 charges.");
    }
}
