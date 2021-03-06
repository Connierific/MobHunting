package one.lindegaard.MobHunting.compatibility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import mc.alk.arena.objects.ArenaPlayer;
import one.lindegaard.MobHunting.Messages;

public class BattleArenaHelper {

	// ***************************************************************************
	// Integration to BattleArena
	// ***************************************************************************
	private static List<UUID> playersPlayingBattleArena = new ArrayList<UUID>();

	/**
	 * Determine if the player is currently playing BattleArena
	 * 
	 * @param player
	 * @return Returns true when the player is in game.
	 */
	public static boolean isPlayingBattleArena(Player player) {
		return playersPlayingBattleArena.contains(player.getUniqueId());
	}

	/**
	 * Add the player to the list of active BattleArena players.
	 * 
	 * @param arenaPlayer
	 */
	public static void startPlayingBattleArena(ArenaPlayer arenaPlayer) {
		playersPlayingBattleArena.add(arenaPlayer.getID());
	}

	/**
	 * Remove the player from list of active BattleArena players
	 * 
	 * @param arenaPlayer
	 */
	public static void stopPlayingBattleArena(ArenaPlayer arenaPlayer) {
		if (!playersPlayingBattleArena.remove(arenaPlayer.getID())) {
			Messages.debug("Player: %s is not a the BattleArena", arenaPlayer.getName());
		}
	}

}
