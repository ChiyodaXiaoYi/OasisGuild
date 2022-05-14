package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.ArrayList;
import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public class SetIconCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new SetIconCommand();
    private final List<String> items;

    private SetIconCommand() {
        super("setIcon", null);
        items = new ArrayList<>();
        loadMaterials();
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMsg(sender, "adminCommand.notGuild");
            return true;
        }
        if (args.size() < 2) {
            sendMsg(sender, "adminCommand.missingParam");
            return true;
        }
        IGuild guild = getDataManager().getGuildByName(args.get(0));
        if (guild == null) {
            sendMsg(sender, "adminCommand.notGuild");
            return true;
        }
        Material material = Material.matchMaterial(args.get(1));
        if (material == null) {
            sendMsg(sender, "adminCommand.illegalItems");
            return true;
        }
        DataManager.getDataManager().getGuildDao().setGuildIcon(args.get(0), args.get(1));
        sendMsg(sender, "adminCommand.success");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            List<String> returnList = DataManager.getDataManager().getGuildNameList();
            returnList.removeIf(str -> !str.startsWith(args.get(0)));
            return returnList;
        } else {
            return items;
        }
    }

    private void loadMaterials() {
        for (Material value : Material.values()) {
            items.add(value.name());
        }
    }

}
