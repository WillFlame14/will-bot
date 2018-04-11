package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BossBattle implements Category{
    static HashMap<Integer, ArrayList<Player>> rooms = new HashMap<>();
    static HashSet<Player> inboss = new HashSet<>();
    static ArrayList<ArrayList<Player>> turnover = new ArrayList<>();
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!boss") || action.equals("w!join");
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
                    players.add(Bot.bosses.get(0));  //Cyrus
                    players.add(Bot.bosses.get(1));  //Troubadour
                    players.add(Bot.playermap.get(args.get(1)));
                    if(args.size() == 2) {      //no one is added to inboss yet.
                        rooms.put(0, players);
                        throw new ValidationException("A room has been created. Other users can join this room with `w!join Cyrus`.");
                    }
                    players.add(Bot.playermap.get(args.get(2)));
                    rooms.put(0, players);      //overwrite
                    inboss.add(Bot.playermap.get(args.get(1)));     //remember, args.get(0) is the id
                    inboss.add(Bot.playermap.get(args.get(2)));
                    turnover.add(0, new ArrayList<>());
                    c.sendMessage("Entering boss battle.").queue();
                    c.sendMessage(Bot.bosses.get(0).showStats().build()).queue();
                    c.sendMessage(Bot.bosses.get(1).showStats().build()).queue();
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
            switch(args.get(0)) {
                case "1":
                case "Cyrus":
                    id = 0;
                    break;
                default:
                    throw new ValidationException("You did not specify a valid boss name.");
            }
            
            if(!rooms.containsKey(id)) {
                throw new ValidationException("The room has not been created yet. Use `w!boss` to create one.");
            }       //size() can be used since initalizing determines the capacity, not the size
            if(rooms.get(id).size() + args.size() - 1 > getSize(id) * 2) {        //subtract 1 so the id/name is not included
                throw new ValidationException("You provided too many characters. The room only has " + ((getSize(id) * 2) - rooms.get(id).size()) + " spaces left.");
            }
            for(int i = 1; i < args.size(); i++) {
                Utilities.checkPlayer(args.get(i), event.getMessage().getAuthor().getIdLong());
            }
            
            for(int i = 1; i < args.size(); i++) {
                Player p = Bot.playermap.get(args.get(i));
                if(rooms.get(id).contains(p)) {
                    c.sendMessage(p.username + " is already in the room!").queue();
                    continue;
                }
                c.sendMessage(p.username + " has joined the room!").queue();
                rooms.get(id).add(p);
            }
            
            if(rooms.get(id).size() == getSize(id) * 2) {
                c.sendMessage("Room is full. Entering boss battle.").queue();
                for(int i = getSize(id); i < rooms.get(id).size(); i++) {         //add all players into inboss
                    inboss.add(rooms.get(id).get(i));
                }
                turnover.add(id, new ArrayList<>());     //create turnover
            }
            else {
                int remain = (getSize(id) * 2) - rooms.get(id).size();
                c.sendMessage("The room is not full yet. There " + (remain == 1?"is ":"are ") + remain + " space" + (remain == 1?"":"s") + " left.").queue();
            }
        }
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
