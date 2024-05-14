package com.example.task_management_app

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_management_app.Adapter.ToDoAdapter
import com.example.task_management_app.Model.ToDoModel
import com.example.task_management_app.Utils.DatabaseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), DialogCloseListener {

    private lateinit var db: DatabaseHandler
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var tasksAdapter: ToDoAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var spinnerPriority: Spinner
    private var taskList: MutableList<ToDoModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        db = DatabaseHandler(this)
        db.openDatabase()

        tasksRecyclerView = findViewById(R.id.taskRecyclerView)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksAdapter = ToDoAdapter(db, this)
        tasksRecyclerView.adapter = tasksAdapter

        val itemTouchHelper = ItemTouchHelper(RecyclerItemTouchHelper(tasksAdapter))
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val dialog = AddNewTask()
            dialog.show(supportFragmentManager, AddNewTask.TAG)
        }

        spinnerPriority = findViewById(R.id.sort)
        val priorities = resources.getStringArray(R.array.priority_array)
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPriority = parent?.getItemAtPosition(position).toString()
                tasksAdapter.filterTasks(selectedPriority) // Filter tasks based on priority
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadTasks()
    }

    private fun loadTasks() {
        taskList.clear() // Clear existing task list
        taskList.addAll(db.allTasks) // Retrieve tasks from the database
        taskList.reverse() // Reverse the order of tasks (optional)
        tasksAdapter.setTasks(taskList) // Update RecyclerView with the new task list
    }

    override fun handleDialogClose(dialog: DialogInterface?) {
        loadTasks() // Reload tasks when dialog is closed
        tasksAdapter.notifyDataSetChanged() // Notify RecyclerView adapter of data change
    }
}
