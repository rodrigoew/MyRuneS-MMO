package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.content.achievement_diary.impl.WildernessDiaryEntry;
import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.ItemToDestroy;
import io.xeros.model.entity.player.DestroyType;
import io.xeros.model.shops.ShopAssistant;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;

/** Ordinary alchemy. Arena alchemy is handled separately by its activity. */
public final class Alchemy {
    private final Player player;
    private int warningThreshold=1000;
    private boolean warnUntradeable=true;
    public Alchemy(Player player){this.player=player;}
    public int getWarningThreshold(){return warningThreshold;}
    public boolean warnsUntradeable(){return warnUntradeable;}
    public boolean setWarningThreshold(int value){if(value<0)return false;warningThreshold=value;return true;}
    public void setWarnUntradeable(boolean value){warnUntradeable=value;}
    public String encodeWarnings(){return warningThreshold+","+warnUntradeable+","+player.isAlchWarning();}
    public boolean decodeWarnings(String value){
        try{
            String[] fields=value.split(",",-1);
            if(fields.length!=3||!fields[1].matches("true|false")||!fields[2].matches("true|false"))return false;
            int threshold=Integer.parseInt(fields[0]);if(threshold<0)return false;
            warningThreshold=threshold;warnUntradeable=Boolean.parseBoolean(fields[1]);player.setAlchWarning(Boolean.parseBoolean(fields[2]));return true;
        }catch(RuntimeException e){return false;}
    }
    public boolean warningRequired(int item){
        AlchemyItemData.Entry entry=AlchemyItemData.forId(item);
        // A private offline world has local valuations rather than a live Grand Exchange feed.
        return player.isAlchWarning()&&entry!=null&&entry.allowed()
            &&((warnUntradeable&&!entry.isTradeable())||ShopAssistant.getItemShopValue(entry.getBase())>=warningThreshold);
    }
    public void configureWarnings(){
        Player p=player;if(p.isDead||p.getHealth().getCurrentHealth()<=0||p.getLock().cannotInteract(p)||p.isDisconnected())return;
        p.getPA().closeAllWindows();
        new DialogueBuilder(p).option("Alchemy warnings (local values)",
            new DialogueOption("Set value threshold ("+warningThreshold+")",q->{
                q.getPA().closeAllWindows();
                q.getPA().sendEnterAmount("Warn from which local value? (0 = every item)",(r,n)->{
                    if(setWarningThreshold(n)){r.setAlchWarning(true);r.sendMessage("Alchemy warning threshold: "+n+" coins (local value).");}
                    else r.sendMessage("Please enter a value from 0 to 2,147,483,647.");
                });
                q.rawAmountInput=true;
            }),
            new DialogueOption((warnUntradeable?"Disable":"Enable")+" untradeable item warning",q->{
                warnUntradeable=!warnUntradeable;q.setAlchWarning(true);q.getPA().closeAllWindows();
                q.sendMessage("Untradeable alchemy item warning: "+(warnUntradeable?"enabled.":"disabled."));
            }),DialogueOption.nevermind()).send();
    }
    public boolean request(int item,int slot,boolean high){
        if(!canStart(item,slot,high))return false;
        if(warningRequired(item)){
            player.destroyItem=new ItemToDestroy(item,slot,high?DestroyType.HIGH_ALCH:DestroyType.LOW_ALCH);
            player.getPA().destroyInterface("alch");return false;
        }
        return cast(item,slot,high);
    }
    private boolean canStart(int item,int slot,boolean high){
        Player p=player;
        if(p.isDead||p.getHealth().getCurrentHealth()<=0||p.isDisconnected()||p.getSession()==null||p.getLock().cannotInteract(p)||p.isBanking||p.isShopping||p.teleTimer>0)return false;
        if(!p.getUtilityMagic().ready())return false;
        if(p.playerMagicBook!=0||p.playerLevel[6]<(high?55:21)){p.sendMessage("You need the normal spellbook and the required Magic level.");return false;}
        if(!p.getItems().isItemInInventorySlot(item,slot))return false;
        AlchemyItemData.Entry entry=AlchemyItemData.forId(item);
        if(entry==null||!entry.allowed()){p.sendMessage("This item cannot be alchemised.");return false;}
        return true;
    }
    public boolean cast(int item,int slot,boolean high){
        if(!canStart(item,slot,high))return false;
        Player p=player;
        AlchemyItemData.Entry entry=AlchemyItemData.forId(item);
        boolean fountain=Boundary.isIn(p,Boundary.FOUNTAIN_OF_RUNE_BOUNDARY);
        int coins=entry.coins(high);
        int[][] costs=fountain?new int[0][]:new int[][]{{561,1},{554,high?5:3}};
        int[][] output=coins==0?new int[0][]:new int[][]{{995,coins}};
        if(!RunePayment.exchangeWithInventoryInputs(p,new int[][]{{item,1}},costs,output)){
            p.sendMessage("You need the required runes and enough room for the coins.");return false;
        }
        p.getUtilityMagic().committedCast(high?5:3);
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();p.attacking.reset();
        p.startAnimation(high?713:712);p.gfx100(high?113:112);p.alchDelay=System.currentTimeMillis();
        if(!fountain)p.getPA().addSkillXPMultiplied(high?65:31,6,true);
        else if(!high)p.getDiaryManager().getWildernessDiary().progress(WildernessDiaryEntry.LOW_ALCH);
        p.getPA().sendFrame106(6);
        if(p.getItems().getInventoryCount(item)==0)p.getStorageCapture().destroyed(item);
        if(item==12791&&p.getItems().getTotalCount(item)==0){p.getRunePouch().getItems().clear();p.getRunePouch().sendPouchRuneInventory();}
        if(item==20714&&p.getItems().getTotalCount(item)==0)p.getTomeOfFire().reset();
        if(item==25574&&p.getItems().getTotalCount(item)==0)p.getTomeOfWater().reset();
        if(item==22370&&p.getItems().getTotalCount(item)==0)p.bryophytaStaffCharges=0;
        return true;
    }
}
