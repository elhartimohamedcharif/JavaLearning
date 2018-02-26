package org.liuyehcf.grammar.rg.nfa;

import org.liuyehcf.grammar.core.definition.Symbol;
import org.liuyehcf.grammar.rg.Matcher;
import org.liuyehcf.grammar.rg.utils.SymbolUtils;
import org.liuyehcf.grammar.utils.Pair;

import java.util.*;

import static org.liuyehcf.grammar.utils.AssertUtils.assertNull;

public class NfaMatcher implements Matcher {

    // Nfa自动机
    private final Nfa nfa;

    // 待匹配的输入字符串
    private final String input;

    // group i --> 起始索引 的映射表，闭
    private Map<Integer, Integer> groupStartIndexes = null;

    // group i --> 接收索引 的映射表，开
    private Map<Integer, Integer> groupEndIndexes = null;

    // 匹配的区间集合
    private List<Pair<Integer, Integer>> matchIntervals;

    // 目前进行匹配操作的子串
    private String subInput;

    // 匹配子串索引
    private int indexOfMatchIntervals;

    NfaMatcher(Nfa nfa, String input) {
        if (nfa == null || input == null) {
            throw new NullPointerException();
        }
        this.nfa = nfa;
        this.input = input;
    }

    @Override
    public boolean matches() {
        return doMatch(input) != null;
    }

    private NfaState doMatch(String curInput) {
        this.subInput = curInput;

        reset();

        NfaState curNfaState = nfa.getNfaClosure().getStartNfaState();

        Set<String> visitedNfaState = new HashSet<>();

        NfaState result = isMatchDfsProxy(curNfaState, 0, visitedNfaState);

        Set<Integer> keySets = groupStartIndexes.keySet();
        for (int group : keySets.toArray(new Integer[0])) {
            if (groupEndIndexes.get(group) == -1) {
                groupStartIndexes.put(group, -1);
            }
        }

        return result;
    }

    private void reset() {
        groupStartIndexes = new HashMap<>();
        groupEndIndexes = new HashMap<>();
        for (int i = 0; i <= groupCount(); i++) {
            groupStartIndexes.put(i, -1);
            groupEndIndexes.put(i, -1);
        }
    }

