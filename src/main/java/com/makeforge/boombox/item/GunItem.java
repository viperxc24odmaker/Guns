package com.makeforge.boombox.item;

import com.makeforge.boombox.BoomBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GunItem extends Item {
    public final GunConfig config;
    private static final int MAX_USE = 72000;

    public GunItem(Item.Settings settings, GunConfig config) {
        super(settings);
        this.config = config;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // full-auto: begin "using" so usageTick keeps firing while held
        if (config.auto) {
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }

        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }
        if (!consumeAmmo(user)) {
            playDry(world, user);
            user.getItemCooldownManager().set(stack, 6);
            return ActionResult.FAIL;
        }
        fire(serverWorld, user);
        user.getItemCooldownManager().set(stack, config.cooldown);
        return ActionResult.SUCCESS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!config.auto) return;
        if (!(user instanceof PlayerEntity player)) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        int elapsed = MAX_USE - remainingUseTicks;
        if (elapsed % Math.max(1, config.cooldown) != 0) return; // gate the fire rate

        if (!consumeAmmo(player)) {
            playDry(world, player);
            player.stopUsingItem();
            return;
        }
        fire(serverWorld, player);
    }

    private void playDry(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.7f, 1.4f);
    }

    // ---------------------------------------------------------------------
    private void fire(ServerWorld world, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Random random = new Random();

        Vec3d tip = start.add(look.multiply(1.0));
        world.spawnParticles(config.muzzle, tip.x, tip.y, tip.z, 8, 0.06, 0.06, 0.06, 0.03);
        world.spawnParticles(ParticleTypes.SMOKE, tip.x, tip.y, tip.z, 4, 0.05, 0.05, 0.05, 0.01);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                config.sound, SoundCategory.PLAYERS, config.volume, config.pitch);

        int shots = Math.max(1, config.pellets);
        for (int i = 0; i < shots; i++) {
            Vec3d dir = applySpread(look, config.spread, random);
            fireRay(world, player, start, dir);
        }

        if (config.recoil > 0f) {
            Vec3d back = look.multiply(-config.recoil);
            player.addVelocity(back.x, config.recoil * 0.25, back.z);
        }
    }

    private void fireRay(ServerWorld world, PlayerEntity player, Vec3d start, Vec3d dir) {
        Vec3d end = start.add(dir.multiply(config.range));

        BlockHitResult blockHit = world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        Vec3d wallPos = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getPos();

        if (config.explosionPower > 0f) {
            EntityHitResult eh = raycastEntity(world, player, start, wallPos);
            Vec3d impact = (eh != null) ? eh.getPos() : wallPos;
            drawTrail(world, start, impact);
            world.createExplosion(player, impact.x, impact.y, impact.z,
                    config.explosionPower, config.fire, World.ExplosionSourceType.TNT);
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                    impact.x, impact.y, impact.z, 1, 0, 0, 0, 0);
            return;
        }

        drawTrail(world, start, wallPos);

        List<Entity> hits = gatherEntities(world, player, start, wallPos);
        int pierced = 0;
        for (Entity e : hits) {
            if (pierced >= config.pierce) break;
            applyHit(world, player, e);
            pierced++;
        }

        if (blockHit.getType() != HitResult.Type.MISS) {
            world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    wallPos.x, wallPos.y, wallPos.z, 6, 0.1, 0.1, 0.1, 0.02);
            world.spawnParticles(ParticleTypes.CRIT,
                    wallPos.x, wallPos.y, wallPos.z, 4, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void applyHit(ServerWorld world, PlayerEntity player, Entity target) {
        if (!(target instanceof LivingEntity living)) return;

        living.damage(world, world.getDamageSources().playerAttack(player), config.damage);

        if (config.knockback > 0f) {
            living.takeKnockback(config.knockback,
                    player.getX() - living.getX(), player.getZ() - living.getZ());
        }
        if (config.fireSeconds > 0) {
            living.setOnFireFor(config.fireSeconds);
        }
        if (config.effect != null && config.effectDuration > 0) {
            living.addStatusEffect(new StatusEffectInstance(
                    config.effect, config.effectDuration, config.effectAmplifier));
        }

        double hx = living.getX();
        double hy = living.getBodyY(0.6);
        double hz = living.getZ();
        world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, hx, hy, hz, 6, 0.2, 0.3, 0.2, 0.1);
        world.spawnParticles(ParticleTypes.CRIT, hx, hy, hz, 8, 0.2, 0.3, 0.2, 0.2);
    }

    private Vec3d applySpread(Vec3d dir, float spreadDeg, Random random) {
        if (spreadDeg <= 0f) return dir.normalize();
        double s = Math.sin(Math.toRadians(spreadDeg));
        return dir.add(
                (random.nextDouble() * 2 - 1) * s,
                (random.nextDouble() * 2 - 1) * s,
                (random.nextDouble() * 2 - 1) * s
        ).normalize();
    }

    private List<Entity> gatherEntities(ServerWorld world, PlayerEntity player, Vec3d start, Vec3d end) {
        Box area = new Box(start, end).expand(0.3);
        List<Entity> candidates = world.getOtherEntities(player, area,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());

        List<Entity> onBeam = new ArrayList<>();
        for (Entity e : candidates) {
            Optional<Vec3d> opt = e.getBoundingBox().expand(0.25).raycast(start, end);
            if (opt.isPresent()) onBeam.add(e);
        }
        onBeam.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(start)));
        return onBeam;
    }

    private EntityHitResult raycastEntity(ServerWorld world, PlayerEntity player, Vec3d start, Vec3d end) {
        Box box = player.getBoundingBox().stretch(end.subtract(start)).expand(1.0);
        return ProjectileUtil.raycast(player, start, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator(),
                start.squaredDistanceTo(end));
    }

    private void drawTrail(ServerWorld world, Vec3d start, Vec3d end) {
        Vec3d diff = end.subtract(start);
        double len = diff.length();
        if (len < 0.1) return;
        Vec3d step = diff.normalize().multiply(0.5);
        int steps = (int) (len * 2);
        Vec3d pos = start;
        for (int i = 0; i < steps; i++) {
            pos = pos.add(step);
            world.spawnParticles(config.trail, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private boolean consumeAmmo(PlayerEntity player) {
        if (player.getAbilities().creativeMode) return true;
        Item ammo = BoomBox.ammoItem(config.ammo);
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(ammo)) {
                s.decrement(1);
                return true;
            }
        }
        return false;
    }
}
