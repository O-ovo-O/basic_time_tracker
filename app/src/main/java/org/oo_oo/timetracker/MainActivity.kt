package org.oo_oo.timetracker

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Entity
data class WorkEntry(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val startTime: String,
    val endTime: String?,
    val totalTime: Long
)

fun timeStringToSeconds(timeString: String): Long {
    val parts = timeString.split(":")
    val days = parts[0].toLong()
    val hours = parts[1].toLong()
    val minutes = parts[2].toLong()
    return (days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60)
}

fun secondsToTimeString(totalSeconds: Long, sign: Boolean): String {
    val days = totalSeconds / (24 * 60 * 60)
    val hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60)
    val minutes = (totalSeconds % (60 * 60)) / 60
//    println("d ${days}, h ${hours}, m $minutes / ${abs(days)}, ${abs(hours)}, ${abs(minutes)}")
    return if (sign) {
        String.format("%+02d:%02d:%02d", days, hours, minutes)
    }
    else
        String.format("%02d:%02d:%02d", days, hours, minutes)
}
fun secondsToView(totalSeconds: Long): String {
    val days = totalSeconds / (24 * 60 * 60)
    val hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60)
    val minutes = (totalSeconds % (60 * 60)) / 60
//    println("d ${days}, h ${hours}, m $minutes / ${abs(days)}, ${abs(hours)}, ${abs(minutes)}")
    val sign = if (days >= 0 && hours >= 0 && minutes >= 0) { "+" } else { "-" }
    return String.format("${sign} %02d:%02d:%02d", abs(days), abs(hours), abs(minutes))
}



@Dao
interface WorkEntryDao {
    @Query("SELECT * FROM WorkEntry")
    fun getAll(): List<WorkEntry>

    @Insert
    fun insert(workEntry: WorkEntry)
}

class WorkTimeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "WorkTime2024.db"
        const val TABLE_NAME = "work_entries"

        private const val COLUMN_ID = "id"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_TOTAL_TIME = "total_time"
    }
    fun clearDatabase() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        createTable(db)
        db.close()
    }
    fun createTable(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_START_TIME DATETIME, " +
                "$COLUMN_END_TIME DATETIME, " +
                "$COLUMN_TOTAL_TIME LONG)"
        try {
            db.execSQL(createTable)
        } catch (e: Exception) {
            // exists, ignore
            println(e)
        }
    }
    override fun onCreate(db: SQLiteDatabase) {
        createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Method to get all work entries
    fun getAllWorkEntries(): List<WorkEntry> {
        val workEntries = mutableListOf<WorkEntry>()
        val selectQuery = "SELECT * FROM $TABLE_NAME"

        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val workEntry = WorkEntry(
                    id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    startTime = cursor.getString(cursor.getColumnIndex(COLUMN_START_TIME)),
                    endTime = cursor.getString(cursor.getColumnIndex(COLUMN_END_TIME)),
                    totalTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TOTAL_TIME))
                )
                workEntries.add(workEntry)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return workEntries
    }
}

