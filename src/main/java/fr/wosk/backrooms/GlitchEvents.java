package fr.wosk.backrooms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Gère :
 *  - l'apparition aléatoire du bloc glitché dans le monde normal
 *  - la séquence quand un joueur marche dessus (noclip, noir, sons, lumières)
 */
public class GlitchEvents {
	// ====== RÉGLAGES (tu peux les changer) ======
	/** Durée totale de la séquence en ticks (20 ticks = 1 seconde). */
	private static final int TOTAL_TICKS = 260;
	/** Chaque seconde, 1 chance sur X qu'un bloc glitché apparaisse près d'un joueur (900 = ~15 min en moyenne). */
	private static final int SPAWN_CHANCE = 900;
	// ============================================

	private static final Random RANDOM = new Random();
	private static final Map<UUID, Sequence> ACTIVE = new HashMap<>();

	private static final SoundEvent[] WEIRD_SOUNDS = {
		SoundEvents.WARDEN_NEARBY_CLOSE,
		SoundEvents.ENDERMAN_STARE,
		SoundEvents.ELDER_GUARDIAN_CURSE,
		SoundEvents.SCULK_SHRIEKER_SHRIEK,
		SoundEvents.PORTAL_AMBIENT,
		SoundEvents.GHAST_AMBIENT,
		SoundEvents.ENDERMAN_SCREAM
	};

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(GlitchEvents::onServerTick);
		// Si le joueur quitte pendant la séquence, on le remet normal
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.getPlayer();
			Sequence sequence = ACTIVE.remove(player.getUUID());
			if (sequence != null) {
				sequence.restore(player);
			}
		});
	}

	/** Lance la séquence. Renvoie false si elle n'a pas pu démarrer (déjà en cours, créatif...). */
	public static boolean startSequence(ServerPlayer player) {
		if (ACTIVE.containsKey(player.getUUID())) {
			return false;
		}
		GameType mode = player.gameMode.getGameModeForPlayer();
		if (mode != GameType.SURVIVAL && mode != GameType.ADVENTURE) {
			return false; // les joueurs en créatif peuvent marcher dessus sans rien déclencher
		}
		ACTIVE.put(player.getUUID(), new Sequence(player, mode));
		return true;
	}

	private static void onServerTick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Sequence>> it = ACTIVE.entrySet().iterator();
		while (it.hasNext()) {
			Sequence sequence = it.next().getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(sequence.playerId);
			if (player == null) {
				it.remove();
				continue;
			}
			if (sequence.tick(player)) {
				it.remove();
			}
		}

		if (server.getTickCount() % 20 == 0) {
			trySpawnGlitchBlocks(server);
		}
	}

	/** Fait apparaître (rarement) un bloc glitché à la surface, près d'un joueur en survie. */
	private static void trySpawnGlitchBlocks(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (RANDOM.nextInt(SPAWN_CHANCE) != 0) continue;
			if (ACTIVE.containsKey(player.getUUID())) continue;
			if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) continue;

			ServerLevel level = player.serverLevel();
			if (level.dimension() != Level.OVERWORLD) continue;
			if (!level.canSeeSky(player.blockPosition())) continue;

			double angle = RANDOM.nextDouble() * Math.PI * 2;
			double distance = 8 + RANDOM.nextDouble() * 8;
			int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
			if (!level.hasChunkAt(new BlockPos(x, 64, z))) continue;

			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			BlockPos below = pos.below();

			if (!level.getBlockState(pos).isAir()) continue;
			if (!level.getFluidState(below).isEmpty()) continue;
			if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;

			level.setBlock(pos, ModBlocks.GLITCH_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	private static class Sequence {
		final UUID playerId;
		final GameType previousMode;
		final ServerLevel level;
		final double x, y, z;
		final float yRot, xRot;
		int age = 0;

		Sequence(ServerPlayer player, GameType previousMode) {
			this.playerId = player.getUUID();
			this.previousMode = previousMode;
			this.level = player.serverLevel();
			this.x = player.getX();
			this.y = player.getY();
			this.z = player.getZ();
			this.yRot = player.getYRot();
			this.xRot = player.getXRot();

			// Noclip : le mode spectateur laisse traverser les blocs
			player.setGameMode(GameType.SPECTATOR);
			// Petit coup vers le bas : on "tombe" à travers le sol
			player.setDeltaMovement(0.0, -0.6, 0.0);
			player.hurtMarked = true;
			level.playSound(null, x, y, z, SoundEvents.BEACON_DEACTIVATE, SoundSource.MASTER, 1.5f, 0.5f);
		}

		/** Renvoie true quand la séquence est terminée. */
		boolean tick(ServerPlayer player) {
			age++;

			// Le noir arrive d'un coup après une seconde
			if (age == 20) {
				int duration = TOTAL_TICKS - 20 + 10;
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false, false));
				player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 0, false, false, false));
			}

			// Sons très chelous dans le noir
			if (age > 30 && age < TOTAL_TICKS - 10) {
				if (age % 25 == 0) {
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.WARDEN_HEARTBEAT, SoundSource.MASTER, 2.0f, 0.7f);
				}
				if (RANDOM.nextInt(25) == 0) {
					SoundEvent sound = WEIRD_SOUNDS[RANDOM.nextInt(WEIRD_SOUNDS.length)];
					double sx = player.getX() + (RANDOM.nextDouble() - 0.5) * 16;
					double sy = player.getY() + (RANDOM.nextDouble() - 0.5) * 6;
					double sz = player.getZ() + (RANDOM.nextDouble() - 0.5) * 16;
					float pitch = 0.5f + RANDOM.nextFloat() * 0.4f;
					level.playSound(null, sx, sy, sz, sound, SoundSource.MASTER, 1.5f, pitch);
				}
			}

			if (age >= TOTAL_TICKS) {
				finish(player);
				return true;
			}
			return false;
		}

		/** Les lumières s'allument. Plus tard (étape 2) : téléportation vers le Level 0. */
		void finish(ServerPlayer player) {
			restore(player);
			level.playSound(null, x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 1.5f, 0.5f);
			player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 30));
			player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("(bientôt...)")));
			player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("LEVEL 0")));
		}

		/** Remet le joueur normal (effets, position, mode de jeu). */
		void restore(ServerPlayer player) {
			player.removeEffect(MobEffects.BLINDNESS);
			player.removeEffect(MobEffects.DARKNESS);
			player.teleportTo(level, x, y, z, yRot, xRot);
			player.setGameMode(previousMode);
		}
	}
}
