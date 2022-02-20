package dev.tr7zw.minimepets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MiniMeShared {

    public static final Logger LOGGER = LogManager.getLogger("MiniMePets");
    public static MiniMeShared instance;
    
    public void init() {
        instance = this;
        LOGGER.info("Loading MiniMePets!");
    }
    
}