    private NfaState isMatchDfsProxy(NfaState curNfaState, int index, Set<String> visitedNfaState) {
        Pair<Map<Integer, Integer>, Map<Integer, Integer>> pair = setGroupIndex(curNfaState, index);

        NfaState result = isMatchDfs(curNfaState, index, visitedNfaState);

        if (result == null) {
            groupBackTrack(pair);
        }

        return result;
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

    private void groupBackTrack(Pair<Map<Integer, Integer>, Map<Integer, Integer>> pair) {
        groupStartIndexes = pair.getFirst();
        groupEndIndexes = pair.getSecond();
    }

    private NfaState isMatchDfs(NfaState curNfaState, int index, Set<String> visitedNfaState) {

        // 首先走非ε边，贪婪模式
        if (index != subInput.length()) {
            // 从当前节点出发，经过非ε边的next节点集合
            Set<NfaState> nextStates = curNfaState.getNextNfaStatesWithInputSymbol(
                    SymbolUtils.getAlphabetSymbolWithChar(subInput.charAt(index)));

            for (NfaState nextState : nextStates) {

                NfaState result;
                if ((result = isMatchDfsProxy(nextState, index + 1, visitedNfaState)) != null) {
                    return result;
                }
            }
        }

        if (index == subInput.length() && curNfaState.canReceive()) {
            return curNfaState;
        }

        // 从当前节点出发，经过ε边的后继节点集合
        Set<NfaState> epsilonNextStates = curNfaState.getNextNfaStatesWithInputSymbol(
                Symbol.EPSILON
        );
        for (NfaState nextState : epsilonNextStates) {
            // 为了避免重复经过相同的 ε边，每次访问ε边，给一个标记
            // 在匹配目标字符串的不同位置时，允许经过相同的ε边
            String curStateString = nextState.toString() + index;

            if (visitedNfaState.add(curStateString)) {
                NfaState result;
                if ((result = isMatchDfsProxy(nextState, index, visitedNfaState)) != null) {
                    return result;
                }
                visitedNfaState.remove(curStateString);
            }
        }

        return null;
    }

    @Override
    public boolean find() {
        // todo 解决了 "(a)|(b)|(ab)" 匹配 "ab" 的问题，不知道还有没有其他find贪婪匹配问题

        if (matchIntervals == null) {
            initMatchIntervals();
        }

        if (indexOfMatchIntervals < matchIntervals.size()) {

            Pair<Integer, Integer> interval = matchIntervals.get(indexOfMatchIntervals);

            doMatch(input.substring(
                    interval.getFirst(),
                    interval.getSecond()
            ));

            indexOfMatchIntervals++;

            return true;
        }
        return false;
    }

    private void initMatchIntervals() {
        /*
         * 该映射表用来解决"(a)|(b)|(ab)" 匹配 "ab" 的问题
         * 以 regex="(a)|(b)|(ab)" 为例解释一下，这个正则表达式构造成NfaClosure之后，有三个终止节点
         * 字符串"ab"会有三个匹配子串 "a"[0,1)  "b"[1,2)  "ab"[0,2)，虽然"ab"完全包含了"a"和"b"，
         * 但是由于"a"先出现，且"ab"并不是"a"的贪婪子串（这两个子串在不同的状态被接受），因此优先选取"a"，因此"ab"子串就被丢弃了
         *
         * 对于"a+"匹配"aa"的问题
         * 字符串"aa"会有三个匹配子串 "a"[0,1)  "a"[1,2)  "aa"[0,2)，此时，这三个子串在相同的状态被接受，即
         * "aa"[0,2) 是 "a"[0,1)  "a"[1,2) 的贪婪子串，故而"aa"被选取，其余两个"a"被丢弃
         */
        Map<Pair<Integer, Integer>, NfaState> intervalNfaStateMap = new HashMap<>();

        matchIntervals = new ArrayList<>();

        if (input.length() == 0) {
            if (matches()) {
                matchIntervals.add(new Pair<>(0, 0));
            }
        }

        for (int startIndex = 0; startIndex < input.length(); startIndex++) {
            for (int endIndex = startIndex + 1; endIndex <= input.length(); endIndex++) {
                NfaState result;
                if ((result = doMatch(input.substring(startIndex, endIndex))) != null) {
                    Pair<Integer, Integer> interval = new Pair<>(startIndex, endIndex);
                    matchIntervals.add(interval);
                    intervalNfaStateMap.put(interval, result);
                }
            }
        }

        if (matchIntervals.isEmpty()) {
            return;
        }

        // 目前仅支持以贪婪模式查询匹配的子串
        // 首先排序
        matchIntervals.sort((o1, o2) -> {
            if (o1.getFirst() < o2.getFirst()) {
                return -1;
            } else if (o1.getFirst() > o2.getFirst()) {
                return 1;
            } else {
                if (o1.getSecond() < o2.getSecond()) {
                    return -1;
                } else if (o1.getSecond() > o2.getSecond()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // 然后合并包含的区间，例如[1,6)包含着[1,2] [3,5)，那么删去[1,2] [3,5)两个区间
        List<Pair<Integer, Integer>> filteredMatchIntervals = new ArrayList<>();

        Pair<Integer, Integer> preInterval = matchIntervals.get(0);

        for (int i = 1; i < matchIntervals.size(); i++) {
            Pair<Integer, Integer> nextInterval = matchIntervals.get(i);

            // 当区间包含时
            if (preInterval.getFirst() >= nextInterval.getFirst()
                    && preInterval.getSecond() <= nextInterval.getSecond()) {
                // 如果终止状态不同，说明不是*或者+之类，可能是"(a)|(b)|(ab)"匹配"ab"这种情况，那么需要保留小的区间；否则就是"a*"，匹配aa，保留大的区间
                if (intervalNfaStateMap.get(preInterval).equals(intervalNfaStateMap.get(nextInterval))) {
                    preInterval = nextInterval;
                }
            }

            // 若区间完全分离
            if (nextInterval.getFirst() >= preInterval.getSecond()) {
                filteredMatchIntervals.add(preInterval);
                preInterval = nextInterval;
            }

        }

        filteredMatchIntervals.add(preInterval);

        matchIntervals = filteredMatchIntervals;
    }

    @Override
    public String group(int group) {
        if (groupStartIndexes == null) {
            assertNull(groupEndIndexes);
            throw new IllegalStateException("No match found");
        }
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        if (groupStartIndexes.get(group) == -1
                || groupEndIndexes.get(group) == -1) {
            return null;
        }
        return subInput.substring(
                groupStartIndexes.get(group),
                groupEndIndexes.get(group)
        );
    }


    @Override
    public int groupCount() {
        return nfa.groupCount();
    }

    @Override
    public int start(int group) {
        if (groupStartIndexes == null) {
            assertNull(groupEndIndexes);
            throw new IllegalStateException("No match available");
        }
        if (group < 0 || group > groupCount()) {
            throw new IndexOutOfBoundsException("No group " + group);
        }
        return groupStartIndexes.get(group);
    }

    @Override
    public int end(int group) {
        if (groupStartIndexes == null) {
            assertNull(groupEndIndexes);
            throw new IllegalStateException("No match available");
        }
        if (group < 0 || group > groupCount()) {
            throw new IndexOutOfBoundsException("No group " + group);
        }
        return groupEndIndexes.get(group);
    }
}
