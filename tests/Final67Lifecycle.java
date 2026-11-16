package solo;
import java.nio.file.*;import java.util.concurrent.*;import java.net.*;
public final class Final67Lifecycle {
 public static void main(String[]a)throws Exception{Thread.setDefaultUncaughtExceptionHandler((thread,error)->{error.printStackTrace();System.exit(1);});
  Path real=Path.of(a[0]),root=Path.of(a[1]),control=Path.of(a[2]),classes=Path.of(a[3]),user=Path.of(a[4]);Files.createDirectories(root.resolve("logs"));Files.createDirectories(control);
  Launcher l=new Launcher(root);javax.swing.SwingUtilities.invokeAndWait(()->{try{l.ui();}catch(Exception e){throw new RuntimeException(e);}});l.busy=true;l.buttons();
  String javaExe=real.resolve("runtime/jre/bin/java.exe").toString();
  l.server=new ProcessBuilder(javaExe,"-Xmx1536m","-Dsolo.xp=1","-cp",(real.resolve("runtime/server/lib")+java.io.File.separator+"*")+java.io.File.pathSeparator+classes,"io.xeros.solo.Final67Server").directory(root.resolve("runtime/server").toFile()).redirectErrorStream(true).redirectOutput(root.resolve("logs/server-"+control.getFileName()+".log").toFile()).start();
  boolean ready=false;for(int i=0;i<180;i++){if(!l.server.isAlive())break;try(Socket s=new Socket()){s.connect(new InetSocketAddress("127.0.0.1",43597),150);ready=true;break;}catch(Exception e){Thread.sleep(500);}}if(!ready)throw new AssertionError("Fixture server did not start");
  l.client=new ProcessBuilder(javaExe,"-Xmx1536m","-Dsolo.offline=true","-Duser.home="+user,"-cp",(real.resolve("runtime/client/lib")+java.io.File.separator+"*")+java.io.File.pathSeparator+classes,"com.client.Final67Client",control.toString()).directory(real.resolve("runtime").toFile()).redirectErrorStream(true).redirectOutput(root.resolve("logs/client-"+control.getFileName()+".log").toFile()).start();
  Process client=l.client,server=l.server;l.watchClientExit(client);l.busy=false;l.buttons();Files.writeString(control.resolve("driver-ready"),"READY");int clientExit=client.waitFor();if(!server.waitFor(55,TimeUnit.SECONDS))throw new AssertionError("Automatic stop did not finish");if(server.exitValue()!=0)throw new AssertionError("Server save exit failed");Files.writeString(control.resolve("saved-and-stopped"),"SERVER_EXIT_0 CLIENT_EXIT_"+clientExit);System.out.println("FINAL67_REAL_CLIENT_EXIT_LAUNCHER_WATCH_AUTOSAVE_BACKUP_PASS");javax.swing.SwingUtilities.invokeAndWait(()->l.frame.dispose());l.worker.shutdown();System.exit(0);
 }
}
