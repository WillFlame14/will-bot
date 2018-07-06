package bot.willbot;

import java.util.*;

public class Map{
    char[][] grid;
    HashMap<Character, Player> players;
    HashMap<Player, Character> playerValues;
    
    public Map() {
        grid = new char[8][6];
        players = new HashMap<>();
        playerValues = new HashMap<>();
    }
    
    public String toString() {
        String s = "```";
        for(int i = 0; i < 8; i++) {
            s += "\n";
            for(int j = 0; j < 6; j++) {
                s += grid[i][j] + " ";
            }
        }
        return s + "```";
    }
    
    public Map duplicate() {
        Map duplicate = new Map();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 6; j++) {
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
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 6; j++) {
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
        y = 8 - oldx;
        return new Pair(x, y);
    }
    
    public Pair toComp() {
        int oldx = x;
        x = 8 - y;      
        y = oldx - 1;
        return new Pair(x, y);
    }
    
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
