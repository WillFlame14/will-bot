package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Weapon implements Category{  //mt, wt, crt, accuracy, physical, staff
    NA("NA", Colour.COLOURLESS, 0, 0, 0, 0, false, false),
    Fist("Fist", Colour.COLOURLESS, 0, 1, 0, 100, true, false),
    IronLance("Iron Lance", Colour.BLUE, 7, 9, 0, 85, true, false), 
    IronAxe("Iron Axe", Colour.GREEN, 8, 11, 0, 80, true, false), 
    IronSword("Iron Sword", Colour.RED, 6, 7, 0, 90, true, false),
    SteelLance("Steel Lance", Colour.BLUE, 10, 13, 0, 80, true, false), 
    SteelAxe("Steel Axe", Colour.GREEN, 11, 15, 0, 75, true, false),
    SteelSword("Steel Sword", Colour.RED, 9, 11, 0, 85, true, false),
    BraveLance("Brave Lance", Colour.BLUE, 10, 11, 0, 85, true, false), 
    BraveAxe("Brave Axe", Colour.GREEN, 11, 13, 0, 80, true, false),
    BraveSword("Brave Sword", Colour.RED, 9, 9, 0, 90, true, false),
    Fire("Fire", Colour.COLOURLESS, 5, 3, 0, 90, false, false),
    Wind("Wind", Colour.COLOURLESS, 4, 2, 0, 95, false, false),
    Thunder("Thunder", Colour.COLOURLESS, 3, 4, 5, 80, false, false),
    Elfire("Elfire", Colour.COLOURLESS, 7, 5, 0, 85, false, false),
    Elwind("Elwind", Colour.COLOURLESS, 6, 4, 0, 90, false, false),
    Elthunder("Elthunder", Colour.COLOURLESS, 5, 6, 10, 75, false, false),
    Heal("Heal", Colour.COLOURLESS, 5, 2, 5, 100, false, true),
    Mend("Mend", Colour.COLOURLESS, 10, 4, 10, 100, false, true);
    
    String displayName;
    Colour colour;
    int mt, wt, crt, accuracy;
    boolean physical;
    boolean staff;
    
    Weapon(String displayName, Colour colour, int mt, int wt, int crt, int accuracy, boolean physical, boolean staff) {
        this.displayName = displayName;
        this.colour = colour;
        this.mt = mt;
        this.wt = wt;
        this.crt = crt;
        this.accuracy = accuracy;
        this.physical = physical;
        this.staff = staff;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!equip") || action.equals("w!unequip") || action.equals("w!weapons");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(action.equals("w!equip")) {        //w!equip <user> <weapon>
            if(args.size() < 2) {    
                c.sendMessage("You did not provide enough arguments. The correct format is `w!equip <user> <weapon>`").queue();
                return;
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            Player user = Bot.playermap.get(args.get(0));
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
                user.weapon = Weapon.valueOf(args.get(1));
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
            if(args.isEmpty()) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!unequip <user>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            Player user = Bot.playermap.get(args.get(0));
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
            c.sendMessage("```Weapons that are currently usable:\n"
                    + "Iron Sword - 6 MT\n"
                    + "Iron Axe - 8 MT\n"
                    + "Iron Lance - 7 MT\n"
                    + "Fire - 5 MT\n"
                    + "Wind - 4 MT\n"
                    + "Thunder - 3 MT\n"
                    + "Heal - Restores HP equal to 5 + user's Mag```").queue();
        }
    }
}
