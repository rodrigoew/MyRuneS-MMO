package io.xeros.content.skills.hunter;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Bundled-map scenery and native footprint directions; local route reconstruction. */
public final class FeldipWeaselTracking extends TrackedCreature {
    private static final Random RANDOM=new Random();
    private static final Place[] PLACES={
        new Place(2525,2882,false,true),
        new Place(2531,2890,false,true),
        new Place(2533,2885,false,true),
        new Place(2540,2886,false,true),
        new Place(2542,2881,false,true),
        new Place(2553,2888,false,true),
        new Place(2525,2889,true,false),
        new Place(2554,2882,true,false),
        new Place(2533,2880,false,false),
        new Place(2541,2890,false,false)
    };
    private static final Edge[] EDGES={
        new Edge(3036,6,0,2524,2886,924,14),
        new Edge(3037,0,6,2524,2886,924,17),
        new Edge(3038,1,6,2527,2890,924,20),
        new Edge(3039,2,1,2530,2887,924,23),
        new Edge(3040,0,8,2531,2881,924,26),
        new Edge(3041,2,8,2533,2882,924,29),
        new Edge(3043,9,1,2533,2889,925,0),
        new Edge(3044,3,2,2534,2886,925,3),
        new Edge(3045,8,4,2537,2880,925,6),
        new Edge(3046,3,4,2541,2883,925,9),
        new Edge(3047,9,3,2540,2887,925,12),
        new Edge(3048,5,9,2543,2890,925,15),
        new Edge(3049,4,7,2551,2881,925,18),
        new Edge(3050,7,5,2552,2885,925,21),
        new Edge(3051,7,5,2555,2881,925,24)
    };
    private static final Map<Integer,List<List<Step>>> ROUTES=routes(PLACES,EDGES,4);
    private static final Profile PROFILE=new Profile("feldip weasel",7,48,10119,5258,948,0,
        new int[]{2510,2570,2870,2900},PLACES,EDGES,new int[]{19593,19594},
        java.util.stream.IntStream.rangeClosed(19403,19417).toArray(),new int[]{19427},924,12,ROUTES);
    public FeldipWeaselTracking(Player player){super(player,PROFILE,RANDOM);}
}
