package org.jaaksi.pickerview.picker.option;

import java.util.List;
import org.jaaksi.pickerview.adapter.ArrayWheelAdapter;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.picker.OptionPicker;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/7/6.<br/>
 * description：关联的Option Picker Delegate
 */

public class OptionDelegate implements IOptionDelegate {
  //private int mHierarchy;
  //private int[] mDelegate.getSelectedPosition();
  //private List<PickerView> mPickerViews;
  private OptionPicker.Delegate mDelegate;
  private List<? extends OptionDataSet> mOptions;

  //@Override public void init(int hierarchy, List<PickerView> pickerViews, int[] selectedPosition) {
  //  mHierarchy = hierarchy;
  //  mPickerViews = pickerViews;
  //  mDelegate.getSelectedPosition() = selectedPosition;
  //}

  @Override public void init(OptionPicker.Delegate delegate) {
    mDelegate = delegate;
  }

  @Override public void setData(List<? extends OptionDataSet>... options) {
    mOptions = options[0];
    setSelectedWithValues();
  }

  /**
   * 根据选中的values初始化选中的position
   *
   * @param values 选中数据的value{@link OptionDataSet#getValue()}，如果values[i]==null，如果该列有数据，则进行默认选中，否则认为没有该列
   */
  @SuppressWarnings("unchecked") public void setSelectedWithValues(String... values) {
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      PickerView pickerView = mDelegate.getPickerViews().get(i);
      ArrayWheelAdapter adapter = (ArrayWheelAdapter) pickerView.getAdapter();
      if (adapter == null || adapter.getData() != temp) {
        pickerView.setAdapter(new ArrayWheelAdapter<>(temp));
      }
      if (temp == null || temp.size() == 0) { // 数据源无效
        mDelegate.getSelectedPosition()[i] = -1;
      } else if (values.length <= i || values[i] == null) { // 选中默认项0...
        mDelegate.getSelectedPosition()[i] = 0;
      } else { // 遍历找到选中的下标，如果没有找到，则将下标置为0
        for (int j = 0; j < temp.size(); j++) {
          OptionDataSet dataSet = temp.get(j);
          if (dataSet != null) {
            if (values[i].equals(dataSet.getValue())) {
              mDelegate.getSelectedPosition()[i] = j;
              break;
            }
            if (j == temp.size()) {
              mDelegate.getSelectedPosition()[i] = 0;
            }
          }
        }
      }

      if (mDelegate.getSelectedPosition()[i] == -1) {
        temp = null;
      } else {
        pickerView.setSelectedPosition(mDelegate.getSelectedPosition()[i], false);
        OptionDataSet dataSet = temp.get(mDelegate.getSelectedPosition()[i]);
        if (dataSet != null) {
          temp = dataSet.getSubs();
        }
      }
    }
  }

  @SuppressWarnings("unchecked") public void reset() {
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < mDelegate.getPickerViews().size(); i++) {
      PickerView pickerView = mDelegate.getPickerViews().get(i);
      ArrayWheelAdapter adapter = (ArrayWheelAdapter) pickerView.getAdapter();
      if (adapter == null || adapter.getData() != temp) {
        pickerView.setAdapter(new ArrayWheelAdapter<>(temp));
      }
      // 重置下标
      pickerView.setSelectedPosition(mDelegate.getSelectedPosition()[i], false);
      if (temp == null || temp.size() == 0) {
        mDelegate.getSelectedPosition()[i] = -1; // 下标置为-1表示选中的第i列没有
      } else if (temp.size() <= mDelegate.getSelectedPosition()[i]) { // 下标超过范围，取默认值0
        mDelegate.getSelectedPosition()[i] = 0;
      }
      if (mDelegate.getSelectedPosition()[i] == -1) {
        temp = null;
      } else {
        OptionDataSet dataSet = temp.get(mDelegate.getSelectedPosition()[i]);
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
    OptionDataSet[] optionDataSets = new OptionDataSet[mDelegate.getHierarchy()];
    List<? extends OptionDataSet> temp = mOptions;
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      if (mDelegate.getSelectedPosition()[i] == -1) break;
      // !=-1则一定会有数据，所以不需要判断temp是否为空，也不用担心会下标越界
      optionDataSets[i] = temp.get(mDelegate.getSelectedPosition()[i]);
      temp = optionDataSets[i].getSubs();
    }
    return optionDataSets;
  }
}
