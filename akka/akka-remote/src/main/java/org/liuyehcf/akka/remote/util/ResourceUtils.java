package org.liuyehcf.akka.remote.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author chenlu
 * @date 2019/1/25
 */
public abstract class ResourceUtils {
    public static String getAkkaConfig(String path) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        URL resource = systemClassLoader.getResource(path);

        if (resource == null) {
            throw new NullPointerException();
        }

        File file = new File(resource.getFile());
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(file)) {
            int c;
            while ((c = inputStream.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }
}
