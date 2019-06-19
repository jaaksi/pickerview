package org.jaaksi.pickerview.picker;

import android.content.Context;
import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.picker.option.ForeignOptionDelegate;
import org.jaaksi.pickerview.picker.option.IOptionDelegate;
import org.jaaksi.pickerview.picker.option.OptionDelegate;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：多级别的的选项picker，支持联动，与非联动
 * 强大点：
 * 与https://github.com/Bigkoo/Android-PickerView对比
 * 1.支持设置层级
 * 2.构造数据源简单，只需要实现OptionDataSet接口
 * 3.支持联动及不联动
 * 4.支持通过选中的value设置选中项，内部处理选中项逻辑，避免用户麻烦的遍历处理
 */

public class OptionPicker extends BasePicker
  implements BasePickerView.OnSelectedListener, BasePickerView.Formatter {
  // 层级，有几层add几个pickerview
  private final int mHierarchy;
  // 选中的下标。如果为-1，表示当前选中的index列没有数据
  private final int[] mSelectedPosition;

  /** 是否无关连 */
  private boolean mIsForeign;
  private Formatter mFormatter;
  private OnOptionSelectListener mOnOptionSelectListener;

  private IOptionDelegate mDelegate;

  private OptionPicker(Context context, int hierarchy, OnOptionSelectListener listener) {
    super(context);
    mHierarchy = hierarchy;
    mOnOptionSelectListener = listener;
    mSelectedPosition = new int[mHierarchy];
  }

  private void initPicker() {
    for (int i = 0; i < mHierarchy; i++) {
      PickerView pickerView = createPickerView(i, 1);
      pickerView.setOnSelectedListener(this);
      pickerView.setFormatter(this);
    }
  }

  public void setFormatter(Formatter formatter) {
    mFormatter = formatter;
  }

  private void initForeign(boolean foreign) {
    mIsForeign = foreign;
    if (mIsForeign) { // 不关联的
      mDelegate = new ForeignOptionDelegate();
    } else {
      mDelegate = new OptionDelegate();
    }
    mDelegate.init(new Delegate() {
      @Override
      public int getHierarchy() {
        return mHierarchy;
      }

      @Override
      public int[] getSelectedPosition() {
        return mSelectedPosition;
      }

      @Override
      public List<PickerView> getPickerViews() {
        return OptionPicker.this.getPickerViews();
      }
    });
  }

  /**
   * 根据选中的values初始化选中的position并初始化pickerview数据
   *
   * @param options data
   */
  public void setData(List<? extends OptionDataSet>... options) {
    // 初始化是否关联
    initForeign(options.length > 1);
    mDelegate.setData(options);
  }

  /**
   * 根据选中的values初始化选中的position
   *
   * @param values 选中数据的value{@link OptionDataSet#getValue()}，如果values[0]==null，则进行默认选中，其他为null认为没有该列
   */
  public void setSelectedWithValues(String... values) {
    mDelegate.setSelectedWithValues(values);
  }

  public int getHierarchy() {
    return mHierarchy;
  }

  /**
   * 获取选中的下标
   *
   * @return 选中的下标，数组size=mHierarchy，如果为-1表示该列没有数据
   */
  public int[] getSelectedPosition() {
    return mSelectedPosition;
  }

  private void reset() {
    mDelegate.reset();
  }

  /**
   * 获取选中的选项
   *
   * @return 选中的选项，如果指定index为null则表示该列没有数据
   */
  public OptionDataSet[] getSelectedOptions() {
    return mDelegate.getSelectedOptions();
  }

  @Override
  protected void onConfirm() {
    if (mOnOptionSelectListener != null) {
      mOnOptionSelectListener.onOptionSelect(this, mSelectedPosition, getSelectedOptions());
    }
  }

  // 重置选中的position
  private void resetPosition(int index, int position) {
    for (int i = index; i < mSelectedPosition.length; i++) {
      if (i == index) {
        mSelectedPosition[i] = position;
      } else {
        if (!mIsForeign) {
          // 如果是无关的则不需要处理后面的index，关联的则直接重置为0
          mSelectedPosition[i] = 0;
        }
      }
    }
  }

  @Override
  public void onSelected(BasePickerView pickerView, int position) {
    // 1联动2联动3...当前选中position，后面的都重置为0，更改mSelectedPosition,然后直接reset
    int index = (int) pickerView.getTag();
    resetPosition(index, position);
    reset();
  }

  @Override
  public CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence) {
    if (mFormatter == null) return charSequence;
    return mFormatter.format(this, (int) pickerView.getTag(), position, charSequence);
  }

  public static class Builder {
    private Context mContext;
    private Interceptor mInterceptor;
    private final int mHierarchy;
    private Formatter mFormatter;
    private OnOptionSelectListener mOnOptionSelectListener;

    /**
     * 强制设置的属性直接在构造方法中设置
     *
     * @param hierarchy 层级，有几层add几个pickerview
     * @param listener listener
     */
    public Builder(Context context, int hierarchy, OnOptionSelectListener listener) {
      mContext = context;
      mHierarchy = hierarchy;
      mOnOptionSelectListener = listener;
    }

    /**
     * 设置内容 Formatter
     *
     * @param formatter formatter
     */
    public Builder setFormatter(Formatter formatter) {
      mFormatter = formatter;
      return this;
    }

    /**
     * 设置拦截器
     *
     * @param interceptor 拦截器
     */
    public Builder setInterceptor(Interceptor interceptor) {
      mInterceptor = interceptor;
      return this;
    }

    public OptionPicker create() {
      OptionPicker picker = new OptionPicker(mContext, mHierarchy, mOnOptionSelectListener);
      picker.setFormatter(mFormatter);
      picker.setInterceptor(mInterceptor);
      picker.initPicker();
      return picker;
    }
  }

  public interface Formatter {
    /**
     * @param level 级别 0 ~ mHierarchy - 1
     * @param charSequence charSequence
     */
    CharSequence format(OptionPicker picker, int level, int position, CharSequence charSequence);
  }

  public interface OnOptionSelectListener {
    /**
     * @param selectedPosition length = mHierarchy。选中的下标:如果指定index为-1，表示当前选中的index列没有数据
     * @param selectedOptions length = mHierarchy。选中的选项，如果指定index为null则表示该列没有数据
     */
    void onOptionSelect(OptionPicker picker, int[] selectedPosition,
      OptionDataSet[] selectedOptions);
  }

  public interface Delegate {
    int getHierarchy();

    int[] getSelectedPosition();

    List<PickerView> getPickerViews();
  }
}
