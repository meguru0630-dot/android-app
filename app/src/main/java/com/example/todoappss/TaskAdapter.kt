package com.example.todoappss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val taskList: List<Pair<String, String>>,
    private val formatRepeatInfo: (String) -> String
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTaskName: TextView = view.findViewById(R.id.textTaskName)
        val textRepeatInfo: TextView = view.findViewById(R.id.textRepeatInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unified_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, repeat) = taskList[position]
        holder.textTaskName.text = name

        if (repeat.isEmpty()) {
            holder.textRepeatInfo.visibility = View.GONE
        } else {
            holder.textRepeatInfo.visibility = View.VISIBLE
            holder.textRepeatInfo.text = "繰り返し: ${formatRepeatInfo(repeat)}"
        }
    }

    override fun getItemCount(): Int = taskList.size
}
