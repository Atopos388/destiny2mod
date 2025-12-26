package com.Atopos.destiny2mod.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final String CATEGORY = "key.destiny2mod.category";

    public static final KeyMapping GRENADE_KEY = new KeyMapping(
            "key.destiny2mod.grenade_ability",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping MELEE_KEY = new KeyMapping(
            "key.destiny2mod.melee_ability",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );

    // [新增] 诊断按键
    public static final KeyMapping DIAG_KEY = new KeyMapping(
            "key.destiny2mod.diagnostic_tool",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            CATEGORY
    );
}