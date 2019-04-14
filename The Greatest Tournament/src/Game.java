import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

class Game {
    private static final Scanner SCAN = new Scanner(System.in);

    //You connect to the opponent
    static void playOnYourSide(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Let the fun begin!");
        System.out.println("Enter the number:");
        int a = enterNumber();
        out.println(a);
        System.out.println("Wait for " + Connection.opponent.getNickname() + "'s choice!");
        int b = Integer.parseInt(in.readLine());
        System.out.println(Connection.opponent.getNickname() + " chose: " + b);
        int sum = a + b;
        b = 0;
        System.out.println("Mission objective: " + sum);
        System.out.println("Starts counting from you!");
        while (true) {
            a = b;
            out.println(++a);
            System.out.println("You: " + a);
            if (a == sum) {
                System.out.println("You win!");
                break;
            }
            b = Integer.parseInt(in.readLine());
            System.out.println(Connection.opponent.getNickname() + ": " + b);
            if (b == sum) {
                System.out.println("You lose!");
                break;
            }
        }
    }

    //Opponent connect to you
    static void playOnOpponentSide(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Let's play!");
        System.out.println(in.readLine());
        System.out.println("Wait for " + Listen.opponent + "'s choice!");
        int a = Integer.parseInt(in.readLine());
        System.out.println(Listen.opponent + " chose: " + a);
        System.out.println("Now it's your turn to choose:");
        int b = enterNumber();
        out.println(b);
        int sum = a + b;
        System.out.println("Mission objective: " + sum);

        System.out.println("Starts counting from " + Listen.opponent + "!");
        while (true) {
            a = Integer.parseInt(in.readLine());
            System.out.println(Listen.opponent + ": " + a);
            if (a == sum) {
                System.out.println("You lose!");
                break;
            }
            b = a;
            out.println(++b);
            System.out.println("You: " + b);
            if (b == sum) {
                System.out.println("You win!");
                break;
            }
        }
    }

    private static int enterNumber() {
        int number;
        while (true) {
            try {
                number = Integer.parseInt(SCAN.next());
                if (number <= 0 || number >= 100) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Input number between 1 and 100, please");
            }
        }
        return number;
    }
}
