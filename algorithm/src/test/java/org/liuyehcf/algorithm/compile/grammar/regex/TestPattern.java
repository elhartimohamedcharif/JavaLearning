package org.liuyehcf.algorithm.compile.grammar.regex;

import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by Liuye on 2017/10/26.
 */
public class TestPattern {
    @Test
    public void test1() {
        Pattern p = Pattern.compile("[^\\w]+");
        System.out.println(p.matcher("#").matches());
    }
}