package drasouls.nullstorage.items;

import necesse.inventory.ItemCombineResult;
import necesse.inventory.container.Container;
import necesse.inventory.container.ContainerTransferResult;
import necesse.inventory.container.SlotIndexRange;
import necesse.inventory.container.slots.ContainerSlot;

public interface NoFillInventoryContainer {
    Container getContainer();

    default ContainerTransferResult doTransferToSlots(ContainerSlot slot, Iterable<SlotIndexRange> ranges, int amount, String purpose) {
        String error = null;

        for(SlotIndexRange range : ranges) {
            for(int i = range.fromIndex; i <= range.toIndex; ++i) {
                if (slot.isClear() || amount <= 0) {
                    return new ContainerTransferResult(0, error);
                }

                ContainerSlot toSlot = this.getContainer().getSlot(i);
                if (toSlot.isClear()) continue;

                int startAmount = slot.getItemAmount();
                ItemCombineResult combineResult = toSlot.combineSlots(
                        this.getContainer().client.playerMob.getLevel(),
                        this.getContainer().client.playerMob,
                        slot, amount, true, purpose);
                if (combineResult.success) {
                    int amountMoved = startAmount - slot.getItemAmount();
                    amount -= amountMoved;
                } else if (combineResult.error != null) {
                    error = combineResult.error;
                }
            }
        }

        return new ContainerTransferResult(amount, error);
    }
}
