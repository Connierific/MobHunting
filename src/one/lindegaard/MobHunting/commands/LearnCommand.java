package one.lindegaard.MobHunting.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.storage.DataStoreManager;
import one.lindegaard.MobHunting.storage.PlayerSettings;

public class LearnCommand implements ICommand {

	public LearnCommand() {
	}

	// Used case
	// /mh learn - args.length = 0 || arg[0]=""

	@Override
	public String getName() {
		return "learn";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "learnmode" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.learn";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to enable/disable learningmode.",
				ChatColor.GOLD + label + ChatColor.GREEN + " playername" + ChatColor.WHITE
						+ " - to enable/disable learningmode for a specific player." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.learn.description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length == 0) {
			togglePlayerLearningMode((Player) sender);
			return true;
		} else if (args.length == 1) {
			DataStoreManager ds = MobHunting.getDataStoreManager();
			Player player = (Player) ds.getPlayerByName(args[0]);
			if (player != null) {
				if (sender.hasPermission("mobhunting.learn.other") || sender instanceof ConsoleCommandSender) {
					togglePlayerLearningMode(player);
				} else {
					sender.sendMessage(
							ChatColor.RED + "You dont have permission " + ChatColor.AQUA + "'mobhunting.learn.other'");
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
				return false;
			}
		}
		return false;
	}

	private void togglePlayerLearningMode(Player player) {
		DataStoreManager ds = MobHunting.getDataStoreManager();
		boolean mm = MobHunting.getPlayerSettingsmanager().getPlayerSettings(player).isMuted();
		if (MobHunting.getPlayerSettingsmanager().getPlayerSettings(player).isLearningMode()) {
			ds.updatePlayerSettings(player, false, mm);
			MobHunting.getPlayerSettingsmanager().setPlayerSettings(player, new PlayerSettings(player, false, mm));
			player.sendMessage(Messages.getString("mobhunting.commands.learn.disabled", "player", player.getName()));
		} else {
			MobHunting.getPlayerSettingsmanager().setPlayerSettings(player, new PlayerSettings(player, true, mm));
			player.sendMessage(Messages.getString("mobhunting.commands.learn.enabled", "player", player.getName()));
		}
	}

}
