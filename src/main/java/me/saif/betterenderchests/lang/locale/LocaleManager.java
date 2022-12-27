package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.lang.MessageKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LocaleManager {

    private VariableEnderChests plugin;
    private final Map<String, Locale> localeMap = new HashMap<>();
    private final String defaultLocale = "en_us";

    public LocaleManager(VariableEnderChests plugin) {
        this.plugin = plugin;
        try {
            this.loadLocales();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Locale getLocale(String name) {
        return localeMap.get(name);
    }

    private void loadLocales() throws IOException, InvalidConfigurationException {
        List<String> inJar = findLocaleResources();
        for (String s : inJar) {
            this.saveResource(s, true);
        }

        FileConfiguration config = new YamlConfiguration();
        config.load(new File(this.plugin.getDataFolder(), inJar.get(0)));
        for (MessageKey value : MessageKey.values()) {
            config.set(value.getPath(), value.getDefault());
        }

        config.save(new File(this.plugin.getDataFolder(), inJar.get(0)));
    }

    private List<String> findLocaleResources(){
        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();

        try {
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry ze = null;

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
