package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.skills.SkillingOutfit;
import io.xeros.content.achievement.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

/** Gem/amethyst actions use the original cache's actual object IDs and 2022 depletion rules.
 * Chance interpolation, coefficients and gem weights reference OpenRune under its ISC licence. */
public final class PreciousRockMining {
    private PreciousRockMining(){}
    public static final int[] GEMS={1625,1627,1629,1623,1621,1619,1617};
    public static boolean gem(int id){return id==9030||id==11380||id==11381;}
    public static boolean amethyst(int id){return id==11388||id==11389;}
    public static int gemForRoll(int roll){
        if(roll<0||roll>=128)throw new IllegalArgumentException("Gem roll");
        int[] cumulative={60,90,105,114,119,124,128};for(int i=0;i<cumulative.length;i++)if(roll<cumulative[i])return GEMS[i];
        throw new AssertionError();
    }
    public static boolean chargedGlory(Player p){
        for(int item:new int[]{1706,1708,1710,1712,11976,11978,10354,10356,10358,10360,11964,11966,19707})
            if(p.getItems().isWearingItem(item,Player.playerAmulet))return true;
        return false;
    }
    public static int invisibleBoost(Player p,boolean gem){
        // Stars and ordinary ores share the historical 1/256 or glory 1/86 gem table.
        int boost=0;for(int id:new int[]{25539,25541,25543,25545})if(p.getItems().isWearingItem(id,Player.playerRing)){boost=4;break;}
        if(!gem&&MiningGuildArea.contains(p.absX,p.absY,p.heightLevel))boost+=7;
        return boost;
    }
    public static int gemTableDenominator(Player p){return (chargedGlory(p)?86:256)*128;}
    public static int threshold(int visible,int boost,boolean gem,boolean glory){
        int level=Math.max(1,Math.min(99,visible))+Math.max(0,boost);
        int low=gem?28:-18,high=gem?70:10;
        if(gem&&glory){low*=3;high*=3;}
        return Math.max(0,Math.min(256,1+(int)Math.floor(low*(99d-level)/98d+high*(level-1d)/98d+.5)));
    }
    private static boolean room(Player p,boolean gem){
        if(p.getItems().freeSlots()>0)return true;
        if(gem)for(int id:new int[]{1623,1621,1619,1617,1631})if(p.getStorageCapture().capacity(id)>0)return true;
        return false;
    }
    public static boolean mine(Player p,int id,int x,int y){
        if(!gem(id)&&!amethyst(id))return false;
        start(p,id,x,y,Misc::trueRand);return true;
    }
    public static boolean start(Player p,int id,int x,int y,IntUnaryOperator random){
        boolean gem=gem(id);if(!gem&&!amethyst(id))return false;
        if(!p.getUtilityMagic().canInteract())return false;
        WorldObject rock=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);
        if(rock==null||gem&&(rock.type!=10&&rock.type!=11)||!gem&&rock.type!=0)return false;
        int empty=gem?Mineral.EMPTY_VEIN:11393;
        if(Server.getGlobalObjects().exists(empty,x,y,p.heightLevel))return false;
        if(Math.max(Math.abs(p.absX-x),Math.abs(p.absY-y))>1)return false;
        int required=gem?40:92;
        if(p.playerLevel[14]<required){p.sendMessage("You need Mining level "+required+" to mine this rock.");return false;}
        if(!gem&&!p.getMiningGuild().canMineAmethyst(x,y,p.heightLevel)){p.sendMessage("You need the Elite Falador Diary and permission from Belona to mine these western crystals.");return false;}
        Pickaxe pick=Pickaxe.getBestPickaxe(p);if(pick==null){p.sendMessage("You need a suitable pickaxe.");return false;}
        if(!room(p,gem)){p.sendMessage("Your inventory is full.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        final long token=p.productionActionToken;final int ox=p.absX,oy=p.absY,h=p.heightLevel;
        p.playerSkilling[14]=true;p.startAnimation(pick.getAnimation());p.facePosition(x,y);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
            private int until=VolcanicAsh.delay(pick,random);
            public void execute(){
                Pickaxe current=Pickaxe.getBestPickaxe(p);
                if(token!=p.productionActionToken||!p.playerSkilling[14]||p.isDisconnected()||p.getSession()==null
                    ||!p.getUtilityMagic().canInteract()||p.absX!=ox||p.absY!=oy||p.heightLevel!=h
                    ||p.getRegionProvider().get(x,y).getWorldObject(id,x,y,h).isEmpty()||p.playerLevel[14]<required||current==null||!gem&&!p.getMiningGuild().canMineAmethyst(x,y,h)||Server.getGlobalObjects().exists(empty,x,y,h)){stop();return;}
                if(!room(p,gem)){stop();return;}
                if(--until>0)return;
                until=VolcanicAsh.delay(current,random);p.startAnimation(current.getAnimation());
                if(random.applyAsInt(256)>=threshold(p.playerLevel[14],invisibleBoost(p,gem),gem,chargedGlory(p)))return;
                int item=gem?gemForRoll(random.applyAsInt(128)):21347;
                int quantity=!gem&&p.getItems().isWearingItem(13107,Player.playerChest)&&random.applyAsInt(10)==0
                        &&p.getItems().freeSlots()>=2?2:1;
                if(!ProductionBatch.exchange(p,new int[0][],new int[][]{{item,quantity}})){stop();return;}
                p.getMiningAccessories().consumeCrystal(current,random);
                p.getPA().addSkillXPMultiplied((gem?65:240)*quantity*SkillingOutfit.PROSPECTOR.multiplier(p),14,true);
                Achievements.increase(p,AchievementType.MINE,1);
                if(gem){p.getDiaryManager().getFaladorDiary().progress(io.xeros.content.achievement_diary.impl.FaladorDiaryEntry.MINE_GEM_FAL);io.xeros.content.dailytasks.DailyTasks.increase(p,io.xeros.content.dailytasks.DailyTasks.PossibleTasks.GEM_ROCK);}
                else if(MiningGuildArea.contains(x,y,h)&&p.getMiningGuild().receivesMinerals()&&random.applyAsInt(20)==0)p.getItems().addItemUnderAnyCircumstance(21341,1);
                pet(p,gem,random);
                p.sendMessage(gem?"You mine an uncut gem.":"You mine some amethyst.");
                boolean saved=!gem&&p.getItems().isWearingItem(21392,Player.playerHands)&&random.applyAsInt(4)==0;
                if(!saved){Server.getGlobalObjects().add(new GlobalObject(empty,x,y,h,rock.face,rock.type,gem?99:125,id));stop();}
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[14]=false;p.startAnimation(65535);}}
        });return true;
    }
    public static int petDenominator(int baseLevel,boolean gem){return (gem?211886:46350)-25*Math.max(1,Math.min(99,baseLevel));}
    private static void pet(Player p,boolean gem,IntUnaryOperator random){
        int base=p.getPA().getLevelForXP(p.playerXP[14]);
        if(random.applyAsInt(petDenominator(base,gem))!=0||p.getItems().getItemCount(13321,false)>0||p.petSummonId==7439)return;
        p.getCollectionLog().handleDrop(p,5,13321,1);p.getItems().addItemUnderAnyCircumstance(13321,1);
        p.sendMessage("A rock golem has formed from the rock you mined!");
    }
}
