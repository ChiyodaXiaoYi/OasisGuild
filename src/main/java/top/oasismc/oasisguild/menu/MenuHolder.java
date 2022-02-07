package top.oasismc.oasisguild.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    Inventory inventory;

    MenuType type;

    public MenuHolder(MenuType type) {
        this.type = type;
    }

    public MenuHolder(Inventory inventory, MenuType type) {
        this.inventory = inventory;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuHolder)) return false;

        MenuHolder holder = (MenuHolder) o;

        if (!inventory.equals(holder.inventory)) return false;
        return type == holder.type;
    }

    @Override
    public int hashCode() {
        int result = inventory.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
