package org.jaaksi.pickerview.dataset;

import java.util.List;
import org.jaaksi.pickerview.picker.OptionPicker;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：{@link OptionPicker}专用数据集
 */

public interface OptionDataSet extends PickerDataSet {

  /**
   * @return 下一级的数据集
   */
  List<? extends OptionDataSet> getSubs();
}
