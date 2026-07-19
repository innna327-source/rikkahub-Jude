package me.rerere.rikkahub.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.rerere.rikkahub.data.db.migrations.Migration_25_26
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnonymousQuestionMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate25To26CreatesAnonymousQuestionTables() {
        helper.createDatabase("anonymous-question-migration", 25).close()
        helper.runMigrationsAndValidate("anonymous-question-migration", 26, true, Migration_25_26).use { db ->
            db.query("SELECT COUNT(*) FROM anonymous_questions").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
            db.query("PRAGMA index_list('anonymous_question_replies')").use { cursor ->
                var foundUniqueIndex = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(1).contains("question_id_author_kind")) foundUniqueIndex = true
                }
                assertEquals(true, foundUniqueIndex)
            }
        }
    }
}
