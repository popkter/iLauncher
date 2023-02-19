package com.popkter.ilauncher.common

import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.popkter.ilauncher.R
import com.popkter.ilauncher.data.AppsDataModel
import com.popkter.ilauncher.databinding.IconLayoutBinding
import java.util.*
import kotlin.math.max


class IconAdapter(
    private val context: Context, private val viewModel: AppsDataModel
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    companion object {
        const val TAG = "IconAdapter"
    }

    private val listener = Observer<List<PackageInfo>> { notifyItemChanged(max(itemCount - 1, 0)) }

    private val listenerResolveInfo = Observer<List<ResolveInfo>> {
        Log.e(TAG, "listenerResolveInfo fresh ")
        notifyItemChanged(max(itemCount - 1, 0))
    }

    init {
        viewModel.appsDataList.observeForever(listener)
        viewModel.appsDataResolveList.observeForever(listenerResolveInfo)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.e(TAG, "onAttachedToRecyclerView: ")
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: IconViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.clearAnimation()
        holder.itemView.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.all_menu_scroll
            )
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        Log.e(TAG, "onCreateViewHolder: ")
        return IconViewHolder(
            IconLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        Log.e(TAG, "onBindViewHolder: ")
        //initIconByPackageInfo(holder, position)
        initIconByResolveInfo(holder, position)
    }

    private fun initIconByResolveInfo(holder: IconViewHolder, position: Int) {
        Log.e(TAG, "initIconByResolveInfo: ")
        val activityInfo = viewModel.appsDataResolveList.value?.get(position)?.activityInfo
        val appName = activityInfo?.loadLabel(context.packageManager)
        val appIcon = activityInfo?.loadIcon(context.packageManager)
        holder.icon.setImageDrawable(appIcon)
        holder.title.text = appName
        Log.e(TAG, "initIconByResolveInfo: $appName")
        holder.icon.setOnClickListener {
            Log.e("ILauncher", "onBindViewHolder: onClick")
            if (activityInfo != null) {
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(
                        activityInfo.packageName, activityInfo.name
                    )
                    flags = Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                }
                context.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
                )
            }
        }
    }

    private fun initIconByPackageInfo(holder: IconAdapter.IconViewHolder, position: Int) {
        val packageInfo = viewModel.appsDataList.value?.get(position)

        val appName: String =
            packageInfo?.applicationInfo?.loadLabel(context.packageManager).toString()

        val drawable: Drawable? = packageInfo?.applicationInfo?.loadIcon(context.packageManager)

        holder.icon.background =
            drawable ?: (context as AppCompatActivity).getDrawable(R.drawable.icon_bg)
        holder.title.text = appName
        holder.icon.setOnClickListener {
            Log.e("ILauncher", "onBindViewHolder: onClick")
            if (packageInfo != null) {
                Log.e("ILauncher", "onBindViewHolder: ")
                packageInfo.packageName?.let {
                    val intent = context.packageManager.getLaunchIntentForPackage(it)
                    if (intent != null) {
                        intent.flags = Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    }
                    try {
                        context.startActivity(intent)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return viewModel.appsDataResolveList.value?.size ?: 0
    }

    fun finish() {
        //viewModel.appsDataList.removeObserver(listener)
        viewModel.appsDataResolveList.removeObserver(listenerResolveInfo)
    }

    fun itemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                viewModel.appsDataResolveList.value?.let { Collections.swap(it, i, i + 1) }
                notifyItemMoved(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                viewModel.appsDataResolveList.value?.let { Collections.swap(it, i, i - 1) }
                notifyItemMoved(i, i - 1)
            }
        }
    }


    class IconViewHolder(iconBinding: IconLayoutBinding) : ViewHolder(iconBinding.root) {
        val icon = iconBinding.icon
        val title = iconBinding.title
    }
}