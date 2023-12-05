package cn.oyzh.fx.plus.tree;

import javafx.collections.ModifiableObservableListBase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2023/12/5
 */
public class AvdTreeItemList<E> extends ModifiableObservableListBase<E> {

    private final List<E> list = new CopyOnWriteArrayList<>();

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    protected void doAdd(int index, E element) {
        list.add(index, element);
    }

    @Override
    protected E doSet(int index, E element) {
        return list.set(index, element);
    }

    @Override
    protected E doRemove(int index) {
        return list.remove(index);
    }
}
