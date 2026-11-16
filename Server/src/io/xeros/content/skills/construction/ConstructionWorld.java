package io.xeros.content.skills.construction;
import io.xeros.content.dialogue.*;import io.xeros.content.skills.ProductionBatch;import io.xeros.content.skills.mining.MiningGuildStores;import io.xeros.model.collisionmap.WorldObject;import io.xeros.model.entity.npc.*;import io.xeros.model.entity.player.*;
public final class ConstructionWorld {
    private ConstructionWorld(){}
    static boolean near(Player p,NPC npc){
        return npc!=null&&npc.getIndex()>=0&&npc.getIndex()<NPCHandler.npcs.length&&NPCHandler.npcs[npc.getIndex()]==npc&&!npc.isDead()&&!p.isDead&&p.getInstance()==null&&p.heightLevel==npc.getHeight()&&p.distanceToPoint(npc.getX(),npc.getY())<=2&&!p.getLock().cannotInteract(p)&&p.getUtilityMagic().canInteract()&&!p.isForceMovementActive();
    }
    public static boolean npc(Player p,NPC npc,int option){
        if(npc==null)return false;int id=npc.getNpcId();
        if(id==7303||id==7304){
            if(option!=1)return false;if(!near(p,npc)||p.getHealth().getCurrentHealth()<=0)return true;
            new DialogueBuilder(p).option("Watson",new DialogueOption("Exchange clue scrolls",q->{q.getPA().removeAllWindows();if(near(q,npc))io.xeros.content.trails.MasterClue.exchangeClue(q);}),new DialogueOption("Buy S.T.A.S.H. blueprint (500 coins)",q->{q.getPA().removeAllWindows();if(near(q,npc)&&q.getHealth().getCurrentHealth()>0)ProductionBatch.exchange(q,new int[][]{{995,500}},new int[][]{{25612,1}});}),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();return true;
        }
        if(id==3100){if(option==1)p.getHouse().workshop().heraldry().talk(npc);return true;}
        if(id==3097){
            if(!near(p,npc))return true;
            if(option==1)p.getHouse().menu();
            return true;
        }
        int shop=MiningGuildStores.constructionStore(npc);if(shop<0)return false;if(!near(p,npc))return true;
        boolean sawmill=id==3101||id==9140;
        if(sawmill&&option==2){plankMenu(p,npc);return true;}
        if(option==(sawmill?3:2)){MiningGuildStores.open(p,shop);return true;}
        if(option!=1)return false;
        if(sawmill)new DialogueBuilder(p).option("Sawmill operator",new DialogueOption("Buy planks",q->plankMenu(q,npc)),new DialogueOption("Construction supplies",q->MiningGuildStores.open(q,shop)),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();
        else new DialogueBuilder(p).option(id==3098?"Stonemason":"Garden supplier",new DialogueOption("Trade",q->MiningGuildStores.open(q,shop)),new DialogueOption("Leave",q->q.getPA().removeAllWindows())).send();
        return true;
    }
    private static void plankMenu(Player p,NPC npc){
        if(!near(p,npc))return;
        new DialogueBuilder(p).option("Convert unnoted logs into planks",new DialogueOption("Logs: 100 coins each",q->quantity(q,npc,1511,960,100)),new DialogueOption("Oak: 250 coins each",q->quantity(q,npc,1521,8778,250)),new DialogueOption("Teak: 500 coins each",q->quantity(q,npc,6333,8780,500)),new DialogueOption("Mahogany: 1,500 coins each",q->quantity(q,npc,6332,8782,1500))).send();
    }
    private static void quantity(Player p,NPC npc,int log,int plank,int price){
        if(!near(p,npc))return;
        new DialogueBuilder(p).option("How many planks?",new DialogueOption("1",q->makePlanks(q,npc,log,plank,price,1)),new DialogueOption("5",q->makePlanks(q,npc,log,plank,price,5)),new DialogueOption("10",q->makePlanks(q,npc,log,plank,price,10)),new DialogueOption("All",q->makePlanks(q,npc,log,plank,price,28))).send();
    }
    public static boolean makePlanks(Player p,NPC npc,int log,int plank,int price,int requested){
        if(!near(p,npc)||MiningGuildStores.constructionStore(npc)<224||MiningGuildStores.constructionStore(npc)>226||requested<1||requested>28)return false;
        if(!((log==1511&&plank==960&&price==100)||(log==1521&&plank==8778&&price==250)||(log==6333&&plank==8780&&price==500)||(log==6332&&plank==8782&&price==1500)))return false;
        int n=Math.min(requested,Math.min(p.getItems().getInventoryCount(log),p.getItems().getInventoryCount(995)/price));
        p.getPA().removeAllWindows();
        if(n==0||!ProductionBatch.exchange(p,new int[][]{{log,n},{995,n*price}},new int[][]{{plank,n}})){p.sendMessage("Bring unnoted logs and enough coins.");return false;}
        p.interruptActions();p.sendMessage("The sawmill operator makes "+n+" planks.");return true;
    }
    public static boolean portal(Player p,WorldObject o,int option){
        if(o.id!=15478||o.x!=2951||o.y!=3222||o.height!=0)return false;
        if(p.getInstance()!=null||p.heightLevel!=0||Math.max(Math.max(o.x-p.absX,p.absX-o.x-o.getObjectSize().getX()+1),Math.max(o.y-p.absY,p.absY-o.y-o.getObjectSize().getY()+1))>1||p.isDead||p.getLock().cannotInteract(p))return true;
        if(option==3)p.getHouse().enter(true);else if(option==2)p.getHouse().enter(false);else if(option==1)p.getHouse().menu();return true;
    }
}
