package com.location.locationutills

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.github.nitrico.lastadapter.LastAdapter
import com.location.locationutills.databinding.ActivityLastAdapterBinding
import com.location.locationutills.databinding.RowDataBinding

/**
 * Created by Android on 3/6/2018.
 */
class LastAdapterActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityLastAdapterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@LastAdapterActivity, R.layout.activity_last_adapter)
        mBinding.rvData.layoutManager = LinearLayoutManager(this)
        setAdapter()
    }

    fun setAdapter() {

        val list = arrayListOf<ListItem>(ListItem("name"), ListItem("email"))
        LastAdapter(list, BR.item)
                .map<ListItem, RowDataBinding>(R.layout.row_data)
                .into(mBinding.rvData)
    }
}

data class ListItem(val name: String)



