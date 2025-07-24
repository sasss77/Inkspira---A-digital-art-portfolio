package com.example.inkspira_adigitalartportfolio.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.controller.repositoryImpl.UserRepositoryImpl

class UpdateUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            updateUser()
        }
    }
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun updateUser() {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val repo = remember { UserRepositoryImpl() }
    val viewModel = remember { UserViewModel(repo) }

    val context = LocalContext.current
    val activity = context as? Activity

    val userID : String? = activity?.intent?.getStringExtra("userID")

    val user = viewModel.users.observeAsState(initial = null)

            LaunchedEffect(Unit) {
                viewModel.getUserByID(userID.toString())
            }


    firstName = user.value?.firstName ?: ""
    lastName = user.value?.lastName ?: ""
    address = user.value?.address ?: ""


    Scaffold() {
            padding -> LazyColumn(
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
        item {

            Text(text = "First Name:", color = Color.Black, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                },
                placeholder = {
                    Text("First Name")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))


            Text(text = "Last Name:", color = Color.Black, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                },
                placeholder = {
                    Text("Last Name")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))


            Text(text = "Address:", color = Color.Black, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                },
                placeholder = {
                    Text("Address")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))



            Button(onClick = {
                var data = mutableMapOf<String,Any?>()
                data["address"] = address
                data["lastName"] = lastName
                data["firstName"] = firstName
                data["userID"] = userID


                viewModel.updateProfile(
                        userID.toString(),data
                ) {
                        success,message->
                    if(success){
                        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }else{
                        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                    }
                }

            },
                modifier = Modifier.fillMaxWidth()) {
                Text("Update User")
            }



        }

    }
    }
}