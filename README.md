# One very very user-friendly Picker library
一个非常好用的Android PickerView库，内部提供2种常用类型的Picker。支持扩展自定义Picker。支持自定义弹窗。支持作为view的非弹窗场景。
* TimePicker：时间选择器，支持聚合模式（合并v1.x的MixedTimePicker），支持12小时制（上下午）。
* OptionPicker：联动选择器

## Screenshot
![](https://github.com/jaaksi/pickerview/blob/master/docs/TimePicker.png)
![](https://github.com/jaaksi/pickerview/blob/master/docs/MixedTimePicker.png)
![](https://github.com/jaaksi/pickerview/blob/master/docs/OptionPicker.png)

## APK
[Demo App](https://github.com/jaaksi/pickerview/blob/master/docs/app-debug.apk)下载连接

### [PickerView README](https://github.com/jaaksi/pickerview/blob/master/README_PickerView.md)

## Picker
通过组装PickerView实现常用的Picker选择器。上面已经列举提供的3中常用的Picker。

## BasePicker
Picker基类：封装PickerView容器，create and add PickerView方法，Picker弹窗等方法。
三种Picker都继承自BasePicker，你也可以继承它扩展自己的Picker。

### API
|api|description|
---|---
| setPickerBackgroundColor | 设置picker背景
| setPadding | 设置PickerView父容器padding 单位:px
| setTag | 给Picker 设置tag，用于区分不同的picker等。用法同View setTag
| setInterceptor | 设置拦截器
| createPickerView | 创建PickerView
| getPickerViews | 获取Picker中所有的pickerview集合
| addPicker | 将创建的PickerView 添加到上面集合中，createPickerView内部已调用该方法
| findPickerViewByTag | 通过tag找到对应的PickerView
| canSelected | 是否可以Picker的取消，确定按键
| dialog | 获取Picker弹窗接口，用于设置title等
| view | 获取picker的view，用于非弹窗的场景
| show | 显示picker弹窗

### Interceptor
拦截器：用于在pickerview创建时拦截，设置pickerview的属性。
>Picker内部并不提供对PickerView的设置方法，而是通过Interceptor实现。这种设计用来实现Picker和PickerView的属性设置完美解耦。
```java
   private void init(){
    mTimePicker.setInterceptor(new BasePicker.Interceptor() {
      @Override public void intercept(PickerView pickerView, LinearLayout.LayoutParams params) {
        pickerView.setVisibleItemCount(5);
        // 将年月设置为循环的
        int type = (int) pickerView.getTag();
        if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
          pickerView.setIsCirculation(true);
        }
      }
    })
   }
```
这一点对比 [Android-PickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/TimePickerView.java), 每个Picker都需要声明对PickerView的设置方法，与PickerView严重耦合。需要开发者copy大量重复代码，且无法区分每一个PickerView设置不同的属性。

## TimePicker
常用的时间选择器，支持 年、月、日、时、分，支持聚合（1.x 的MixTimePicker）,支持12小时制（上下午）
  * 时间类型type的设计：自由组合、随心所欲(当然应该是有意义的)
  ```
    TYPE_YEAR | TYPE_MONTH | TYPE_DAY | TYPE_12_HOUR | TYPE_HOUR | TYPE_MINUTE
    
    TYPE_MIXED_DATE | TYPE_MIXED_TIME
  ``` 
对比 Android-PickerView [TimePickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/TimePickerView.java)
  ```java
    /**
    * Android-PickerView中的设置type方法：参数设置麻烦且不易理解
    * 长度必须为6的数组，表示年月日时分秒 的显示与否，不设置则默认全部显示
    */
    setType(boolean[] type)
    
    // 本项目设置type方法：简单易懂，组合方便
    setType(TYPE_DATE | TYPE_HOUR)
  ```
  * 完美支持时间区间设置以及选中联动
  * 支持支持自定义日期、时间格式（Format），如显示今年，明年
  * 支持混合模式，支持日期，时间混合
  * 支持设置时间间隔，如30分钟
  * 支持12小时制（上下午）

### API
|api|description|
---|---
| type | 时间类型，需要在Builder构造方法中指定，不能改变
| OnTimeSelectListener | 选中时间回调，需要在Builder构造方法中指定，不能改变
| setRangDate | 设置起止时间
| setSelectedDate | 设置选中时间戳
| setInterceptor | 设置拦截器
| setFormatter | 设置Formatter，内部提供默认的Formatter
| create | 通过Builder构建 TimePicker
| |以上是TimePicker.Builder的，下面是TimePicker的
| setFormatter | 同上
| setSelectedDate | 同上
| getType | 获取type
| hasType | 判断是否包含某种type

### Formatter
TimePicker Formatter：用于根据type和num格式化时间文案
  ```java
    public interface Formatter {
        /**
         * 根据type和num格式化时间
         *
         * @param picker picker
         * @param type 并不是模式，而是当前item所属的type，如年，时
         * @param position position
         * @param value position item对应的value，如果是TYPE_MIXED_DATE表示日期时间戳，否则表示显示的数字
         */
        CharSequence format(TimePicker picker, int type, int position,
          long value);
      }
  ``` 
内部提供默认的 Formatter实现DefaultFormatter。用户可以设置自定义Formatter或继承DefaultFormatter进行扩展。

> *TimePicker初始化，如果未设置时间区间，会使用默认区间。三种Picker都采用Builder模式初始化。且用户自定义的Picker也应该采用这种模式进行初始化。*

### Simple Example
```java
    mTimePicker = new TimePicker.Builder(mActivity, type, this)
      // 设置时间区间
      .setRangDate(1526361240000L, 1893563460000L)
      // 设置选中时间
      //.setSelectedDate()
      // 设置pickerview样式
      .setInterceptor(new BasePicker.Interceptor() {
        @Override public void intercept(PickerView pickerView, LinearLayout.LayoutParams params) {
          pickerView.setVisibleItemCount(5);
          // 将年月设置为循环的
          int type = (int) pickerView.getTag();
          if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
            pickerView.setIsCirculation(true);
          }
        }
      })
      // 设置 Formatter
      .setFormatter(new TimePicker.DefaultFormatter() {
        // 自定义Formatter显示去年，今年，明年
        @Override
        public CharSequence format(TimePicker picker, int type, int position, long num) {
          if (type == TimePicker.TYPE_YEAR) {
            long offset = num - mCurrYear;
            if (offset == -1) return "去年";
            if (offset == 0) return "今年";
            if (offset == 1) return "明年";
            return num + "年";
          } else if (type == TimePicker.TYPE_MONTH) {
            return String.format("%d月", num);
          }

          return super.format(picker, type, position, num);
        }
      }).create();
      PickerDialog dialog = (PickerDialog) mTimePicker.dialog();
      dialog.getTitleView().setText("请选择时间");

    //mTimePicker.setSelectedDate(1549349843000L);
    mTimePicker.show();
```

## ~~MixedTimePicker~~
1.x 版本，2.x后已经与TimePicker合并。

## OptionPicker
  * 支持设置层级
  * 支持联动和不联动（2.x版本）
  * 构造数据源及其简单，只需要实现OptionDataSet接口
  * 支持通过对应选中的values设置选中项。内部处理选中项逻辑，避免用户记录下标且麻烦的遍历处理

> 对比 Android-PickerView的 [OptionsPickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/OptionsPickerView.java)

function | Android-PickerViews | 本控件
---|---|---
多级 | 最多支持3级(写死的) | 构造时设置级别(无限制)
构造数据源 | 需要构建每一级的集合，二三级为嵌套 | 一级数据entity实现OptionDataSet接口即可
设置数据源 | 提供三个方法，分别用于一、二、三级的 | 只需要设置一级数据集
联动选中 | 提供三个，只能设置选中的下标。<br/>
需要用户自己通过多层遍历定位每一级别选中的下标，然后再设置 | 只需要传入选中的values(可变长数组)，不需要任何计算

Android-PickerView 中的 OptionsPickerView 代码。由于不知道层级，所以每个方法都提供3个用来对应（最多）3级选择。
```java
    // 提供3个选中的方法，分别对应1,2,3级联动的情况
    public void setSelectOptions(int option1)

    public void setSelectOptions(int option1, int option2)

    public void setSelectOptions(int option1, int option2, int option3)

    // 提供3个设置数据源的方法，分别对应1,2,3级联动的情况
    public void setPicker(List<T> optionsItems)

    public void setPicker(List<T> options1Items, List<List<T>> options2Items)

    public void setPicker(List<T> options1Items, List<List<T>> options2Items,
          List<List<List<T>>> options3Items) {
    }
```

本库中的OptionPicker（2.x）
```java
/**
   * 根据选中的values初始化选中的position
   *
   * @param values 选中数据的value{@link OptionDataSet#getValue()}，如果values[0]==null，则进行默认选中，其他为null认为没有该列
   */
  public void setSelectedWithValues(String... values) {
    mDelegate.setSelectedWithValues(values);
  }
```
> 如上面对比表格中所列举的，无论是层级，构造数据源和设置数据源，还是设置选中的选项，本库的API都十分简单，方便。

### API
|api|description|
---|---
| mHierarchy | 层级，需要在Builder构造方法中指定，不能改变
| OnOptionSelectListener | 选中回调，需要在Builder构造方法中指定，不能改变
| setInterceptor | 设置拦截器
| setFormatter | 设置Formatter
| create | 通过Builder构建 OptionPicker
| |以上是OptionPicker.Builder的，下面是OptionPicker的
| setFormatter | 同上
| setData | 初始化pickerview数据。
| setSelectedWithValues | 根据选中的values初始化选中的position
| getOptions | 获取数据集
| getSelectedPosition | 获取选中的下标，数组size=mHierarchy，如果为-1表示该列没有数据
| getSelectedOptions | 获取选中的选项，如果指定index为null则表示该列没有数据

> 需要注意的是：本库中的OptionPicker只用于联动的，不支持多级别且不联动。
基本没有这种需求，如果大家有这种需求，我会在后续迭代中支持。

## Others
> 奇葩设计：部分default属性声明为static而非final

### 全局设置default属性
奇葩也好，亮点也罢。作为一个UI控件，不同的app，不同的UI，不同的产品自然会有不同的样式。
考虑到在一个app中我们会用到很多Picker，而我们又需要定制自己的UI的样式，如果通过动态方法设置样式就太麻烦了。
故做此设计。你可以通过配置这些static变量来快速定制一个满足自己app样式需求的Picker。
当然你也可以通过封装方法来处理PickerView，Picker,装饰器等样式，但这样一样十分麻烦。我相信你自己都会烦。

### 静态默认值
> *所有的这些静态属性值都以 sDefault 开头*

* BasePickerView

field|description|defaultValue
---|---|---
sDefaultVisibleItemCount | 默认可见的item个数 | 5
sDefaultItemSize | 默认itemSize | 50(dp)
sDefaultIsCirculation | 默认是否循环 | false

* PickerView

field|description|defaultValue
---|---|---
sOutTextSize | default out text size | 18(dp)
sCenterTextSize | default center text size | 22(dp)
sCenterColor | default center text color | Color.BLUE
sOutColor | default out text color | Color.GRAY

* BasePicker

field|description|defaultValue
---|---|---
sDefaultPaddingRect | pickerView父容器的 default padding | null(无padding)
sDefaultPickerBackgroundColor | default picker background color | Color.WHITE
sDefaultDialogCreator | 用于构建自定义Dialog的接口 | null

* DefaultCenterDecoration

field|description|defaultValue
---|---|---
sDefaultLineColor | default line color | Color.BLUE
sDefaultLineWidth | default line width | 1(dp)
sDefaultDrawable | default item background drawable | null
sDefaultMarginRect | default line margin | null(无margin)


> **建议初始化这些属性值放到Application中完成，避免app发生crash而导致失效**

### Simple Example
```java
public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    // 建议在application中初始化picker 默认属性实现全局设置
    initDefaultPicker();
  }

  private void initDefaultPicker() {
    // 利用修改静态默认属性值，快速定制一套满足自己app样式需求的Picker.
    // BasePickerView
    PickerView.sDefaultVisibleItemCount = 3;
    PickerView.sDefaultItemSize = 50;
    PickerView.sDefaultIsCirculation = true;

    // PickerView
    PickerView.sOutTextSize = 18;
    PickerView.sCenterTextSize = 18;
    PickerView.sCenterColor = Color.RED;
    PickerView.sOutColor = Color.GRAY;

    // BasePicker
    int padding = Util.dip2px(this, 20);
    BasePicker.sDefaultPaddingRect = new Rect(padding, padding, padding, padding);
    BasePicker.sDefaultPickerBackgroundColor = Color.WHITE;
    // 自定义弹窗
    BasePicker.sDefaultDialogCreator = new IGlobalDialogCreator() {
      @Override
      public IPickerDialog create(Context context) {
        return new PickerDialog();
      }
    };

    // DefaultCenterDecoration
    DefaultCenterDecoration.sDefaultLineWidth = 1;
    DefaultCenterDecoration.sDefaultLineColor = Color.RED;
    //DefaultCenterDecoration.sDefaultDrawable = new ColorDrawable(Color.WHITE);
    int leftMargin = Util.dip2px(this, 10);
    int topMargin = Util.dip2px(this, 2);
    DefaultCenterDecoration.sDefaultMarginRect =
      new Rect(leftMargin, -topMargin, leftMargin, -topMargin);
  }
}
```

## Change Log
  > v3.0.2(2020-10-27)
  - fix#32 setShadowsColors() 屏蔽阴影 无效

  > v3.0.1
  - 修复startTime > endTime时造成数据混乱

  > v3.0.0(2019-09-02)
  - release v3.0.0
  - 支持自定义弹窗
  - 支持费弹窗样式，作为view的形式
  - 支持12小时制（上下午）

  > v2.0.3(2019-09-02)
  - release v2.0.3
  - 修复滑动但不点击确定键，关闭弹窗后，再次点击无法回显

  > v2.0.1(2019-04-23)
  - release v2.0.1
  - 修复OptionDelegate遍历不存在value错误引起的bug

  > v2.0.0(2019-03-25)
  - release v2.0.0
  - 合并MixedTimePicker和TimePicker，更强大
  _ OptionPicker支持数据不联动
  - 添加蒙版遮罩
  - 修复fling时可能会导致onSelect不回调等部分小bug
  - 优化部分小细节

  > v1.0.0(2018-03-03)
  - release v1.0.0

  > v1.0.1(2018-04-13)
  - 修复MixedTimePicker选中时日期未更改
  - 修复部分手机Picker弹窗无法居底部

## Gradle
```java
    compile 'org.jaaksi:pickerview:3.0.2'
```

## Thanks
*   [ScrollPickerView](https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/ScrollPickerView.java)
*   [Android-PickerView](https://github.com/Bigkoo/Android-PickerView)
