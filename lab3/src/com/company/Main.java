package com.company;

public class Main {
    static int[][] matrix = {
            {2, 9, 4},
            {1, 5, 6},
            {3, 7, 8}
    };

    static boolean[][] visited = new boolean[3][3];
    static String maxNumber = "";

    static int[] dx = {-1, 1, 0, 0};
    static int[] dy = {0, 0, -1, 1};

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                dfs(i, j, "");
            }
        }
        System.out.println(maxNumber);
    }

    static void dfs(int x, int y, String path) {
        visited[x][y] = true;
        path += matrix[x][y];

        if (path.compareTo(maxNumber) > 0) {
            maxNumber = path;
        }

        for (int d = 0; d < 4; d++) {
            int nx = x + dx[d];
            int ny = y + dy[d];

            if (nx >= 0 && nx < 3 && ny >= 0 && ny < 3 && !visited[nx][ny]) {
                dfs(nx, ny, path);
            }
        }

        visited[x][y] = false;
    }
}
