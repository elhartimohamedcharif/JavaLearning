package org.liuyehcf.compile;

import org.junit.Test;
import org.liuyehcf.compile.definition.Grammar;
import org.liuyehcf.compile.definition.Production;
import org.liuyehcf.compile.definition.SymbolSequence;

import static org.junit.Assert.assertEquals;
import static org.liuyehcf.compile.definition.Symbol.createNonTerminator;
import static org.liuyehcf.compile.definition.Symbol.createTerminator;

public class TestGrammarConvert {
    @Test
    public void convertCase1() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("+"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("*"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("("),
                                createNonTerminator("E"),
                                createTerminator(")")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("id")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"E → (E)E′|idE′\",\"E′ → +EE′|*EE′|__EPSILON__\",\"__START__ → (E)E′|idE′\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }

    @Test
    public void convertCase2() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("D"),
                        SymbolSequence.create(
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("+"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("*"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("("),
                                createNonTerminator("E"),
                                createTerminator(")")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("id")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"D → (E)E′|idE′\",\"E → (E)E′|idE′\",\"E′ → +EE′|*EE′|__EPSILON__\",\"__START__ → (E)E′|idE′\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }

    @Test
    public void convertCase3() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("D"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("e")
                        )
                ),
                Production.create(
                        createNonTerminator("D"),
                        SymbolSequence.create(
                                createTerminator("e"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("+"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createNonTerminator("E"),
                                createTerminator("*"),
                                createNonTerminator("E")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("("),
                                createNonTerminator("E"),
                                createTerminator(")")
                        )
                ),
                Production.create(
                        createNonTerminator("E"),
                        SymbolSequence.create(
                                createTerminator("id")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"D → (E)E′e|idE′e|eE\",\"E → (E)E′|idE′\",\"E′ → +EE′|*EE′|__EPSILON__\",\"__START__ → (E)E′e|idE′e|eE\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }

    @Test
    public void convertCase4() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("b")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("b"),
                                createTerminator("c")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("b"),
                                createTerminator("d")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("b"),
                                createTerminator("c")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"A → bA′′|aA′\",\"A′ → bA′′′\",\"A′′ → d|c\",\"A′′′ → __EPSILON__|c\",\"__START__ → bA′′|aA′\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }

    @Test
    public void convertCase5() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("β1")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("β2")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("βn")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("γ1")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("γ2")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("γm")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"A → aA′|γ1|γ2|γm\",\"A′ → β1|β2|βn\",\"__START__ → aA′|γ1|γ2|γm\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }

    @Test
    public void convertCase6() {
        Grammar grammar = Grammar.create(
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("b")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("b"),
                                createTerminator("c")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("a"),
                                createTerminator("b"),
                                createTerminator("c"),
                                createTerminator("d")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("b")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("b"),
                                createTerminator("c")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("b"),
                                createTerminator("c"),
                                createTerminator("d")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("c")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("c"),
                                createTerminator("d")
                        )
                ),
                Production.create(
                        createNonTerminator("A"),
                        SymbolSequence.create(
                                createTerminator("d")
                        )
                )
        );

        Grammar convertedGrammar = LL1Compiler.GrammarConverter.convert(grammar);

        assertEquals(
                "{\"productions\":[\"A → cA′′′|bA′′|aA′|d\",\"A′ → bA′′′′′|__EPSILON__\",\"A′′ → cA′′′′|__EPSILON__\",\"A′′′ → __EPSILON__|d\",\"A′′′′ → __EPSILON__|d\",\"A′′′′′ → cA′′′′′′|__EPSILON__\",\"__START__ → cA′′′|bA′′|aA′|d\",\"A′′′′′′ → __EPSILON__|d\"]}",
                convertedGrammar.toReadableJSONString()
        );
    }
}