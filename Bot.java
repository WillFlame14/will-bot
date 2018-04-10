package bot.willbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.awt.Color;
import java.util.*;
import java.io.*;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class Bot extends ListenerAdapter {
    static JDA jda;
    static HashMap<String, Player> playermap = new HashMap<>();     //username --> player
    static HashMap<Long, Player> defaultPlayer = new HashMap<>();   //user --> defaultPlayer
    static HashMap<String, Long> idmap = new HashMap<>();       //username --> authorid
    static HashMap<String, AttackSave> attacksaves = new HashMap<>();       //attacker's username --> battle stats
    static HashMap<Long, String> attackusers = new HashMap<>();     //authorid --> player + " " + enemy
    static HashMap<Long, String> enemyattackusers = new HashMap<>();     //authorid --> player + " " + enemy
    static HashMap<String, Players> enemySave = new HashMap<>();      //player + " " + enemy --> saved player stats, saved enemy stats
    static LinkedList<Category> categories = new LinkedList<>();    
    static MessageEmbed changelog, helpEmbed; 
    static EmbedBuilder statsEmbed = new EmbedBuilder();
    static HashMap<Long, Boolean> calculated = new HashMap<>();
    
    public Bot() throws Exception {
        jda = new JDABuilder(AccountType.BOT).setToken("NDIyNDgxMzM3MzE2ODAyNTYw.DYcaBg.AQrb8xn6vR9DXt2dwEE9pEqXE4k").buildBlocking();
        jda.addEventListener(this);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        String m = event.getMessage().getContentDisplay();
        String[] parts = m.split(" ");
        boolean valid = false;
        MessageChannel c = event.getChannel();
        if(!m.startsWith("w!")) {       //no command
            return;
        }
//        c.addReactionById(event.getMessageId(), "✅").queue();
        
        ArrayList<String> args = new ArrayList<>();
        if(parts.length > 1) {
            for(int i = 0; i < parts.length - 1; i++) {
                args.add(parts[i + 1]);
            }
        }
        
        if(calculated.containsKey(event.getAuthor().getIdLong()) && calculated.get(event.getAuthor().getIdLong()) && !parts[0].equals("w!confirm")) {        //true, but wasn't confirmed
            calculated.replace(event.getAuthor().getIdLong(), false);
        }
        
        try {
            for(Category category:categories) {
                if(category.isActionApplicable(parts[0])) {
                    valid = true;
                    category.response(parts[0], args, event);
                    break;
                }
            }
        }
        catch(ValidationException e) {
            c.sendMessage(e.getMessage()).queue();
        }
        
        if(!valid) {
            c.sendMessage("Your command was not recognized. Use `w!help` for help.").queue();
        }
    }
    
    public static void main(String[] args) {
        init();
        try {
            new Bot();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void init() {
        try {       //reading from file
            Scanner config = new Scanner(new File("config.txt"));
            while(config.hasNextLine()) {
                String[] temp = config.nextLine().split(",.,");
                if(temp[0].contains("*g^")) {       //defaultPlayer stuff, everything else should be done by now
                    defaultPlayer.put(Long.parseLong(temp[0].substring(3)), playermap.get(temp[1]));
                }
                else {
                    if(temp[0].contains("Fighter")) {
                        continue;
                    }
                    Weapon weapon = Weapon.valueOf(temp[20]);
                    Skill skill = Skill.valueOf(temp[temp.length - 2]);
                    long authorid = Long.parseLong(temp[temp.length - 1]);
                    int[] stats = new int[11];
                    int[] growths = new int[8];
                    int[] weaponRanks = new int[14];
                    for(int i = 1; i < 12; i++) {
                        stats[i - 1] = Integer.parseInt(temp[i]);
                    }
                    for(int i = 12; i < 20; i++) {
                        growths[i - 12] = Integer.parseInt(temp[i]);
                    }
                    for(int i = 21; i < 35; i++) {
                        weaponRanks[i - 21] = Integer.parseInt(temp[i]);
                    }
                    playermap.put(temp[0], new Player(temp[0], new Stats(stats), new Growths(growths), weapon, new WeaponRanks(weaponRanks), skill, authorid));
                    idmap.put(temp[0], authorid);
                    calculated.put(authorid, false);
                }
            }
        }
        catch(FileNotFoundException e) {
        }
        
        EmbedBuilder changelogEmbed = new EmbedBuilder();
        changelogEmbed.setTitle("Solace Changelog", null);
        changelogEmbed.setColor(Color.red);
        changelogEmbed.setDescription("To see a list of planned features, use w!planned.");
        changelogEmbed.addField("v1.13: The Combat Update", "- An **enemy turn** has been added.\n"
                + "\t- The enemy will **always retaliate** if neither units are dead after initial combat.\n"
                + "- The combat system has been reworked (no visible effects).\n"
                + "\t- Everything should be a lot smoother going forward.\n"
                + "- Stratum enemies are now **limited to lv 1000**.\n"
                + "\t- Generating higher-level enemies could hang the bot given the badly optimized code.\n"
                + "- You are now **forced to re-calculate** if either your character or the enemy changes between calculation and conformation.\n"
                + "- Player comparisons now work properly.\n"
                + "- Stats for stratum enemies now display correctly.\n"
                + "- Healing now correctly awards weapon XP.\n"
                + "- Characters are now able to attack without confirming their previous action.\n"
                + "- Skills that are permanently on no longer randomly display activation in combat.", true);
        changelogEmbed.addBlankField(true);
        changelogEmbed.addField("v1.12: The Weapon Ranks Update", "- Many more weapons (**Bronze**, **Steel**, **Brave** variants) have been added.\n"
                + "\t- The shop has been updated to contain more information.\n"
                + "- **Weapon ranks** have been added. Weapons now require a certain rank to wield.\n"
                + "\t- Use **w!weaponranks** to show your weapon ranks.\n"
                + "- **Default character selection** has been added.\n"
                + "\t- Use **w!select <user>** to auto-fill all future <user> tags.\n"
                + "- The **Blossom** and **Discipline** skills have been added.", true);
        changelogEmbed.addBlankField(true);
        changelogEmbed.addField("v1.11: The Hit Chance Update", "- **Hit Rates** have been added.\n"
                + "\t- Have fun missing 99% hits and getting critted with a 1% chance.\n"
                + "- Many skills have been added.\n"
                + "\t- These include **Aptitude**, **Fortune**, **Paragon** and **Pavise**.\n"
                + "- **Tomes** (**Fire**, **Wind**, **Thunder**) have been added.\n"
                + "- **Training stratums** have been introuced.\n"
                + "\t- Use these by replacing the enemy's name in an attack with **\"stratum+<level>\"**.\n"
                + "- The heal for infinite HP bug has been quashed.\n", true);
        changelogEmbed.setFooter("Created by WillFlame#5739", null);
        changelog = changelogEmbed.build();
        
        EmbedBuilder help = new EmbedBuilder();
        help.setTitle("Help", null);
        help.setColor(Color.blue);
        help.setDescription("**Solace v1.12** - The Weapon Ranks Update");
        help.addField("Commands", "**w!register <username>** - Registers a user."
                + "\n**w!select <user>** - Auto-fills <user>."
                + "\n**w!stats <user>** - Displays stats."
                + "\n**w!reroll <user>** - Rerolls stats."
                + "\n**w!weaponranks <user>** - Displays weapon ranks."
                + "\n**w!users** - Displays a list of registered users.\n"
                + "\n**w!weapons** - Shows a list of weapons."
                + "\n**w!equip <user> <weapon>** - Equips a weapon."
                + "\n**w!unequip <user>** - Unequips a weapon.\n"
                + "\n**w!skills** - Shows a list of skills."
                + "\n**w!assign <user> <skill>** - Assigns a skill."
                + "\n**w!remove <user>** - Removes a skill.\n"
                + "\n**w!attackp <user> <enemy>** - Attacks an enemy physically."
                + "\n**w!attackm <user> <enemy>** - Attacks an enemy magically."
                + "\n**w!confirm** - Confirms the attack. Only used immediately after w!attackp or w!attackm."
                + "\n**w!heal <user> <recipient>** - Heals the recipient. Requires a staff equipped.\n"
                + "\nTo train against stratum units, replace the enemy name with \"stratum\"+<level>."
                + "\nStratum enemies can be then attacked using their name.\n" 
                + "\n**w!ping** - Pong!"
                + "\n**w!roll** - Rolls a 6-sided die.", true);
        help.setFooter("Created by @WillFlame#5739", null);
        helpEmbed = help.build();
        
        Stats g = new Stats();
        AttackPreview gg = new AttackPreview();
        AttackConfirm ggg = new AttackConfirm();
        Heal gggg = new Heal();
        BotSystem ggggg = new BotSystem();
        
        categories.add(Skill.NA);
        categories.add(g);
        categories.add(gg);
        categories.add(ggg);
        categories.add(Weapon.NA);
        categories.add(gggg);
        categories.add(ggggg);
        Utilities.init();
    }
    
    public static void update() {
        try {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("config.txt"), "utf-8"))) {
                Object[] players = playermap.values().toArray();
                for (Object player : players) {
                    Player p = (Player) player;
                    if(p.username.contains("Fighter")) {
                        continue;
                    }
                    writer.write(p.toString());
                    writer.newLine();
                }
                Object[] defaults1 = defaultPlayer.keySet().toArray();
                Object[] defaults2 = defaultPlayer.values().toArray();
                for(int i = 0; i < defaults1.length - 1; i++) {
                    Player p = (Player)defaults2[i];
                    writer.write("*g^" + defaults1[i] + ",.," + p.username);
                    writer.newLine();
                }
                if(!defaultPlayer.isEmpty()) {
                    writer.write("*g^" + defaults1[defaults1.length - 1] + ",.," + ((Player)defaults2[defaults2.length - 1]).username);
                }
            }
        }
        catch(IOException e) {
            System.out.println("how in the world did you get this to happen");
        }
    }
}
