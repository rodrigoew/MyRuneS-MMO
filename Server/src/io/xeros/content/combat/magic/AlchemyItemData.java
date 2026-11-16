package io.xeros.content.combat.magic;

import com.google.gson.Gson;
import io.xeros.Server;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Numeric 2022 item facts; separate from custom shop prices. See third-party license/reference. */
public final class AlchemyItemData {
    private AlchemyItemData() {}
    public static final class Entry {
        private int id,value,low,high,base;
        private boolean tradeable;
        public int getValue(){return value;}
        public int getBase(){return base;}
        public int coins(boolean highLevel){return highLevel?high:low;}
        public boolean allowed(){return low>=0&&high>=0;}
        public boolean isTradeable(){return tradeable;}
    }
    private static final class Data {private static final Map<Integer,Entry> ITEMS=read();}
    private static Map<Integer,Entry> read(){
        try(Reader input=Files.newBufferedReader(Path.of(Server.getDataDirectory(),"cfg/item/alchemy_2022.json"))){
            Entry[] rows=new Gson().fromJson(input,Entry[].class);Map<Integer,Entry> map=new HashMap<>();
            for(Entry e:rows){
                if(e.id<0||e.base<0||e.value<0||e.low< -1||e.high< -1||(e.low<0)!=(e.high<0)
                    ||e.allowed()&&(e.low!=(long)e.value*2/5||e.high!=(long)e.value*3/5)
                    ||map.put(e.id,e)!=null)throw new IllegalStateException("Invalid alchemy item record "+e.id);
            }
            return Collections.unmodifiableMap(map);
        }catch(Exception e){throw new IllegalStateException("Unable to load historical alchemy item facts",e);}
    }
    public static Entry forId(int id){return Data.ITEMS.get(id);}
    public static int size(){return Data.ITEMS.size();}
}