@Database(entities = [WorkEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workEntryDao(): WorkEntryDao
}




class MainActivity : AppCompatActivity() {

    fun populate_table(){
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val entriesContainer: LinearLayout = findViewById(R.id.entriesContainer)
        entriesContainer.removeAllViews()
        //val entries = db.workEntryDao().getAll()

        val entries = dbHelper.getAllWorkEntries()
//                val entries = arrayOf(WorkEntry(123, "", "", "")) ;

        var textView = TextView(this)
        val d = 25
        textView.text = "${"started".padEnd(d)} | ${"finished".padEnd(d)} | ${"duration".padEnd(d)}"
        entriesContainer.addView(textView)

        for (entry in entries) {
            textView = TextView(this)
            val a = entry.startTime.toLong()
            val b = if (entry.endTime.isNullOrEmpty()) { a } else { entry.endTime.toLong() }
            textView.text =
                "${sdf.format(a)} | ${sdf.format(b.toLong())} | ${secondsToTimeString(entry.totalTime, true)}"
            entriesContainer.addView(textView)
        }
    }

    private var startTime: Long? = null
    private var endTime: Long? = null
    private var lastEntry: Long? = null
    private var isCounting = false
    private lateinit var db: AppDatabase
    private lateinit var dbHelper: WorkTimeDatabaseHelper
    private var lastView: TextView? = null

    override fun onResume() {
        super.onResume()
        populate_table()
    }

    override fun onPause() {
        super.onPause()
        if (isCounting) {
            println("DUPA")
//            findViewById(R.id.startStopButton)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val ot_show = sharedPreferences.getString("ot", "??:??:??")
        val overtimeTextView: TextView = findViewById(R.id.overtimeTextView)

        overtimeTextView.text = "LAST: ${ot_show}"

        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        dbHelper = WorkTimeDatabaseHelper(this)
//        db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "work-time-db"
//        ).build()

        val startStopButton: Button = findViewById(R.id.startStopButton)
        startStopButton.setOnLongClickListener {
            if (isCounting) {
                startStopButton.text = "Start"
                isCounting = false
            }
            true
        }

        startStopButton.setOnClickListener {
            lateinit var values: ContentValues
            val db = dbHelper.writableDatabase
            val entriesContainer: LinearLayout = findViewById(R.id.entriesContainer)

            if (isCounting) {
                // Stop counting
                endTime = System.currentTimeMillis()


//                val totalTime = (endTime!! - startTime!! + 60_000 * (60 * 8) - 60_000*20) / 1000    // FAKE -120 min niedoczasu
                val totalTime = (endTime!! - startTime!! + 60_000 * (60 * 8) + 60_000*22) / 1000    // FAKE +120 min OT
//                val totalTime = (endTime!! - startTime!! + 60_000 * 60 * 8) / 1000 // zero OT, jesli szybko klikne on/off = 8h pracy


                val total = sharedPreferences.getString("ot", "00:00:00")

                var temp = timeStringToSeconds(total!!)
                temp += totalTime - 8 * 60 * 60 // 8 h

                val overtimeTotal = secondsToTimeString(temp, false)
                sharedPreferences.edit().putString("ot", overtimeTotal).commit()

                val is_ot_positive = if (temp > 0) { "Overtime" } else  "Undertime"
                val in_hours = if (temp >= 0) {
                    temp / 3600 + if (temp % 3600 / 60 > 35) 1 else 0
                } else {
                    temp / 3600 - if (abs(temp) % 3600 / 60 > 35) 1 else 0
                }


                overtimeTextView.text = "${is_ot_positive}: ${secondsToView(temp)} " +
                        "~ ${in_hours} hours"



                values = ContentValues().apply {
                    put(WorkTimeDatabaseHelper.COLUMN_START_TIME, startTime)
                    put(WorkTimeDatabaseHelper.COLUMN_END_TIME, endTime)
                    put(WorkTimeDatabaseHelper.COLUMN_TOTAL_TIME, totalTime)
                }
                isCounting = false
                startStopButton.text = "Start"
                db.execSQL("DELETE FROM work_entries WHERE id = (SELECT id FROM work_entries ORDER BY id DESC LIMIT 1)")

//                fun updateLastEntry(newStartTime: String, newEndTime: String?, newTotalTime: String) {
//                    val db = this.writableDatabase
//                    db.execSQL("UPDATE work_entries SET start_time = ?, end_time = ?, total_time = ? WHERE id = (SELECT id FROM work_entries ORDER BY timestamp DESC LIMIT 1)")
//                    db.close()
//                }

                entriesContainer.removeView(lastView)
                val textView = TextView(this)
                textView.text =
                    "${sdf.format(Date(startTime!!))} | ${sdf.format(Date(endTime!!))} | ${secondsToTimeString(totalTime, true)}"
                entriesContainer.addView(textView)
            } else {
                // Start counting
                startTime = System.currentTimeMillis()
                isCounting = true
                startStopButton.text = "Stop (long press to abort)"

                values = ContentValues().apply {
                    put(WorkTimeDatabaseHelper.COLUMN_START_TIME, startTime)
                    put(WorkTimeDatabaseHelper.COLUMN_END_TIME, "")
                    put(WorkTimeDatabaseHelper.COLUMN_TOTAL_TIME, "")
                }

                lastView = TextView(this)
                lastView!!.text =
                    "${sdf.format(Date(startTime!!))} -> ..."
                entriesContainer.addView(lastView)
            }
            lastEntry = db.insert(WorkTimeDatabaseHelper.TABLE_NAME, null, values)
            db.close()


//            val workEntry = WorkEntry(0, startTime.toString(), endTime.toString(), totalTime.toString())
            //db.workEntryDao().insert(workEntry)


//            val workEntry = WorkEntry(0, , endTime.toString(), totalTime.toString())

        }
        populate_table()


    }
}
