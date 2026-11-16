package io.xeros.content.skills.smithing;

import io.xeros.Server;
import io.xeros.content.combat.Hitmark;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.collisionmap.*;
import io.xeros.model.cycleevent.Event;
import io.xeros.model.definitions.ItemDef;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Regicide material processing; producing lime grants no Smithing XP or quest completion. */
public final class Quicklime {
    private Quicklime(){}
    // Explicit protective gloves from the December 2022 reference; hand-slot jewellery is not sufficient.
    private static final Set<String> GLOVES=new HashSet<>(Arrays.asList(
        "adamant gloves",
        "ancient ceremonial gloves",
        "barrows gloves",
        "black gloves",
        "bloodbark gauntlets",
        "bronze gloves",
        "bunny paws",
        "chaos gauntlets",
        "clue hunter gloves",
        "cooking gauntlets",
        "crab claw",
        "crabclaw hook",
        "dragon gloves",
        "dragonstone gauntlets",
        "expert mining gloves",
        "ferocious gloves",
        "fremennik gloves",
        "ghostly gloves",
        "gilded d'hide vambraces",
        "gloves of darkness",
        "gloves of silence",
        "goldsmith gauntlets",
        "graceful gloves",
        "granite gloves",
        "grey gloves",
        "group iron bracers",
        "group iron bracers (unranked)",
        "ham gloves",
        "hardcore group iron bracers",
        "hardleather gloves",
        "holy wraps",
        "ice gloves",
        "infinity gloves",
        "iron gloves",
        "karamja gloves 1",
        "karamja gloves 2",
        "karamja gloves 3",
        "karamja gloves 4",
        "klank's gauntlets",
        "leather gloves",
        "lunar gloves",
        "mime gloves",
        "mining gloves",
        "mithril gloves",
        "moonclan gloves",
        "mourner gloves",
        "mystic gloves",
        "mystic gloves (dark)",
        "mystic gloves (dusk)",
        "mystic gloves (light)",
        "mystic gloves (or)",
        "ornate gloves",
        "penance gloves",
        "pirate's hook",
        "purple gloves",
        "ranger gloves",
        "red gloves",
        "regen bracelet",
        "rock-shell gloves",
        "rogue gloves",
        "rune gloves",
        "samurai gloves",
        "shayzien gloves (1)",
        "shayzien gloves (2)",
        "shayzien gloves (3)",
        "shayzien gloves (4)",
        "shayzien gloves (5)",
        "skeletal gloves",
        "slayer gloves",
        "smiths gloves (i)",
        "snow imp costume gloves",
        "spined gloves",
        "splitbark gauntlets",
        "spookier gloves",
        "spooky gloves",
        "steel gauntlets",
        "steel gloves",
        "superior mining gloves",
        "swampbark gauntlets",
        "teal gloves",
        "void knight gloves",
        "void knight gloves (or)",
        "warm gloves",
        "white gloves",
        "yellow gloves",
        "zaryte vambraces",
        "zombie gloves"));
    public static boolean protectedHands(Player p){
        int id=p.playerEquipment[Player.playerHands];
        return id>0&&GLOVES.contains(ItemDef.forId(id).getName().toLowerCase(Locale.ROOT));
    }
    private static boolean furnace(Player p,int id,int x,int y,int h){
        ObjectDef def=ObjectDef.getObjectDef(id);
        WorldObject o=p.getRegionProvider().get(x,y).getWorldObject(id,x,y,h).orElse(null);
        if(def==null||!"Furnace".equalsIgnoreCase(def.name)||o==null)return false;
        var size=o.getObjectSize();
        return p.heightLevel==h&&Math.max(Math.max(x-p.absX,p.absX-x-size.getX()+1),Math.max(y-p.absY,p.absY-y-size.getY()+1))<=1;
    }
    public static boolean use(Player p,int item,int id,int x,int y){
        if(item!=3211)return false;
        ObjectDef def=ObjectDef.getObjectDef(id);
        if(def==null||!"Furnace".equalsIgnoreCase(def.name))return false;
        start(p,id,x,y);return true;
    }
    public static boolean start(Player p,int id,int x,int y){
        int h=p.heightLevel;
        if(p.isDead||p.getLock().cannotInteract(p)||p.isBanking||p.isShopping||!furnace(p,id,x,y,h)||!p.getItems().playerHasItem(3211))return false;
        p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();long token=p.productionActionToken;
        int ox=p.absX,oy=p.absY;p.playerSkilling[13]=true;p.startAnimation(899);p.facePosition(x,y);
        Server.getEventHandler().submit(new Event<Player>("skilling",p,3){
            public void execute(){
                if(token==p.productionActionToken&&p.playerSkilling[13]&&!p.isDead&&!p.getLock().cannotInteract(p)
                    &&!p.isDisconnected()&&p.getSession()!=null&&p.absX==ox&&p.absY==oy&&p.heightLevel==h
                    &&p.isWalkingQueueEmpty()&&furnace(p,id,x,y,h)
                    &&ProductionBatch.exchange(p,new int[][]{{3211,1}},new int[][]{{3213,1}})){
                    if(!protectedHands(p)){p.appendDamage(null,8,Hitmark.HIT);p.sendMessage("The lime burns your unprotected hands!");}
                    else p.sendMessage("You make some quicklime.");
                }stop();
            }
            public void stop(){super.stop();if(token==p.productionActionToken){p.playerSkilling[13]=false;p.startAnimation(65535);}}
        });return true;
    }
    public static boolean grind(Player p,int a,int b){
        if(!((a==3213&&b==233)||(b==3213&&a==233)))return false;
        if(p.isDead||p.getLock().cannotInteract(p)||p.isBanking||p.isShopping)return true;
        if(!p.getItems().playerHasItem(1931)){p.sendMessage("You need an empty pot to catch the ground quicklime.");return true;}
        if(ProductionBatch.exchange(p,new int[][]{{233,0},{3213,1},{1931,1}},new int[][]{{3214,1}})){
            p.interruptActions();p.getPA().stopSkilling();p.startAnimation(364);p.sendMessage("You grind the quicklime into the pot.");
        }return true;
    }
}
