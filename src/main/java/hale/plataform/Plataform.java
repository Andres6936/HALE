package main.java.hale.plataform;

import java.io.File;

public interface Plataform
{
    /**
     * Returns the directory containing config and similar files
     *
     * @return the config directory
     */
    String getConfigDirectory();

    /**
     * Returns the directory used to read and write character data.  Default
     * characters and parties are also stored in a separate directory "characters/" (relative
     * to the hale executable)
     *
     * @return the characters directory
     */
    String getCharactersDirectory();

    /**
     * Returns the directory used to read and write party data. Default
     * characters and parties are also stored in a separate directory "characters/" (relative
     * to the hale executable)
     *
     * @return the parties directory
     */
    String getPartiesDirectory();

    /**
     * Returns the directory used to read and write save games
     *
     * @return the save directory
     */
    String getSaveDirectory();

    /**
     * Returns the directory used to write log files
     *
     * @return the log directory
     */
    String getLogDirectory();


    default void createDiretoriesIfNotExist()
    {
        new File(getConfigDirectory()).mkdirs();
        new File(getCharactersDirectory()).mkdirs();
        new File(getPartiesDirectory()).mkdirs();
        new File(getSaveDirectory()).mkdirs();
        new File(getLogDirectory()).mkdirs();
    }
}
