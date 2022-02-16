package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    public MenuHolder() {}

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, 54);
    }

}
