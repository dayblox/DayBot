import java.awt.*;
import java.util.ArrayList;

class Check {
    static int dimensions;
    static boolean[][] map;

    static class Germe {
        int x, y, size = 1;
        ArrayList<Point> sides = new ArrayList<>();

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
                    if (!map[neighbor.x][neighbor.y]) {
                        sides.add(neighbor);
                        map[neighbor.x][neighbor.y] = true;
                        ++size;
                        res = true;
                    }
            }
            return res;
        }
    }

    static void check(int dim, String input) {
        dimensions = dim;
        String[] players = input.split(",");
        ArrayList<Germe> germes = new ArrayList<>();
        for (String player : players) {
            String[] positions = player.split(" ");
            Germe germe = new Germe(Integer.parseInt(positions[0]), Integer.parseInt(positions[1]));
            germes.add(germe);
        }
        ArrayList<Germe> germes2 = new ArrayList<>(germes.size());
        map = new boolean[dimensions][dimensions];
        for (int i = 0; i < germes.size(); ++i) {
            int x = germes.get(i).x;
            int y = germes.get(i).y;
            map[x][y] = true;
            Germe tmp2 = new Germe(x, y);
            germes2.add(0, tmp2);
        }
        boolean go = true;
        while (go) {
            go = false;
            for (Germe g : germes) {
                if (g.iterate())
                    go = true;
            }
        }
        map = new boolean[dimensions][dimensions];
        for (Germe g : germes2)
            map[g.x][g.y] = true;
        go = true;
        while (go) {
            go = false;
            for (Germe g : germes2) {
                if (g.iterate())
                    go = true;
            }
        }
        float min = 5000;
        float max = 0;
        int size = germes.size();
        for (int i = 0; i < size; ++i) {
            float f = (germes.get(i).size + germes2.get(size - i - 1).size) / 2f;
            if (f < min)
                min = f;
            if (f > max)
                max = f;
            System.out.print(f + " ");
        }
        System.out.println("- " + (max - min));
    }

    public static void main(String[] args) {
        check(16, "5 12,1 8,6 2");
    }
}
