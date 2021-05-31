package com.example.appfacialrec

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request


class FaceRecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image:ImageView = itemView.findViewById(R.id.imageView)
    private val date:TextView = itemView.findViewById(R.id.textView)

    fun authorizeImageDownload(context: Context, token: String): Picasso? {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        return Picasso.Builder(context)
            .downloader(OkHttp3Downloader(client))
            .build()
    }

    fun bindData(data: FaceRecord, context: Context, token: String){
        authorizeImageDownload(context, token)?.load(data.imageURL)?.into(image)
        date.text = data.date
    }
}