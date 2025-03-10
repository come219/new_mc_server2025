package net.minecraft.world.inventory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public class ContainerSmithing extends ContainerAnvilAbstract {

    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final World level;
    private final RecipePropertySet baseItemTest;
    private final RecipePropertySet templateItemTest;
    private final RecipePropertySet additionItemTest;
    private final ContainerProperty hasRecipeError;

    public ContainerSmithing(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerSmithing(int i, PlayerInventory playerinventory, ContainerAccess containeraccess) {
        this(i, playerinventory, containeraccess, playerinventory.player.level());
    }

    private ContainerSmithing(int i, PlayerInventory playerinventory, ContainerAccess containeraccess, World world) {
        super(Containers.SMITHING, i, playerinventory, containeraccess, createInputSlotDefinitions(world.recipeAccess()));
        this.hasRecipeError = ContainerProperty.standalone();
        this.level = world;
        this.baseItemTest = world.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE);
        this.templateItemTest = world.recipeAccess().propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        this.additionItemTest = world.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
        this.addDataSlot(this.hasRecipeError).set(0);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions(RecipeAccess recipeaccess) {
        RecipePropertySet recipepropertyset = recipeaccess.propertySet(RecipePropertySet.SMITHING_BASE);
        RecipePropertySet recipepropertyset1 = recipeaccess.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        RecipePropertySet recipepropertyset2 = recipeaccess.propertySet(RecipePropertySet.SMITHING_ADDITION);
        ItemCombinerMenuSlotDefinition.a itemcombinermenuslotdefinition_a = ItemCombinerMenuSlotDefinition.create();

        Objects.requireNonNull(recipepropertyset1);
        itemcombinermenuslotdefinition_a = itemcombinermenuslotdefinition_a.withSlot(0, 8, 48, recipepropertyset1::test);
        Objects.requireNonNull(recipepropertyset);
        itemcombinermenuslotdefinition_a = itemcombinermenuslotdefinition_a.withSlot(1, 26, 48, recipepropertyset::test);
        Objects.requireNonNull(recipepropertyset2);
        return itemcombinermenuslotdefinition_a.withSlot(2, 44, 48, recipepropertyset2::test).withResultSlot(3, 98, 48).build();
    }

    @Override
    protected boolean isValidBlock(IBlockData iblockdata) {
        return iblockdata.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected void onTake(EntityHuman entityhuman, ItemStack itemstack) {
        itemstack.onCraftedBy(entityhuman.level(), entityhuman, itemstack.getCount());
        this.resultSlots.awardUsedRecipes(entityhuman, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((world, blockposition) -> {
            world.levelEvent(1044, blockposition, 0);
        });
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private SmithingRecipeInput createRecipeInput() {
        return new SmithingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private void shrinkStackInSlot(int i) {
        ItemStack itemstack = this.inputSlots.getItem(i);

        if (!itemstack.isEmpty()) {
            itemstack.shrink(1);
            this.inputSlots.setItem(i, itemstack);
        }

    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        super.slotsChanged(iinventory);
        if (this.level instanceof WorldServer) {
            boolean flag = this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && this.getSlot(2).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();

            this.hasRecipeError.set(flag ? 1 : 0);
        }

    }

    @Override
    public void createResult() {
        SmithingRecipeInput smithingrecipeinput = this.createRecipeInput();
        World world = this.level;
        Optional optional;

        if (world instanceof WorldServer worldserver) {
            optional = worldserver.recipeAccess().getRecipeFor(Recipes.SMITHING, smithingrecipeinput, worldserver);
        } else {
            optional = Optional.empty();
        }

        optional.ifPresentOrElse((recipeholder) -> {
            ItemStack itemstack = ((SmithingRecipe) recipeholder.value()).assemble(smithingrecipeinput, this.level.registryAccess());

            this.resultSlots.setRecipeUsed(recipeholder);
            this.resultSlots.setItem(0, itemstack);
        }, () -> {
            this.resultSlots.setRecipeUsed((RecipeHolder) null);
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack itemstack) {
        return this.templateItemTest.test(itemstack) && !this.getSlot(0).hasItem() ? true : (this.baseItemTest.test(itemstack) && !this.getSlot(1).hasItem() ? true : this.additionItemTest.test(itemstack) && !this.getSlot(2).hasItem());
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError.get() > 0;
    }
}
