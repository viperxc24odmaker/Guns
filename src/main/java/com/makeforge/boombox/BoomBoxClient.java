package com.makeforge.boombox;

import com.makeforge.boombox.item.GunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class BoomBoxClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((DrawContext ctx, Object tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            if (player == null || mc.options.hudHidden) return;

            ItemStack held = player.getMainHandStack();
            if (!(held.getItem() instanceof GunItem)) return;

            int ammo = 0;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (s.isOf(Items.FIREWORK_STAR)) ammo += s.getCount();
            }
            boolean creative = player.getAbilities().creativeMode;

            String name = held.getName().getString();
            String ammoStr = creative ? "\u221E" : ("\u2726 " + ammo);
            int color = (!creative && ammo == 0) ? 0xFFFF5555 : 0xFFFFFFFF;

            int w = ctx.getScaledWindowWidth();
            int h = ctx.getScaledWindowHeight();
            int x = w - 6;
            int y = h - 62;

            int nameW = mc.textRenderer.getWidth(name);
            int ammoW = mc.textRenderer.getWidth(ammoStr);
            int boxW = Math.max(nameW, ammoW) + 10;

            ctx.fill(x - boxW, y - 3, x + 2, y + 21, 0x99000000);
            ctx.fill(x - boxW, y - 3, x - boxW + 2, y + 21, 0xFF6EBEFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(name), x - nameW, y, 0xFF6EBEFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(ammoStr), x - ammoW, y + 11, color);
        });
    }
}
