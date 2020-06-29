package main.java.hale.plataform;

import main.java.hale.util.Logger;

public final class WindowsOS implements Plataform
{
    // Final Fields

    private final String CONFIG_DIRECTORY;
    private final String CHARACTER_DIRECTORY;
    private final String PARTIES_DIRECTORY;
    private final String SAVE_DIRECTORY;
    private final String LOG_DIRECTORY;

    // Construct

    public WindowsOS()
    {
        String baseDir = System.getProperty("user.home");

        // Handle the special case, when has Windows 7
        if (System.getProperty("os.name").contains("7")) {
            baseDir += "\\Documents\\My Games\\hale\\";
        } else {
            baseDir += "\\My Documents\\My Games\\hale\\";
        }

        CONFIG_DIRECTORY = baseDir + "\\config\\";
        CHARACTER_DIRECTORY = baseDir + "\\characters\\";
        PARTIES_DIRECTORY = baseDir + "\\parties\\";
        SAVE_DIRECTORY = baseDir + "\\saves\\";
        LOG_DIRECTORY = baseDir + "\\log\\";

        createTimerAccuracyThread();
    }

    // Methods

    @Override
    public String getConfigDirectory()
    {
        return CONFIG_DIRECTORY;
    }

    @Override
    public String getCharactersDirectory()
    {
        return CHARACTER_DIRECTORY;
    }

    @Override
    public String getPartiesDirectory()
    {
        return PARTIES_DIRECTORY;
    }

    @Override
    public String getSaveDirectory()
    {
        return SAVE_DIRECTORY;
    }

    @Override
    public String getLogDirectory()
    {
        return LOG_DIRECTORY;
    }

    private void createTimerAccuracyThread()
    {
        // if we are in Windows OS, start a thread and make it sleep
        // this will ensure reasonable timer accuracy from the OS
        Thread timerAccuracyThread = new Thread(() ->
        {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (Exception e) {
                Logger.appendToErrorLog("Timer accuracy thread error", e);
            }
        });

        timerAccuracyThread.setDaemon(true);
        timerAccuracyThread.start();
    }
}
