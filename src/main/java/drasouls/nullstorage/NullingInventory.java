package drasouls.nullstorage;

import necesse.entity.mobs.PlayerMob;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.ItemCombineResult;
import necesse.inventory.SlotPriority;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;

import java.util.LinkedHashMap;
import java.util.Map;

public class NullingInventory extends Inventory {
    public NullingInventory(int size) {
        super(size);
    }

    // Slot, item
    public Map<Integer, InventoryItem> getInvItemsMatchingItem(InventoryItem item, String purpose) {
        Map<Integer, InventoryItem> matches = new LinkedHashMap<>();

        for(SlotPriority slotPriority : this.getPriorityList(null, null, 0, this.getSize() - 1, purpose)) {
            InventoryItem invItem = this.getItem(slotPriority.slot);
            if (invItem != null && invItem.item == item.item) {
                matches.put(slotPriority.slot, invItem);
            }
        }
        return matches;
    }

    @Override
    public int canAddItem(Level level, PlayerMob player, InventoryItem input, int startSlot, int endSlot, String purpose, boolean ignoreValid, boolean ignoreStackLimit) {
        if (player == null) {
            if (getFirstItem(null, null, new Item[]{ input.item }, purpose) != null) {
                return Integer.MAX_VALUE;
            } else {
                return 0;
            }
        }
        return super.canAddItem(level, player, input, startSlot, endSlot, purpose, ignoreValid, ignoreStackLimit);
    }

    @Override
    public ItemCombineResult combineItem(Level level, PlayerMob player, int staySlot, InventoryItem combineItem, int amount, String purpose) {
        if (!this.isSlotClear(staySlot) && combineItem != null) {
            ItemCombineResult out = this.getItem(staySlot).combine(level, player, combineItem, amount, purpose);
            if (combineItem.getAmount() > 0) {
                Map<Integer, InventoryItem> matches = getInvItemsMatchingItem(combineItem, purpose);
                int last = -1;
                for (int slot : matches.keySet()) last = slot;
                // if we're combining to the last occurrence of the item
                if (staySlot == last) combineItem.setAmount(0);
            }

            if (out.success) this.updateSlot(staySlot);
            return out;
        } else {
            return ItemCombineResult.failure();
        }
    }

    @Override
    public boolean addItem(Level level, PlayerMob player, InventoryItem input, int startSlot, int endSlot, String purpose, boolean ignoreValid, boolean ignoreStackLimit) {
        if (input != null) {
            Map<Integer, InventoryItem> matches = getInvItemsMatchingItem(input, purpose);
            int available = 0;
            for (InventoryItem invItem : matches.values()) {
                if (invItem.item == input.item) {
                    available += Math.max(0, invItem.itemStackSize() - invItem.getAmount());
                }
            }

            input.setAmount(Math.min(input.getAmount(), available));
        }

        return super.addItem(level, player, input, startSlot, endSlot, purpose, ignoreValid, ignoreStackLimit);
    }

    @Override
    public boolean addItem(Level level, PlayerMob player, InventoryItem input, int preferredSlot, boolean isLocked, String purpose, Inventory previousMoveToInventory) {
        if (player == null) preferredSlot = 0;
        if (input != null) {
            Map<Integer, InventoryItem> matches = getInvItemsMatchingItem(input, purpose);
            int available = 0;
            for (Map.Entry<Integer, InventoryItem> entry : matches.entrySet()) {
                if (entry.getValue().item == input.item) {
                    int free = Math.max(0, entry.getValue().itemStackSize() - entry.getValue().getAmount());
                    if (free > 0 && preferredSlot == 0) preferredSlot = entry.getKey();
                    available += free;
                }
            }
            input.setAmount(Math.min(input.getAmount(), available));
        }

        return super.addItem(level, player, input, preferredSlot, isLocked, purpose, previousMoveToInventory);
    }

    // notes //

    //region player

