package com.spun.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.spun.util.ArrayUtils;
import com.spun.util.Asserts;
import com.spun.util.ObjectUtils;

/**
 * A static class of convenience functions for Files
 **/
public class FileUtils
{
  /***********************************************************************/
  /**
   * @see java.io.File.createTempFile(String,String)
   **/
  public static File createTempDirectory() throws IOException
  {
    File tempFile = File.createTempFile("TEMP", null);
    tempFile.delete();
    tempFile.mkdirs();
    return tempFile;
  }
  /***********************************************************************/
  public static void deleteDirectory(File directory) throws IOException
  {
    // delete all directory
    File directories[] = directory.listFiles(new SimpleDirectoryFilter());
    for (int i = 0; i < directories.length; i++)
    {
      deleteDirectory(directories[i]);
    }
    // Delete all Files.
    File files[] = directory.listFiles(new SimpleFileFilter());
    for (int i = 0; i < files.length; i++)
    {
      files[i].delete();
    }
    // delete self.
    directory.delete();
  }
  public static String readFromClassPath(Class<?> clazz, String string)
  {
    final InputStream resourceAsStream = clazz.getResourceAsStream(string);
    if (resourceAsStream == null)
    {
      String message = String.format("Could not find %s from %s", string, clazz.getName());
      throw new RuntimeException(message);
    }
    String resource = FileUtils.readStream(resourceAsStream);
    return resource;
  }
  /***********************************************************************/
  public static File[] getRecursiveFileList(File directory)
  {
    return getRecursiveFileList(directory, new SimpleFileFilter());
  }
  /***********************************************************************/
  public static File[] getRecursiveFileList(File directory, FileFilter filter)
  {
    ArrayList<File> list = new ArrayList<File>();
    if (!directory.isDirectory()) { throw new Error("File is not a directory: " + directory.getName()); }
    File directories[] = directory.listFiles(new SimpleDirectoryFilter());
    for (int i = 0; i < directories.length; i++)
    {
      ArrayUtils.addArray(list, getRecursiveFileList(directories[i], filter));
    }
    File files[] = directory.listFiles(filter);
    ArrayUtils.addArray(list, files);
    return list.toArray(new File[list.size()]);
  }
  /***********************************************************************/
  public static void copyFile(File in, File out)
  {
    try
    {
      out.getParentFile().mkdirs();
      try (FileInputStream sIn = new FileInputStream(in))
      {
        try (FileChannel inChannel = sIn.getChannel())
        {
          try (FileOutputStream sOut = new FileOutputStream(out))
          {
            try (FileChannel outChannel = sOut.getChannel())
            {
              outChannel.transferFrom(inChannel, 0, inChannel.size());
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      ObjectUtils.throwAsError(e);
    }
  }
  public static void copyStream(InputStream in, OutputStream out)
  {
    try
    {
      byte[] buf = new byte[1024];
      int i = 0;
      while ((i = in.read(buf)) != -1)
      {
        out.write(buf, 0, i);
      }
      in.close();
      out.close();
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /***********************************************************************/
  public static void redirectInputToFile(String fileName, InputStream in)
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(new File(fileName), false);
      copyStream(in, fos);
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /***********************************************************************/
  public static void copyFileToDirectory(String file, File tempDir)
  {
    try
    {
      File in = new File(file);
      File out = new File(tempDir, in.getName());
      copyFile(in, out);
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /***********************************************************************/
  public static void writeFile(File file, String text)
  {
    try
    {
      Asserts.assertNotNull("Writing to file: " + file, text);
      file.getCanonicalFile().getParentFile().mkdirs();
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(text);
      out.close();
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /***********************************************************************/
  public static void writeFileQuietly(File file, String text)
  {
    writeFile(file, text);
  }
  public static void writeFile(File file, CharSequence data)
  {
    try
    {
      Asserts.assertNotNull("Writing to file: " + file, data);
      file.getCanonicalFile().getParentFile().mkdirs();
      DataOutputStream writer = new DataOutputStream(new FileOutputStream(file));
      for (int i = 0; i < data.length(); i++)
      {
        writer.write(data.charAt(i));
      }
      writer.close();
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  public static void writeFile(File file, InputStream data)
  {
    try
    {
      Asserts.assertNotNull("Writing to file: " + file, data);
      file.getCanonicalFile().getParentFile().mkdirs();
      copyStream(data, new FileOutputStream(file));
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /***********************************************************************/
  public static String readFile(String absolutePath)
  {
    return readFile(new File(absolutePath));
  }
  /***********************************************************************/
  public static String readFile(File file)
  {
    try
    {
      if (!file.exists()) { throw new RuntimeException("Invalid file '" + file.getAbsolutePath() + "'"); }
      BufferedReader in = new BufferedReader(new FileReader(file));
      return readBuffer(in);
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  public static String readBuffer(BufferedReader in)
  {
    try
    {
      StringBuffer string = new StringBuffer();
      while (in.ready())
      {
        string.append(in.readLine());
        string.append("\n");
      }
      in.close();
      return string.toString();
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  /************************************************************************/
  public static String readFileWithSuppressedExceptions(File databaseFile)
  {
    return FileUtils.readFile(databaseFile);
  }
  /************************************************************************/
  public static File saveToFile(String prefix, Reader input)
  {
    File file;
    BufferedWriter bw = null;
    try
    {
      file = File.createTempFile(prefix, null);
      bw = new BufferedWriter(new FileWriter(file));
      BufferedReader inputReader = new BufferedReader(input);
      String thisLine;
      while ((thisLine = inputReader.readLine()) != null)
      {
        bw.write(thisLine);
        bw.newLine();
      }
      inputReader.close();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Unable to store order: " + e.getMessage(), e);
    }
    finally
    {
      try
      {
        if (bw != null)
        {
          bw.close();
        }
      }
      catch (IOException e)
      {
      }
    }
    return file;
  }
  /************************************************************************/
  public static String getDirectoryFriendlyName(String name)
  {
    if (name == null) { return ""; }
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < name.length(); i++)
    {
      char c = name.charAt(i);
      switch (c)
      {
        case '.' :
          break;
        default :
          result.append(c);
          break;
      }
    }
    return result.toString();
  }
  /************************************************************************/
  /************************************************************************/
  public static String getExtensionWithDot(String filename)
  {
    int p = filename.lastIndexOf('.');
    return filename.substring(p);
  }
  public static String getExtensionWithoutDot(String filename)
  {
    return getExtensionWithDot(filename).substring(1);
  }
  public static void createIfNeeded(String file)
  {
    try
    {
      File f = new File(file);
      if (!f.exists())
      {
        writeFile(f, "");
      }
    }
    catch (Throwable e)
    {
      throw ObjectUtils.throwAsError(e);
    }
  }
  public static String readStream(InputStream resourceAsStream)
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
    return FileUtils.readBuffer(reader);
  }
  public static char[] loadResourceFromClasspathAsBytes(Class<?> clazz, String name)
  {
    return extractBytes(clazz.getResourceAsStream(name));
  }
  public static char[] extractBytes(final InputStream resourceAsStream)
  {
    try
    {
      ArrayList<Character> data = new ArrayList<Character>();
      int b = resourceAsStream.read();
      while (b != -1)
      {
        data.add(new Character((char) b));
        b = resourceAsStream.read();
      }
      return FileUtils.toChars(data);
    }
    catch (Throwable t)
    {
      throw ObjectUtils.throwAsError(t);
    }
  }
  public static char[] toChars(List<Character> data)
  {
    char[] out = new char[data.size()];
    for (int i = 0; i < out.length; i++)
    {
      out[i] = data.get(i);
    }
    return out;
  }
  public static boolean isNonEmptyFile(String approved)
  {
    File file = new File(approved);
    return file.exists() && file.length() > 0;
  }
  public static void ensureParentDirectoriesExist(File file)
  {
    File dir = file.getParentFile();
    if (!dir.exists())
    {
      dir.mkdirs();
    }
  }
  public static String readFile(File file, String defaultText)
  {
    try
    {
      return readFile(file);
    }
    catch (Throwable e)
    {
      return defaultText;
    }
  }
  public static String getCurrentDirectory()
  {
    try
    {
      return new File(".").getCanonicalPath();
    }
    catch (Throwable e)
    {
      throw ObjectUtils.throwAsError(e);
    }
  }
}