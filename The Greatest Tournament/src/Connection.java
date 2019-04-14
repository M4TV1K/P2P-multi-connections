
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Connection {
    static final Scanner SCAN = new Scanner(System.in);
    static boolean firstTime = true;
    static Thread game;
    static Coordinator coordinate;
    static boolean updated;
    static String currPlayers = "";
    static List<Player> players = new ArrayList<>();

    static List<String> played = new ArrayList<>();
    static boolean available = false;
    static Player myself;
    static Player opponent;
    private ServerSocket server = null;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    Connection(Player p) {
        myself = p;
        game = new Thread(() -> {
            do {
                System.out.println(
                        "\nEnter JOIN to start game" +
                        "\nEnter WAIT to start waiting for someone to connect to you" +
                        "\nEnter SHOW to show all players" +
                        "\nEnter QUIT to finish game"
                );
                String choice = SCAN.next();
                switch (choice.toLowerCase()) {
                    case "join":
                        join();
                        break;
                    case "wait":
                        waiting();
                        break;
                    case "show":
                        if (players.size() > 1) players.forEach(System.out::println);
                        else System.out.println("There is only you, connect to someone)");
                        break;
                    case "quit":
                        quit();
                        break;
                    default:
                        System.out.println("You don't have such an option)");
                        break;
                }
            } while (true);
        });
        game.start();
    }

    private void listenServerSocket() {
        new Thread(() -> {
            Socket player = null;
            try {
                server = new ServerSocket(myself.getPort());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not listen");
                System.exit(-1);
            }

            while (true) {
                try {
                    player = server.accept();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                (new Listen(player)).start();
            }
        }).start();
    }

    private boolean connect(int player) {
        //if this is your first action
        if (firstTime) {
            connectToSomeone();
            listenServerSocket();
        }
        //if you are alone and there is no player on the list
        else if (players.size() == 1) {
            Coordinator.coordinator = false;
            connectToSomeone();
        }
        //connect to anyone who is on the list of all players
        else {
            synchronized (players) {
                try {
                    opponent = players.get(player);
                    if (myself.getPort() == opponent.getPort()) return false;
                } catch (NullPointerException e) {
                    System.out.println("This guy already left and code missed something)");
                    return false;
                }
            }
            played.forEach(System.out::println);
            for (String was : played) {
                if (was.equals(opponent.getNickname())) {
                    System.out.println("I've already played with " + opponent.getNickname());
                    return false;
                }
            }
        }

        System.out.println("Connecting to opponent...");
        try {
            socket = new Socket(opponent.getIp(), opponent.getPort());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (firstTime) {
                firstTime = false;
                Coordinator.coordinator = false;
                coordinate = new Coordinator();
            }
            return true;
        } catch (UnknownHostException e) {
            System.out.println("I cannot connect to this player");
        } catch (IOException e) {
            System.out.println("Player with this IP address and Port number doesn't exist");
            if (firstTime) coordinate = Coordinator.becomingCoordinator();
            else if (players.size() > 1) {
                System.out.println(players.get(player).getNickname() +
                        " has already left the game");
            }
        }
        return false;
    }

    private void connectToSomeone() {
        System.out.println("Enter friend's ip address and port number:");
        String ipAddress = "";
        int port;
        while (true) {
            try {
                if (ipAddress.equals("")) {
                    System.out.println("IP Address: ");
                    ipAddress = SCAN.next();
                    if (ipAddress.split("\\.").length != 4) throw new NumberFormatException();
                }
                System.out.println("Port number: ");
                port = Integer.parseInt(SCAN.next());
                if (port < 1024 || port > 65535) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println(
                        "Enter correct data:" +
                                "\nIP address: 0-255.0-255.0-255.0-255" +
                                "\nPort number should be between 1024 and 65535");
            }
        }
        opponent = new Player("", ipAddress, port);
    }

    private void join(){
        System.out.println("I'm joining the game");
        for (int player = 0; player < players.size() || firstTime; ++player) {
            if (connect(player)) {
                try {
                    if (players.isEmpty() || players.size() == 1) {
                        out.println("list");
                        out.println(myself.toString());

                        String temp = in.readLine();
                        if (!temp.equals("OK")) {
                            currPlayers = temp;
                            Player.getPlayers(currPlayers);
                        }
                        System.out.println("I've received the list with all players!");
                    } else out.println("I already have one!");

                    out.println("Hello! I am " + myself.getNickname());
                    String line = in.readLine();
                    System.out.println(line);
                    opponent.setNickname(line.substring(12));
                    line = in.readLine(); //Let's play line or not!
                    System.out.println(line);
                    if (line.substring(0, 4).equals("Busy")) {
                        System.out.println("I need to find someone else(((");
                    }
                    else {
                        played.add(opponent.getNickname());
                        Game.playOnYourSide(in, out);
                    }
                    socket.close();
                }
                catch (IOException e) {
                    System.out.println("Opponent has left the game");
                }
            }
        }
        System.out.println("You've tried to play with everyone on the list");
    }

    private void waiting() {
        if (firstTime) {
            coordinate = Coordinator.becomingCoordinator();
            listenServerSocket();
        }
        try {
            available = true;
            System.out.println("Your IP Address and Port number: " + myself.getIp() + " " + myself.getPort() +
                    "\nYou're waiting 1 minute for someone...");
            synchronized (this) {
                wait(60000);
            }
            available = false;
            System.out.println("No one is connected to you");
        } catch (InterruptedException e) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e1) {
                System.out.println("GAME HAS BEEN ENDED");
            }
        }
    }

    private void quit() {
        if (played.isEmpty())
            System.out.println("You haven't played at all(\nBut still thank you for visiting us");
        else {
            System.out.println("You've played with:");
            played.forEach(System.out::println);
            System.out.println("Thank you for the game!");
        }
        System.out.println("Bye " + myself.getNickname());
        try {
            server.close();
        } catch (IOException e) {}
        catch (NullPointerException e) {}
        System.exit(0);
    }
}
