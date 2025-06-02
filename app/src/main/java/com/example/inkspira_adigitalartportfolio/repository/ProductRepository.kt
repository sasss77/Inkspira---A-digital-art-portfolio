package com.example.inkspira_adigitalartportfolio.repository

import com.example.inkspira_adigitalartportfolio.model.ProductModel

interface ProductRepository {
    fun addProduct(productID : String, model: ProductModel, callback: (Boolean, String) -> Unit)
    fun removeProduct(productID : String, callback: (Boolean, String) -> Unit)
    fun editProduct(productID : String, productData : MutableMap<String, Any?>, callback: (Boolean, String) -> Unit)
    fun getProductByID(productID : String, callback : (ProductModel?, Boolean, String) ->Unit)
    fun getAllProduct(callback: (List<ProductModel?>, Boolean, String) -> Unit)
}
