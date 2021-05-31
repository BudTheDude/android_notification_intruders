package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.rabbitmq.client.*
import java.io.IOException

object MqUtil {
    var subscribeThread: Thread? = null
    private var mMessage: String? = null
    private const val EXCHANGE = "orderInfo33"
    fun subscribe(handler: Handler, factory: ConnectionFactory) {
        subscribeThread = Thread {
            //                while (true) {
            try {
                //Use the previous settings to establish a connection
                val connection: Connection = factory.newConnection()
                //Create a channel
                val channel: Channel = connection.createChannel()
                //Only send one at a time, get one after processing one
                channel.basicQos(1)
                channel.exchangeDeclare(
                    EXCHANGE,
                    "direct",
                    true
                ) //Here we should change according to the actual
                channel.queueBind(
                    "hello",
                    EXCHANGE,
                    "hello"
                ) //This should be changed according to the actual situation
                val consumer: Consumer = object : DefaultConsumer(channel) {
                    @Throws(IOException::class)
                    override fun handleDelivery(
                        consumerTag: String?, envelope: Envelope,
                        properties: AMQP.BasicProperties?, body: ByteArray?
                    ) {
                        mMessage = String(body!!)
                        System.out.println(
                            " [x] Received '" + envelope.getRoutingKey()
                                .toString() + "':'" + mMessage.toString() + "'"
                        )
                        // It is more efficient to get the msg object from the message pool
                        val msg = handler.obtainMessage()
                        val bundle = Bundle()
                        bundle.putString("msg", mMessage)
                        msg.data = bundle
                        handler.sendMessage(msg)
                    }
                }
                channel.basicConsume("hello", true, consumer)
                //
            } catch (e1: Exception) {
                Log.d("", "Connection broken: " + e1.javaClass.name)
                try {
                    Thread.sleep(2000) //sleep and then try again
                } catch (e: InterruptedException) {
                    //                            break;
                }
            }
            Log.i("1111111111111111111111", "run: ")
            //                }
        }
        subscribeThread!!.start()
    }
}