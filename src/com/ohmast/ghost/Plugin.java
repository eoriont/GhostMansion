package com.ohmast.ghost;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;
import java.util.*;

/**
 * oriont's ghost mansion
 * made by oriont
 */

public class Plugin extends JavaPlugin implements Listener {

    public String logo = ""+ChatColor.AQUA+ChatColor.BOLD+"Cycle"+ChatColor.GREEN + ChatColor.BOLD + "MC"+ChatColor.GRAY+ChatColor.BOLD+": "+ChatColor.RESET+ChatColor.GOLD;

    public HashMap<Player, Boolean> flashlights = new HashMap<>();
    public HashMap<Player, Boolean> ingame = new HashMap<>();
    public HashMap<Player, Integer> flashlighthealth = new HashMap<>();

    public Server server = Bukkit.getServer();

    public ItemStack flashlighton;
    public ItemStack flashlightoff;
    public ItemStack ghostdetector;
    public ItemStack ghostdetector2;
    public ItemStack ghostdetector3;

    public BossBar bb = server.createBossBar("Ghost Health", BarColor.BLUE, BarStyle.SEGMENTED_6, BarFlag.CREATE_FOG);

    public Player ghost;

    public boolean gameon = false;
    public boolean ghostf = false;

    public int count;
    public int countdown = 3;
    public int id = 0;
    public int countdown1 = 10;
    public int id1 = 0;
    public int id2 = 0;
    public int countdown2 = 5;
    public int time = 0;
    public int numingame = 0;

    public ScoreboardManager manager;
    public Scoreboard board;
    public Objective objective;

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        messageAll("Loaded Ghost Mansion!");

        flashlighton = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta flashlightmeta = flashlighton.getItemMeta();
        flashlightmeta.setDisplayName(ChatColor.AQUA + "Flashlight");
        flashlighton.setItemMeta(flashlightmeta);

        flashlightoff = new ItemStack(Material.STICK, 1);
        flashlightoff.setItemMeta(flashlightmeta);

        ghostdetector = new ItemStack(Material.SUGAR);
        ItemMeta detectormeta = ghostdetector.getItemMeta();
        detectormeta.setDisplayName(ChatColor.AQUA + "Ghost Detector");
        ghostdetector.setItemMeta(detectormeta);

        ghostdetector2 = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta d2meta = ghostdetector2.getItemMeta();
        d2meta.setDisplayName(ChatColor.AQUA + "Ghost Detector");
        ghostdetector2.setItemMeta(d2meta);

