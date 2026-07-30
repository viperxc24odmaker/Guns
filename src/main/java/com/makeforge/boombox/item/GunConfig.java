package com.makeforge.boombox.item;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * Holds all the tuning for a single gun. Fluent builder style so the
 * factory methods below read like a stat sheet.
 */
public class GunConfig {
    public enum AmmoTier { LIGHT, HEAVY, EXPLOSIVE }
    public AmmoTier ammo = AmmoTier.LIGHT;
    public float damage = 6f;          // per-hit damage (hitscan guns)
    public int cooldown = 10;          // ticks between shots
    public float range = 48f;          // max reach in blocks
    public float spread = 1.5f;        // inaccuracy in degrees (0 = laser precise)
    public int pellets = 1;            // rays fired per trigger pull (shotgun!)
    public int pierce = 1;             // how many entities one ray punches through
    public float explosionPower = 0f;  // >0 turns the gun into a boomstick
    public boolean fire = false;       // explosion leaves fire
    public int fireSeconds = 0;        // sets target on fire on hit
    public float knockback = 0.3f;     // yeet strength
    public float recoil = 0f;          // pushes the shooter back
    public boolean auto = false;       // full-auto: hold right-click to spray

    public ParticleEffect trail = ParticleTypes.CRIT;
    public ParticleEffect muzzle = ParticleTypes.FLAME;

    public SoundEvent sound = SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST;
    public float volume = 1.4f;
    public float pitch = 1.0f;

    public RegistryEntry<StatusEffect> effect = null; // optional debuff on hit
    public int effectDuration = 0;
    public int effectAmplifier = 0;

    // ---- fluent setters ----
    public GunConfig dmg(float v)        { this.damage = v; return this; }
    public GunConfig cd(int v)           { this.cooldown = v; return this; }
    public GunConfig range(float v)      { this.range = v; return this; }
    public GunConfig spread(float v)     { this.spread = v; return this; }
    public GunConfig pellets(int v)      { this.pellets = v; return this; }
    public GunConfig pierce(int v)       { this.pierce = v; return this; }
    public GunConfig boom(float v)       { this.explosionPower = v; return this; }
    public GunConfig fire(boolean v)     { this.fire = v; return this; }
    public GunConfig burn(int s)         { this.fireSeconds = s; return this; }
    public GunConfig kb(float v)         { this.knockback = v; return this; }
    public GunConfig recoil(float v)     { this.recoil = v; return this; }
    public GunConfig auto()              { this.auto = true; return this; }
    public GunConfig ammo(AmmoTier t)    { this.ammo = t; return this; }
    public GunConfig trail(ParticleEffect p) { this.trail = p; return this; }
    public GunConfig muzzle(ParticleEffect p){ this.muzzle = p; return this; }
    public GunConfig sound(SoundEvent s, float vol, float pit) {
        this.sound = s; this.volume = vol; this.pitch = pit; return this;
    }
    public GunConfig effect(RegistryEntry<StatusEffect> e, int dur, int amp) {
        this.effect = e; this.effectDuration = dur; this.effectAmplifier = amp; return this;
    }

    // =====================================================================
    //  THE ARSENAL (all cranked WAY up because Divine said super strong 😤)
    // =====================================================================

    /** Beat Pistol — snappy sidearm, respectable punch. */
    public static GunConfig pistol() {
        return new GunConfig()
                .dmg(14f).cd(5).range(56f).spread(1.0f).kb(0.4f)
                .trail(ParticleTypes.CRIT).muzzle(ParticleTypes.FLAME)
                .sound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.3f, 1.5f);
    }

    /** Rhythm Rifle (AK) — fast, punchy, a little spray. */
    public static GunConfig ak47() {
        return new GunConfig()
                .dmg(11f).cd(2).range(72f).spread(2.4f).kb(0.25f).recoil(0.04f).auto()
                .trail(ParticleTypes.CRIT).muzzle(ParticleTypes.FLAME)
                .sound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.1f, 1.7f)
                .effect(StatusEffects.SLOWNESS, 30, 0);
    }

    /** Frequency Sniper — laser accurate, hits like a truck, marks + pierces. */
    public static GunConfig sniper() {
        return new GunConfig()
                .dmg(48f).cd(32).range(220f).spread(0.0f).pierce(3).kb(1.6f).recoil(0.35f).ammo(AmmoTier.HEAVY)
                .trail(ParticleTypes.END_ROD).muzzle(ParticleTypes.END_ROD)
                .sound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 1.6f, 1.2f)
                .effect(StatusEffects.GLOWING, 80, 0);
    }

    /** Bass Bazooka — big single explosive shell, leaves fire. */
    public static GunConfig bazooka() {
        return new GunConfig()
                .cd(42).range(140f).spread(0.6f).boom(6.5f).fire(true).recoil(0.7f).ammo(AmmoTier.EXPLOSIVE)
                .trail(ParticleTypes.FLAME).muzzle(ParticleTypes.LARGE_SMOKE)
                .sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.8f, 0.9f);
    }

    /** Drop Launcher (grenade) — faster, smaller boom, spammier. */
    public static GunConfig grenade() {
        return new GunConfig()
                .cd(22).range(100f).spread(1.4f).boom(4.0f).recoil(0.3f).ammo(AmmoTier.EXPLOSIVE)
                .trail(ParticleTypes.FLAME).muzzle(ParticleTypes.SMOKE)
                .sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.4f, 1.3f);
    }

    /** Blast Shotgun — 9 pellets of pain up close, huge knockback. */
    public static GunConfig shotgun() {
        return new GunConfig()
                .dmg(9f).cd(15).range(26f).spread(7.0f).pellets(9).kb(1.2f).recoil(0.25f).ammo(AmmoTier.HEAVY)
                .trail(ParticleTypes.CRIT).muzzle(ParticleTypes.FLAME)
                .sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2f, 1.6f);
    }

    /** Tempo Minigun — spam-click for a wall of lead, suppresses targets. */
    public static GunConfig minigun() {
        return new GunConfig()
                .dmg(7f).cd(1).range(68f).spread(3.6f).kb(0.15f).auto()
                .trail(ParticleTypes.ELECTRIC_SPARK).muzzle(ParticleTypes.FLAME)
                .sound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.9f, 2.0f)
                .effect(StatusEffects.SLOWNESS, 20, 1);
    }

    /** Sonic Railgun — infinite pierce, monster damage, warden boom. */
    public static GunConfig railgun() {
        return new GunConfig()
                .dmg(65f).cd(52).range(280f).spread(0.0f).pierce(9999).kb(2.0f).recoil(0.5f).ammo(AmmoTier.HEAVY)
                .trail(ParticleTypes.ELECTRIC_SPARK).muzzle(ParticleTypes.SONIC_BOOM)
                .sound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f)
                .effect(StatusEffects.WEAKNESS, 100, 1);
    }
}
