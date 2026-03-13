package com.astrais.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DatabaseDAOImpl : DatabaseDAO {
    override suspend fun test() {
        withContext(Dispatchers.IO) {
            transaction {
                exec("SELECT 1")
            }
        }
    }
}