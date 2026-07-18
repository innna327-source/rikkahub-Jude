package me.rerere.rikkahub.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `moments` (
                `id` TEXT NOT NULL,
                `assistant_id` TEXT NOT NULL,
                `author` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `context_note` TEXT NOT NULL,
                `image_description` TEXT NOT NULL,
                `images` TEXT NOT NULL,
                `reply_due_at` INTEGER NOT NULL,
                `reply_status` TEXT NOT NULL,
                `ai_liked` INTEGER NOT NULL,
                `ai_reply_content` TEXT NOT NULL,
                `replied_at` INTEGER,
                `ai_reply_seen_at` INTEGER,
                `user_liked` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `moment_comments` (
                `id` TEXT NOT NULL,
                `moment_id` TEXT NOT NULL,
                `author` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `reply_due_at` INTEGER,
                `reply_status` TEXT NOT NULL,
                `seen_at` INTEGER,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`moment_id`) REFERENCES `moments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `moment_profiles` (
                `assistant_id` TEXT NOT NULL,
                `cover_uri` TEXT NOT NULL,
                `last_viewed_at` INTEGER NOT NULL,
                PRIMARY KEY(`assistant_id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_moments_assistant_id_created_at` ON `moments` (`assistant_id`, `created_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_moments_assistant_id_reply_status_reply_due_at` ON `moments` (`assistant_id`, `reply_status`, `reply_due_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_moment_comments_moment_id_created_at` ON `moment_comments` (`moment_id`, `created_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_moment_comments_author_reply_status_reply_due_at` ON `moment_comments` (`author`, `reply_status`, `reply_due_at`)")
    }
}

