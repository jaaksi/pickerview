package org.jaaksi.pickerview.topbar;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.jaaksi.pickerview.R;
import org.jaaksi.pickerview.util.Util;

/**
 * Created by fuchaoyang on 2018/2/16.<br/>
 * description：默认提供的topbar handler
 */

public class DefaultTopBar implements ITopBar {
  private Context mContext;
  private View mTopBar, mDivider;
  private TextView mBtnCancel, mBtnConfirm, mTvTitle;

  public DefaultTopBar(@NonNull ViewGroup parent) {
    mContext = parent.getContext();
    mTopBar =
      LayoutInflater.from(mContext).inflate(R.layout.pickerview_topbar_default, parent, false);
    mDivider = mTopBar.findViewById(R.id.divider);
    mBtnCancel = mTopBar.findViewById(R.id.btn_cancel);
    mBtnConfirm = mTopBar.findViewById(R.id.btn_confirm);
    mTvTitle = mTopBar.findViewById(R.id.tv_title);
  }

  /**
   * 设置topbar bottom line color
   *
   * @param color color
   */
  public DefaultTopBar setDividerColor(@ColorInt int color) {
    mDivider.setBackgroundColor(color);
    return this;
  }

  /**
   * 设置bottom divider line height
   *
   * @param height dp
   */
  public DefaultTopBar setDividerHeight(int height) {
    mDivider.getLayoutParams().height = Util.dip2px(mContext, height);
    mDivider.requestLayout();
    return this;
  }

  public View getDivider() {
    return mDivider;
  }

  @Override public View getTopBarView() {
    return mTopBar;
  }

  @Override public TextView getBtnCancel() {
    return mBtnCancel;
  }

  @Override public TextView getBtnConfirm() {
    return mBtnConfirm;
  }

  /**
   * 获取TopBar的title view
   *
   * @return title
   */
  @Override public TextView getTitleView() {
    return mTvTitle;
  }
}
