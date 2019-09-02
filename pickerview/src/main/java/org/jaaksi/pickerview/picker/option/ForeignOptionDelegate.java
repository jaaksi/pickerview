package org.jaaksi.pickerview.picker.option;

import java.util.List;
import org.jaaksi.pickerview.adapter.ArrayWheelAdapter;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.picker.OptionPicker;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/7/6.<br/>
 * description：无关联的 OptionPicker Delegate
 */

public class ForeignOptionDelegate implements IOptionDelegate {
  private OptionPicker.Delegate mDelegate;
  private List<? extends OptionDataSet>[] mOptions;

  @Override
  public void init(OptionPicker.Delegate delegate) {
    mDelegate = delegate;
  }

  @Override
  public void setData(List<? extends OptionDataSet>[] options) {
    mOptions = options;
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      PickerView pickerView = mDelegate.getPickerViews().get(i);
      pickerView.setAdapter(new ArrayWheelAdapter<>(mOptions[i]));
    }
  }

  @Override
  public void setSelectedWithValues(String... values) {
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      if (mOptions == null || mOptions.length == 0) { // 数据源无效
        mDelegate.getSelectedPosition()[i] = -1;
      } else if (values.length <= i || values[i] == null) { // 选中默认项0...
        mDelegate.getSelectedPosition()[i] = 0;
      } else {
        List<? extends OptionDataSet> options = mOptions[i];
        for (int j = 0; j <= options.size(); j++) {
          // 遍历找到选中的下标，如果没有找到，则将下标置为0
          if (j == options.size()) {
            mDelegate.getSelectedPosition()[i] = 0;
            break;
          }

          if (values[i].equals(options.get(j).getValue())) {
            mDelegate.getSelectedPosition()[i] = j;
            break;
          }
        }
      }

      if (mDelegate.getSelectedPosition()[i] != -1) {
        mDelegate.getPickerViews().get(i)
          .setSelectedPosition(mDelegate.getSelectedPosition()[i], false);
      }
    }
  }

  @Override
  public OptionDataSet[] getSelectedOptions() {
    OptionDataSet[] optionDataSets = new OptionDataSet[mDelegate.getHierarchy()];
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      int selectedPosition = mDelegate.getSelectedPosition()[i];
      if (selectedPosition == -1) break;
      optionDataSets[i] = mOptions[i].get(selectedPosition);
    }
    return optionDataSets;
  }

  @Override
  public void reset() {
    for (int i = 0; i < mDelegate.getHierarchy(); i++) {
      PickerView pickerView = mDelegate.getPickerViews().get(i);
      pickerView.setSelectedPosition(mDelegate.getSelectedPosition()[i],false);
    }
  }
}
