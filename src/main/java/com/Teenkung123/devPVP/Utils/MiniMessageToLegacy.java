package com.Teenkung123.devPVP.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageToLegacy {
    private static final Map<String, String> COLOR_MAP = new LinkedHashMap<>();
    static {
        COLOR_MAP.put("black", "§0");
        COLOR_MAP.put("dark_blue", "§1");
        COLOR_MAP.put("dark_green", "§2");
        COLOR_MAP.put("dark_aqua", "§3");
        COLOR_MAP.put("dark_red", "§4");
        COLOR_MAP.put("dark_purple", "§5");
        COLOR_MAP.put("gold", "§6");
        COLOR_MAP.put("gray", "§7");
        COLOR_MAP.put("grey", "§7");
        COLOR_MAP.put("dark_gray", "§8");
        COLOR_MAP.put("dark_grey", "§8");
        COLOR_MAP.put("blue", "§9");
        COLOR_MAP.put("green", "§a");
        COLOR_MAP.put("aqua", "§b");
        COLOR_MAP.put("red", "§c");
        COLOR_MAP.put("light_purple", "§d");
        COLOR_MAP.put("purple", "§d");
        COLOR_MAP.put("yellow", "§e");
        COLOR_MAP.put("white", "§f");
    }

    private static final Map<String, String> DECORATION_MAP = new LinkedHashMap<>();
    static {
        DECORATION_MAP.put("bold", "§l");
        DECORATION_MAP.put("b", "§l");

        DECORATION_MAP.put("italic", "§o");
        DECORATION_MAP.put("italics", "§o");
        DECORATION_MAP.put("i", "§o");
        DECORATION_MAP.put("em", "§o");

        DECORATION_MAP.put("underlined", "§n");
        DECORATION_MAP.put("underline", "§n");
        DECORATION_MAP.put("u", "§n");

        DECORATION_MAP.put("strikethrough", "§m");
        DECORATION_MAP.put("strike", "§m");
        DECORATION_MAP.put("st", "§m");

        DECORATION_MAP.put("obfuscated", "§k");
        DECORATION_MAP.put("obf", "§k");
        DECORATION_MAP.put("magic", "§k");
    }

    private static final Pattern HEX_PATTERN_SIMPLE = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern COLOR_TAG_PATTERN = Pattern.compile("<(color|colour|c):([^>]+)>");
    private static final Pattern RESET_PATTERN = Pattern.compile("<reset>");

    // Gradient pattern: <gradient:color1:color2:...> ... </gradient>
    private static final Pattern GRADIENT_OPEN_PATTERN = Pattern.compile("<gradient([^>]*)>");
    private static final Pattern GRADIENT_CLOSE_PATTERN = Pattern.compile("</gradient>");

    // Another gradient form: <#rrggbb> ... </#rrggbb>
    private static final Pattern HEX_GRADIENT_OPEN_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    // We'll dynamically find the closing </#rrggbb> match for the open

    /**
     * Converts a MiniMessage string into a legacy-formatted string.
     */
    public static String convert(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Handle gradients first, as they involve extracting sections of text
        input = processGradientTags(input);
        input = processHexGradientTags(input);

        // Convert simple hex colors
        input = convertSimpleHex(input);

        // Convert <color:...> or <colour:...> tags
        input = convertColorTags(input);

        // Convert named colors
        input = convertNamedColors(input);

        // Convert decorations
        input = convertDecorations(input);

        // Convert <reset>
        input = RESET_PATTERN.matcher(input).replaceAll("§r");

        // Remove unknown tags
        input = removeUnknownTags(input);

        return input;
    }

    private static String convertSimpleHex(String input) {
        Matcher matcher = HEX_PATTERN_SIMPLE.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, hexToLegacy(hex));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String convertColorTags(String input) {
        Matcher matcher = COLOR_TAG_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String colorValue = matcher.group(2).toLowerCase();
            String replacement;
            if (colorValue.startsWith("#") && colorValue.length() == 7) {
                // Hex color
                replacement = hexToLegacy(colorValue.substring(1));
            } else {
                // Named color
                replacement = COLOR_MAP.getOrDefault(colorValue.replaceAll(" ", "_"), "");
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String convertNamedColors(String input) {
        for (Map.Entry<String, String> entry : COLOR_MAP.entrySet()) {
            String tag = "<" + entry.getKey() + ">";
            input = input.replace(tag, entry.getValue());
        }
        return input;
    }

    private static String convertDecorations(String input) {
        // Replace known decoration tags
        for (Map.Entry<String, String> entry : DECORATION_MAP.entrySet()) {
            String tag = "<" + entry.getKey() + ">";
            input = input.replace(tag, entry.getValue());
        }

        // Disable patterns like <something:false> or <!something>
        Pattern disablePattern = Pattern.compile("<(![a-z_]+|[a-z_]+:false)>");
        input = disablePattern.matcher(input).replaceAll("§r");

        return input;
    }

    private static String removeUnknownTags(String input) {
        return input.replaceAll("<.*?>", "");
    }

    private static String processGradientTags(String input) {
        // We need to handle <gradient:color1:color2:...> ... </gradient>
        // We'll do this in a loop until no more gradients are found
        while (true) {
            Matcher openMatcher = GRADIENT_OPEN_PATTERN.matcher(input);
            if (!openMatcher.find()) {
                break;
            }

            int startIndex = openMatcher.end();
            // find the matching </gradient>
            Matcher closeMatcher = GRADIENT_CLOSE_PATTERN.matcher(input);
            if (!closeMatcher.find(startIndex)) {
                // no matching closing tag, break
                break;
            }
            int endIndex = closeMatcher.start();

            // Extract the inside text
            String inside = input.substring(startIndex, endIndex);

            // Parse colors
            String colorArgs = openMatcher.group(1); // something like :red:blue or :#5e4fa2:#f79459
            List<Integer> colors = parseGradientColors(colorArgs);

            // Apply gradient
            String gradiented = applyGradient(inside, colors);

            // Replace the entire segment <gradient...> ... </gradient> with gradiented text
            input = input.substring(0, openMatcher.start()) + gradiented + input.substring(closeMatcher.end());
        }
        return input;
    }

    private static String processHexGradientTags(String input) {
        // handle <#000000> ... </#ffffff> form
        // We'll find the first <#rrggbb>, then find a matching </#rrggbb> and interpolate
        while (true) {
            Matcher openMatcher = HEX_GRADIENT_OPEN_PATTERN.matcher(input);
            if (!openMatcher.find()) {
                break;
            }
            String startHex = openMatcher.group(1);
            int startIndex = openMatcher.end();

            // Find a matching closing tag </#xxxxxx>
            Pattern closePattern = Pattern.compile("</#" + "([A-Fa-f0-9]{6})" + ">");
            Matcher closeMatcher = closePattern.matcher(input);
            if (!closeMatcher.find(startIndex)) {
                // no matching closing tag
                break;
            }

            String endHex = closeMatcher.group(1);
            int endIndex = closeMatcher.start();

            String inside = input.substring(startIndex, endIndex);

            List<Integer> colors = new ArrayList<>();
            colors.add(parseColor(startHex));
            colors.add(parseColor(endHex));

            String gradiented = applyGradient(inside, colors);

            input = input.substring(0, openMatcher.start()) + gradiented + input.substring(closeMatcher.end());
        }
        return input;
    }

    // Convert #RRGGBB to §x§r§r§g§g§b§b
    private static String hexToLegacy(String hex) {
        StringBuilder replacement = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            replacement.append('§').append(Character.toLowerCase(c));
        }
        return replacement.toString();
    }

    private static List<Integer> parseGradientColors(String args) {
        // args like :red:blue or :#123456:#ffffff
        // Split by ':', skip empty entries
        List<Integer> colors = new ArrayList<>();
        String[] parts = args.split(":");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            // Could be a named color or hex color
            if (part.startsWith("#") && part.length() == 7) {
                colors.add(parseColor(part.substring(1)));
            } else {
                // Named color
                String named = part.toLowerCase().replaceAll(" ", "_");
                if (COLOR_MAP.containsKey(named)) {
                    // Convert named color's code back to RGB if possible
                    // Named colors are known Minecraft colors. We can define a map for them or just guess.
                    // For simplicity, we'll hardcode a few known named colors to RGB.
                    colors.add(namedColorToRGB(named));
                } else {
                    // fallback black
                    colors.add(parseColor("000000"));
                }
            }
        }

        if (colors.isEmpty()) {
            colors.add(parseColor("ffffff")); // default white if no colors found
        }
        return colors;
    }

    private static int namedColorToRGB(String named) {
        // You could define a map from named colors to RGB. We'll do a simple partial map:
        // This is a small subset; you can expand as needed.
        return switch (named) {
            case "black" -> 0x000000;
            case "dark_blue" -> 0x0000AA;
            case "dark_green" -> 0x00AA00;
            case "dark_aqua" -> 0x00AAAA;
            case "dark_red" -> 0xAA0000;
            case "dark_purple" -> 0xAA00AA;
            case "gold" -> 0xFFAA00;
            case "gray", "grey" -> 0xAAAAAA;
            case "dark_gray", "dark_grey" -> 0x555555;
            case "blue" -> 0x5555FF;
            case "green" -> 0x55FF55;
            case "aqua" -> 0x55FFFF;
            case "red" -> 0xFF5555;
            case "light_purple", "purple" -> 0xFF55FF;
            case "yellow" -> 0xFFFF55;
            default -> 0xFFFFFF;
        };
    }

    private static int parseColor(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private static String applyGradient(String text, List<Integer> colors) {
        if (text.isEmpty() || colors.isEmpty()) return text;

        int length = text.length();

        // If only one color, just apply that color to the whole text
        if (colors.size() == 1) {
            String colorCode = hexToLegacy(toHexString(colors.getFirst()));
            return colorCode + text;
        }

        // Multiple colors
        // Divide the text among the (colors.size()-1) segments
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            float progress = (float)i / (length - 1);
            int rgb = interpolateColors(colors, progress);
            sb.append(hexToLegacy(toHexString(rgb))).append(text.charAt(i));
        }

        return sb.toString();
    }

    private static int interpolateColors(List<Integer> colors, float progress) {
        // If we have multiple colors: for n colors, we have n-1 segments
        // find which segment progress falls into
        if (colors.size() == 1) return colors.getFirst();

        int segmentCount = colors.size() - 1;
        float segmentLength = 1.0f / segmentCount;
        int segmentIndex = Math.min((int)(progress / segmentLength), segmentCount - 1);

        float segmentStart = segmentIndex * segmentLength;
        float segmentEnd = (segmentIndex + 1) * segmentLength;
        float segmentProgress = (progress - segmentStart) / (segmentEnd - segmentStart);

        int startColor = colors.get(segmentIndex);
        int endColor = colors.get(segmentIndex + 1);

        int r1 = (startColor >> 16) & 0xFF;
        int g1 = (startColor >> 8) & 0xFF;
        int b1 = startColor & 0xFF;

        int r2 = (endColor >> 16) & 0xFF;
        int g2 = (endColor >> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int r = (int)(r1 + (r2 - r1) * segmentProgress);
        int g = (int)(g1 + (g2 - g1) * segmentProgress);
        int b = (int)(b1 + (b2 - b1) * segmentProgress);

        return (r << 16) | (g << 8) | b;
    }

    private static String toHexString(int rgb) {
        return String.format("%06x", rgb);
    }
}