package de.jeff_media.jefflib;

import org.bukkit.Material;
import org.bukkit.entity.Entity;

import java.util.*;

public class WordUtils {

    /**
     * Turns Material names into a nicer name. E.g. DIAMOND_PICKAXE will return "Diamond Pickaxe"
     * @param mat The Material
     * @return Human readable name
     */
    public static String getNiceMaterialName(Material mat) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(mat.name().split("_")).forEach(word -> builder.append(upperCaseFirstLetterOnly(word)));
        return builder.toString();
    }

    /**
     * Turns the first letter of a String to uppercase, while making the rest lowercase
     * @param word String to change
     */
    public static String upperCaseFirstLetterOnly(String word) {
        return upperCaseFirstLetter(word.toLowerCase(Locale.ROOT));
    }

    /**
     * Turns the first letter of a String to uppercase
     * @param word String to change
     */
    public static String upperCaseFirstLetter(String word) {
        if(word.length()<1) return word;
        if(word.length()==1) return word.toUpperCase(Locale.ROOT);
        return word.substring(0,1).toUpperCase(Locale.ROOT) + word.substring(1);
    }



}