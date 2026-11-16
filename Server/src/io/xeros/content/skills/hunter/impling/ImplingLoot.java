package io.xeros.content.skills.hunter.impling;
import io.xeros.Server;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;

/** Weighted 2022 rewards; one lucky roll includes an explicitly linked potion bundle. */
public final class ImplingLoot {
    private static volatile Dataset tables;
    public static final Random RANDOM=new Random();
    private ImplingLoot(){}
    public static final class Dataset { List<Creature> species;List<Tier> tiers;Map<String,int[]> clueFamilies; }
    public static final class Creature { String name;int jar;List<Entry> main,preroll,tertiary; }
    public static final class Tier { String tier;List<Entry> entries; }
    public static class Amount { int item=-1,minimum,maximum; }
    public static final class Entry extends Amount {
        long weightNumerator,weightDenominator;String clueTable;List<Amount> additional;
        double rate(){return (double)weightNumerator/weightDenominator;}
        void add(List<int[]> out,Random random){addAmount(out,this,random);if(additional!=null)for(Amount a:additional)addAmount(out,a,random);}
    }
    public static final class Outcome {
        public final int[][] items;public final boolean broken;public final int luckyTier;
        Outcome(List<int[]> items,boolean broken,int tier){this.items=items.toArray(new int[0][]);this.broken=broken;luckyTier=tier;}
    }
    private static void require(boolean b,String message){if(!b)throw new IllegalStateException("Impling rewards: "+message);}
    public static void load()throws Exception{
        Dataset d=new Gson().fromJson(Files.readString(Path.of(Server.getDataDirectory(),"cfg/item/impling_2022.json")),Dataset.class);
        require(d!=null&&d.species!=null&&d.species.size()==12&&d.tiers!=null&&d.tiers.size()==5,"missing historical tables");
        for(ImplingData k:ImplingData.values()){
            Creature c=d.species.get(k.ordinal());require(k.name.equals(c.name)&&c.jar==k.jar,"species order "+k);
            check(c.main,true,k==ImplingData.LUCKY);check(c.preroll,false,false);check(c.tertiary,false,false);
        }
        String[] tiers={"easy","medium","hard","elite","master"};
        for(int i=0;i<5;i++){require(tiers[i].equals(d.tiers.get(i).tier),"clue tier order");check(d.tiers.get(i).entries,true,false);}
        require(d.clueFamilies!=null,"clue ownership groups");tables=d;
    }
    private static Dataset data(){Dataset d=tables;if(d==null)throw new IllegalStateException("Impling reward tables were not loaded");return d;}
    private static void check(List<Entry> list,boolean unit,boolean lucky){
        require(list!=null,"missing entry list");double sum=0;
        for(Entry e:list){require(e.weightNumerator>0&&e.weightDenominator>0&&e.weightNumerator<=e.weightDenominator,"bad probability");sum+=e.rate();
            if(lucky){require(e.clueTable!=null,"lucky tier entry");continue;}
            amount(e);if(e.additional!=null)for(Amount a:e.additional)amount(a);
        }
        if(unit)require(Math.abs(sum-1)<1e-12,"probability mass "+sum);
    }
    private static void amount(Amount a){
        require(a.item==-1||(a.minimum>0&&a.maximum>=a.minimum&&ItemDef.getDefinitions().containsKey(a.item)),"unknown item or bad amount "+a.item);
        require(a.item!=19835&&a.item!=19730,"forbidden master clue or bloodhound");
    }
    private static void addAmount(List<int[]> out,Amount a,Random random){if(a.item>=0)out.add(new int[]{a.item,a.minimum+random.nextInt(a.maximum-a.minimum+1)});}
    private static Entry pick(List<Entry> entries,Random random){double roll=random.nextDouble();for(Entry e:entries){roll-=e.rate();if(roll<0)return e;}return entries.get(entries.size()-1);}
    private static boolean ownsClue(Player p,int item){
        int[] family=data().clueFamilies.get(Integer.toString(item));if(family==null)return false;
        for(int i:family)if(p.getItems().playerHasItem(i)||p.getItems().bankContains(i))return true;
        return false;
    }
    public static Outcome roll(Player p,ImplingData kind,boolean returnJar,Random random){
        Dataset d=data();Creature c=d.species.get(kind.ordinal());List<int[]> out=new ArrayList<>();int tier=-1;
        if(kind==ImplingData.LUCKY){tier=random.nextInt(5);pick(d.tiers.get(tier).entries,random).add(out,random);}
        else{
            Entry selected=null;for(Entry e:c.preroll)if(random.nextDouble()<e.rate()){selected=e;break;}
            if(selected==null)selected=pick(c.main,random);selected.add(out,random);
            for(Entry e:c.tertiary)if(random.nextDouble()<e.rate()&&!ownsClue(p,e.item))e.add(out,random);
        }
        boolean broken=returnJar&&random.nextInt(10)==0;
        if(returnJar&&!broken)out.add(new int[]{11260,1});
        return new Outcome(out,broken,tier);
    }
    public static Outcome roll(Player p,ImplingData kind,boolean returnJar){return roll(p,kind,returnJar,RANDOM);}
    public static boolean open(Player p,int jar){
        ImplingData kind=ImplingData.forJar(jar);if(kind==null)return false;
        if(!p.getUtilityMagic().canInteract()||p.getMovementState().isLocked()||Server.getMultiplayerSessionListener().inAnySession(p)||!p.getItems().playerHasItem(jar))return true;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
        Outcome loot=roll(p,kind,true);
        if(ProductionBatch.exchange(p,new int[][]{{jar,1}},loot.items))p.sendMessage(loot.broken?"You loot the impling. The jar breaks.":"You loot the impling and keep the empty jar.");
        return true;
    }
}
