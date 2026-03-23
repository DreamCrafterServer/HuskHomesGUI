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

package net.william278.huskhomes.gui;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.gui.command.HuskHomesGuiCommand;
import net.william278.huskhomes.gui.config.Locales;
import net.william278.huskhomes.gui.config.Settings;
import net.william278.huskhomes.gui.listener.ListListener;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class HuskHomesGui extends JavaPlugin implements HuskHomesGuiPlugin {
    private static final String COMMAND_PERMISSION = "huskhomesgui.command";
    private static final String MINIMUM_MINECRAFT_VERSION = "1.21.6";
    private BukkitAudiences adventure;
    private Settings settings;
    private Locales locales;

    private HuskHomes huskHomes;
    public HuskHomes getHuskHomes(){
        return huskHomes;
    }

    public void onEnable() {
        if (!isSupportedServerVersion()) {
            getLogger().severe("HuskHomesGUI requires Minecraft 1.21.6 or newer. Please update the server before allowing players to join.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save huskhomes instance
        huskHomes = (HuskHomes) getServer().getPluginManager().getPlugin("HuskHomes");

        // Load audiences
        this.adventure = BukkitAudiences.create(this);

        // Load settings and locales
        this.reloadConfigFiles();

        // Register event listener, permission and command
        getServer().getPluginManager().registerEvents(new ListListener(this), this);
        registerPermissions();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(
                "huskhomesgui",
                "View HuskHomesGUI plugin information & reload configs",
                List.of(),
                new HuskHomesGuiCommand(this)
        ));

        // Log to console
        getLogger().log(Level.INFO, "Successfully enabled HuskHomes v" + getDescription().getVersion());
    }

    public void reloadConfigFiles() {
        this.settings = loadSettings();
        this.locales = loadLocales();
    }

    @Override
    @NotNull
    public AudienceProvider getAudiences() {
        return adventure;
    }

    @Override
    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    @NotNull
    public Locales getLocales() {
        return locales;
    }

    private void registerPermissions() {
        if (getServer().getPluginManager().getPermission(COMMAND_PERMISSION) == null) {
            getServer().getPluginManager().addPermission(new Permission(
                    COMMAND_PERMISSION,
                    "Allows access to the /huskhomesgui command",
                    PermissionDefault.OP
            ));
        }
    }

    private boolean isSupportedServerVersion() {
        return compareVersions(Bukkit.getMinecraftVersion(), MINIMUM_MINECRAFT_VERSION) >= 0;
    }

    private int compareVersions(@NotNull String actualVersion, @NotNull String minimumVersion) {
        final String[] actualParts = actualVersion.split("\\.");
        final String[] minimumParts = minimumVersion.split("\\.");
        final int maxLength = Math.max(actualParts.length, minimumParts.length);

        for (int i = 0; i < maxLength; i++) {
            final int actualPart = i < actualParts.length ? parseVersionPart(actualParts[i]) : 0;
            final int minimumPart = i < minimumParts.length ? parseVersionPart(minimumParts[i]) : 0;
            if (actualPart != minimumPart) {
                return Integer.compare(actualPart, minimumPart);
            }
        }

        return 0;
    }

    private int parseVersionPart(@NotNull String versionPart) {
        final String normalized = versionPart.replaceAll("[^0-9].*$", "");
        if (normalized.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(normalized);
    }
}