        ghostdetector3 = new ItemStack(Material.REDSTONE);
        ItemMeta d3meta = ghostdetector3.getItemMeta();
        d3meta.setDisplayName(ChatColor.AQUA + "Ghost Detector");
        ghostdetector3.setItemMeta(d3meta);

        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("","");
        clearStats();
    }

    public void onDisable() {
        bb.removeAll();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if(label.equalsIgnoreCase("start")) {
            instructions();
        }
        if(label.equalsIgnoreCase("test")) {
            p.sendMessage(ingame.toString());
        }
        if(label.equalsIgnoreCase("spawnb")) {
            spawnBattery();
        }
        if(label.equalsIgnoreCase("cl")) {
            clearStats();
        }
        if(label.equalsIgnoreCase("st")) {
            stopGame();
        }
        return true;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        if(gameon) {
            killPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if(gameon) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (item != null)
                    if (item.getItemMeta().getDisplayName().contains("Flashlight")) switchFlashlight(p);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        for(Entity entity : p.getNearbyEntities(0, 0, 0)) {
            if(entity instanceof Player) {
                Player pl = (Player) entity;
                if(!ingame.get(pl) || !ingame.get(p)) {
                    return;
                }
                if(!ghostf) {
                    if (pl == ghost) {
                        messageAll("The Ghost Caught " + ChatColor.RED + p.getDisplayName());
                        killPlayer(p);
                    } else if (p == ghost) {
                        messageAll("The Ghost Caught " + ChatColor.RED + pl.getDisplayName());
                        killPlayer(pl);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(gameon) {
            e.setDeathMessage(null);
            if(e.getEntity() == ghost) {
                win("players");
            }
        }
    }

    @EventHandler
    public void foodChange(FoodLevelChangeEvent e) {
        e.setCancelled(false);
    }

    @EventHandler
    public void logOff(PlayerQuitEvent e) {
        e.setQuitMessage("");
        if(gameon) {
            killPlayer(e.getPlayer());
            if(e.getPlayer() == ghost) {
                messageAll("The ghost has left the game! Choosing another one...");
                newGhost();
                setupGhost();
            }
        }
    }

    ///////////////////////////////////////////////////////

    public void startGame() {
        if(gameon) stopGame();
        newGhost();
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("people", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(""+ ChatColor.AQUA+ChatColor.BOLD+"oriont's Ghost Mansion");
        objective.getScore(ChatColor.GREEN+"Time Left:").setScore(6);
        objective.getScore(ChatColor.GOLD+"Ghost:").setScore(0);
        objective.getScore(ghost.getDisplayName()).setScore(-1);
        objective.getScore(ChatColor.GREEN+"Players Alive: ").setScore(-2);
        objective.getScore(ChatColor.GOLD+"oriont's Ghost Mansion").setScore(-4);
        ingame.clear();
        flashlights.clear();
        flashlighthealth.clear();
        bb = server.createBossBar("Ghost Health", BarColor.BLUE, BarStyle.SEGMENTED_6, BarFlag.CREATE_FOG);
        time = 600;
        numingame = 0;
        clearStats();
        for (Player p : server.getOnlinePlayers()) {
            ingame.put(p, true);
            bb.addPlayer(p);
            if(p == ghost) setupGhost();
            else setupPlayer(p);
            numingame++;
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (Player p : ingame.keySet()) {
                    if(flashlights.get(p) != null && flashlights.get(p) && flashlighthealth.get(p) > 0) flashlighthealth.put(p, flashlighthealth.get(p) - 1);
                }
                if(count % 10 == 0) spawnBattery();
                count++;
            }
        }, 0, 20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {public void run() {
                tick();
            }}, 0, 1);
        gameon = true;
    }

    public void tick() {
        if(gameon) {
            bb.setProgress(ghost.getHealth()*0.05);
            time = 600-count;
            objective.getScore(ChatColor.GREEN+"Time Left:").setScore(time);
            for (Player p : ingame.keySet()) {
                if (flashlighthealth.get(p) == 0 && flashlights.get(p)) switchFlashlight(p);
                if (p.getInventory().contains(Material.DIAMOND_BLOCK)) collectBattery(p);
                if (p != ghost) {
                    sendActionBar(p, batteryStringMaker(flashlighthealth.get(p)));
                    ghostDetect(p);
                }
                if (flashlights.get(p)) raycast(p);
            }
            if(time == 0) {
                win("players");
            }
            if(numingame == 1) {
                if(ingame.get(ghost)) {
                    win("ghost");
                } else {
                    win("players");
                }
            }
        }
    }

    public void stopGame() {
        Bukkit.getScheduler().cancelAllTasks();
        ingame.clear();
        flashlights.clear();
        flashlighthealth.clear();
        count = 0;
        gameon = false;
        countdown = 3;
        countdown1 = 10;
        id = 0;
        id1 = 0;
        id2 = 0;
        countdown2 = 5;
        time = 600;
        ghost = null;
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("","");
        clearStats();
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void switchFlashlight(Player p) {
        flashlights.put(p, !flashlights.get(p));
        if (flashlights.get(p)) {
            if (flashlighthealth.get(p) > 0) {
                p.getInventory().setItem(0, flashlighton);
                p.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1));
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 255, true, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 3, true, false));
                raycast(p);
            }
        } else {
            p.getInventory().setItem(0, flashlightoff);
            p.getActivePotionEffects().clear();
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.removePotionEffect(PotionEffectType.SLOW);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 255, true, false));
            p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
        }
    }
    public void clearStats() {
        bb.removeAll();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            clearPotionEffects(p);
            p.getInventory().clear();
            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);
        }
    }
    public void clearPotionEffects(Player p) {
        for(PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
    }
    public void killPlayer(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        ingame.put(p, false);
        numingame--;
    }
    public void raycast(Player p) {
        LivingEntity from = p;
        int distance = 10;

        BlockIterator blocksToAdd = new BlockIterator(from.getEyeLocation(), 1, distance);
        blocksToAdd.next();
        while(blocksToAdd.hasNext()){
            Block block = blocksToAdd.next();
            if(block.getType().isSolid()) {
                break;
            }
            for(Entity e : getNearbyEntities(block.getLocation(), 1)) {
                if(e instanceof Player) {
                    Player pl = (Player) e;
                    if(ingame.get(pl)) {
                        if(pl == ghost) {
                            flashlightGhost();
                            break;
                        } else break;
                    }
                }
            }
        }
    }
    public static List<Entity> getNearbyEntities(Location where, int range) {
        List<Entity> found = new ArrayList<Entity>();

        for (Entity entity : where.getWorld().getEntities()) {
            if (isInBorder(where, entity.getLocation(), range)) found.add(entity);
        }
        return found;
    }
    public static boolean isInBorder(Location center, Location notCenter, int range) {
        int x = center.getBlockX(), z = center.getBlockZ();
        int x1 = notCenter.getBlockX(), z1 = notCenter.getBlockZ();
        if (x1 >= (x + range) || z1 >= (z + range) || x1 <= (x - range) || z1 <= (z - range)) return false;
        return true;
    }
    public void message(String message, Player p) {
        p.sendMessage(logo + message);
    }
    public void messageAll(String message) {
        Bukkit.broadcastMessage(logo + message);
    }
    public void messageAllNL(String message) {Bukkit.broadcastMessage(ChatColor.GOLD + message);}
    public void sendTitle(String message, String message2, int duration, int fade, Player p) {
        Title title = new Title(message, message2, fade, duration, fade);
        title.send(p);
    }
    public void sendTitle(String message, String message2, int duration, int fade) {
        Title title = new Title(message, message2, fade, duration, fade);
        title.broadcast();
    }
    public static void sendActionBar(Player player, String message){
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }
    public String batteryStringMaker(Integer battery) {
        String str = "";
        if(battery == null) {
            return str;
        }
        for(int i = 0; i < battery; i++) {
            str = str.concat(ChatColor.GREEN+"▉");
        }
        for(int i = 0; i < 10-battery; i++) {
            str = str.concat(ChatColor.RED+"▉");
        }
        str = ChatColor.GOLD + "Flashlight Battery: " + str;
        return str;
    }
    public void collectBattery(Player p) {
        flashlighthealth.put(p, 10);
        p.getInventory().remove(Material.DIAMOND_BLOCK);
    }
    public void ghostDetect(Player p) {
        p.getInventory().setItem(1, ghostdetector);
        for (Entity e : p.getNearbyEntities(10, 1, 10)) {
            if(p.getNearbyEntities(5, 1, 5).contains(e)) break;
            if(e instanceof Player) {
                Player pl = (Player) e;
                if(pl == ghost) {
                    p.getInventory().setItem(1, ghostdetector2);
                }
            }
        }

        for (Entity e : p.getNearbyEntities(5, 1, 5)) {
            if(e instanceof Player) {
                Player pl = (Player) e;
                if(pl == ghost) {
                    p.getInventory().setItem(1, ghostdetector3);
                }
            }
        }
    }
    public void setupGhost() {
        message("You are the Ghost!", ghost);
        sendTitle(ChatColor.GREEN + "You are the Ghost!", "Get all the Players!",1,3, ghost);
        flashlights.put(ghost, false);
        flashlighthealth.put(ghost, 0);
        ghost.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, true, false));
        ghost.setScoreboard(board);
        ghost.getInventory().clear();
    }
    public void setupPlayer(Player p) {
        message("You are a Ghost Hunter!", p);
        sendTitle(ChatColor.GOLD + "You are a Ghost Hunter!", "Hunt the ghost!",1, 3, p);
        flashlights.put(p, false);
        flashlighthealth.put(p, 10);

        Inventory inv = p.getInventory();
        inv.clear();
        inv.setItem(0, flashlightoff);
        inv.setItem(1, ghostdetector);
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 255, true, false));

        objective.getScore(p.getDisplayName()).setScore(-4);
        p.setScoreboard(board);
    }
    public void spawnBattery() {
        World w = Bukkit.getWorld("world");
        for (Chunk c : w.getLoadedChunks()) {
            int cx = c.getX() << 4;
            int cz = c.getZ() << 4;
            for (int x = cx; x < cx + 16; x++) {
                for (int z = cz; z < cz + 16; z++) {
                    for (int y = 0; y < 128; y++) {
                        if (w.getBlockAt(x, y, z).getType() == Material.IRON_BLOCK) {
                            w.dropItem(new Location(w, x, y+1, z), new ItemStack(Material.DIAMOND_BLOCK, 1));
                        }
                    }

                }
            }
        }
    }
    public void delayStart() {
        countdown = 4;
        id = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if(countdown == 0) {
                    getServer().getScheduler().cancelTask(id);
                    startGame();
                    return;
                } else if (countdown == 1) {
                    sendTitle(ChatColor.GREEN + "GO!", ChatColor.GRAY+"Have Fun!",1,0);
                } else {
                    sendTitle(ChatColor.GOLD + "Game Starts in " + (countdown-1),"",3,0);
                }
                countdown--;
            }
        }, 0,  20);
    }
    public void flashlightGhost() {
        ghost.damage(3);
        ghostf = true;
        ghost.removePotionEffect(PotionEffectType.INVISIBILITY);

        countdown1 = 10;
        id1 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                ghost.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 2, true, false));
                ghost.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 25, true, false));
                if(countdown1 == 0) {
                    server.getScheduler().cancelTask(id1);
                    ghost.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, true, false));
                    ghost.removePotionEffect(PotionEffectType.SPEED);
                    ghost.removePotionEffect(PotionEffectType.GLOWING);
                    ghostf = false;
                    return;
                }
                countdown1--;
            }
        }, 0, 20);
    }
    public void instructions() {
        messageAllNL(""+ChatColor.BOLD+ChatColor.STRIKETHROUGH+"--------------------");
        messageAllNL(""+ChatColor.BOLD+"oriont's Ghost Mansion");
        messageAllNL(ChatColor.RED+"If you are a player:");
        messageAllNL("1. Try to shine your light on the ghost");
        messageAllNL("2. Help other players kill the ghost");
        messageAllNL("3. Find battery generators for your flashlights (iron blocks)");
        messageAllNL(ChatColor.RED+"If you are a ghost:");
        messageAllNL("1. SCARE EVERYONE");
        messageAllNL("2. Don't get hit by flashlights");
        messageAllNL(""+ChatColor.BOLD+ChatColor.STRIKETHROUGH+"--------------------");
        id2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if(countdown2 == 0) {
                    Bukkit.getScheduler().cancelTask(id2);
                    delayStart();
                }
                countdown2--;
            }
        }, 0, 20);
    }
    public void win(String who) {
        if(who.equalsIgnoreCase("ghost")) {
            messageAll("The ghost has scared all the players!");
            messageAll("The Ghost Wins!");
            sendTitle(ChatColor.GREEN+"You Win!", "You got all the players!", 3, 2, ghost);
            for(Player p : ingame.keySet()) {
                if(p != ghost) {
                    sendTitle(ChatColor.RED+"You Lose!", "The ghost got all the players!", 3, 2, p);
                }
            }
        } else {
            messageAll("The ghost has been DEFEATED!");
            messageAll("Players Win!");
            sendTitle(ChatColor.RED+"You Lose!", "You the players got you!", 3, 2, ghost);
            for(Player p : ingame.keySet()) {
                if(p != ghost) {
                    sendTitle(ChatColor.GREEN+"You Win!", "You got the ghost!", 3, 2, p);
                }
            }
        }
        stopGame();
    }
    public void newGhost() {
        ArrayList players = new ArrayList<Player>(server.getOnlinePlayers());
        int rand = new Random().nextInt(players.size());
        Player ghost = (Player) players.get(rand);
        this.ghost = ghost;
        messageAll("The new ghost is " + ghost.getDisplayName());
    }
}
