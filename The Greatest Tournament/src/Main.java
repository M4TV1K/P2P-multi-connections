import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        String ipAddress = InetAddress.getLocalHost().toString().split("/")[1];
        System.out.println("Enter your nickname:");
        String nickname = Connection.SCAN.next();
        Random random = new Random();
        Player myself = new Player(nickname, ipAddress, (random.nextInt(10001) + 20000));
        System.out.println("Your character: " + myself.toString() + " (IP address and port number)");
        new Connection(myself);
    }
}
