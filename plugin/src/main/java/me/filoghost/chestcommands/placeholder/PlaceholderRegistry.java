/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import me.filoghost.chestcommands.api.PlaceholderReplacer;
import me.filoghost.chestcommands.placeholder.scanner.PlaceholderMatch;
import org.bukkit.plugin.Plugin;

public class PlaceholderRegistry {

    private final Map<String, PlaceholderReplacer> internalPlaceholders = new HashMap<>();
    private final Map<String, Map<String, PlaceholderReplacer>> externalPlaceholders = new HashMap<>();

    public void registerInternalPlaceholder(String identifier, PlaceholderReplacer replacer) {
        internalPlaceholders.put(identifier, replacer);
    }

    public void registerExternalPlaceholder(Plugin plugin, String identifier, PlaceholderReplacer placeholderReplacer) {
        externalPlaceholders
                .computeIfAbsent(identifier, key -> new LinkedHashMap<>())
                .put(plugin.getName(), placeholderReplacer);
    }

    public PlaceholderReplacer getPlaceholderReplacer(PlaceholderMatch placeholderMatch) {
        if (placeholderMatch.getPluginNamespace() == null) {
            PlaceholderReplacer internalReplacer = internalPlaceholders.get(placeholderMatch.getIdentifier());
            if (internalReplacer != null) {
                return internalReplacer;
            }
        }

        Map<String, PlaceholderReplacer> externalReplacers = externalPlaceholders.get(placeholderMatch.getIdentifier());

        // Find exact replacer if plugin name is specified
        if (placeholderMatch.getPluginNamespace() != null) {
            return externalReplacers.get(placeholderMatch.getPluginNamespace());
        }

        if (externalReplacers != null && !externalReplacers.isEmpty()) {
            return externalReplacers.values().iterator().next();
        }

        return null;
    }

}
