package org.liuyehcf.grammar.definition;

import org.liuyehcf.grammar.core.MorphemeType;

/**
 * 文法符号，包括终结符和非终结符
 * 特殊符号用"__"作为前后缀，且全部字母大写，同时禁止普通Symbol带有"__"前后缀
 * 该类型与String一样，都是无状态的。只要字段数值一样，那么equals必定相同，hashCode也相同
 */
public class Symbol {

    public static final Symbol EPSILON = new Symbol(true, "__EPSILON__", 0, MorphemeType.NORMAL);
    public static final Symbol DOLLAR = new Symbol(true, "__DOLLAR__", 0, MorphemeType.NORMAL);
    public static final Symbol DOT = new Symbol(true, "__DOT__", 0, MorphemeType.NORMAL);

    private static final String SPECIAL_PREFIX = "__";
    private static final String SPECIAL_SUFFIX = "__";
    private static final String PRIME = "′";

    // 是否为终结符
    private final boolean isTerminator;

    // 符号的字符串
    private final String value;

    // 一撇"′"的数量（value本身包含的"′"不算）
    private final int primeCount;

    // 词素类型（符号也是一种词素），词素类型不同，value字段的含义也不同
    // 如果是REGEX类型，那么value就是指正则表达式的id
    private final MorphemeType type;

    /**
     * 唯一构造方法
     *
     * @param isTerminator
     * @param value
     * @param primeCount
     */
    private Symbol(boolean isTerminator, String value, int primeCount, MorphemeType type) {
        if (value == null || type == null) {
            throw new NullPointerException();
        }
        this.isTerminator = isTerminator;
        this.value = value;
        this.primeCount = primeCount;
        this.type = type;
    }

    public static Symbol createTerminator(String value) {
        return createSymbol(true, value, MorphemeType.NORMAL);
    }

    public static Symbol createRegexTerminator(String value) {
        return createSymbol(true, value, MorphemeType.REGEX);
    }

    public static Symbol createNonTerminator(String value) {
        return createSymbol(false, value, MorphemeType.NORMAL);
    }

    /**
     * 仅用于创建普通Symbol，需要检查value值的合法性
     *
     * @param isTerminator
     * @param value
     */
    private static Symbol createSymbol(boolean isTerminator, String value, MorphemeType type) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value.startsWith(SPECIAL_PREFIX) || value.endsWith(SPECIAL_SUFFIX)) {
            throw new IllegalArgumentException();
        }

        return new Symbol(isTerminator, value, 0, type);
    }

    public boolean isTerminator() {
        return isTerminator;
    }

    public String getValue() {
        return value;
    }

    public MorphemeType getType() {
        return type;
    }

    /**
     * 带上异变后缀的符号字符串
     *
     * @return
     */
    public String getPrimedValue() {
        if (primeCount == 0) {
            return value;
        } else {
            return '(' + value + ')' + toPrimeString();
        }
    }

    /**
     * 是否为异变符号
     *
     * @return
     */
    public boolean isPrimedSymbol() {
        return primeCount != 0;
    }

    /**
     * 根据异变次数生成后缀符号，例如1次异变就是"′"
     *
     * @return
     */
    private String toPrimeString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < primeCount; i++) {
            sb.append(PRIME);
        }
        return sb.toString();
    }

    /**
     * 产生一个异变符号
     *
     * @return
     */
    public Symbol getPrimedSymbol() {
        return new Symbol(this.isTerminator, this.value, this.primeCount + 1, this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            return other.isTerminator == this.isTerminator
                    && other.value.equals(this.value)
                    && other.primeCount == this.primeCount
                    && other.type.equals(this.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 这里不能用type.hashCode，否则会调用Object的hashCode，导致每次运行hashCode不一致（会导致Map顺序变化，以至于测试用例通不过）
        return Boolean.valueOf(this.isTerminator).hashCode() +
                this.value.hashCode() +
                Integer.valueOf(this.primeCount).hashCode() +
                this.type.getOrder();
    }

    public String toJSONString() {
        return '{' +
                "\"isTerminator\":" + '\"' + isTerminator + '\"' +
                ", \"value\":" + '\"' + getPrimedValue() + '\"' +
                '}';
    }

    public String toReadableJSONString() {
        return getPrimedValue();
    }

    @Override
    public String toString() {
        return toReadableJSONString();
    }
}