package org.jaaksi.pickerview.topbar;

import android.view.View;

/**
 * Created by fuchaoyang on 2018/2/16.<br/>
 * description：
 */

public interface ITopBar {
  View getTopBarView();

  View getBtnCancel();

  View getBtnConfirm();

  View getTitleView();
}
