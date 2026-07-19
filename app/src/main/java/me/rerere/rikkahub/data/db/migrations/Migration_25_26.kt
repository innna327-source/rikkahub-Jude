package me.rerere.rikkahub.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `anonymous_questions` (
                `id` TEXT NOT NULL, `scope_id` TEXT NOT NULL, `author` TEXT NOT NULL,
                `content` TEXT NOT NULL, `reply_due_at` INTEGER, `reply_status` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `anonymous_question_replies` (
                `id` TEXT NOT NULL, `question_id` TEXT NOT NULL, `author` TEXT NOT NULL,
                `kind` TEXT NOT NULL, `content` TEXT NOT NULL, `reply_due_at` INTEGER,
                `reply_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`),
                FOREIGN KEY(`question_id`) REFERENCES `anonymous_questions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `anonymous_question_profiles` (
                `scope_id` TEXT NOT NULL, `last_viewed_at` INTEGER NOT NULL, PRIMARY KEY(`scope_id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anonymous_questions_scope_id_created_at` ON `anonymous_questions` (`scope_id`, `created_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anonymous_questions_scope_id_author_reply_status_reply_due_at` ON `anonymous_questions` (`scope_id`, `author`, `reply_status`, `reply_due_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anonymous_question_replies_question_id_created_at` ON `anonymous_question_replies` (`question_id`, `created_at`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_anonymous_question_replies_question_id_author_kind` ON `anonymous_question_replies` (`question_id`, `author`, `kind`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anonymous_question_replies_author_reply_status_reply_due_at` ON `anonymous_question_replies` (`author`, `reply_status`, `reply_due_at`)")
    }
}
