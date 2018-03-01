package org.jaaksi.pickerview.topbar;

import android.view.View;

/**
 * Created by fuchaoyang on 2018/2/16.<br/>
 * description：TopBar抽象接口，不提供Title方法，但建议自定义的时候实现getTitleView方法
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

  //TextView getTitleView();
}
