/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.misc;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class is used to maintain mappings from packages, classes
 * and resources to their enclosing JAR files. Mappings are kept
 * at the package level except for class or resource files that
 * are located at the root directory. URLClassLoader uses the mapping
 * information to determine where to fetch an extension class or
 * resource from.
 *
 * @author Zhenghua Li
 * @version %I%, %G%
 * @since 1.3
 */

public class JarIndex {

    /**
     * The index file name.
     */
    public static final String INDEX_NAME = "META-INF/INDEX.LIST";
    /**
     * true if, and only if, sun.misc.JarIndex.metaInfFilenames is set to true.
     * If true, the names of the files in META-INF, and its subdirectories, will
     * be added to the index. Otherwise, just the directory names are added.
     */
    private static final boolean metaInfFilenames =
            "true".equals(System.getProperty("sun.misc.JarIndex.metaInfFilenames")) ? true : false;
    /**
     * The hash map that maintains mappings from
     * package/classe/resource to jar file list(s)
     */
    private HashMap indexMap;
    /**
     * The hash map that maintains mappings from
     * jar file to package/class/resource lists
     */
    private HashMap jarMap;
    /*
     * An ordered list of jar file names.
     */
    private String[] jarFiles;

    /**
     * Constructs a new, empty jar index.
     */
    public JarIndex() {
        indexMap = new HashMap();
        jarMap = new HashMap();
    }

    /**
     * Constructs a new index from the specified input stream.
     *
     * @param is the input stream containing the index data
     */
    public JarIndex(InputStream is) throws IOException {
        this();
        read(is);
    }

    /**
     * Constructs a new index for the specified list of jar files.
     *
     * @param files the list of jar files to construct the index from.
     */
    public JarIndex(String[] files) throws IOException {
        this();
        this.jarFiles = files;
        parseJars(files);
    }

    /**
     * Returns the jar index, or <code>null</code> if none.
     * <p>
     * This single parameter version of the method is retained
     * for binary compatibility with earlier releases.
     *
     * @param jar the JAR file to get the index from.
     * @throws IOException if an I/O error has occurred.
     */
    public static JarIndex getJarIndex(JarFile jar) throws IOException {
        return getJarIndex(jar, null);
    }

    /**
     * Returns the jar index, or <code>null</code> if none.
     *
     * @param jar the JAR file to get the index from.
     * @throws IOException if an I/O error has occurred.
     */
    public static JarIndex getJarIndex(JarFile jar, MetaIndex metaIndex) throws IOException {
        JarIndex index = null;
    /* If metaIndex is not null, check the meta index to see
	   if META-INF/INDEX.LIST is contained in jar file or not.
	*/
        if (metaIndex != null &&
                !metaIndex.mayContain(INDEX_NAME)) {
            return null;
        }
        JarEntry e = jar.getJarEntry(INDEX_NAME);
        // if found, then load the index
        if (e != null) {
            index = new JarIndex(jar.getInputStream(e));
        }
        return index;
    }

    /**
     * Returns the jar files that are defined in this index.
     */
    public String[] getJarFiles() {
        return jarFiles;
    }

    /*
     * Add the key, value pair to the hashmap, the value will
     * be put in a linked list which is created if necessary.
     */
    private void addToList(String key, String value, HashMap t) {
        LinkedList list = (LinkedList) t.get(key);
        if (list == null) {
            list = new LinkedList();
            list.add(value);
            t.put(key, list);
        } else if (!list.contains(value)) {
            list.add(value);
        }
    }

    /**
     * Returns the list of jar files that are mapped to the file.
     *
     * @param fileName the key of the mapping
     */
    public LinkedList get(String fileName) {
        LinkedList jarFiles = null;
        if ((jarFiles = (LinkedList) indexMap.get(fileName)) == null) {
            /* try the package name again */
            int pos;
            if ((pos = fileName.lastIndexOf("/")) != -1) {
                jarFiles = (LinkedList) indexMap.get(fileName.substring(0, pos));
            }
        }
        return jarFiles;
    }

