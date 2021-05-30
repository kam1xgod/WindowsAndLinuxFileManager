import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;

public class WritingCPUThread extends Thread {

    DecimalFormat df = new DecimalFormat("#.##"); //formatter for double variables.
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean.class); //this is really helpful lib for getting OS info outside of JVM.
    public String singleMessage;
    public Semaphore semaphore;

    WritingCPUThread(String singleMessage, Semaphore semaphore) {
        this.singleMessage = singleMessage;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            while (!Controller.isCanceled) {
                semaphore.acquire(); //take a place in semaphore.
                singleMessage = "p " + df.format(osBean.getProcessCpuTime() / 1e+9) + " seconds";
                //getting process cpu usage time in nanoseconds. to convert into seconds used "/1e+9".
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
