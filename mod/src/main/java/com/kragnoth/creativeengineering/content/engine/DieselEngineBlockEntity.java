package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * A diesel crankcase (docs/engines-spec.md). Every crankcase is a Create kinetic block on the crankshaft; the one at
 * the output end of a line is the engine's controller and the only one that generates:
 * <ul>
 *   <li>Structure (re-scanned every second): up to {@link #MAX_LENGTH} crankcases in a line with the same facing;
 *       cylinders diagonally above each, leaning away (2 per crankcase); fluid tanks touching a crankcase's sides.</li>
 *   <li>Start: when stopped with cylinders and fuel, any rotation in the block at the front (a Create Hand Crank, a
 *       shaft from anything) turns it over. The front is read-only: it never joins the engine's network.</li>
 *   <li>Running: {@link #RPM} RPM; capacity = cylinders x the fuel's stress per cylinder; burns the fuel's mB per
 *       cylinder per second straight from the touching tanks (no pump). Stops when the fuel runs out.</li>
 * </ul>
 */
public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity {
    public static final int MAX_LENGTH = 8;
    public static final float RPM = 64;
    private static final int SCAN_TICKS = 20;

    protected boolean running;
    protected int cylinders;
    @Nullable protected Fluid fuelFluid;
    protected float burnDebt;
    protected int scanTimer;
    protected int length = 1;
    private final List<BlockPos> cylinderPositions = new ArrayList<>();
    private final List<IFluidHandler> tanks = new ArrayList<>();
    @Nullable private BlockPos starterPos;

    public DieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Direction out() {
        return getBlockState().getValue(DieselCrankcaseBlock.HORIZONTAL_FACING);
    }

    private boolean isCrankcase(BlockPos p, Direction facing) {
        BlockState s = level.getBlockState(p);
        return s.is(ModBlocks.DIESEL_CRANKCASE.get()) && s.getValue(DieselCrankcaseBlock.HORIZONTAL_FACING) == facing;
    }

    /** The output-end crankcase of the line runs the engine; the rest only carry the crankshaft. */
    public boolean isController() {
        return level != null && !isCrankcase(worldPosition.relative(out()), out());
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || !isController()) {
            return;
        }
        if (--scanTimer <= 0) {
            scanTimer = SCAN_TICKS;
            int before = cylinders;
            Fluid fuelBefore = fuelFluid;
            scan();
            if (running && (before != cylinders || fuelBefore != fuelFluid)) {
                if (cylinders == 0) {
                    stop();
                } else {
                    updateGeneratedRotation();   // capacity changed
                }
            }
        }
        if (!running) {
            if (cylinders > 0 && fuelFluid != null && starterTurning()) {
                start();
            }
            return;
        }
        EngineFuel fuel = fuelFluid == null ? null : EngineFuel.of(new FluidStack(fuelFluid, 1));
        if (fuel == null) {
            stop();
            return;
        }
        burnDebt += cylinders * fuel.burn() / 20F;
        int mb = (int) burnDebt;
        if (mb > 0) {
            if (drain(mb) < mb) {
                stop();
                return;
            }
            burnDebt -= mb;
        }
    }

    private void scan() {
        Direction out = out(), back = out.getOpposite();
        cylinderPositions.clear();
        tanks.clear();
        cylinders = 0;
        length = 0;
        BlockPos p = worldPosition;
        while (length < MAX_LENGTH && isCrankcase(p, out)) {
            for (Direction side : new Direction[]{out.getClockWise(), out.getCounterClockWise()}) {
                BlockPos c = p.above().relative(side);
                BlockState cs = level.getBlockState(c);
                if (cs.is(ModBlocks.DIESEL_CYLINDER.get()) && cs.getValue(DieselCylinderBlock.FACING) == side) {
                    cylinders++;
                    cylinderPositions.add(c);
                }
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, p.relative(side), side.getOpposite());
                if (h != null && !tanks.contains(h)) {
                    tanks.add(h);
                }
            }
            length++;
            p = p.relative(back);
        }
        starterPos = p;   // the block in front of the front-most crankcase
        fuelFluid = findFuel();
    }

    @Nullable
    private Fluid findFuel() {
        if (fuelFluid != null && available(fuelFluid) > 0) {
            return fuelFluid;   // stick with what we're burning while it lasts
        }
        for (IFluidHandler h : tanks) {
            for (int t = 0; t < h.getTanks(); t++) {
                FluidStack s = h.getFluidInTank(t);
                if (EngineFuel.of(s) != null) {
                    return s.getFluid();
                }
            }
        }
        return null;
    }

    private int available(Fluid f) {
        int n = 0;
        for (IFluidHandler h : tanks) {
            n += h.drain(new FluidStack(f, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE).getAmount();
        }
        return n;
    }

    private int drain(int mb) {
        int got = 0;
        for (IFluidHandler h : tanks) {
            if (got >= mb) {
                break;
            }
            got += h.drain(new FluidStack(fuelFluid, mb - got), IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
        return got;
    }

    private boolean starterTurning() {
        return starterPos != null && level.getBlockEntity(starterPos) instanceof KineticBlockEntity k && k.getSpeed() != 0;
    }

    private void start() {
        running = true;
        burnDebt = 0;
        updateGeneratedRotation();
        setCylinders(true);
        level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.4F, 0.5F);
        sendData();
    }

    private void stop() {
        running = false;
        updateGeneratedRotation();
        setCylinders(false);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 0.6F);
        sendData();
    }

    private void setCylinders(boolean on) {
        for (BlockPos c : cylinderPositions) {
            BlockState s = level.getBlockState(c);
            if (s.is(ModBlocks.DIESEL_CYLINDER.get()) && s.getValue(DieselCylinderBlock.RUNNING) != on) {
                level.setBlock(c, s.setValue(DieselCylinderBlock.RUNNING, on), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** The output-end crankcase of this line (this one if it is the controller), for goggles / Jade from any part. */
    @Nullable
    public DieselEngineBlockEntity controller() {
        if (level == null) {
            return null;
        }
        Direction out = out();
        BlockPos p = worldPosition;
        for (int i = 0; i < MAX_LENGTH && isCrankcase(p.relative(out), out); i++) {
            p = p.relative(out);
        }
        return level.getBlockEntity(p) instanceof DieselEngineBlockEntity c ? c : null;
    }

    public boolean isRunning() { return running; }
    public int cylinderCount() { return cylinders; }
    public int lineLength() { return length; }
    @Nullable public Fluid fuelFluid() { return fuelFluid; }

    /** Total stress capacity while running (SU), 0 when stopped. */
    public float totalCapacity() {
        EngineFuel f = fuelFluid == null ? null : EngineFuel.of(new FluidStack(fuelFluid, 1));
        return running && f != null ? cylinders * f.stress() : 0;
    }

    /** Status key: running / needs_start / no_cylinders / no_fuel_state. */
    public String statusKey() {
        return running ? "running" : cylinders == 0 ? "no_cylinders" : fuelFluid == null ? "no_fuel_state" : "needs_start";
    }

    @Override
    public float getGeneratedSpeed() {
        if (!running || !isController()) {
            return 0;
        }
        return out().getAxisDirection() == Direction.AxisDirection.POSITIVE ? RPM : -RPM;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float cap = 0;
        if (running && isController() && fuelFluid != null) {
            EngineFuel fuel = EngineFuel.of(new FluidStack(fuelFluid, 1));
            cap = fuel == null ? 0 : cylinders * fuel.stress() / RPM;   // Create counts capacity per RPM
        }
        this.lastCapacityProvided = cap;
        return cap;
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide && running) {
            setCylinders(false);
        }
        super.remove();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        if (!isController()) {
            return super.addToGoggleTooltip(tooltip, sneaking);
        }
        tooltip.add(Component.literal("    ").append(Component.translatable("creativeengineering.engine.title")));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.engine.layout", length, cylinders)).withStyle(ChatFormatting.GRAY));
        Component fuel = fuelFluid == null ? Component.translatable("creativeengineering.engine.no_fuel").withStyle(ChatFormatting.RED)
                : new FluidStack(fuelFluid, 1).getHoverName().copy().withStyle(ChatFormatting.GOLD);
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.engine.fuel", fuel)).withStyle(ChatFormatting.GRAY));
        String state = statusKey();
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.engine.state." + state))
                .withStyle(running ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        super.addToGoggleTooltip(tooltip, sneaking);
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("Running", running);
        tag.putInt("Cylinders", cylinders);
        tag.putInt("Length", length);
        if (fuelFluid != null) {
            tag.putString("Fuel", BuiltInRegistries.FLUID.getKey(fuelFluid).toString());
        }
        tag.putFloat("BurnDebt", burnDebt);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        running = tag.getBoolean("Running");
        cylinders = tag.getInt("Cylinders");
        length = Math.max(1, tag.getInt("Length"));
        fuelFluid = tag.contains("Fuel") ? BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("Fuel"))) : null;
        if (fuelFluid == Fluids.EMPTY) {
            fuelFluid = null;
        }
        burnDebt = tag.getFloat("BurnDebt");
        super.read(tag, registries, clientPacket);
    }
}
