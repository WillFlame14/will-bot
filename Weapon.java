package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Weapon implements Category{  //mt, wt, crt, accuracy, WEx, rank, physical, staff
    NA("NA", Colour.COLOURLESS, 0, 0, 0, 0, 0, 0, false, false),
    Fist("Fist", Colour.COLOURLESS, 0, 1, 0, 100, 0, 0, true, false),
    BronzeLance("Bronze Lance", Colour.BLUE, 4, 6, -100, 90, 2, 0, true, false), 
    BronzeAxe("Bronze Axe", Colour.GREEN, 5, 7, -100, 85, 2, 0, true, false), 
    BronzeSword("Bronze Sword", Colour.RED, 3, 5, -100, 95, 2, 0, true, false),
    IronLance("Iron Lance", Colour.BLUE, 7, 9, 0, 85, 2, 1, true, false), 
    IronAxe("Iron Axe", Colour.GREEN, 8, 11, 0, 80, 2, 1, true, false), 
    IronSword("Iron Sword", Colour.RED, 6, 7, 0, 90, 2, 1, true, false),
    SteelLance("Steel Lance", Colour.BLUE, 10, 13, 0, 80, 3, 2, true, false), 
    SteelAxe("Steel Axe", Colour.GREEN, 11, 15, 0, 75, 3, 2, true, false),
    SteelSword("Steel Sword", Colour.RED, 9, 11, 0, 85, 3, 2, true, false),
    BraveLance("Brave Lance", Colour.BLUE, 10, 11, 0, 85, 2, 4, true, false), 
    BraveAxe("Brave Axe", Colour.GREEN, 11, 13, 0, 80, 2, 4, true, false),
    BraveSword("Brave Sword", Colour.RED, 9, 9, 0, 90, 2, 4, true, false),
    Fire("Fire", Colour.COLOURLESS, 5, 3, 0, 90, 1, 0, false, false),
    Wind("Wind", Colour.COLOURLESS, 4, 2, 0, 95, 1, 0, false, false),
    Thunder("Thunder", Colour.COLOURLESS, 3, 4, 5, 80, 1, 0, false, false),
    Elfire("Elfire", Colour.COLOURLESS, 7, 5, 0, 85, 2, 1, false, false),
    Elwind("Elwind", Colour.COLOURLESS, 6, 4, 0, 90, 2, 1, false, false),
    Elthunder("Elthunder", Colour.COLOURLESS, 5, 6, 10, 75, 2, 2, false, false),
    Heal("Heal", Colour.COLOURLESS, 5, 2, 5, 100, 3, 0, false, true),
    Mend("Mend", Colour.COLOURLESS, 10, 4, 10, 100, 5, 1, false, true);
    
    String displayName;
    Colour colour;
    int mt, wt, crt, wex, rank, accuracy;
    boolean physical;
    boolean staff;
    
    Weapon(String displayName, Colour colour, int mt, int wt, int crt, int accuracy, int wex, int rank, boolean physical, boolean staff) {
        this.displayName = displayName;
        this.colour = colour;
        this.mt = mt;
        this.wt = wt;
        this.crt = crt;
        this.accuracy = accuracy;
        this.wex = wex;
        this.rank = rank;
        this.physical = physical;
        this.staff = staff;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!equip") || action.equals("w!unequip") || action.equals("w!weapons");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        Player user;
        if(action.equals("w!equip")) {        //w!equip <user> <weapon>
            if(args.size() < 2 && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {    
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(0, user.username);
            }
            else if(args.size() < 2) {
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!equip <user> <weapon>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = Bot.playermap.get(args.get(0));
            if(user.weapon != Weapon.Fist) {        //remove current weapon first
                if (user.weapon.physical) {
                    user.stats.str -= user.weapon.mt;
                } else {
                    if (!user.weapon.staff) {        //staves do not provide Mag
                        user.stats.mag -= user.weapon.mt;
                    }
                }
            }
            try {
                Weapon w = Weapon.valueOf(args.get(1));
                if(getRankLevel(user, w) < w.rank) {
                    throw new ValidationException(user.username + " does not have sufficient weapon rank to wield that weapon.");
                }
                user.weapon = w;
                if(user.weapon.physical) {
                    user.stats.str += user.weapon.mt;
                }
                else {
                    if(!user.weapon.staff) {        //staves do not provide Mag
                        user.stats.mag += user.weapon.mt;
                    }
                }
                c.sendMessage("Your weapon has been equipped.").queue();
                Bot.update();
            }
            catch(IllegalArgumentException e) {     //attempted to equip non-existing weapon
                throw new ValidationException("You did not specify a valid weapon to equip. Remember that weapon names should not contain spaces.");
            }
        }
        else if(action.equals("w!unequip")) {      //w!unequip <user>
            if(args.isEmpty() && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(user.username);
            }
            else if(args.isEmpty()) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!unequip <user>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = Bot.playermap.get(args.get(0));
            if(user.weapon.equals(Weapon.Fist)) {
                throw new ValidationException("You do not currently have a weapon equipped.");
            }
            if (user.weapon.physical) {
                user.stats.str -= user.weapon.mt;
            } else {
                if (!user.weapon.staff) {        //staves do not provide Mag
                    user.stats.mag -= user.weapon.mt;
                }
            }
            user.weapon = Weapon.Fist;
            c.sendMessage("Your weapon has been successfully unequipped.").queue();
            Bot.update();
        }
        else if(action.equals("w!weapons")) {
            Weapon[] weapons = Weapon.values();
            String s = "```List of Weapons:";
            for(int i = 2; i < weapons.length; i++) {
                Weapon w = weapons[i];
                if(w.rank != weapons[i - 1].rank) {
                    s += "\n";
                }
                if(!w.physical && weapons[i - 1].physical) {
                    s += "\n";
                }
                if(w.staff && !weapons[i - 1].staff) {
                    s += "\n";
                }
                s += "\n[" + WeaponRanks.toRank(w.rank) + "] " + w.displayName + " - " + w.mt + " Mt, " + w.wt + " Wt, " + w.crt + " Crt, " + w.accuracy + " Hit";
            }
            s += "```";
            c.sendMessage(s).queue();
        }
    }
    
    public int getRankLevel(Player p, Weapon w) {
        switch(w.colour) {
            case RED:
                return p.ranks.sword;
            case GREEN:
                return p.ranks.axe;
            case BLUE:
                return p.ranks.lance;
            default:
                if(w.staff) {
                    return p.ranks.staff;
                }
                if(w.displayName.toLowerCase().contains("fire")) {
                    return p.ranks.fire;
                }
                if(w.displayName.toLowerCase().contains("wind")) {
                    return p.ranks.wind;
                }
                if(w.displayName.toLowerCase().contains("thunder")) {
                    return p.ranks.thunder;
                }
        }
        System.out.println("you dun goofed again somehow");
        return -1;
    }
}
