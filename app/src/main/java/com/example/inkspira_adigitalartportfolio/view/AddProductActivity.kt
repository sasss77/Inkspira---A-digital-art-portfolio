package com.example.inkspira_adigitalartportfolio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.model.ProductModel
import com.example.inkspira_adigitalartportfolio.model.UserModel
import com.example.inkspira_adigitalartportfolio.repository.ProductRepositoryImpl
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraADigitalArtPortfolioTheme
import com.example.inkspira_adigitalartportfolio.viewmodel.ProductViewModel

class AddProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           addProduct()
        }
    }
}

@Composable
fun addProduct() {
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold() {
        padding -> LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            item {
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                    },
                    placeholder = {
                        Text("Product Name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                    },
                    placeholder = {
                        Text("price")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    placeholder = {
                        Text("Description")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))



                Button(onClick = {
                    val model = ProductModel("", productName, price.toDouble(), description)
                    viewModel.addProduct(model) {
                        success, message -> {
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                activity?.finish()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG) .show()
                            }
                    }
                    }


                },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Register")
                }



            }

    }
    }
}


@Preview
@Composable
fun Prev() {
    addProduct()
}
