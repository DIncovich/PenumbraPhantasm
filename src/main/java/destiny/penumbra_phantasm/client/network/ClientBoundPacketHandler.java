package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxMetrics;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxScript;
import destiny.penumbra_phantasm.client.render.screen.DarknessFallScreen;
import destiny.penumbra_phantasm.client.render.screen.EggRoomCoverScreen;
import destiny.penumbra_phantasm.client.render.screen.FireDoorScreen;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.fountain.FireDoor;
import destiny.penumbra_phantasm.server.network.ServerBoundFireDoorPacket;
import destiny.penumbra_phantasm.server.network.ServerBoundIntroPacket;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ClientBoundPacketHandler
{
	public static void openIntroScreen(BlockPos pos, ResourceKey<Level> dim)
	{
		Minecraft minecraft = Minecraft.getInstance();

		minecraft.setScreen(new IntroScreen(() -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundIntroPacket(pos, dim));
		}));
	}

	public static void openFireDoorScreen(List<FireDoor> fireDoors, ResourceKey<Level> originDarkWorld, BlockPos originPos) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new FireDoorScreen(fireDoors, originDarkWorld, originPos, chosenDoor -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundFireDoorPacket(chosenDoor.darkWorld(), chosenDoor.doorPos(), chosenDoor.facingAngle(),
					originDarkWorld, originPos));
		}));
	}

	public static void openDarknessFallScreen(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dim,
			boolean narrowGreatDoorPrepare, BlockPos arrivalGreatDoorAnchor) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new DarknessFallScreen(() -> minecraft.setScreen(null), destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dim,
				narrowGreatDoorPrepare, arrivalGreatDoorAnchor));
	}

	public static void openEggRoomCover(ResourceKey<Level> dim, int chunkX, int chunkZ) {
		EggRoomCoverScreen.open(dim, chunkX, chunkZ);
	}

	public static void syncSoulBreak(boolean diedWithSoulHearth, int soulType)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null)
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
				{
					cap.diedWithSoulHearth = diedWithSoulHearth;
					cap.soulType = soulType;
				});
	}

	public static void syncSoulStuff(boolean seenIntro, boolean diedWithSoulHearth, int soulType, int determination, int connectionLevel, int eggRoomManGone, int eggObtained) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null) {
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
				cap.seenIntro = seenIntro;
				cap.diedWithSoulHearth = diedWithSoulHearth;
				cap.soulType = soulType;
				cap.determination = determination;
				cap.connectionLevel = connectionLevel;
				cap.eggRoomManGone = eggRoomManGone;
				cap.eggObtained = eggObtained;
			});
		}
	}

	public static void openTextBox(String scriptId) {
		if (DarkWorldDialogue.isActive()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !DarkWorldUtil.isDarkWorld(minecraft.player.level())) {
			return;
		}
		DarkWorldDialogue.start(createTextBoxScript(scriptId));
	}

	private static TextBoxScript createTextBoxScript(String id) {
		TextBoxScript script = new TextBoxScript().id(id);
		switch (id) {
			case ClientBoundTextBoxPacket.TREE_FRONT -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.he_is_behind"));
			case ClientBoundTextBoxPacket.TREE_FRONT_GONE -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.it_is_a_tree"));
			case ClientBoundTextBoxPacket.TREE_BEHIND -> script
					.speed(TextBoxMetrics.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.man_here"))
					.waitAfter(',', TextBoxMetrics.WAIT_AFTER_WELL)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.offered"))
					.choices();
			case ClientBoundTextBoxPacket.TREE_BEHIND_GONE -> script
					.speed(TextBoxMetrics.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.no_man"))
					.waitAfter(',', TextBoxMetrics.WAIT_AFTER_WELL);
			case ClientBoundTextBoxPacket.RECEIVED_EGG -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.received"), ClientBoundPacketHandler::playEggAcquire);
			case ClientBoundTextBoxPacket.THEN_NEEDNT -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.neednt"));
			case ClientBoundTextBoxPacket.USED_EGG -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.used"), ClientBoundPacketHandler::playEggAcquire);
			default -> script.line(Component.literal(id));
		}
		return script;
	}

	private static void playEggAcquire() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.level().playSound(player, player.blockPosition(), SoundRegistry.EGG_ACQUIRE.get(), SoundSource.PLAYERS, 1f, 1f);
		}
	}

	public static void sendParticle(ResourceLocation particleId, double x, double y, double z, double vx, double vy, double vz, int count) {
		Level level = Minecraft.getInstance().level;

		if (level == null) return;

		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);

		if (!(type instanceof SimpleParticleType simpleType)) return;

		for (int i = 0; i < count; i++) {
			level.addParticle(simpleType, x, y, z, vx, vy, vz);
		}
	}

	@SuppressWarnings("unchecked")
	public static void playPlayerAnimation(int entityId, ResourceLocation animationId) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		Entity entity = level.getEntity(entityId);
		if (!(entity instanceof AbstractClientPlayer player)) return;

		ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess
				.getPlayerAssociatedData(player)
				.get(animationId);

		if (animation != null) {
			animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(animationId)));
		}
	}

	@SuppressWarnings("unchecked")
	public static void cancelPlayerAnimation(int entityId, ResourceLocation animationId) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		Entity entity = level.getEntity(entityId);
		if (!(entity instanceof AbstractClientPlayer player)) return;

		ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess
				.getPlayerAssociatedData(player)
				.get(animationId);

		if (animation != null) {
			animation.setAnimation(null);
		}
	}
}
