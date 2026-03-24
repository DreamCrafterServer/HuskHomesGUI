/*
 * This file is part of HuskHomesGUI, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.gui.dialog;

import de.themoep.inventorygui.GuiElement;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.gui.menu.EditMenu;
import net.william278.huskhomes.gui.menu.ListMenu;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.ValidationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DescEditing<T extends SavedPosition> {

    private HuskHomesAPI api;
    private final T position;
    private final GuiElement.Click click;
    private HuskHomesGui plugin;
    private final ListMenu<T> parentMenu;
    private final int pageNumber;

    public DescEditing(@NotNull T position, GuiElement.Click click, @NotNull ListMenu<T> parentMenu, int pageNumber , HuskHomesGui plugin){
        api = HuskHomesAPI.getInstance();
        this.position = position;
        this.click = click;
        this.plugin = plugin;
        this.parentMenu = parentMenu;
        this.pageNumber = pageNumber;
    }

    public void open(Player player){

        Dialog dialog = Dialog.create(builder ->
                builder.empty()
                        .base(DialogBase.builder(localized("dialog_edit_description_title"))
                                .body(List.of(
                                        DialogBody.plainMessage(localized("dialog_edit_description_body"))
                                ))

                                .inputs(List.of(
                                        DialogInput.text("huskhomesgui_home_desc", localized("dialog_edit_description_input"))
                                                .initial("")
                                                .width(300)
                                                .build()
                                ))

                                .build())
                        .type(DialogType.confirmation(
                                ActionButton.create(
                                        localized("dialog_action_confirm_title"),
                                        localized("dialog_action_confirm_description"),
                                        100,
                                        DialogAction.customClick((view, audience) -> {
                                                    if(!(audience instanceof Player p)) return;
                                                    if(view == null) return;

                                                    String newDesc = view.getText("huskhomesgui_home_desc"); // 可能為 null
                                                    if (newDesc == null || newDesc.isEmpty() ) return;

                                                    try {
                                                        if (position instanceof Home home) {
                                                            plugin.getHuskHomes().getManager().homes().setHomeDescription(home, newDesc);
                                                        } else if (position instanceof Warp warp) {
                                                            plugin.getHuskHomes().getManager().warps().setWarpDescription(warp, newDesc);
                                                        }
                                                    } catch (ValidationException e) {
                                                        if (position instanceof Home home) {
                                                            final boolean editingOtherHome = !player.getUniqueId().equals(home.getOwner().getUuid());
                                                            e.dispatchHomeError(api.adaptUser(player), editingOtherHome, plugin.getHuskHomes(), newDesc);
                                                        } else {
                                                            e.dispatchWarpError(api.adaptUser(player), plugin.getHuskHomes(), newDesc);
                                                        }
                                                        return;
                                                    }
                                                    position.getMeta().setDescription(newDesc);

                                                    // Refresh menu title
                                                    click.getGui().close(player);
                                                    click.getGui().destroy();
                                                    new EditMenu<>(plugin, position, parentMenu, pageNumber).show(api.adaptUser(player));


                                                }, ClickCallback.Options.builder()
                                                        .uses(1)
                                                        .build()
                                        )

                                ),
                                ActionButton.create(
                                        localized("dialog_action_cancel_title"),
                                        localized("dialog_action_cancel_description"),
                                        100,
                                        null
                                )
                        ))
        );




        player.getScheduler().execute(
                plugin,
                () -> {
                    player.showDialog(dialog);
                },null,1L
        );

    }

    @NotNull
    private Component localized(@NotNull String key, @NotNull String... replacements) {
        return Component.text(plugin.getLocales().getRawLocale(key, replacements).orElse(""));
    }

}
