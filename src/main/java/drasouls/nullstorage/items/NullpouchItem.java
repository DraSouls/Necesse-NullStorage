package drasouls.nullstorage.items;

import drasouls.nullstorage.NullStorage;
import drasouls.nullstorage.NullingInventory;
import necesse.engine.Screen;
import necesse.engine.localization.Localization;
import necesse.engine.network.gameNetworkData.GNDItem;
import necesse.engine.network.gameNetworkData.GNDItemInventory;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameColor;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.container.item.ItemInventoryContainer;
import necesse.inventory.item.Item;
import necesse.inventory.item.miscItem.PouchItem;
import necesse.level.maps.Level;

public class NullpouchItem extends PouchItem {
    private long l = 0L;

    public NullpouchItem() {
        this.rarity = Item.Rarity.UNCOMMON;
        this.canEatFoodFromPouch = true;
        this.canUseBuffPotionsFromPouch = true;
        this.canUseHealthPotionsFromPouch = true;
        this.drawStoredItems = false;
    }

    @Override
    public ListGameTooltips getTooltips(InventoryItem item, PlayerMob p) {
        ListGameTooltips tooltips = super.getTooltips(item, p);
        tooltips.add(Localization.translate("itemtooltip", "rclickinvopentip"));
        if (!Screen.isKeyDown(340) && !Screen.isKeyDown(344)) {
            tooltips.add(new StringTooltips(Localization.translate("ui", "shiftmoreinfo"), GameColor.LIGHT_GRAY));
            this.l = 0;
        } else {
            tooltips.add(Localization.translate("itemtooltip", "drs_nullpouch_tip1"));
            tooltips.add(Localization.translate("itemtooltip", "drs_nullpouch_tip2"));
            tooltips.add(Localization.translate("itemtooltip", "drs_nullpouch_tip3"));
            if (this.l == 0) this.l = p.getWorldEntity().getTime() - 420;
            else if (p.getWorldEntity().getTime() - this.l > 6969)
                tooltips.add(new StringTooltips(Localization.translate("itemtooltip", "drs_nullpouch_unk0"), GameColor.CYAN));
        }

        return tooltips;
    }

    @Override
    protected void openContainer(ServerClient client, int slotIndex) {
        PacketOpenContainer p = new PacketOpenContainer(NullStorage.nullpouchContainer, ItemInventoryContainer.getContainerContent(this, slotIndex));
        ContainerRegistry.openAndSendContainer(client, p);
    }

    // Combine/add stuff
    @Override
    public boolean onCombine(Level level, PlayerMob player, InventoryItem me, InventoryItem other, int maxStackSize, int amount, String purpose) {
        boolean valid = false;
        System.out.println("purpose: "+purpose+ " inv:"+this.getInternalInventory(me));
        if (this.isValidPurpose(this.combinePurposes, this.isCombinePurposesBlacklist, purpose)) {
            if (purpose.equals("lootall")) {
                valid = this.isValidAddItem(other);
            } else {
                valid = this.isValidPouchItem(other);
            }
        }

        if (valid) {
            Inventory internalInventory = this.getInternalInventory(me);
            if (this.isValidPurpose(this.insertPurposes, this.isInsertPurposesBlacklist, purpose)) {
                return false;
            } else {
                int startAmount = Math.min(amount, other.getAmount());
                InventoryItem copy = other.copy(startAmount);
                internalInventory.addItem(level, player, copy, "pouchinsert");
                if (copy.getAmount() != startAmount) {
                    int diff = startAmount - copy.getAmount();
                    other.setAmount(other.getAmount() - diff);
                    this.saveInternalInventory(me, internalInventory);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return super.onCombine(level, player, me, other, maxStackSize, amount, purpose);
        }
    }


    @Override
    public boolean inventoryAddItem(Level level, PlayerMob player, InventoryItem item, InventoryItem input, String purpose, boolean isValid, int stackLimit) {
        System.out.println("a "+purpose);
        if (this.isValidAddItem(input) && this.isValidPurpose(this.insertPurposes, this.isInsertPurposesBlacklist, purpose)) {
            Inventory internalInventory = this.getInternalInventory(item);
            if (this.isValidPurpose(this.insertPurposes, this.isInsertPurposesBlacklist, purpose)) {
                if (internalInventory.getFirstItem(level, player, new Item[]{ input.item }, purpose) == null) {
                    return false;
                }
            }
            boolean success = internalInventory.addItem(level, player, input, purpose);
            if (success) {
                this.saveInternalInventory(item, internalInventory);
                return true;
            }
        }

        return super.inventoryAddItem(level, player, item, input, purpose, isValid, stackLimit);
    }

    @Override
    public int inventoryCanAddItem(Level level, PlayerMob player, InventoryItem item, InventoryItem input, String purpose, boolean isValid, int stackLimit) {
        System.out.println("b "+purpose);
        Inventory internalInventory = this.getInternalInventory(item);
        if (this.isValidPurpose(this.insertPurposes, this.isInsertPurposesBlacklist, purpose)) {
            if (internalInventory.getFirstItem(level, player, new Item[]{ input.item }, purpose) == null) {
                return 0;
            }
        }
        if (this.isValidAddItem(input)) {
            return internalInventory.canAddItem(level, player, input, purpose);
        } else {
            return super.inventoryCanAddItem(level, player, item, input, purpose, isValid, stackLimit);
        }
    }


    // Save/load stuff
    @Override
    public Inventory getInternalInventory(InventoryItem item) {
        GNDItem gndItem = item.getGndData().getItem("inventory");
        if (gndItem instanceof GNDItemInventory) {
            GNDItemInventory gndInventory = (GNDItemInventory)gndItem;
            Inventory old = gndInventory.inventory;
            gndInventory.inventory = new NullingInventory(this.getInternalInventorySize());
            gndInventory.inventory.override(old);

            return gndInventory.inventory;
        } else {
            Inventory inventory = new NullingInventory(this.getInternalInventorySize());
            item.getGndData().setItem("inventory", new GNDItemInventory(inventory));
            return inventory;
        }
    }


    @Override
    public boolean isValidPouchItem(InventoryItem item) {
        return this.isValidRequestItem(item.item);
    }

    @Override
    public boolean isValidRequestItem(Item item) {
        return item != this;
    }

    @Override
    public boolean isValidRequestType(Item.Type type) {
        return false;
    }

    @Override
    public int getInternalInventorySize() {
        return 10;
    }

    // ser inventoryAddItem:293 purpose "itempickup"  125
    // cli inventoryAddItem:293 purpose "itempickup"  125
    // ser inventoryAddItem:293 purpose "itempickup"  250
    // cli inventoryAddItem:293 purpose "itempickup"  250

    // not in inv:
    // ser inventoryAddItem:293 purpose "itempickup"  125
    // cli inventoryAddItem:293 purpose "itempickup"  125
}
