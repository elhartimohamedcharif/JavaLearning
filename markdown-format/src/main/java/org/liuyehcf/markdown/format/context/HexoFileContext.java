package org.liuyehcf.markdown.format.context;

import org.liuyehcf.markdown.format.model.HexoParam;
import org.liuyehcf.markdown.format.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

import static org.liuyehcf.markdown.format.constant.RegexConstant.PROPERTY_PATTERN;
import static org.liuyehcf.markdown.format.constant.RegexConstant.SUB_PROPERTY_PATTERN;
import static org.liuyehcf.markdown.format.constant.StringConstant.HEXO_PROPERTY_BOUNDARY;
import static org.liuyehcf.markdown.format.constant.StringConstant.MARKDOWN_SUFFIX;
import static org.liuyehcf.markdown.format.log.DefaultLogger.LOGGER;

/**
 * Created by HCF on 2018/1/13.
 */
public class HexoFileContext implements FileContext {

    private final File rootDirectory;

    private final File fileDirectory;

    private final File imageDirectory;

    private int index;

    private List<File> files;

    private Map<String, String> properties;

    private LinkedList<LineElement> lineElements;

    public HexoFileContext(HexoParam hexoParam) {
        rootDirectory = hexoParam.getRootDirectory();
        fileDirectory = hexoParam.getFileDirectory();
        imageDirectory = hexoParam.getImageDirectory();
        index = 0;
        initFiles();
    }

    private static void readProperty(BufferedReader reader, File file, LinkedList<LineElement> lineElements, Map<String, String> properties) throws IOException {
        // 首先读取文件属性
        String line;
        line = reader.readLine();
        lineElements.add(new DefaultLineElement(line, false));
        if (!line.equals(HEXO_PROPERTY_BOUNDARY)) {
            LOGGER.error("file [{}] contains wrong hexo header", file);
            throw new RuntimeException();
        }

        StringBuilder sb = null;
        String preKey = null;
        while ((line = reader.readLine()) != null
                && !line.equals(HEXO_PROPERTY_BOUNDARY)) {
            if (!StringUtils.isBlankLine(line)) {
                Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);

                // 当前是正常属性
                if (propertyMatcher.matches()) {

                    // 处理一下之前的子属性
                    if (sb != null) {
                        if (preKey == null || sb.length() == 0) {
                            LOGGER.error("file [{}] contains wrong hexo header", file);
                            throw new RuntimeException();
                        }
                        properties.put(preKey, sb.substring(0, sb.length() - 1));
                        sb = null;
                        preKey = null;
                    }

                    String propertyKey = propertyMatcher.group(1);
                    String propertyValue = propertyMatcher.group(2);

                    // 子属性
                    if ("".equals(propertyValue)) {
                        preKey = propertyKey;
                        sb = new StringBuilder();
                    }
                    // 正常属性
                    else {
                        properties.put(propertyKey, propertyValue);
                    }

                }
                // 子属性
                else {
                    Matcher subPropertyMatcher = SUB_PROPERTY_PATTERN.matcher(line);
                    if (!subPropertyMatcher.matches()) {
                        LOGGER.error("file [{}] contains wrong hexo header", file);
                        throw new RuntimeException();
                    } else {
                        if (sb == null) {
                            LOGGER.error("file [{}] contains wrong hexo header", file);
                            throw new RuntimeException();
                        }
                        sb.append(subPropertyMatcher.group(1)).append(",");
                    }
                }
            }
            lineElements.add(new DefaultLineElement(line, false));
        }
        // 处理一下之前的子属性
        if (sb != null) {
            if (preKey == null || sb.length() == 0) {
                LOGGER.error("file [{}] contains wrong hexo header", file);
                throw new RuntimeException();
            }
            properties.put(preKey, sb.substring(0, sb.length() - 1));
        }
        if (!HEXO_PROPERTY_BOUNDARY.equals(line)) {
            LOGGER.error("file [{}] contains wrong hexo header", file);
            throw new RuntimeException();
        }
        lineElements.add(new DefaultLineElement(line, false));

    }

    private void initFiles() {
        File[] fileArray = fileDirectory.listFiles(
                (pathname) -> pathname.getName().endsWith(MARKDOWN_SUFFIX));

        files = new ArrayList<>();

        if (fileArray != null) {
            files.addAll(Arrays.asList(fileArray));
        }

        files = Collections.unmodifiableList(files);
    }

    @Override
    public void initFileContext() {
        try {
            readCurrentFile();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public File getFile() {
        return files.get(index);
    }

    @Override
    public LineIterator getLineIterator() {
        return new DefaultLineIterator(lineElements);
    }

    @Override
    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public File getFileDirectory() {
        return fileDirectory;
    }

    @Override
    public File getImageDirectory() {
        return imageDirectory;
    }

    @Override
    public boolean hasNextFile() {
        return index < files.size();
    }

    @Override
    public void moveForward() {
        index++;
        lineElements = null;
    }

    private void readCurrentFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getFile()));
        lineElements = new LinkedList<>();
        properties = new HashMap<>();

        readProperty(reader, getFile(), lineElements, properties);
        FileContext.readFile(reader, getFile(), lineElements);
    }

    @Override
    public boolean containsFile(String name) {
        if (!name.endsWith(MARKDOWN_SUFFIX)) {
            name = name + MARKDOWN_SUFFIX;
        }

        for (File file : files) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
