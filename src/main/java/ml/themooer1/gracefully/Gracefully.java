package ml.themooer1.gracefully;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.server.MinecraftServer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.Date;


@Mod(modid= Gracefully.MODID, version= Gracefully.VERSION, acceptableRemoteVersions = "*")
public class Gracefully {
    public static final String MODID = "Gracefully";
    public static final String VERSION = "1.7.10-1.0";
    private static final String LOGFILE = "spotTermination.log";

    // Checks for spot interruption alerts
    AWSPoller poller = new AWSPoller();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        poller.subscribe(() -> {
            FMLLog.getLogger().info("Spot instance will be terminated.  Stopping server.");
//            try {
//                Method[] m = FMLCommonHandler.instance().getMinecraftServerInstance().getClass().getDeclaredMethods();
//                for (int i = 0; i < m.length; i++)
//                    System.out.println(m[i].toString());
//            } catch (Throwable e) {
//                System.err.println(e);
//            }
//
//            try {
//                Method[] m = Minecraft.getMinecraft().getClass().getDeclaredMethods();
//                for (int i = 0; i < m.length; i++)
//                    System.out.println(m[i].toString());
//            } catch (Throwable e) {
//                System.err.println(e);
//            }
//            FMLServerHandler.instance().getServer().initiateShutdown();
//            FMLCommonHandler.instance().getMinecraftServerInstance().stopServer();
            FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
        });

        // Register spot termination logger.
        poller.subscribe(() -> {
            try {
                PrintWriter logger = new PrintWriter(new FileWriter(LOGFILE));
                logger.printf("Spot instance termination notice at: %s", new Date().toString());
                logger.close();
            }
            catch (IOException e) {
                FMLLog.getLogger().error(String.format("Could not write to %s", LOGFILE));
            }
        });

//        FMLServerHandler.instance().haltGame("Spot instance will be shut down.", new NullPointerException());
    }

    @EventHandler
    public void startPolling (FMLServerStartedEvent event) {
        FMLLog.getLogger().info("Started monitoring.");
        poller.start();
    }
}
