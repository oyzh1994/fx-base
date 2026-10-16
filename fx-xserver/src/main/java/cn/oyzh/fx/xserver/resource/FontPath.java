package cn.oyzh.fx.xserver.resource;

import cn.oyzh.common.log.JulLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Where a font name is looked up: the directories a font path names, and the font files in them.
 *
 * <p>An X font path is a list of directories, and the standard says how a name is found in one of
 * them: <b>{@code fonts.alias}</b> maps a short name to a font's full name, <b>{@code fonts.dir}</b>
 * maps a full name to the file that holds it, and the file itself is what a server reads. That is
 * the whole of the resolution, and it is what makes {@code SetFontPath} meaningful - before this the
 * path was stored and reported and nothing else (D3).
 *
 * <p>Reading a name costs a scan of two text files per directory, so both are read once and kept;
 * a font path is the system's own font configuration and does not change under a running server.
 * A name that resolves to nothing is remembered as such, because a client that asks for a font
 * nobody has usually asks again.
 */
public final class FontPath {


    /** The alias and directory files, which are the two names the standard fixes. */
    private static final String ALIASES = "fonts.alias";
    private static final String DIRECTORY = "fonts.dir";

    /** The font names a client asks for and what came of it, misses included. */
    private final Map<String, BitmapFont.Font> opened = new HashMap<>();
    private final java.util.Set<String> misses = new java.util.HashSet<>();

    /** The alias and directory tables, read once per directory. */
    private final Map<String, Map<String, String>> aliases = new HashMap<>();
    private final Map<String, Map<String, String>> directories = new HashMap<>();

    /**
     * The font path as the client last set it, or null before it sets one. Null means the default,
     * which is the server's own directory of fonts plus the built-in tables.
     */
    private List<String> clientPath;

    /** The directory this server keeps its own fonts in, which is also part of the default path. */
    private final String ownDirectory;

    public FontPath(String ownDirectory) {
        this.ownDirectory = ownDirectory;
    }

    /**
     * The directories a font name is resolved in before a client sets a path of its own: this
     * server's own directory, then the ones a machine with X fonts keeps them in.
     *
     * <p>The platform's directories are looked at because that is where a font path normally points
     * and a server that ignored them would answer a Java approximation for fonts sitting right there.
     * Only the ones that exist are used, and this server's own directory comes first so a font
     * dropped into it wins over a system one of the same name. `built-ins` is last and is not a
     * directory: it is the generated tables, which the caller looks at.
     */
    private List<String> defaultPath() {
        List<String> elements = new ArrayList<>();
        elements.add(ownDirectory);
        for (String system : new String[] {"/usr/share/fonts/X11/misc", "/usr/share/fonts/misc",
                "/opt/X11/share/fonts/misc"}) {
            if (Files.isDirectory(Path.of(system))) {
                elements.add(system);
            }
        }
        elements.add("built-ins");
        return List.copyOf(elements);
    }

    /** The path a client set with {@code SetFontPath}, or the default. */
    public List<String> elements() {
        return clientPath != null ? clientPath : defaultPath();
    }

    /** Replaces the path a client set. */
    public void setClientPath(List<String> elements) {
        this.clientPath = List.copyOf(elements);
        this.opened.clear();
        this.misses.clear();
        this.aliases.clear();
        this.directories.clear();
    }

    /**
     * The font a name means, read from the path - or null when nothing on it holds that name. The
     * built-in tables are looked at first by the caller: they are the fonts this server always has,
     * whatever the path says.
     */
    public BitmapFont.Font open(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        BitmapFont.Font cached = opened.get(name);
        if (cached != null || misses.contains(name)) {
            return cached;
        }
        BitmapFont.Font font = resolve(name);
        if (font == null) {
            misses.add(name);
        } else {
            opened.put(name, font);
        }
        return font;
    }

