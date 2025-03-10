package net.minecraft.world.item;

import java.util.List;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;

public class SmithingTemplateItem extends Item {

    private static final EnumChatFormat TITLE_FORMAT = EnumChatFormat.GRAY;
    private static final EnumChatFormat DESCRIPTION_FORMAT = EnumChatFormat.BLUE;
    private static final IChatBaseComponent INGREDIENTS_TITLE = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.ingredients"))).withStyle(SmithingTemplateItem.TITLE_FORMAT);
    private static final IChatBaseComponent APPLIES_TO_TITLE = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.applies_to"))).withStyle(SmithingTemplateItem.TITLE_FORMAT);
    private static final IChatBaseComponent SMITHING_TEMPLATE_SUFFIX = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template"))).withStyle(SmithingTemplateItem.TITLE_FORMAT);
    private static final IChatBaseComponent ARMOR_TRIM_APPLIES_TO = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.armor_trim.applies_to"))).withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT);
    private static final IChatBaseComponent ARMOR_TRIM_INGREDIENTS = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.armor_trim.ingredients"))).withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT);
    private static final IChatBaseComponent ARMOR_TRIM_BASE_SLOT_DESCRIPTION = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.armor_trim.base_slot_description")));
    private static final IChatBaseComponent ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.armor_trim.additions_slot_description")));
    private static final IChatBaseComponent NETHERITE_UPGRADE_APPLIES_TO = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.netherite_upgrade.applies_to"))).withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT);
    private static final IChatBaseComponent NETHERITE_UPGRADE_INGREDIENTS = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.netherite_upgrade.ingredients"))).withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT);
    private static final IChatBaseComponent NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.netherite_upgrade.base_slot_description")));
    private static final IChatBaseComponent NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.netherite_upgrade.additions_slot_description")));
    private static final MinecraftKey EMPTY_SLOT_HELMET = MinecraftKey.withDefaultNamespace("container/slot/helmet");
    private static final MinecraftKey EMPTY_SLOT_CHESTPLATE = MinecraftKey.withDefaultNamespace("container/slot/chestplate");
    private static final MinecraftKey EMPTY_SLOT_LEGGINGS = MinecraftKey.withDefaultNamespace("container/slot/leggings");
    private static final MinecraftKey EMPTY_SLOT_BOOTS = MinecraftKey.withDefaultNamespace("container/slot/boots");
    private static final MinecraftKey EMPTY_SLOT_HOE = MinecraftKey.withDefaultNamespace("container/slot/hoe");
    private static final MinecraftKey EMPTY_SLOT_AXE = MinecraftKey.withDefaultNamespace("container/slot/axe");
    private static final MinecraftKey EMPTY_SLOT_SWORD = MinecraftKey.withDefaultNamespace("container/slot/sword");
    private static final MinecraftKey EMPTY_SLOT_SHOVEL = MinecraftKey.withDefaultNamespace("container/slot/shovel");
    private static final MinecraftKey EMPTY_SLOT_PICKAXE = MinecraftKey.withDefaultNamespace("container/slot/pickaxe");
    private static final MinecraftKey EMPTY_SLOT_INGOT = MinecraftKey.withDefaultNamespace("container/slot/ingot");
    private static final MinecraftKey EMPTY_SLOT_REDSTONE_DUST = MinecraftKey.withDefaultNamespace("container/slot/redstone_dust");
    private static final MinecraftKey EMPTY_SLOT_QUARTZ = MinecraftKey.withDefaultNamespace("container/slot/quartz");
    private static final MinecraftKey EMPTY_SLOT_EMERALD = MinecraftKey.withDefaultNamespace("container/slot/emerald");
    private static final MinecraftKey EMPTY_SLOT_DIAMOND = MinecraftKey.withDefaultNamespace("container/slot/diamond");
    private static final MinecraftKey EMPTY_SLOT_LAPIS_LAZULI = MinecraftKey.withDefaultNamespace("container/slot/lapis_lazuli");
    private static final MinecraftKey EMPTY_SLOT_AMETHYST_SHARD = MinecraftKey.withDefaultNamespace("container/slot/amethyst_shard");
    private final IChatBaseComponent appliesTo;
    private final IChatBaseComponent ingredients;
    private final IChatBaseComponent baseSlotDescription;
    private final IChatBaseComponent additionsSlotDescription;
    private final List<MinecraftKey> baseSlotEmptyIcons;
    private final List<MinecraftKey> additionalSlotEmptyIcons;

