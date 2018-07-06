package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BossBattle implements Category {
    static HashMap<Integer, ArrayList<Player>> rooms = new HashMap<>();     //bossid --> players
    static HashMap<Integer, ArrayList<Player>> deadBosses = new HashMap<>();        //bossid --> bosses that are dead
    static HashMap<Integer, Map> battles = new HashMap<>();     //bossid --> map
    static HashMap<Player, Integer> toBoss = new HashMap<>();       //player --> bossid
    static HashSet<Player> inboss = new HashSet<>();        //list of players currently fighting a boss
    static HashMap<Integer, ArrayList<Player>> turnover = new HashMap<>();       //index is the bossid, list contains players who have taken their turns
    static HashMap<Integer, ArrayList<Player>> moveover = new HashMap<>();       //index is the bossid, list contains players who have taken their moves
    static HashMap<Integer, ArrayList<Player>> bossMove = new HashMap<>();
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!boss") || action.equals("w!bosses") || action.equals("w!join") || action.equals("w!rooms") || action.equals("w!endturn")
                || action.equals("w!move") || action.equals("w!whoswho");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(action.equals("w!boss")) {
            if(args.size() < 2) {
                throw new ValidationException("Not enough arguments were provided. The correct format is `w!boss <name> <characters>`.");
            }
            for(int i = 1; i < args.size(); i++) {      //first, check all players
                Utilities.checkPlayer(args.get(i), event.getMessage().getAuthor().getIdLong());
            }
            switch(args.get(0)) {
                case "1":
                case "Cyrus":       //prevent multiple rooms of same boss
                    if(args.size() > 3) {
                        throw new ValidationException("Too many arguments were provided. Cyrus is a 2v2 battle.");
                    }
                    if(!Bot.playermap.containsKey(args.get(0)) || (args.size() > 1 && !Bot.playermap.containsKey(args.get(1)))) {
                        throw new ValidationException("You did not specify valid usernames.");
                    }
                    if(rooms.containsKey(0)) {
                        throw new ValidationException("A room with this boss already exists.");
                    }
                    ArrayList<Player> players = new ArrayList<>(4);
                    players.add(Bot.bosses.get(1));  //Troubadour - added first so they go first
                    players.add(Bot.bosses.get(0));  //Cyrus
                    players.add(Bot.playermap.get(args.get(1)));
                    if(args.size() == 2) {      //no one is added to inboss yet.
                        rooms.put(0, players);
                        throw new ValidationException("A room has been created. Other users can join this room with `w!join Cyrus`.");
                    }
                    players.add(Bot.playermap.get(args.get(2)));
                    rooms.put(0, players);      //overwrite
                    inboss.add(Bot.playermap.get(args.get(1)));     //remember, args.get(0) is the id
                    inboss.add(Bot.playermap.get(args.get(2)));
                    turnover.put(0, new ArrayList<>());
                    moveover.put(0, new ArrayList<>());
                    deadBosses.put(0, new ArrayList<>());
                    bossMove.put(0, new ArrayList<>());
                    Map battle = Bot.maps.get(0).duplicate();
                    battle.setCharacter(Bot.bosses.get(0), '1');        //Cyrus is 1
                    battle.setCharacter(Bot.bosses.get(1), '2');        //Troubadour is 2
                    battle.setCharacter(Bot.playermap.get(args.get(1)), 'A');        //P1 is A
                    battle.setCharacter(Bot.playermap.get(args.get(2)), 'B');        //P2 is 2
                    toBoss.put(Bot.playermap.get(args.get(1)), 0);
                    toBoss.put(Bot.playermap.get(args.get(2)), 0);      //add these players to link to Cyrus
                    battles.put(0, battle);
                    c.sendMessage("Entering boss battle.").queue();
                    c.sendMessage(BossBattle.battles.get(0).toString()).queue();
                    c.sendMessage(whosWho(battles.get(0))).queue();
                    break;
                default:
                    throw new ValidationException("You did not specify a valid boss name.");
            }
        }
        else if(action.equals("w!join")) {
            if(args.size() < 2) {
                throw new ValidationException("Not enough arguments were provided. The correct format is `w!join <boss name> <characters>`.");
            }
            int id;
            switch(args.get(0)) {       //find ID
                case "1":
                case "Cyrus":
                    id = 0;
                    break;
                default:
                    throw new ValidationException("You did not specify a valid boss name.");
            }
            
            //ERROR CHECKING
            if(!rooms.containsKey(id)) {
                throw new ValidationException("The room has not been created yet. Use `w!boss` to create one.");
            }       //size() can be used since initalizing determines the capacity, not the size
            if(rooms.get(id).size() + args.size() - 1 > getSize(id) * 2) {        //subtract 1 so the id/name is not included
                throw new ValidationException("You provided too many characters. The room only has " + ((getSize(id) * 2) - rooms.get(id).size()) + " spaces left.");
            }
            for(int i = 1; i < args.size(); i++) {
                Utilities.checkPlayer(args.get(i), event.getMessage().getAuthor().getIdLong());
            }
            
            //JOINING ROOM
            for(int i = 1; i < args.size(); i++) {
                Player p = Bot.playermap.get(args.get(i));
                if(rooms.get(id).contains(p)) {
                    c.sendMessage(p.username + " is already in the room!").queue();
                    continue;
                }
                c.sendMessage(p.username + " has joined the room!").queue();
                rooms.get(id).add(p);
            }
            
            //CHECK IF ROOM IS FULL
            if(rooms.get(id).size() == getSize(id) * 2) {
                c.sendMessage("Room is full. Entering boss battle.").queue();
                for(int i = getSize(id); i < rooms.get(id).size(); i++) {         //add all players into inboss
                    inboss.add(rooms.get(id).get(i));
                }
                turnover.put(id, new ArrayList<>());     //create turnover
            }
            else {
                int remain = (getSize(id) * 2) - rooms.get(id).size();
                c.sendMessage("The room is not full yet. There " + (remain == 1?"is ":"are ") + remain + " space" + (remain == 1?"":"s") + " left.").queue();
            }
        }
        else if(action.equals("w!bosses")) {
            c.sendMessage("```List of Boss Battles:\n"
                    + "Cyrus [Lv. 15] Format: 2v2 Reward: Brave Axe```").queue();
        }
        else if(action.equals("w!rooms")) {
            String s = "```List of Rooms:\n";
            Object[] rms = rooms.values().toArray();
            Object[] ids = rooms.keySet().toArray();
            for(int h = 0; h < rooms.values().size(); h++) {
                ArrayList<Player> pls = (ArrayList<Player>)rms[h];
                s += getName((int)ids[h]) + ": ";
                for(int i = 0; i < pls.size() - 1; i++) {
                    s += pls.get(i).username + ", ";
                }
                s += pls.get(pls.size() - 1).username + (pls.size() == getSize((int)ids[h]) * 2?" [FULL]":"") + "\n";
            } 
            c.sendMessage(s + "```").queue();
        }
        else if(action.equals("w!endturn")) {       //w!endturn <user>
            Player user;
            if (args.isEmpty() && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(user.username);
            } else if (args.isEmpty()) {
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!endturn <user>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = Bot.playermap.get(args.get(0));
            if(turnover.get(toBoss.get(user)).contains(user)) {
                throw new ValidationException(user.username + "'s turn has already ended.");
            }
            turnover.get(toBoss.get(user)).add(user);
            c.sendMessage(user.username + "'s turn has ended.").queue();
            if(turnover.get(toBoss.get(user)).size() == BossBattle.getSize(toBoss.get(user))) {
                bossAttack(toBoss.get(user));       //do prep
                AttackConfirm.bossEnemyPhase(toBoss.get(user), c);       //start enemy phase
                if(!Attack.defeatBoss) {     //once boss is defeated, the map doesn't exist anymore
                    c.sendMessage(BossBattle.battles.get(BossBattle.toBoss.get(user)).toString()).queue();        //send the updated map
                }
                Attack.defeatBoss = false;
            }
            if(Attack.failure) {
                int id = toBoss.get(user);
                for(int i = 0; i < BossBattle.getSize(id); i++) {
                    BossBattle.rooms.get(id).get(i).stats.chp = BossBattle.rooms.get(id).get(i).stats.thp;     //refill HP on all bosses
                    c.sendMessage("The team was defeated. Better luck next time...").queue();
                    BossBattle.turnover.remove(id);
                    BossBattle.moveover.remove(id);
                    BossBattle.rooms.remove(id);
                    BossBattle.battles.remove(id);
                    BossBattle.bossMove.remove(id);
                    BossBattle.deadBosses.remove(id);
                    Attack.failure = false;
                }
            }
        }
        else if(action.equals("w!move")) {       //w!move <character> <location>         TO DO: Check that player is not already in a map (room).
            Player user;
            int x, y;
            //ERROR PARSING
            if(args.size() < 2) {
                throw new ValidationException("Not enough arguments were provided. The correct format is `w!move <character> <location>`.");
            }       
            if(!Bot.playermap.containsKey(args.get(0))) {
                throw new ValidationException("You did not specify a valid character.");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = Bot.playermap.get(args.get(0));      //set by username
            if(!inboss.contains(user)) {
                throw new ValidationException(user.username + " is not currently in a map.");
            }
            if(turnover.get(toBoss.get(user)).contains(user)) {
                throw new ValidationException(user.username + " has already used their turn.");
            }
            if(moveover.get(toBoss.get(user)).contains(user)) {
                throw new ValidationException(user.username + " has already used their movement for this turn.");
            }
            Map map = battles.get(toBoss.get(user));
            try {
                String[] parts = args.get(1).split(",");
                x = Integer.parseInt(parts[0]);     
                y = Integer.parseInt(parts[1]);
            }
            catch(Exception e) {
                throw new ValidationException("Your destination was not recognized. Coordinates should be given as `x,y`.");
            }
            if(x < 1 || x > 6 || y < 1 || y > 8) {      //invalid location - x and y are user input, and so are in Cartesian
                throw new ValidationException("You did not specify a valid location to move.");
            }
            Pair original = map.getLocation(map.playerValues.get(user)).toCartesian();      //note that user inputs Cartesian coordinates, so getLocation should be in Cartesian
            if(Math.abs(x - original.x) + Math.abs(y - original.y) > 2) {
                throw new ValidationException(user.username + " does not have enough movement.");
            }
            
            //EXECUTION
            if(map.setLocation(map.playerValues.get(user), new Pair(x, y).toComp())) {       //x and y are in Cartesian, setLocation takes comp
                c.sendMessage(battles.get(toBoss.get(user)).toString()).queue();
                moveover.get(toBoss.get(user)).add(user);
            }
            else {
                throw new ValidationException(user.username + " cannot move there.");
            }
        }
        else if(action.equals("w!whoswho")) {   //w!whoswho [<user>]
            Player user;
            if(args.isEmpty() && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(0, user.username);     //so that args.get(0) works
            }
            else if (args.size() == 1) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!whoswho <user>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = Bot.playermap.get(args.get(0));
            if(!inboss.contains(user)) {
                throw new ValidationException(user.username + "is not currently in a map.");
            }
            Map map = battles.get(toBoss.get(user));
            c.sendMessage(whosWho(map)).queue();
        }
    }

    public static String whosWho(Map map) {
        String s = "```";
        Object[] chars = map.players.keySet().toArray();
        Object[] values = map.players.values().toArray();
        for(int i = 0; i < chars.length; i++) {
            s += "\n" + chars[i] + " - " + ((Player)values[i]).username;
        }
        return s + "```";
    }
    
    public static void bossAttack(int id) {
        int size = BossBattle.getSize(id);
        ArrayList<Player> players = BossBattle.rooms.get(id);
        Map map = battles.get(id);
        
        for (int i = 0; i < size; i++) {    //only go through the bosses
            Boss boss = (Boss)players.get(i);
            if(deadBosses.get(boss.bossid).contains(boss)) {        //this boss is dead, they don't get a turn
                continue;
            }
            ArrayList<Player> availablePlayers = new ArrayList<>(3);
            
            //MOVE RANGE - ABLE TO COMBAT OR HEAL
            ArrayList<Pair> movableSpaces = new ArrayList<>(12);
            Pair originalLocation = map.getLocation(map.playerValues.get(boss));        //this is in comp
            
            //BFS to find movableSpaces
            Queue<int[]> queue = new LinkedList<>();
            boolean[][] visited = new boolean[8][6];
            for (int k = 0; k < 8; k++) {
                for (int j = 0; j < 6; j++) {
                    visited[k][j] = false;
                }
            }
            int[] start = {originalLocation.x, originalLocation.y, 0};      //this is in comp
            queue.add(start);
            visited[originalLocation.x][originalLocation.y] = true;     //don't come back to the start
            movableSpaces.add(new Pair(originalLocation.x, originalLocation.y));        //try attacking without moving, first
            
            while (true) {
                int[] array = queue.remove();
                if(array[2] > 1) {      //2 cannot run because it will add 3's in.
                    break;
                }
                for (int k = -1; k <= 1; k++) {
                    for (int j = -1; j <= 1; j++) {
                        if(array[0] + k < 0 || array[0] + k > 7 || array[1] + j < 0 || array[1] + j > 5) {      //invalid location- these are array locations, not Cartesian
                            continue;
                        }
                        if (Math.abs(k + j) == 1 && map.grid[array[0] + k][array[1] + j] == '.' && visited[array[0] + k][array[1] + j] == false) {
                            visited[array[0] + k][array[1] + j] = true;
                            int[] temp = {array[0] + k, array[1] + j, array[2] + 1};
                            queue.add(temp);
                            movableSpaces.add(new Pair(array[0] + k, array[1] + j));
                        }
                    }
                }
            }
            
            //Find all players in range of movableSpaces
            HashMap<Player, Pair> attackLocations = new HashMap<>(3);       //player --> location (in comp)
            boolean healed = false;
            for(Pair p: movableSpaces) {
                if(healed) {        //if healer found someone to heal, end their turn
                    break;
                }
                //HEALER
                if (boss.weapon.staff) {
                    Player toAction = null;
                    Pair toMove = null;
                    int minHP = Integer.MAX_VALUE;
                    for (int k = 0; k < size; k++) {
                        Player current = players.get(k);
                        if (current.stats.chp < current.stats.thp && k != i && current.stats.chp > 0 && map.getDistance(current, p) <= boss.weapon.range) {
                            //healer can't heal themselves, nor a boss that just attacked and killed themselves, and they must be in range
                            if (current.stats.chp < minHP) {        //healer will attempt to heal the boss with the lowest health
                                minHP = current.stats.chp;
                                toAction = current;
                                toMove = p;
                            }
                        }
                    }
                    if (minHP != Integer.MAX_VALUE) {   //someone requires healing- if no one, healer continues the attack
                        toAction.stats.chp += boss.weapon.mt + boss.stats.mag;
                        int overflow = toAction.stats.chp - toAction.stats.thp;
                        if (overflow > 0) {
                            toAction.stats.chp = toAction.stats.thp;
                            Attack.heals.put(boss.authorid, Utilities.bold(boss.username) + " healed " + Utilities.bold(toAction.username)
                                + " for " + (boss.weapon.mt + boss.stats.mag - overflow) + " HP." + "\n\n" + Utilities.bold(toAction.username + "'s HP: ")
                                + toAction.stats.chp + "/" + toAction.stats.thp);
                        }
                        else {
                            Attack.heals.put(boss.authorid, Utilities.bold(boss.username) + " healed " + Utilities.bold(toAction.username)
                                + " for " + (boss.weapon.mt + boss.stats.mag) + " HP." + "\n\n" + Utilities.bold(toAction.username + "'s HP: ")
                                + toAction.stats.chp + "/" + toAction.stats.thp);
                        }
                        map.setLocation(map.playerValues.get(boss), toMove);
                        healed = true;      //stop interating through pairs
                        continue;   //do not let them attack as well
                    }
                }
                //NON-HEALER
                int range = boss.weapon.range;
                for(int j = -range; j <= range; j++) {
                    for(int k = -range; k <= range; k++) {
                        if(p.x + j < 0 || p.x + j > 7 || p.y + k < 0 || p.y + k > 5) {      //invalid location- these are array locations, not Cartesian
                            continue;
                        }
                        if(Math.abs(j) + Math.abs(k) <= range && "AB".contains(map.grid[p.x + j][p.y + k] + "")) {      //location contains A or B
                            Player victim = map.players.get(map.grid[p.x + j][p.y + k]);
                            if(!availablePlayers.contains(victim)) {     //if they aren't already in the list
                                availablePlayers.add(victim);       //add that player to list
                                attackLocations.put(victim, p);     //victim was attacked from the current p
                            }
                        }
                    }
                }
            }
            if(healed) {        //if healer found someone to heal, continue onto next boss
                continue;
            }
            if(!availablePlayers.isEmpty()) {
                Player toAttack = findBest(availablePlayers, boss);
                AttackPreview.battleCalc(boss, toAttack, boss.weapon.physical, true);       //prep a calculation against toAttack
                if(!map.setLocation(map.playerValues.get(boss), attackLocations.get(toAttack))) {       //try to move the boss to the location to attack
                    System.out.println(boss.username + " was unable to move to " + attackLocations.get(toAttack));
                }     
                continue;       //move on to next boss
            }
            
            //IF NO ONE IS WITHIN MOVE+COMBAT RANGE - availablePlayers is empty
            for (int j = size; j < size * 2; j++) {
                availablePlayers.add(players.get(j));       //adds all players to availablePlayers
            }
            ArrayList<Pair> path = findPath(map, boss, findBest(availablePlayers, players.get(i)));     //find move path
            int index = 3;      //path.size() - 1 is the last item, which is the original node. Therefore, path.size() - 3 is correct.
            while(!map.setLocation(map.playerValues.get(boss), path.get(path.size() - index))) {       //try to move to location- if false, that space is occupied
                if(index == 0) {
                    break;      //welp, guess they can't move at all.
                }
                i--;        //attempt to move to a location one mov closer
            }
            bossMove.get(((Boss)boss).bossid).add(boss);
        }
        Attack.bossAttack = true;
    }

    //find the path toward the best enemy, and return the location to move to.
    private static ArrayList<Pair> findPath(Map map, Player b, Player toAttack) {      
        Queue<Pair> queue = new LinkedList<>();
        Pair originalLocation = map.getLocation(map.playerValues.get(b));
        boolean[][] visited = new boolean[8][6];
        Pair[][] last = new Pair[8][6];     //stores where the path came from
        last[originalLocation.x][originalLocation.y] = new Pair(-1, -1);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 6; j++) {
                visited[i][j] = false;
            }
        }
        queue.add(new Pair(originalLocation.x, originalLocation.y));
        visited[originalLocation.x][originalLocation.y] = true;     //don't come back to the start
        
        while(queue.peek() != null) {
            Pair p = queue.remove();
            
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    if(p.x + i < 0 || p.x + i > 7 || p.y + j < 0 || p.y + j > 5) {      //invalid location
                        continue;
                    }
                    if(Math.abs(i + j) == 1 && visited[p.x + i][p.y + j] == false) {
                        if(map.grid[p.x + i][p.y + j] == '#') {     //ignore walls
                            continue;
                        }
                        if(map.grid[p.x + i][p.y + j] == map.playerValues.get(toAttack)) {      
                            ArrayList<Pair> path = new ArrayList<>(12);                         //the path is missing the target node
                            int x = p.x, y = p.y;
                            path.add(new Pair(x, y));
                            while(last[x][y].x != -1 && last[x][y].y != -1) {       //it will have just inserted the last node
                                path.add(last[x][y]);
                                int oldx = x;
                                x = last[x][y].x;
                                y = last[oldx][y].y;
                            }
                            return path;       
                        }
                        else {
                            visited[p.x + i][p.y + j] = true;
                            last[p.x + i][p.y + j] = new Pair(p.x, p.y);        //the next node will point to this node
                            queue.add(new Pair(p.x + i, p.y + j));
                        }
                    }
                }
            }
        }
        ArrayList<Pair> no = new ArrayList<>();
        no.add(new Pair(-100, -100));
        return no;    //impossible to find a path
    }
    
    private static Player findBest(ArrayList<Player> players, Player b) {
        Player toAction = players.get(0); //default, attack the first person

        int highestrating = Integer.MIN_VALUE;
        boolean bossphysical = b.weapon.physical;

        for (Player p : players) {
            int currentrating = 0;
            boolean playerphysical = p.weapon.physical;
            int dmg = (bossphysical ? b.stats.str : b.stats.mag) - (bossphysical ? p.stats.def : p.stats.res);
            currentrating += (dmg > p.stats.chp) ? p.stats.chp : dmg;
            int dmgTaken = (playerphysical ? p.stats.str : p.stats.mag) - (playerphysical ? b.stats.def : b.stats.res);
            currentrating += (dmgTaken > b.stats.chp) ? 0 : (b.stats.chp - dmgTaken);
            if (currentrating > highestrating) {
                highestrating = currentrating;
                toAction = p;
            }
        }
        return toAction;
    }
    
    public static int getSize(int id) {
        switch(id){
            case 0:     //Cyrus 
                return 2;
            default:
                return -1;
        }
    }
    
    public static String getName(int id) {
        switch(id){
            case 0:     
                return "Cyrus";
            default:
                return "";
        }
    }
    
    public static Weapon getReward(int id) {
        switch(id){
            case 0:     
                return Weapon.BraveAxe;
            default:
                return Weapon.NA;
        }
    }

}
