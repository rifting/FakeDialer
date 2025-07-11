package com.example.fakedialer

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fakedialer.ui.theme.FakedialerTheme
import android.net.Uri
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.MediaController
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    private lateinit var requestDialerRoleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestDialerRoleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // hooray
            } else {
                // epic fail
            }
        }

        // I had to do so much stupid stuff for this haha
        val telecomManager = getSystemService(TelecomManager::class.java)
        val phoneAccountHandle = PhoneAccountHandle(
            ComponentName(this, MyConnectionService::class.java),
            "fake_dialer_account"
        )
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Fake Dialer")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()
        telecomManager.registerPhoneAccount(phoneAccount)

        // Actually requesting our dialer role
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                requestDialerRoleLauncher.launch(intent)
            }
        }

        setContent {
            FakedialerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).background(Color.Black)) {
                        AndroidView(factory = { context ->
                            VideoView(context).apply {
                                val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.sample}")
                                setVideoURI(videoUri)
                                val mediaController = MediaController(context)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                start()
                            }
                        }, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}