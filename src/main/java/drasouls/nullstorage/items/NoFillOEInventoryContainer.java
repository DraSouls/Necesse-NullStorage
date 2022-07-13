package drasouls.nullstorage.items;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.entity.objectEntity.interfaces.OEInventory;
import necesse.inventory.container.Container;
import necesse.inventory.container.ContainerTransferResult;
import necesse.inventory.container.SlotIndexRange;
import necesse.inventory.container.object.OEInventoryContainer;
import necesse.inventory.container.slots.ContainerSlot;

// Same like NoFillItemInventoryContainer
public class NoFillOEInventoryContainer extends OEInventoryContainer implements NoFillInventoryContainer {
    public NoFillOEInventoryContainer(NetworkClient client, int uniqueSeed, OEInventory oeInventory, PacketReader reader) {
        super(client, uniqueSeed, oeInventory, reader);
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
