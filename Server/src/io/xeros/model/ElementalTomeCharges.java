package io.xeros.model;

import io.xeros.model.entity.player.Player;

/** A full page is twenty casts; reserve/current fields retain the legacy fire-save format. */
public class ElementalTomeCharges {
    public static final int CHARGES_PER_PAGE=20,MAX_PAGES=1000,MAX_CHARGES=20000;
    private final Player player;
    private final int chargedItem,emptyItem,loadLimit;
    private final String name;
    private int pages,charges;
    protected ElementalTomeCharges(Player player,int chargedItem,int emptyItem,String name,boolean legacyFire){
        this.player=player;this.chargedItem=chargedItem;this.emptyItem=emptyItem;this.name=name;
        // Old fire saves can contain one extra bucket. Preserve that finite balance on load.
        loadLimit=legacyFire?MAX_CHARGES+CHARGES_PER_PAGE:MAX_CHARGES;
    }
    public int getTotalCharges(){return Math.min(loadLimit,pages*CHARGES_PER_PAGE+charges);}
    public int getRemovablePages(){return getTotalCharges()/CHARGES_PER_PAGE;}
    public int getFreePageSlots(){return Math.max(0,MAX_CHARGES-getTotalCharges())/CHARGES_PER_PAGE;}
    public boolean hasCharges(){return getTotalCharges()>0;}
    public boolean hasPages(){return getRemovablePages()>0;}
    public void setTotalCharges(int total){
        total=Math.max(0,Math.min(loadLimit,total));
        if(total==0){pages=charges=0;return;}
        pages=(total-1)/CHARGES_PER_PAGE;charges=(total-1)%CHARGES_PER_PAGE+1;
    }
    public void addPages(int amount){
        int accepted=Math.min(Math.max(0,amount),getFreePageSlots());
        if(accepted>0)setTotalCharges(getTotalCharges()+accepted*CHARGES_PER_PAGE);
    }
    public void removePages(int amount){if(amount>0)setTotalCharges((int)Math.max(0,getTotalCharges()-Math.min((long)amount,Integer.MAX_VALUE)*CHARGES_PER_PAGE));}
    public void incrPage(){addPages(1);}
    public void decrPage(){removePages(1);}
    public boolean consumeCharge(){
        if(!hasCharges())return false;setTotalCharges(getTotalCharges()-1);
        if(!hasCharges()){player.sendMessage("Your "+name+" has run out of charges.");replaceEmptyEquipped();}
        return true;
    }
    public void decrCharge(){consumeCharge();}
    public void replaceEmptyEquipped(){
        if(player.playerEquipment[Player.playerShield]==chargedItem)player.getItems().setEquipment(emptyItem,1,Player.playerShield,true);
    }
    /** Separate setters let either ordering of the two legacy save keys retain its balance. */
    public int getPages(){return pages;}
    public void setPages(int pages){this.pages=Math.max(0,Math.min(MAX_PAGES,pages));}
    public int getCharges(){return charges;}
    public void setCharges(int charges){this.charges=Math.max(0,Math.min(CHARGES_PER_PAGE,charges));}
    public void reset(){pages=charges=0;}
}
