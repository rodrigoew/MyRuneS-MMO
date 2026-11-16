package io.xeros.content.skills.hunter;

import io.xeros.model.entity.player.Player;
import java.util.*;

/** Razor-backed scenery and routes, using the shared noose action engine. */
public final class RazorTracking extends TrackedCreature {
    private static final Random RANDOM=new Random();
    private static final Place[] PLACES = {
        new Place(2349,3604,false), new Place(2354,3609,false),
        new Place(2355,3601,false), new Place(2360,3602,false),
        new Place(2357,3607,false), new Place(2351,3619,false),
        new Place(2358,3620,false), new Place(2362,3615,false),
        new Place(2353,3595,true), new Place(2360,3611,true),
        new Place(2357,3624,true), new Place(2344,3612,false,false)
    };
    // Endpoints and inspection scenery are local reconstructions, rather than a
    // certification of the original server's unpublished route generator.
    private static final Edge[] EDGES = {
        new Edge(2974,8,3,2358,3599), new Edge(2975,8,2,2355,3598),
        new Edge(2976,8,0,2347,3603), new Edge(2977,3,2,2358,3599),
        new Edge(2978,2,0,2352,3603), new Edge(2979,3,4,2358,3603),
        new Edge(2980,3,9,2363,3602), new Edge(2981,4,9,2358,3607),
        new Edge(2982,4,1,2355,3608), new Edge(2983,1,0,2351,3608),
        new Edge(2985,10,7,2360,3618), new Edge(2986,5,10,2356,3620),
        new Edge(2987,6,10,2356,3620), new Edge(2988,11,0,2347,3607),
        new Edge(2989,1,11,2348,3612), new Edge(2990,5,11,2349,3617),
        new Edge(2991,6,5,2356,3618), new Edge(2992,7,9,2362,3614),
        new Edge(2993,6,7,2360,3618)
    };
    private static final Map<Integer,List<List<Step>>> ROUTES = new HashMap<>();
    static {
        for(int start=8;start<=10;start++) {
            List<List<Step>> choices=new ArrayList<>();
            enumerate(start,new HashSet<>(Collections.singleton(start)),new ArrayList<>(),choices);
            if(choices.isEmpty())throw new IllegalStateException("Missing kebbit routes");
            ROUTES.put(start,Collections.unmodifiableList(choices));
        }
    }
    private static void enumerate(int at,Set<Integer> visited,List<Step> path,List<List<Step>> result) {
        if(path.size()==4) {
            if(at<8)result.add(Collections.unmodifiableList(new ArrayList<>(path)));
            return;
        }
        for(Edge edge:EDGES) {
            int next=edge.from==at?edge.to:edge.to==at?edge.from:-1;
            if(next<0||visited.contains(next)||PLACES[next].burrow)continue;
            // One inspection must never advance two separate sections of a trail.
            if(path.stream().skip(1).anyMatch(s->s.edge.plantX==edge.plantX&&s.edge.plantY==edge.plantY))continue;
            visited.add(next);path.add(new Step(edge,next));enumerate(next,visited,path,result);
            path.remove(path.size()-1);visited.remove(next);
        }
    }
    private static final Profile PROFILE=new Profile("razor-backed kebbit",49,348.5,10107,5349,951,0,
        new int[]{2338,2377,3583,3635},PLACES,EDGES,new int[]{19438,19439,19440},
        java.util.stream.IntStream.rangeClosed(19356,19380).toArray(),new int[]{19428},919,30,ROUTES);
    public RazorTracking(Player player){super(player,PROFILE,RANDOM);}
}
