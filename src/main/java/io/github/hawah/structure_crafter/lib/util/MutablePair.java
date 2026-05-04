package io.github.hawah.structure_crafter.lib.util;

public class MutablePair<L, R> {
    private L left;
    private R right;

    public MutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public MutablePair<R, L> swap() {
        return new MutablePair<>(right, left);
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public void setRight(R right) {
        this.right = right;
    }

    @SuppressWarnings("unchecked")
    public <T> void set(T value) {
        if (left.getClass().equals(value.getClass())) {
            setLeft((L) value);
        } else if (right.getClass().equals(value.getClass())) {
            setRight((R) value);
        }
    }

    public static <U, V> MutablePair<U, V> of(U left, V right) {
        return new MutablePair<>(left, right);
    }
}
