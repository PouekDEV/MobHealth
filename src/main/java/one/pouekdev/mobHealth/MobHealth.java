package one.pouekdev.mobHealth;

import org.bukkit.plugin.java.JavaPlugin;

public final class MobHealth extends JavaPlugin{
    @Override
    public void onEnable(){
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new MobHealthListener(this), this);
    }
}
