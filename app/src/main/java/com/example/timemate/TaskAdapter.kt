package com.example.timemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val items: MutableList<TaskEntity>,
    private val onClick: (TaskEntity) -> Unit,
    private val onLongClick: (TaskEntity) -> Boolean
) : RecyclerView.Adapter<TaskAdapter.TaskVH>() {

    class TaskVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvTaskDesc)
        val ivBell: ImageView = itemView.findViewById(R.id.ivBell)
        val tvReminder: TextView = itemView.findViewById(R.id.tvTaskReminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskVH(view)
    }

    override fun onBindViewHolder(holder: TaskVH, position: Int) {
        val task = items[position]

        holder.tvTitle.text = task.title
        holder.tvDesc.text = if (task.desc.isBlank()) "(Sin descripción)" else task.desc

        if (task.remindAt != null) {
            holder.ivBell.visibility = View.VISIBLE
            holder.tvReminder.visibility = View.VISIBLE
            val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            holder.tvReminder.text = "Recordatorio: ${fmt.format(task.remindAt)}"
        } else {
            holder.ivBell.visibility = View.GONE
            holder.tvReminder.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(task) }
        holder.itemView.setOnLongClickListener { onLongClick(task) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<TaskEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

