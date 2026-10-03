package fr.wosk.backrooms;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackroomsMod implements ModInitializer {
	public static final String MOD_ID = "backrooms";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		GlitchEvents.register();
		LOGGER.info("Backrooms : bonne chance...");
	}
}
