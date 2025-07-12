package one.pouekdev.mobHealth;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class MobHealthListener implements Listener{
    private final MobHealth plugin;

    public MobHealthListener(MobHealth plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        event.getWorld().getLivingEntities().stream().filter((e) -> (!(e instanceof Player) && !(e instanceof ArmorStand))).forEach(this::updateHealthBar);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        Entity var3 = event.getEntity();
        if(var3 instanceof final LivingEntity entity){
            if(!(entity instanceof Player) && !(entity instanceof ArmorStand)){
                double finalHealth = entity.getHealth() - event.getFinalDamage();
                if(finalHealth <= 0.0){
                    entity.setCustomName(null);
                    entity.setCustomNameVisible(false);
                }
                else{
                    (new BukkitRunnable(){
                        public void run(){
                            MobHealthListener.this.updateHealthBar(entity);
                        }
                    }).runTaskLater(this.plugin, 1L);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }

    @EventHandler
    public void onPlayerNameTagEntity(PlayerInteractEntityEvent event){
        Entity var3 = event.getRightClicked();
        if(var3 instanceof final LivingEntity entity){
            if(!(entity instanceof ArmorStand)){
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                if(item.getType() == Material.NAME_TAG && item.hasItemMeta()){
                    ItemMeta meta = item.getItemMeta();
                    if(meta.hasDisplayName()){
                        String newName = ChatColor.stripColor(meta.getDisplayName());
                        NamespacedKey key = new NamespacedKey(this.plugin, "custom_mob_name");
                        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, newName);
                        (new BukkitRunnable(){
                            public void run(){
                                MobHealthListener.this.updateHealthBar(entity);
                            }
                        }).runTaskLater(this.plugin, 1L);
                    }
                }
            }
        }
    }

    public void updateHealthBar(LivingEntity entity){
        double health = Math.max(0.0F, entity.getHealth());
        double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
        FileConfiguration config = this.plugin.getConfig();
        String formatKey = health / maxHealth <= 0.4 ? "healthbar-low" : "healthbar-full";
        String format = ChatColor.translateAlternateColorCodes('&', config.getString(formatKey));
        NamespacedKey key = new NamespacedKey(this.plugin, "custom_mob_name");
        String mobName;
        if(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING) == null){
            mobName = entity.getType().name().replace("_", " ").toLowerCase();
            char var10000 = Character.toUpperCase(mobName.charAt(0));
            mobName = var10000 + mobName.substring(1);
        }
        else{
            mobName = entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }
        String formattedHealth = health % 1.0F == 0.0F ? String.valueOf((int) health) : String.format("%.1f", health);
        String formattedMaxHealth = maxHealth % 1.0F == 0.0F ? String.valueOf((int) maxHealth) : String.format("%.1f", maxHealth);
        String name = format.replace("%mob%", mobName).replace("%health%", formattedHealth).replace("%max_health%", formattedMaxHealth);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
    }
}
