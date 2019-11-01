package net.klubblan.points;

public class ConfigSaver implements Runnable
{
    private final Points plugin;

    public ConfigSaver(final Points plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        this.plugin.saveConfig();
    }
}