    public SmithingTemplateItem(IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1, IChatBaseComponent ichatbasecomponent2, IChatBaseComponent ichatbasecomponent3, List<MinecraftKey> list, List<MinecraftKey> list1, Item.Info item_info) {
        super(item_info);
        this.appliesTo = ichatbasecomponent;
        this.ingredients = ichatbasecomponent1;
        this.baseSlotDescription = ichatbasecomponent2;
        this.additionsSlotDescription = ichatbasecomponent3;
        this.baseSlotEmptyIcons = list;
        this.additionalSlotEmptyIcons = list1;
    }

    public static SmithingTemplateItem createArmorTrimTemplate(Item.Info item_info) {
        return new SmithingTemplateItem(SmithingTemplateItem.ARMOR_TRIM_APPLIES_TO, SmithingTemplateItem.ARMOR_TRIM_INGREDIENTS, SmithingTemplateItem.ARMOR_TRIM_BASE_SLOT_DESCRIPTION, SmithingTemplateItem.ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION, createTrimmableArmorIconList(), createTrimmableMaterialIconList(), item_info);
    }

    public static SmithingTemplateItem createNetheriteUpgradeTemplate(Item.Info item_info) {
        return new SmithingTemplateItem(SmithingTemplateItem.NETHERITE_UPGRADE_APPLIES_TO, SmithingTemplateItem.NETHERITE_UPGRADE_INGREDIENTS, SmithingTemplateItem.NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION, SmithingTemplateItem.NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createNetheriteUpgradeIconList(), createNetheriteUpgradeMaterialList(), item_info);
    }

    private static List<MinecraftKey> createTrimmableArmorIconList() {
        return List.of(SmithingTemplateItem.EMPTY_SLOT_HELMET, SmithingTemplateItem.EMPTY_SLOT_CHESTPLATE, SmithingTemplateItem.EMPTY_SLOT_LEGGINGS, SmithingTemplateItem.EMPTY_SLOT_BOOTS);
    }

    private static List<MinecraftKey> createTrimmableMaterialIconList() {
        return List.of(SmithingTemplateItem.EMPTY_SLOT_INGOT, SmithingTemplateItem.EMPTY_SLOT_REDSTONE_DUST, SmithingTemplateItem.EMPTY_SLOT_LAPIS_LAZULI, SmithingTemplateItem.EMPTY_SLOT_QUARTZ, SmithingTemplateItem.EMPTY_SLOT_DIAMOND, SmithingTemplateItem.EMPTY_SLOT_EMERALD, SmithingTemplateItem.EMPTY_SLOT_AMETHYST_SHARD);
    }

    private static List<MinecraftKey> createNetheriteUpgradeIconList() {
        return List.of(SmithingTemplateItem.EMPTY_SLOT_HELMET, SmithingTemplateItem.EMPTY_SLOT_SWORD, SmithingTemplateItem.EMPTY_SLOT_CHESTPLATE, SmithingTemplateItem.EMPTY_SLOT_PICKAXE, SmithingTemplateItem.EMPTY_SLOT_LEGGINGS, SmithingTemplateItem.EMPTY_SLOT_AXE, SmithingTemplateItem.EMPTY_SLOT_BOOTS, SmithingTemplateItem.EMPTY_SLOT_HOE, SmithingTemplateItem.EMPTY_SLOT_SHOVEL);
    }

    private static List<MinecraftKey> createNetheriteUpgradeMaterialList() {
        return List.of(SmithingTemplateItem.EMPTY_SLOT_INGOT);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        list.add(SmithingTemplateItem.SMITHING_TEMPLATE_SUFFIX);
        list.add(CommonComponents.EMPTY);
        list.add(SmithingTemplateItem.APPLIES_TO_TITLE);
        list.add(CommonComponents.space().append(this.appliesTo));
        list.add(SmithingTemplateItem.INGREDIENTS_TITLE);
        list.add(CommonComponents.space().append(this.ingredients));
    }

    public IChatBaseComponent getBaseSlotDescription() {
        return this.baseSlotDescription;
    }

    public IChatBaseComponent getAdditionSlotDescription() {
        return this.additionsSlotDescription;
    }

    public List<MinecraftKey> getBaseSlotEmptyIcons() {
        return this.baseSlotEmptyIcons;
    }

    public List<MinecraftKey> getAdditionalSlotEmptyIcons() {
        return this.additionalSlotEmptyIcons;
    }
}
