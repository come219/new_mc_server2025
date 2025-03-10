package net.minecraft.stats;

import net.minecraft.world.inventory.RecipeBookType;

public class RecipeBook {

    protected final RecipeBookSettings bookSettings = new RecipeBookSettings();

    public RecipeBook() {}

    public boolean isOpen(RecipeBookType recipebooktype) {
        return this.bookSettings.isOpen(recipebooktype);
    }

    public void setOpen(RecipeBookType recipebooktype, boolean flag) {
        this.bookSettings.setOpen(recipebooktype, flag);
    }

    public boolean isFiltering(RecipeBookType recipebooktype) {
        return this.bookSettings.isFiltering(recipebooktype);
    }

    public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
        this.bookSettings.setFiltering(recipebooktype, flag);
    }

    public void setBookSettings(RecipeBookSettings recipebooksettings) {
        this.bookSettings.replaceFrom(recipebooksettings);
    }

    public RecipeBookSettings getBookSettings() {
        return this.bookSettings.copy();
    }

    public void setBookSetting(RecipeBookType recipebooktype, boolean flag, boolean flag1) {
        this.bookSettings.setOpen(recipebooktype, flag);
        this.bookSettings.setFiltering(recipebooktype, flag1);
    }
}
