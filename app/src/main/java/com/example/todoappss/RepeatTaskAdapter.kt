package com.example.todoappss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RepeatTaskAdapter(
    private val repeatTaskList: List<Pair<String, String>>,
    private val formatRepeatInfo: (String) -> String
) : RecyclerView.Adapter<RepeatTaskAdapter.ViewHolder>() {

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
        val (taskName, repeatInfo) = repeatTaskList[position]
        holder.textTaskName.text = taskName
        holder.textRepeatInfo.text =
            if (repeatInfo.isEmpty()) ""
            else "繰り返し: ${formatRepeatInfo(repeatInfo)}"
    }

    override fun getItemCount(): Int = repeatTaskList.size
}
