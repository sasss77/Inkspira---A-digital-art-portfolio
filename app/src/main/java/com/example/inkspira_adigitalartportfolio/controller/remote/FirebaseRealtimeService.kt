package com.example.inkspira_adigitalartportfolio.controller.remote

import com.example.inkspira_adigitalartportfolio.utils.Constants
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseRealtimeService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Generic method to save data to any node
    suspend fun <T> saveData(node: String, key: String, data: T): NetworkResult<T> {
        return try {
            database.child(node).child(key).setValue(data).await()
            NetworkResult.Success(data)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to save data: ${e.message}")
        }
    }

    // Generic method to save data with auto-generated key
    suspend fun <T> saveDataWithAutoKey(node: String, data: T): NetworkResult<Pair<String, T>> {
        return try {
            val key = database.child(node).push().key ?: throw Exception("Failed to generate key")
            database.child(node).child(key).setValue(data).await()
            NetworkResult.Success(Pair(key, data))
        } catch (e: Exception) {
            NetworkResult.Error("Failed to save data: ${e.message}")
        }
    }

    // Generic method to get data by key
    suspend fun <T> getData(node: String, key: String, dataClass: Class<T>): NetworkResult<T?> {
        return try {
            val snapshot = database.child(node).child(key).get().await()
            val data = snapshot.getValue(dataClass)
            NetworkResult.Success(data)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to get data: ${e.message}")
        }
    }

    // Generic method to get all data from a node
    suspend fun <T> getAllData(node: String, dataClass: Class<T>): NetworkResult<List<T>> {
        return try {
            val snapshot = database.child(node).get().await()
            val dataList = mutableListOf<T>()

            snapshot.children.forEach { childSnapshot ->
                childSnapshot.getValue(dataClass)?.let { data ->
                    dataList.add(data)
                }
            }

            NetworkResult.Success(dataList)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to get all data: ${e.message}")
        }
    }

    // Generic method to update data
    suspend fun <T> updateData(node: String, key: String, updates: Map<String, Any>): NetworkResult<Boolean> {
        return try {
            database.child(node).child(key).updateChildren(updates).await()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update data: ${e.message}")
        }
    }

    // Generic method to delete data
    suspend fun deleteData(node: String, key: String): NetworkResult<Boolean> {
        return try {
            database.child(node).child(key).removeValue().await()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to delete data: ${e.message}")
        }
    }

    // Query data with conditions
    suspend fun <T> queryData(
        node: String,
        orderByChild: String,
        equalTo: String,
        dataClass: Class<T>
    ): NetworkResult<List<T>> {
        return try {
            val query = database.child(node).orderByChild(orderByChild).equalTo(equalTo)
            val snapshot = query.get().await()
            val dataList = mutableListOf<T>()

            snapshot.children.forEach { childSnapshot ->
                childSnapshot.getValue(dataClass)?.let { data ->
                    dataList.add(data)
                }
            }

            NetworkResult.Success(dataList)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to query data: ${e.message}")
        }
    }

    // Check if data exists
    suspend fun checkDataExists(node: String, key: String): NetworkResult<Boolean> {
        return try {
            val snapshot = database.child(node).child(key).get().await()
            NetworkResult.Success(snapshot.exists())
        } catch (e: Exception) {
            NetworkResult.Error("Failed to check data existence: ${e.message}")
        }
    }

    // Real-time listener for data changes (for future use)
    fun addDataChangeListener(
        node: String,
        key: String,
        onDataChange: (DataSnapshot) -> Unit,
        onError: (DatabaseError) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        }
        database.child(node).child(key).addValueEventListener(listener)
        return listener
    }

    // Remove listener
    fun removeDataChangeListener(node: String, key: String, listener: ValueEventListener) {
        database.child(node).child(key).removeEventListener(listener)
    }
}
