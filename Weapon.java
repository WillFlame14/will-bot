package bot.willbot;

import java.util.ArrayList;
import java.util.HashMap;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Weapon implements Category{
    NA("NA", Colour.COLOURLESS, 0, false, false),
    Fist("Fist", Colour.COLOURLESS, 0, true, false),
    IronLance("Iron Lance", Colour.BLUE, 7, true, false), 
    IronAxe("Iron Axe", Colour.GREEN, 8, true, false), 
    IronSword("Iron Sword", Colour.RED, 6, true, false),
    SteelLance("Steel Lance", Colour.BLUE, 10, true, false), 
    SteelAxe("Steel Axe", Colour.GREEN, 11, true, false),
    SteelSword("Steel Sword", Colour.RED, 9, true, false),
    Heal("Heal", Colour.COLOURLESS, 5, false, true),
    Mend("Mend", Colour.COLOURLESS, 10, false, true);
    
    String displayName;
    Colour colour;
    int mt;
    boolean physical;
    boolean staff;
    
    Weapon(String displayName, Colour colour, int mt, boolean physical, boolean staff) {
        this.displayName = displayName;
        this.colour = colour;
        this.mt = mt;
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
                    + "Heal - Restores HP equal to 5 + user's Mag```").queue();
        }
    }
}
