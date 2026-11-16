package io.xeros.content.items;

import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.ElementalTomeCharges;
import io.xeros.model.entity.player.Player;

/** Page transactions are committed only while the selected book is still present. */
public final class ElementalTomes {
    private ElementalTomes() {}
    public enum Kind {
        FIRE(20714,20716,20718,"fire"), WATER(25574,25576,25578,"water");
        public final int charged,empty,page; public final String name;
        Kind(int charged,int empty,int page,String name){this.charged=charged;this.empty=empty;this.page=page;this.name=name;}
        public ElementalTomeCharges charges(Player p){return this==FIRE?p.getTomeOfFire():p.getTomeOfWater();}
    }
    public static Kind kind(int id){for(Kind k:Kind.values())if(id==k.charged||id==k.empty)return k;return null;}
    private static boolean present(Player p,int id,boolean worn){return worn?p.playerEquipment[Player.playerShield]==id:p.getItems().getInventoryCount(id)>0;}
    private static boolean allowed(Player p,int id,boolean worn){return !p.isDead&&!p.getLock().cannotInteract(p)&&present(p,id,worn);}
    public static void check(Player p,int id){Kind k=kind(id);if(k!=null)p.sendMessage("Your tome of "+k.name+" has "+(id==k.empty?0:k.charges(p).getTotalCharges())+" charges remaining.");}
    public static boolean option(Player p,int id,int action,boolean worn){
        Kind k=kind(id);if(k==null)return false;
        if(!allowed(p,id,worn))return true;
        if(action==2)check(p,id);
        else if(action==3)new DialogueBuilder(p).option("Tome of "+k.name+" pages",
            new DialogueOption("Add pages",q->amountMenu(q,id,worn,true)),
            new DialogueOption("Remove pages",q->amountMenu(q,id,worn,false)),DialogueOption.nevermind()).send();
        return true;
    }
    private static void amountMenu(Player p,int id,boolean worn,boolean add){
        if(!allowed(p,id,worn))return;
        new DialogueBuilder(p).option(add?"Add pages":"Remove pages",
            new DialogueOption("1",q->transfer(q,id,worn,add,1)),
            new DialogueOption("5",q->transfer(q,id,worn,add,5)),
            new DialogueOption("All",q->transfer(q,id,worn,add,Integer.MAX_VALUE)),
            new DialogueOption("X",q->{q.getPA().closeAllWindows();q.getPA().sendEnterAmount("How many pages?",(r,n)->transfer(r,id,worn,add,n));}),DialogueOption.nevermind()).send();
    }
    public static boolean use(Player p,int first,int second){
        Kind k=kind(first);int id=first;
        if(k==null){k=kind(second);id=second;}
        if(k==null||(first!=k.page&&second!=k.page))return false;
        transfer(p,id,false,true,Integer.MAX_VALUE);return true;
    }
    public static boolean transfer(Player p,int id,boolean worn,boolean add,int requested){
        Kind k=kind(id);if(k==null||requested<=0||!allowed(p,id,worn))return false;
        ElementalTomeCharges book=k.charges(p);p.getPA().closeAllWindows();
        if(add){
            // This engine stores charges per character; a second empty book cannot claim that balance.
            if(id==k.empty&&book.hasCharges()){p.sendMessage("Empty your charged tome before charging another one.");return false;}
            int n=Math.min(requested,Math.min(book.getFreePageSlots(),p.getItems().getInventoryCount(k.page)));
            if(n<=0){p.sendMessage(book.getFreePageSlots()==0?"Your tome cannot hold another full page.":"You have no pages to add.");return false;}
            int[][] in=id==k.empty&&!worn?new int[][]{{k.page,n},{id,1}}:new int[][]{{k.page,n}};
            int[][] out=id==k.empty&&!worn?new int[][]{{k.charged,1}}:new int[0][];
            if(!ProductionBatch.exchange(p,in,out))return false;
            book.addPages(n);if(id==k.empty&&worn)p.getItems().setEquipment(k.charged,1,Player.playerShield,true);
            p.sendMessage("You add "+n+" pages ("+(n*20)+" charges) to your tome of "+k.name+".");return true;
        }
        if(id!=k.charged)return false;
        int total=book.getTotalCharges(),n=Math.min(requested,book.getRemovablePages());
        // A partly used page is not refundable; removing all remaining full pages empties the book.
        boolean empty=requested>=book.getRemovablePages();
        if(!empty&&n==0)return false;
        int[][] in=empty&&!worn?new int[][]{{id,1}}:new int[0][];
        int[][] out=n==0?(empty&&!worn?new int[][]{{k.empty,1}}:new int[0][]):
            empty&&!worn?new int[][]{{k.page,n},{k.empty,1}}:new int[][]{{k.page,n}};
        if(!ProductionBatch.exchange(p,in,out))return false;
        book.setTotalCharges(empty?0:total-n*20);
        if(empty&&worn)p.getItems().setEquipment(k.empty,1,Player.playerShield,true);
        p.sendMessage("You remove "+n+" unused pages from your tome of "+k.name+".");return true;
    }
}
