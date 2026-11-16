package io.xeros.content.skills.crafting;

import io.xeros.Server;
import io.xeros.content.combat.magic.RunePayment;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.types.MakeItemDialogue;
import io.xeros.model.collisionmap.RegionProvider;
import io.xeros.model.collisionmap.WorldObject;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;
import io.xeros.model.tickable.impl.WalkToTickable;

/** Each charging action owns its obelisk, quantity and pending cast. */
public final class OrbCharging {
    private final Player player;
    private long generation;
    public OrbCharging(Player player){this.player=player;}
    private static CraftingData.chargeOrbData spell(int widget,int object){
        for(CraftingData.chargeOrbData s:CraftingData.chargeOrbData.values())if(s.getSpell()==widget&&s.getObjectId(object)==object)return s;
        return null;
    }
    private int[][] runes(CraftingData.chargeOrbData s){
        int rune=s==CraftingData.chargeOrbData.AIR?556:s==CraftingData.chargeOrbData.WATER?555:s==CraftingData.chargeOrbData.EARTH?557:554;
        return new int[][]{{564,3},{rune,30}};
    }
    private boolean available(CraftingData.chargeOrbData s){
        return player.getUtilityMagic().canInteract()&&!player.getMovementState().isLocked()&&player.playerMagicBook==0
            &&player.playerLevel[6]>=s.getLevel()&&player.getMageTrainingArena().room()<0;
    }
    private boolean live(CraftingData.chargeOrbData s,WorldObject target,RegionProvider provider){
        return target!=null&&(target.type==10||target.type==11)&&target.height==player.heightLevel&&player.getRegionProvider()==provider
            &&s.getObjectId(target.id)==target.id&&provider.get(target.x,target.y).getWorldObject(target.id,target.x,target.y,target.height).filter(target::equals).isPresent();
    }
    private boolean adjacent(WorldObject target){
        Position size=target.getObjectSize();int w=Math.max(1,size.getX()),h=Math.max(1,size.getY());
        return player.absX>=target.x-1&&player.absX<=target.x+w&&player.absY>=target.y-1&&player.absY<=target.y+h;
    }
    private boolean materials(CraftingData.chargeOrbData s){
        return RunePayment.canExchangeWithInventoryInputs(player,new int[][]{{567,1}},runes(s),new int[][]{{s.getProduct(),1}});
    }
    public boolean request(int widget,int objectId,int x,int y){
        CraftingData.chargeOrbData s=spell(widget,objectId);
        if(s==null||!available(s)||!player.getUtilityMagic().ready())return false;
        if(Math.max(Math.abs(player.absX-x),Math.abs(player.absY-y))>25)return false;
        RegionProvider provider=player.getRegionProvider();WorldObject target=provider.get(x,y).getWorldObject(objectId,x,y,player.heightLevel).orElse(null);
        if(!live(s,target,provider))return false;
        if(!materials(s)){player.sendMessage("You need an unpowered orb, the required runes and room for the charged orb.");return false;}
        player.interruptActions();player.getPA().stopSkilling();final long selection=++generation;
        if(adjacent(target)&&player.newWalkCmdSteps==0&&player.isWalkingQueueEmpty())choice(s,target,provider,selection);
        else {Position size=target.getObjectSize();player.setTickable(new WalkToTickable(player,target.getPosition(),Math.max(1,size.getX()),Math.max(1,size.getY()),p->choice(s,target,provider,selection)));}
        return true;
    }
    private void choice(CraftingData.chargeOrbData s,WorldObject target,RegionProvider provider,long selection){
        if(selection!=generation||!available(s)||!live(s,target,provider)||!adjacent(target)||!materials(s))return;
        player.facePosition(target.x,target.y);player.getPA().removeAllWindows();final long token=player.productionActionToken;
        new DialogueBuilder(player).makeItems("How many orbs would you like to charge?",150,selected->{
            if(selection==generation&&token==player.productionActionToken)start(s,target,provider,selected.getAmount());
        },new MakeItemDialogue.MakeItem(s.getProduct())).send();
    }
    public boolean start(int widget,int objectId,int x,int y,int amount){
        CraftingData.chargeOrbData s=spell(widget,objectId);if(s==null)return false;
        RegionProvider provider=player.getRegionProvider();WorldObject target=provider.get(x,y).getWorldObject(objectId,x,y,player.heightLevel).orElse(null);
        return start(s,target,provider,amount);
    }
    private boolean start(CraftingData.chargeOrbData s,WorldObject target,RegionProvider provider,int amount){
        if(amount<=0||!available(s)||!player.getUtilityMagic().ready()||!live(s,target,provider)||!adjacent(target)
            ||player.newWalkCmdSteps>0||!player.isWalkingQueueEmpty()||!materials(s))return false;
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();player.attacking.reset();
        final long mine=++generation,token=player.productionActionToken;final int x=player.absX,y=player.absY,h=player.heightLevel;
        final int[] remaining={Math.min(amount,player.getItems().getInventoryCount(567))};player.playerIsCrafting=true;
        animate(s,target);player.getUtilityMagic().committedCast(3);
        Server.getEventHandler().submit(new Event<Player>("orb-charging",player,1){
            private boolean casting=true;
            private long due=Server.getTickCount()+3;
            public void execute(){
                if(mine!=generation||token!=player.productionActionToken||!player.playerIsCrafting||!available(s)
                    ||player.absX!=x||player.absY!=y||player.heightLevel!=h||player.newWalkCmdSteps>0||!player.isWalkingQueueEmpty()
                    ||!live(s,target,provider)||!adjacent(target)){stop();return;}
                if(Server.getTickCount()<due)return;
                if(!casting){
                    if(!player.getUtilityMagic().ready()||!materials(s)){stop();return;}
                    animate(s,target);player.getUtilityMagic().committedCast(3);casting=true;due=Server.getTickCount()+3;return;
                }
                if(!RunePayment.exchangeWithInventoryInputs(player,new int[][]{{567,1}},runes(s),new int[][]{{s.getProduct(),1}})){stop();return;}
                player.getPA().addSkillXPMultiplied(s.getXP(),6,true);
                if(--remaining[0]<=0||player.getItems().getInventoryCount(567)==0){stop();return;}
                casting=false;due=Server.getTickCount()+3;
            }
            public void stop(){super.stop();if(mine==generation&&token==player.productionActionToken)player.playerIsCrafting=false;}
        });return true;
    }
    private void animate(CraftingData.chargeOrbData s,WorldObject target){player.facePosition(target.x,target.y);player.startAnimation(726);player.gfx100(s.getOrbGfx());}
}
