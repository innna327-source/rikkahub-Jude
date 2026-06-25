package me.rerere.rikkahub.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfMissing(
            db,
            tableName = "ConversationEntity",
            columnName = "workspace_cwd",
            sql = "ALTER TABLE `ConversationEntity` ADD COLUMN `workspace_cwd` TEXT NOT NULL DEFAULT ''",
        )
        addColumnIfMissing(
            db,
            tableName = "ConversationEntity",
            columnName = "compressed_summary",
            sql = "ALTER TABLE `ConversationEntity` ADD COLUMN `compressed_summary` TEXT NOT NULL DEFAULT ''",
        )
        addColumnIfMissing(
            db,
            tableName = "ConversationEntity",
            columnName = "compressed_node_ids",
            sql = "ALTER TABLE `ConversationEntity` ADD COLUMN `compressed_node_ids` TEXT NOT NULL DEFAULT '[]'",
        )
        addColumnIfMissing(
            db,
            tableName = "ConversationEntity",
            columnName = "auto_compress_config",
            sql = "ALTER TABLE `ConversationEntity` ADD COLUMN `auto_compress_config` TEXT NOT NULL DEFAULT ''",
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workspaces` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `root` TEXT NOT NULL,
                `shell_status` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `last_access_at` INTEGER,
                `tool_approvals` TEXT NOT NULL DEFAULT '{}',
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_workspaces_root` ON `workspaces` (`root`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workspaces_updated_at` ON `workspaces` (`updated_at`)")
    }
}

private fun addColumnIfMissing(
    db: SupportSQLiteDatabase,
    tableName: String,
    columnName: String,
    sql: String,
) {
    if (!hasColumn(db, tableName, columnName)) {
        db.execSQL(sql)
    }
}

private fun hasColumn(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
    db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) {
                return true
            }
        }
    }
    return false
}
