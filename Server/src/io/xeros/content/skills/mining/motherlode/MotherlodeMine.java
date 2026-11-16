package io.xeros.content.skills.mining.motherlode;

import io.xeros.Server;import io.xeros.content.dialogue.*;import io.xeros.content.skills.*;import io.xeros.content.skills.mining.*;
import io.xeros.model.collisionmap.*;import io.xeros.model.cycleevent.Event;import io.xeros.model.entity.player.*;import io.xeros.model.entity.npc.*;import io.xeros.util.Misc;
import java.util.*;import java.util.function.*;

/** Solo Motherlode state. Persistent pre-rolled ores, washing batches and sack never share rewards. */
public final class MotherlodeMine {
    public static final int PAYDIRT=12011,NUGGET=12012;
    private static final class Batch {final int[] ores;int ticks;Batch(int[] ores,int ticks){this.ores=ores;this.ticks=ticks;}}
    private final Player player;private final int[] held=new int[6],sack=new int[6];private final Deque<Batch> washing=new ArrayDeque<>();
    private boolean upperUnlocked,bigSack,attached;private int coal;
    private final MotherlodeWorld world=MotherlodeWorld.INSTANCE;
    public MotherlodeMine(Player player){this.player=player;}
    public int capacity(){return bigSack?162:81;}public boolean upperUnlocked(){return upperUnlocked;}
    public int sackTotal(){return total(sack);}public int waiting(){int n=0;for(Batch b:washing)n+=total(b.ores);return n;}
    public int coal(){return coal;}
    public void clearCoal(){coal=0;}
    public void storeCoal(int amount){if(amount<=0||coal+amount>coalCapacity())throw new IllegalArgumentException("Coal bag capacity");coal+=amount;}
    public int coalCapacity(){return io.xeros.content.SkillcapePerks.SMITHING.isWearing(player)||io.xeros.content.SkillcapePerks.isWearingMaxCape(player)?36:27;}
    public void coalBag(int option){
        if(!ready()||!player.getItems().playerHasItem(12019))return;
        if(option==2){player.sendMessage("Your coal bag contains "+coal+" coal.");return;}
        if(option==1){int amount=Math.min(player.getItems().getInventoryCount(453),Math.max(0,coalCapacity()-coal));
            if(amount>0&&ProductionBatch.exchange(player,new int[][]{{12019,0},{453,amount}},new int[0][])){player.interruptActions();coal+=amount;}return;}
        int amount=player.isBanking?coal:Math.min(coal,player.getItems().freeSlots());
        if(amount>0&&player.getStorageCapture().without(()->player.isBanking?io.xeros.content.items.pouch.ContainerStorage.bank(player,453,amount):ProductionBatch.exchange(player,new int[][]{{12019,0}},new int[][]{{453,amount}}))){player.interruptActions();coal-=amount;}
    }
    public boolean itemOnItem(int first,int second){if(first==12019||second==12019){if(first==453||second==453)coalBag(1);return true;}return false;}
    public boolean use(int item,int id,int x,int y){
        if(item==PAYDIRT&&id==26674&&x==3748&&y==5672){deposit(Misc::trueRand);return true;}return false;
    }
    public static int bagGem(int roll){if(roll<0||roll>=200)throw new IllegalArgumentException();return roll<100?1623:roll<169?1621:roll<193?1619:roll<199?1617:1631;}
    public boolean openGems(IntUnaryOperator random){
        return openGems(19473,random);
    }
    public boolean openGems(int bag,IntUnaryOperator random){
        if((bag!=19473&&bag!=24853&&bag!=25537)||!ready()||!player.getItems().playerHasItem(bag))return false;
        Map<Integer,Integer> outputs=new LinkedHashMap<>();for(int i=0;i<40;i++){int id=random.applyAsInt(100_000_000)==0?6571:bagGem(random.applyAsInt(200));outputs.merge(id+1,1,Integer::sum);}
        int[][] result=new int[outputs.size()][2];int index=0;for(Map.Entry<Integer,Integer> item:outputs.entrySet())result[index++]=new int[]{item.getKey(),item.getValue()};
        if(!ProductionBatch.exchange(player,new int[][]{{bag,1}},result))return false;
        player.interruptActions();if(outputs.containsKey(6572))player.getCollectionLog().handleDrop(player,5,6571,outputs.get(6572));return true;
    }
    private static int total(int[] rows){int count=0;for(int n:rows)count+=n;return count;}
    public void login(){world.initialize();configs();if(!attached){attached=true;player.addTickable((container,p)->process());}}
    public void configs(){player.getPA().sendConfig(375,Math.min(255,sackTotal())<<8);player.getPA().sendConfig(638,upperUnlocked?1<<21:0);}
    private boolean ready(){return !player.isDead&&!player.isDisconnected()&&player.getSession()!=null&&!player.getLock().cannotInteract(player);}
    private WorldObject object(int id,int x,int y){return player.getRegionProvider().get(x,y).getWorldObject(id,x,y,player.heightLevel).orElse(null);}
    private boolean near(WorldObject o){
        if(o==null||!ready()||player.heightLevel!=o.height)return false;
        Position s=o.getObjectSize();int dx=Math.max(o.x-player.absX,player.absX-o.x-Math.max(1,s.getX())+1),dy=Math.max(o.y-player.absY,player.absY-o.y-Math.max(1,s.getY())+1);
        return Math.max(dx,dy)<=1;
    }
    private void action(int animation,int interval,BooleanSupplier guard,BooleanSupplier step){
        player.interruptActions();player.getPA().stopSkilling();player.getPA().removeAllWindows();long token=player.productionActionToken;
        int x=player.absX,y=player.absY,h=player.heightLevel;player.playerSkilling[14]=true;player.startAnimation(animation);
        Server.getEventHandler().submit(new Event<Player>("skilling",player,interval){
            public void execute(){if(token!=player.productionActionToken||!player.playerSkilling[14]||!ready()||player.absX!=x||player.absY!=y||player.heightLevel!=h||!guard.getAsBoolean()){stop();return;}
                if(!step.getAsBoolean()){stop();return;}player.startAnimation(animation);}
            public void stop(){super.stop();if(token==player.productionActionToken){player.playerSkilling[14]=false;player.startAnimation(65535);}}
        });
    }
    public boolean click(int id,int x,int y,int option){
        boolean inside=MotherlodeWorld.area(x,y,player.heightLevel);
        if(!inside&&!(id==26654&&x==3059&&y==9764)&&!(id==30374&&x==3055&&y==9743))return false;
        world.initialize();WorldObject o=object(id,x,y);if(!near(o))return true;
        if(id>=26661&&id<=26668){mine(id,x,y,Misc::trueRand);return true;}
        if(option!=1)return false;
        if(id==26674&&x==3748&&y==5672){deposit(Misc::trueRand);return true;}
        if(id==26688&&x==3748&&y==5659){collect();return true;}
        if(id==26707){player.getItems().openUpBank();return true;}
        if(id==26670&&x==3742&&(y==5663||y==5669)){repair(y==5663?0:1,Misc::trueRand);return true;}
        if(id==26679||id==26680){rockfall(o);return true;}
        if(id==19044&&x==3755&&y==5673){
            if(!upperUnlocked||player.getPA().getLevelForXP(player.playerXP[14])<72){player.sendMessage("Pay Percy 100 nuggets and reach base Mining level 72 to use the upper level.");return true;}
            travel(o,new Position(3755,5675,0),828);return true;
        }
        if(id==19045&&x==3755&&y==5674){travel(o,new Position(3755,5672,0),827);return true;}
        if(id==26654){if(player.playerLevel[14]<30){player.sendMessage("You need Mining level 30 to enter the Motherlode Mine.");return true;}travel(o,new Position(3728,5692,0),844);return true;}
        if(id==26655){travel(o,new Position(3060,9766,0),844);return true;}
        if(id==30374){travel(o,new Position(3718,5678,0),844);return true;}
        if(id==30375){if(player.playerLevel[14]<60){player.sendMessage("You need Mining level 60 to enter the Mining Guild.");return true;}travel(o,new Position(3054,9744,0),844);return true;}
        return false;
    }
    private void travel(WorldObject o,Position destination,int animation){action(animation,2,()->near(o),()->{player.moveTo(destination);configs();return false;});}
    public boolean mine(int id,int x,int y,IntUnaryOperator random){
        world.initialize();MotherlodeWorld.Vein vein=world.vein(x,y);if(vein==null||!vein.active||id!=vein.activeId||!near(object(id,x,y)))return false;
        if(player.playerLevel[14]<30){player.sendMessage("You need Mining level 30 to mine pay-dirt.");return false;}
        if(vein.upper&&(!upperUnlocked||player.getPA().getLevelForXP(player.playerXP[14])<72)){player.sendMessage("Unlock the upper level with Percy first.");return false;}
        Pickaxe pick=Pickaxe.getBestPickaxe(player);if(pick==null){player.sendMessage("You need a suitable pickaxe.");return false;}
        if(player.getItems().freeSlots()==0){player.sendMessage("Your inventory is full.");return false;}
        long generation=vein.generation;final int[] delay={VolcanicAsh.delay(pick,random)};
        action(pick.getAnimation(),1,()->vein.active&&vein.generation==generation&&player.playerLevel[14]>=30&&Pickaxe.getBestPickaxe(player)!=null,()->{
            if(player.getItems().freeSlots()==0)return false;
            if(--delay[0]>0)return true;
            delay[0]=VolcanicAsh.delay(Pickaxe.getBestPickaxe(player),random);
            int pre=preGem(random.applyAsInt(PreciousRockMining.gemTableDenominator(player)));
            if(pre!=-2){if(pre>=0)ProductionBatch.exchange(player,new int[0][],new int[][]{{pre,1}});return true;}
            int level=Math.min(99,player.playerLevel[14])+PreciousRockMining.invisibleBoost(player,true);
            int threshold=Math.max(0,Math.min(256,1+(int)Math.floor((60*(99d-level)+105*(level-1d))/98+.5)));
            if(random.applyAsInt(256)>=threshold)return true;
            if(!ProductionBatch.exchange(player,new int[0][],new int[][]{{PAYDIRT,1}}))return false;
            player.getMiningAccessories().consumeCrystal(Pickaxe.getBestPickaxe(player),random);
            MotherlodeOre ore=MotherlodeOre.roll(player.playerLevel[14],random);if(total(held)<4096)held[ore.ordinal()]++;
            int baseLevel=player.getPA().getLevelForXP(player.playerXP[14]);
            player.getPA().addSkillXPMultiplied(60*SkillingOutfit.PROSPECTOR.multiplier(player),14,true);
            if(random.applyAsInt(247200-25*Math.min(99,Math.max(1,baseLevel)))==0&&player.getItems().getItemCount(13321,false)==0&&player.petSummonId!=7439){
                player.getCollectionLog().handleDrop(player,5,13321,1);player.getItems().addItemUnderAnyCircumstance(13321,1);player.sendMessage("A rock golem has formed from the ore vein!");
            }
            return world.harvested(vein,random);
        });return true;
    }
    public static int preGem(int roll){if(roll<70)return -1;if(roll<102)return 1623;if(roll<118)return 1621;if(roll<126)return 1619;if(roll<128)return 1617;return -2;}
    public boolean deposit(IntUnaryOperator random){
        if(!near(object(26674,3748,5672)))return false;
        int count=player.getItems().getInventoryCount(PAYDIRT);
        if(count==0){player.sendMessage("You have no pay-dirt to deposit.");return false;}
        // Before 2024 a last full inventory could be deposited while the sack was below its threshold.
        if(sackTotal()+waiting()>=capacity()){player.sendMessage("The sack is full. Collect your ore first.");return false;}
        int[] source=held.clone(),rolled=new int[6];int left=count;
        for(int i=0;i<source.length&&left>0;i++){int take=Math.min(left,source[i]);rolled[i]+=take;source[i]-=take;left-=take;}
        while(left-->0)rolled[MotherlodeOre.roll(player.playerLevel[14],random).ordinal()]++;
        if(!ProductionBatch.exchange(player,new int[][]{{PAYDIRT,count}},new int[0][]))return false;
        System.arraycopy(source,0,held,0,held.length);washing.addLast(new Batch(rolled,13));player.startAnimation(832);configs();return true;
    }
    public void process(){
        if(!world.flowing()||washing.isEmpty())return;
        Batch batch=washing.peekFirst();if(--batch.ticks>0)return;washing.removeFirst();double xp=0;
        for(MotherlodeOre ore:MotherlodeOre.values()){
            int amount=batch.ores[ore.ordinal()];if(ore==MotherlodeOre.NUGGET)amount=Math.min(3,amount);
            sack[ore.ordinal()]+=amount;xp+=ore.xp*amount;
        }
        player.getPA().addSkillXPMultiplied(xp*SkillingOutfit.PROSPECTOR.multiplier(player),14,true);configs();player.sendMessage("Your cleaned ore is ready in the sack.");
    }
    public boolean collect(){
        if(!near(object(26688,3748,5659)))return false;boolean changed=false;
        for(MotherlodeOre ore:MotherlodeOre.values()){
            int stored=sack[ore.ordinal()];if(stored==0)continue;
            int quantity=ore==MotherlodeOre.NUGGET?stored:Math.min(stored,player.getItems().freeSlots()+player.getStorageCapture().capacity(ore.item));
            if(quantity>0&&ProductionBatch.exchange(player,new int[0][],new int[][]{{ore.item,quantity}})){sack[ore.ordinal()]-=quantity;changed=true;}
        }
        configs();if(!changed)player.sendMessage(sackTotal()>0?"Make room to collect your ore.":waiting()>0?"Your pay-dirt is still being cleaned.":"The sack is empty.");return changed;
    }
    private boolean hammer(){return player.getItems().playerHasItem(2347)||player.getItems().playerHasItem(25644)||player.getItems().isWearingItem(25644);}
    public boolean repair(int index,IntUnaryOperator random){
        int y=index==0?5663:5669;
        if(!near(object(26670,3742,y))||!world.broken(index))return false;
        if(!hammer()){player.sendMessage("You need a hammer to repair this strut.");return false;}
        action(898,1,()->world.broken(index)&&hammer(),()->{
            int level=Math.max(1,Math.min(99,player.playerLevel[13]));double chance=.12+.15*(level-1)/98d;
            if(random.applyAsInt(10000)>=chance*10000)return true;
            if(world.repair(index,random))player.getPA().addSkillXPMultiplied(Math.max(1,player.playerLevel[13])*1.5,13,true);return false;
        });return true;
    }
    private void rockfall(WorldObject rock){
        Pickaxe pick=Pickaxe.getBestPickaxe(player);if(pick==null){player.sendMessage("You need a suitable pickaxe to clear this rockfall.");return;}
        action(pick.getAnimation(),2,()->Pickaxe.getBestPickaxe(player)!=null&&object(rock.id,rock.x,rock.y)!=null,()->{
            if(world.clearRockfall(rock,Misc::trueRand))player.getPA().addSkillXPMultiplied(10,14,true);return false;
        });
    }
    private boolean nearPercy(){
        return ready()&&player.heightLevel==0&&Arrays.stream(NPCHandler.npcs).filter(Objects::nonNull).anyMatch(n->n.getNpcId()==6562&&!n.isDead()&&n.getHeight()==0&&n.getX()==3756&&n.getY()==5666&&player.distanceToPoint(n.getX(),n.getY())<=2);
    }
    public boolean percy(NPC npc,int option){
        if(npc==null||npc.getNpcId()!=6562||npc.getX()!=3756||npc.getY()!=5666)return false;
        if(!nearPercy())return true;
        new DialogueBuilder(player).option("Prospector Percy",
            new DialogueOption("Prospector equipment",p->equipment()),new DialogueOption("Bags and supplies",p->supplies()),
            new DialogueOption("Improve the mine",p->upgrades()),new DialogueOption("Sell prospector equipment",p->sellMenu()),
            new DialogueOption("Leave",p->p.getPA().removeAllWindows())).send();return true;
    }
    private void equipment(){new DialogueBuilder(player).option("Pay in golden nuggets",
        new DialogueOption("Helmet: 40",p->purchase(12013,40)),new DialogueOption("Jacket: 60",p->purchase(12014,60)),
        new DialogueOption("Legs: 50",p->purchase(12015,50)),new DialogueOption("Boots: 30",p->purchase(12016,30)),new DialogueOption("Leave",p->p.getPA().removeAllWindows())).send();}
    private void supplies(){new DialogueBuilder(player).option("Pay in golden nuggets",
        new DialogueOption("Coal bag: 100",p->purchase(12019,100)),new DialogueOption("Gem bag: 100",p->purchase(12020,100)),
        new DialogueOption("Soft clay pack: 10",p->purchase(12009,10)),new DialogueOption("Bag full of gems: 40",p->purchase(19473,40)),new DialogueOption("Leave",p->p.getPA().removeAllWindows())).send();}
    private void upgrades(){new DialogueBuilder(player).option("Mine upgrades",
        new DialogueOption("Upper level: 100 nuggets, base Mining 72",p->upgrade(true)),
        new DialogueOption("Larger sack: 200 nuggets",p->upgrade(false)),new DialogueOption("Leave",p->p.getPA().removeAllWindows())).send();}
    private void sellMenu(){new DialogueBuilder(player).option("Sell equipment for nuggets",
        new DialogueOption("Helmet: 32",p->sell(12013,32)),new DialogueOption("Jacket: 48",p->sell(12014,48)),
        new DialogueOption("Legs: 40",p->sell(12015,40)),new DialogueOption("Boots: 24",p->sell(12016,24)),new DialogueOption("Leave",p->p.getPA().removeAllWindows())).send();}
    public boolean purchase(int item,int price){
        if(!nearPercy()||!Map.of(12013,40,12014,60,12015,50,12016,30,12019,100,12020,100,12009,10,19473,40).containsKey(item))return false;
        int expected=Map.of(12013,40,12014,60,12015,50,12016,30,12019,100,12020,100,12009,10,19473,40).get(item);if(price!=expected)return false;
        if((item==12019||item==12020)&&player.getItems().getItemCount(item,false)>0){player.sendMessage("You already own this bag.");return false;}
        if(!ProductionBatch.exchange(player,new int[][]{{NUGGET,price}},new int[][]{{item,1}})){player.sendMessage("You need enough nuggets and inventory space.");return false;}
        player.getPA().removeAllWindows();return true;
    }
    private void sell(int item,int price){if(nearPercy())ProductionBatch.exchange(player,new int[][]{{item,1}},new int[][]{{NUGGET,price}});player.getPA().removeAllWindows();}
    public boolean upgrade(boolean upper){
        if(!nearPercy()||upper&&upperUnlocked||!upper&&bigSack)return false;
        if(upper&&player.getPA().getLevelForXP(player.playerXP[14])<72){player.sendMessage("You need base Mining level 72.");return false;}
        if(!ProductionBatch.exchange(player,new int[][]{{NUGGET,upper?100:200}},new int[0][])){player.sendMessage("You do not have enough nuggets.");return false;}
        if(upper)upperUnlocked=true;else bigSack=true;configs();player.getPA().removeAllWindows();return true;
    }
    private static String numbers(int[] values){StringJoiner out=new StringJoiner(",");for(int value:values)out.add(Integer.toString(value));return out.toString();}
    public String encode(){StringJoiner queue=new StringJoiner(";");for(Batch b:washing)queue.add(b.ticks+":"+numbers(b.ores));return(upperUnlocked?1:0)+","+(bigSack?1:0)+","+coal+"|"+numbers(held)+"|"+numbers(sack)+"|"+queue;}
    private static int[] parseNumbers(String value,int max){String[] f=value.split(",",-1);if(f.length!=6)throw new IllegalArgumentException();int[] out=new int[6];long sum=0;for(int i=0;i<6;i++){out[i]=Integer.parseInt(f[i]);if(out[i]<0)throw new IllegalArgumentException();sum+=out[i];}if(sum>max)throw new IllegalArgumentException();return out;}
    public boolean decode(String value){try{
        String[] fields=value.split("\\|",-1);if(fields.length!=4)return false;String[] flags=fields[0].split(",",-1);if(flags.length!=3)return false;
        int up=Integer.parseInt(flags[0]),big=Integer.parseInt(flags[1]),bag=Integer.parseInt(flags[2]);if(up<0||up>1||big<0||big>1||bag<0||bag>36)return false;
        int[] nextHeld=parseNumbers(fields[1],4096),nextSack=parseNumbers(fields[2],189);Deque<Batch> next=new ArrayDeque<>();int count=total(nextSack);
        if(!fields[3].isEmpty())for(String row:fields[3].split(";",-1)){
            String[] part=row.split(":",-1);if(part.length!=2)return false;int ticks=Integer.parseInt(part[0]);int[] ores=parseNumbers(part[1],28);if(ticks<1||ticks>13||total(ores)==0)return false;count+=total(ores);if(count>189)return false;next.addLast(new Batch(ores,ticks));
        }
        if(count>(big==1?189:108))return false;
        upperUnlocked=up==1;bigSack=big==1;coal=bag;System.arraycopy(nextHeld,0,held,0,6);System.arraycopy(nextSack,0,sack,0,6);washing.clear();washing.addAll(next);return true;
    }catch(RuntimeException malformed){return false;}}
}
