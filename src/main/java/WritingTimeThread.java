import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;

public class WritingTimeThread extends Thread {

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss"); //Time formatter.
    public String singleMessage;
    public Semaphore semaphore;

    WritingTimeThread(String singleMessage, Semaphore semaphore) {
        this.singleMessage = singleMessage;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            while (!Controller.isCanceled) {
                semaphore.acquire(); //take a place in semaphore.
                singleMessage = "t " + dtf.format(LocalDateTime.now()); //method for getting current time.
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
