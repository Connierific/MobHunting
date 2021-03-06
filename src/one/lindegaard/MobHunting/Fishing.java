package one.lindegaard.MobHunting;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.modifier.IModifier;

public class Fishing implements Listener {

	public Fishing() {

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void Fish(PlayerFishEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player == null)
			return;

		State state = event.getState();
		Messages.debug("FishingEvent: %s %s", player.getName(),state);
		switch (state) {
		case CAUGHT_FISH:
			// When a player has successfully caught a fish and is reeling it
			// in.
			// break;
		case CAUGHT_ENTITY:
			// When a player has successfully caught an entity
			if (player.getGameMode() != GameMode.SURVIVAL) {
				Messages.debug("FishingBlocked: %s is not in survival mode", player.getName());
				Messages.learn(player, Messages.getString("mobhunting.learn.survival"));
				return;
			}

			Entity fish = event.getCaught();
			if (fish == null)
				return;
			ItemStack is = ((Item) fish).getItemStack();
			if (is.getType() == Material.RAW_FISH) {
				Messages.debug("%s caught a %s ", player.getName(), is.getData());

				// Calculate basic the reward
				ExtendedMob eMob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(fish);
				if (eMob.getMob_id() == 0) {
					Bukkit.getLogger().warning("Unknown Mob:" + eMob.getName() + " from plugin " + eMob.getMobPlugin());
					Bukkit.getLogger().warning("Please report this to developer!");
					return;
				}
				double cash = MobHunting.getConfigManager().getBaseKillPrize(fish);

				Messages.debug("Basic Prize=%s for catching a %s", cash, eMob.getName());

				// Pay the reward to player and assister
				if ((cash >= MobHunting.getConfigManager().minimumReward)
						|| (cash <= -MobHunting.getConfigManager().minimumReward)) {

					// Handle MobHuntFishingEvent
					// MobHuntKillEvent event2 = new MobHuntKillEvent(data,
					// info, killed, killer);
					// Bukkit.getPluginManager().callEvent(event2);
					// if (event2.isCancelled()) {
					// Messages.debug("KillBlocked %s: MobHuntKillEvent was
					// cancelled", killer.getName());
					// return;
					// }

					// Apply the modifiers to Basic reward
					double multiplier = 1.0;
					ArrayList<String> modifiers = new ArrayList<String>();
					for (IModifier mod : MobHunting.getMobHuntingManager().getFishingModifiers()) {
						if (mod.doesApply(fish, player, null, null, null)) {
							double amt = mod.getMultiplier(fish, player, null, null, null);
							if (amt != 1.0) {
								modifiers.add(mod.getName());
								multiplier *= amt;
								Messages.debug("Multiplier: %s = %s", mod.getName(), amt);
							}
						}
					}

					String extraString = "";

					// Only display the multiplier if its not 1
					if (Math.abs(multiplier - 1) > 0.05)
						extraString += String.format("x%.1f", multiplier);

					// Add on modifiers
					for (String modifier : modifiers)
						extraString += ChatColor.WHITE + " * " + modifier;

					cash *= multiplier;

					if (cash >= MobHunting.getConfigManager().minimumReward) {
						MobHunting.getRewardManager().depositPlayer(player, cash);
						Messages.debug("%s got a reward (%s)", player.getName(),
								MobHunting.getRewardManager().format(cash));
					} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
						MobHunting.getRewardManager().withdrawPlayer(player, -cash);
						Messages.debug("%s got a penalty (%s)", player.getName(),
								MobHunting.getRewardManager().format(cash));
					}

					// Record the kill in the Database
					if (player != null) {
						Messages.debug("RecordFishing: %s caught a %s (%s)", player.getName(), eMob.getName(),
								eMob.getMobPlugin().name());
						MobHunting.getDataStoreManager().recordKill(player, eMob, player.hasMetadata("MH:hasBonus"));
					}

					// Handle Muted mode
					boolean fisherman_muted = false;
					if (MobHunting.getPlayerSettingsmanager().containsKey(player))
						fisherman_muted = MobHunting.getPlayerSettingsmanager().getPlayerSettings(player).isMuted();

					// Tell the player that he got the reward/penalty, unless
					// muted
					if (!fisherman_muted)
						if (extraString.trim().isEmpty()) {
							if (cash >= MobHunting.getConfigManager().minimumReward) {
								if (!MobHunting.getConfigManager().dropMoneyOnGroup)
									Messages.playerActionBarMessage(player,
											ChatColor.GREEN + "" + ChatColor.ITALIC
													+ Messages.getString("mobhunting.fishcaught.reward", "prize",
															MobHunting.getRewardManager().format(cash)));
							} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
								Messages.playerActionBarMessage(player,
										ChatColor.RED + "" + ChatColor.ITALIC
												+ Messages.getString("mobhunting.fishcaught.penalty", "prize",
														MobHunting.getRewardManager().format(cash)));
							}

						} else {
							if (cash >= MobHunting.getConfigManager().minimumReward) {
								Messages.playerActionBarMessage(player,
										ChatColor.GREEN + "" + ChatColor.ITALIC
												+ Messages.getString("mobhunting.fishcaught.reward", "prize",
														MobHunting.getRewardManager().format(cash)));
							} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
								Messages.playerActionBarMessage(player,
										ChatColor.RED + "" + ChatColor.ITALIC
												+ Messages.getString("mobhunting.fishcaught.penalty", "prize",
														MobHunting.getRewardManager().format(cash)));
							} else
								Messages.debug("FishingBlocked %s: Reward was less than %s", player.getName(),
										MobHunting.getConfigManager().minimumReward);
						}

					String fishermanPos = player.getLocation().getBlockX() + " " + player.getLocation().getBlockY()
							+ " " + player.getLocation().getBlockZ();
					if (MobHunting.getConfigManager().isCmdGointToBeExcuted(fish)) {
						String worldname = player.getWorld().getName();
						String prizeCommand = MobHunting.getConfigManager().getKillConsoleCmd(fish)
								.replaceAll("\\{player\\}", player.getName())
								.replaceAll("\\{killer\\}", player.getName()).replaceAll("\\{world\\}", worldname)
								.replace("\\{prize\\}", MobHunting.getRewardManager().format(cash))
								.replaceAll("\\{killerpos\\}", fishermanPos);
						Messages.debug("command to be run is:" + prizeCommand);
						if (!MobHunting.getConfigManager().getKillConsoleCmd(fish).equals("")) {
							String str = prizeCommand;
							do {
								if (str.contains("|")) {
									int n = str.indexOf("|");
									Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
											str.substring(0, n));
									str = str.substring(n + 1, str.length()).toString();
								}
							} while (str.contains("|"));
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
						}
					}
					// send a message to the player
					if (!MobHunting.getConfigManager().getKillRewardDescription(fish).equals("") && !fisherman_muted) {
						String worldname = player.getWorld().getName();
						String message = ChatColor.GREEN + "" + ChatColor.ITALIC + MobHunting.getConfigManager()
								.getKillRewardDescription(fish).replaceAll("\\{player\\}", player.getName())
								.replaceAll("\\{killer\\}", player.getName())
								.replace("\\{prize\\}", MobHunting.getRewardManager().format(cash))
								.replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", fishermanPos);

						Messages.debug("Description to be send:" + message);
						player.sendMessage(message);
					}
				}

			} else {
				Messages.debug("FishingBlocked %s: You dont get a reward for catching a %s", player.getName(),
						is.getType());
			}

			break;
		case BITE:
			// Called when there is a bite on the hook and it is ready to be
			// reeled in.
			break;
		case FAILED_ATTEMPT:
			// When a player fails to catch anything while fishing usually due
			// to poor aiming or timing
			break;
		case FISHING:
			// When a player is fishing, ie casting the line out.
			break;
		case IN_GROUND:
			// When a bobber is stuck in the ground
			break;
		// default:
		// break;

		}
	}
}
