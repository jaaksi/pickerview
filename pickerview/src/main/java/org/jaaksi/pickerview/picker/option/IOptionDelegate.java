package org.jaaksi.pickerview.picker.option;

import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.picker.OptionPicker;

/**
 * Created by fuchaoyang on 2018/7/6.<br/>
 * description：
 */

public interface IOptionDelegate {
  //void init(int hierarchy, List<PickerView> pickerViews, int[] selectedPosition);
  void init(OptionPicker.Delegate delegate);

  void setData(List<? extends OptionDataSet>... options);

  void setSelectedWithValues(String... values);

  OptionDataSet[] getSelectedOptions();

  void reset();
}
