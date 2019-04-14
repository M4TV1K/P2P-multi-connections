import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Coordinator extends Thread {
    static boolean coordinator;
    //This is an implementation of the P2P connection that allows to
    //keep multiple connections. Due to the fact that it has one player
    //who is the coordinator and he sends the updated data about all of the connected players
    //to everyone on the "server"
    //Here is the implementation of this so called "Coordinator"
    Coordinator() {
        start();
    }

    @Override
    public void run() {
        if (!Connection.players.isEmpty()) {
            //Coordinator is the first player on the list of all players
            //And the list is always being updated in the right way.
            //Therefore, for all of the connected players this list will be the Same
            coordinator = (Connection.myself.toString().equals(Connection.players.get(0).toString()));
        }
        if (coordinator) System.out.println("I'm coordinator");

        //"If you are coordinator" part
        while (coordinator) {
            synchronized (Connection.players) {
                //you send updated data concerning connected players to every player on the list
                for (int player = 0; player < Connection.players.size(); ++player) {
                    if (!Connection.players.get(player).toString().equals(Connection.myself.toString()) ) {
                        try {
                            //establish a connection with one player
                            Socket socket = new Socket(Connection.players.get(player).getIp(),
                                    Connection.players.get(player).getPort());
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            //send command
                            out.println("Update");
                            //send list of all players
                            out.println(Connection.players);
                        } catch (IOException e) {
                            //If cannot connect to the player, remove him from the list of players
                            Connection.players.remove(player);
                            //and resend updated data from the beginning
                            player = 0;
                        }
                    }
                }
            }
            //And coordinator does it every 5 seconds
            synchronized (this) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //"If you are not coordinator" part
        while (!coordinator) {
            //You are waiting 7 seconds for updates
            synchronized (this) {
                try {
                    wait(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //if "updated" boolean changes in the Listen class
            //it means that data has been updated
            if (!Connection.updated) Connection.updated = true;
            else {
                //but if not, it means that the coordinator left the game
                System.out.println("Coordinator left the game");
                //And we should choose new coordinator
                if (!Connection.players.get(0).getNickname().equals(Connection.myself.getNickname()))
                    Connection.players.remove(0); //coordinator is always on 0 position
                //run this thread again
                run();
                break;
            }
        }
    }

    static Coordinator becomingCoordinator() {
        Connection.players.add(Connection.myself);
        coordinator = true;
        Connection.firstTime = false;
        return new Coordinator();
    }
}
