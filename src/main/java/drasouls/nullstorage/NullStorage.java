package drasouls.nullstorage;

import drasouls.nullstorage.items.NoFillItemInventoryContainer;
import drasouls.nullstorage.items.NoFillOEInventoryContainer;
import drasouls.nullstorage.items.NullpouchItem;
import drasouls.nullstorage.objects.NullboxObject;
import drasouls.nullstorage.ui.NullboxContainerForm;
import drasouls.nullstorage.ui.NullpouchContainerForm;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.entity.objectEntity.interfaces.OEInventory;
import necesse.gfx.forms.presets.containerComponent.item.ItemInventoryContainerForm;
import necesse.gfx.forms.presets.containerComponent.object.OEInventoryContainerForm;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.container.item.ItemInventoryContainer;
import necesse.inventory.container.object.OEInventoryContainer;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;

import java.awt.*;

@ModEntry
public class NullStorage {
    public static GameTexture voidSlotIcon;
    public static int nullboxContainer;
    public static int nullpouchContainer;


    public void init() {
        // Containers are UI stuff
        nullboxContainer = Containers.registerContainer(NullboxContainerForm::new, NoFillOEInventoryContainer::new);
        nullpouchContainer = Containers.registerContainer(NullpouchContainerForm::new, NoFillItemInventoryContainer::new);

        ItemRegistry.registerItem("drs_nullpouch", new NullpouchItem(), 100.0f, true);

        ObjectRegistry.registerObject("drs_nullbox",
                new NullboxObject("drs_nullbox", 40, new Color(120, 95, 56)),
                15.0f,
                true);
    }

    public void initResources() {
        voidSlotIcon = GameTexture.fromFile("ui/legacy/drs_inventoryslot_icon_void");
    }

    public void postInit() {
        Recipes.registerModRecipe(new Recipe(
                "drs_nullpouch", 1,
                RecipeTechRegistry.DEMONIC,
                new Ingredient[]{
                        new Ingredient("leather", 8),
                        new Ingredient("voidshard", 12)
                }
        ));
        Recipes.registerModRecipe(new Recipe(
                "drs_nullbox", 1,
                RecipeTechRegistry.DEMONIC,
                new Ingredient[]{
                        new Ingredient("anylog", 8),
                        new Ingredient("voidshard", 12)
                }
        ));
    }


    public static final class Containers {
        private static int registerContainer(OEICFormConstructor formCtor, OEICConstructor containerCtor) {
            return ContainerRegistry.registerOEContainer(
                    (client, uniqueSeed, oe, content) ->
                            formCtor.create(client,
                                    containerCtor.create(client.getClient(), uniqueSeed, (OEInventory) oe, new PacketReader(content))
                            ),
                    (client, uniqueSeed, oe, content, serverObject) ->
                            containerCtor.create(client, uniqueSeed, (OEInventory) oe, new PacketReader(content))
            );
        }

        private static int registerContainer(IICFormConstructor formCtor, IICConstructor containerCtor) {
            return ContainerRegistry.registerContainer(
                    (client, uniqueSeed, packet) ->
                            formCtor.create(client, containerCtor.create(client.getClient(), uniqueSeed, packet)),
                    (client, uniqueSeed, packet, serverObject) ->
                            containerCtor.create(client, uniqueSeed, packet)
            );
        }

        @FunctionalInterface
        private interface OEICConstructor {
            <C extends NetworkClient> OEInventoryContainer create(C client, int uniqueSeed, OEInventory oe, PacketReader packetReader);
        }

        @FunctionalInterface
        private interface IICConstructor {
            <C extends NetworkClient> ItemInventoryContainer create(C client, int uniqueSeed, Packet packet);
        }

        @FunctionalInterface
        private interface OEICFormConstructor {
            <I extends OEInventoryContainer> OEInventoryContainerForm<? extends OEInventoryContainer> create(Client client, I container);
        }

        @FunctionalInterface
        private interface IICFormConstructor {
            <I extends ItemInventoryContainer> ItemInventoryContainerForm<? extends ItemInventoryContainer> create(Client client, I container);
        }
    }
}