    // add new (player dragging):
    // main:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      setItem:72, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:102, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:117, ContainerSlot (necesse.inventory.container.slots)
    //      applyLeftClick:264, Container (necesse.inventory.container)
    // ServerThread:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      setItem:72, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:102, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:117, ContainerSlot (necesse.inventory.container.slots)
    //      applyLeftClick:264, Container (necesse.inventory.container)
    // main:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    // combine stack (player shift click):
    // main:
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // ServerThread:
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // main:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    // combine overflow (player shift click):
    // main:    // combineItem 141: combine 80 become full
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // main:    // excess 61, slot 0
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // main:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      setItem:72, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:102, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // ServerThread:    // combineItem 141: combine 80 become full
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // ServerThread:    // excess 61, slot 0
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // ServerThread:
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      setItem:72, ContainerSlot (necesse.inventory.container.slots)
    //      combineSlots:102, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // main: slot 0 update
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)
    // main: slot 1 update
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    // combine overflow (player drag)
    // main:    // combineItem 141: combine 80 become full
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // ServerThread:    // combineItem 141: combine 80 become full
    //      combineItem:617, Inventory (necesse.inventory)
    //      combineSlots:105, ContainerSlot (necesse.inventory.container.slots)
    //      transferToSlots:521, Container (necesse.inventory.container)
    //      transferToSlots:508, Container (necesse.inventory.container)
    //      transferFromAmount:324, Container (necesse.inventory.container)
    //      applyMove1LeftClick:355, Container (necesse.inventory.container)
    // main: slot 0 update full
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    // try combine stack for each slot from 0 even if different item
    //

    //endregion player


    //region npc

    // Adding to current stack
    // ServerThread:
    //      canAddItem this -> 0 if no item in chest like item, X if have
    //      canAddItem:559, Inventory (necesse.inventory)
    //      getJobSequence:103, HaulFromLevelJob (necesse.level.maps.levelData.jobs)
    //      getJobSequence:84, HaulFromLevelJob (necesse.level.maps.levelData.jobs)
    // ServerThread:
    //      (if X items exist)
    //      addItem:527, Inventory (necesse.inventory)
    //      addItem:450, Inventory (necesse.inventory)
    //      addItem:101, StorageDropOff (necesse.level.maps.levelData.settlementData)
    //      perform:70, DropOffSettlementStorageActiveJob (necesse.entity.mobs.job.activeJob)
    // main: update
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    // Adding to current stack (has item and overflow)
    // ServerThread:
    //      canAddItem this
    //      canAddItem:559, Inventory (necesse.inventory)
    //      getJobSequence:103, HaulFromLevelJob (necesse.level.maps.levelData.jobs)
    //      getJobSequence:84, HaulFromLevelJob (necesse.level.maps.levelData.jobs)
    // ServerThread:
    //      addItem:527, Inventory (necesse.inventory)
    //      addItem:450, Inventory (necesse.inventory)
    //      addItem:101, StorageDropOff (necesse.level.maps.levelData.settlementData)
    //      perform:70, DropOffSettlementStorageActiveJob (necesse.entity.mobs.job.activeJob)
    // ServerThread: (slot 1)
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      addItem:545, Inventory (necesse.inventory)
    //      addItem:450, Inventory (necesse.inventory)
    //      addItem:101, StorageDropOff (necesse.level.maps.levelData.settlementData)
    //      perform:70, DropOffSettlementStorageActiveJob (necesse.entity.mobs.job.activeJob)
    // main: update slot 0
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)
    // main: update slot 1
    //      setItem:196, Inventory (necesse.inventory)
    //      setItem:192, Inventory (necesse.inventory)
    //      processClient:57, PacketOEInventoryUpdate (necesse.engine.network.packet)
    //      processClient:247, NetworkPacket (necesse.engine.network)
    //      frameTick:214, Client (necesse.engine.network.client)

    //endregion npc
}
