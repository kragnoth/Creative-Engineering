package com.kragnoth.creativeengineering.content.blastfurnace;

import static com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity.FUEL;
import static com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity.IN;
import static com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity.METAL;
import static com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity.SLAG;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * The whole furnace as four slots, the same on every face and the side-less query (spec section 0, tarbound T1-T3):
 * 0 iron in (insert only), 1 fuel (insert only), 2 metal out (extract only), 3 slag out (extract only).
 * Totals across bricks; inserts go to the emptiest brick first.
 */
final class BlastFurnaceAutomation implements IItemHandler {
    private final BlastFurnaceBlockEntity be;

    BlastFurnaceAutomation(BlastFurnaceBlockEntity be) {
        this.be = be;
    }

    private List<BlastFurnaceBlockEntity> parts() {
        return be.parts();
    }

    @Override public int getSlots() { return 4; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack kind = ItemStack.EMPTY;
        int total = 0;
        for (BlastFurnaceBlockEntity p : parts()) {
            ItemStack s = p.ownItems().getStackInSlot(slot);
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

    @Override public int getSlotLimit(int slot) { return 64 * parts().size(); }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean ok = slot == IN ? be.recipeFor(stack).isPresent() : slot == FUEL && BlastFuel.unitsOf(stack) > 0;
        if (!ok) {
            return false;
        }
        ItemStack current = getStackInSlot(slot);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if ((slot != IN && slot != FUEL) || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        for (BlastFurnaceBlockEntity p : parts().stream()
                .sorted(Comparator.comparingInt(p -> p.ownItems().getStackInSlot(slot).getCount())).toList()) {
            remaining = p.ownItems().insertItem(slot, remaining, simulate);
            if (remaining.isEmpty()) {
                break;
            }
        }
        return remaining;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if ((slot != METAL && slot != SLAG) || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack kind = getStackInSlot(slot);
        if (kind.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int want = Math.min(amount, kind.getMaxStackSize()), got = 0;
        for (BlastFurnaceBlockEntity p : parts()) {
            if (ItemStack.isSameItemSameComponents(kind, p.ownItems().getStackInSlot(slot))) {
                got += p.ownItems().extractItem(slot, want - got, simulate).getCount();
                if (got >= want) {
                    break;
                }
            }
        }
        return got == 0 ? ItemStack.EMPTY : kind.copyWithCount(got);
    }
}
