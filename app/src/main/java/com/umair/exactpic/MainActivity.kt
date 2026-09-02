package com.umair.exactpic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.umair.exactpic.ui.ImagePadderScreen
import com.umair.exactpic.ui.theme.MyApplicationTheme
import com.umair.exactpic.viewmodel.ImagePadderViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: ImagePadderViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ImagePadderScreen(viewModel = viewModel)
      }
    }
  }
}

