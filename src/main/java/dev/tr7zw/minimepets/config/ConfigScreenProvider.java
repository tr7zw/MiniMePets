package dev.tr7zw.minimepets.config;

import dev.tr7zw.minimepets.*;
import dev.tr7zw.transition.mc.*;
import dev.tr7zw.trender.gui.client.*;
import dev.tr7zw.trender.gui.widget.*;
import dev.tr7zw.trender.gui.widget.data.*;
import dev.tr7zw.trender.gui.widget.icon.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;

import java.util.*;

public class ConfigScreenProvider {

    public static Screen createConfigScreen(Screen parent) {
        return new CustomConfigScreen(parent).createScreen();
    }

    private static class CustomConfigScreen extends AbstractConfigScreen {

        public CustomConfigScreen(Screen previous) {
            super(ComponentProvider.translatable("text.minimepets.title"), previous);

            WGridPanel root = new WGridPanel(8);
            root.setInsets(Insets.ROOT_PANEL);
            setRootPanel(root);

            WTabPanel wTabPanel = new WTabPanel();

            wTabPanel.add(getList(MiniMeShared.CONFIG_MANAGER.getConfig().names),
                    b -> b.title(ComponentProvider.translatable("text.minimepets.tab.whitelist")));

            root.add(wTabPanel, 0, 1, 43, 25);

            List<OptionInstance> options = new ArrayList<>();

            options.add(
                    getOnOffOption("text.minimepets.whitelist", () -> MiniMeShared.CONFIG_MANAGER.getConfig().whitelist,
                            b -> MiniMeShared.CONFIG_MANAGER.getConfig().whitelist = b));

            var optionList = createOptionList(options);
            optionList.setGap(-1);
            optionList.setSize(6 * 20, 1 * 20);
            root.add(optionList, 0, 23, 45, 4);

            WTextField addBox = new WTextField();
            root.add(addBox, 20, 24, 17, 1);
            WButton addButton = new WButton(ComponentProvider.translatable("text.minimepets.add"));
            addButton.setOnClick(() -> {
                MiniMeShared.CONFIG_MANAGER.getConfig().names.add(addBox.getText());
                addBox.setText("");
                root.layout();
                MiniMeShared.CONFIG_MANAGER.writeConfig();
            });
            root.add(addButton, 38, 24, 6, 2);

            WButton doneButton = new WButton(CommonComponents.GUI_DONE);
            doneButton.setOnClick(() -> {
                save();
                GeneralUtil.setScreen(previous);
            });
            root.add(doneButton, 0, 27, 6, 2);

            WButton resetButton = new WButton(ComponentProvider.translatable("controls.reset"));
            resetButton.setOnClick(() -> {
                reset();
                root.layout();
            });
            root.add(resetButton, 37, 27, 6, 2);

            root.setBackgroundPainter(BackgroundPainter.VANILLA);

            root.validate(this);
            root.setHost(this);
        }

        private String getResourceString(ResourceKey key) {
            //? if >= 1.21.11 {

            return key.identifier().toString();
            //? } else {
            /*
                return key.location().toString();
            *///? }
        }

        private WWidget getList(List<String> list) {
            WListPanel<String, WGridPanel> listPanel = new WListPanel<String, WGridPanel>(list, () -> new WGridPanel(),
                    (s, panel) -> {
                        panel.setGaps(-17, -17);
                        WButton button = new WButton(new ItemIcon(Items.BARRIER));
                        button.setToolip(ComponentProvider.translatable("text.minimepets.remove"));
                        button.setOnClick(() -> {
                            list.remove(s);
                            panel.getParent().layout();
                            MiniMeShared.CONFIG_MANAGER.writeConfig();
                        });
                        panel.add(button, 0, 0, 2, 2);
                        panel.add(new WLabel(ComponentProvider.literal(s)), 23, 5);
                    });
            listPanel.setGap(0);
            listPanel.setSize(18 * 20, 8 * 20);
            return listPanel;
        }

        @Override
        public void reset() {
            MiniMeShared.CONFIG_MANAGER.reset();
        }

        @Override
        public void save() {
            MiniMeShared.CONFIG_MANAGER.writeConfig();
        }

    }

}
