package org.jaaksi.pickerview.dataset;

import java.util.List;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：
 */

public interface OptionDataSet extends PickerDataSet {

  /**
   * @return 下一级
   */
  List<? extends OptionDataSet> getSubs();

  // 上传的value，用于匹配初始化选中的下标
  String getValue();
}