    /**
     * Add the mapping from the specified file to the specified
     * jar file. If there were no mapping for the package of the
     * specified file before, a new linked list will be created,
     * the jar file is added to the list and a new mapping from
     * the package to the jar file list is added to the hashmap.
     * Otherwise, the jar file will be added to the end of the
     * existing list.
     *
     * @param fileName the file name
     * @param jarName  the jar file that the file is mapped to
     */
    public void add(String fileName, String jarName) {
        String packageName;
        int pos;
        if ((pos = fileName.lastIndexOf("/")) != -1) {
            packageName = fileName.substring(0, pos);
        } else {
            packageName = fileName;
        }

        // add the mapping to indexMap
        addToList(packageName, jarName, indexMap);

        // add the mapping to jarMap
        addToList(jarName, packageName, jarMap);
    }

    /**
     * Same as add(String,String) except that it doesn't strip off from the
     * last index of '/'. It just adds the filename.
     */
    private void addExplicit(String fileName, String jarName) {
        // add the mapping to indexMap
        addToList(fileName, jarName, indexMap);

        // add the mapping to jarMap
        addToList(jarName, fileName, jarMap);
    }

    /**
     * Go through all the jar files and construct the
     * index table.
     */
    private void parseJars(String[] files) throws IOException {
        if (files == null) {
            return;
        }

        String currentJar = null;

        for (int i = 0; i < files.length; i++) {
            currentJar = files[i];
            ZipFile zrf = new ZipFile(currentJar.replace
                    ('/', File.separatorChar));

            Enumeration entries = zrf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String fileName = entry.getName();

                // Skip the META-INF directory, the index, and manifest.
                // Any files in META-INF/ will be indexed explicitly
                if (fileName.equals("META-INF/") ||
                        fileName.equals(INDEX_NAME) ||
                        fileName.equals(JarFile.MANIFEST_NAME))
                    continue;

                if (!metaInfFilenames) {
                    add(fileName, currentJar);
                } else {
                    if (!fileName.startsWith("META-INF/")) {
                        add(fileName, currentJar);
                    } else if (!entry.isDirectory()) {
                        // Add files under META-INF explicitly so that certain services,
                        // like ServiceLoader, etc, can be located with greater accuracy.
                        // Directories can be skipped since each file will be added explicitly.
                        addExplicit(fileName, currentJar);
                    }
                }
            }
            zrf.close();
        }
    }

    /**
     * Writes the index to the specified OutputStream
     *
     * @param out the output stream
     * @throws IOException if an I/O error has occurred
     */
    public void write(OutputStream out) throws IOException {
        BufferedWriter bw = new BufferedWriter
                (new OutputStreamWriter(out, "UTF8"));
        bw.write("JarIndex-Version: 1.0\n\n");

        if (jarFiles != null) {
            for (int i = 0; i < jarFiles.length; i++) {
                /* print out the jar file name */
                String jar = jarFiles[i];
                bw.write(jar + "\n");
                LinkedList jarlist = (LinkedList) jarMap.get(jar);
                if (jarlist != null) {
                    Iterator listitr = jarlist.iterator();
                    while (listitr.hasNext()) {
                        bw.write((String) (listitr.next()) + "\n");
                    }
                }
                bw.write("\n");
            }
            bw.flush();
        }
    }


    /**
     * Reads the index from the specified InputStream.
     *
     * @param is the input stream
     * @throws IOException if an I/O error has occurred
     */
    public void read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader
                (new InputStreamReader(is, "UTF8"));
        String line = null;
        String currentJar = null;

        /* an ordered list of jar file names */
        Vector jars = new Vector();

        /* read until we see a .jar line */
        while ((line = br.readLine()) != null && !line.endsWith(".jar")) ;

        for (; line != null; line = br.readLine()) {
            if (line.length() == 0)
                continue;

            if (line.endsWith(".jar")) {
                currentJar = line;
                jars.add(currentJar);
            } else {
                String name = line;
                addToList(name, currentJar, indexMap);
                addToList(currentJar, name, jarMap);
            }
        }

        jarFiles = (String[]) jars.toArray(new String[jars.size()]);
    }

    /**
     * Merges the current index into another index, taking into account
     * the relative path of the current index.
     *
     * @param toIndex The destination index which the current index will
     *                merge into.
     * @param path    The relative path of the this index to the destination
     *                index.
     */
    public void merge(JarIndex toIndex, String path) {
        Iterator itr = indexMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry) itr.next();
            String packageName = (String) e.getKey();
            LinkedList from_list = (LinkedList) e.getValue();
            Iterator listItr = from_list.iterator();
            while (listItr.hasNext()) {
                String jarName = (String) listItr.next();
                if (path != null) {
                    jarName = path.concat(jarName);
                }
                toIndex.add(packageName, jarName);
            }
        }
    }
}
