package com.example.appfacialrec

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class ImageLoadTask(private val url: String, imageView: ImageView) : AsyncTask<Void?, Void?, Bitmap?>() {
    private val imageView: ImageView = imageView
    override fun doInBackground(vararg params: Void?): Bitmap? {
        try {
            val urlConnection = URL(url)
            val connection: HttpURLConnection = urlConnection
                    .openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        imageView.setImageBitmap(result)
    }


}