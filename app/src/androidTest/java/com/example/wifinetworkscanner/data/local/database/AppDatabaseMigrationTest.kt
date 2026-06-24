package com.example.wifinetworkscanner.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        deleteTestDatabase()
    }

    @After
    fun tearDown() {
        deleteTestDatabase()
    }

    @Test
    fun migrate1To2_whenDatabaseIsEmpty_shouldValidateSchema() {
        createVersion1Database().close()

        val migratedDatabase = openMigratedDatabase()

        try {
            val supportDatabase = migratedDatabase.openHelper.writableDatabase

            assertEquals(NEW_DATABASE_VERSION, readUserVersion(supportDatabase))
            assertColumnExists(
                database = supportDatabase,
                tableName = HISTORY_TABLE_NAME,
                columnName = NETWORK_NAME_COLUMN
            )
            assertColumnExists(
                database = supportDatabase,
                tableName = HISTORY_TABLE_NAME,
                columnName = NETWORK_IDENTIFIER_COLUMN
            )
            assertIndexExists(
                database = supportDatabase,
                tableName = HISTORY_TABLE_NAME,
                indexName = NETWORK_IDENTIFIER_INDEX_NAME
            )
        } finally {
            migratedDatabase.close()
        }
    }

    @Test
    fun migrate1To2_whenHistoryExists_shouldPreserveDataAndFillNetworkDefaults() {
        val oldDatabase = createVersion1Database()

        try {
            insertVersion1History(oldDatabase)
            insertVersion1Device(oldDatabase)
        } finally {
            oldDatabase.close()
        }

        val migratedDatabase = openMigratedDatabase()

        try {
            val supportDatabase = migratedDatabase.openHelper.writableDatabase

            val historyCursor = supportDatabase.query(
                """
                SELECT
                    network_name,
                    network_identifier,
                    local_ip_address,
                    gateway_ip_address,
                    found_device_count
                FROM tabela_historico_varreduras
                WHERE scan_history_id = 1
                """.trimIndent()
            )

            try {
                assertTrue(historyCursor.moveToFirst())
                assertEquals("Rede desconhecida", historyCursor.getString(0))
                assertEquals("unknown_network", historyCursor.getString(1))
                assertEquals("192.168.1.10", historyCursor.getString(2))
                assertEquals("192.168.1.1", historyCursor.getString(3))
                assertEquals(1, historyCursor.getInt(4))
            } finally {
                historyCursor.close()
            }

            val deviceCursor = supportDatabase.query(
                """
                SELECT
                    ip_address,
                    open_ports_csv,
                    label
                FROM tabela_dispositivos_varredura
                WHERE scan_history_owner_id = 1
                """.trimIndent()
            )

            try {
                assertTrue(deviceCursor.moveToFirst())
                assertEquals("192.168.1.20", deviceCursor.getString(0))
                assertEquals("80,443", deviceCursor.getString(1))
                assertEquals("Notebook", deviceCursor.getString(2))
            } finally {
                deviceCursor.close()
            }
        } finally {
            migratedDatabase.close()
        }
    }

    private fun createVersion1Database(): SQLiteDatabase {
        val database = context.openOrCreateDatabase(
            TEST_DATABASE_NAME,
            Context.MODE_PRIVATE,
            null
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tabela_historico_varreduras (
                scan_history_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                started_at_epoch_millis INTEGER NOT NULL,
                completed_at_epoch_millis INTEGER NOT NULL,
                local_ip_address TEXT NOT NULL,
                gateway_ip_address TEXT,
                network_prefix_length INTEGER NOT NULL,
                total_host_count INTEGER NOT NULL,
                scanned_host_count INTEGER NOT NULL,
                found_device_count INTEGER NOT NULL,
                max_hosts INTEGER NOT NULL,
                timeout_millis INTEGER NOT NULL,
                parallelism INTEGER NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_tabela_historico_varreduras_started_at_epoch_millis
            ON tabela_historico_varreduras(started_at_epoch_millis)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tabela_dispositivos_varredura (
                scan_device_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                scan_history_owner_id INTEGER NOT NULL,
                ip_address TEXT NOT NULL,
                latency_millis INTEGER NOT NULL,
                scanned_at_epoch_millis INTEGER NOT NULL,
                detection_method TEXT NOT NULL,
                open_ports_csv TEXT NOT NULL,
                label TEXT,
                FOREIGN KEY(scan_history_owner_id)
                REFERENCES tabela_historico_varreduras(scan_history_id)
                ON UPDATE NO ACTION
                ON DELETE CASCADE
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_tabela_dispositivos_varredura_scan_history_owner_id
            ON tabela_dispositivos_varredura(scan_history_owner_id)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_tabela_dispositivos_varredura_ip_address
            ON tabela_dispositivos_varredura(ip_address)
            """.trimIndent()
        )

        database.setVersion(OLD_DATABASE_VERSION)

        return database
    }

    private fun insertVersion1History(database: SQLiteDatabase) {
        database.execSQL(
            """
            INSERT INTO tabela_historico_varreduras (
                scan_history_id,
                started_at_epoch_millis,
                completed_at_epoch_millis,
                local_ip_address,
                gateway_ip_address,
                network_prefix_length,
                total_host_count,
                scanned_host_count,
                found_device_count,
                max_hosts,
                timeout_millis,
                parallelism
            ) VALUES (
                1,
                1000,
                2000,
                '192.168.1.10',
                '192.168.1.1',
                24,
                254,
                254,
                1,
                254,
                700,
                8
            )
            """.trimIndent()
        )
    }

    private fun insertVersion1Device(database: SQLiteDatabase) {
        database.execSQL(
            """
            INSERT INTO tabela_dispositivos_varredura (
                scan_device_id,
                scan_history_owner_id,
                ip_address,
                latency_millis,
                scanned_at_epoch_millis,
                detection_method,
                open_ports_csv,
                label
            ) VALUES (
                1,
                1,
                '192.168.1.20',
                12,
                1500,
                'REACHABLE_AND_TCP_PORT',
                '80,443',
                'Notebook'
            )
            """.trimIndent()
        )
    }

    private fun openMigratedDatabase(): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
    }

    private fun readUserVersion(database: SupportSQLiteDatabase): Int {
        val cursor = database.query("PRAGMA user_version")

        return try {
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        } finally {
            cursor.close()
        }
    }

    private fun assertColumnExists(
        database: SupportSQLiteDatabase,
        tableName: String,
        columnName: String
    ) {
        val cursor = database.query("PRAGMA table_info(`$tableName`)")

        try {
            val nameColumnIndex = cursor.getColumnIndexOrThrow("name")
            var columnExists = false

            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumnIndex) == columnName) {
                    columnExists = true
                }
            }

            assertTrue(
                "Coluna $columnName não encontrada na tabela $tableName.",
                columnExists
            )
        } finally {
            cursor.close()
        }
    }

    private fun assertIndexExists(
        database: SupportSQLiteDatabase,
        tableName: String,
        indexName: String
    ) {
        val cursor = database.query("PRAGMA index_list(`$tableName`)")

        try {
            val nameColumnIndex = cursor.getColumnIndexOrThrow("name")
            var indexExists = false

            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumnIndex) == indexName) {
                    indexExists = true
                }
            }

            assertTrue(
                "Índice $indexName não encontrado na tabela $tableName.",
                indexExists
            )
        } finally {
            cursor.close()
        }
    }

    private fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DATABASE_NAME)
    }

    private companion object {
        const val TEST_DATABASE_NAME = "migration-test.db"
        const val OLD_DATABASE_VERSION = 1
        const val NEW_DATABASE_VERSION = 2

        const val HISTORY_TABLE_NAME = "tabela_historico_varreduras"
        const val NETWORK_NAME_COLUMN = "network_name"
        const val NETWORK_IDENTIFIER_COLUMN = "network_identifier"
        const val NETWORK_IDENTIFIER_INDEX_NAME =
            "index_tabela_historico_varreduras_network_identifier"
    }
}