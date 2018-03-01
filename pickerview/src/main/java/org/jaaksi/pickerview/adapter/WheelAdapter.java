package org.jaaksi.pickerview.adapter;

public interface WheelAdapter<T> {

  /**
   * Gets items count
   *
   * @return the count of wheel items
   */
  int getItemCount();

  /**
   * Gets a wheel item by index.
   *
   * @param index the item index
   * @return the wheel item text or null
   */
  T getItem(int index);
}
