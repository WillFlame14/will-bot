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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.simple.JSONValue;

public class Bot extends ListenerAdapter {
    static JDA jda;
    static HashMap<String, Player> playermap = new HashMap<>();     //username --> player
    static HashMap<Long, Player> defaultPlayer = new HashMap<>();   //user --> defaultPlayer
    static HashMap<String, Long> idmap = new HashMap<>();           //username --> authorid
    static HashMap<String, AttackSave> attacksaves = new HashMap<>();       //attacker's username --> battle stats
    static HashMap<Long, String> attackusers = new HashMap<>();     //authorid --> player + " " + enemy
    static HashMap<Long, String> enemyattackusers = new HashMap<>();     //authorid --> player + " " + enemy
    static HashMap<String, Players> enemySave = new HashMap<>();      //player + " " + enemy --> saved player stats, saved enemy stats
    static LinkedList<Category> categories = new LinkedList<>();    
    static MessageEmbed changelog, helpEmbed, attackHelpEmbed, tagHelpEmbed, rollHelpEmbed;           //these can be MessageEmbeds since they're only generated once
    static EmbedBuilder statsEmbed = new EmbedBuilder();        //not MessageEmbed since will vary each generation
    static HashMap<Long, Boolean> calculated = new HashMap<>();     //authorid --> calculated?
    static ArrayList<Boss> bosses = new ArrayList<>(10);        //list of bosses
    static ArrayList<Map> maps = new ArrayList<>(10);        //list of maps
    static HashMap<String, Tag> tags = new HashMap<>();      //list of tags
    static String token, globalDescription = "Solace v1.8";
    
