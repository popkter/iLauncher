package com.popkter.ilauncher.data

import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppsDataModel : ViewModel() {

    val appsDataList = MutableLiveData<List<PackageInfo>>()

    val appsDataResolveList = MutableLiveData<List<ResolveInfo>>()
}