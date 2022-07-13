package drasouls.nullstorage.items;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.inventory.container.Container;
import necesse.inventory.container.ContainerTransferResult;
import necesse.inventory.container.SlotIndexRange;
import necesse.inventory.container.item.ItemInventoryContainer;
import necesse.inventory.container.slots.ContainerSlot;

// Makes transfer all work exactly like quick stack, because accidents happen
// don't want to drain your fat stacks of coins accidentally, would you
public class NoFillItemInventoryContainer extends ItemInventoryContainer implements NoFillInventoryContainer {
    public NoFillItemInventoryContainer(NetworkClient client, int uniqueSeed, Packet content) {
        super(client, uniqueSeed, content);
    }

    @Override
    public ContainerTransferResult transferToSlots(ContainerSlot slot, Iterable<SlotIndexRange> ranges, int amount, String purpose) {
        return this.doTransferToSlots(slot, ranges, amount, purpose);
    }

    @Override
    public Container getContainer() {
        return this;
    }
}
