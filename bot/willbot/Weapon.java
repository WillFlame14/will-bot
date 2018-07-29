package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Weapon implements Category{  //mt, wt, crt, accuracy, WEx, rank, physical, range, available classes
    NA("NA", WeaponType.NA, 0, 0, 0, 0, 0, 0, false, 0),
    Fist("Fist", WeaponType.NA, 0, 1, 0, 100, 0, 0, true, 1),
    BronzeLance("Bronze Lance", WeaponType.Lance, 4, 6, -100, 90, 2, 0, true, 1), 
    BronzeAxe("Bronze Axe", WeaponType.Axe, 5, 7, -100, 85, 2, 0, true, 1), 
    BronzeSword("Bronze Sword", WeaponType.Sword, 3, 5, -100, 95, 2, 0, true, 1),
    IronLance("Iron Lance", WeaponType.Lance, 7, 9, 0, 85, 2, 1, true, 1), 
    IronAxe("Iron Axe", WeaponType.Axe, 8, 11, 0, 80, 2, 1, true, 1), 
    IronSword("Iron Sword", WeaponType.Sword, 6, 7, 0, 90, 2, 1, true, 1),
    SteelLance("Steel Lance", WeaponType.Lance, 10, 13, 0, 80, 3, 2, true, 1), 
    SteelAxe("Steel Axe", WeaponType.Axe, 11, 15, 0, 75, 3, 2, true, 1),
    SteelSword("Steel Sword", WeaponType.Sword, 9, 11, 0, 85, 3, 2, true,  1),
    BraveLance("Brave Lance", WeaponType.Lance, 10, 11, 0, 85, 2, 4, true, 1), 
    BraveAxe("Brave Axe", WeaponType.Axe, 11, 13, 0, 80, 2, 4, true, 1),
    BraveSword("Brave Sword", WeaponType.Sword, 9, 9, 0, 90, 2, 4, true, 1),
    Fire("Fire", WeaponType.Fire, 5, 3, 0, 90, 1, 0, false, 2),
    Wind("Wind", WeaponType.Wind, 4, 2, 0, 95, 1, 0, false, 2),
    Thunder("Thunder", WeaponType.Thunder, 3, 4, 5, 80, 1, 0, false, 2),
    Elfire("Elfire", WeaponType.Fire, 7, 5, 0, 85, 2, 1, false, 2),
    Elwind("Elwind", WeaponType.Wind, 6, 4, 0, 90, 2, 1, false, 2),
    Elthunder("Elthunder", WeaponType.Thunder, 5, 6, 10, 75, 2, 2, false, 2),
    Heal("Heal", WeaponType.Staff, 5, 2, 5, 100, 3, 0, false, 1),
    Mend("Mend", WeaponType.Staff, 10, 4, 10, 100, 5, 1, false, 1),
    Physic("Physic", WeaponType.Staff, 1, 5, 25, 100, 7, 2, false, 5);
    
    String displayName;
    WeaponType weaponType;
    int mt, wt, crt, wex, rank, accuracy, range;
    boolean physical;
    
    Weapon(String displayName, WeaponType weaponType, int mt, int wt, int crt, int accuracy, int wex, int rank, boolean physical, int range) {
        this.displayName = displayName;
        this.weaponType = weaponType;
        this.mt = mt;
        this.wt = wt;
        this.crt = crt;
        this.accuracy = accuracy;
        this.wex = wex;
        this.rank = rank;
        this.physical = physical;
        this.range = range;
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
                    if (user.weapon.weaponType != WeaponType.Staff) {        //staves do not provide Mag
                        user.stats.mag -= user.weapon.mt;
                    }
                }
            }
            try {
                Weapon w = Weapon.valueOf(args.get(1));
                if(getRankLevel(user, w) < w.rank) {
                    throw new ValidationException(user.username + " does not have sufficient weapon rank to wield that weapon.");
                }
                if(!user.pclass.usableWeapons.contains(w.weaponType)) {
                    throw new ValidationException(user.username + " cannot wield that weapon as a result of their class.");
                }
                user.weapon = w;
                if(user.weapon.physical) {
                    user.stats.str += user.weapon.mt;
                }
                else {
                    if(user.weapon.weaponType != WeaponType.Staff) {        //staves do not provide Mag
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
                if (user.weapon.weaponType != WeaponType.Staff) {        //staves do not provide Mag
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
                if(w.weaponType == WeaponType.Staff && weapons[i - 1].weaponType != WeaponType.Staff) {
                    s += "\n";
                }
                s += "\n[" + WeaponRanks.toRank(w.rank) + "] " + w.displayName + " - " + w.mt + " Mt, " + w.wt + " Wt, " + w.crt + " Crt, " + w.accuracy + " Hit";
            }
            s += "```";
            c.sendMessage(s).queue();
        }
    }
    
    public int getRankLevel(Player p, Weapon w) {
        switch(w.weaponType) {
            case Sword:
                return p.ranks.sword;
            case Axe:
                return p.ranks.axe;
            case Lance:
                return p.ranks.lance;
            case Staff:
                return p.ranks.staff;
            case Fire:
                return p.ranks.fire;
            case Wind:
                return p.ranks.wind;
            case Thunder:
                return p.ranks.thunder;
        }
        System.out.println("you dun goofed again somehow");
        return -1;
    }
}

enum WeaponType{
    NA, Sword, Lance, Axe, Fire, Wind, Thunder, Staff, Knife, Bow;
}
