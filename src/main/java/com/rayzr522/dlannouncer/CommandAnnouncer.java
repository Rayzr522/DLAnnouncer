/**
 * 
 */
package com.rayzr522.dlannouncer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Rayzr
 *
 */
public class CommandAnnouncer implements CommandExecutor {

    private DLAnnouncer plugin;

    public CommandAnnouncer(DLAnnouncer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("DLAnnouncer.admin")) {
            tell(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            tell(sender, "usage");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("add")) {

            List<String> broadcasts = plugin.getConfig().getStringList("broadcasts");
            broadcasts.add(getRestOf(args));

            plugin.set("broadcasts", broadcasts);
            tell(sender, "added-message");

        } else if (sub.equals("say")) {

            plugin.broadcast(getRestOf(args));

        } else if (sub.equals("prefix")) {

            plugin.set("prefix", getRestOf(args));
            tell(sender, "set-prefix");

        } else if (sub.equals("suffix")) {

            plugin.set("suffix", getRestOf(args));
            tell(sender, "set-suffix");

        } else if (sub.equals("reload")) {

            plugin.reload();
            tell(sender, "reloaded");

        } else {

            tell(sender, "usage");

        }

        return true;
    }

    private String getRestOf(String[] args) {
        if (args.length <= 1)
            return "";
        return Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.joining(" "));
    }

    private void tell(CommandSender sender, String key) {
        String location = "message." + key;
        String message = plugin.getConfig().isList(location)
                ? plugin.getConfig().getStringList(location).stream().collect(Collectors.joining("\n"))
                : plugin.getConfig().getString(location);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
