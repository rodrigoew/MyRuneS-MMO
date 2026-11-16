package com.client;
import java.nio.file.*;
public final class Final67CleanStartup {
 public static void main(String[]args)throws Exception{Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(1);});Client.main(new String[0]);for(int i=0;i<600&&(Client.instance==null||!Client.instance.soloStartupComplete);i++)Thread.sleep(100);if(Client.instance==null||!Client.instance.soloStartupComplete)throw new AssertionError("Clean offline client startup");Thread.sleep(1200);com.client.definitions.NpcDefinition nex=com.client.definitions.NpcDefinition.forID(11278);if(!nex.name.toLowerCase().contains("nex"))throw new AssertionError("Nex cache model");if(Client.loggedIn)throw new AssertionError("Clean package contains remembered login");System.out.println("FINAL67_CLEAN_PACKAGE_OFFLINE_CLIENT_STARTUP_LOGIN_SCREEN_CACHE_NEX_ASSET_NO_REMEMBERED_ACCOUNT_PASS");System.exit(0);}
}
