package com.example.inkspira_adigitalartportfolio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.repository.UserRepositoryImpl
import com.example.inkspira_adigitalartportfolio.viewmodel.UserViewModel

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            forgetBody()

        }
    }
}

@Composable
fun forgetBody() {


    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }
    val context = LocalContext.current
    val activity = context as Activity
    var email by remember { mutableStateOf("") }
    Scaffold  { padding->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                placeholder = {
                    Text("abc@gmail.com")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    userViewModel.forgetPassword(email) {
                        success, message -> {
                        if(success) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//                            val intent = Intent(context, NavigationActivity:: class.java)
//                            context.startActivity(intent)
//                            activity.finish()
                        }

                        else {

                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 15.dp)
            ) {
                Text("Submit")
            }
        }
    }
}

@Preview
@Composable
fun prev() {
    forgetBody()
}