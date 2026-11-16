package io.xeros.content.combat.magic;

import io.xeros.Server;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;

/** A bolt enchantment pays once for up to ten bolts, independently of jewellery spells. */
public final class BoltEnchanting {
    public static final int INTERFACE=64600;
    public enum Spell {
        OPAL(4,9,879,9236,21955,21932,new int[][]{{564,1},{556,2}}),
        SAPPHIRE(7,17,9337,9240,21957,21934,new int[][]{{564,1},{555,1},{558,1}}),
        JADE(14,19,9335,9237,21959,21936,new int[][]{{564,1},{557,2}}),
        PEARL(24,29,880,9238,21961,21938,new int[][]{{564,1},{555,2}}),
        EMERALD(27,37,9338,9241,21963,21940,new int[][]{{564,1},{556,3},{561,1}}),
        TOPAZ(29,33,9336,9239,21965,21942,new int[][]{{564,1},{554,2}}),
        RUBY(49,59,9339,9242,21967,21944,new int[][]{{564,1},{554,5},{565,1}}),
        DIAMOND(57,67,9340,9243,21969,21946,new int[][]{{564,1},{557,10},{563,2}}),
        DRAGONSTONE(68,78,9341,9244,21971,21948,new int[][]{{564,1},{557,15},{566,1}}),
        ONYX(87,97,9342,9245,21973,21950,new int[][]{{564,1},{554,20},{560,1}});
        private final int level,xp,input,output,dragonInput,dragonOutput;
        private final int[][] runes;
        Spell(int level,int xp,int input,int output,int dragonInput,int dragonOutput,int[][] runes){
            this.level=level;this.xp=xp;this.input=input;this.output=output;this.dragonInput=dragonInput;this.dragonOutput=dragonOutput;this.runes=runes;
        }
        public int level(){return level;}public int xp(){return xp;}
        public int input(boolean dragon){return dragon?dragonInput:input;}public int output(boolean dragon){return dragon?dragonOutput:output;}
        public int[][] runes(){return java.util.Arrays.stream(runes).map(int[]::clone).toArray(int[][]::new);}
    }
    private final Player player;
    private boolean dragon;
    private int quantity=1;
    private long nextCastTick,generation;
    public BoltEnchanting(Player player){this.player=player;}
    private boolean allowed(){return player.getUtilityMagic().canInteract()&&player.playerMagicBook==0&&player.getMageTrainingArena().room()<0;}
    public void cancel(){generation++;}
    public void open(){
        if(!allowed())return;
        if(player.playerLevel[6]<4){player.sendMessage("You need Magic level 4 to enchant bolts.");return;}
        cancel();player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();refresh();player.getPA().showInterface(INTERFACE);
    }
    private void refresh(){
        Player p=player;
        p.getPA().sendString(64607,(dragon?"Dragon":"Normal")+" bolts - "+(quantity==Integer.MAX_VALUE?"All casts":quantity+" cast"+(quantity==1?"":"s")));
        for(Spell spell:Spell.values()){
            int base=64620+spell.ordinal()*15;
            p.getPA().itemOnInterface(spell.output(dragon),1,base+4,0);
            p.getPA().sendString(base+3,(p.playerLevel[6]>=spell.level()?"@gre@":"@red@")+"Level "+spell.level());
        }
    }
    public boolean button(int nativeId){
        if(nativeId!=64604&&nativeId!=64605&&nativeId!=64606&&(nativeId<64780||nativeId>64784)
            &&!(nativeId>=64620&&nativeId<=64755&&(nativeId-64620)%15==0))return false;
        if(player.getOpenInterface()!=INTERFACE||!allowed())return true;
        if(nativeId==64604){cancel();player.getPA().closeAllWindows();return true;}
        if(nativeId==64605||nativeId==64606){dragon=nativeId==64606;refresh();return true;}
        if(nativeId>=64780){
            if(nativeId==64783){
                player.getPA().closeAllWindows();player.getPA().sendEnterAmount("How many casts? (up to ten bolts each)",(p,n)->{if(n>0){quantity=n;open();}});
            }else{quantity=new int[]{1,5,10,0,Integer.MAX_VALUE}[nativeId-64780];refresh();}
            return true;
        }
        start((nativeId-64620)/15,dragon,quantity);return true;
    }
    public boolean start(int gem,boolean useDragon,int casts){
        if(gem<0||gem>=Spell.values().length||casts<=0)return false;
        Spell spell=Spell.values()[gem];
        if(!castOne(spell,useDragon))return false;
        cancel();player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();player.getPA().sendFrame106(6);
        if(casts==1||player.getItems().getInventoryCount(spell.input(useDragon))==0)return true;
        final long mine=generation,token=player.productionActionToken;final int[] remaining={casts-1};
        Server.getEventHandler().submit(new Event<Player>("bolt-auto-enchant",player,3){
            public void execute(){
                if(mine!=generation||token!=player.productionActionToken||!castOne(spell,useDragon)
                    ||--remaining[0]<=0||player.getItems().getInventoryCount(spell.input(useDragon))==0)stop();
            }
        });return true;
    }
    private boolean castOne(Spell spell,boolean useDragon){
        Player p=player;if(!allowed()||Server.getTickCount()<nextCastTick)return false;
        if(p.playerLevel[6]<spell.level()){p.sendMessage("You need Magic level "+spell.level()+" for this bolt enchantment.");return false;}
        int input=spell.input(useDragon),amount=Math.min(10,p.getItems().getInventoryCount(input));
        if(amount==0){p.sendMessage("You do not have these unenchanted "+(useDragon?"dragon ":"")+"bolts.");return false;}
        if(!RunePayment.exchangeWithInventoryInputs(p,new int[][]{{input,amount}},spell.runes,new int[][]{{spell.output(useDragon),amount}})){
            p.sendMessage("You need all the runes and room for the enchanted bolts.");return false;
        }
        nextCastTick=Server.getTickCount()+1;p.alchDelay=System.currentTimeMillis();
        p.startAnimation(4462);p.gfx100(759);p.getPA().addSkillXPMultiplied(spell.xp(),6,true);return true;
    }
}
