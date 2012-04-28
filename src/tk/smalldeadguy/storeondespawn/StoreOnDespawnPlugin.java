package tk.smalldeadguy.storeondespawn;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class StoreOnDespawnPlugin extends JavaPlugin implements Listener {
	public Logger log;

	public int radius = 5;

	public boolean despawn(Item i) {
		World w = i.getWorld();
		for(int x = 0; x <= radius; x++) for(int y = 0; y <= radius; y++) for(int z = 0; z <= radius; z++) {
			Location[] ls = {
					new Location(w,  x,  y,  z),
					new Location(w,  x,  y, -z),
					new Location(w,  x, -y,  z),
					new Location(w,  x, -y, -z),
					new Location(w, -x,  y,  z),
					new Location(w, -x,  y, -z),
					new Location(w, -x, -y,  z),
					new Location(w, -x, -y, -z)
			};
			for(Location l : ls) {
				Block b = w.getBlockAt(i.getLocation().add(l));
				if(b.getState() instanceof InventoryHolder) {
					InventoryHolder c = (InventoryHolder) b.getState();
					Inventory inv = c.getInventory();
					int index = inv.firstEmpty();
					Map<Integer, ItemStack> didntFit = inv.addItem(i.getItemStack());
					
					if(didntFit.size() == 0) {
						i.remove();
						return true;
					}
					
					i.setItemStack((ItemStack) didntFit.values().toArray()[0]);
					
					if(i.getItemStack() == null || i.getItemStack().getAmount() == 0) {
						i.remove();
						return true;
					}
				}
			}
		}
		return false;
	}

	@EventHandler
	public void itemDespawn(ItemDespawnEvent e) {
		Item i = e.getEntity();
		despawn(i);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("despawn")) {
			int despawned = 0;
			int tried = 0;

			if(sender instanceof Player) {
				Player p = (Player) sender;
				World w = p.getWorld();
				for(Entity e : w.getEntities())
					if(e instanceof Item) {
						if(despawn((Item) e))
							despawned++;
						tried++;
					}

			}
			else
				for(World w : sender.getServer().getWorlds())
					for(Entity e : w.getEntities())
						if(e instanceof Item)
							if(despawn((Item) e))
								despawned++;
			tried++;
			sender.sendMessage("Tried despawning " + tried + " items, " + despawned + " succeeded");
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("despawn-radius") && args.length == 1) {
			radius = Integer.parseInt(args[0]);
			getConfig().set("despawn.radius", radius);
		}
		return false;
	}

	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);

		Object c = getConfig().get("despawn.radius");
		if (c != null && c instanceof Integer)
			radius = (int) c;
		else
			getConfig().set("despawn.radius", 5);

		log = getLogger();
		log.info("StoreOnDespawn has been enabled!");
	}

	public void onDisable(){
		log.info("StoreOnDespawn has been disabled.");
	}
}
