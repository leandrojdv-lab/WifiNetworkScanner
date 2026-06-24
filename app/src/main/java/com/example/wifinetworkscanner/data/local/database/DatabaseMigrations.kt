package com.example.wifinetworkscanner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    private val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE tabela_historico_varreduras
                ADD COLUMN network_name TEXT NOT NULL DEFAULT 'Rede desconhecida'
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE tabela_historico_varreduras
                ADD COLUMN network_identifier TEXT NOT NULL DEFAULT 'unknown_network'
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_tabela_historico_varreduras_network_identifier
                ON tabela_historico_varreduras(network_identifier)
                """.trimIndent()
            )
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2
    )
}