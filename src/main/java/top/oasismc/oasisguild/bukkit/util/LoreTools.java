package top.oasismc.oasisguild.bukkit.util;

import java.util.List;

public final class LoreTools {

    public static boolean hasLore(String str, List<String> lore) {
        if (lore == null)
            return false;
        boolean has = false;
        for (String l : lore) {
            if (l.equals(str)) {
                has = true;
                break;
            }
        }
        return has;
    }

}
