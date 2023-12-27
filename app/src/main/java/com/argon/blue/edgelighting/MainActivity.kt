package com.argon.blue.edgelighting

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.argon.blue.edgelighting.ui.theme.EdgeLightingDemoTheme


class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_REQUEST_CODE = 1
    var storage_permissions = arrayOf<String>(
        READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storage_permissions_33 = arrayOf<String>(
        READ_MEDIA_IMAGES,
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout);

        // Check if the READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("DD", "request SD read permission!")
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, perform your desired operation
            // Read external storage code here
            Log.d("DD", "permission granted!")
        }

    }
    fun onClick(view: View) {
        Log.d("GG","onClick")
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, MatrixLiveWallpaperService::class.java)
            //ComponentName(this, IOS16WallpaperService::class.java)
            //ComponentName(this, MyWallpaperService::class.java)
            //ComponentName(this, SqueezWallpaperService::class.java)
        )
        startActivity(intent);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("DD", "onRequestPermissionsResult ${requestCode}, " +
                "grantResults is Not empty=${grantResults.isNotEmpty()}!")
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                // Check if the permission is granted or not
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted, perform your desired operation
                    // Read external storage code here
                    Log.d("DD", "permission granted after request!")
                } else {
                    // Permission is denied, handle the scenario gracefully or show an explanation
                    Log.d("DD", "permission denied after request!")
                }
                return
            }
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EdgeLightingDemoTheme {
        Greeting("Android")
    }
}