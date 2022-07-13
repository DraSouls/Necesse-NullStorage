package drasouls.nullstorage.ui;

import necesse.engine.network.client.Client;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.presets.containerComponent.object.OEInventoryContainerForm;
import necesse.inventory.container.object.OEInventoryContainer;

public class NullboxContainerForm extends OEInventoryContainerForm<OEInventoryContainer> {
    public NullboxContainerForm(Client client, OEInventoryContainer container) {
        super(client, container);
    }

    @Override
    protected void addSlots() {
        this.slots = new FormContainerSlot[this.container.INVENTORY_END - this.container.INVENTORY_START + 1];

        for (int i = 0; i < this.slots.length; i++) {
            int slotIndex = i + this.container.INVENTORY_START;
            int x = i % 10;
            int y = i / 10;
            this.slots[i] = this.inventoryForm.addComponent(new NullingContainerSlot(this.client, slotIndex, 4 + x * 40, 4 + y * 40 + 30));
        }
    }
}
