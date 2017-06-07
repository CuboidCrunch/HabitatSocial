package com.gmail.Xeiotos.HabitatSocial.Managers;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
 
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
 
/**
* You're free to modify and redistribute this as long as some credit is given to me
* @author Foodyling
*/
public class ConfigManager {
    public static class ConfigPath {
        private final String configName;
        private final String resourcePath;
        private final String outputPath;
 
        public ConfigPath(String configName, String resourcePath, String outputPath) {
            this.configName = configName;
            this.resourcePath = resourcePath;
            this.outputPath = outputPath;
        }
 
        public String getName() {
            return configName;
        }
 
        public String getResourcePath() {
            return resourcePath;
        }
 
        public String getOutputPath() {
            return outputPath;
        }
    }
    public static class Configuration {
        private final File configFile;
        private FileConfiguration config;
 
        public Configuration (File configFile, FileConfiguration config) {
            this.configFile = configFile;
            this.config = config;
        }
 
        public FileConfiguration getConfig() {
            return config;
        }
 
        public File getFile() {
            return configFile;
        }
 
        public boolean reloadConfig() {
            try {
                config = YamlConfiguration.loadConfiguration(configFile);
                return true;
            } catch (Exception erorr) {
                return false;
            }
        }
 
        public boolean saveConfig() {
            if (configFile != null) {
                try {
                    config.save(configFile);
                    return true;
                } catch (Throwable error) {
           
                }
            }
            return false;
        }
    }
 
    private Plugin caller;
    private File configFolder;
    private final TreeMap<String, ConfigManager.Configuration> configurations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
 
    /**
    * Create a new instance of a ConfigManager for a specific plugin
    * @param pluginInstance Plugin that calls the ConfigManager
    */
    public ConfigManager(Plugin pluginInstance) {
        if (pluginInstance != null) {
            this.caller = pluginInstance;
            this.configFolder = pluginInstance.getDataFolder();
            if (!configFolder.exists()) {
                configFolder.mkdirs();
            }
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Configuration Manager failed to initialize");
        }
    }
 
    /**
    * Load all configuration files
    * @param configPaths Collection of configuration files to load
    */
    public void loadConfigFiles(ConfigManager.ConfigPath... configPaths) {
        for (ConfigManager.ConfigPath path : configPaths) {
            try {
                String resourcePath = path.getResourcePath(),
                        outputPath = path.getOutputPath();
                FileConfiguration config = null;
                File configFile = null;
                if (outputPath != null) {
                    configFile = new File(configFolder, outputPath);
                    if (configFile.exists()) {
                        config = YamlConfiguration.loadConfiguration(configFile);
                    } else {
                        if (resourcePath == null) {
                            configFile.mkdirs();
                            configFile.createNewFile();
                            config = YamlConfiguration.loadConfiguration(configFile);
                        } else {
                            InputStream inputStream = caller.getResource(resourcePath);
                            if (inputStream != null) {
                                config = YamlConfiguration.loadConfiguration(inputStream);
                                config.save(configFile);
                            }
                        }
                    }
                } else {
                    InputStream inputStream = caller.getResource(resourcePath);
                    if (inputStream != null) {
                        config = YamlConfiguration.loadConfiguration(inputStream);
                    }
                }
                if (resourcePath != null && outputPath != null) {
                    try {
                        config.setDefaults(YamlConfiguration.loadConfiguration(caller.getResource(resourcePath)));
                    } catch (Throwable error) {
                        caller.getLogger().log(Level.SEVERE, "Failed to set defaults of config: {0}", path.getName());
                    }
                }
                if (config != null) {
                    configurations.put(path.getName(), new ConfigManager.Configuration(configFile, config));
                } else {
                    caller.getLogger().log(Level.SEVERE, "Error loading configuration: {0}", path.getName());
                }
            } catch (Throwable error) {
                caller.getLogger().log(Level.SEVERE, "Error loading configuration: {0}", path.getName());
            }
        }
    }
 
    /**
    *
    * @param configName Configuration name to save
    * @return Whether configuration saved successfully
    */
    public boolean saveConfig(String configName, boolean logError) {
        if (configurations.containsKey(configName)) {
            ConfigManager.Configuration config = configurations.get(configName);
            if (config != null) {
                boolean saved = config.saveConfig();
                if (logError && !saved) {
                    caller.getLogger().log(Level.SEVERE, "Failed to save configuration: {0}", configName);
                }
                return saved;
            }
        }
        return false;
    }
 
    public boolean saveConfig(String configName) {
        return saveConfig(configName, false);
    }
 
    /**
    * Saves all configuration files
    */
    public void saveAllConfigs(boolean logError) {
        for (String configName : configurations.keySet()) {
            if (logError && !saveConfig(configName, logError)) {
                caller.getLogger().log(Level.SEVERE, "Failed to save configuration: {0}", configName);
            }
        }
    }
 
    /**
    *
    * @param configName
    * @return Whether configuration was reloaded succesfully
    */
    public boolean reloadConfig(String configName) {
        if (configurations.containsKey(configName)) {
            ConfigManager.Configuration config = configurations.get(configName);
            if (config != null) {
                return config.reloadConfig();
            }
        }
        return false;
    }
 
    /**
    * Reloads all registered configuration manager
    */
    public void reloadAllConfigs() {
        for (String configName : configurations.keySet()) {
            reloadConfig(configName);
        }
    }
    
    /**
     *
     * @param configName Config to unload
     */
    public void unloadConfig(String configName) {
        configurations.remove(configName);
    }
    
    /**
     * Unloads all configuration files in memory
     */
    public void unloadAllConfigs() {
        for (Iterator<ConfigManager.Configuration> iterator = configurations.values().iterator(); iterator.hasNext();) {
            iterator.next();
            iterator.remove();
        }
    }
 
 
    /**
    *
    * @param configName Configuration name
    * @return Configuration interface, returns null if not found
    */
    public ConfigManager.Configuration getConfig(String configName) {
        if (configurations.containsKey(configName)) {
            return configurations.get(configName);
        } else {
            return null;
        }
    }
 
    /**
    *
    * @param configName Configuration name
    * @return FileConfiguration, returns null if not found
    */
    public FileConfiguration getFileConfig(String configName) {
        ConfigManager.Configuration config = getConfig(configName);
        if (config != null) {
            return config.getConfig();
        } else {
            return null;
        }
    }
}