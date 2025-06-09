package com.example.inkspira_adigitalartportfolio.repository

import com.example.inkspira_adigitalartportfolio.model.ProductModel
import com.google.firebase.database.FirebaseDatabase

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

    }

    override fun getAllProduct(callback: (List<ProductModel?>, Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }
}