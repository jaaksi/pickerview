package org.jaaksi.pickerview.adapter;

/**
 * 数字adapter
 */
public class NumericWheelAdapter implements WheelAdapter<Integer> {

  private int minValue;
  private int maxValue;

  /**
   * Constructor
   *
   * @param minValue the wheel min value
   * @param maxValue the wheel max value
   */
  public NumericWheelAdapter(int minValue, int maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override public int getItemCount() {
    return maxValue - minValue + 1;
  }

  @Override public Integer getItem(int index) {
    if (index >= 0 && index < getItemCount()) {
      return minValue + index;
    }
    return 0;
  }
}
