
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Listen extends Thread{
    private final Socket socket;
    static String opponent;

    Listen(Socket socket){
        super();
        this.socket = socket;
    }

    @Override
    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //LOOK AT THIS PIECE OF CODE
            //---------------------------------------------------------------------------------
            String needs = in.readLine();
            //Coordinator sends updated data concerning players
            if (needs.equals("Update")){
                Connection.updated = false;
                needs = in.readLine(); //receives all the data about players
                if (!needs.equals(Connection.currPlayers)) { //compare with existing one
                    Connection.currPlayers = needs;
                    Player.getPlayers(needs);
                }

                //notify "If you aren't coordinator part" that you have received updated data
                synchronized (Connection.coordinate){
                    Connection.coordinate.notify();
                }

                //and you start waiting for the new one
                synchronized (this) {
                    wait(6000);
                    Connection.updated = !Connection.updated;
                }
            }

            //skip this for now, and return here when you see
            //"he redirects this message to the coordinator" comment
            else if (needs.equals("NEWBIE")) {
                String opp = in.readLine();
                if (Coordinator.coordinator) {
                    Connection.players.add(new Player(opp.split(" ")[0],
                            opp.split(" ")[1],
                            Integer.parseInt(opp.split(" ")[2])));
                    synchronized (Connection.coordinate){
                        Connection.coordinate.notify();
                    }
                }
            }
            else {
                //You give the list of players to another player
                if (needs.equals("list")) {
                    String opp = in.readLine();
                    System.out.println(opp);
                    synchronized (Connection.players) {
                        //If you are the coordinator, you add this player to the list
                        //and send him the updated list
                        if (Coordinator.coordinator) {
                            Connection.players.add(new Player(opp.split(" ")[0],
                                    opp.split(" ")[1],
                                    Integer.parseInt(opp.split(" ")[2])));
                            out.println(Connection.players);
                        }
                        // if not,
                        else {
                            //he redirects this message to the coordinator
                            for (int i = 0; i < Connection.players.size(); ++i) {
                                try {
                                    Socket redirect = new Socket(Connection.players.get(i).getIp(),
                                            Connection.players.get(i).getPort());
                                    PrintWriter outR = new PrintWriter(redirect.getOutputStream(), true);
                                    outR.println("NEWBIE");
                                    outR.println(opp);
                                    out.println("OK");
                                    redirect.close();
                                    break;
                                } catch (UnknownHostException e) {
                                    System.out.println("I cannot connect to such a guy");
                                } catch (IOException e) {
                                    System.out.println("There is no coordinator maybe he's next in the list");
                                }
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------
                //Thank you for looking at this code:)


                opponent = in.readLine();
                System.out.println(opponent);
                opponent = opponent.substring(12);
                out.println("Hello! I am " + Connection.myself.getNickname());
                if (Connection.available) {
                    Connection.game.interrupt();
                    Connection.available = false;
                    Connection.played.add(opponent);
                    Game.playOnOpponentSide(in, out);
                    Connection.game.interrupt();
                }
                else out.println("Busy I am. Play with anyone else");
            }
            socket.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Connection with opponent has been lost");
            Connection.game.interrupt();
        }
    }
}