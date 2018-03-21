package org.liuyehcf.grammar.cfg.lr;

import org.junit.Test;
import org.liuyehcf.grammar.GrammarCase;
import org.liuyehcf.grammar.JdkLexicalAnalyzer;
import org.liuyehcf.grammar.LexicalAnalyzer;

import static org.junit.Assert.assertEquals;

public class TestSLR {
    @Test
    public void testCase1() {
        LexicalAnalyzer analyzer = JdkLexicalAnalyzer.builder()
                .addMorpheme("(")
                .addMorpheme(")")
                .addMorpheme("*")
                .addMorpheme("+")
                .addMorpheme("id")
                .build();

        LRParser parser = SLR.create(analyzer, GrammarCase.GRAMMAR_CASE_11);

        assertEquals(
                "| 状态\\文法符号 | ( | ) | * | + | id | __DOLLAR__ | T | E | F |\n" +
                        "|:--|:--|:--|:--|:--|:--|:--|:--|:--|:--|\n" +
                        "| 0 | MOVE_IN \"4\" | \\ | \\ | \\ | MOVE_IN \"5\" | \\ | JUMP \"2\" | JUMP \"1\" | JUMP \"3\" |\n" +
                        "| 1 | \\ | \\ | \\ | MOVE_IN \"6\" | \\ | ACCEPT \"__START__ → E\" | \\ | \\ | \\ |\n" +
                        "| 2 | \\ | REDUCTION \"E → T\" | MOVE_IN \"7\" | REDUCTION \"E → T\" | \\ | REDUCTION \"E → T\" | \\ | \\ | \\ |\n" +
                        "| 3 | \\ | REDUCTION \"T → F\" | REDUCTION \"T → F\" | REDUCTION \"T → F\" | \\ | REDUCTION \"T → F\" | \\ | \\ | \\ |\n" +
                        "| 4 | MOVE_IN \"4\" | \\ | \\ | \\ | MOVE_IN \"5\" | \\ | JUMP \"2\" | JUMP \"8\" | JUMP \"3\" |\n" +
                        "| 5 | \\ | REDUCTION \"F → id\" | REDUCTION \"F → id\" | REDUCTION \"F → id\" | \\ | REDUCTION \"F → id\" | \\ | \\ | \\ |\n" +
                        "| 6 | MOVE_IN \"4\" | \\ | \\ | \\ | MOVE_IN \"5\" | \\ | JUMP \"9\" | \\ | JUMP \"3\" |\n" +
                        "| 7 | MOVE_IN \"4\" | \\ | \\ | \\ | MOVE_IN \"5\" | \\ | \\ | \\ | JUMP \"10\" |\n" +
                        "| 8 | \\ | MOVE_IN \"11\" | \\ | MOVE_IN \"6\" | \\ | \\ | \\ | \\ | \\ |\n" +
                        "| 9 | \\ | REDUCTION \"E → E + T\" | MOVE_IN \"7\" | REDUCTION \"E → E + T\" | \\ | REDUCTION \"E → E + T\" | \\ | \\ | \\ |\n" +
                        "| 10 | \\ | REDUCTION \"T → T * F\" | REDUCTION \"T → T * F\" | REDUCTION \"T → T * F\" | \\ | REDUCTION \"T → T * F\" | \\ | \\ | \\ |\n" +
                        "| 11 | \\ | REDUCTION \"F → ( E )\" | REDUCTION \"F → ( E )\" | REDUCTION \"F → ( E )\" | \\ | REDUCTION \"F → ( E )\" | \\ | \\ | \\ |\n",
                parser.getAnalysisTableMarkdownString()
        );
    }

    @Test
    public void testCase2() {
        LexicalAnalyzer analyzer = JdkLexicalAnalyzer.builder()
                .addMorpheme("(")
                .addMorpheme(")")
                .addMorpheme("*")
                .addMorpheme("+")
                .addMorpheme("id")
                .build();

        LRParser parser = SLR.create(analyzer, GrammarCase.GRAMMAR_CASE_12);

        assertEquals(
                "{\"nonTerminator\":{\"__START__\":\"__DOLLAR__\",\"B\":\"d\",\"T\":\"b,__DOLLAR__\"}}",
                parser.getFollowJSONString()
        );

        assertEquals(
                "{\"1\":\"[0, a] → 2\",\"2\":\"[0, T] → 1\",\"3\":\"[2, a] → 2\",\"4\":\"[2, B] → 3\",\"5\":\"[2, T] → 4\",\"6\":\"[3, d] → 5\",\"7\":\"[4, b] → 6\"}",
                parser.getClosureTransferTableJSONString()
        );

        assertEquals(
                "{\"0\":[\"__START__ → · T\",\"T → · a B d\",\"T → __EPSILON__ ·\"],\"1\":[\"__START__ → T ·\"],\"2\":[\"T → a · B d\",\"B → · T b\",\"B → __EPSILON__ ·\",\"T → · a B d\",\"T → __EPSILON__ ·\"],\"3\":[\"T → a B · d\"],\"4\":[\"B → T · b\"],\"5\":[\"T → a B d ·\"],\"6\":[\"B → T b ·\"]}",
                parser.getClosureJSONString()
        );

        assertEquals(
                "| 状态\\文法符号 | a | b | d | __DOLLAR__ | B | T |\n" +
                        "|:--|:--|:--|:--|:--|:--|:--|\n" +
                        "| 0 | MOVE_IN \"2\" | REDUCTION \"T → __EPSILON__\" | \\ | REDUCTION \"T → __EPSILON__\" | \\ | JUMP \"1\" |\n" +
                        "| 1 | \\ | \\ | \\ | ACCEPT \"__START__ → T\" | \\ | \\ |\n" +
                        "| 2 | MOVE_IN \"2\" | REDUCTION \"T → __EPSILON__\" | REDUCTION \"B → __EPSILON__\" | REDUCTION \"T → __EPSILON__\" | JUMP \"3\" | JUMP \"4\" |\n" +
                        "| 3 | \\ | \\ | MOVE_IN \"5\" | \\ | \\ | \\ |\n" +
                        "| 4 | \\ | MOVE_IN \"6\" | \\ | \\ | \\ | \\ |\n" +
                        "| 5 | \\ | REDUCTION \"T → a B d\" | \\ | REDUCTION \"T → a B d\" | \\ | \\ |\n" +
                        "| 6 | \\ | \\ | REDUCTION \"B → T b\" | \\ | \\ | \\ |\n",
                parser.getAnalysisTableMarkdownString()
        );
    }
}