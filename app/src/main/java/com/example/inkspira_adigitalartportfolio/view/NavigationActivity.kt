package com.example.inkspira_adigitalartportfolio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.repository.ProductRepositoryImpl
import com.example.inkspira_adigitalartportfolio.repository.UserRepositoryImpl
import com.example.inkspira_adigitalartportfolio.viewmodel.ProductViewModel
import com.example.inkspira_adigitalartportfolio.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
          navigationBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun navigationBody() {

    data class BottomNavItem(val label: String, val icon: ImageVector)

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Search", Icons.Default.Search),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex by remember { mutableStateOf(0) }

    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val context = LocalContext.current
    val activity = context as Activity



    Scaffold(

        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }


        ,

        topBar = {
            TopAppBar(
                title = {
                    Text("Ecommerce",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp)
                },
                actions = {
                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }



                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },


        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, AddProductActivity:: class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }





    )  {
        padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()

        ) {




                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedIndex) {
                        0 -> Home1()
                        1 -> Home2()
                        2 -> Home3()
                    }
                }


        }

    }
}

@Composable
fun BottomNavItem(x0: String, x1: ImageVector) {
    TODO("Not yet implemented")
}

@Preview
@Composable
fun Preview() {
    navigationBody()
}



@Composable
fun Home1() {

    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val context = LocalContext.current
    val activity = context as Activity

    val products = viewModel.allProducts.observeAsState(initial = emptyList())

    val loading = viewModel.loading.observeAsState(initial = true)


    LaunchedEffect(Unit) {
        viewModel.getAllProduct()
    }




        LazyColumn( modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)) {
            if (loading.value) {
                item {
                    CircularProgressIndicator()
                }
            } else {



            items(products.value.size) { index ->
                val eachProduct = products.value[index]
                Card(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text("${eachProduct?.productName}")
                        Text("${eachProduct?.price}")
                        Text("${eachProduct?.description}")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(context, UpdateProductActivity::class.java)
                                    intent.putExtra("productID", "${eachProduct?.productID}")
                                    context.startActivity(intent)
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteProduct(eachProduct?.productID.toString()) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, message, Toast.LENGTH_LONG)
                                                .show()
                                        } else {
                                            Toast.makeText(context, message, Toast.LENGTH_LONG)
                                                .show()
                                        }
                                    }
                                }, colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
            }
        }

}


@Composable
fun Home2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Yellow)
    ) {

    }
}








@Composable
fun Home3() {

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity

    val repo = remember { UserRepositoryImpl() }
    val viewModel = remember { UserViewModel(repo) }



//    val userID : String? = activity?.intent?.getStringExtra("userID")
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val user = viewModel.users.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.getUserByID(userID.toString())
    }


    firstName = user.value?.firstName ?: ""
    lastName = user.value?.lastName ?: ""
    email = user.value?.email ?: ""
    country = user.value?.address ?: ""




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {

        Text(text = "User Profile", fontSize = 24.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        




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
            modifier = Modifier.fillMaxWidth(),

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

        Text(text = "Email:", color = Color.Black, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            placeholder = {
                Text("Address")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Address:", color = Color.Black, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = country,
            onValueChange = {
                country = it
            },
            placeholder = {
                Text("Address")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                val intent = Intent(context, UpdateUserActivity:: class.java)
                intent.putExtra("userID", "${userID}")
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Update")
        }

    }
    }