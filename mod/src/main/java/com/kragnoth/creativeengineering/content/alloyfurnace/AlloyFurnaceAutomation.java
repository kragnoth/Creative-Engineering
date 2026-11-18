package com.kragnoth.creativeengineering.content.alloyfurnace;

import static com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity.LEGACY;
import static com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity.INPUTS;
import static com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity.OUT_START;
import static com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity.SLOTS;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * The formed furnace as 19 slots, the same on every face and the side-less query (spec section 0, tarbound T1-T3):
 * 0-8 ingredients (insert only; one kind takes at most 3 slots so one metal can't crowd out the rest), 9 unused
 * (pre-0.6.6 fuel; extract only, so leftovers can be pulled out), 10-18 output (extract only). Always the controller's storage.
 */
final class AlloyFurnaceAutomation implements IItemHandler {
    static final int MAX_SLOTS_PER_KIND = 3;
    private final AlloyFurnaceBlockEntity be;

    AlloyFurnaceAutomation(AlloyFurnaceBlockEntity be) {
        this.be = be;
    }

    private ItemStackHandler inv() {
        AlloyFurnaceBlockEntity c = be.getControllerBE();
        return c == null ? null : c.items();
    }

    @Override public int getSlots() { return SLOTS; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStackHandler inv = inv();
        return inv == null ? ItemStack.EMPTY : inv.getStackInSlot(slot);
    }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        ItemStackHandler inv = inv();
        if (inv == null || stack.isEmpty() || be.getLevel() == null) {
            return false;
        }
        if (slot >= INPUTS) {
            return false;
        }
        if (AlloyRecipes.all(be.getLevel()).stream().noneMatch(j -> j.uses(stack))) {
            return false;
        }
        ItemStack here = inv.getStackInSlot(slot);
        if (!here.isEmpty()) {
            return ItemStack.isSameItemSameComponents(here, stack);
        }
        int same = 0; // up to 3 slots per kind: room for a Create package of several stacks, but never crowding out
        for (int i = 0; i < INPUTS; i++) {
            if (ItemStack.isSameItemSameComponents(inv.getStackInSlot(i), stack)) {
                same++;
            }
        }
        return same < MAX_SLOTS_PER_KIND;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStackHandler inv = inv();
        if (inv == null || slot >= INPUTS || !isItemValid(slot, stack)) {
            return stack;
        }
        return inv.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStackHandler inv = inv();
        if (inv == null || (slot < OUT_START && slot != LEGACY)) {
            return ItemStack.EMPTY;
        }
        return inv.extractItem(slot, amount, simulate);
    }
}
