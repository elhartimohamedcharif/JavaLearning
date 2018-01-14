package org.liuyehcf.markdownformat.processor.impl;

import org.liuyehcf.markdownformat.context.DefaultLineElement;
import org.liuyehcf.markdownformat.context.FileContext;
import org.liuyehcf.markdownformat.context.LineElement;
import org.liuyehcf.markdownformat.context.LineIterator;
import org.liuyehcf.markdownformat.processor.PreFileProcessor;

import java.util.Arrays;

import static org.liuyehcf.markdownformat.util.LineIteratorUtils.nextLineIsEmpty;
import static org.liuyehcf.markdownformat.util.LineIteratorUtils.previousLineIsEmpty;

/**
 * Created by HCF on 2018/1/14.
 */
public class IndexProcessor implements PreFileProcessor {
    //1~6级标题
    private int[] indexes = new int[6];

    @Override
    public void process(FileContext fileContext) {
        reset();

        LineIterator iterator = fileContext.getLineIteratorOfCurrentFile();

        while (iterator.isNotFinish()) {

            LineElement lineElement = iterator.getCurrentLineElement();

            // escape code line
            if (!lineElement.isCode()) {

                String content = lineElement.getContent();

                if (content.startsWith("# ")) {
                    checkBlank(iterator);
                    indexes[0]++;
                    freshIndex(0);
                    content = removeIndex(content, 0);
                    content = "# "
                            + indexes[0]
                            + " "
                            + content.substring(2);
                } else if (content.startsWith("## ")) {
                    checkBlank(iterator);
                    indexes[1]++;
                    freshIndex(1);
                    content = removeIndex(content, 1);
                    content = "## "
                            + indexes[0]
                            + "."
                            + indexes[1]
                            + " "
                            + content.substring(3);
                } else if (content.startsWith("### ")) {
                    checkBlank(iterator);
                    indexes[2]++;
                    freshIndex(2);
                    content = removeIndex(content, 2);
                    content = "### "
                            + indexes[0]
                            + "."
                            + indexes[1]
                            + "."
                            + indexes[2]
                            + " "
                            + content.substring(4);
                } else if (content.startsWith("#### ")) {
                    checkBlank(iterator);
                    indexes[3]++;
                    freshIndex(3);
                    content = removeIndex(content, 3);
                    content = "#### "
                            + indexes[0]
                            + "."
                            + indexes[1]
                            + "."
                            + indexes[2]
                            + "."
                            + indexes[3]
                            + " "
                            + content.substring(5);
                } else if (content.startsWith("##### ")) {
                    checkBlank(iterator);
                    indexes[4]++;
                    freshIndex(4);
                    content = removeIndex(content, 4);
                    content = "##### "
                            + indexes[0]
                            + "."
                            + indexes[1]
                            + "."
                            + indexes[2]
                            + "."
                            + indexes[3]
                            + "."
                            + indexes[4]
                            + " "
                            + content.substring(6);
                } else if (content.startsWith("###### ")) {
                    checkBlank(iterator);
                    indexes[5]++;
                    freshIndex(5);
                    content = removeIndex(content, 5);
                    content = "###### "
                            + indexes[0]
                            + "."
                            + indexes[1]
                            + "."
                            + indexes[2]
                            + "."
                            + indexes[3]
                            + "."
                            + indexes[4]
                            + "."
                            + indexes[5]
                            + " "
                            + content.substring(7);
                }

                lineElement.setContent(content);
            }
            iterator.moveForward();
        }
    }

    private void freshIndex(int level) {
        if (level <= 4) {
            indexes[5] = 0;
        }
        if (level <= 3) {
            indexes[4] = 0;
        }
        if (level <= 2) {
            indexes[3] = 0;
        }
        if (level <= 1) {
            indexes[2] = 0;
        }
        if (level <= 0) {
            indexes[1] = 0;
        }
    }

    private void reset() {
        Arrays.fill(indexes, 0);
    }

    private String removeIndex(String content, int level) {
        switch (level) {
            case 0:
                content = content.replaceFirst("^# +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "# ");
                break;
            case 1:
                content = content.replaceFirst("^## +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "## ");
                break;
            case 2:
                content = content.replaceFirst("^### +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "### ");
                break;
            case 3:
                content = content.replaceFirst("^#### +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "#### ");
                break;
            case 4:
                content = content.replaceFirst("^##### +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "##### ");
                break;
            case 5:
                content = content.replaceFirst("^###### +(&emsp;)*[0-9]+((\\.[0-9]+)+)? ", "###### ");
                break;
            default:
                throw new RuntimeException();
        }
        return content;
    }

    private void checkBlank(LineIterator iterator) {
        if (!previousLineIsEmpty(iterator)) {
            iterator.insertPrevious(new DefaultLineElement("", false));
        }

        if (!nextLineIsEmpty(iterator)) {
            iterator.insertNext(new DefaultLineElement("", false));
        }
    }
}
