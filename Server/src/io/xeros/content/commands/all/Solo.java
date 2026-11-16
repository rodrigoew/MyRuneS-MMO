package io.xeros.content.commands.all;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.Position;
public class Solo extends Command {
    public void execute(Player p,String name,String input) {
        if(!Boolean.getBoolean("solo.mode")) return;
        String request=input.trim().toLowerCase();
        if(request.equals("star")||request.startsWith("star ")){
            if(p.getInstance()!=null||p.isDead){p.sendMessage("Use ::solo home first.");return;}
            int size=2;try{if(request.length()>4)size=Integer.parseInt(request.substring(5).trim());}catch(NumberFormatException e){p.sendMessage("Use ::solo star [1..9].");return;}
            if(size<1||size>9){p.sendMessage("Use ::solo star [1..9].");return;}
            p.interruptActions();p.getPA().stopSkilling();p.getPA().removeAllWindows();
            io.xeros.content.shooting_star.StarLocations.Site site=io.xeros.content.shooting_star.StarLocations.find("MINING_GUILD");
            io.xeros.content.shooting_star.ShootingStarsWorld.INSTANCE.spawn(site,size);
            p.moveTo(new Position(site.x-1,site.y,site.height));return;
        }
        switch(request) {
            case "sire": io.xeros.content.bosses.SoloBossEncounters.enter(p,true);break;
            case "kq": io.xeros.content.bosses.SoloBossEncounters.enter(p,false);break;
            case "hydra": io.xeros.content.bosses.hydra.AlchemicalHydra.enterSolo(p);break;
            case "guardians": if(io.xeros.content.bosses.SoloBossEncounters.canEnter(p))new io.xeros.content.bosses.grotesqueguardians.GrotesqueInstance().enter(p);break;
            case "nightmare": io.xeros.content.bosses.nightmare.NightmareInstance.enterSolo(p);break;
            case "cox": io.xeros.content.minigames.raids.SoloCox.enter(p);break;
            case "inferno": io.xeros.content.minigames.inferno.Inferno.startInferno(p,0);break;
            case "pest": io.xeros.content.minigames.pest_control.SoloPest.enter(p);break;
            case "pestrewards": if(p.getInstance()==null&&p.getUtilityMagic().canInteract())p.getPestControlRewards().showInterface();break;
            case "magearena": p.getMageArena().start();break;
            case "tob": io.xeros.content.minigames.tob.instance.TobInstance.enterSolo(p); break;
            case "toa": io.xeros.content.minigames.toa.SoloToa.enter(p); break;
            case "nex":
                if(p.getInstance()!=null) {p.sendMessage("Use ::solo home before entering another instance.");return;}
                io.xeros.content.bosses.nex.Nex.enterSolo(p); break;
            case "birdhouses":
                if(p.getInstance()!=null){p.sendMessage("Use ::solo home first.");return;}
                p.moveTo(new Position(3676,3882,0));p.getBirdhouses().refresh(true);break;
            case "home":
                if(p.isDead||p.getHealth().getCurrentHealth()<=0)return;
                p.getFightCave().abandon();
                io.xeros.content.bosses.nex.NexCombat.clear(p);
                if(p.getInstance()!=null) p.getInstance().remove(p);
                p.moveTo(new Position(3101,3509,0)); break;
            case "heal":
                p.getHealth().reset(); p.getHealth().removeAllStatuses();
                p.playerLevel[5]=p.getPA().getLevelForXP(p.playerXP[5]);
                p.getPA().refreshSkill(5); break;
            default: p.sendMessage("Novos bosses: ::solo hydra | ::solo guardians | ::solo nightmare | ::solo sire | ::solo kq. Cargas: ::charges | ::charge shadow | ::charge scythe. Solo: ::solo cox | ::solo inferno | ::solo pest | ::solo pestrewards | ::solo toa | ::solo tob | ::solo magearena | ::solo star [1..9] | ::solo nex | ::solo home | ::solo heal | ::solo birdhouses. Items: ::item ID quantity. Levels: ::setlevel ID level. XP: ::addxp SKILL quantity | ::addxp list. Training rate configured in launcher.");
        }
    }
}
