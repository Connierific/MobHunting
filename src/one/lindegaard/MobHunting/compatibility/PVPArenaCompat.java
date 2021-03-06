package one.lindegaard.MobHunting.compatibility;

import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.events.PALeaveEvent;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PVPArenaCompat implements Listener {

	private static boolean supported = false;
	private Plugin mPlugin;

	public PVPArenaCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with PvpArena is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("PVPArena");
			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
			Bukkit.getLogger().info("[MobHunting] Enabling Compatibility with PVPArena ("
					+ mPlugin.getDescription().getVersion() + ")");
			supported = true;
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	public static boolean isSupported() {
		return supported;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationPvpArena;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationPvpArena;
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPvpPlayerJoin(PAJoinEvent event) {
		Messages.debug("[MH]Player %s joined PVPArena: %s", event.getPlayer().getName(), event.getArena());
		MobArenaHelper.startPlayingMobArena(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPvpPlayerDeath(PADeathEvent event) {
		Messages.debug("[MH]Player %s died in PVPArena: %s", event.getPlayer().getName(), event.getArena());
		MobArenaHelper.startPlayingMobArena(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPvpPlayerLeave(PALeaveEvent event) {
		Messages.debug("[MH]Player %s left PVPArena: %s", event.getPlayer().getName(), event.getArena());
		MobArenaHelper.startPlayingMobArena(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPAExit(PAExitEvent event) {
		Messages.debug("[MH]Player %s exit PVPArena: %s", event.getPlayer().getName(), event.getArena());
		MobArenaHelper.startPlayingMobArena(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPADeath(PADeathEvent event) {
		Messages.debug("[MH]Player %s died in PVPArena: %s", event.getPlayer().getName(), event.getArena());
		MobArenaHelper.startPlayingMobArena(event.getPlayer());
	}

	// More events at
	// https://github.com/slipcor/pvparena/tree/master/src/net/slipcor/pvparena/events

}
