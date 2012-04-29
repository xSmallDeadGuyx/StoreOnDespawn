package tk.smalldeadguy.storeondespawn;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
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

public class StoreOnDespawnPlugin extends JavaPlugin implements Listener {
	public Logger log;

	public int radius = 5;

	public boolean despawn(Item i) {
		SortedMap<Double, Inventory> storage = new TreeMap<>();
		Location item = i.getLocation();
		
		//Get the center of the block the item is in
		Location blockCenter = item.getBlock().getLocation();
		blockCenter.add(0.5, 0.5, 0.5);
		
		for(int x = -radius; x <= radius; x++) for(int y = -radius; y <= radius; y++) for(int z = -radius; z <= radius; z++) {
			Location cursor = blockCenter.clone().add(x, y, z);
			BlockState bs = cursor.getBlock().getState();

			if(bs instanceof InventoryHolder)
				storage.put(cursor.distance(item), ((InventoryHolder) bs).getInventory());
		}
		
		for(Inventory inv : storage.values()) {
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
				for(Entity e : w.getEntities()) {
					if(e instanceof Item) {
						if(despawn((Item) e)) despawned++;
						tried++;
					}
				}
			}
			else {
				for(World w : sender.getServer().getWorlds()) {
					for(Entity e : w.getEntities()) {
						if(e instanceof Item) {
							if(despawn((Item) e)) despawned++;
							tried++;
						}
					}
				}
			}
			sender.sendMessage("Tried despawning " + tried + " items, " + despawned + " succeeded");
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("despawn-radius") && args.length == 1) {
			radius = Integer.parseInt(args[0]);
			getConfig().set("despawn.radius", radius);
			return true;
		}
		return false;
	}

	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);

		radius = getConfig().getInt("despawn.radius", 5);

		log = getLogger();
		log.info("StoreOnDespawn has been enabled!");
	}

	public void onDisable(){
		log.info("StoreOnDespawn has been disabled.");
	}
}
