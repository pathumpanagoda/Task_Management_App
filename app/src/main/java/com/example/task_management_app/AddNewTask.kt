package com.example.task_management_app

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.task_management_app.Model.ToDoModel
import com.example.task_management_app.Utils.DatabaseHandler
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Objects

class AddNewTask : BottomSheetDialogFragment() {
    private var newTaskText: EditText? = null
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
        newTaskSaveButton = view.findViewById(R.id.newTaskButton)

        var isUpdate = false
        val bundle = arguments
        if (bundle != null) {
            isUpdate = true
            val task = bundle.getString("task") ?: ""
            newTaskText?.setText(task)

            if (task.isNotEmpty()) {
                newTaskSaveButton?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                )
            }
        }

        db = DatabaseHandler(requireActivity())
        db?.openDatabase()

        newTaskText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    newTaskSaveButton?.isEnabled = false
                    newTaskSaveButton?.setTextColor(Color.GRAY)
                } else {
                    newTaskSaveButton?.isEnabled = true
                    newTaskSaveButton?.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val finalIsUpdate = isUpdate
        newTaskSaveButton?.setOnClickListener {
            val text = newTaskText?.text.toString()
            if (finalIsUpdate) {
                bundle?.getInt("id")?.let { taskId ->
                    db?.updateTask(taskId, text)
                }
            } else {
                val task = ToDoModel()
                task.task = text
                task.status = 0
                db?.insertTask(task)
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
