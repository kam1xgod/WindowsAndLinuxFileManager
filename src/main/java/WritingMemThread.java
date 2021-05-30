import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

public class WritingMemThread extends Thread {

    DecimalFormat df = new DecimalFormat("#.##"); //formatter for double variables.
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean.class); //this is really helpful lib for getting OS info outside of JVM.
    public String singleMessage;
    public Semaphore semaphore;

    WritingMemThread(String singleMessage, Semaphore semaphore) {
        this.singleMessage = singleMessage;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            while (!Controller.isCanceled) {
                semaphore.acquire(); //take a place in semaphore.
                singleMessage = "m " + df.format(1.0 * (osBean.getTotalSwapSpaceSize() - osBean.getFreeSwapSpaceSize())  / 1024 / 1024  / 1024) +  " GB";
                //getting total swap space and free swap space for calculate used swap space. converting bytes in GB.
                String finalMessage = singleMessage; //create new final variable. only this type can be used in runnable interface.
                Main.connection.send(finalMessage); //sending message to client.
                semaphore.release(); //release place in semaphore.
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