    public Bot() throws Exception {
        jda = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
        jda.addEventListener(this);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        String m = event.getMessage().getContentDisplay();      
        String[] parts = m.split(" ");
        boolean valid = false;      //whether the command was valid
        MessageChannel c = event.getChannel();
        if(!m.startsWith("w!")) {       //no command
            return;
        }
//        c.addReactionById(event.getMessageId(), "✅").queue();
        
        ArrayList<String> args = new ArrayList<>();     //place all the arguments into a list
        if(parts.length > 1) {
            for(int i = 0; i < parts.length - 1; i++) {
                args.add(parts[i + 1]);
            }
        }
        
        //true, but wasn't confirmed
        if(calculated.containsKey(event.getAuthor().getIdLong()) && calculated.get(event.getAuthor().getIdLong()) && !parts[0].equals("w!confirm")) {        
            calculated.replace(event.getAuthor().getIdLong(), false);
        }
        
        try {
            for(Category category:categories) {
                if(category.isActionApplicable(parts[0])) {     //go through each category and attempt to parse
                    valid = true;
                    category.response(parts[0], args, event);
                    break;
                }
            }
        }
        catch(ValidationException e) {
            c.sendMessage(e.getMessage()).queue();      //send error message if failed to execute
        }
        
        if(!valid) {        //no action was applicable
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
    
    public static void init() {         //all the stuff that needs to be done first
        try {       //reading from file
            Scanner config = new Scanner(new File("config.txt"));
            while(config.hasNextLine()) {
                String[] temp = config.nextLine().split(",.,");
                if(temp.length == 1) {
                    token = temp[0];
                    continue;
                }
                if(temp[0].contains("*g^")) {       //filling defaultPlayer
                    defaultPlayer.put(Long.parseLong(temp[0].substring(3)), playermap.get(temp[1]));
                }
                else if(temp[0].contains("^t*")) {       //filling tags
                    tags.put(temp[0].substring(3), new Tag(Long.parseLong(temp[2]), temp[1]));
                }
                else {
                    Weapon weapon = Weapon.valueOf(temp[20]);
                    Skill skill = Skill.valueOf(temp[temp.length - 3]);
                    PClass pclass = PClass.valueOf(temp[temp.length - 2]);
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
                    playermap.put(temp[0], new Player(temp[0], new Stats(stats), new Growths(growths), weapon, new WeaponRanks(weaponRanks), skill, pclass, authorid));
                    idmap.put(temp[0], authorid);
                    calculated.put(authorid, false);
                }
            }
            
            Scanner mapReader = new Scanner(new File("maps.txt"));
            Map map = new Map();
            int counter = 0;
            while(mapReader.hasNextLine()) {
                String line = mapReader.nextLine();
                if(line.equals("~")) {
                    maps.add(map);
                    map = new Map();        //start generating a new map
                    counter = 0;
                    continue;
                }
                for(int i = 0; i < Map.WIDTH; i++) {
                    map.grid[counter][i] = line.charAt(i);
                }
                counter++;
            }
        }
        catch(FileNotFoundException e) {
        }
        
        EmbedBuilder changelogEmbed = new EmbedBuilder();
        changelogEmbed.setTitle("Solace Changelog", null);
        changelogEmbed.setColor(Color.red);
        changelogEmbed.setDescription("The full changelog can be found on github.");
        changelogEmbed.addField("v1.8: The DnD Update", "- The **w!roll** command has been overhauled.\n"
                + "    - Dice of any size and number can be rolled (i.e. **w!roll 4d6**).\n"
                + "    - Multiple types of dice can be rolled by inserting a '+' sign between dice.\n"
                + "    - Checks can be performed (i.e. **w!roll 3d8>5**).\n"
                + "    - **w!roll** will still roll a standard 6-sided die.\n"
                + "    - Use **w!rollhelp** for more info.", true);
        changelogEmbed.addBlankField(true);
        changelogEmbed.addField("v1.7: The Class Update", "- **Classes** have been added.\n"
                + "\t- Classes can be chosen during registration by typing **w!register <username> <class>**.\n"
                + "\t- Use **w!classes** for a list of currently implemented classes.\n"
                + "\t- Classes affect **base stats**, **growths**, and **abilities to use weapons**.\n"
                + "\t- Stratum enemies will now also be categorized into classes.\n"
                + "\t- However, as a result, **all character data has been cleared**. Sorry.\n"
                + "- The **Anima weapon triangle** has been added to combat.\n"
                + "- Maps are now **15x15**, although larger maps are possible and will be randomly generated.\n"
                + "- Hit chances no longer stack each time an attack is calculated.\n"
                + "- An opponent with Fortune equipped no longer sets their own crit chance to 0.\n"
                + "- Tags with no {args} fields no longer break when given extra arguments.", true);
        changelog = changelogEmbed.build();
                
        EmbedBuilder help = new EmbedBuilder();
        help.setTitle("Help", null);
        help.setColor(Color.blue);
        help.setDescription(globalDescription);
        help.addField("Commands", 
                  "**w!register <username> [<class>]** - Registers a user."
                + "\n**w!classes** - Shows a list of classes."
                + "\n**w!select <user>** - Auto-fills <user>."
                + "\n**w!stats <user>** - Displays stats."
                + "\n**w!reroll <user>** - Rerolls stats."
                + "\n**w!weaponranks <user>** - Displays weapon ranks."
                + "\n**w!users** - Displays a list of users.\n"
                + "\n**w!weapons** - Shows a list of weapons."
                + "\n**w!equip <user> <weapon>** - Equips a weapon."
                + "\n**w!unequip <user>** - Unequips a weapon.\n"
                + "\n**w!skills** - Shows a list of skills."
                + "\n**w!assign <user> <skill>** - Assigns a skill."
                + "\n**w!remove <user>** - Removes a skill.\n"
                + "\n**w!attackhelp** - Shows help regarding attacks."
                + "\n**w!taghelp** - Shows help regarding tags.\n"
                + "\n**w!roll [<dice>]** - Rolls dice."
                + "\n**w!rollhelp** - Shows help regarding dice rolling.\n"          
                + "\n**w!ping** - Pong!", true);
        help.setFooter("Created by @WillFlame#5739", null);
        helpEmbed = help.build();        
        
        EmbedBuilder attackHelp = new EmbedBuilder();
        attackHelp.setTitle("Attack Help", null);
        attackHelp.setColor(Color.blue);
        attackHelp.setDescription(globalDescription);
        attackHelp.addField("Commands", 
                  "\n**w!attack <user> <enemy>** - Attacks an enemy."
                + "\n**w!confirm** - Confirms the attack. Only used immediately after w!attackp or w!attackm.\n"
                + "\nTo train against stratum units, replace the enemy name with \"stratum\"+<level>."
                + "\nStratum enemies can be then attacked using their name.\n"
                + "\n**w!heal <user> <recipient>** - Heals the recipient. Requires a staff equipped.\n"
                + "\n**w!bosses** - Displays a list of boss battles."
                + "\n**w!rooms** - Diplays a list of rooms."
                + "\n**w!boss <bossname> <players...>** - Creates a room to fight a boss battle."
                + "\n**w!join <bossname> <players...>** - Joins a room, if the room exists and is not full."
                + "\n**w!move <user> <location>** - Moves a character."
                + "\n**w!endturn <user>** - Ends the user's turn.", true);
        attackHelp.setFooter("Created by @WillFlame#5739", null);
        attackHelpEmbed = attackHelp.build();
        
        EmbedBuilder tagHelp = new EmbedBuilder();
        tagHelp.setTitle("Tag Help", null);
        tagHelp.setColor(Color.blue);
        tagHelp.setDescription(globalDescription);
        tagHelp.addField("Commands", 
                  "\n**w!tag create <name> <contents>** - Creates a tag."
                + "\n**w!tag edit <name> <contents>** - Overwrites a tag."
                + "\n**w!tag delete <name>** - Deletes a tag."
                + "\n**w!tags** - Dispalys a list of tags.", true);
        tagHelp.addField("Fields", 
                  "\n**{args}** - Returns an argument provided. (i.e. w!tag <name> <args>)"
                + "\n**{args;n}** - Returns the nth argument provided."
                + "\n**{user}** - Returns the user's username.", true);
        tagHelp.setFooter("Created by @WillFlame#5739", null);
        tagHelpEmbed = tagHelp.build();
        
        EmbedBuilder rollHelp = new EmbedBuilder();
        rollHelp.setTitle("Roll Help", null);
        rollHelp.setColor(Color.blue);
        rollHelp.setDescription(globalDescription);
        rollHelp.addField("Commands", 
                  "\n**w!roll** - Rolls a 6-sided die."
                + "\n**w!roll NdS** - Rolls an S-sided die N times."
                + "\n**w!roll NdS+OdT** - Rolls an S-sided die N times, and then an O-sided die T times. Can be extended further."
                + "\n**w!roll NdS>T** - Rolls an S-sided die N times, compares with T to count successes. Works with <.", true);
        rollHelp.setFooter("Created by @WillFlame#5739", null);
        rollHelpEmbed = rollHelp.build();
        
        categories.add(Skill.NA);
        categories.add(new Stats());
        categories.add(new AttackPreview());
        categories.add(new AttackConfirm());
        categories.add(Weapon.NA);
        categories.add(new Heal());
        categories.add(new BotSystem());
        categories.add(new BossBattle());
        categories.add(new Tags());
        categories.add(new Dice());
        
        Boss boss1 = new Boss("Cyrus", new Stats(new int[] {49, 49, 30, 20, 23, 20, 22, 30, 18, 25, 0}), Weapon.SteelAxe, Skill.Luna, -1, PClass.Fighter, 0);
        Boss boss2 = new Boss("Troubadour", new Stats(new int[] {48, 48, 20, 27, 31, 9, 28, 20, 30, 25, 0}), Weapon.Mend, Skill.Pavise, -2, PClass.Priest, 0);
        bosses.add(boss1);
        bosses.add(boss2);
        Bot.playermap.put("Cyrus", boss1);
        Bot.playermap.put("Troubadour", boss2);
    }
    
    public static void update() {
        try {
            try (FileWriter writer = new FileWriter("config.json")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token", token);
                
                LinkedHashMap characters = new LinkedHashMap();
                Object[] players = playermap.values().toArray();
                for (Object player : players) {
                    
                    
                    Player p = (Player) player;
                    if(p.authorid < 0) {        //don't put stratum enemies/bosses into config
                        continue;
                    }
                    LinkedHashMap attributes = new LinkedHashMap();
                    attributes.put("stats", p.stats.toLinkedList());
                    attributes.put("growths", p.growths.toLinkedList());
                    attributes.put("weapon", p.weapon);
                    attributes.put("skill", p.skill);
                    attributes.put("userid", p.authorid);
                    attributes.put("class", p.pclass);
                    
                    characters.put(p.username, attributes);
                }
                jsonObject.put("characters", characters);
                
                Object[] defaults1 = defaultPlayer.keySet().toArray();
                Object[] defaults2 = defaultPlayer.values().toArray();
                for(int i = 0; i < defaults1.length; i++) {
                    Player p = (Player)defaults2[i];
                    writer.write("*g^" + defaults1[i] + ",.," + p.username);
                    writer.newLine();
                }
                Object[] tags1 = tags.keySet().toArray();
                Object[] tags2 = tags.values().toArray();
                for(int i = 0; i < tags1.length - 1; i++) {
                    Tag t = (Tag)tags2[i];
                    writer.write("^t*" + tags1[i] + ",.," + t.contents + ",.," + t.userid);
                    writer.newLine();
                }
                if(!tags.isEmpty()) {      //no dangling newline
                    Tag t = (Tag)tags2[tags2.length - 1];
                    writer.write("^t*" + tags1[tags1.length - 1] + ",.," + t.contents + ",.," + t.userid);
                }
            }
        }
        catch(IOException e) {
            System.out.println("how in the world did you get this to happen");
        }
    }
}
