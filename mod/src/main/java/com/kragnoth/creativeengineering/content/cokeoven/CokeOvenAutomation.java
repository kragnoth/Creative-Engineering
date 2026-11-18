package com.kragnoth.creativeengineering.content.cokeoven;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * The whole oven as two slots (docs/tarbound-compat.md T1-T3), the same on every face and on the side-less query:
 * <ul>
 *   <li>slot 0, input: insert only. Takes only items with a coking recipe, one kind of input per oven at a time,
 *       spread evenly across the bricks. Reads as the total of that input.</li>
 *   <li>slot 1, output: extract only. Reads as the total of the first output kind found; extracting pulls it from
 *       whichever bricks hold it.</li>
 * </ul>
 * Inputs can never be pulled back out, so funnels and autocrafters can't drain the coal you just fed in.
 */
final class CokeOvenAutomation implements IItemHandler {
    private final CokeOvenBlockEntity be;

    CokeOvenAutomation(CokeOvenBlockEntity be) {
        this.be = be;
    }

    private List<CokeOvenBlockEntity> parts() {
        return be.parts();
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int idx = slot == 0 ? 0 : 1;
        ItemStack kind = ItemStack.EMPTY;
        int total = 0;
        for (CokeOvenBlockEntity p : parts()) {
            ItemStack s = p.ownItems().getStackInSlot(idx);
            if (s.isEmpty()) {
                continue;
            }
            if (kind.isEmpty()) {
                kind = s;
            }
            if (ItemStack.isSameItemSameComponents(kind, s)) {
                total += s.getCount();
            }
        }
        return kind.isEmpty() ? ItemStack.EMPTY : kind.copyWithCount(total);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * parts().size();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot != 0 || stack.isEmpty() || be.recipeFor(stack).isEmpty()) {
            return false;
        }
        ItemStack current = getStackInSlot(0);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        // Fill the emptiest bricks first so every brick's coking slots stay fed.
        List<CokeOvenBlockEntity> order = parts().stream()
                .sorted(Comparator.comparingInt(p -> p.ownItems().getStackInSlot(0).getCount()))
                .toList();
        for (CokeOvenBlockEntity p : order) {
            remaining = p.ownItems().insertItem(0, remaining, simulate);
            if (remaining.isEmpty()) {
                break;
            }
        }
        return remaining;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 1 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack kind = getStackInSlot(1);
        if (kind.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int want = Math.min(amount, kind.getMaxStackSize());
        int got = 0;
        for (CokeOvenBlockEntity p : parts()) {
            ItemStack s = p.ownItems().getStackInSlot(1);
            if (!ItemStack.isSameItemSameComponents(kind, s)) {
                continue;
            }
            got += p.ownItems().extractItem(1, want - got, simulate).getCount();
            if (got >= want) {
                break;
            }
        }
        return got == 0 ? ItemStack.EMPTY : kind.copyWithCount(got);
    }

    /** Whether some brick could still take {@code result} into its output slot. */
    boolean canAcceptOutput(ItemStack result) {
        for (CokeOvenBlockEntity p : parts()) {
            if (p.ownItems().insertItem(1, result.copy(), true).getCount() < result.getCount()) {
                return true;
            }
        }
        return false;
    }
}
