package com.denialmc.compassnavigation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AutoUpdater
{
  public CompassNavigation plugin;
  public boolean versionCheck;
  public String versionName;
  public String versionLink;
  public String versionType;
  public String versionGameVersion;
  public String updateFolder;
  public URL url;
  public File file;
  public Thread thread;
  public UpdateResult result = UpdateResult.SUCCESS;
  
  public static enum UpdateResult
  {
    SUCCESS,  NO_UPDATE,  FAIL_DOWNLOAD,  FAIL_DBO,  FAIL_NOVERSION,  FAIL_BADID,  UPDATE_AVAILABLE;
  }
  
  public AutoUpdater(CompassNavigation plugin, File file, boolean versionCheck)
  {
    this.plugin = plugin;
    this.file = file;
    this.versionCheck = versionCheck;
    this.updateFolder = plugin.getServer().getUpdateFolder();
    try
    {
      this.url = new URL("https://api.curseforge.com/servermods/files?projectIds=54751");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result = UpdateResult.FAIL_BADID;
    }
    this.thread = new Thread(new UpdateRunnable());
    this.thread.start();
  }
  
  public UpdateResult getResult()
  {
    waitForThread();
    return this.result;
  }
  
  public String getLatestType()
  {
    waitForThread();
    return this.versionType;
  }
  
  public String getLatestGameVersion()
  {
    waitForThread();
    return this.versionGameVersion;
  }
  
  public String getLatestName()
  {
    waitForThread();
    return this.versionName;
  }
  
  public String getLatestFileLink()
  {
    waitForThread();
    return this.versionLink;
  }
  
  public void download()
  {
    this.versionCheck = false;
    this.thread = new Thread(new UpdateRunnable());
    this.thread.start();
    waitForThread();
    this.versionCheck = true;
  }
  
  public void waitForThread()
  {
    if ((this.thread != null) && (this.thread.isAlive())) {
      try
      {
        this.thread.join();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public void saveFile(File folder, String file, String link)
  {
    if (!folder.exists()) {
      folder.mkdir();
    }
    try
    {
      URL url = new URL(link);
      int length = url.openConnection().getContentLength();
      byte[] data = new byte[1024];
      long downloaded = 0L;
      

      BufferedInputStream input = new BufferedInputStream(url.openStream());
      FileOutputStream output = new FileOutputStream(folder.getAbsolutePath() + File.separator + file);
      
      this.plugin.getLogger().info("About to download a new update: " + this.versionName);
      int count;
      while ((count = input.read(data, 0, 1024)) != -1)
      {
        downloaded += count;
        output.write(data, 0, count);
        int percent = (int)(downloaded * 100L / length);
        if (percent % 10 == 0) {
          this.plugin.getLogger().info("Downloading update: " + percent + "% of " + length + " bytes.");
        }
      }
      this.plugin.getLogger().info("Finished updating.");
      this.result = UpdateResult.SUCCESS;
      if (input != null) {
        input.close();
      }
      if (output != null) {
        output.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result = UpdateResult.FAIL_DOWNLOAD;
    }
  }
  
  public boolean versionCheck(String title)
  {
    if (this.versionCheck)
    {
      String version = this.plugin.getDescription().getVersion();
      if (title.split(" v").length == 2)
      {
        String remoteVersion = title.split(" v")[1].split(" ")[0];
        if (version.equalsIgnoreCase(remoteVersion))
        {
          this.result = UpdateResult.NO_UPDATE;
          return false;
        }
      }
      else
      {
        this.result = UpdateResult.FAIL_NOVERSION;
        return false;
      }
    }
    return true;
  }
  
  public boolean read()
  {
    try
    {
      URLConnection connection = this.url.openConnection();
      connection.setConnectTimeout(5000);
      connection.setDoOutput(true);
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        connection.getInputStream()));
      String response = reader.readLine();
      
      JSONArray array = (JSONArray)JSONValue.parse(response);
      if (array.size() == 0)
      {
        this.result = UpdateResult.FAIL_BADID;
        return false;
      }
      this.versionName = ((String)((JSONObject)array.get(array.size() - 1)).get("name"));
      this.versionLink = ((String)((JSONObject)array.get(array.size() - 1)).get("downloadUrl"));
      this.versionType = ((String)((JSONObject)array.get(array.size() - 1)).get("releaseType"));
      this.versionGameVersion = ((String)((JSONObject)array.get(array.size() - 1)).get("gameVersion"));
      
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.result = UpdateResult.FAIL_DBO;
    }
    return false;
  }
  
  public class UpdateRunnable
    implements Runnable
  {
    public UpdateRunnable() {}
    
    public void run()
    {
      if ((AutoUpdater.this.url != null) && 
        (AutoUpdater.this.read()) && 
        (AutoUpdater.this.versionCheck(AutoUpdater.this.versionName))) {
        if (AutoUpdater.this.versionLink != null)
        {
          String name = AutoUpdater.this.file.getName();
          AutoUpdater.this.saveFile(new File(AutoUpdater.this.plugin.getDataFolder().getParent(), AutoUpdater.this.updateFolder), name, AutoUpdater.this.versionLink);
        }
        else
        {
          AutoUpdater.this.result = AutoUpdater.UpdateResult.UPDATE_AVAILABLE;
        }
      }
    }
  }
}
