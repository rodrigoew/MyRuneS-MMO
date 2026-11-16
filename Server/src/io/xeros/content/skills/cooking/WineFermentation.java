package io.xeros.content.skills.cooking;

import io.xeros.content.dialogue.*;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
import io.xeros.model.items.bank.*;
import java.util.*;
import java.util.function.LongSupplier;

/** One 12-second batch per character, including wines deposited in the bank. */
public final class WineFermentation {
    public static final int GRAPES=1987,WATER=1937,UNFERMENTED=1995,WINE=1993,BAD=1991;
    private final Player player;
    private final LongSupplier clock;
    private long deadline,seed;
    private boolean blockedMessage;
    public WineFermentation(Player player){this(player,System::currentTimeMillis);}
    public WineFermentation(Player player,LongSupplier clock){this.player=player;this.clock=clock;}
    public boolean itemOnItem(int first,int second){
        if(!(first==GRAPES&&second==WATER||first==WATER&&second==GRAPES))return false;
        new DialogueBuilder(player).option("Make unfermented wine",
            new DialogueOption("Make 1",p->mix(1)),new DialogueOption("Make 5",p->mix(5)),new DialogueOption("Make all",p->mix(28))).send();return true;
    }
    public boolean mix(int quantity){
        process();
        if(!player.getUtilityMagic().canInteract())return false;
        if(deadline>0&&clock.getAsLong()>=deadline)return false;
        return ProductionBatch.start(player,7,35,0,quantity,363,2,new int[][]{{GRAPES,1},{WATER,1}},new int[][]{{UNFERMENTED,1}},()->{
            long now=clock.getAsLong();deadline=now>Long.MAX_VALUE-12000?Long.MAX_VALUE:now+12000;
            seed=new SplittableRandom().nextLong();blockedMessage=false;
        },()->player.getUtilityMagic().canInteract());
    }
    public long deadline(){return deadline;}
    public static long successful(long count,int level,SplittableRandom random){
        double chance=Math.max(0,Math.min(1,.19+1.19*(level-1)/98d));
        long group=count/15,good=0;
        for(int i=0;i<15;i++)if(random.nextDouble()<chance)good+=group;
        for(long i=0;i<count%15;i++)if(random.nextDouble()<chance)good++;
        return good;
    }
    public boolean process(){
        if(deadline==0||clock.getAsLong()<deadline)return false;
        List<Integer> inventory=new ArrayList<>();
        for(int i=0;i<player.playerItems.length;i++)if(player.playerItems[i]==UNFERMENTED+1){
            if(player.playerItemsN[i]!=1)return false;inventory.add(i);
        }
        long bankCount=0;for(BankTab tab:player.getBank().getBankTab())for(BankItem item:tab.getItems())if(item.getId()==UNFERMENTED+1)bankCount+=Math.max(0,item.getAmount());
        long total=inventory.size()+bankCount;
        if(total==0){deadline=0;seed=0;return true;}
        SplittableRandom random=new SplittableRandom(seed);long good=successful(total,player.playerLevel[7],random),remainingGood=good,remaining=total;
        List<Integer> products=new ArrayList<>();
        for(int slot:inventory){boolean success=random.nextDouble()<remainingGood/(double)remaining;products.add(success?WINE:BAD);remaining--;if(success)remainingGood--;}
        Map<Integer,Long> bankInputs=new LinkedHashMap<>(),bankOutputs=new LinkedHashMap<>();
        if(bankCount>0){bankInputs.put(UNFERMENTED,bankCount);if(remainingGood>0)bankOutputs.put(WINE,remainingGood);if(bankCount-remainingGood>0)bankOutputs.put(BAD,bankCount-remainingGood);}
        BankTransformation plan=BankTransformation.prepare(player.getBank(),bankInputs,bankOutputs,true);
        if(plan==null){if(!blockedMessage)player.sendMessage("Your wine is ready, but a bank stack cannot hold the results. Make room to finish fermenting.");blockedMessage=true;return false;}
        for(int i=0;i<inventory.size();i++)player.playerItems[inventory.get(i)]=products.get(i)+1;
        plan.commit();deadline=0;seed=0;blockedMessage=false;
        if(good>0)player.getPA().addSkillXPMultiplied(good*200d,7,true);
        player.getItems().resetItems(3214);if(player.isBanking){player.getItems().resetTempItems();player.getItems().queueBankContainerUpdate();}
        player.sendMessage("Your wine has fermented: "+good+" good, "+(total-good)+" bad.");return true;
    }
    public String encode(){return deadline+","+seed;}
    public boolean decode(String data){
        try{String[] parts=data.split(",",-1);if(parts.length!=2)return false;long next=Long.parseLong(parts[0]),nextSeed=Long.parseLong(parts[1]);if(next<0)return false;deadline=next;seed=next==0?0:nextSeed;blockedMessage=false;return true;}catch(RuntimeException e){return false;}
    }
}
