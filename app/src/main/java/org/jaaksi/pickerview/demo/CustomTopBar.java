package org.jaaksi.pickerview.demo;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.jaaksi.pickerview.topbar.ITopBar;
import org.jaaksi.pickerview.util.Util;

/**
 * Created by fuchaoyang on 2018/2/16.<br/>
 * description：自定义topbar
 */

public class CustomTopBar implements ITopBar {
  private Context mContext;
  private View mTopBar, mDivider;
  private ImageView mBtnCancel;
  private TextView mBtnConfirm, mTvTitle;

  public CustomTopBar(ViewGroup parent) {
    mContext = parent.getContext();
    mTopBar =
      LayoutInflater.from(mContext).inflate(R.layout.pickerview_topbar_custom, parent, false);
    mDivider = mTopBar.findViewById(R.id.divider);
    mBtnCancel = mTopBar.findViewById(R.id.btn_cancel);
    mBtnConfirm = mTopBar.findViewById(R.id.btn_confirm);
    mTvTitle = mTopBar.findViewById(R.id.tv_title);
  }

  /**
   * 设置bottom divider line color
   *
   * @param color linecolor
   */
  public CustomTopBar setDividerColor(@ColorInt int color) {
    mDivider.setBackgroundColor(color);
    return this;
  }

  /**
   * 设置bottom divider line height
   *
   * @param height dp
   */
  public CustomTopBar setDividerHeight(float height) {
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

  @Override public ImageView getBtnCancel() {
    return mBtnCancel;
  }

  @Override public TextView getBtnConfirm() {
    return mBtnConfirm;
  }

  @Override public TextView getTitleView() {
    return mTvTitle;
  }
}
