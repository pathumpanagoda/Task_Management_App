package com.example.task_management_app

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.example.task_management_app.Model.ToDoModel
import com.example.task_management_app.Utils.DatabaseHandler
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddNewTask : BottomSheetDialogFragment() {
    private var newTaskText: EditText? = null
    private var newTaskDescription: EditText? = null
    private var checkBoxLow: CheckBox? = null
    private var checkBoxMedium: CheckBox? = null
    private var checkBoxHigh: CheckBox? = null
    private var newTaskSaveButton: Button? = null
    private var db: DatabaseHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.new_task, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newTaskText = view.findViewById(R.id.newTaskText)
        newTaskDescription = view.findViewById(R.id.newDescription)
        checkBoxLow = view.findViewById(R.id.checkBox1)
        checkBoxMedium = view.findViewById(R.id.checkBox2)
        checkBoxHigh = view.findViewById(R.id.checkBox3)
        newTaskSaveButton = view.findViewById(R.id.newTaskButton)

        var isUpdate = false
        val bundle = arguments
        if (bundle != null) {
            isUpdate = true
            val task = bundle.getString("task") ?: ""
            val description = bundle.getString("description") ?: ""
            val priority = bundle.getString("priority") ?: ""
            newTaskText?.setText(task)
            newTaskDescription?.setText(description)

            if (priority.isNotEmpty()) {
                when (priority) {
                    "Low" -> checkBoxLow?.isChecked = true
                    "Medium" -> checkBoxMedium?.isChecked = true
                    "High" -> checkBoxHigh?.isChecked = true
                }
            }

            if (task.isNotEmpty()) {
                newTaskSaveButton?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                )
            }
        }

        db = DatabaseHandler(requireActivity())
        db?.openDatabase()

        // Enable/disable save button based on task text
        newTaskText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                newTaskSaveButton?.isEnabled = s?.isNotEmpty() ?: false
                newTaskSaveButton?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (s?.isNotEmpty() == true) R.color.colorPrimaryDark else android.R.color.darker_gray
                    )
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        newTaskSaveButton?.setOnClickListener {
            val text = newTaskText?.text.toString()
            val description = newTaskDescription?.text.toString()
            val priority = when {
                checkBoxLow?.isChecked == true -> "Low"
                checkBoxMedium?.isChecked == true -> "Medium"
                checkBoxHigh?.isChecked == true -> "High"
                else -> "" // Default priority if none selected
            }

            if (isUpdate) {
                bundle?.getInt("id")?.let { taskId ->
                    db?.updateTask(taskId, text, description, priority)
                }
            } else {
                // Create a new ToDoModel instance with all required fields
                val task = ToDoModel().apply {
                    this.task = text
                    this.description = description // Assign the description here
                    this.priority = priority
                    this.status = 0 // Assuming default status here
                }
                db?.insertTask(task) // Insert the task into the database
            }
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = activity
        if (activity is DialogCloseListener) {
            activity.handleDialogClose(dialog)
        }
    }

    companion object {
        const val TAG = "ActionBottomDialog"
        fun newInstance(): AddNewTask {
            return AddNewTask()
        }
    }
}



