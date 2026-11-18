package com.kragnoth.creativeengineering.content.metalworking;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

/** The die fitted in a Mechanical Press. Attached to every press through Create's BlockEntityBehaviourEvent. */
public class DieBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<DieBehaviour> TYPE = new BehaviourType<>("creativeengineering_die");

    private ItemStack die = ItemStack.EMPTY;

    public DieBehaviour(SmartBlockEntity be) {
        super(be);
    }

    public ItemStack getDie() {
        return die;
    }

    /** Fits {@code stack} (one item) and returns what was fitted before. */
    public ItemStack swap(ItemStack stack) {
        ItemStack old = die;
        die = stack.copyWithCount(1);
        blockEntity.notifyUpdate();
        return old;
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        if (!die.isEmpty()) {
            nbt.put("CreativeEngineeringDie", die.save(registries));
        }
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        die = nbt.contains("CreativeEngineeringDie")
                ? ItemStack.parse(registries, nbt.getCompound("CreativeEngineeringDie")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    @Override
    public void destroy() {
        if (!die.isEmpty() && getWorld() != null && !getWorld().isClientSide) {
            BlockPos p = getPos();
            Containers.dropItemStack(getWorld(), p.getX(), p.getY(), p.getZ(), die);
            die = ItemStack.EMPTY;
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
