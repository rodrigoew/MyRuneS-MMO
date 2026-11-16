package io.xeros.content.skills.hunter;
import io.xeros.model.entity.player.Player;
import java.util.*;

/** Snow tracks use real tunnel exits; moving between inspections still uses normal walking. */
public final class PolarKebbitTracking extends TrackedCreature {
    private static final Random RANDOM=new Random();
    private static final Place[] PLACES={
        new Place(2708,3819,false,true),new Place(2712,3831,false,true),
        new Place(2716,3827,false,true),new Place(2718,3820,false,true),
        new Place(2711,3830,true,false),new Place(2717,3819,true,false),
        new Place(2711,3819,false,false),new Place(2714,3821,false,false),
        new Place(2715,3820,false,false),new Place(2718,3829,false,false),
        new Place(2718,3832,false,false),new Place(2721,3827,false,false)
    };
    private static final Edge[] EDGES={
        new Edge(3053,0,5,2712,3815,926,0),new Edge(3054,0,6,2712,3815,926,3),
        new Edge(3055,5,8,2712,3815,926,6),new Edge(3056,3,11,2713,3827,926,9),
        new Edge(3057,4,0,2708,3825,926,12),new Edge(3058,2,7,2713,3827,926,15),
        new Edge(3059,9,2,2713,3827,926,18),new Edge(3060,2,4,2713,3827,926,21),
        new Edge(3061,1,10,2713,3827,926,24)
    };
    private static final Map<Integer,List<List<Step>>> ROUTES=polarRoutes();
    private static final Profile PROFILE=new Profile("polar kebbit",1,30,10117,5256,949,1,
        new int[]{2702,2727,3812,3837},PLACES,EDGES,new int[]{19640,19641},
        new int[]{19418,19419,19420,19421,19422,19423,19424,19425,19426},new int[]{19435},926,30,ROUTES);

    private static Edge edge(int bit){for(Edge edge:EDGES)if(edge.bit==bit)return edge;throw new IllegalArgumentException("Missing polar track "+bit);}
    private static Map<Integer,List<List<Step>>> polarRoutes(){
        Map<Integer,List<List<Step>>> result=new LinkedHashMap<>();
        int[][][] initial={{{3057,0},{3060,2},{3061,10}},{{3053,0},{3055,8},{3056,11}}};
        for(int origin=0;origin<2;origin++){
            List<List<Step>> choices=new ArrayList<>();
            for(int[] first:initial[origin]){
                List<Step> path=new ArrayList<>();path.add(new Step(edge(first[0]),first[1]));
                extend(first[1],new HashSet<>(Collections.singleton(first[0])),path,choices);
            }
            result.put(origin+4,Collections.unmodifiableList(choices));
        }
        return Collections.unmodifiableMap(result);
    }
    private static int alias(int at){return at==4?1:at==1?4:at==5?3:at==3?5:-1;}
    private static boolean tunnel(int at){return at>=6;}
    /** Local reconstruction of nearby burrow complexes, not an official route generator. */
    private static void extend(int at,Set<Integer> used,List<Step> path,List<List<Step>> result){
        if(path.size()>=3&&PLACES[at].target){result.add(Collections.unmodifiableList(new ArrayList<>(path)));return;}
        Map<Integer,Integer> sources=new LinkedHashMap<>();sources.put(at,-1);
        int alias=alias(at);if(alias>=0)sources.put(alias,PLACES[at].burrow?at:alias);
        if(tunnel(at)||PLACES[at].burrow){
            for(int candidate=4;candidate<PLACES.length;candidate++){
                int dx=PLACES[candidate].x-PLACES[at].x,dy=PLACES[candidate].y-PLACES[at].y;
                if(dx*dx+dy*dy>36)continue;
                sources.put(candidate,candidate);
                int other=alias(candidate);if(other>=0)sources.put(other,candidate);
            }
        }
        Set<String> unique=new HashSet<>();
        for(Map.Entry<Integer,Integer> source:sources.entrySet())for(Edge edge:EDGES){
            int next=edge.from==source.getKey()?edge.to:edge.to==source.getKey()?edge.from:-1;
            if(next<0||used.contains(edge.bit))continue;
            int inspect=source.getValue();
            int x=inspect>=0?PLACES[inspect].x:edge.plantX,y=inspect>=0?PLACES[inspect].y:edge.plantY;
            if(!unique.add(edge.bit+":"+next+":"+x+":"+y))continue;
            used.add(edge.bit);path.add(new Step(edge,next,x,y));extend(next,used,path,result);
            path.remove(path.size()-1);used.remove(edge.bit);
        }
    }
    public PolarKebbitTracking(Player player){super(player,PROFILE,RANDOM);}
}
