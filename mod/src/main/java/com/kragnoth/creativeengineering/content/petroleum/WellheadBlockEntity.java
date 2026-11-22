package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.content.shared.PipeRuns;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * The Wellhead's work: each tick a Pumpjack strokes it (StrokeDriven), it brings up its WellSources yield x power into
 * an 8 B tank, and every PUSH_TICKS it pushes that down any pipe run leaving its four sides (PipeRuns: line pressure,
 * no pump needed). Pumps may also drain it (drain-only capability). Goggles tell what's below and whether it's driven.
 */
public class WellheadBlockEntity extends SmartBlockEntity implements StrokeDriven, IHaveGoggleInformation {
    public static final int CAPACITY = 8000, PUSH_TICKS = 10, PUSH_RATE = 250, RESAMPLE = 100;

    private final FluidTank tank = new FluidTank(CAPACITY);
    private final IFluidHandler output = new DrainOnly();
    private final List<PipeRuns.Endpoint> targets = new ArrayList<>();
    private Fluid yieldFluid = Fluids.EMPTY;
    private int yieldRate = -1, pushTimer, rescan, lastSynced = -1, sample;
    private float carry, power, lastPower = -1;
    private boolean strokedThisTick;

    public WellheadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public FluidTank tank() {
        return tank;
    }

    public IFluidHandler output() {
        return output;
    }

    @Nullable
    public Fluid yieldFluid() {
        return yieldRate > 0 ? yieldFluid : null;
    }

    public int yieldRate() {
        return Math.max(0, yieldRate);
    }

    /** mB/s at the current stroke power. */
    public float pumpRate() {
        return yieldRate() * power;
    }

    @Override
    public void stroke(float p) {
        power = Math.max(power, p);
        strokedThisTick = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (yieldRate < 0 || --sample <= 0) {
            sample = RESAMPLE;
            WellSources.Yield y = WellSources.at(level, worldPosition);
            Fluid f = y == null ? Fluids.EMPTY : y.fluid();
            int r = y == null ? 0 : y.rate();
            if (f != yieldFluid || r != yieldRate) {
                yieldFluid = f;
                yieldRate = r;
                sendData();
            }
        }
        if (!strokedThisTick) {
            power = 0;
        }
        strokedThisTick = false;
        float rate = pumpRate();
        if (rate > 0 && yieldFluid != Fluids.EMPTY && (tank.isEmpty() || tank.getFluid().is(yieldFluid))) {
            carry += rate / 20F;
            int whole = (int) carry;
            if (whole > 0) {
                tank.fill(new FluidStack(yieldFluid, whole), IFluidHandler.FluidAction.EXECUTE);
                carry -= whole;
            }
            if (tank.getSpace() == 0) {
                carry = 0;   // full and nowhere to go: the well waits
            }
        }
        if (++pushTimer >= PUSH_TICKS) {
            pushTimer = 0;
            push();
        }
        if (level.getGameTime() % 20 == 0 && (tank.getFluidAmount() != lastSynced || power != lastPower)) {
            lastSynced = tank.getFluidAmount();
            lastPower = power;
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
            for (Direction side : Direction.Plane.HORIZONTAL) {
                targets.addAll(PipeRuns.endpoints(level, worldPosition, side, be -> be == this));
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
        tag.putFloat("Carry", carry);
        if (clientPacket) {
            tag.putString("Yield", BuiltInRegistries.FLUID.getKey(yieldFluid).toString());
            tag.putInt("Rate", yieldRate);
            tag.putFloat("Power", power);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        carry = tag.getFloat("Carry");
        if (clientPacket) {
            yieldFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("Yield")));
            yieldRate = tag.getInt("Rate");
            power = tag.getFloat("Power");
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.wellhead")).withStyle(ChatFormatting.WHITE));
        if (yieldRate() <= 0) {
            tooltip.add(line(Component.translatable("creativeengineering.wellhead.dry"), ChatFormatting.RED));
        } else {
            tooltip.add(line(Component.translatable("creativeengineering.wellhead.yield", yieldFluid.getFluidType().getDescription(), yieldRate()), ChatFormatting.GOLD));
            if (power <= 0) {
                tooltip.add(line(Component.translatable("creativeengineering.wellhead.idle"), ChatFormatting.GRAY));
            } else {
                tooltip.add(line(Component.translatable("creativeengineering.wellhead.rate", String.format("%.1f", pumpRate())), ChatFormatting.AQUA));
                if (power < 1) {
                    tooltip.add(line(Component.translatable("creativeengineering.wellhead.slow", PumpjackBlockEntity.FULL_RPM), ChatFormatting.DARK_GRAY));
                }
            }
        }
        FluidStack in = tank.getFluid();
        tooltip.add(line(in.isEmpty() ? Component.translatable("creativeengineering.wellhead.empty", CAPACITY)
                : Component.translatable("creativeengineering.wellhead.tank", in.getHoverName(), in.getAmount(), CAPACITY), ChatFormatting.GRAY));
        return true;
    }

    private static Component line(Component c, ChatFormatting style) {
        return Component.literal("     ").append(c).withStyle(style);
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
