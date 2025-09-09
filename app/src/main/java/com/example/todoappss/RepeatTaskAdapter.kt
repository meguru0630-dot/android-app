package com.example.todoappss.com.example.todoappss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoappss.R

class RepeatTaskAdapter(
    private val repeatTasks: List<Pair<String, String>>
) : RecyclerView.Adapter<RepeatTaskAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTaskName: TextView = view.findViewById(R.id.textTaskName)
        val textRepeatType: TextView = view.findViewById(R.id.textRepeatType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repeat_task, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = repeatTasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (taskName, repeatType) = repeatTasks[position]
        holder.textTaskName.text = taskName
        holder.textRepeatType.text = "繰り返し: $repeatType"
    }
}
