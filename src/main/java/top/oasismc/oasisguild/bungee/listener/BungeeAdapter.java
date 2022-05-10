package top.oasismc.oasisguild.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import top.oasismc.oasisguild.bukkit.OasisGuild;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum BungeeAdapter implements PluginMessageListener {

    INSTANCE;

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String msg = in.readUTF();
        if (msg.equals("OasisGuildUpdateData")) {
            DataManager.getDataManager().reloadData();
        }
    }

    public void sendUpdateDataMsg() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("OasisGuildUpdateData");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(bytes);
        try {
            outStream.writeUTF("updateData");
            outStream.writeShort(128);
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.writeShort(bytes.toByteArray().length);
        out.write(bytes.toByteArray());

        Bukkit.getServer().sendPluginMessage(OasisGuild.getPlugin(), "BungeeCord", out.toByteArray());
    }

}
