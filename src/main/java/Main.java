import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        //Bot ranked = new Bot(Bot.Mode.RANKED, "f4354a7fd3e10ff41ed8");
        Bot test = new Bot(Bot.Mode.TEST, "DayBot" + ThreadLocalRandom.current().nextInt(10000));
        test.start();
    }
}
