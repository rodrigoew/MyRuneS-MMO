package com.client.graphics.interfaces.impl;
import com.client.TextDrawingArea;
import com.client.graphics.interfaces.RSInterface;
import java.util.*;
/** A compact tracker rendered with the existing classic client fonts and panels. */
public final class ImplingTracker extends RSInterface {
    public static final int ROOT=64800;
    private static final String[] NAMES={"Baby","Young","Gourmet","Earth","Essence","Eclectic","Nature","Magpie","Ninja","Crystal","Dragon","Lucky"};
    private static void text(int id,String value,TextDrawingArea[] fonts,int font,int colour,boolean center){addText(id,value,fonts,font,colour,center,true);interfaceCache[id].parentID=ROOT;}
    public static void build(TextDrawingArea[] fonts){
        for(int i=ROOT;i<=64899;i++)if(interfaceCache[i]!=null)throw new IllegalStateException("Impling tracker ID already used: "+i);
        RSInterface root=addInterface(ROOT);root.width=512;root.height=334;List<int[]> c=new ArrayList<>();
        RSInterface bg=addInterface(64801);bg.parentID=ROOT;bg.type=3;bg.width=500;bg.height=324;bg.textColor=0x4b4234;bg.aBoolean227=true;bg.mOverInterToTrigger=-1;c.add(new int[]{64801,6,5});
        RSInterface border=addInterface(64802);border.parentID=ROOT;border.type=3;border.width=498;border.height=322;border.textColor=0x98856a;border.aBoolean227=false;border.mOverInterToTrigger=-1;c.add(new int[]{64802,7,6});
        text(64803,"Impling Tracker",fonts,2,0xff981f,true);c.add(new int[]{64803,256,16});
        text(64804,"X",fonts,2,0xff981f,true);interfaceCache[64804].atActionType=3;interfaceCache[64804].tooltip="Close";interfaceCache[64804].width=20;interfaceCache[64804].height=20;c.add(new int[]{64804,482,14});
        text(64805,"Impling",fonts,1,0xffe5af,false);c.add(new int[]{64805,60,49});
        text(64806,"Surface world",fonts,1,0xffe5af,true);c.add(new int[]{64806,300,49});
        text(64807,"Puro-Puro",fonts,1,0xffe5af,true);c.add(new int[]{64807,419,49});
        for(int i=0;i<NAMES.length;i++){
            int id=64810+i*3,y=72+i*18;
            text(id,NAMES[i]+" impling",fonts,1,0xff981f,false);c.add(new int[]{id,60,y});
            text(id+1,"0",fonts,1,0xffffff,true);c.add(new int[]{id+1,300,y});
            text(id+2,i==9?"-":"0",fonts,1,0xffffff,true);c.add(new int[]{id+2,419,y});
        }
        text(64850,"Catches are saved automatically.",fonts,0,0xb4a98d,true);c.add(new int[]{64850,256,309});
        root.totalChildren(c.size());for(int i=0;i<c.size();i++){int[] a=c.get(i);root.child(i,a[0],a[1],a[2]);}
    }
}
