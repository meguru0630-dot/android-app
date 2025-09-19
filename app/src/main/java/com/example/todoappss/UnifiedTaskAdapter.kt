package com.example.todoappss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UnifiedTaskAdapter(
    private val taskList: List<Pair<String, String>>,
    private val formatRepeatInfo: (String) -> String // 🔹 繰り返し情報を文字に変換する関数を受け取る
) : RecyclerView.Adapter<UnifiedTaskAdapter.ViewHolder>() {

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
        val (taskName, repeatText) = taskList[position]

        holder.textTaskName.text = taskName

        if (repeatText.isEmpty()) {
            holder.textRepeatInfo.visibility = View.GONE
        } else {
            // 🔹 数字を文字に変換して表示
            holder.textRepeatInfo.text = "繰り返し: ${formatRepeatInfo(repeatText)}"
            holder.textRepeatInfo.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = taskList.size
}
