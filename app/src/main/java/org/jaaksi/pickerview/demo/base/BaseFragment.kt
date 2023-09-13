package org.jaaksi.pickerview.demo.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

open class BaseFragment<T : ViewBinding> : Fragment() {
    lateinit var binding: T
    private var hasLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val type = javaClass.genericSuperclass as ParameterizedType
        val aClass = type.actualTypeArguments[0] as Class<*>
        val method = aClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        binding = method.invoke(null, layoutInflater, container, false) as T
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData(savedInstanceState)
        initView(savedInstanceState)
        binding.root.setBackgroundColor(Color.WHITE)
        binding.root.isClickable = true
        observer()
    }

    open fun initData(savedInstanceState: Bundle?) {}
    open fun initView(savedInstanceState: Bundle?) {}
    open fun observer() {}

    override fun onResume() {
        super.onResume()
//        if (!isHidden) {
            onVisibleChanged(true, !hasLoaded)
            hasLoaded = true
//        }

    }

    override fun onPause() {
        super.onPause()
        onVisibleChanged(visible = false, isLazy = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasLoaded = false
    }

    /**
     * ViewPager和ViewPager2中都使用了setMaxLifecycle，onResume和onPause就是可见/不可见
     */
    open fun onVisibleChanged(visible: Boolean, isLazy: Boolean) {}

}