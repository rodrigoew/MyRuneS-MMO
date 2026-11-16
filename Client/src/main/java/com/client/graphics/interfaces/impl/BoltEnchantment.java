package com.client.graphics.interfaces.impl;

import com.client.TextDrawingArea;
import com.client.graphics.interfaces.RSInterface;
import java.util.ArrayList;
import java.util.List;

/** Classic spell selection using the client's existing fonts, item models and rune icons. */
public final class BoltEnchantment extends RSInterface {
    public static final int ROOT=64600;
    private static final String[] GEMS={"Opal","Sapphire","Jade","Pearl","Emerald","Red topaz","Ruby","Diamond","Dragonstone","Onyx"};
    private static final int[] LEVELS={4,7,14,24,27,29,49,57,68,87};
    private static final int[] PRODUCTS={9236,9240,9237,9238,9241,9239,9242,9243,9244,9245};
    private static final int[][] COSTS={{564,1,556,2},{564,1,555,1,558,1},{564,1,557,2},{564,1,555,2},{564,1,556,3,561,1},
        {564,1,554,2},{564,1,554,5,565,1},{564,1,557,10,563,2},{564,1,557,15,566,1},{564,1,554,20,560,1}};
    private BoltEnchantment(){}
    private static RSInterface rectangle(int id,int w,int h,int colour,boolean fill,String action){
        RSInterface v=addInterface(id);v.parentID=ROOT;v.type=3;v.width=w;v.height=h;v.textColor=colour;v.aBoolean227=fill;v.atActionType=action==null?0:1;v.tooltip=action;v.mOverInterToTrigger=-1;return v;
    }
    private static void text(int id,String value,TextDrawingArea[] fonts,int font,int colour){
        addText(id,value,fonts,font,colour,true,true);interfaceCache[id].parentID=ROOT;
    }
    private static void item(int id,int item){
        RSInterface v=addInterface(id);v.parentID=ROOT;v.type=2;v.width=1;v.height=1;v.inventoryItemId=new int[]{item+1};v.inventoryAmounts=new int[]{1};
        v.spritesX=new int[20];v.spritesY=new int[20];v.isInventoryInterface=false;v.usableItemInterface=false;
    }
    public static void build(TextDrawingArea[] fonts){
        for(int i=ROOT;i<=64789;i++)if(interfaceCache[i]!=null)throw new IllegalStateException("Bolt interface ID already in use: "+i);
        RSInterface root=addInterface(ROOT);root.width=512;root.height=334;
        List<int[]> children=new ArrayList<>();
        rectangle(64601,500,324,0x4b4234,true,null);children.add(new int[]{64601,6,5});
        rectangle(64602,498,322,0x98856a,false,null);children.add(new int[]{64602,7,6});
        text(64603,"Enchant Crossbow Bolt",fonts,2,0xff981f);children.add(new int[]{64603,256,16});
        text(64604,"X",fonts,2,0xff981f);interfaceCache[64604].atActionType=3;interfaceCache[64604].tooltip="Close";interfaceCache[64604].width=20;interfaceCache[64604].height=20;children.add(new int[]{64604,482,14});
        text(64605,"Normal bolts",fonts,1,0xff981f);interfaceCache[64605].atActionType=1;interfaceCache[64605].tooltip="Choose normal bolts";interfaceCache[64605].width=95;interfaceCache[64605].height=18;children.add(new int[]{64605,65,45});
        text(64606,"Dragon bolts",fonts,1,0xff981f);interfaceCache[64606].atActionType=1;interfaceCache[64606].tooltip="Choose dragon bolts";interfaceCache[64606].width=95;interfaceCache[64606].height=18;children.add(new int[]{64606,175,45});
        text(64607,"Normal bolts - 1 cast",fonts,0,0xffe5af);children.add(new int[]{64607,362,47});
        for(int i=0;i<GEMS.length;i++){
            int id=64620+i*15,x=19+(i%5)*95,y=75+(i/5)*110;
            rectangle(id,92,105,0x302a22,true,"Enchant "+GEMS[i]+" bolts");children.add(new int[]{id,x,y});
            rectangle(id+1,92,105,0x766850,false,null);children.add(new int[]{id+1,x,y});
            text(id+2,GEMS[i],fonts,0,0xff981f);children.add(new int[]{id+2,x+46,y+5});
            text(id+3,"Level "+LEVELS[i],fonts,0,0xb4a98d);children.add(new int[]{id+3,x+46,y+19});
            item(id+4,PRODUCTS[i]);children.add(new int[]{id+4,x+30,y+34});
            int[] costs=COSTS[i];int start=x+(costs.length==4?15:2);
            for(int j=0;j<costs.length/2;j++){
                item(id+5+j*2,costs[j*2]);children.add(new int[]{id+5+j*2,start+j*29,y+65});
                text(id+6+j*2,Integer.toString(costs[j*2+1]),fonts,0,0xffffff);children.add(new int[]{id+6+j*2,start+j*29+15,y+91});
            }
        }
        String[] quantities={"1 cast","5 casts","10 casts","X casts","All"};
        for(int i=0;i<quantities.length;i++){
            int id=64780+i;text(id,quantities[i],fonts,1,0xff981f);interfaceCache[id].atActionType=1;interfaceCache[id].tooltip="Select "+quantities[i];interfaceCache[id].width=76;interfaceCache[id].height=20;children.add(new int[]{id,63+i*96,303});
        }
        root.totalChildren(children.size());for(int i=0;i<children.size();i++){int[] child=children.get(i);root.child(i,child[0],child[1],child[2]);}
    }
}
