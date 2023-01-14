package me.saif.betterenderchests.lang.locale;

import com.google.common.collect.Sets;
import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.lang.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LocaleManager {

    private VariableEnderChests plugin;
    private final Map<String, Locale> localeMap = new ConcurrentHashMap<>();
    private Locale defaultLocale;

    public LocaleManager(VariableEnderChests plugin) {
        this.plugin = plugin;
        this.loadLocales();
        this.defaultLocale = this.localeMap.get(plugin.getConfig().getString("default-locale", "en_us").toLowerCase());

        if (this.defaultLocale == null) {
            defaultLocale = this.localeMap.values().stream().findFirst().orElse(new Locale("en", Sets.newHashSet("us"), new HashMap<>()));
        }
    }

    public Locale getLocale(String name) {
        return localeMap.get(name);
    }

    private void loadLocales() {
        List<String> inJar = findLocaleResources();
        for (String s : inJar) {
            this.saveResource(s, false);
        }

        File langFolder = new File(plugin.getDataFolder(), "lang");

        for (File localeFile : langFolder.listFiles()) {
            if (localeFile.isDirectory())
                continue;

            if (!localeFile.getName().endsWith(".yml"))
                continue;

            FileConfiguration localeConfig = new YamlConfiguration();

            try {
                localeConfig.load(localeFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            String lang = localeConfig.getString("lang");
            List<String> countries = localeConfig.getStringList("countries");
            Map<MessageKey, String[]> messages = new HashMap<>();

            boolean madeChanges = false;

            for (MessageKey key : MessageKey.values()) {
                List<String> lines = localeConfig.getStringList(key.getPath());
                if (lines.size() != 0) {
                    messages.put(key, lines.toArray(new String[0]));
                } else {
                    String single = localeConfig.getString(key.getPath());
                    if (single == null) {
                        //we set it in the file
                        madeChanges = true;
                        plugin.getLogger().info(key.getPath() +" was not found in " + localeFile.getName() + ". Setting defaults.");
                        String[] def = key.getDefault();
                        if (def.length == 1) {
                            localeConfig.set(key.getPath(), def[0]);
                        } else {
                            localeConfig.set(key.getPath(), Arrays.asList(def));
                        }
                    }

                    messages.put(key, new String[]{single});
                }
            }

            if (madeChanges) {
                try {
                    localeConfig.save(localeFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Locale locale = new Locale(lang, new HashSet<>(countries), messages);

            for (String localeLocale : locale.getLocales()) {
                this.localeMap.put(localeLocale.toLowerCase(), locale);
            }
        }
    }

    private List<String> findLocaleResources() {
        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();

        try {
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry ze;

                while ((ze = zip.getNextEntry()) != null) {
                    String entryName = ze.getName();
                    if (entryName.startsWith("lang/") && entryName.endsWith(".yml")) {
                        list.add(entryName);
                    }
                }
            }

            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public Locale getOrDefault(String localeString) {
        return this.localeMap.getOrDefault(localeString.toLowerCase(), defaultLocale);
    }

    private void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + plugin.getPluginFile());
        }

        File outFile = new File(plugin.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

}
