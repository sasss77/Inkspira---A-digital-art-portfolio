package com.example.inkspira_adigitalartportfolio.repository

import androidx.compose.runtime.mutableStateOf
import com.example.inkspira_adigitalartportfolio.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductRepositoryImpl : ProductRepository {


    val database = FirebaseDatabase.getInstance()
    val ref = database.reference.child("products")
    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key.toString()     // passing auto generated id so that there is no null value in product model
        model.productID = id
        ref.child(model.productID).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product added Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProduct(
        productID: String,
        productData: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).updateChildren(productData).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product updated Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getProductByID(
        productID: String,
        callback: (ProductModel?, Boolean, String) -> Unit
    ) {
            ref.child(productID).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val product = snapshot.getValue(ProductModel::class.java)
                        if(product != null) {
                            callback(product, true, "product fetched")
                        }
                        else {
                            callback(null, false, "product not found")
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, false, error.message)
                }

            })
    }

    override fun getAllProduct(callback: (List<ProductModel?>, Boolean, String) -> Unit) {
         ref.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
               if(snapshot.exists()) {
                   var allProducts = mutableListOf<ProductModel>()
                   for(eachProduct in snapshot.children) {
                       var products = eachProduct.getValue(ProductModel::class.java)
                       if(products != null) {
                           allProducts.add(products)
                       }

                   }
                   callback(allProducts, true, "product fetched")
               }
             }

             override fun onCancelled(error: DatabaseError) {
                 callback(emptyList(), false, error.message)
             }

         })
    }
}