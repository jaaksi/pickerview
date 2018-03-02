package org.jaaksi.pickerview.topbar;

import android.view.View;
import android.widget.TextView;

/**
 * Created by fuchaoyang on 2018/2/16.<br/>
 * description：TopBar抽象接口
 */

public interface ITopBar {
  /**
   * @return topbar view
   */
  View getTopBarView();

  /**
   * @return 取消按钮view
   */
  View getBtnCancel();

  /**
   * @return 确定按钮view
   */
  View getBtnConfirm();

  /**
   * @return title view
   */
  TextView getTitleView();
}
