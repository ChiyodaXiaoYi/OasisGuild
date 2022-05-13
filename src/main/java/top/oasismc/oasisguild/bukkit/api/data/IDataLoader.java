package top.oasismc.oasisguild.bukkit.api.data;

import java.sql.Connection;

public interface IDataLoader {
    Connection getConnection();

    void loadTables();
}
