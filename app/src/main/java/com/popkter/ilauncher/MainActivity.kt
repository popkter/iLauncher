package com.popkter.ilauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.popkter.ilauncher.common.IconAdapter
import com.popkter.ilauncher.common.IconTouchHelper
import com.popkter.ilauncher.data.AppsDataModel
import com.popkter.ilauncher.databinding.ActivityMainBinding
import java.util.*


class MainActivity : Activity() {
    companion object {
        const val TAG = "iLauncher"
    }

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var viewModel: AppsDataModel
    private lateinit var adapter: IconAdapter
    private var spanCount = MutableLiveData(8)


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        initData()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount.postValue(8)
        } else {
            spanCount.postValue(5)
        }
    }

    private fun initData() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(AppsDataModel::class.java)
        activityMainBinding.allMenu.setItemViewCacheSize(20)
        activityMainBinding.allMenu.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(
                this,
                R.anim.all_menu_load
            )
        ).apply {
            order = LayoutAnimationController.ORDER_NORMAL
            delay = .2F
        }
        adapter = IconAdapter(this, viewModel)
        activityMainBinding.allMenu.adapter = adapter
        val iconTouchHelper = IconTouchHelper(adapter)
        val itemTouchHelper = ItemTouchHelper(iconTouchHelper)
        itemTouchHelper.attachToRecyclerView(activityMainBinding.allMenu)

        spanCount.observeForever {
            activityMainBinding.allMenu.layoutManager = GridLayoutManager(this, it)
            //activityMainBinding.allMenu.layoutManager = StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL).apply { spanCount = it }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun loadApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
    }

    override fun onResume() {
        super.onResume()
        //viewModel.appsDataList.postValue(getAllAppNames())
        val list = loadApps()
        Collections.sort(list, ResolveInfo.DisplayNameComparator(packageManager))
        viewModel.appsDataResolveList.postValue(list)
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.finish()
    }

    override fun onBackPressed() {
        //
    }

    private fun getScreenOrientationLandscape(): Boolean {
        return (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }
}