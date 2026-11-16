package io.xeros.model.items.bank;

import java.util.*;

/** Prepare every tab before changing any of them. Item arguments use normal game IDs. */
public final class BankTransformation {
    private final Bank bank;
    private final List<List<BankItem>> result;
    private BankTransformation(Bank bank,List<List<BankItem>> result){this.bank=bank;this.result=result;}
    public static BankTransformation prepare(Bank bank,Map<Integer,Long> inputs,Map<Integer,Long> outputs,boolean fermentation){
        List<List<BankItem>> tabs=new ArrayList<>();int preferred=0,oldSize=0;
        for(BankTab tab:bank.getBankTab()){
            List<BankItem> copy=new ArrayList<>();for(BankItem item:tab.getItems())copy.add(new BankItem(item.getId(),item.getAmount()));
            oldSize+=copy.size();tabs.add(copy);
        }
        for(Map.Entry<Integer,Long> input:inputs.entrySet()){
            if(input.getKey()<0||input.getValue()<0)return null;long remaining=input.getValue();boolean located=false;
            for(int t=0;t<tabs.size();t++)for(Iterator<BankItem> it=tabs.get(t).iterator();it.hasNext();){
                BankItem item=it.next();if(item.getId()!=input.getKey()+1)continue;
                if(!located){preferred=t;located=true;}int take=(int)Math.min(remaining,Math.max(0,item.getAmount()));
                item.setAmount(item.getAmount()-take);remaining-=take;if(item.getAmount()==0)it.remove();
            }
            if(remaining!=0)return null;
        }
        for(Map.Entry<Integer,Long> output:outputs.entrySet()){
            long amount=output.getValue();if(output.getKey()<0||amount<0||amount>Integer.MAX_VALUE)return null;if(amount==0)continue;
            BankItem existing=null;
            for(List<BankItem> tab:tabs)for(BankItem item:tab)if(item.getId()==output.getKey()+1){existing=item;break;}
            if(existing!=null){long total=existing.getAmount()+amount;if(total>Integer.MAX_VALUE)return null;existing.setAmount((int)total);}
            else tabs.get(preferred).add(new BankItem(output.getKey()+1,(int)amount));
        }
        int size=tabs.stream().mapToInt(List::size).sum();
        // Fermenting one stack into good/bad wine can temporarily add one bank slot.
        if(size>Math.max(oldSize,bank.getBankCapacity())+(fermentation?1:0))return null;
        return new BankTransformation(bank,tabs);
    }
    public void commit(){
        for(int i=0;i<result.size();i++){bank.getBankTab()[i].getItems().clear();bank.getBankTab()[i].getItems().addAll(result.get(i));}
    }
}
