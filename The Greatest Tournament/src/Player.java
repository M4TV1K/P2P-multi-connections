import java.util.ArrayList;

public class Player{
    private String nickname;
    private String ip;
    private int port;

    Player(String nickname, String ip, int port){
        this.nickname = nickname;
        this.ip = ip;
        this.port = port;
    }

    int getPort() {
        return port;
    }

    String getIp() {
        return ip;
    }

    String getNickname() {
        return nickname;
    }

    void setNickname(String nickname) {
        this.nickname = nickname;
    }
    @Override
    public String toString(){
        return nickname + " " + ip + " " + port;
    }

    static void getPlayers(String line) {
        String[] newPlayersList = line.substring(1, line.length() - 1).split(",");
        synchronized (Connection.players) {
            Connection.players = new ArrayList<>();
            for (int i = 0; i < newPlayersList.length; ++i) {
                newPlayersList[i] = newPlayersList[i].trim();
                String[] newPlayer = newPlayersList[i].split(" ");
                boolean change = true;
                for (Player player : Connection.players) {
                    if (player.getPort() == Integer.parseInt(newPlayer[2])) {
                        change = false;
                        break;
                    }
                }
                if (change) Connection.players.add(new Player(newPlayer[0], newPlayer[1], Integer.parseInt(newPlayer[2])));
            }
        }
    }
}
