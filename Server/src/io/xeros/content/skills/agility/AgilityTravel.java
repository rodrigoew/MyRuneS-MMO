package io.xeros.content.skills.agility;

import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueOption;
import io.xeros.content.teleportation.inter.DangerousWarning;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.Position;

/** Solo access to the supported courses, including the level-one training route. */
public final class AgilityTravel {
    private AgilityTravel() {}
    private static DialogueOption course(String name,int level,int x,int y,boolean wilderness) {
        return new DialogueOption(name+" (nível "+level+")",p -> travel(p,level,x,y,wilderness));
    }
    public static boolean travel(Player p,int level,int x,int y,boolean wilderness) {
        if (p.isDead || p.isDisconnected() || p.getMovementState().isLocked() || p.isForceMovementActive()) return false;
        p.getPA().removeAllWindows();
        if (p.playerLevel[Player.playerAgility]<level) {
            p.sendMessage("Você precisa de nível "+level+" em Agility para esse percurso.");
            return false;
        }
        if (wilderness) DangerousWarning.handleTeleport(p,new Position(x,y,0),true);
        else p.getPA().startTeleport(x,y,0,"modern",false);
        return true;
    }
    public static void open(Player p,int page) {
        if (p.isDead || p.isDisconnected() || p.getMovementState().isLocked()) return;
        p.getPA().removeAllWindows();
        DialogueOption next=new DialogueOption("Próximos percursos",q -> open(q,page+1));
        DialogueBuilder menu=new DialogueBuilder(p);
        if(page==0) menu.option("Percursos de Agility — 1/3",
            course("Gnome Stronghold",1,2474,3438,false),course("Draynor",10,3104,3279,false),
            course("Al Kharid",20,3272,3196,false),course("Varrock",30,3222,3414,false),next);
        else if(page==1) menu.option("Percursos de Agility — 2/3",
            course("Barbarian",35,2551,3555,false),course("Canifis",40,3505,3487,false),
            course("Falador",50,3036,3340,false),course("Seers' Village",60,2729,3487,false),next);
        else menu.option("Percursos de Agility — 3/3",
            course("Pollnivneach",70,3351,2960,false),course("Rellekka",80,2625,3676,false),
            course("Ardougne",90,2672,3296,false),course("Wilderness — área perigosa",52,3003,3934,true),
            new DialogueOption("Voltar ao início",q -> open(q,0)));
        menu.send();
    }
}
