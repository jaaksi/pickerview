package org.jaaksi.pickerview.picker;

import android.content.Context;
import java.util.List;
import org.jaaksi.pickerview.adapter.ArrayWheelAdapter;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：联动的选项picker
 * 强大点：
 * 与https://github.com/Bigkoo/Android-PickerView对比
 * 1.支持设置层级
 * 2.构造数据源简单，只需要实现OptionDataSet接口
 * 3.支持通过选中的value设置选中项，内部处理选中项逻辑，避免用户麻烦的遍历处理
 */

public class OptionPicker extends BasePicker
  implements BasePickerView.OnSelectedListener, BasePickerView.Formatter {
  private List<? extends OptionDataSet> mOptions;
  // 层级，有几层add几个pickerview
  private final int mHierarchy;
  // 选中的下标。如果为-1，表示当前选中的index列没有数据
  private final int[] mSelectedPosition;

  private Formatter mFormatter;
  private OnOptionSelectListener mOnOptionSelectListener;

  private OptionPicker(Context context, int hierarchy, OnOptionSelectListener listener) {
    super(context);
    mHierarchy = hierarchy;
    mOnOptionSelectListener = listener;
    mSelectedPosition = new int[mHierarchy];
  }

  public void setFormatter(Formatter formatter) {
    mFormatter = formatter;
  }

  private void initPicker() {
    for (int i = 0; i < mHierarchy; i++) {
      PickerView pickerView = createPickerView(i, 1);
      pickerView.setOnSelectedListener(this);
      pickerView.setFormatter(this);
    }
  }

  /**
   * 根据选中的values初始化选中的position并初始化pickerview数据
   *
   * @param options data
   * @param values 选中数据的value{@link OptionDataSet#getValue()}
   */
  public void setDataWithValues(List<? extends OptionDataSet> options, String... values) {
    // 遍历options比对value算出对应的下标
    mOptions = options;
    setSelectedWithValues(values);
  }

  /**
   * 根据选中的values初始化选中的position
   *
   * @param values 选中数据的value{@link OptionDataSet#getValue()}，如果values[0]==null，则进行默认选中，其他为null认为没有该列
   */
  @SuppressWarnings("unchecked") public void setSelectedWithValues(String... values) {
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < mHierarchy; i++) {
      PickerView pickerView = getPickerViews().get(i);
      ArrayWheelAdapter adapter = (ArrayWheelAdapter) pickerView.getAdapter();
      if (adapter == null || adapter.getData() != temp) {
        pickerView.setAdapter(new ArrayWheelAdapter<>(temp));
      }
      if (temp == null || temp.size() == 0) { // 数据源无效
        mSelectedPosition[i] = -1;
      } else if (values.length <= i || values[0] == null) { // 选中默认项0...
        mSelectedPosition[i] = 0;
      } else if (values[i] == null) {
        mSelectedPosition[i] = -1;
      } else { // 遍历找到选中的下标，如果没有找到，则将下标置为0
        for (int j = 0; j < temp.size(); j++) {
          OptionDataSet dataSet = temp.get(j);
          if (dataSet != null) {
            if (values[i].equals(dataSet.getValue())) {
              mSelectedPosition[i] = j;
              break;
            }
            if (j == temp.size()) {
              mSelectedPosition[i] = 0;
            }
          }
        }
      }

      if (mSelectedPosition[i] == -1) {
        temp = null;
      } else {
        pickerView.setSelectedPosition(mSelectedPosition[i], false);
        OptionDataSet dataSet = temp.get(mSelectedPosition[i]);
        if (dataSet != null) {
          temp = dataSet.getSubs();
        }
      }
    }
  }

  /**
   * 设置数据和选中position
   *
   * @param selectedPosition 选中的下标
   * @deprecated 建议使用{@link #setDataWithValues(List, String...)}
   */
  public void setDataWithIndexs(List<? extends OptionDataSet> options, int... selectedPosition) {
    mOptions = options;
    setSelectedWithIndexs(selectedPosition);
  }

  /**
   * 设置选中的position
   *
   * @deprecated 建议使用{@link #setSelectedWithValues(String...)}
   */
  public void setSelectedWithIndexs(int... selectedPosition) {
    int length = selectedPosition.length;
    for (int i = 0; i < mHierarchy; i++) {
      // 默认值为0，reset中初始化数据如果不存在，会置为-1表示没有
      mSelectedPosition[i] = i < length ? selectedPosition[i] : 0;
    }
    reset();
  }

  /**
   * @return 数据集
   */
  public List<? extends OptionDataSet> getOptions() {
    return mOptions;
  }

  public int getHierarchy() {
    return mHierarchy;
  }

  /**
   * 获取选中的下标
   *
   * @return 选中的下标，数组size=mHierarchy，如果为-1表示该列没有数据
   */
  public int[] getSelectedPosition() {
    return mSelectedPosition;
  }

  @SuppressWarnings("unchecked") private void reset() {
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < getPickerViews().size(); i++) {
      PickerView pickerView = getPickerViews().get(i);
      ArrayWheelAdapter adapter = (ArrayWheelAdapter) pickerView.getAdapter();
      if (adapter == null || adapter.getData() != temp) {
        pickerView.setAdapter(new ArrayWheelAdapter<>(temp));
      }
      // 重置下标
      pickerView.setSelectedPosition(mSelectedPosition[i], false);
      if (temp == null || temp.size() == 0) {
        mSelectedPosition[i] = -1; // 下标置为-1表示选中的第i列没有
      } else if (temp.size() <= mSelectedPosition[i]) { // 下标超过范围，取默认值0
        mSelectedPosition[i] = 0;
      }
      if (mSelectedPosition[i] == -1) {
        temp = null;
      } else {
        OptionDataSet dataSet = temp.get(mSelectedPosition[i]);
        if (dataSet != null) {
          temp = dataSet.getSubs();
        }
      }
    }
  }

  /**
   * 获取选中的选项
   *
   * @return 选中的选项，如果指定index为null则表示该列没有数据
   */
  public OptionDataSet[] getSelectedOptions() {
    OptionDataSet[] optionDataSets = new OptionDataSet[mHierarchy];
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < mHierarchy; i++) {
      if (mSelectedPosition[i] == -1) break;
      // !=-1则一定会有数据，所以不需要判断temp是否为空，也不用担心会下标越界
      optionDataSets[i] = temp.get(mSelectedPosition[i]);
      temp = optionDataSets[i].getSubs();
    }
    return optionDataSets;
  }

  @Override protected void onConfirm() {
    if (mOnOptionSelectListener != null) {
      mOnOptionSelectListener.onOptionSelect(this, mSelectedPosition, getSelectedOptions());
    }
  }

  // 重置选中的position
  private void resetPosition(int index, int position) {
    for (int i = index; i < mSelectedPosition.length; i++) {
      mSelectedPosition[i] = i == index ? position : 0;
    }
  }

  @Override public void onSelected(BasePickerView pickerView, int position) {
    // 1联动2联动3...当前选中position，后面的都重置为0，更改mSelectedPosition,然后直接reset
    int index = (int) pickerView.getTag();
    resetPosition(index, position);
    reset();
  }

  @Override
  public CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence) {
    if (mFormatter == null) return charSequence;
    return mFormatter.format(this, (int) pickerView.getTag(), position, charSequence);
  }

  public static class Builder {
    private Context mContext;
    private Interceptor mInterceptor;
    private final int mHierarchy;
    private Formatter mFormatter;
    private OnOptionSelectListener mOnOptionSelectListener;

    /**
     * 强制设置的属性直接在构造方法中设置
     *
     * @param hierarchy 层级，有几层add几个pickerview
     * @param listener listener
     */
    public Builder(Context context, int hierarchy, OnOptionSelectListener listener) {
      mContext = context;
      mHierarchy = hierarchy;
      mOnOptionSelectListener = listener;
    }

    /**
     * 设置内容 Formatter
     *
     * @param formatter formatter
     */
    public Builder setFormatter(Formatter formatter) {
      mFormatter = formatter;
      return this;
    }

    /**
     * 设置拦截器
     *
     * @param interceptor 拦截器
     */
    public Builder setInterceptor(Interceptor interceptor) {
      mInterceptor = interceptor;
      return this;
    }

    public OptionPicker create() {
      OptionPicker picker = new OptionPicker(mContext, mHierarchy, mOnOptionSelectListener);
      picker.setFormatter(mFormatter);
      picker.setInterceptor(mInterceptor);
      picker.initPicker();
      return picker;
    }
  }

  public interface Formatter {
    /**
     * @param level 级别 0 ~ mHierarchy - 1
     * @param charSequence charSequence
     */
    CharSequence format(OptionPicker picker, int level, int position, CharSequence charSequence);
  }

  public interface OnOptionSelectListener {
    /**
     * @param selectedPosition length = mHierarchy。选中的下标:如果指定index为-1，表示当前选中的index列没有数据
     * @param selectedOptions length = mHierarchy。选中的选项，如果指定index为null则表示该列没有数据
     */
    void onOptionSelect(OptionPicker picker, int[] selectedPosition,
      OptionDataSet[] selectedOptions);
  }
}
