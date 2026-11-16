package com.client.sound;

import com.client.NativeAudioCache;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import javax.sound.midi.*;
import javax.sound.sampled.*;

/** Bounded playback from local game assets, with independent music/effect volumes. */
public final class Sound {
 public interface Output {
  void effect(byte[] wave,DoubleSupplier gain,BooleanSupplier cancelled)throws Exception;
  void music(byte[] midi,DoubleSupplier gain,BooleanSupplier cancelled)throws Exception;
 }
 private static final Sound SINGLETON=new Sound();
 private final AtomicLong effectsGeneration=new AtomicLong(),musicGeneration=new AtomicLong();
 private final ThreadPoolExecutor effects=new ThreadPoolExecutor(0,4,15,TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),Sound::thread,new ThreadPoolExecutor.DiscardPolicy());
 private final ThreadPoolExecutor music=new ThreadPoolExecutor(0,1,15,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1),Sound::thread,new ThreadPoolExecutor.DiscardOldestPolicy());
 private volatile Output output=new JavaOutput();
 private volatile boolean deviceErrorReported;
 private static Thread thread(Runnable task){Thread thread=new Thread(task,"Game audio");thread.setDaemon(true);return thread;}
 public static Sound getSound(){return SINGLETON;}
 private Sound(){}
 public float calculateVolume(SoundType type,double distance){
  if(type==null||!Double.isFinite(distance))return 0;
  double spatial=type==SoundType.AREA_SOUND?Math.max(0,Math.min(1,(12-Math.max(0,distance))/12)):1;
  double volume=type.getVolume();return Double.isFinite(volume)?(float)(Math.max(0,Math.min(10,volume))/10*spatial):0;
 }
 public void playSound(int id,SoundType type,double distance){
  if(calculateVolume(type,distance)<=0)return;
  final boolean isMusic=type==SoundType.MUSIC;
  final AtomicLong generation=isMusic?musicGeneration:effectsGeneration;
  final long token=isMusic?generation.incrementAndGet():generation.get();
  (isMusic?music:effects).execute(()->{
   if(token!=generation.get())return;
   try{
    byte[] data=isMusic?jingle(id):NativeAudioCache.wave(id);if(data==null)return;
    DoubleSupplier gain=()->calculateVolume(type,distance);BooleanSupplier cancelled=()->token!=generation.get();
    if(isMusic)output.music(data,gain,cancelled);else output.effect(data,gain,cancelled);
   }catch(InterruptedException e){Thread.currentThread().interrupt();}
   catch(Exception e){if(!deviceErrorReported){deviceErrorReported=true;System.err.println("Game audio output unavailable: "+e.getClass().getSimpleName());}}
  });
 }
 public static byte[] jingle(int id)throws IOException{
  if(id!=72)return null;
  try(InputStream in=Sound.class.getResourceAsStream("/audio/poh-organ-2022.mid")){
   if(in==null)return null;ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[2048];int n;
   while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toByteArray();
  }
 }
 public void stopAll(){effectsGeneration.incrementAndGet();musicGeneration.incrementAndGet();music.getQueue().clear();}
 public static void applyGain(byte[] pcm,int length,double gain){
  gain=Double.isFinite(gain)?Math.max(0,Math.min(1,gain)):0;
  if(gain==1)return;if(gain==0){Arrays.fill(pcm,0,length,(byte)0);return;}
  for(int i=0;i+1<length;i+=2){short sample=(short)((pcm[i]&255)|(pcm[i+1]<<8));int value=(int)Math.round(sample*gain);pcm[i]=(byte)value;pcm[i+1]=(byte)(value>>8);}
 }
 private static final class JavaOutput implements Output {
  public void effect(byte[] wave,DoubleSupplier gain,BooleanSupplier cancelled)throws Exception{
   try(AudioInputStream source=AudioSystem.getAudioInputStream(new ByteArrayInputStream(wave))){
    AudioFormat format=new AudioFormat(source.getFormat().getSampleRate(),16,source.getFormat().getChannels(),true,false);
    try(AudioInputStream pcm=AudioSystem.getAudioInputStream(format,source)){
     SourceDataLine line=(SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,format));
     try{line.open(format,4096);line.start();byte[] buffer=new byte[2048];int n;
      while(!cancelled.getAsBoolean()&&(n=pcm.read(buffer))!=-1){double level=gain.getAsDouble();if(level<=0)break;applyGain(buffer,n,level);line.write(buffer,0,n);}
      if(!cancelled.getAsBoolean())line.drain();
     }finally{line.stop();line.flush();line.close();}
    }
   }
  }
  public void music(byte[] bytes,DoubleSupplier gain,BooleanSupplier cancelled)throws Exception{
   Sequencer sequencer=MidiSystem.getSequencer(false);Synthesizer synthesizer=MidiSystem.getSynthesizer();Receiver receiver=null;Transmitter transmitter=null;
   try{
    sequencer.open();synthesizer.open();receiver=synthesizer.getReceiver();final Receiver target=receiver;
    Receiver scaled=new Receiver(){public void send(MidiMessage message,long timestamp){
     if(message instanceof ShortMessage){ShortMessage note=(ShortMessage)message;int command=note.getCommand();if(command==ShortMessage.NOTE_ON){
      try{ShortMessage adjusted=new ShortMessage();int value=(int)Math.round(note.getData2()*Math.max(0,Math.min(1,gain.getAsDouble())));adjusted.setMessage(command,note.getChannel(),note.getData1(),value);target.send(adjusted,timestamp);return;}catch(InvalidMidiDataException ignored){}
     }}target.send(message,timestamp);
    }public void close(){}};
    transmitter=sequencer.getTransmitter();transmitter.setReceiver(scaled);sequencer.setSequence(new ByteArrayInputStream(bytes));sequencer.start();
    while(sequencer.isRunning()&&!cancelled.getAsBoolean())Thread.sleep(20);
   }finally{sequencer.stop();if(transmitter!=null)transmitter.close();if(receiver!=null)receiver.close();sequencer.close();synthesizer.close();}
  }
 }
}
