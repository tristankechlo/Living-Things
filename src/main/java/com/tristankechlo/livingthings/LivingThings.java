package com.tristankechlo.livingthings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tristankechlo.livingthings.client.RenderHandler;
import com.tristankechlo.livingthings.config.LivingThingsConfig;
import com.tristankechlo.livingthings.config.misc.ConfigManager;
import com.tristankechlo.livingthings.events.BlockEventHandler;
import com.tristankechlo.livingthings.init.ModBlocks;
import com.tristankechlo.livingthings.init.ModEntityTypes;
import com.tristankechlo.livingthings.init.ModItems;
import com.tristankechlo.livingthings.init.ModParticle;
import com.tristankechlo.livingthings.init.ModSounds;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LivingThings.MOD_ID)
public class LivingThings {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "livingthings";
	public static boolean patchouliLoaded = false;

	public LivingThings() {
		ModLoadingContext.get().registerConfig(Type.COMMON, LivingThingsConfig.spec, "livingthings/livingthings.toml");
		ConfigManager.setup();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		ModItems.ITEMS.register(modEventBus);
		ModBlocks.BLOCKS.register(modEventBus);
		ModSounds.SOUNDS.register(modEventBus);
		ModParticle.PARTICLES.register(modEventBus);
		ModEntityTypes.ENTITY_TYPES.register(modEventBus);

		modEventBus.addListener(this::ClientSetup);
		modEventBus.addListener(this::CommonSetup);

		MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void ClientSetup(final FMLClientSetupEvent event) {
		RenderHandler.registerEntityRenders();
	}

	public void CommonSetup(final FMLCommonSetupEvent event) {
		LivingThings.patchouliLoaded = ModList.get().isLoaded("patchouli");
	}

}