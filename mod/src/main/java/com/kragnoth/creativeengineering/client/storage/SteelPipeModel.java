package com.kragnoth.creativeengineering.client.storage;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Steel Pipe's model: Create's PipeAttachmentModel logic (arms, rims and drains per side, as the pipe's transport
 * behaviour reports them) drawn with steel children of Create's attachment partials instead of the copper ones.
 * No casing or brackets (the steel pipe has neither).
 */
public class SteelPipeModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<AttachmentTypes[]> ATTACHMENTS = new ModelProperty<>();
    public static final Map<ComponentPartials, Map<Direction, PartialModel>> PARTS = new EnumMap<>(ComponentPartials.class);

    static {
        for (ComponentPartials kind : ComponentPartials.values()) {
            Map<Direction, PartialModel> m = new EnumMap<>(Direction.class);
            for (Direction d : Direction.values()) {
                m.put(d, PartialModel.of(CreativeEngineering.id("block/steel_pipe/" + kind.name().toLowerCase(Locale.ROOT) + "/" + d.getSerializedName())));
            }
            PARTS.put(kind, m);
        }
    }

    public SteelPipeModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(level, pos, FluidTransportBehaviour.TYPE);
        AttachmentTypes[] a = new AttachmentTypes[6];
        for (Direction d : Direction.values()) {
            a[d.get3DDataValue()] = transport == null ? AttachmentTypes.NONE : transport.getRenderedRimAttachment(level, pos, state, d);
        }
        return super.getModelData(level, pos, state, data).derive().with(ATTACHMENTS, a).build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
            @Nullable RenderType type) {
        List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, data, type));
        AttachmentTypes[] a = data.get(ATTACHMENTS);
        if (a != null) {
            for (Direction d : Direction.values()) {
                for (ComponentPartials p : a[d.get3DDataValue()].partials) {
                    quads.addAll(PARTS.get(p).get(d).get().getQuads(state, side, rand, data, type));
                }
            }
        }
        return quads;
    }
}
