import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class Bot {
    private class Tracker {
        private int x, y;

        Tracker(int x, int y) {
            this.x = x;
            this.y = y;
            map[x][y] = true;
        }

        void move(int direction) {
            switch (direction) {
                case 0:
                    x = (x + 1) % dimensions;
                    break;
                case 1:
                    y = (y == 0 ? dimensions - 1 : y - 1);
                    break;
                case 2:
                    x = (x == 0 ? dimensions - 1 : x - 1);
                    break;
                case 3:
                    y = (y + 1) % dimensions;
                    break;
            }
            map[x][y] = true;
        }
    }

    private String pseudo, token;
    private int turn = 1, dimensions, me;
    private boolean[][] map;
    private ArrayList<Tracker> trackers = new ArrayList<>();

    Bot(String pseudo) {
        this.pseudo = pseudo;
    }

    private JSONObject get(String path) {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://lightningbot.tk/api" + path)).build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(response.body());
        return new JSONObject(response.body());
    }

    private void connect() throws InterruptedException {
        JSONObject json = get("/connect/" + pseudo);
        token = json.getString("token");
        TimeUnit.MILLISECONDS.sleep(json.getInt("wait"));
    }

    private void info() throws InterruptedException {
        JSONObject json = get("/info/" + token);
        dimensions = json.getInt("dimensions");
        JSONArray arr = json.getJSONArray("positions");
        map = new boolean[dimensions][dimensions];
        for (int i = 0; i < arr.length(); ++i) {
            JSONObject bot = arr.getJSONObject(i);
            int x = bot.getInt("x");
            int y = bot.getInt("y");
            String pseudo = bot.getString("pseudo");
            if (pseudo.equals(this.pseudo))
                me = i;
            trackers.add(new Tracker(x, y));
        }
        TimeUnit.MILLISECONDS.sleep(json.getInt("wait"));
    }

    private void directions() {
        JSONObject json = get("/directions/" + token + "/" + turn);
        JSONArray arr = json.getJSONArray("directions");
        for (int i = 0; i < arr.length(); ++i) {
            JSONObject bot = arr.getJSONObject(i);
            trackers.get(i).move(bot.getInt("direction"));
        }
    }

    private void move() throws InterruptedException {
        Tracker me = trackers.get(this.me);
        int x = me.x;
        int y = me.y;
        int direction;
        int xn = (x + 1) % dimensions;
        //int yn = (y + 1) % dimensions;
        int xp = (x == 0 ? dimensions - 1 : x - 1);
        int yp = (y == 0 ? dimensions - 1 : y - 1);
        if (!map[xn][y])
            direction = 0;
        else if (!map[x][yp])
            direction = 1;
        else if (!map[xp][y])
            direction = 2;
        else
            direction = 3;
        JSONObject json = get("/move/" + token + "/" + direction + "/" + turn++);
        TimeUnit.MILLISECONDS.sleep(json.getInt("wait"));
    }

    public void start() {
        try {
            connect();
            info();
            while (true) {
                directions();
                move();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
