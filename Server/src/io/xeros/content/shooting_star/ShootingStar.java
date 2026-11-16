package io.xeros.content.shooting_star;
import io.xeros.util.Misc;
/** Compatibility facade for existing event and quest tabs. */
public final class ShootingStar {
 public static boolean spawned;public static int lastX=-1,lastY=-1;
 private ShootingStar(){}
 public static void spawnShootingStar(){ShootingStarsWorld.INSTANCE.initialize();}
 public static void spawnStar(){ShootingStarsWorld.INSTANCE.natural(Misc::trueRand);}
 public static void destroyLocation(){ShootingStarsWorld.INSTANCE.clear();}
 public static int generateTime(){return 12000;}
 public static ShootingStarLocation getCurrentLocation(){StarLocations.Site site=ShootingStarsWorld.INSTANCE.site();return site==null?null:new ShootingStarLocation(site.x,site.y,site.name,"near");}
 public static void setCurrentLocation(ShootingStarLocation location){if(location==null)destroyLocation();else throw new IllegalArgumentException("Use a validated StarLocations site");}
 public static String getName(){return "Shooting Star";}
}
