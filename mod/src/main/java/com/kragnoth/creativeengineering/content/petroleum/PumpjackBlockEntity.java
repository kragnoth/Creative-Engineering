package com.kragnoth.creativeengineering.content.petroleum;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Crank Gearbox logic. Every tick it turns it strokes the rig's well (StrokeDriven) with power min(1, RPM / 64) x stroke
 * (PumpjackRig: throw / rear arm x reach / 4, 1 to 6). Stress follows the stroke: 8 SU per RPM per unit of stroke, 2 with no
 * well on the beam. The rig is looked up again every second (parts are separate blocks and may change).
 */
public class PumpjackBlockEntity extends KineticBlockEntity {
    public static final int FULL_RPM = 64, RESCAN = 20;
    public static final float SU_PER_RPM = 8, IDLE_SU_PER_RPM = 2;
    /** Shaft turns per crank turn (the gearbox's reduction): 64 RPM is 16 strokes a minute. */
    public static final float REDUCTION = 4F;

    @Nullable
    private PumpjackRig rig;
    private int rescan;
    private float appliedStroke = -1;

    public PumpjackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
    }

    public float power() {
        return Math.min(1F, Math.abs(getSpeed()) / FULL_RPM);
    }

    @Nullable
    public PumpjackRig rig() {
        if (level != null && (rig == null || level.isClientSide)) {
            rig = PumpjackRig.fromGearbox(level, worldPosition, facing());
        }
        return rig;
    }

    private float strokeLoad() {
        PumpjackRig r = rig;
        return r != null && r.runs() ? r.stroke() : 0;
    }

    @Override
    public float calculateStressApplied() {
        float load = strokeLoad();
        float impact = load > 0 ? SU_PER_RPM * load : IDLE_SU_PER_RPM;
        lastStressApplied = impact;
        return impact;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (--rescan <= 0) {
            rescan = RESCAN;
            rig = PumpjackRig.fromGearbox(level, worldPosition, facing());
            float load = strokeLoad();
            if (load != appliedStroke) {
                appliedStroke = load;
                if (hasNetwork()) {
                    getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
                }
            }
        }
        if (getSpeed() != 0 && rig != null && rig.runs() && level.getBlockEntity(rig.well()) instanceof StrokeDriven d) {
            d.stroke(power() * rig.stroke());
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        super.addToGoggleTooltip(tooltip, sneaking);
        rigTooltip(tooltip, level, PumpjackRig.fromGearbox(level, worldPosition, facing()));
        if (Math.abs(getSpeed()) < FULL_RPM) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.slow", FULL_RPM), ChatFormatting.DARK_GRAY));
        }
        return true;
    }

    /** Shared goggle lines (gearbox and post): which parts are found, and the stroke. */
    static boolean rigTooltip(List<Component> tooltip, Level level, @Nullable PumpjackRig r) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.pumpjack")).withStyle(ChatFormatting.WHITE));
        if (r == null) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.no_post"), ChatFormatting.RED));
            return true;
        }
        if (r.h() < PumpjackRig.MIN_H) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.no_column"), ChatFormatting.RED));
        }
        if (r.gearbox() == null) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.no_gearbox"), ChatFormatting.RED));
        }
        if (r.well() == null) {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.no_target"), ChatFormatting.RED));
        } else {
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.driving", level.getBlockState(r.well()).getBlock().getName()), ChatFormatting.GOLD));
            tooltip.add(line(Component.translatable("creativeengineering.pumpjack.stroke", r.h(), r.a(), r.b(), String.format("%.1f", r.stroke())), ChatFormatting.AQUA));
            if (r.tooShort()) {
                tooltip.add(line(Component.translatable("creativeengineering.pumpjack.too_short"), ChatFormatting.RED));
            }
        }
        return true;
    }

    private static Component line(Component c, ChatFormatting style) {
        return Component.literal("     ").append(c).withStyle(style);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    /** Crank angle in radians for this frame (client). Slower than the shaft by REDUCTION; frozen when stopped. */
    public float crankAngle(float partialTick) {
        float time = AnimationTickHolder.getRenderTime(level);
        return (time * getSpeed() * 3F / 10F / REDUCTION) % 360F * Mth.DEG_TO_RAD;
    }
}
