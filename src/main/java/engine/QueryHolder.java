package engine;

import java.util.ArrayList;
import java.util.List;

public class QueryHolder<T> {
    private final List<T> elements;

    public QueryHolder() {
        elements = new ArrayList<>();
    }

    public void push(T item) {
        elements.add(item);
    }

    public T pop() {
        if (elements.isEmpty()) return null;
        return elements.remove(elements.size() - 1);
    }

    public void join(QueryHolder<T> other) {
        this.elements.addAll(other.elements);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }
}
