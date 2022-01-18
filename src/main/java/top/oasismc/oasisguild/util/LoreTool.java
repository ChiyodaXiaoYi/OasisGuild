package top.oasismc.oasisguild.util;

import java.util.List;

public class LoreTool {

    public static boolean hasLore(String need, List<String> lore) {
        if (lore == null)
            return false;
        boolean has = false;
        for (String l : lore) {
            if (l.equals(need)) {
                has = true;
                break;
            }
        }
        return has;
    }

}
