package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.core.MsgSender;
import top.oasismc.oasisguild.bukkit.listener.GuildChunkListener;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.api.job.Jobs.ADVANCED;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public class ChunkCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new ChunkCommand();

    private ChunkCommand() {
        super("chunk", null);
        regSubCommand(ChunkAddCommand.INSTANCE.getSubCommand(), ChunkAddCommand.INSTANCE);
        regSubCommand(ChunkRemoveCommand.INSTANCE.getSubCommand(), ChunkRemoveCommand.INSTANCE);
        regSubCommand(ChunkConfirmCommand.INSTANCE.getSubCommand(), ChunkConfirmCommand.INSTANCE);
        regSubCommand(ChunkCancelCommand.INSTANCE.getSubCommand(), ChunkCancelCommand.INSTANCE);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        String gName = getDataManager().getGuildNameByPlayer(sender.getName());
        if (gName == null) {
            sendMsg(sender, "command.chunk.notJoinGuild");
            return true;
        }
        int job = getDataManager().getPlayerJob(gName, sender.getName());
        if (job < ADVANCED) {
            sendMsg(sender, "command.chunk.notLeader");
            return true;
        }
        ISubCommand subCommand = getSubCommands().get(args.get(0));
        if (subCommand == null) {
            MsgSender.sendMsg(sender, "unknown_cmd");
        } else {
            subCommand.onCommand(sender, args.subList(1, args.size()));
        }
        return true;
    }

    static final class ChunkAddCommand extends AbstractSubCommand {

        public static final ISubCommand INSTANCE = new ChunkAddCommand();

        private ChunkAddCommand() { super("add", null); }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            sendMsg(sender, "command.chunk.start");
            GuildChunkListener.getListener().startChunkAddSelect((Player) sender);
            return true;
        }

    }

    static final class ChunkRemoveCommand extends AbstractSubCommand {

        public static final ISubCommand INSTANCE = new ChunkRemoveCommand();

        private ChunkRemoveCommand() { super("remove", null); }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            sendMsg(sender, "command.chunk.start");
            GuildChunkListener.getListener().startChunkRemoveSelect((Player) sender);
            return true;
        }

    }

    static final class ChunkConfirmCommand extends AbstractSubCommand {

        public static final ISubCommand INSTANCE = new ChunkConfirmCommand();

        private ChunkConfirmCommand() { super("confirm", null); }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            String gName = getDataManager().getGuildNameByPlayer(sender.getName());
            List<IGuildChunk> chunkList = GuildChunkListener.getListener().getSelChunkMap().get(gName);
            if (chunkList == null || chunkList.size() == 0) {
                sendMsg(sender, "command.chunk.notSelect");
                return true;
            }
            switch (GuildChunkListener.getListener().getChunkSelSwitchMap().getOrDefault(((Player) sender).getUniqueId(), 1)) {
                case 1:
                    GuildManager.addGuildChunks(gName, chunkList);
                    sendMsg(sender, "command.chunk.confirm");
                    break;
                case -1:
                    GuildManager.removeGuildChunks(gName, chunkList);
                    sendMsg(sender, "command.chunk.delete");
                    break;
            }
            GuildChunkListener.getListener().endChunkSelect((Player) sender);
            return true;
        }

    }

    static final class ChunkCancelCommand extends AbstractSubCommand {

        public static final ISubCommand INSTANCE = new ChunkCancelCommand();

        private ChunkCancelCommand() { super("cancel", null); }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            sendMsg(sender, "command.chunk.cancel");
            GuildChunkListener.getListener().endChunkSelect((Player) sender);
            return true;
        }

    }

}



