package drasouls.nullstorage.ui;

import drasouls.nullstorage.NullStorage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;

public class NullingContainerSlot extends FormContainerSlot {
    public NullingContainerSlot(Client client, int containerSlotIndex, int x, int y) {
        super(client, containerSlotIndex, x, y);
        this.setDecal(NullStorage.voidSlotIcon);
    }
}
