package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.content.shared.PipeRuns;
import com.kragnoth.creativeengineering.registry.ModFluids;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Pumpjack logic (docs/petroleum-spec.md). While its crank turns it draws the oil vein under the Wellhead's chunk
 * (OilVeins) at vein rate x min(1, RPM / FULL_RPM) into an 8 B tank, and pushes that crude down any pipe run leaving the
 * Wellhead's sides or front (PipeRuns: no pump needed, like a real well's line pressure). Pumps and hoses may also
 * drain the Wellhead directly. No vein: it still nods, but nothing comes up (goggles say so).
 */
public class PumpjackBlockEntity extends KineticBlockEntity {
    public static final int CAPACITY = 8000, FULL_RPM = 64, PUSH_TICKS = 10, PUSH_RATE = 250;
    /** Shaft turns per crank turn (the gearbox's reduction): 64 RPM is 16 strokes a minute. */
    public static final float REDUCTION = 4F;

    private final FluidTank tank = new FluidTank(CAPACITY, fs -> fs.is(ModFluids.CRUDE_OIL.source.get()));
    private final IFluidHandler output = new DrainOnly();
    private final List<PipeRuns.Endpoint> targets = new ArrayList<>();
    private int veinRate = -1, pushTimer, rescan, lastSynced = -1;
    private float carry;

    public PumpjackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
    }

    public BlockPos wellhead() {
        return worldPosition.relative(facing(), 2);
    }

    public int veinRate() {
        return Math.max(0, veinRate);
    }

    /** mB per second at the current speed. */
    public float pumpRate() {
        return veinRate() * Math.min(1F, Math.abs(getSpeed()) / FULL_RPM);
    }

    public FluidTank tank() {
        return tank;
    }

    public IFluidHandler output() {
        return output;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (veinRate < 0 || level.getGameTime() % 100 == 0) {
            int r = OilVeins.rate(level, new ChunkPos(wellhead()));
            if (r != veinRate) {
                veinRate = r;
                sendData();
            }
        }
        float rate = pumpRate();
        if (rate > 0) {
            carry += rate / 20F;
            int whole = (int) carry;
            if (whole > 0) {
                tank.fill(new FluidStack(ModFluids.CRUDE_OIL.source.get(), whole), IFluidHandler.FluidAction.EXECUTE);
                carry -= whole;
            }
            if (tank.getSpace() == 0) {
                carry = 0;   // full and nowhere to go: the vein waits
            }
        }
        if (++pushTimer >= PUSH_TICKS) {
            pushTimer = 0;
            push();
        }
        if (level.getGameTime() % 20 == 0 && tank.getFluidAmount() != lastSynced) {
            lastSynced = tank.getFluidAmount();
            sendData();   // goggles
        }
    }

    private void push() {
        if (tank.isEmpty()) {
            return;
        }
        if (--rescan <= 0) {
            rescan = 4;
            targets.clear();
            Direction f = facing();
            for (Direction side : new Direction[] {f, f.getClockWise(), f.getCounterClockWise()}) {
                targets.addAll(PipeRuns.endpoints(level, wellhead(), side, be -> be == this));
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        int share = Math.max(1, Math.min(PUSH_RATE, tank.getFluidAmount()) / targets.size());
        for (PipeRuns.Endpoint e : targets) {
            if (e.handler() == output || tank.isEmpty()) {
                continue;
            }
            int took = e.handler().fill(tank.getFluid().copyWithAmount(Math.min(share, tank.getFluidAmount())), IFluidHandler.FluidAction.EXECUTE);
            if (took > 0) {
                tank.drain(took, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Vein", veinRate);
        tag.putFloat("Carry", carry);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        veinRate = tag.contains("Vein") ? tag.getInt("Vein") : -1;
        carry = tag.getFloat("Carry");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable("creativeengineering.pumpjack.header")).withStyle(ChatFormatting.WHITE));
        if (veinRate() <= 0) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.no_vein"), ChatFormatting.RED));
        } else {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.vein", veinRate()), ChatFormatting.GOLD));
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.rate", String.format("%.1f", pumpRate())), ChatFormatting.AQUA));
            if (Math.abs(getSpeed()) < FULL_RPM) {
                tooltip.add(line(Component.translatable("creativeengineering.pumpjack.slow", FULL_RPM), ChatFormatting.DARK_GRAY));
            }
        }
        tooltip.add(line(Component.translatable("creativeengineering.pumpjack.tank", tank.getFluidAmount(), CAPACITY), ChatFormatting.GRAY));
        return true;
    }

    private static Component line(Component c, ChatFormatting style) {
        return Component.literal("     ").append(c).withStyle(style);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(3, 3, 3);   // the beam overhangs the frame and wellhead
    }

    // ------------------------------------------------------------------------------------------------ animation
    /** Crank angle in radians for this frame (client). Slower than the shaft by REDUCTION; frozen when stopped. */
    public float crankAngle(float partialTick) {
        float time = AnimationTickHolder.getRenderTime(level);
        float deg = (time * getSpeed() * 3F / 10F / REDUCTION) % 360F;
        return deg * Mth.DEG_TO_RAD;
    }

    /**
     * Linkage pose, in pixels of the gearbox block's model space (machine built facing south: frame at z 16..32,
     * wellhead at z 32..48). Crank on X through (y 8, z 8), radius 4; beam pivot (y 28, z 24); the beam's rear pin is
     * (-1, -16.5) and the horsehead's lower lip (-6, 15.5) from the pivot.
     */
    public record Pose(float beamDeg, float pinY, float pinZ, float pitmanLen, float pitmanDeg, float rodTop) {}

    public static Pose pose(float crank) {
        float pinY = 8 + 4 * Mth.cos(crank), pinZ = 8 + 4 * Mth.sin(crank);
        float phi = (float) Math.asin((pinY - 8) / 16.5F);
        float c = Mth.cos(phi), s = Mth.sin(phi);
        float rearY = 28 + (-1 * c + 16.5F * s), rearZ = 24 + (-1 * s - 16.5F * c);
        float dy = rearY - pinY, dz = rearZ - pinZ;
        float lipY = 28 + (-6 * c - 15.5F * s);
        return new Pose(phi * Mth.RAD_TO_DEG, pinY, pinZ, Mth.sqrt(dy * dy + dz * dz), (float) Math.atan2(dz, dy) * Mth.RAD_TO_DEG, lipY);
    }

    /** Flywheel's (counter-clockwise) y rotation for the facing; the model faces south. */
    public static float yRot(Direction f) {
        return switch (f) {
            case EAST -> 90;
            case NORTH -> 180;
            case WEST -> -90;
            default -> 0;
        };
    }

    private class DrainOnly implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int t) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int t) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int t, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int max, FluidAction action) {
            return tank.drain(max, action);
        }
    }
}
