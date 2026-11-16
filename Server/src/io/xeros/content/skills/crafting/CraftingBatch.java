package io.xeros.content.skills.crafting;

import io.xeros.model.cycleevent.*;
import io.xeros.model.entity.player.Player;

/** One recipe, quantity and origin per player; materials rechecked every cycle. */
final class CraftingBatch {
    private CraftingBatch() {}
    static boolean start(Player p, int quantity, int level, double xp, int product,
                         int animation, int ticks, int[][] materials, Runnable made) {
        if (quantity <= 0 || p.playerIsCrafting || !p.getUtilityMagic().canInteract()) return false;
        if (p.playerLevel[12] < level) { p.sendMessage("Your Crafting level is too low."); return false; }
        for (int[] material : materials) if (!p.getItems().playerHasItem(material[0], Math.max(1, material[1]))) {
            p.sendMessage("You do not have the tools or materials required."); return false;
        }
        p.interruptActions();p.getPA().stopSkilling();final long token=p.productionActionToken;
        int x=p.absX,y=p.absY,h=p.heightLevel;int[] remaining={Math.min(quantity,28)};
        p.getPA().removeAllWindows();p.playerIsCrafting=true;p.startAnimation(animation);
        CycleEventHandler.getSingleton().addEvent(p,new CycleEvent(){
            public void execute(CycleEventContainer event){
                if (token!=p.productionActionToken || !p.playerIsCrafting || !p.getUtilityMagic().canInteract() || p.isDisconnected() || p.getSession()==null || p.absX!=x || p.absY!=y || p.heightLevel!=h || p.playerLevel[12]<level) { event.stop();return; }
                for(int[] material:materials) if(!p.getItems().playerHasItem(material[0],Math.max(1,material[1]))) { event.stop();return; }
                int[][] actual=new int[materials.length][];
                for(int i=0;i<materials.length;i++)actual[i]=new int[]{materials[i][0],materials[i][0]==1734&&p.craftingThreadUses>1?0:materials[i][1]};
                if(!io.xeros.content.skills.ProductionBatch.exchange(p,actual,new int[][]{{product,1}})){event.stop();return;}
                for(int[] material:materials) if(material[1]>0) {

                    if(material[0]==1734){if(--p.craftingThreadUses<=0){p.craftingThreadUses=5;}}
                    
                }
                p.getPA().addSkillXPMultiplied(xp,12,true);p.startAnimation(animation);made.run();
                if(--remaining[0]<=0) event.stop();
            }
            public void onStopped(){if(token!=p.productionActionToken)return;p.playerIsCrafting=false;p.craftDialogue=false;p.battlestaffDialogue=false;p.startAnimation(65535);}
        },ticks);
        return true;
    }
}
