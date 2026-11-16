package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.content.skills.crafting.Enchantment;
import io.xeros.content.skills.smithing.GoldSmeltingBonus;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;

/** Per-character cadence shared by ordinary item spells; materials and runes commit together. */
public final class UtilityMagic {
    private static final int[] ENCHANT_WIDGETS={1155,1165,1176,1180,1187,6003,23649};
    private static final int[][][] ENCHANT_RUNES={
        {{564,1},{555,1}},{{564,1},{556,3}},{{564,1},{554,5}},{{564,1},{557,10}},
        {{564,1},{555,15},{557,15}},{{564,1},{554,20},{557,20}},{{564,1},{565,20},{566,20}}
    };
    private final Player player;
    private long nextCastTick;
    public UtilityMagic(Player player){this.player=player;}
    public boolean ready(){return Server.getTickCount()>=nextCastTick;}
    public void committedCast(int ticks){nextCastTick=Server.getTickCount()+ticks;player.alchDelay=System.currentTimeMillis();}
    public boolean canInteract(){
        Player p=player;
        return !p.isDead&&p.getHealth().getCurrentHealth()>0&&!p.isDisconnected()&&p.getSession()!=null
            &&!p.getLock().cannotInteract(p)&&!p.getMovementState().isLocked()&&!p.getBankPin().requiresUnlock()&&!p.isBanking&&!p.isShopping&&p.teleTimer<=0;
    }
    private void beginManual(){
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();player.attacking.reset();
    }
    public boolean superheat(int item,int slot){
        Player p=player;
        if(!canInteract()||!ready()||p.playerMagicBook!=0||!p.getItems().isItemInInventorySlot(item,slot))return false;
        int[][] recipes={{436,438,1,2349,1},{438,436,1,2349,1},{440,0,0,2351,15},{442,0,0,2355,20},
            {444,0,0,2357,40},{447,453,4,2359,50},{449,453,6,2361,70},{451,453,8,2363,85}};
        double[] experience={6.2,6.2,12.5,13.7,22.5,30,37.5,50};
        for(int i=0;i<recipes.length;i++)if(item==recipes[i][0]){
            int second=recipes[i][1],amount=recipes[i][2],bar=recipes[i][3],smithing=recipes[i][4];double xp=experience[i];
            // Two physical coal selects steel even when the character cannot yet smelt steel.
            if(item==440&&p.getItems().getInventoryCount(453)>=2){second=453;amount=2;bar=2353;smithing=30;xp=17.5;}
            if(p.playerLevel[6]<43||p.playerLevel[13]<smithing){p.sendMessage("You do not have the Magic or Smithing level required.");return false;}
            int[][] ores=second==0?new int[][]{{item,1}}:new int[][]{{item,1},{second,amount}};
            if(!RunePayment.exchangeWithInventoryInputs(p,ores,new int[][]{{561,1},{554,4}},new int[][]{{bar,1}})){
                p.sendMessage("You need the ores, runes and room for the bar.");return false;
            }
            committedCast(3);beginManual();
            p.startAnimation(CombatSpellData.MAGIC_SPELLS[54][2]);p.gfx100(CombatSpellData.MAGIC_SPELLS[54][3]);
            p.getPA().addSkillXPMultiplied(GoldSmeltingBonus.experience(p,bar,xp),13,true);
            p.getPA().addSkillXPMultiplied(53,6,true);p.getPA().sendFrame106(6);return true;
        }
        p.sendMessage("You can only cast Superheat Item on unnoted ore.");return false;
    }
    public boolean enchantJewellery(int item,int slot,int widget){
        if(!enchantOne(item,slot,widget,true))return false;
        if(player.getItems().getInventoryCount(item)>0)autoEnchant(item,widget);
        return true;
    }
    private boolean enchantOne(int item,int slot,int widget,boolean manual){
        Player p=player;Enchantment.Enchant recipe=Enchantment.Enchant.forId(item);int tier=-1;
        for(int i=0;i<ENCHANT_WIDGETS.length;i++)if(ENCHANT_WIDGETS[i]==widget)tier=i;
        if(recipe==null||tier<0||!canInteract()||!ready()||p.playerMagicBook!=0||!p.getItems().isItemInInventorySlot(item,slot))return false;
        if(recipe.getELevel()!=tier+1){p.sendMessage("That enchantment does not match this jewellery.");return false;}
        if(p.playerLevel[6]<recipe.getLevelReq()){p.sendMessage("You need Magic level "+recipe.getLevelReq()+" to cast this enchantment.");return false;}
        if(!RunePayment.exchangeWithInventoryInputs(p,new int[][]{{item,1}},ENCHANT_RUNES[tier],new int[][]{{recipe.getEnchanted(),1}})){
            p.sendMessage("You need the jewellery, runes and room for the enchanted item.");return false;
        }
        committedCast(3);if(manual)beginManual();
        p.startAnimation(recipe.getAnim());p.gfx100(recipe.getGFX());
        p.getPA().addSkillXPMultiplied(recipe.getXp(),6,true);if(manual)p.getPA().sendFrame106(6);return true;
    }
    private void autoEnchant(int item,int widget){
        Player p=player;final long token=p.productionActionToken;final int x=p.absX,y=p.absY,h=p.heightLevel;
        Server.getEventHandler().submit(new Event<Player>("jewellery-auto-enchant",p,7){
            public void execute(){
                if(token!=p.productionActionToken||p.absX!=x||p.absY!=y||p.heightLevel!=h||!p.isWalkingQueueEmpty()
                    ||!enchantOne(item,p.getItems().getInventoryItemSlot(item),widget,false)||p.getItems().getInventoryCount(item)==0)stop();
            }
        });
    }
}
