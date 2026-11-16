package io.xeros.content.skills.hunter;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Bundled-map scenery and native footprint directions; local route reconstruction. */
public final class DesertDevilTracking extends TrackedCreature {
    private static final Random RANDOM=new Random();
    private static final Place[] PLACES={
        new Place(3393,3122,false,true),
        new Place(3400,3114,false,true),
        new Place(3405,3124,false,true),
        new Place(3407,3121,false,true),
        new Place(3411,3108,false,true),
        new Place(3414,3121,false,true),
        new Place(3396,3106,true,false),
        new Place(3402,3131,true,false),
        new Place(3398,3121,false,false),
        new Place(3407,3113,false,false)
    };
    private static final Edge[] EDGES={
        new Edge(3016,7,0,3393,3129,922,26),
        new Edge(3017,7,2,3403,3126,922,29),
        new Edge(3018,5,7,3408,3128,923,0),
        new Edge(3019,0,8,3396,3121,923,3),
        new Edge(3020,8,2,3401,3123,923,6),
        new Edge(3021,2,3,3405,3122,923,9),
        new Edge(3022,5,3,3409,3121,923,12),
        new Edge(3023,0,6,3392,3112,923,15),
        new Edge(3024,1,8,3402,3116,923,18),
        new Edge(3025,3,9,3406,3118,923,21),
        new Edge(3026,4,5,3411,3114,923,24),
        new Edge(3027,6,1,3399,3111,923,27),
        new Edge(3030,9,1,3403,3110,924,0),
        new Edge(3031,9,4,3406,3111,924,3),
        new Edge(3032,6,4,3402,3101,924,6)
    };
    private static final Map<Integer,List<List<Step>>> ROUTES=routes(PLACES,EDGES,4);
    private static final Profile PROFILE=new Profile("desert devil",13,66,10123,5257,950,0,
        new int[]{3380,3420,3090,3145},PLACES,EDGES,new int[]{19552,19553},
        java.util.stream.IntStream.rangeClosed(19388,19402).toArray(),new int[]{19430},923,30,ROUTES);
    public DesertDevilTracking(Player player){super(player,PROFILE,RANDOM);}
}
