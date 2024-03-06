package dev.tr7zw.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.animal.Wolf;

public class MixinTests {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testMixins() {
        Objenesis objenesis = new ObjenesisStd();
        objenesis.newInstance(CatRenderer.class);
        objenesis.newInstance(Wolf.class);
        objenesis.newInstance(WolfRenderer.class);
    }

}