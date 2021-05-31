package com.example.appfacialrec

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class FaceRecordAdapter(private val context: Context, private val dataSource: Queue<FaceRecord>, private val token: String)
    : RecyclerView.Adapter<FaceRecordHolder>() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceRecordHolder {
        val view = inflater.inflate(viewType,parent,false)
        return FaceRecordHolder(view)
    }

    override fun onBindViewHolder(holder: FaceRecordHolder, position: Int) {

        holder.bindData(dataSource.elementAt(position), context,token)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_face_record
    }
}