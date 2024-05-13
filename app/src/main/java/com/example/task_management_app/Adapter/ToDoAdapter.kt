package com.example.task_management_app.Adapter


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.task_management_app.AddNewTask
import com.example.task_management_app.MainActivity
import com.example.task_management_app.Model.ToDoModel
import com.example.task_management_app.R
import com.example.task_management_app.Utils.DatabaseHandler


class ToDoAdapter(private val db: DatabaseHandler, private val activity: MainActivity) :
    RecyclerView.Adapter<ToDoAdapter.ViewHolder>() {

    val context: Context
        get() = activity

    private var todoList: List<ToDoModel> = ArrayList()
    private var filteredList: MutableList<ToDoModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position] // Use filtered list for binding
        holder.task.text = item.task
        holder.task.isChecked = toBoolean(item.status)

        when (item.priority) {
            "Low" -> holder.taskContainer.setBackgroundResource(android.R.color.holo_green_light)
            "Medium" -> holder.taskContainer.setBackgroundResource(android.R.color.holo_orange_light)
            "High" -> holder.taskContainer.setBackgroundResource(android.R.color.holo_red_light)
            else -> holder.taskContainer.setBackgroundResource(android.R.color.transparent)
        }

        holder.task.setOnCheckedChangeListener { buttonView, isChecked ->
            val newStatus = if (isChecked) 1 else 0
            db.updateStatus(item.id, newStatus)
        }
    }

    private fun toBoolean(n: Int): Boolean {
        return n != 0
    }

    override fun getItemCount(): Int {
        return filteredList.size // Return size of filtered list
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val task: CheckBox = view.findViewById(R.id.todoCheckBox)
        val taskContainer: RelativeLayout = view.findViewById(R.id.taskContainer)

        init {
            task.setOnLongClickListener {
                deleteItem(adapterPosition)
                true
            }


        }
    }

    fun setTasks(todoList: List<ToDoModel>) {
        this.todoList = todoList
        this.filteredList = todoList.toMutableList() // Initialize filtered list with all tasks
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        val item = filteredList[position]
        db.deleteTask(item.id)
        filteredList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun editItem(position: Int) {
        val item = filteredList[position]
        val fragment = AddNewTask()
        val bundle = Bundle().apply {
            putInt("id", item.id)
            putString("task", item.task)
            putString("description", item.description)
            putString("priority", item.priority)
        }
        fragment.arguments = bundle
        fragment.show(activity.supportFragmentManager, AddNewTask.TAG)
    }

    // Filter tasks based on search query
    fun filterTasks(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(todoList) // Show all tasks if query is empty
        } else {
            val lowerCaseQuery = query.toLowerCase()
            todoList.forEach { task ->
                if (task.task!!.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(task)
                }
            }
        }
        notifyDataSetChanged()
    }
}
