package drasouls.nullstorage.objects.objectEntities;

import drasouls.nullstorage.NullingInventory;
import necesse.engine.Screen;
import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.save.levelData.InventorySave;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.InventoryObjectEntity;
import necesse.gfx.fairType.FairType;
import necesse.gfx.fairType.FairTypeDrawOptions;
import necesse.gfx.forms.presets.containerComponent.object.OEInventoryContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.FairTypeTooltip;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryFilter;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;

import java.util.ArrayList;

public class NullboxObjectEntity extends InventoryObjectEntity {
    public final Inventory altInventory;
    private FairTypeDrawOptions textDrawOptions;
    private String name;
    private int textDrawFontSize;

    public NullboxObjectEntity(Level level, int x, int y, int slots) {
        super(level, x, y, slots);
        this.name = "";
        this.altInventory = new NullingInventory(slots);
        this.altInventory.filter = new InventoryFilter() {
            @Override
            public boolean isItemValid(int slot, InventoryItem item) {
                return NullboxObjectEntity.this.isItemValid(slot, item);
            }

            @Override
            public int getItemStackLimit(int slot, InventoryItem item) {
                return NullboxObjectEntity.this.getItemStackLimit(slot, item);
            }
        };
    }

    // from InventoryObjectEntity. because inventory is final, and we have to use our own inventory.
    @Override
    public void addSaveData(SaveData save) {
        save.addSafeString("name", this.name);
        save.addSaveData(InventorySave.getSave(this.altInventory));
    }

    @Override
    public void applyLoadData(LoadData save) {
        this.setInventoryName(save.getSafeString("name", this.name));
        this.altInventory.override(InventorySave.loadSave(save.getFirstLoadDataByName("INVENTORY")));
    }

    @Override
    public void setupContentPacket(PacketWriter writer) {
        this.users.writeUsersSpawnPacket(writer);
        this.altInventory.writeContent(writer);
        writer.putNextString(this.name);
    }

    @Override
    public void applyContentPacket(PacketReader reader) {
        this.users.readUsersSpawnPacket(reader, this);
        this.altInventory.override(Inventory.getInventory(reader));
        this.setInventoryName(reader.getNextString());
    }

    @Override
    public ArrayList<InventoryItem> getDroppedItems() {
        ArrayList<InventoryItem> list = new ArrayList<>();

        for(int i = 0; i < this.altInventory.getSize(); ++i) {
            if (!this.altInventory.isSlotClear(i)) {
                list.add(this.altInventory.getItem(i));
            }
        }

        return list;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        this.altInventory.tickItems(this);
        this.serverTickInventorySync(this.getLevel().getServer(), this);
        this.users.serverTick(this);
    }

    @Override
    public void clientTick() {
        super.clientTick();
        this.altInventory.tickItems(this);
        this.users.clientTick(this);
    }

    @Override
    public Inventory getInventory() {
        return this.altInventory;
    }


    @Override
    public void onMouseHover(PlayerMob perspective, boolean debug) {
        super.onMouseHover(perspective, debug);
        if (!this.name.isEmpty()) {
            Screen.addTooltip(new FairTypeTooltip(this.getTextDrawOptions()));
        }

    }

    @Override
    public GameMessage getInventoryName() {
        return this.name.isEmpty() ? this.getLevel().getObjectName(this.getTileX(), this.getTileY()) : new StaticMessage(this.name);
    }

    @Override
    public void setInventoryName(String name) {
        String oldName = this.name;
        if (this.getLevel().getObjectName(this.getX(), this.getY()).translate().equals(name)) {
            this.name = "";
        } else {
            this.name = name;
        }

        if (!this.name.equals(oldName)) {
            this.textDrawOptions = null;
        }

    }

    private FairTypeDrawOptions getTextDrawOptions() {
        if (this.textDrawOptions == null || this.textDrawFontSize != Settings.tooltipTextSize) {
            FairType type = new FairType();
            FontOptions fontOptions = (new FontOptions(Settings.tooltipTextSize)).outline();
            type.append(fontOptions, this.getInventoryName().translate());
            type.applyParsers(OEInventoryContainerForm.getParsers(fontOptions));
            this.textDrawOptions = type.getDrawOptions(FairType.TextAlign.LEFT);
            this.textDrawFontSize = fontOptions.getSize();
        }

        return this.textDrawOptions;
    }
}
