package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.inventory.container.tile.filter.LSModIDFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTModIDFilter extends GuiModIDFilter<TModIDFilter, TileEntityLogisticalSorter, LSModIDFilterContainer> {

    public GuiTModIDFilter(PlayerEntity player, TileEntityLogisticalSorter tile, int index) {
        //TODO
        super(player, tile);
        origFilter = (TModIDFilter) tileEntity.filters.get(index);
        filter = ((TModIDFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getModID());
    }

    public GuiTModIDFilter(LSModIDFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        isNew = true;
        filter = new TModIDFilter();
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTModIDFilter.png");
    }

    @Override
    protected void updateStackList(String modName) {
        iterStacks = OreDictCache.getModIDStacks(modName, false);
        stackSwitch = 0;
        stackIndex = -1;
    }

    @Override
    protected void addButtons() {
        buttons.add(saveButton = new GuiButtonTranslation(guiLeft + 47, guiTop + 62, 60, 20, "gui.save", onPress -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getModID() != null && !filter.getModID().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.modIDFilter.noID"));
                ticker = 20;
            }
        }));
        buttons.add(deleteButton = new GuiButtonTranslation(guiLeft + 109, guiTop + 62, 60, 20, "gui.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? 5 : 0)));
        buttons.add(defaultButton = new GuiButtonDisableableImage(guiLeft + 11, guiTop + 64, 11, 11, 199, 11, -11, getGuiLocation(),
              onPress -> filter.allowDefault = !filter.allowDefault));
        buttons.add(checkboxButton = new GuiButtonDisableableImage(guiLeft + 131, guiTop + 47, 12, 12, 187, 12, -12, getGuiLocation(),
              onPress -> setText()));
        buttons.add(colorButton = new GuiColorButton(guiLeft + 12, guiTop + 44, 16, 16, () -> filter.color,
              onPress -> {
                  if (InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                      filter.color = null;
                  } else {
                      filter.color = TransporterUtils.increment(filter.color);
                  }
              }));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }
}