package com.example.appfacialrec

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.MqUtil
import com.google.gson.Gson
import com.rabbitmq.client.ConnectionFactory
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val faceRecordList= LinkedList<FaceRecord>()
    private lateinit var linearLayoutManager: LinearLayoutManager;
    private lateinit var faceRecordAdapter:FaceRecordAdapter


    //Set the notification bar message style
    private fun setNotification(msg: String?) {
        //String title = msg.split("\\;")[0];
        //String content = msg.split("\\;")[1];
        val i = 0
        //Click on the notification bar to jump to the page
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        //Create notification message management class
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //The high version requires channels, many people can't pop up the message notification, just because this is not added
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Only channels are needed on Android O
            val notificationChannel = NotificationChannel(
                "channelid1",
                "channelname",
                NotificationManager.IMPORTANCE_HIGH
            )
            //If you use IMPORTANCE_NOENE here, you need to open the channel in the system settings for the notification to pop up normally
            manager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(this, "channelid1") //Create notification message instance
                .setContentTitle("Intruder")
                .setContentText("Watch Out!!")
                .setWhen(System.currentTimeMillis()) //The notification bar shows the time
                .setSmallIcon(R.mipmap.ic_launcher) //Small icon in the notification bar
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)) //The notification bar drop-down is an icon
                .setContentIntent(pendingIntent) //Click the notification bar to jump to the page
                .setPriority(NotificationCompat.PRIORITY_MAX) //Set the notification message priority
                .setAutoCancel(true) //Set the notification message to disappear automatically after clicking the notification bar message
                // .setSound(Uri.fromFile(new File("/system/MP3/music.mp3"))) // notification bar message prompt tone
                .setVibrate(longArrayOf(0, 1000, 1000, 1000)) // Notification bar message vibration
                .setLights(Color.GREEN, 1000, 2000) //Notice bar message flashes (lights for one second and then lights for two seconds)
                .setDefaults(NotificationCompat.DEFAULT_ALL) //The notification bar notification sound, vibration, flashing lights, etc. are set as default
        //Short text
        val notification = builder.build()
        //Constant.TYPE1 is the message identifier of the notification bar, each id is different
        manager.notify(i, notification)
    }

    val incomingMessageHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val message = msg.data.getString("msg")
            setNotification(message)
        }
    }

    fun fetchPeople(linearLayoutManager: LinearLayoutManager){
        val parentFolder="1mFZQ-pYeVD85tHD1Xp3ai3NJwyj-D1OF"
        val url = "https://accounts.google.com/o/oauth2/token"
        val clientId = "95743473574-u8ij971ddpkln501mp9oph6oho1iuo4i.apps.googleusercontent.com"
        val clientSecret = "xBKQqq2RUsEi4oZRa1uqahCU"
        val refreshToken = "1//040DRovhy4m8SCgYIARAAGAQSNwF-L9IrMe_L2iSoXszc8orsrlnOb5CwmapcMLpAyyI9ojbpe2kpBg2Ulrfm-Aj0_WCTU3_ZjbI"

        val queue = Volley.newRequestQueue(this)

        val urlFetch = "https://www.googleapis.com/drive/v2/files/$parentFolder/children"

        val params = HashMap<String?, String?>()
        params["client_id"] = clientId
        params["client_secret"] = clientSecret
        params["refresh_token"] = refreshToken
        params["grant_type"] = "refresh_token"





        val request_json = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>),
            {response->
                try {

                    val accessToken = response.getString("access_token")

                    val jsonReqFetch = object : JsonObjectRequest(Method.GET, urlFetch, null,
                        Response.Listener<JSONObject> { responseFetch ->
                            val images = responseFetch.getJSONArray("items")
                            for(i in 0 until images.length()){
                                val image = images.getJSONObject(i)
                                val imageLink = "https://www.googleapis.com/drive/v2/files/${image.getString("id")}"+"?alt=media&source=downloadUrl"

                                val urlFetchDate = "https://www.googleapis.com/drive/v2/files/${image.getString("id")}"

                                val jsonReqFetchDate = object : JsonObjectRequest(Method.GET, urlFetchDate, null,
                                        Response.Listener<JSONObject> { responseFetch ->
                                            val fileDate = responseFetch.getString("createdDate")
                                            Log.d("image_link",imageLink)
                                            val faceRecord =FaceRecord(imageLink,fileDate)
                                            faceRecordList.add(faceRecord)
                                            faceRecordAdapter = FaceRecordAdapter(this,faceRecordList,accessToken)
                                            recyclerView = findViewById(R.id.recycler_view)
                                            recyclerView.layoutManager = linearLayoutManager
                                            recyclerView.adapter = faceRecordAdapter

                                        },
                                        Response.ErrorListener { error ->
                                        }) {
                                    @Throws(AuthFailureError::class)
                                    override fun getHeaders(): Map<String, String> {
                                        val headers = HashMap<String, String>()
                                        headers.put("Authorization", "Bearer $accessToken")
                                        return headers
                                    }
                                }
                                queue.add(jsonReqFetchDate)

                            }

                        },
                        Response.ErrorListener { error ->
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            headers.put("Authorization", "Bearer $accessToken")
                            return headers
                        }
                    }

                    queue.add(jsonReqFetch)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) { error -> VolleyLog.e("Error: ", error.toString()) }

        queue.add(request_json)



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        linearLayoutManager = LinearLayoutManager(this)
        factory = ConnectionFactory()
        //The following parameters should be changed according to the actual
        val uri = "amqps://vdwwsmtz:8t1_gM675-qlXqIZ70C4lA5gQyZTTnpR@crow.rmq.cloudamqp.com/vdwwsmtz"
        try {
            factory!!.setAutomaticRecoveryEnabled(false)
            factory!!.setUri(uri)
        } catch (e1: KeyManagementException) {
            e1.printStackTrace()
        } catch (e1: NoSuchAlgorithmException) {
            e1.printStackTrace()
        } catch (e1: URISyntaxException) {
            e1.printStackTrace()
        }

        fetchPeople(linearLayoutManager)
        //Open the consumer thread
        MqUtil.subscribe(incomingMessageHandler, factory!!)

    }

    companion object {
        private var factory: ConnectionFactory? = null
    }
}