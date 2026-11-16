/*
 * Copyright (c) 2020, dekvall <https://github.com/dekvall>
 * Copyright (c) 2020, Jordan <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.xeros.content.skills.hunter;
import io.xeros.model.collisionmap.WorldObject;
import java.util.*;
/** Numeric 2022 RuneLite search/exit tables, joined to verified native scenery.
 * Single-ended connections and route selection are local map reconstructions.
 */
public final class HerbiboarData2022 {
 private HerbiboarData2022(){}
 public enum Node {
 A(3671,3890),
 B(3728,3893),
 C(3698,3875),
 D(3709,3876),
 E(3668,3864),
 F(3681,3860),
 G(3696,3847),
 H(3714,3850),
 I(3680,3837),
 J(3713,3840),
 K(3706,3811),
 MIDDLE(3686,3870),
 LEPRECHAUN(3705,3830),
 CAMP_ENTRANCE(3704,3810),
 GHOST_MUSHROOM(3695,3800),
 DRIFTWOOD(3751,3850);
 public final int x,y;Node(int x,int y){this.x=x;this.y=y;}public boolean start(){return ordinal()>=11;}
 }
 public static final class Exit {public final int bit,value;Exit(int bit,int value){this.bit=bit;this.value=value;}}
 public static final class Edge {public final int bit,parent,shift;public final Node one,two;Edge(int bit,Node one,Node two,int parent,int shift){this.bit=bit;this.one=one;this.two=two;this.parent=parent;this.shift=shift;}public Node other(Node n){return n==one?two:n==two?one:null;}public int visibleFrom(Node from){if(other(from)==null)throw new IllegalArgumentException("Unconnected trail");return from==one?3:4;}}
 public static final class Spot {public final String name;public final Node node;public final WorldObject object;public final Exit[] exits;Spot(String name,Node node,WorldObject object,Exit[] exits){this.name=name;this.node=node;this.object=object;this.exits=exits;}}
 public static final class Start {public final Node node;public final WorldObject object;Start(Node n,WorldObject o){node=n;object=o;}}
 public static final class End {public final int code;public final Node node;public final WorldObject object;End(int code,Node n,WorldObject o){this.code=code;node=n;object=o;}}
 public static final List<Spot> SPOTS=List.of(
 new Spot("A_MUSHROOM",Node.A,new WorldObject(30534,3670,3889,0,10,0),new Exit[]{new Exit(5742,1),new Exit(5743,1)}),
 new Spot("A_PATCH",Node.A,new WorldObject(30546,3672,3890,0,10,0),new Exit[]{new Exit(5738,2)}),
 new Spot("B_SEAWEED",Node.B,new WorldObject(30537,3728,3893,0,10,2),new Exit[]{new Exit(5741,2),new Exit(5742,2),new Exit(5769,1),new Exit(5770,1)}),
 new Spot("C_MUSHROOM",Node.C,new WorldObject(30545,3697,3875,0,10,1),new Exit[]{new Exit(5737,2)}),
 new Spot("C_PATCH",Node.C,new WorldObject(30533,3699,3875,0,10,3),new Exit[]{new Exit(5740,1),new Exit(5741,1)}),
 new Spot("D_PATCH",Node.D,new WorldObject(30536,3708,3876,0,10,0),new Exit[]{new Exit(5746,1),new Exit(5768,1)}),
 new Spot("D_SEAWEED",Node.D,new WorldObject(30551,3710,3877,0,10,0),new Exit[]{new Exit(5740,2),new Exit(5770,2)}),
 new Spot("E_MUSHROOM",Node.E,new WorldObject(30539,3668,3865,0,10,0),new Exit[]{new Exit(5771,1),new Exit(5772,1)}),
 new Spot("E_PATCH",Node.E,new WorldObject(30549,3667,3862,0,10,0),new Exit[]{new Exit(5743,2)}),
 new Spot("F_MUSHROOM",Node.F,new WorldObject(30535,3681,3860,0,10,2),new Exit[]{new Exit(5744,1),new Exit(5745,1),new Exit(5771,2)}),
 new Spot("F_PATCH",Node.F,new WorldObject(30544,3681,3859,0,10,0),new Exit[]{new Exit(5739,2)}),
 new Spot("G_MUSHROOM",Node.G,new WorldObject(30541,3694,3847,0,10,3),new Exit[]{new Exit(5768,2),new Exit(5775,1)}),
 new Spot("G_PATCH",Node.G,new WorldObject(30543,3698,3847,0,10,3),new Exit[]{new Exit(5745,2)}),
 new Spot("H_SEAWEED_EAST",Node.H,new WorldObject(30542,3715,3851,0,10,3),new Exit[]{new Exit(5776,1),new Exit(5777,1)}),
 new Spot("H_SEAWEED_WEST",Node.H,new WorldObject(30550,3713,3850,0,10,0),new Exit[]{new Exit(5746,2),new Exit(5747,1)}),
 new Spot("I_MUSHROOM",Node.I,new WorldObject(30540,3680,3838,0,10,2),new Exit[]{new Exit(5773,1),new Exit(5774,1)}),
 new Spot("I_PATCH",Node.I,new WorldObject(30547,3680,3836,0,10,1),new Exit[]{new Exit(5744,2),new Exit(5772,2)}),
 new Spot("J_PATCH",Node.J,new WorldObject(31480,3713,3840,0,10,0),new Exit[]{new Exit(5776,2),new Exit(5750,1)}),
 new Spot("K_PATCH",Node.K,new WorldObject(31479,3706,3811,0,10,0),new Exit[]{new Exit(5773,2),new Exit(5748,1),new Exit(5749,1)}));
 public static final List<Edge> EDGES=List.of(
 new Edge(5737,Node.MIDDLE,Node.C,1617,0),
 new Edge(5738,Node.MIDDLE,Node.A,1617,3),
 new Edge(5739,Node.MIDDLE,Node.F,1617,6),
 new Edge(5740,Node.C,Node.D,1617,9),
 new Edge(5741,Node.C,Node.B,1617,12),
 new Edge(5742,Node.A,Node.B,1617,15),
 new Edge(5743,Node.A,Node.E,1617,18),
 new Edge(5744,Node.F,Node.I,1617,21),
 new Edge(5745,Node.F,Node.G,1617,24),
 new Edge(5746,Node.D,Node.H,1617,27),
 new Edge(5747,Node.H,Node.G,1619,0),
 new Edge(5748,Node.K,Node.GHOST_MUSHROOM,1619,3),
 new Edge(5749,Node.K,Node.J,1619,6),
 new Edge(5750,Node.J,Node.D,1619,9),
 new Edge(5768,Node.D,Node.G,1618,0),
 new Edge(5769,Node.B,Node.C,1618,3),
 new Edge(5770,Node.B,Node.D,1618,6),
 new Edge(5771,Node.E,Node.F,1618,9),
 new Edge(5772,Node.E,Node.I,1618,12),
 new Edge(5773,Node.I,Node.K,1618,15),
 new Edge(5774,Node.I,Node.GHOST_MUSHROOM,1618,18),
 new Edge(5775,Node.G,Node.LEPRECHAUN,1618,21),
 new Edge(5776,Node.H,Node.J,1618,24),
 new Edge(5777,Node.H,Node.DRIFTWOOD,1618,27),
 new Edge(-1,Node.CAMP_ENTRANCE,Node.K,-1,0));
 public static final List<Start> STARTS=List.of(
 new Start(Node.MIDDLE,new WorldObject(30519,3686,3870,0,10,0)),
 new Start(Node.GHOST_MUSHROOM,new WorldObject(30520,3695,3800,0,10,1)),
 new Start(Node.CAMP_ENTRANCE,new WorldObject(30521,3704,3810,0,10,1)),
 new Start(Node.LEPRECHAUN,new WorldObject(30522,3705,3830,0,10,1)),
 new Start(Node.DRIFTWOOD,new WorldObject(30523,3751,3850,0,10,1)));
 public static final List<End> ENDS=List.of(
 new End(1,Node.GHOST_MUSHROOM,new WorldObject(30532,3693,3798,0,22,2)),
 new End(2,Node.CAMP_ENTRANCE,new WorldObject(30532,3702,3808,0,22,2)),
 new End(3,Node.LEPRECHAUN,new WorldObject(30532,3703,3826,0,22,2)),
 new End(4,Node.D,new WorldObject(30532,3710,3881,0,22,2)),
 new End(5,Node.C,new WorldObject(30532,3700,3877,0,22,1)),
 new End(6,Node.J,new WorldObject(30532,3715,3840,0,10,2)),
 new End(7,Node.DRIFTWOOD,new WorldObject(30532,3751,3849,0,10,1)),
 new End(8,Node.MIDDLE,new WorldObject(30532,3685,3869,0,22,1)),
 new End(9,Node.F,new WorldObject(30532,3681,3863,0,10,3)));
 public static Edge edge(int bit){return EDGES.stream().filter(e->e.bit==bit).findFirst().orElseThrow();}
 public static List<Spot> at(Node n){List<Spot> out=new ArrayList<>();for(Spot s:SPOTS)if(s.node==n)out.add(s);return out;}
 public static End end(Node n){return ENDS.stream().filter(e->e.node==n).findFirst().orElse(null);}
 /** Shortest chain of actual footprint fields; no fictitious straight lines through scenery. */
 public static List<Edge> path(Node from,Node to){Map<Node,Edge> came=new EnumMap<>(Node.class);ArrayDeque<Node> queue=new ArrayDeque<>();queue.add(from);came.put(from,null);while(!queue.isEmpty()){Node at=queue.removeFirst();if(at==to)break;for(Edge e:EDGES){Node next=e.other(at);if(next!=null&&!came.containsKey(next)){came.put(next,e);queue.addLast(next);}}}if(!came.containsKey(to))throw new IllegalArgumentException("Disconnected native trail");List<Edge> result=new ArrayList<>();for(Node n=to;n!=from;){Edge e=came.get(n);result.add(e);n=e.other(n);}Collections.reverse(result);return result;}
 public static String guaranteed(Node to,Node from){
 if(to==Node.A&&from==Node.MIDDLE)return "A_MUSHROOM";
 if(to==Node.C&&from==Node.MIDDLE)return "C_PATCH";
 if(to==Node.D&&(from==Node.MIDDLE||from==Node.C))return "D_PATCH";
 if(to==Node.E&&from==Node.A)return "E_MUSHROOM";
 if(to==Node.F&&from==Node.G)return "F_MUSHROOM";
 if(to==Node.G&&from==Node.F)return "G_MUSHROOM";
 if(to==Node.H&&from==Node.D)return "H_SEAWEED_EAST";
 if(to==Node.H&&from==Node.DRIFTWOOD)return "H_SEAWEED_WEST";
 if(to==Node.I&&(from==Node.LEPRECHAUN||from==Node.GHOST_MUSHROOM||from==Node.CAMP_ENTRANCE))return "I_PATCH";
 if(to==Node.I&&from==Node.E)return "I_MUSHROOM";
 return null;
 }
}
