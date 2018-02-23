package org.liuyehcf.grammar.rg.nfa;

import org.liuyehcf.grammar.core.definition.Symbol;
import org.liuyehcf.grammar.rg.Matcher;
import org.liuyehcf.grammar.rg.utils.SymbolUtils;
import org.liuyehcf.grammar.utils.Pair;

import java.util.*;

import static org.liuyehcf.grammar.utils.AssertUtils.assertTrue;

public class NfaMatcher implements Matcher {

    // Nfa自动机
    private final Nfa nfa;

    // 待匹配的输入符号
    private final String input;

    // group i --> 起始索引 的映射表，闭集
    private Map<Integer, Integer> groupStartIndexes = new HashMap<>();

    // group i --> 接收索引 的映射表，闭集
    private Map<Integer, Integer> groupEndIndexes = new HashMap<>();

    NfaMatcher(Nfa nfa, String input) {
        this.nfa = nfa;
        this.input = input;
    }

    @Override
    public boolean matches() {
        NfaState curNfaState = nfa.getNfaClosure().getStartNfaState();

        Set<String> visitedNfaState = new HashSet<>();

        return isMatchDfs(curNfaState, input, 0, visitedNfaState);
    }

    private Pair<Map<Integer, Integer>, Map<Integer, Integer>> setGroupIndex(NfaState curNfaState, int index) {
        // 由于需要回溯，因此保留一下原始状态
        Pair<Map<Integer, Integer>, Map<Integer, Integer>> pair = new Pair<>(
                new HashMap<>(groupStartIndexes),
                new HashMap<>(groupEndIndexes)
        );

        if (!curNfaState.getGroupStart().isEmpty()) {
            for (int group : curNfaState.getGroupStart()) {
                groupStartIndexes.put(group, index);
            }
        }

        if (!curNfaState.getGroupReceive().isEmpty()) {
            for (int group : curNfaState.getGroupReceive()) {
                groupEndIndexes.put(group, index);
            }
        }

        return pair;
    }

    private boolean isMatchDfs(NfaState curNfaState, String s, int index, Set<String> visitedNfaState) {
        Pair<Map<Integer, Integer>, Map<Integer, Integer>> pair = setGroupIndex(curNfaState, index);

        // 从当前节点出发，经过ε边的后继节点集合
        List<NfaState> epsilonNextStates = curNfaState.getNextNfaStatesWithInputSymbol(
                Symbol.EPSILON
        );
        for (NfaState nextState : epsilonNextStates) {
            // 为了避免重复经过相同的 ε边，每次访问ε边，给一个标记
            String curStateString = nextState.toString() + index + Symbol.EPSILON;

            if (visitedNfaState.add(curStateString)) {
                if (isMatchDfs(nextState, s, index, visitedNfaState))
                    return true;
                visitedNfaState.remove(curStateString);
            }
        }

        if (index == s.length()) {
            return curNfaState.canReceive(0);
        }

        // 从当前节点出发，经过非ε边的next节点集合
        List<NfaState> nextStates = curNfaState.getNextNfaStatesWithInputSymbol(
                SymbolUtils.getAlphabetSymbolWithChar(s.charAt(index)));

        for (NfaState nextState : nextStates) {

            if (isMatchDfs(nextState, s, index + 1, visitedNfaState))
                return true;
        }

        groupStartIndexes = pair.getFirst();
        groupEndIndexes = pair.getSecond();

        return false;
    }


    @Override
    public boolean find() {
        return false;
    }

    @Override
    public String group(int group) {
        assertTrue(groupStartIndexes.containsKey(group));
        assertTrue(groupEndIndexes.containsKey(group));
        return input.substring(
                groupStartIndexes.get(group),
                groupEndIndexes.get(group)
        );
    }
}