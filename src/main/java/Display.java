public class Display {
    public static void main(String[] args) {
        String[] players = "61 58,9 61,38 52,25 48,48 23,29 20,45 45,54 13,35 4,6 16,32 29,19 10,57 0,51 36,22 42,41 7,3 39,0 26,13 55,16 32".split(",");
        int dim = 64;
        boolean[][] map = new boolean[dim][dim];
        for (String s : players) {
            String[] tmp = s.split(" ");
            int x = Integer.parseInt(tmp[0]);
            int y = Integer.parseInt(tmp[1]);
            map[x][y] = true;
        }
        for (int a = 0; a < dim + 1; ++a)
            System.out.print("--");
        System.out.println();
        for (int i = 0; i < dim; ++i) {
            System.out.print("|");
            for (int j = 0; j < dim; ++j) {
                if (map[i][j])
                    System.out.print("XX");
                else
                    System.out.print("  ");
            }
            System.out.print("|\n");
        }
        for (int a = 0; a < dim + 1; ++a)
            System.out.print("--");
    }
}
