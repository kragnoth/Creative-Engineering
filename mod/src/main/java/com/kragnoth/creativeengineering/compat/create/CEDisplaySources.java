package com.kragnoth.creativeengineering.compat.create;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.shared.MachineReadout;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Create Display Link sources for the coke oven, blast furnace and alloy furnace (status line, progress as percent or
 * bar, finished output count, coke oven creosote fill) and the diesel engine (status, stress capacity, fuel level).
 * Point a Display Link at any brick / crankcase.
 */
public final class CEDisplaySources {
    public static final DeferredRegister<DisplaySource> SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, CreativeEngineering.MODID);

    public static final DeferredHolder<DisplaySource, DisplaySource> STATUS = SOURCES.register("machine_status", () -> new SingleLineDisplaySource() {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            MachineReadout r = readout(context);
            return r == null ? EMPTY_LINE : r.readoutStatus().copy();
        }

        @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
        @Override protected String getTranslationKey() { return "machine_status"; }
    });

    public static final DeferredHolder<DisplaySource, DisplaySource> PROGRESS = SOURCES.register("machine_progress",
            () -> new Fill("machine_progress", MachineReadout::readoutProgress));

    public static final DeferredHolder<DisplaySource, DisplaySource> CREOSOTE = SOURCES.register("creosote_level",
            () -> new Fill("creosote_level", MachineReadout::readoutTank));

    public static final DeferredHolder<DisplaySource, DisplaySource> OUTPUT = SOURCES.register("machine_output", () -> new NumericSingleLineDisplaySource() {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            MachineReadout r = readout(context);
            return r == null ? EMPTY_LINE : Component.literal(Integer.toString(r.readoutOutput()));
        }

        @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
        @Override protected String getTranslationKey() { return "machine_output"; }
    });

    public static final DeferredHolder<DisplaySource, DisplaySource> ENGINE_FUEL = SOURCES.register("engine_fuel_level",
            () -> new Fill("engine_fuel_level", MachineReadout::readoutTank));

    public static final DeferredHolder<DisplaySource, DisplaySource> ENGINE_CAPACITY = SOURCES.register("engine_capacity", () -> new NumericSingleLineDisplaySource() {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            MachineReadout r = readout(context);
            return r == null ? EMPTY_LINE : Component.literal(String.format("%,d SU", r.readoutOutput()));
        }

        @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
        @Override protected String getTranslationKey() { return "engine_capacity"; }
    });

    /** Percent or progress bar, the same widget Create's own fill-level source uses. */
    private static final class Fill extends PercentOrProgressBarDisplaySource {
        private final String key;
        private final Function<MachineReadout, Float> value;

        Fill(String key, Function<MachineReadout, Float> value) {
            this.key = key;
            this.value = value;
        }

        @Override
        @Nullable
        protected Float getProgress(DisplayLinkContext context) {
            MachineReadout r = readout(context);
            if (r == null) {
                return null;
            }
            Float v = value.apply(r);
            return v == null ? 0F : v;
        }

        @Override
        protected boolean progressBarActive(DisplayLinkContext context) {
            return context.sourceConfig().getInt("Mode") != 0;
        }

        @Override protected String getTranslationKey() { return key; }
        @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            super.initConfigurationWidgets(context, builder, isFirstLine);
            if (isFirstLine) {
                return;
            }
            builder.addSelectionScrollInput(0, 120, (si, l) -> si.forOptions(CreateLang.translatedOptions(
                    "display_source.fill_level", "percent", "progress_bar"))
                    .titled(CreateLang.translateDirect("display_source.fill_level.display")), "Mode");
        }
    }

    @Nullable
    private static MachineReadout readout(DisplayLinkContext context) {
        return context.getSourceBlockEntity() instanceof MachineReadout r ? r : null;
    }

    private CEDisplaySources() {}

    public static void register(IEventBus modBus) {
        SOURCES.register(modBus);
        modBus.addListener(FMLCommonSetupEvent.class, e -> e.enqueueWork(() -> {
            for (var block : new net.minecraft.world.level.block.Block[]{ModBlocks.COKE_OVEN_BRICKS.get(),
                    ModBlocks.BLAST_FURNACE_BRICKS.get(), ModBlocks.ALLOY_FURNACE_BRICKS.get()}) {
                DisplaySource.BY_BLOCK.add(block, STATUS.get());
                DisplaySource.BY_BLOCK.add(block, PROGRESS.get());
                DisplaySource.BY_BLOCK.add(block, OUTPUT.get());
            }
            DisplaySource.BY_BLOCK.add(ModBlocks.COKE_OVEN_BRICKS.get(), CREOSOTE.get());
            DisplaySource.BY_BLOCK.add(ModBlocks.DIESEL_CRANKCASE.get(), STATUS.get());
            DisplaySource.BY_BLOCK.add(ModBlocks.FERMENTING_VAT.get(), STATUS.get());
            DisplaySource.BY_BLOCK.add(ModBlocks.FERMENTING_VAT.get(), PROGRESS.get());
            DisplaySource.BY_BLOCK.add(ModBlocks.DIESEL_CRANKCASE.get(), ENGINE_CAPACITY.get());
            DisplaySource.BY_BLOCK.add(ModBlocks.DIESEL_CRANKCASE.get(), ENGINE_FUEL.get());
        }));
    }
}
