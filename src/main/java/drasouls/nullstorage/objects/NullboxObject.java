package drasouls.nullstorage.objects;

import drasouls.nullstorage.NullStorage;
import drasouls.nullstorage.objects.objectEntities.NullboxObjectEntity;
import necesse.engine.Screen;
import necesse.engine.localization.Localization;
import necesse.engine.sound.SoundEffect;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.gfx.GameColor;
import necesse.gfx.GameResources;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.container.object.OEInventoryContainer;
import necesse.level.gameObject.furniture.StorageBoxInventoryObject;
import necesse.level.maps.Level;

import java.awt.*;

public class NullboxObject extends StorageBoxInventoryObject {
    public NullboxObject(String textureName, int slots, Color mapColor) {
        super(textureName, slots, mapColor);
    }

    @Override
    public ListGameTooltips getItemTooltips(InventoryItem item, PlayerMob perspective) {
        ListGameTooltips tooltips = new ListGameTooltips();
        tooltips.add(Localization.translate("itemtooltip", "placetip"));
        if (!Screen.isKeyDown(340) && !Screen.isKeyDown(344)) {
            tooltips.add(new StringTooltips(Localization.translate("ui", "shiftmoreinfo"), GameColor.LIGHT_GRAY));
        } else {
            tooltips.add(Localization.translate("itemtooltip", "drs_nullbox_tip1"));
            tooltips.add(Localization.translate("itemtooltip", "drs_nullbox_tip2"));
            tooltips.add(Localization.translate("itemtooltip", "drs_nullbox_tip3"));
            tooltips.add(Localization.translate("itemtooltip", "drs_nullbox_tip4"));
        }
        return tooltips;
    }

    @Override
    public void interact(Level level, int x, int y, PlayerMob player) {
        if (level.isServerLevel()) {
            OEInventoryContainer.openAndSendContainer(NullStorage.nullboxContainer, player.getServerClient(), level, x, y);
        }
    }

    @Override
    public ObjectEntity getNewObjectEntity(Level level, int x, int y) {
        return new NullboxObjectEntity(level, x, y, this.slots);
    }

    @Override
    public void playOpenSound(Level level, int tileX, int tileY) {
        if (this.openTexture != null) {
            Screen.playSound(GameResources.chestopen, SoundEffect.effect((float)(tileX * 32 + 16), (float)(tileY * 32 + 16)).pitch(0.7f));
        }
    }

    @Override
    public void playCloseSound(Level level, int tileX, int tileY) {
        if (this.openTexture != null) {
            Screen.playSound(GameResources.chestclose, SoundEffect.effect((float)(tileX * 32 + 16), (float)(tileY * 32 + 16)).pitch(0.7f));
        }
    }
}
