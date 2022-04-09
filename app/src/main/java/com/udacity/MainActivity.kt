package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var binding: ActivityMainBinding

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent

    private var url: String? = null
    private var fileName = ""

    private var notificationId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createNotificationChannel()
        binding.contentMain.customButton.setLoadingButtonState(ButtonState.Completed)
        binding.contentMain.customButton.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent?.action
            if (downloadID == id) {
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
                    val manager =
                        context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                binding.contentMain.customButton.setLoadingButtonState(ButtonState.Completed)
                                makeNotification(fileName, applicationContext, SUCCESS)
                            } else {
                                binding.contentMain.customButton.setLoadingButtonState(ButtonState.Completed)
                                makeNotification(fileName, applicationContext, FAILED)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun makeNotification(name: String, applicationContext: Context, status: String) {
        notificationId++

        val intent = Intent(this, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("name", name)
            putExtra("status", status)
        }
        pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.txt_notification_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fileName)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                getString(R.string.txt_open_detail),
                pendingIntent
            )

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val descriptionText = getString(R.string.txt_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download() {
        if (url != null) {
            binding.contentMain.customButton.setLoadingButtonState(ButtonState.Loading)
            notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager
            createChannel(
                getString(R.string.url_notification_channel_id),
                getString(R.string.url_notification_channel_name)
            )

            val file = File(getExternalFilesDir(null), "/repos")

            if (!file.exists()) {
                file.mkdirs()
            }
            val request =
                DownloadManager.Request(Uri.parse(URL))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "/repos/repository.zip"
                    )

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        } else {
            binding.contentMain.customButton.setLoadingButtonState(ButtonState.Completed)
            showToast(getString(R.string.txt_select_a_file))
        }
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download is done!"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            when (view.getId()) {
                R.id.glide -> {
                    url = GLIDE_URL
                    fileName = getString(R.string.glide)
                }

                R.id.load_app -> {
                    url = URL
                    fileName = getString(R.string.load_app)
                }

                R.id.retrofit -> {
                    url = RETROFIT_URL
                    fileName = getString(R.string.retrofit)
                }
            }
        }
    }

    companion object {
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit"
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"

        private const val SUCCESS = "Success"

        private const val FAILED = "Failed"
    }
}