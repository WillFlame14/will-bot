package bot.willbot;

import java.util.*;

public class Map{
    char[][] grid;
    HashMap<Character, Player> players;
    HashMap<Player, Character> playerValues;
    
    static int HEIGHT = 15, WIDTH = 15;
    
    public Map() {
        grid = new char[HEIGHT][WIDTH];
        players = new HashMap<>();
        playerValues = new HashMap<>();
    }
    
    public String toString() {
        String s = "```";
        for(int i = 0; i < HEIGHT; i++) {
            s += "\n";
            for(int j = 0; j < WIDTH; j++) {
                s += grid[i][j] + " ";
            }
        }
        return s + "```";
    }
    
    public Map duplicate() {
        Map duplicate = new Map();
        for(int i = 0; i < HEIGHT; i++) {
            for(int j = 0; j < WIDTH; j++) {
                duplicate.grid[i][j] = grid[i][j];
            }
        }
        return duplicate;
    }
    
    public void setCharacter(Player p, char s) {
        players.put(s, p);
        playerValues.put(p, s);
    }
    
    public Pair getLocation(char s) {       //RETURNS COMP COORDINATES
        for(int i = 0; i < HEIGHT; i++) {
            for(int j = 0; j < WIDTH; j++) {
                if(grid[i][j] == s) {
                    return new Pair(i, j);      
                }
            }
        }
        return new Pair(-1, -1);
    }
    
    public boolean setLocation(char s, Pair p) {        //TAKES COMP COORDINATES
        if(grid[p.x][p.y] == '.') {
            Pair oldLocation = getLocation(s);
            grid[oldLocation.x][oldLocation.y] = '.';
            grid[p.x][p.y] = s;
            return true;
        }
        return false;
    }
    
    public int getDistance(Player p, Player e) {        //no adjustment needed since distances are relative
        Pair pp = getLocation(playerValues.get(p));
        Pair ee = getLocation(playerValues.get(e));
        return Math.abs(pp.x - ee.x) + Math.abs(pp.y - ee.y);
    }
    
    public int getDistance(Player p, Pair e) {        //no adjustment needed since distances are relative - this compares player to location
        Pair pp = getLocation(playerValues.get(p));
        return Math.abs(pp.x - e.x) + Math.abs(pp.y - e.y);
    }
    
    public boolean removePlayer(Player p) {
        Pair location = getLocation(playerValues.get(p));
        if(location.x == -1 && location.y == -1) {
            return false;
        }
        grid[location.x][location.y] = '.';
        return true;
    }
    
    public Map fill() {     //randomly fill a map
        for(int i = 0; i < Map.HEIGHT; i++) {
            for(int j = 0; j < Map.WIDTH; j++) {
                int rng = (int)(Math.random() * 100) + 1;   //1 to 100
                if(rng <= 13) {
                    grid[i][j] = '#';
                }
                else {
                    grid[i][j] = '.';
                }
            }
        }
        int y1 = (int)(Math.random() * Map.HEIGHT), x1 = (int)(Math.random() * Map.WIDTH);
        grid[y1][x1] = 'A';     //characters
        int rng1 = (int)(Math.random() * 5) - 2, rng2 = (int)(Math.random() * 5) - 2;     //randomness of p2, should be near p1
        if(rng1 == 0 && rng2 == 0) {
            rng1 = 1;
            rng2 = 2;
        }
        if(y1 + rng1 >= Map.HEIGHT) {
            rng1 -= Map.HEIGHT;
        }
        else if(y1 + rng1 <= 0) {
            rng1 += Map.HEIGHT;
        }
        if(x1 + rng2 >= Map.WIDTH) {
            rng2 -= Map.WIDTH;
        }
        else if(x1 + rng2 <= 0) {
            rng2 += Map.WIDTH;
        }
        grid[y1 + rng1][x1 + rng2] = 'B';
        
        while(true) {
            int y2 = (int)(Math.random() * Map.HEIGHT), x2 = (int)(Math.random() * Map.WIDTH), y3 = (int)(Math.random() * Map.HEIGHT), x3 = (int)(Math.random() * Map.WIDTH);
            if(grid[y2][x2] == 'A' || grid[y2][x2] == 'B' || grid[y3][x3] == 'A' || grid[y3][x3] == 'B' || (y2 == y3 && x2 == x3)) {
                continue;
            }
            grid[y2][x2] = '1';
            grid[y3][x3] = '2';
            break;
        }
        return this;
    }
}

class Pair {
    int x, y;
    
    public Pair(int a, int b) {
        x = a;
        y = b;
    }
    
    public Pair toCartesian() {
        int oldx = x;
        x = y + 1;
        y = Map.HEIGHT - oldx;
        return new Pair(x, y);
    }
    
    public Pair toComp() {
        int oldx = x;
        x = Map.HEIGHT - y;      
        y = oldx - 1;
        return new Pair(x, y);
    }
    
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
