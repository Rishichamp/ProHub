package com.prohub.assistant.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.prohub.assistant.data.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var todoRepository: TodoRepository

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val action = intent.getStringExtra("action") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = todoRepository.getById(taskId)
                if (task != null) {
                    when (action) {
                        "complete" -> {
                            todoRepository.update(task.copy(
                                completed = true,
                                status = "completed",
                                completionTime = System.currentTimeMillis()
                            ))
                            showToast(context, "Task completed!")
                        }
                        "snooze_15" -> {
                            todoRepository.update(task.copy(
                                snoozedUntil = System.currentTimeMillis() + 15 * 60 * 1000,
                                status = "snoozed"
                            ))
                            showToast(context, "Snoozed for 15 minutes")
                        }
                        "snooze_30" -> {
                            todoRepository.update(task.copy(
                                snoozedUntil = System.currentTimeMillis() + 30 * 60 * 1000,
                                status = "snoozed"
                            ))
                            showToast(context, "Snoozed for 30 minutes")
                        }
                        "snooze_60" -> {
                            todoRepository.update(task.copy(
                                snoozedUntil = System.currentTimeMillis() + 60 * 60 * 1000,
                                status = "snoozed"
                            ))
                            showToast(context, "Snoozed for 1 hour")
                        }
                        "reprioritize" -> {
                            todoRepository.update(task.copy(
                                priority = "high",
                                status = "urgent"
                            ))
                            showToast(context, "Task marked as high priority")
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(context, "Error: ${e.message}")
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
