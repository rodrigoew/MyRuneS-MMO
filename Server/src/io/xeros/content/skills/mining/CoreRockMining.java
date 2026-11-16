package io.xeros.content.skills.mining;

import io.xeros.Server;
import io.xeros.content.skills.*;
import io.xeros.content.skills.mining.motherlode.MotherlodeMine;
import io.xeros.content.achievement.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.Player;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** Standard pre-2023 ores. Coefficients reference OpenRune ISC, with historical glove rules. */
public final class CoreRockMining {
    private CoreRockMining(){}
    public enum Rock {
        CLAY(Mineral.CLAY,434,128,400,2,741600,1,0),
        COPPER(Mineral.COPPER,436,100,350,4,741600,1,0),
        TIN(Mineral.TIN,438,100,350,4,741600,1,0),
        IRON(Mineral.IRON,440,96,350,9,741600,1,0),
        SILVER(Mineral.SILVER,442,25,200,100,741600,1,2),
        COAL(Mineral.COAL,453,16,100,50,290640,1,0),
        GOLD(Mineral.GOLD,444,7,75,100,296640,1,3),
        MITHRIL(Mineral.MITHRIL,447,4,50,200,148320,2,4),
        ADAMANT(Mineral.ADAMANT,449,2,25,400,59328,3,6),
        RUNITE(Mineral.RUNE,451,1,18,1200,42377,4,8),
        ESSENCE(Mineral.ESSENCE,1436,256,256,0,0,0,0),
        BLURITE(Mineral.BLURITE,668,90,350,42,741600,1,0),
        SOFT_CLAY(Mineral.SOFT_CLAY,1761,256,256,2,148320,2,0);
        public final Mineral mineral;public final int item,low,high,respawn,petBase,varrockTier,gloveDenominator;
        Rock(Mineral mineral,int item,int low,int high,int respawn,int petBase,int tier,int glove){this.mineral=mineral;this.item=item;this.low=low;this.high=high;this.respawn=respawn;this.petBase=petBase;this.varrockTier=tier;this.gloveDenominator=glove;}
        public int item(Player p){return this==ESSENCE&&p.playerLevel[14]>=30?7936:item;}
        public int level(){return mineral.getLevel();}public double xp(){return mineral.getExperience();}
    }
    public static Rock rock(int id){Mineral mineral=Mineral.forObjectId(id);for(Rock rock:Rock.values())if(rock.mineral==mineral)return rock;return null;}
    public static int threshold(Rock rock,int visible,int invisible){
        int level=Math.max(1,Math.min(99,visible))+Math.max(0,invisible);
        return Math.max(0,Math.min(256,1+(int)Math.floor((rock.low*(99d-level)+rock.high*(level-1d))/98d+.5)));
    }
    public static boolean guild(int x,int y,int height){return MiningGuildArea.contains(x,y,height);}
    public static int invisibleBoost(Player p){
        int result=guild(p.absX,p.absY,p.heightLevel)?7:0;
        for(int ring:new int[]{25539,25541,25543,25545})if(p.getItems().isWearingItem(ring,Player.playerRing)){result+=4;break;}return result;
    }
    public static int armour(Player p){for(int tier=4;tier>=1;tier--)if(p.getItems().isWearingItem(13103+tier,Player.playerChest))return tier;return 0;}
    public static boolean cape(Player p){return p.getItems().isWearingItem(9792,Player.playerCape)||p.getItems().isWearingItem(9793,Player.playerCape)||io.xeros.content.SkillcapePerks.isWearingMaxCape(p);}
    public static int gloveDenominator(Player p,Rock rock){
        boolean expert=p.getItems().isWearingItem(21392,Player.playerHands);
        if(rock==Rock.COAL)return expert||p.getItems().isWearingItem(21343,Player.playerHands)?5:0;
        if(rock.gloveDenominator==0)return 0;
        boolean basic=rock==Rock.SILVER||rock==Rock.GOLD;
        return expert||p.getItems().isWearingItem(basic?21343:21345,Player.playerHands)?rock.gloveDenominator:0;
    }
    public static boolean gloveSave(Player p,Rock rock,IntUnaryOperator random){int n=gloveDenominator(p,rock);return n>0&&random.applyAsInt(n)<(rock==Rock.COAL?2:1);}
    public static int respawn(Rock rock,int x,int y,int h){return guild(x,y,h)?Math.max(1,rock.respawn/2):rock.respawn;}
    private static boolean room(Player p,int item){return p.getItems().freeSlots()+p.getStorageCapture().capacity(item)>0;}
    public static boolean mine(Player p,int id,int x,int y,int height){
        if(rock(id)==null)return false;
        if(height==p.heightLevel)start(p,id,x,y,Misc::trueRand);return true;
    }
    public static boolean start(Player p,int id,int x,int y,IntUnaryOperator random){
        Rock data=rock(id);if(data==null||!p.getUtilityMagic().canInteract())return false;
        WorldObject object=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);
        ObjectDef def=ObjectDef.getObjectDef(id);
        if(object==null||(object.type!=10&&object.type!=11)||def==null||def.actions==null||!Arrays.asList(def.actions).contains("Mine"))return false;
        if(Server.getGlobalObjects().exists(Mineral.EMPTY_VEIN,x,y,p.heightLevel))return false;
        var size=object.getObjectSize();int dx=Math.max(x-p.absX,p.absX-(x+size.getX()-1)),dy=Math.max(y-p.absY,p.absY-(y+size.getY()-1));
        if(Math.max(dx,dy)>1)return false;
        if(p.playerLevel[14]<data.level()){p.sendMessage("You need Mining level "+data.level()+" to mine this rock.");return false;}
        Pickaxe pick=Pickaxe.getBestPickaxe(p);if(pick==null){p.sendMessage("You need a suitable pickaxe.");return false;}
        if(!room(p,data.item(p))){p.sendMessage("Your inventory is full.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();long token=p.productionActionToken;int ox=p.absX,oy=p.absY,h=p.heightLevel;
        p.playerSkilling[14]=true;p.startAnimation(pick.getAnimation());p.facePosition(x,y);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
            int until=VolcanicAsh.delay(pick,random);
            public void execute(){
                Pickaxe current=Pickaxe.getBestPickaxe(p);
                if(token!=p.productionActionToken||!p.playerSkilling[14]||p.isDisconnected()||p.getSession()==null||!p.getUtilityMagic().canInteract()||p.absX!=ox||p.absY!=oy||p.heightLevel!=h||!p.isWalkingQueueEmpty()||p.playerLevel[14]<data.level()||current==null||p.getRegionProvider().get(x,y).getWorldObject(id,x,y,h).isEmpty()||Server.getGlobalObjects().exists(Mineral.EMPTY_VEIN,x,y,h)){stop();return;}
                boolean bracelet=p.getMiningAccessories().bracelet(data);int item=bracelet?1761:data.item(p);if(!room(p,item)){stop();return;}if(--until>0)return;until=VolcanicAsh.delay(current,random);p.startAnimation(current.getAnimation());
                if(data!=Rock.ESSENCE){int gem=MotherlodeMine.preGem(random.applyAsInt(PreciousRockMining.gemTableDenominator(p)));if(gem!=-2){if(gem>0&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{gem,1}}))stop();return;}}
                if(random.applyAsInt(256)>=threshold(data,p.playerLevel[14],invisibleBoost(p)))return;
                if(!award(p,data,item,random)){stop();return;}
                if(bracelet&&data==Rock.SOFT_CLAY&&room(p,item))ProductionBatch.exchange(p,new int[0][],new int[][]{{item,1}});
                p.getMiningAccessories().consumeCrystal(current,random);
                // Independent bonuses never force an extra ore or award XP when delivery fails.
                if(data.varrockTier>0&&data!=Rock.RUNITE&&cape(p)&&random.applyAsInt(100)<5)award(p,data,item,random);
                if(data.varrockTier>0&&armour(p)>=data.varrockTier&&random.applyAsInt(100)<10)award(p,data,item,random);
                if(p.getMiningTools().ringBonus(data,random))award(p,data,item,random);
                if(bracelet)p.getMiningAccessories().consumeClay();
                pet(p,data,random);
                if(guild(x,y,h)&&p.getMiningGuild().receivesMinerals()){int chance=data==Rock.IRON?100:data==Rock.COAL?60:data==Rock.MITHRIL?40:data==Rock.ADAMANT?30:data==Rock.RUNITE?20:0;if(chance>0&&random.applyAsInt(chance)==0)p.getItems().addItemUnderAnyCircumstance(21341,1);}
                Achievements.increase(p,AchievementType.MINE,1);diaries(p,data);p.sendMessage("You mine some "+io.xeros.model.definitions.ItemDef.forId(item).getName()+".");
                if(data!=Rock.ESSENCE&&!gloveSave(p,data,random)){
                    GlobalObject empty=new GlobalObject(Mineral.EMPTY_VEIN,x,y,h,object.face,object.type,respawn(data,x,y,h),id);empty.setInstance(p.getInstance());Server.getGlobalObjects().add(empty);stop();
                }
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[14]=false;p.startAnimation(65535);}}
        });return true;
    }
    private static boolean award(Player p,Rock rock,int item,IntUnaryOperator random){
        if(!p.getMiningTools().combust(rock,random)&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{item,1}}))return false;
        p.getPA().addSkillXPMultiplied(rock.xp()*SkillingOutfit.PROSPECTOR.multiplier(p),14,true);return true;
    }
    private static void diaries(Player p,Rock rock){
        switch(rock){
            case COAL:
                if(io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.RELLEKKA_BOUNDARY))p.getDiaryManager().getFremennikDiary().progress(io.xeros.content.achievement_diary.impl.FremennikDiaryEntry.MINE_COAL_FREM);break;
            case ESSENCE:p.getDiaryManager().getVarrockDiary().progress(io.xeros.content.achievement_diary.impl.VarrockDiaryEntry.MINE_ESSENCE);break;
            case GOLD:
                if(io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.TZHAAR_CITY_BOUNDARY))p.getDiaryManager().getKaramjaDiary().progress(io.xeros.content.achievement_diary.impl.KaramjaDiaryEntry.MINE_GOLD_KAR);break;
            case IRON:
                if(p.getPosition().inWild())p.getDiaryManager().getWildernessDiary().progress(io.xeros.content.achievement_diary.impl.WildernessDiaryEntry.MINE_IRON_WILD);
                if(io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.VARROCK_BOUNDARY))p.getDiaryManager().getVarrockDiary().progress(io.xeros.content.achievement_diary.impl.VarrockDiaryEntry.MINE_IRON);
                if(io.xeros.model.entity.player.Boundary.isIn(p,io.xeros.model.entity.player.Boundary.AL_KHARID_BOUNDARY))p.getDiaryManager().getLumbridgeDraynorDiary().progress(io.xeros.content.achievement_diary.impl.LumbridgeDraynorDiaryEntry.MINE_IRON_LUM);break;
            case MITHRIL:
                if(p.getPosition().inWild())p.getDiaryManager().getWildernessDiary().progress(io.xeros.content.achievement_diary.impl.WildernessDiaryEntry.MINE_MITHRIL_WILD);
                io.xeros.content.dailytasks.DailyTasks.increase(p,io.xeros.content.dailytasks.DailyTasks.PossibleTasks.MITHRIL_ORES);break;
            case CLAY:p.getDiaryManager().getDesertDiary().progress(io.xeros.content.achievement_diary.impl.DesertDiaryEntry.MINE_CLAY);break;
            default:break;
        }
    }
    public static int petDenominator(Rock rock,int baseLevel){return rock.petBase==0?0:Math.max(1,rock.petBase-25*Math.max(1,Math.min(99,baseLevel)));}
    private static void pet(Player p,Rock rock,IntUnaryOperator random){
        int chance=petDenominator(rock,p.getPA().getLevelForXP(p.playerXP[14]));if(chance==0||p.getItems().getItemCount(13321,false)>0||p.petSummonId==7439||random.applyAsInt(chance)!=0)return;
        p.getItems().addItemUnderAnyCircumstance(13321,1);p.getCollectionLog().handleDrop(p,5,13321,1);p.sendMessage("A rock golem has formed from the rock you mined!");
    }
}
