package com.makeforge.boombox;

import com.makeforge.boombox.item.GunConfig;
import com.makeforge.boombox.item.GunItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BoomBox implements ModInitializer {
    public static final String MOD_ID = "boombox";

    public static Item BEAT_PISTOL;
    public static Item RHYTHM_RIFLE;
    public static Item FREQUENCY_SNIPER;
    public static Item BASS_BAZOOKA;
    public static Item DROP_LAUNCHER;
    public static Item BLAST_SHOTGUN;
    public static Item TEMPO_MINIGUN;
    public static Item SONIC_RAILGUN;

    @Override
    public void onInitialize() {
        BEAT_PISTOL      = registerGun("beat_pistol",      GunConfig.pistol());
        RHYTHM_RIFLE     = registerGun("rhythm_rifle",     GunConfig.ak47());
        FREQUENCY_SNIPER = registerGun("frequency_sniper", GunConfig.sniper());
        BASS_BAZOOKA     = registerGun("bass_bazooka",     GunConfig.bazooka());
        DROP_LAUNCHER    = registerGun("drop_launcher",    GunConfig.grenade());
        BLAST_SHOTGUN    = registerGun("blast_shotgun",    GunConfig.shotgun());
        TEMPO_MINIGUN    = registerGun("tempo_minigun",    GunConfig.minigun());
        SONIC_RAILGUN    = registerGun("sonic_railgun",    GunConfig.railgun());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(BEAT_PISTOL);
            entries.add(RHYTHM_RIFLE);
            entries.add(FREQUENCY_SNIPER);
            entries.add(BASS_BAZOOKA);
            entries.add(DROP_LAUNCHER);
            entries.add(BLAST_SHOTGUN);
            entries.add(TEMPO_MINIGUN);
            entries.add(SONIC_RAILGUN);
        });
    }

    private static Item registerGun(String name, GunConfig config) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        Item item = new GunItem(new Item.Settings().registryKey(key).maxCount(1), config);
        return Registry.register(Registries.ITEM, key, item);
    }
}
