/*
 * Copyright (c) 2023. JEFF Media GbR / mfnalex et al.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jeff_media.jefflib.internal.glowenchantment;

import com.jeff_media.jefflib.EnchantmentUtils;
import com.jeff_media.jefflib.PDCUtils;
import java.util.Objects;

import com.jeff_media.jefflib.internal.annotations.Internal;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

@Deprecated
@Internal
public abstract class GlowEnchantmentFactory {

    public static final NamespacedKey GLOW_ENCHANTMENT_KEY = Objects.requireNonNull(PDCUtils.getKeyFromString("jefflib", "glow"));

    private static final Enchantment instance;

    @Deprecated
    public static Enchantment getDeprecatedInstance() {
        return instance;
    }

    static {
        Enchantment existing = Enchantment.getByKey(GLOW_ENCHANTMENT_KEY);
        if (existing == null) {
            try {
                existing = new KeyedGlowEnchantment();
            } catch (Throwable ignored) {
                try {
                    existing = new LegacyGlowEnchantment();
                } catch (Throwable ignored2) {
//                    ServerUtils.log("Could not register glow enchantment");
//                    throw new RuntimeException("Could not register glow enchantment");
                }
            }
        }
        instance = existing;
    }

    public static void register() {
//        Throwable throwable = null;
        try {
            EnchantmentUtils.registerEnchantment(instance);
        } catch (Throwable throwable1) {
//            throwable = throwable1;
        }

//        if(instance == null && throwable != null) {
//            throw new RuntimeException("Could not register nor get existing glow enchantment",throwable);
//        }
    }

}
