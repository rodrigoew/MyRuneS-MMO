package io.xeros.content.skills.woodcutting;

import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.content.achievement.*;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.entity.player.*;
import io.xeros.model.items.GameItem;
import io.xeros.model.world.objects.GlobalObject;
import io.xeros.util.Misc;
import java.util.function.IntUnaryOperator;

/** Pre-Forestry resource action. Special activities retain their own handlers. */
public final class HistoricalWoodcutting {
    private HistoricalWoodcutting(){}
    public static boolean click(Player p,int id,int x,int y){
        if(HistoricalTreeData.node(id)==null)return false;start(p,id,x,y,Misc::trueRand);return true;
    }
    public static int axeTier(Player p){Hatchet axe=Hatchet.getBest(p);return axe==null?-1:Math.min(8,axe.tier(p));}
    private static boolean near(Player p,WorldObject o){
        var size=o.getObjectSize();
        return Math.max(Math.max(o.x-p.absX,p.absX-o.x-size.getX()+1),Math.max(o.y-p.absY,p.absY-o.y-size.getY()+1))<=1;
    }
    public static int nestForRoll(int roll,boolean rabbit){
        if(roll<0||roll>=(rabbit?95:100))throw new IllegalArgumentException("Nest roll");
        return roll<3?5070+roll:roll<35?5074:5073;
    }
    public static boolean nestCape(Player p){
        return p.getItems().isWearingItem(9807,Player.playerCape)||p.getItems().isWearingItem(9808,Player.playerCape)
            ||io.xeros.content.SkillcapePerks.isWearingMaxCape(p);
    }
    public static int respawn(HistoricalTreeData.Kind kind,IntUnaryOperator random){
        // 2022 wiki's observed 35.4–58.8 seconds for one-log trees.
        return kind==HistoricalTreeData.Kind.REGULAR||kind==HistoricalTreeData.Kind.ACHEY?59+random.applyAsInt(40):kind.respawn;
    }
    public static boolean start(Player p,int id,int x,int y,IntUnaryOperator random){
        HistoricalTreeData.Node node=HistoricalTreeData.node(id);
        if(node==null||!p.getUtilityMagic().canInteract())return false;
        WorldObject o=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,p.heightLevel).orElse(null);
        if(o==null||(o.type!=10&&o.type!=11)||!near(p,o))return false;
        if(p.playerLevel[8]<node.kind.level){p.sendMessage("You need Woodcutting level "+node.kind.level+" to cut this tree.");return false;}
        Hatchet axe=Hatchet.getBest(p);
        if(axe==null){p.sendMessage("You need a suitable axe.");return false;}
        if(p.getItems().freeSlots()==0){p.sendMessage("Your inventory is full.");return false;}
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        long token=p.productionActionToken;int ox=p.absX,oy=p.absY,h=p.heightLevel;
        p.playerSkilling[8]=true;p.facePosition(x,y);p.startAnimation(axe.getAnimation());
        Server.getEventHandler().submit(new Event<Player>("skilling",p,1){
            int until=4;
            public void execute(){
                Hatchet tool=Hatchet.getBest(p);
                if(token!=p.productionActionToken||!p.playerSkilling[8]||p.isDisconnected()||p.getSession()==null
                    ||!p.getUtilityMagic().canInteract()||p.absX!=ox||p.absY!=oy||p.heightLevel!=h
                    ||!p.isWalkingQueueEmpty()||p.playerLevel[8]<node.kind.level||tool==null||p.getItems().freeSlots()==0
                    ||p.getRegionProvider().get(x,y).getWorldObject(id,x,y,h).isEmpty()){stop();return;}
                if(--until>0)return;until=4;p.startAnimation(tool.getAnimation());
                if(random.applyAsInt(256)>=HistoricalTreeData.threshold(node.kind,axeTier(p),p.playerLevel[8],WoodcuttingGuildArea.contains(p.absX,p.absY,h)?7:0))return;
                boolean burned=p.getWoodcuttingTools().combust(tool,node.kind,random);
                if(!burned&&!ProductionBatch.exchange(p,new int[0][],new int[][]{{node.kind.item,1}})){stop();return;}
                p.getWoodcuttingTools().consumeCrystal(tool,random);
                double xp=node.kind.xp;
                if(node.kind==HistoricalTreeData.Kind.MAPLE&&Boundary.isIn(p,Boundary.SEERS_BOUNDARY)
                    &&p.getDiaryManager().getKandarinDiary().hasCompleted("MEDIUM"))xp*=1.1;
                p.getPA().addSkillXPMultiplied(xp*LumberjackOutfit.multiplier(p.getItems()::isWearingItem),8,true);
                if(node.kind==HistoricalTreeData.Kind.REGULAR&&headgear(p)&&p.getItems().freeSlots()>0)
                    ProductionBatch.exchange(p,new int[0][],new int[][]{{1511,1}});
                Achievements.increase(p,AchievementType.WOODCUT,1);
                WoodcuttingDiaries.progress(p,node.kind);
                drops(p,node.kind,random);
                p.sendMessage("You get some "+io.xeros.model.definitions.ItemDef.forId(node.kind.item).getName()+".");
                if(random.applyAsInt(node.kind.deplete)==0){
                    GlobalObject stump=new GlobalObject(node.stump,x,y,h,o.face,o.type,respawn(node.kind,random),id);
                    stump.setInstance(p.getInstance());Server.getGlobalObjects().add(stump);stop();
                }
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[8]=false;p.startAnimation(65535);}}
        });return true;
    }
    private static boolean headgear(Player p){for(int id:new int[]{13137,13138,13139,13140})if(p.getItems().isWearingItem(id,Player.playerHat))return true;return false;}
    private static void drops(Player p,HistoricalTreeData.Kind kind,IntUnaryOperator random){
        if(kind!=HistoricalTreeData.Kind.REDWOOD&&random.applyAsInt(2560)<(nestCape(p)?11:10)){
            boolean rabbit=p.getItems().isWearingItem(10132,Player.playerAmulet);
            int nest=nestForRoll(random.applyAsInt(rabbit?95:100),rabbit);
            Server.itemHandler.createPrivateTimedGroundItem(p,new GameItem(nest,1),p.getPosition(),200);
            p.sendMessage("A bird's nest falls out of the tree.");
        }
        int chance=kind.petBase-25*Math.max(1,Math.min(99,p.getPA().getLevelForXP(p.playerXP[8])));
        if(random.applyAsInt(chance)==0&&p.getItems().getItemCount(13322,false)==0&&p.petSummonId!=6717){
            p.getItems().addItemUnderAnyCircumstance(13322,1);p.getCollectionLog().handleDrop(p,5,13322,1);
        }
    }
}
