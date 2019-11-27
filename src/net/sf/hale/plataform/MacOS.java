package net.sf.hale.plataform;

public final class MacOS implements Plataform
{
    // Final Fields

    private final String CONFIG_DIRECTORY;
    private final String CHARACTER_DIRECTORY;
    private final String PARTIES_DIRECTORY;
    private final String SAVE_DIRECTORY;
    private final String LOG_DIRECTORY;

    // Construct

    public MacOS()
    {
        // use XDG compliant data and configuration directories
        String xdgDataHome = System.getenv( "XDG_DATA_HOME" );
        String xdgConfigHome = System.getenv( "XDG_CONFIG_HOME" );

        if ( xdgDataHome == null || xdgDataHome.length( ) == 0 )
        {
            // fallback to XDG default
            xdgDataHome = System.getProperty( "user.home" ) + "/.local/share";
        }

        if ( xdgConfigHome == null || xdgConfigHome.length( ) == 0 )
        {
            // fallback to XDG default
            xdgConfigHome = System.getProperty( "user.home" ) + "/.config";
        }

        xdgDataHome = xdgDataHome + "/hale/";
        xdgConfigHome = xdgConfigHome + "/hale/";

        CONFIG_DIRECTORY = xdgConfigHome;
        CHARACTER_DIRECTORY = xdgDataHome + "characters/";
        PARTIES_DIRECTORY = xdgDataHome + "parties/";
        SAVE_DIRECTORY = xdgDataHome + "saves/";
        LOG_DIRECTORY = xdgDataHome + "log/";
    }

    // Methods

    @Override
    public String getConfigDirectory( )
    {
        return CONFIG_DIRECTORY;
    }

    @Override
    public String getCharactersDirectory( )
    {
        return CHARACTER_DIRECTORY;
    }

    @Override
    public String getPartiesDirectory( )
    {
        return PARTIES_DIRECTORY;
    }

    @Override
    public String getSaveDirectory( )
    {
        return SAVE_DIRECTORY;
    }

    @Override
    public String getLogDirectory( )
    {
        return LOG_DIRECTORY;
    }
}
