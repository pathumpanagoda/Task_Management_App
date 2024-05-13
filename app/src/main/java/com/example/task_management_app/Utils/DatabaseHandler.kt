package com.example.task_management_app.Utils


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.task_management_app.Model.ToDoModel

class DatabaseHandler(context: Context?) :
    SQLiteOpenHelper(context, NAME, null, VERSION) {

    private var db: SQLiteDatabase? = null

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TODO_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TODO_TABLE")
        // Create tables again
        onCreate(db)
    }

    fun openDatabase() {
        db = this.writableDatabase
    }

    fun insertTask(task: ToDoModel) {
        val cv = ContentValues()
        cv.put(TASK, task.task)
        cv.put(DESCRIPTION, task.description)
        cv.put(PRIORITY, task.priority)
        cv.put(STATUS, 0)
        db?.insert(TODO_TABLE, null, cv)
    }

    val allTasks: List<ToDoModel>
        @SuppressLint("Range")
        get() {
            val taskList: MutableList<ToDoModel> = ArrayList()
            var cur: Cursor? = null
            db?.beginTransaction()
            try {
                cur = db?.query(TODO_TABLE, null, null, null, null, null, null, null)
                cur?.let {
                    if (it.moveToFirst()) {
                        do {
                            val task = ToDoModel()
                            task.id = it.getInt(it.getColumnIndex(ID))
                            task.task = it.getString(it.getColumnIndex(TASK))
                            task.description = it.getString(it.getColumnIndex(DESCRIPTION))
                            task.priority = it.getString(it.getColumnIndex(PRIORITY))
                            task.status = it.getInt(it.getColumnIndex(STATUS))
                            taskList.add(task)
                        } while (it.moveToNext())
                    } else {
                        // Cursor is empty
                        Log.d("DatabaseHandler", "Cursor is empty")
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseHandler", "Error fetching tasks: ${e.message}")
            } finally {
                db?.endTransaction()
                cur?.close()
            }

            // Print cursor column names and indices for debugging
            cur?.getColumnNames()?.forEachIndexed { index, columnName ->
                Log.d("DatabaseHandler", "Column $columnName at index $index")
            }

            return taskList
        }


    fun updateStatus(id: Int, status: Int) {
        val cv = ContentValues()
        cv.put(STATUS, status)
        db?.update(TODO_TABLE, cv, "$ID=?", arrayOf(id.toString()))
    }

    fun updateTask(id: Int, task: String?, description: String?, priority: String?) {
        val cv = ContentValues()
        cv.put(TASK, task)
        cv.put(DESCRIPTION, description)
        cv.put(PRIORITY, priority)
        db?.update(TODO_TABLE, cv, "$ID=?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Int) {
        db?.delete(TODO_TABLE, "$ID=?", arrayOf(id.toString()))
    }

    companion object {
        private const val VERSION = 2
        private const val NAME = "toDoListDatabase"
        private const val TODO_TABLE = "todo"
        private const val ID = "id"
        private const val TASK = "task"
        private const val DESCRIPTION = "description"
        private const val PRIORITY = "priority"
        private const val STATUS = "status"
        private const val CREATE_TODO_TABLE =
            ("CREATE TABLE $TODO_TABLE ($ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TASK TEXT, $DESCRIPTION TEXT, $PRIORITY TEXT, $STATUS INTEGER)")
    }
}
