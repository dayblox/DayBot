import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("1- Test\n2- Ranked");
        int input = in.nextInt();
        Bot.Mode mode;
        String param;
        switch (input) {
            case 2:
                mode = Bot.Mode.RANKED;
                param = "f4354a7fd3e10ff41ed8";
                break;
            default:
                mode = Bot.Mode.TEST;
                param = "DayBot" + ThreadLocalRandom.current().nextInt(10000);
                break;
        }
        Bot bot = new Bot(mode, param);
        bot.start();
    }
}
