package org.jaaksi.pickerview.adapter;

import java.util.List;
import org.jaaksi.pickerview.dataset.PickerDataSet;

/**
 * The simple Array wheel adapter
 * 数据实现 {@link PickerDataSet}即可
 *
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelAdapter<T> {

  private List<? extends T> items;

  public ArrayWheelAdapter(List<? extends T> items) {
    this.items = items;
  }

  public List<? extends T> getData() {
    return items;
  }

  @Override public T getItem(int index) {
    if (items == null) return null;

    if (index >= 0 && index < getItemCount()) {
      return items.get(index);
    }
    return null;
  }

  @Override public int getItemCount() {
    return items != null ? items.size() : 0;
  }
}
