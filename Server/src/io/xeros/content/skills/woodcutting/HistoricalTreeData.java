package io.xeros.content.skills.woodcutting;

import java.util.*;

/** Frozen pre-Forestry rates from 2022; native tree/stump pairs reference OpenRune ISC. */
public final class HistoricalTreeData {
    private HistoricalTreeData(){}
    public enum Kind {
        REGULAR(1511,1,25,59,1,317647,new int[][]{{64,200},{96,300},{128,400},{144,450},{160,500},{192,600},{224,700},{240,750},{232,800}},new int[][]{{1276,1342},{1277,1343},{1278,1342},{1279,1342},{1280,1343},{1282,1347},{1283,1347},{1284,6212},{1285,1349},{1286,1351},{1289,1353},{1290,1354},{1291,23054},{1318,1355},{1319,1355},{1330,1355},{1331,1355},{1332,1355},{1365,1352},{1383,1358},{1384,1353},{2091,1342},{2092,1355},{3648,3649},{3879,3880},{3881,3880},{3882,3880},{3883,3884},{4818,4819},{4820,4821},{5904,5905},{14308,1342},{14309,1342},{36672,36673},{36674,36675},{36677,36678},{36679,36678},{40750,40751},{40752,40753},{42393,42394}}),
        OAK(1521,15,37.5,14,8,361146,new int[][]{{32,100},{48,150},{64,200},{72,225},{80,250},{96,300},{112,350},{120,375},{120,375}},new int[][]{{10820,1356},{42395,42396}}),
        WILLOW(1519,30,67.5,14,8,289286,new int[][]{{16,50},{24,75},{32,100},{36,112},{40,125},{48,150},{56,175},{60,187},{60,187}},new int[][]{{10819,9711},{10829,9471},{10831,9471},{10833,9471}}),
        TEAK(6333,35,85,15,8,264336,new int[][]{{15,46},{23,70},{31,93},{35,102},{39,117},{47,140},{55,164},{60,190},{60,200}},new int[][]{{9036,9037},{36686,36687},{40758,40759}}),
        MAPLE(1517,45,100,59,8,221918,new int[][]{{8,25},{12,37},{16,50},{18,56},{20,62},{24,75},{28,87},{30,93},{30,93}},new int[][]{{10832,9712},{36681,36682},{40754,40755}}),
        MAHOGANY(6332,50,125,14,8,220623,new int[][]{{8,25},{12,38},{16,50},{18,54},{20,63},{25,75},{29,88},{34,94},{36,97}},new int[][]{{9034,9035},{36688,36689},{40760,40761}}),
        ARCTIC(10810,54,40,14,8,145758,new int[][]{{6,30},{8,44},{11,60},{13,67},{14,74},{17,90},{20,104},{21,112},{21,112}},new int[][]{{3037,21274}}),
        YEW(1515,60,175,99,8,145013,new int[][]{{4,12},{6,19},{8,25},{9,28},{10,31},{12,37},{14,44},{15,47},{15,47}},new int[][]{{10822,9714},{10828,9714},{36683,36684},{40756,40757},{42391,42392}}),
        MAGIC(1513,75,250,199,8,72321,new int[][]{{2,6},{3,9},{4,12},{5,13},{5,15},{6,18},{7,21},{7,22},{7,22}},new int[][]{{10834,9713}}),
        REDWOOD(19669,90,380,199,11,72321,new int[][]{{2,6},{3,9},{4,12},{4,14},{5,15},{6,18},{7,21},{7,30},{8,35}},new int[][]{{29668,29669},{29670,29671}}),
        HOLLOW(3239,45,82.5,44,8,214367,new int[][]{{18,26},{28,40},{36,54},{42,57},{46,68},{59,81},{64,94},{67,101},{67,101}},new int[][]{{10821,2310},{10830,4061}}),
        ACHEY(2862,1,25,59,1,317647,new int[][]{{64,200},{96,300},{128,400},{144,450},{160,500},{192,600},{224,700},{240,750},{232,800}},new int[][]{{2023,3371}});
        public final int item,level,respawn,deplete,petBase;public final double xp;
        public final int[][] rates,nodes;
        Kind(int item,int level,double xp,int respawn,int deplete,int petBase,int[][] rates,int[][] nodes){
            this.item=item;this.level=level;this.xp=xp;this.respawn=respawn;this.deplete=deplete;this.petBase=petBase;this.rates=rates;this.nodes=nodes;
        }
    }
    public static final class Node {
        public final Kind kind;public final int id,stump;
        private Node(Kind kind,int id,int stump){this.kind=kind;this.id=id;this.stump=stump;}
    }
    private static final Map<Integer,Node> NODES=new HashMap<>();
    static {for(Kind kind:Kind.values())for(int[] pair:kind.nodes){if(NODES.put(pair[0],new Node(kind,pair[0],pair[1]))!=null)throw new ExceptionInInitializerError("Duplicate tree");}}
    public static Node node(int id){return NODES.get(id);}
    public static Collection<Node> nodes(){return Collections.unmodifiableCollection(NODES.values());}
    public static int threshold(Kind kind,int axeTier,int visible,int invisible){
        if(axeTier<0||axeTier>8)throw new IllegalArgumentException("Axe tier");
        int level=Math.max(1,Math.min(99,visible))+Math.max(0,invisible);int[] c=kind.rates[axeTier];
        return Math.max(0,Math.min(256,1+(int)Math.floor((c[0]*(99d-level)+c[1]*(level-1d))/98d+.5)));
    }
}
