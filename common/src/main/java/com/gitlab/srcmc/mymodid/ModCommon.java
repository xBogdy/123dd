package com.gitlab.srcmc.mymodid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.srcmc.mymodid.api.TrainerManager;

public class ModCommon {
    public static final String MOD_ID = "mymodid"; // must match mod_id
    public static final String MOD_NAME = "Example Mod"; // should match mod_name
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final TrainerManager TRAINER_MANAGER = new TrainerManager();

    public ModCommon() {
        TRAINER_MANAGER.load();
    }
}
