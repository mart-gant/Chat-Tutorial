package com.example.chattutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.chattutorial.databinding.ActivityChannelBinding
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.view.MessageInputView.PermissionRequestListener
import com.getstream.sdk.chat.viewmodel.ChannelViewModel
import com.getstream.sdk.chat.viewmodel.ChannelViewModelFactory
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.models.Channel
import java.util.*

class ChannelActivity : AppCompatActivity(), PermissionRequestListener {

    private lateinit var binding: ActivityChannelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // receive the intent and create a channel object
        val intent = intent
        val channelType = intent.getStringExtra(EXTRA_CHANNEL_TYPE)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)

        // we're using data binding in this example
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel)
        // most the business logic of the chat is handled in the ChannelViewModel view model
        binding.lifecycleOwner = this

        initViewModel(channelType, channelId)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // If you are using own MessageInputView please comment this line.
        binding.messageInput.captureMedia(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) { // If you are using own MessageInputView please comment this line.
        binding.messageInput.permissionResult(requestCode, permissions, grantResults)
    }

    override fun openPermissionRequest() {
        PermissionChecker.permissionCheck(this, null)
        // If you are writing a Channel Screen in a Fragment, use the code below instead of the code above.
        // PermissionChecker.permissionCheck(getActivity(), this);
    }

    private fun initViewModel(
        channelType: String,
        channelId: String
    ) {

        val viewModelFactory = ChannelViewModelFactory(application, channelType, channelId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ChannelViewModel::class.java)
        val client = ChatClient.instance()
        val channelController = client.channel(channelType, channelId)
        val lifeCycleOwner = this

        viewModel.initialized.observe(this, Observer<Channel> { channel ->
            // connect the view model
            val factory = MyMessageViewHolderFactory()
            binding.messageList.setViewHolderFactory(factory)
            binding.viewModel = viewModel
            binding.messageList.setViewModel(viewModel, this)
            binding.messageInput.setViewModel(viewModel, this)
            binding.channelHeader.setViewModel(viewModel, this)
            // If you are using own MessageInputView please comment this line.
            binding.messageInput.setPermissionRequestListener(this)
        }
        )
    }

    companion object {

        private const val EXTRA_CHANNEL_TYPE = "com.example.chattutorial.CHANNEL_TYPE"
        private const val EXTRA_CHANNEL_ID = "com.example.chattutorial.CHANNEL_ID"

        fun newIntent(context: Context, channel: Channel): Intent {
            val intent = Intent(context, ChannelActivity::class.java)
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.type)
            intent.putExtra(EXTRA_CHANNEL_ID, channel.id)
            return intent
        }
    }
}