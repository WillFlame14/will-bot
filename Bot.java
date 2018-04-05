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
    public JDA jda;
    static HashMap<String, Player> playermap = new HashMap<>();
    static HashMap<String, Long> idmap = new HashMap<>();
    static LinkedList<Category> categories = new LinkedList<>();
    static MessageEmbed battleHelp, helpEmbed; 
    static EmbedBuilder statsEmbed = new EmbedBuilder();
    static boolean calculated;
    static long calcid;
    
    public Bot() throws Exception {
        jda = new JDABuilder(AccountType.BOT).setToken("NDIyNDgxMzM3MzE2ODAyNTYw.DYcaBg.AQrb8xn6vR9DXt2dwEE9pEqXE4k").buildBlocking();
        jda.addEventListener(this);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        String m = event.getMessage().getContentDisplay();
        String[] parts = m.split(" ");
        boolean valid = false;
        MessageChannel c = event.getChannel();
        if(event.getAuthor().isBot() || !m.startsWith("w!")) {       //from bot, no command
            return;
        }
//        c.addReactionById(event.getMessageId(), "✅").queue();
        
        ArrayList<String> args = new ArrayList<>();
        if(parts.length > 1) {
            for(int i = 0; i < parts.length - 1; i++) {
                args.add(parts[i + 1]);
            }
        }
        
        if(!parts[0].equals("w!confirm")) {
            calculated = false;
        }
        
        try {
            for(Category category:categories) {
                if(category.isActionApplicable(parts[0])) {
                    category.response(parts[0], args, event);
                    valid = true;
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
                Weapon weapon = Weapon.valueOf(temp[20]);
                Skill skill = Skill.valueOf(temp[21]);
                long authorid = Long.parseLong(temp[22]);
                int[] stats = new int[12];
                int[] growths = new int[8];
                for(int i = 1; i < 12; i++) {
                    stats[i - 1] = Integer.parseInt(temp[i]);
                }
                for(int i = 12; i < 20; i++) {
                    growths[i - 12] = Integer.parseInt(temp[i]);
                }
                playermap.put(temp[0], new Player(temp[0], new Stats(stats), new Growths(growths), weapon, skill, authorid));
                idmap.put(temp[0], authorid);
            }
        }
        catch(FileNotFoundException e) {
        }
        
        EmbedBuilder battleEmbed = new EmbedBuilder();
        battleEmbed.setTitle("Battle", null);
        battleEmbed.setColor(Color.blue);
        battleEmbed.setDescription("Welcome to the Battle interface!");
        battleEmbed.addField("Starting", "Use `w!register <username>` to register.", true);
        battleEmbed.setFooter("this is a footer", null);
        battleHelp = battleEmbed.build();
        
        EmbedBuilder help = new EmbedBuilder();
        help.setTitle("Help", null);
        help.setColor(Color.blue);
        help.setDescription("Solace v1.1");
        help.addField("Commands", "w!register <username> - Registers a user."
                + "\nw!stats <user> - Displays stats."
                + "\nw!reroll <user> - Rerolls stats."
                + "\nw!users - Displays a list of all currently registered users.\n"
                + "\nw!weapons - Shows a list of weapons."
                + "\nw!equip <user> <weapon> - Equips a weapon."
                + "\nw!unequip <user> - Unequips a weapon.\n"
                + "\nw!skills - Shows a list of skills."
                + "\nw!assign <user> <skill> - Assigns a skill."
                + "\nw!remove <user> - Removes a skill.\n"
                + "\nw!attackp <user> <enemy> - Attacks an enemy physically."
                + "\nw!attackm <user> <enemy> - Attacks an enemy magically."
                + "\nw!confirm - Confirms the attack. Can only be used immediately after w!attackp or w!attackm.\n"
                + "\nTo train against stratum units, replace the enemy name with \"stratum\"+<stratum level>."
                + "\nFor example, \"stratum6\" matches you against a generated enemy that is from stratum level 6."
                + "\nStratum enemies can be then attacked using their username.\n" 
                + "\nw!ping - Pong!"
                + "\nw!roll - Rolls a 6-sided die.", true);
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
                for(int i = 0; i < playermap.size() - 1; i++) {
                    Player p = (Player)players[i];
                    writer.write(p.toString());
                    writer.newLine();
                }
                writer.write(((Player)players[players.length - 1]).toString());
            }
        }
        catch(IOException e) {
            System.out.println("how in the world did you get this to happen");
        }
    }
}