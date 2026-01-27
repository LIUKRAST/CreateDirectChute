package net.liukrast.chute.content;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class CombinedItemHandler implements IItemHandler {
    private final IItemHandler[] itemHandlers;

    public CombinedItemHandler(IItemHandler... handlers) {
        this.itemHandlers = handlers;
    }

    @Override
    public int getSlots() {
        int counter = 0;
        for(IItemHandler handler : itemHandlers) {
            counter+=handler.getSlots();
        }
        return counter;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        for(IItemHandler handler : itemHandlers) {
            if(slot >= handler.getSlots()) {
                slot-= handler.getSlots();
                continue;
            }
            return handler.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        for(IItemHandler handler : itemHandlers) {
            if(slot >= handler.getSlots()) {
                slot-= handler.getSlots();
                continue;
            }
            return handler.insertItem(slot, stack, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        for(IItemHandler handler : itemHandlers) {
            if(slot >= handler.getSlots()) {
                slot-= handler.getSlots();
                continue;
            }
            return handler.extractItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        for(IItemHandler handler : itemHandlers) {
            if(slot >= handler.getSlots()) {
                slot-= handler.getSlots();
                continue;
            }
            return handler.getSlotLimit(slot);
        }
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        for(IItemHandler handler : itemHandlers) {
            if(slot >= handler.getSlots()) {
                slot-= handler.getSlots();
                continue;
            }
            return handler.isItemValid(slot, stack);
        }
        return false;
    }
}
