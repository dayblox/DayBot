import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
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
        private boolean dead = false;

        Tracker(int x, int y) {
            this.x = x;
            this.y = y;
            map[x][y] = true;
        }

        void move(int direction) {
            if (dead)
                return;
            switch (direction) {
                case -2:
                    dead = true;
                    break;
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

    private class Germe {
        int x, y, direction, size = 0;
        ArrayList<Point> sides = new ArrayList<>();

        Germe(int x, int y, int direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            sides.add(new Point(x, y));
        }

        Germe(int x, int y) {
            this.x = x;
            this.y = y;
            sides.add(new Point(x, y));
        }

        boolean iterate() {
            boolean res = false;
            ArrayList<Point> copy = new ArrayList<>(sides);
            for (Point p : copy) {
                sides.remove(p);
                int x = p.x;
                int y = p.y;
                int xn = (x + 1) % dimensions;
                int yn = (y + 1) % dimensions;
                int xp = (x == 0 ? dimensions - 1 : x - 1);
                int yp = (y == 0 ? dimensions - 1 : y - 1);
                Point[] arr = {new Point(xn, y), new Point(x, yp), new Point(xp, y), new Point(x, yn)};
                for (Point neighbor : arr)
                    if (!voronoi[neighbor.x][neighbor.y]) {
                        sides.add(neighbor);
                        voronoi[neighbor.x][neighbor.y] = true;
                        ++size;
                        res = true;
                    }
            }
            return res;
        }
    }

    enum Mode {
        TEST,
        RANKED
    }

    private String pseudo, token, baseURL = "https://lightningbot.tk/api";
    private int turn = 1, dimensions, me;
    private boolean[][] map, voronoi;
    private Mode mode;
    private ArrayList<Tracker> trackers = new ArrayList<>();

    Bot(Mode mode, String param) {
        this.mode = mode;
        switch (mode) {
            case RANKED:
                this.token = param;
                break;
            case TEST:
                this.pseudo = param;
                this.baseURL += "/test";
                break;
        }
    }

    private JSONObject get(String path) {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseURL + path)).build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        assert response != null;
        System.out.println(response.body());
        return new JSONObject(response.body());
    }

    private void connect() throws InterruptedException {
        JSONObject json;
        if (mode.equals(Mode.RANKED)) {
            json = get("/connect/" + token);
            pseudo = json.getString("pseudo");
        } else {
            json = get("/connect/" + pseudo);
            token = json.getString("token");
        }
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
        ArrayList<Germe> others = new ArrayList<>();
        resetGermes((ArrayList<Germe>) others);
        int x = me.x;
        int y = me.y;
        int xn = (x + 1) % dimensions;
        int yn = (y + 1) % dimensions;
        int xp = (x == 0 ? dimensions - 1 : x - 1);
        int yp = (y == 0 ? dimensions - 1 : y - 1);
        Germe[] arr = {new Germe(xn, y, 0), new Germe(x, yp, 1), new Germe(xp, y, 2), new Germe(x, yn, 3)};
        Germe max = arr[0];
        for (Germe g : arr) {
            voronoi = new boolean[dimensions][dimensions];
            for (int i = 0; i < dimensions; ++i)
                System.arraycopy(map[i], 0, voronoi[i], 0, dimensions);
            if (!voronoi[g.x][g.y]) {
                voronoi[g.x][g.y] = true;
                others = new ArrayList<>();
                resetGermes(others);
                for (Germe ge : others)
                    ge.iterate();
                boolean iterate = true;
                while (iterate) {
                    for (Germe ge : others)
                        ge.iterate();
                    iterate = g.iterate();
                }
                if (g.size > max.size)
                    max = g;
            }
        }
        System.out.println("Chosen: " + max.direction);
        JSONObject json = get("/move/" + token + "/" + max.direction + "/" + turn++);
        TimeUnit.MILLISECONDS.sleep(json.getInt("wait"));
    }

    private void resetGermes(ArrayList<Germe> others) {
        for (int i = 0; i < trackers.size(); ++i)
            if (i != this.me) {
                Tracker t = trackers.get(i);
                if (!t.dead)
                    others.add(new Germe(t.x, t.y));
            }
    }

    void start() {
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