    private BitmapFont.Font resolve(String name) {
        for (String element : elements()) {
            if ("built-ins".equals(element)) {
                continue;                           // the built-in tables are the caller's business
            }
            if (!Files.isDirectory(Path.of(element))) {
                continue;
            }
            String file = fileName(element, name);
            if (file == null) {
                continue;
            }
            Path path = Path.of(element, file);
            try (java.io.InputStream in = Files.newInputStream(path)) {
                BitmapFont.Font font = PcfFont.read(in, name);
                JulLog.debug("opened {} from {}", name, path);
                return font;
            } catch (IOException | RuntimeException e) {
                // A file that is there and cannot be read is worth one line: the alternative is a
                // client being told the font does not exist while it is looking right at it.
                JulLog.warn("could not read the font file " + path + " for " + name, e);
            }
        }
        return null;
    }

    /** The file a name means inside one directory, through the alias and directory tables. */
    private String fileName(String directory, String name) {
        // The tables first: an alias is what a short name *means*, and the misc directories hold
        // both a combined `8x13.pcf.gz` - every charset in one file - and the per-charset
        // `8x13-ISO8859-1.pcf.gz` that fonts.dir lists. Taking the file of that name instead answers
        // the combined font where a client asked for the ISO8859-1 one, and the two differ in
        // exactly the places a client reads: the charset properties, and which glyph each code is.
        // (Measured - this was the shortcut here, and the property list said so.)
        String full = aliases(directory).getOrDefault(name.toLowerCase(Locale.ROOT), name);
        String file = directories(directory).get(full.toLowerCase(Locale.ROOT));
        if (file != null) {
            return file;
        }
        String asFile = asFile(directory, name);
        if (asFile != null) {
            return asFile;
        }
        return full.equals(name) ? null : asFile(directory, full);
    }

    /** The file of that name in the directory, with the extensions a font file has. */
    private static String asFile(String directory, String name) {
        for (String suffix : new String[] {"", ".pcf", ".pcf.gz", ".bdf"}) {
            if (Files.exists(Path.of(directory, name + suffix))) {
                return name + suffix;
            }
        }
        return null;
    }

    /** {@code fonts.alias}: a short name and the full name it stands for, one per line. */
    private Map<String, String> aliases(String directory) {
        return aliases.computeIfAbsent(directory, key -> {
            Map<String, String> table = new HashMap<>();
            for (String line : lines(key, ALIASES)) {
                String[] fields = line.split("\\s+", 2);
                if (fields.length == 2) {
                    table.put(fields[0].toLowerCase(Locale.ROOT), unquote(fields[1].trim()));
                }
            }
            return table;
        });
    }

    /** {@code fonts.dir}: a file name and the full name of the font in it, one per line. */
    private Map<String, String> directories(String directory) {
        return directories.computeIfAbsent(directory, key -> {
            Map<String, String> table = new HashMap<>();
            for (String line : lines(key, DIRECTORY)) {
                String[] fields = line.split("\\s+", 2);
                // The first line is a count and nothing else, and that is the only line to skip:
                // a *file name* in a font directory usually begins with a digit (`8x13-ISO8859-1.pcf.gz`
                // is one), so "does this line start with a number" throws the table away rather than
                // the count.
                if (line.matches("\\d+")) {
                    continue;
                }
                if (fields.length == 2 && !fields[0].isEmpty()) {
                    table.put(unquote(fields[1].trim()).toLowerCase(Locale.ROOT), fields[0]);
                }
            }
            return table;
        });
    }

    private static String unquote(String value) {
        return value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1) : value;
    }

    /** The lines of a file in a font directory, empty when it is not there. */
    private static List<String> lines(String directory, String file) {
        Path path = Path.of(directory, file);
        if (!Files.isReadable(path)) {
            return List.of();
        }
        try {
            List<String> lines = new ArrayList<>();
            for (String line : Files.readAllLines(path, StandardCharsets.ISO_8859_1)) {
                String trimmed = line.strip();
                if (!trimmed.isEmpty() && !trimmed.startsWith("!")) {
                    lines.add(trimmed);
                }
            }
            return lines;
        } catch (IOException e) {
            JulLog.warn("could not read {}", path, e);
            return List.of();
        }
    }
}
