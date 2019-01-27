package ml.themooer1.gracefully;

import cpw.mods.fml.common.FMLLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


/**
 * Polls AWS metadata server every 10 seconds for a spot instance termination notice.
 */
public class AWSPoller {
    // Functions to run on impending termination.
    private static List<Runnable> subscribers = new LinkedList<>();

    // Thread that polls AWS
    private static Thread poller = new Thread(new Poller(), "GraECfully");

    private static URL AWS_INSTANCE_ACTION;
    static {
        try {
            AWS_INSTANCE_ACTION = new URL("http://169.254.169.254/latest/meta-data/spot/instance-action");
//            AWS_INSTANCE_ACTION = new URL("http://localhost:5000/latest/meta-data/spot/instance-action");
        }
        catch (MalformedURLException e) {
            FMLLog.getLogger().error("Invalid AWS instance-action URL.  Spot termination will not be handled!");
        }
    }

    public static void start() {
        poller.start();
    }

    public static void stop() {
        poller.interrupt();
    }

    /**
     * Adds a method to be called in the event of a shutdown.
     * @param onTermination Method to be run when future EC2 shutdown detected.
     */
    public static void subscribe(Runnable onTermination) {
        subscribers.add(onTermination);
    }

    private static void alertAll() {
        for (Runnable preTerminationAction : subscribers) {
            preTerminationAction.run();
        }
    }

    private static class Poller implements Runnable {

        private static boolean serverDown = false;
        @Override
        public void run() {
            while (true) {
                    if (willTerminate()) {
                        // Shutdown detected
                        alertAll();
                    }
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }

        private boolean willTerminate() {
            boolean terminate = false;
            try {
                HttpURLConnection awsmeta = (HttpURLConnection) AWS_INSTANCE_ACTION.openConnection();
                awsmeta.setRequestMethod("GET");
                awsmeta.setInstanceFollowRedirects(true);
                awsmeta.connect();

                // URL will contain data when Spot scheduled for termination
                terminate = awsmeta.getResponseCode() != 404;

                if (serverDown) {
                    FMLLog.getLogger().info("Connection to AWS metadata server re-established.");
                    serverDown = false;
                }
            }
            catch (IOException e) {
//                e.printStackTrace();
                // Mark server as non-responsive then shut up
                if (!serverDown)
                    FMLLog.getLogger().error("Connection to AWS metadata server failed!");
                serverDown = true;
            }

            return terminate;
        }


    }
}
