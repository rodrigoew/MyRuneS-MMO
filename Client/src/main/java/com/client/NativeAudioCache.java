package com.client;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.*;

/** The original packed effect bank; synthesized buffers never escape the cache lock. */
public final class NativeAudioCache {
 private static boolean loaded;
 private static final Map<Integer,byte[]> waves=new LinkedHashMap<Integer,byte[]>(64,.75f,true){
  protected boolean removeEldestEntry(Map.Entry<Integer,byte[]> entry){return size()>64;}
 };
 public static synchronized void load(byte[] packed){
  loaded=false;waves.clear();
  if(packed==null)return;
  try{
   Sounds.unpack(new Stream(packed));
   try(InputStream in=NativeAudioCache.class.getResourceAsStream("/audio/skill-effects-2022.dat")){
    if(in!=null){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[1024];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);Sounds.unpack(new Stream(bytes.toByteArray()));}
   }
   loaded=true;
  }catch(IOException e){System.err.println("Unable to read the additional historical sound effects.");}
  catch(RuntimeException e){System.err.println("Unable to read the cached sound bank: "+e.getClass().getSimpleName());}
 }
 public static synchronized byte[] wave(int id){
  if(!loaded||id<0||id>=5000)return null;
  byte[] cached=waves.get(id);
  if(cached==null){
   try{
    Stream wave=Sounds.method241(1,id);
    if(wave==null||wave.currentOffset<=44||wave.currentOffset>wave.buffer.length)return null;
    cached=Arrays.copyOf(wave.buffer,wave.currentOffset);waves.put(id,cached);
   }catch(RuntimeException ignored){return null;}
  }
  return cached.clone();
 }
 private NativeAudioCache(){}
}
