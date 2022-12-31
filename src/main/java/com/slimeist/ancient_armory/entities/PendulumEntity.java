package com.slimeist.ancient_armory.entities;

import com.slimeist.server_mobs.api.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.api.server_rendering.model.BakedServerEntityModel;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class PendulumEntity extends Entity implements PolymerEntity, IServerRenderedEntity {

    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    public PendulumEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModelInstance().updateHologram();
    }

    @Override
    public BakedServerEntityModel.Instance createModelInstance() {
        return getBakedModel().createInstance(this);
    }

    @Override
    public BakedServerEntityModel.Instance getModelInstance() {
        if (modelInstance == null) {
            modelInstance = createModelInstance();
        }
        return modelInstance;
    }

    @Override
    public BakedServerEntityModel getBakedModel() {
        return bakedModelSupplier.get();
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {}

    @Override
    public void initAngles() {
        this.getModelInstance().setPartParentLocal("base.swinging.bottom", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain1", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain2", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain3", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain4", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain5", true);
        this.getModelInstance().setPartParentLocal("base.swinging.chains.chain6", true);

        boolean knockedDown = false;

        for (int i = 0; i < 24 ; i++) {
            String path = "base.knock_down.k"+(i+1);

            double angle = Math.PI * 2 * (i / 24.0);
            double radius = (knockedDown ? 3.75 : 3.0) * 16;

            this.getModelInstance().setPartPivot(path, new Vec3d(radius * Math.sin(angle), knockedDown ? -12 : 0, radius * Math.cos(angle)));
            this.getModelInstance().setPartRotation(path, new EulerAngle(knockedDown ? -90 : 0, (float) -Math.toDegrees(angle), 0));
        }
    }

    @Override
    public void updateAngles() {
        long time = (this.world.getTimeOfDay() * 8) % 24000;
//        this.getModelInstance().setPartPivot("base", Vec3d.ZERO.add(0, 0, 0));
        double swingProgress = (time % 400) / 400d;
        double pitch = Math.sin(swingProgress * Math.PI * 2) * 0.225;
//        pitch = 0;
//        this.getModelInstance().setPartPivot("base.swinging.chains", Vec3d.ZERO);
//        this.getModelInstance().setPartPivot("base.swinging.chains.chain1", Vec3d.ZERO.add(0, 0, 0));
//        this.getModelInstance().setPartPivot("base.swinging.chains.chain2", Vec3d.ZERO.add(0, 0, 0));
        this.getModelInstance().setPartRotation("base.swinging", new EulerAngle((float) Math.toDegrees(pitch), -(time / 24000f) * 360f, 0));

        boolean subtractHalfDay = (time + 350) % 24000 > 12000;
        for (int i = 0; i < 24 ; i++) {
            int relevantI = (subtractHalfDay ? i + 12 : i) % 24;
            boolean knockedDown = (relevantI % 12) * 1000 <= (subtractHalfDay ? time - 12000 : time) + 103 - (relevantI > 11 != (relevantI % 2 == 0) ? 200 : 0);

            String path = "base.knock_down.k"+(i+1);

            double angle = Math.PI * 2 * (i / 24.0);
            double radius = (knockedDown ? 3.75 : 3.0) * 16;

            this.getModelInstance().setPartPivot(path, new Vec3d(radius * Math.sin(angle), knockedDown ? -12 : 0, radius * Math.cos(angle)));
            this.getModelInstance().setPartRotation(path, new EulerAngle(knockedDown ? -90 : 0, (float) -Math.toDegrees(angle), 0));
        }
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }

    /**
     * This method is used to determine what this entity will look like on client
     * This should never return entity type used by other PolymerEntity!
     *
     * @return Vanilla/Modded entity type
     */
    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.MARKER;
    }

    @Override
    protected void initDataTracker() {}

    /**
     * Reads custom data from {@code nbt}. Subclasses has to implement this.
     *
     * <p>NBT is a storage format; therefore, a data from NBT is loaded to an entity instance's
     * fields, which are used for other operations instead of the NBT. The data is written
     * back to NBT when saving the entity.
     *
     * <p>{@code nbt} might not have all expected keys, or might have a key whose value
     * does not meet the requirement (such as the type or the range). This method should
     * fall back to a reasonable default value instead of throwing an exception.
     *
     * @param nbt
     * @see #writeCustomDataToNbt
     */
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    /**
     * Writes custom data to {@code nbt}. Subclasses has to implement this.
     *
     * <p>NBT is a storage format; therefore, a data from NBT is loaded to an entity instance's
     * fields, which are used for other operations instead of the NBT. The data is written
     * back to NBT when saving the entity.
     *
     * @param nbt
     * @see #readCustomDataFromNbt
     */
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    /**
     * {@return a packet to notify the clients of the entity's spawning}
     *
     * @apiNote Subclasses should return {@code new EntitySpawnS2CPacket(this)},
     * unless they use a custom spawning packet.
     */
    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
