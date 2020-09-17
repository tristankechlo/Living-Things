package com.tristankechlo.livingthings;

import org.apache.logging.log4j.LogManager;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import com.tristankechlo.livingthings.client.renderer.RenderHandler;
import com.tristankechlo.livingthings.config.LivingThingsConfig;
import com.tristankechlo.livingthings.init.ModBlocks;
import com.tristankechlo.livingthings.init.ModItems;
import com.tristankechlo.livingthings.init.ModSounds;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(LivingThings.MOD_ID)
public class LivingThings {
	
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "livingthings";
    
    public LivingThings() {    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LivingThingsConfig.spec);
    	
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
		
        modEventBus.addListener(this::ClientSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void ClientSetup(final FMLClientSetupEvent event) {
    	RenderHandler.registerEntityRenders();
    }
                    
}