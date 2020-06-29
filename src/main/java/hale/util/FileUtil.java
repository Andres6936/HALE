/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package main.java.hale.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Contains static utility methods for working with files
 *
 * @author Jared Stephen
 */

public class FileUtil
{

    /**
     * Extracts the Zip file at the specified location to the destination directory
     *
     * @param destination the path to the destination top level directory
     * @param zipFile     the path to the zip file to extract
     * @throws IOException any of the usual file exceptions
     */

    public static void extractZipFile(String destination, String zipFile) throws IOException
    {
        int bufferSize = 2048;
        byte[] buffer = new byte[bufferSize];

        new File(destination).mkdir();

        ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));

        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                File fout = new File(destination + "/" + entry.getName());
                fout.mkdirs();
            } else {
                int count = 0;

                String filePath = destination + "/" + entry.getName();

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath), bufferSize);

                while ((count = in.read(buffer, 0, bufferSize)) > 0) {
                    out.write(buffer, 0, count);
                }
                out.flush();
                out.close();
            }
        }

        in.close();
    }

    /**
     * Saves the contents of all files in the specified source directoy and all subdirectories
     * and files recursively to a zipfile at the specified path
     *
     * @param source  the top level source directory
     * @param zipFile the path to the new zip file
     * @throws IOException any of the usual I/O exceptions
     */

    public static void saveToZipFile(String source, String zipFile) throws IOException
    {
        int bufferSize = 2048;
        byte[] buffer = new byte[bufferSize];

        File topLevel = new File(source);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

        List<File> files = FileUtil.getFilesAndDirectories(topLevel);

        for (File file : files) {
            String filePath = FileUtil.getRelativePath(topLevel, file);

            if (file.isDirectory()) {
                out.putNextEntry(new ZipEntry(filePath + "/"));
                out.closeEntry();
            } else {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

                out.putNextEntry(new ZipEntry(filePath));

                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
                out.flush();
                out.closeEntry();
                in.close();
            }
        }

        out.close();
    }

    /**
     * Reads the contents of the file at the specified path into a String
     *
     * @param path the path to the file
     * @return the contents of the file as a String
     * @throws IOException
     */

    public static String readFileAsString(String path) throws IOException
    {
        File f = new File(path);

        if (!f.exists()) return null;

        byte[] buffer = new byte[(int)f.length()];
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(path));
            bis.read(buffer);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Logger.appendToErrorLog("Error closing file " + path, e);
                }
            }
        }

        return new String(buffer);
    }

    /**
     * Writes the contents of the specified String to the specified file
     *
     * @param file     the location of the file to write the output to
     * @param contents the contents of the file to be written
     * @throws IOException any of the usual I/O exceptions
     */

    public static void writeStringToFile(File file, String contents) throws IOException
    {
        PrintStream out = new PrintStream(file);

        out.print(contents);
        out.close();
    }

    /**
     * Creates an exact copy of the file at from at the file to
     *
     * @param from the source file
     * @param to   the destination file
     * @throws IOException any of the usual I/O exceptions
     */

    public static void copyFile(File from, File to) throws IOException
    {
        FileReader in = new FileReader(from);
        FileWriter out = new FileWriter(to);

        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();
    }

    private static List<String> getPathList(File f)
    {
        List<String> list = new ArrayList<String>();
        if (f == null) return list;

        try {
            File curFile = f.getCanonicalFile();
            while (curFile != null) {
                list.add(curFile.getName());
                curFile = curFile.getParentFile();
            }
        } catch (Exception e) {
            Logger.appendToErrorLog("Error computing path for " + f.getName(), e);
        }

        return list;
    }

    private static String matchPathLists(List<String> from, List<String> to)
    {
        String s = "";

        int i = from.size() - 1;
        int j = to.size() - 1;

        while ((i >= 0) && (j >= 0) && (from.get(i).equals(to.get(j)))) {
            i--;
            j--;
        }

        for (; i >= 0; i--) {
            s += "../";
        }

        for (; j >= 1; j--) {
            s += to.get(j) + "/";
        }

        if (j != -1) {
            // if j == -1, paths are equal
            s += to.get(j);
        }

        return s;
    }

    /**
     * Returns the relative path, with folders separated by "/" from the "from" file to
     * the "to" file
     *
     * @param from the starting file or directory
     * @param to   the ending file or directory
     * @return the relative path between the 2 files or directories
     */

    public static String getRelativePath(File from, File to)
    {
        return matchPathLists(getPathList(from), getPathList(to));
    }

    /**
     * Returns the relative path, with folders separated by "/" from the file at "from" to
     * the file at "to"
     *
     * @param from the path for the starting file or directory
     * @param to   the path for the ending file or directory
     * @return the relative path between the 2 files or directories
     */

    public static String getRelativePath(String from, String to)
    {
        return matchPathLists(getPathList(new File(from)), getPathList(new File(to)));
    }

    /**
     * Gets the first half of the MD5Sum for the file at the specified location
     *
     * @param file the file at the location
     * @return the MD5Sum for the specified file
     */

    public static String getHalfMD5Sum(File file)
    {
        String md5 = getMD5Sum(file);

        if (md5.length() == 24) {
            return md5.substring(0, 12);
        } else {
            return md5;
        }
    }

    /**
     * Gets the MD5Sum for the file at the specified location
     *
     * @param file the file at the location
     * @return the MD5Sum for the specified file
     */

    public static String getMD5Sum(File file)
    {
        byte[] b = null;
        try {
            b = createSum(file);
        } catch (Exception e) {
            System.err.println("Error creating MD5Sum for versioning.  Exiting.");
            System.exit(1);
        }

        return getHex(b);
    }

    private static byte[] createSum(File file) throws IOException, NoSuchAlgorithmException
    {
        if (!file.exists()) return null;

        FileInputStream in = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest digest = MessageDigest.getInstance("MD5");

        while (true) {
            int numRead = in.read(buffer);
            if (numRead == -1) break;

            digest.update(buffer, 0, numRead);
        }

        in.close();

        return digest.digest();
    }

    private static final String HEXES = "0123456789ABCDEF";

    private static String getHex(byte[] raw)
    {
        if (raw == null) {
            return "0";
        }

        StringBuilder hex = new StringBuilder(2 * raw.length);

        for (byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4));
            hex.append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }

    /**
     * Gets a list of all files recursively contained in the specified directory.  The
     * returned list will not contain any directories.
     *
     * @param startingDir
     * @return the list of all files recursively contained in the specified directory
     */

    public static List<File> getFiles(File startingDir)
    {
        List<File> result = new ArrayList<File>();

        File[] subFiles = startingDir.listFiles();

        if (subFiles == null) return result;

        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].getName().equals(".svn")) continue;

            if (subFiles[i].isFile()) {
                result.add(subFiles[i]);
            } else {
                result.addAll(getFiles(subFiles[i]));
            }
        }

        return result;
    }

    /**
     * Gets a list of all files and all directories recursively contained in the
     * specified directory
     *
     * @param startingDir
     * @return the list of all files and all directories recursively contained
     * in the specified directory
     */

    public static List<File> getFilesAndDirectories(File startingDir)
    {
        List<File> result = new ArrayList<File>();

        File[] subFiles = startingDir.listFiles();

        if (subFiles == null) return result;

        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].getName().equals(".svn")) continue;

            result.add(subFiles[i]);
            if (subFiles[i].isDirectory()) {
                result.addAll(getFilesAndDirectories(subFiles[i]));
            }
        }

        return result;
    }
}
