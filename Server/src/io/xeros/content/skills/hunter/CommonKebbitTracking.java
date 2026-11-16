package io.xeros.content.skills.hunter;
import io.xeros.model.entity.player.Player;
import java.util.*;
/** Numeric scenery/field rotations from the bundled map; route selection is local. */
public final class CommonKebbitTracking extends TrackedCreature {
    private static final Random RANDOM=new Random();
    private static final Place[] PLACES={
        new Place(2322,3570,false),
        new Place(2323,3563,false),
        new Place(2327,3573,false),
        new Place(2332,3568,false),
        new Place(2332,3578,false),
        new Place(2336,3571,false),
        new Place(2337,3565,false),
        new Place(2343,3568,false),
        new Place(2322,3576,true),
        new Place(2331,3562,true),
        new Place(2341,3577,true)
    };
    private static final Edge[] EDGES={
        new Edge(2995,1,9,2330,3562,921,0),
        new Edge(2996,3,9,2333,3565,921,3),
        new Edge(2997,6,9,2335,3561,921,6),
        new Edge(2998,6,7,2343,3566,921,9),
        new Edge(2999,0,1,2321,3565,921,12),
        new Edge(3000,1,3,2325,3565,921,15),
        new Edge(3001,5,6,2338,3570,921,18),
        new Edge(3002,5,7,2338,3570,921,21),
        new Edge(3003,7,10,2340,3574,921,24),
        new Edge(3004,0,8,2320,3573,921,27),
        new Edge(3007,2,0,2324,3571,922,0),
        new Edge(3008,3,2,2331,3572,922,3),
        new Edge(3009,2,8,2326,3575,922,6),
        new Edge(3010,4,8,2326,3575,922,9),
        new Edge(3011,5,4,2335,3574,922,12),
        new Edge(3012,5,10,2340,3574,922,15),
        new Edge(3013,10,4,2336,3579,922,18)
    };
    private static final Map<Integer,List<List<Step>>> ROUTES=routes(PLACES,EDGES,4);
    private static final Profile PROFILE=new Profile("common kebbit",3,36,10121,5259,947,0,
        new int[]{2315,2350,3550,3588},PLACES,EDGES,new int[]{19492,19493,19494},
        java.util.stream.IntStream.rangeClosed(19339,19355).toArray(),new int[]{19429},921,30,ROUTES);
    public CommonKebbitTracking(Player player){super(player,PROFILE,RANDOM);}
}
