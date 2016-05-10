package de.neemann.digital.analyse.expression;

import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * A operation
 * There are only two implementations: The AND and the OR operation
 *
 * @author hneemann
 */
public abstract class Operation implements Expression {
    private static final Comparator<Expression> EXPRESSION_COMPARATOR
            = (a, b) -> a.getOrderString().compareTo(b.getOrderString());

    private final ArrayList<Expression> expr;

    /**
     * Creates a new OR expression
     *
     * @param exp the expressions to OR
     * @return the created expression
     */
    public static Expression or(Iterable<Expression> exp) {
        return simplify(new Or(exp));
    }

    /**
     * Creates a new OR expression
     *
     * @param exp the expressions to OR
     * @return the created expression
     */
    public static Expression or(Expression... exp) {
        return simplify(new Or(Arrays.asList(exp)));
    }

    /**
     * Creates a new AND expression
     *
     * @param exp the expressions to AND
     * @return the created expression
     */
    public static Expression and(Iterable<Expression> exp) {
        return simplify(new And(exp));
    }

    /**
     * Creates a new AND expression
     *
     * @param exp the expressions to AND
     * @return the created expression
     */
    public static Expression and(Expression... exp) {
        return simplify(new And(Arrays.asList(exp)));
    }

    private static Expression simplify(Operation operation) {
        int size = operation.getExpressions().size();
        switch (size) {
            case 0:
                return null;
            case 1:
                return operation.getExpressions().get(0);
            default:
                Collections.sort(operation.getExpressions(), EXPRESSION_COMPARATOR);
                return operation;
        }
    }

    private Operation(Iterable<Expression> exp) {
        expr = new ArrayList<>();
        for (Expression e : exp)
            if (e != null)
                merge(e);
    }

    private void merge(Expression e) {
        if (e.getClass() == getClass()) {
            expr.addAll(((Operation) e).getExpressions());
        } else
            expr.add(e);
    }

    @Override
    public boolean calculate(Context context) throws ExpressionException {
        boolean result = getNeutral();
        for (Expression e : expr)
            result = calc(result, e.calculate(context));
        return result;
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        if (v.visit(this)) {
            for (Expression e : expr)
                e.traverse(v);
        }
        return v;
    }

    @Override
    public void modify(ExpressionModifier modifier) {
        for (int i = 0; i < expr.size(); i++) {
            Expression e = expr.get(i);
            e.modify(modifier);
            expr.set(i, modifier.modify(e));
        }
    }

    /**
     * @return the sub expressions
     */
    public ArrayList<Expression> getExpressions() {
        return expr;
    }

    @Override
    public String getOrderString() {
        return expr.get(0).getOrderString();
    }

    /**
     * @return the neutral element of this operation
     */
    protected abstract boolean getNeutral();

    /**
     * Performs the calculation
     *
     * @param a value a
     * @param b value b
     * @return result
     */
    protected abstract boolean calc(boolean a, boolean b);

    /**
     * The AND expression
     */
    public static final class And extends Operation {

        private And(Iterable<Expression> exp) {
            super(exp);
        }

        @Override
        protected boolean getNeutral() {
            return true;
        }

        @Override
        protected boolean calc(boolean a, boolean b) {
            return a && b;
        }
    }

    /**
     * The OR expression
     */
    public static final class Or extends Operation {

        private Or(Iterable<Expression> exp) {
            super(exp);
        }

        @Override
        protected boolean getNeutral() {
            return false;
        }

        @Override
        protected boolean calc(boolean a, boolean b) {
            return a || b;
        }
    }
}