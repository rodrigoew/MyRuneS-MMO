package io.xeros.content.skills.herblore;
import io.xeros.content.skills.ProductionBatch;
import io.xeros.model.entity.player.Player;
public final class HerbedTar {
    private HerbedTar(){}
    public static boolean mix(Player p,int first,int second){
        int herb=first==1939?second:second==1939?first:-1;
        int product,level;double xp;
        switch(herb){case 249:product=10142;level=19;xp=30;break;case 251:product=10143;level=31;xp=42.5;break;
            case 253:product=10144;level=39;xp=55;break;case 255:product=10145;level=44;xp=72.5;break;default:return false;}
        ProductionBatch.start(p,15,level,xp,28,5249,3,new int[][]{{1939,15},{herb,1},{233,0}},new int[][]{{product,15}},null);return true;
    }
}
