package com.example.task_management_app.Adapter


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.task_management_app.MainActivity
import com.example.task_management_app.Model.ToDoModel
import com.example.task_management_app.R





class ToDoAdapter(private val db: DatabaseHandler, private val activity: MainActivity) :
    RecyclerView.Adapter<ToDoAdapter.ViewHolder>() {

    // Implementing getContext() method to return the context from MainActivity
    val context: Context
        get() = activity

    private var todoList: List<ToDoModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        db.openDatabase()
        val item = todoList[position]
        holder.task.text = item.task
        holder.task.isChecked = toBoolean(item.status)
        holder.task.setOnCheckedChangeListener { buttonView, isChecked ->
            val newStatus = if (isChecked) 1 else 0
            db.updateStatus(item.id, newStatus)
        }
    }

    private fun toBoolean(n: Int): Boolean {
        return n != 0
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val task: CheckBox = view.findViewById(R.id.todoCheckBox)

        init {
            task.setOnLongClickListener {
                deleteItem(adapterPosition)
                true
            }

            task.setOnClickListener {
                editItem(adapterPosition)
            }
        }
    }

    fun setTasks(todoList: List<ToDoModel>) {
        this.todoList = todoList
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        val item = todoList[position]
        db.deleteTask(item.id)
        val newList = todoList.toMutableList()
        newList.removeAt(position)
        todoList = newList
        notifyItemRemoved(position)
    }

    fun editItem(position: Int) {
        val item = todoList[position]
        // Use the context from activity to show the dialog fragment
        val fragment = AddNewTask()
        val bundle = Bundle()
        bundle.putInt("id", item.id)
        bundle.putString("task", item.task)
        fragment.arguments = bundle
        fragment.show(activity.supportFragmentManager, AddNewTask.TAG)
    }
}
