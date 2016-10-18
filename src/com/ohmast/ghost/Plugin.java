package com.ohmast.ghost;

import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.bukkit.util.BlockIterator;

import java.util.*;

public class Plugin extends JavaPlugin implements Listener {

    public String logo = ""+ChatColor.AQUA+ChatColor.BOLD+"Cycle"+ChatColor.GREEN + ChatColor.BOLD + "MC"+ChatColor.GRAY+ChatColor.BOLD+": "+ChatColor.RESET+ChatColor.GOLD;

    public HashMap<Player, Boolean> flashlights = new HashMap<>();
    public HashMap<Player, Boolean> ingame = new HashMap<>();
    public HashMap<Player, Integer> flashlighthealth = new HashMap<>();

    public ItemStack flashlighton;
    public ItemStack flashlightoff;
    public ItemStack ghostdetector;
    public ItemStack ghostdetector2;
    public ItemStack ghostdetector3;

    public Player ghost;

    public int count;
    public boolean gameon = false;

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
        ghostdetector2.setItemMeta(d3meta);

        clearStats();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player p = (Player) sender;

        if(label.equalsIgnoreCase("start")) {
            delay();
        }
        if(label.equalsIgnoreCase("test")) {
            p.sendMessage(flashlighthealth.toString());
            p.sendMessage(flashlights.toString());
        }
        if(label.equalsIgnoreCase("spawnb")) {
            spawnBattery();
        }

        return true;
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
                if(pl == ghost) {
                    messageAll("The Ghost Caught " + ChatColor.RED + p);
                    killPlayer(p);
                } else if(p == ghost) {
                    messageAll("The Ghost Caught " + ChatColor.RED + pl);
                    killPlayer(pl);
                }
            }
        }
    }

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
        for (Player p : Bukkit.getOnlinePlayers()) {
            clearPotionEffects(p);
            p.getInventory().clear();
            p.setGameMode(GameMode.ADVENTURE);
            message("Cleared Stats!", p);
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
                            killPlayer(ghost);
                            messageAll("The Ghost has been Defeated!");
                            break;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void startGame() {
        Server server = Bukkit.getServer();
        ArrayList players = new ArrayList<Player>(server.getOnlinePlayers());
        int rand = new Random().nextInt(players.size());
        Player ghost = (Player) players.get(rand);
        this.ghost = ghost;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("people", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(""+ ChatColor.AQUA+ChatColor.BOLD+"Ghost Mansion");
        objective.getScore(ChatColor.GOLD+"Ghost:").setScore(1);
        objective.getScore(ghost.getDisplayName()).setScore(1);
        objective.getScore(ChatColor.GREEN+"Players Alive: ").setScore(1);

        ingame.clear();
        flashlights.clear();
        flashlighthealth.clear();
        for (Player p : server.getOnlinePlayers()) {
            ingame.put(p, true);
            if(p != ghost) {
                objective.getScore(p.getDisplayName()).setScore(1);
                p.setScoreboard(board);
            }
            if(p == ghost) {
                message("You are the Ghost!", p);
                sendTitle(ChatColor.GREEN + "You are the Ghost!", "",1,3, p);
                flashlights.put(p, false);
                flashlighthealth.put(p, 0);
            } else {
                message("You are a Ghost Hunter!", p);
                sendTitle(ChatColor.GOLD + "You are a Ghost Hunter!", "",1, 3, p);
                flashlights.put(p, false);
                flashlighthealth.put(p, 10);

                Inventory inv = p.getInventory();
                inv.clear();
                inv.setItem(0, flashlightoff);
                inv.setItem(1, ghostdetector);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 255, true, false));
            }
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (Player p : ingame.keySet()) {
                    if(flashlights.get(p) != null) {
                        if (flashlights.get(p)) {
                            if (flashlighthealth.get(p) > 0) {
                                flashlighthealth.put(p, flashlighthealth.get(p) - 1);
                            }
                        }
                    }
                }

                if(count % 10 == 0) {
                    spawnBattery();
                }
                count++;
            }
        }, 0, 20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                tick();
            }
        }, 0, 1);

        gameon = true;
    }

    public static List<Entity> getNearbyEntities(Location where, int range) {
        List<Entity> found = new ArrayList<Entity>();

        for (Entity entity : where.getWorld().getEntities()) {
            if (isInBorder(where, entity.getLocation(), range)) {
                found.add(entity);
            }
        }
        return found;
    }

    public static boolean isInBorder(Location center, Location notCenter, int range) {
        int x = center.getBlockX(), z = center.getBlockZ();
        int x1 = notCenter.getBlockX(), z1 = notCenter.getBlockZ();

        if (x1 >= (x + range) || z1 >= (z + range) || x1 <= (x - range) || z1 <= (z - range)) {
            return false;
        }
        return true;
    }

    private void tick() {
        if(gameon) {
            for (Player p : ingame.keySet()) {
                if (flashlighthealth.get(p) == 0 && flashlights.get(p)) {
                    switchFlashlight(p);
                }
                if (p.getInventory().contains(Material.DIAMOND_BLOCK)) collectBattery(p);
                if (p != ghost) {
                    sendActionBar(p, ChatColor.GOLD + "Flashlight Battery: " + batteryStringMaker(flashlighthealth.get(p)));
                    ghostDetect(p);
                }
                if (flashlights.get(p)) {
                    raycast(p);
                }
                if (p.getInventory().contains(new ItemStack(Material.DIAMOND_BLOCK))) {
                    collectBattery(p);
                }
            }
        }
    }

    public void message(String message, Player p) {
        p.sendMessage(logo + message);
    }
    public void messageAll(String message) {
        Bukkit.broadcastMessage(logo + message);
    }
    public void sendTitle(String message, String message2, int duration, int fade, Player p) {
        Title title = new Title(message, message2, fade, duration, fade);
        title.send(p);
    }
    public void sendTitle(String message, String message2, int duration, int fade) {
        Title title = new Title(message, message2, fade, duration, fade);
        title.broadcast();
    }

    int countdown = 3;
    int id = 0;

    public void delay() {
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
}
