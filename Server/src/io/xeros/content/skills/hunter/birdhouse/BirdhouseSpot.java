package io.xeros.content.skills.hunter.birdhouse;
public enum BirdhouseSpot {
    MUSHROOM_MEADOW(30565,1626,3677,3882), TAR_SWAMP(30566,1627,3679,3815),
    VERDANT_NORTH(30567,1628,3768,3761), VERDANT_SOUTH(30568,1629,3763,3755);
    public final int object,config,x,y;
    BirdhouseSpot(int object,int config,int x,int y){this.object=object;this.config=config;this.x=x;this.y=y;}
    public static BirdhouseSpot find(int object,int x,int y){for(BirdhouseSpot spot:values())if(spot.object==object&&spot.x==x&&spot.y==y)return spot;return null;}
}
