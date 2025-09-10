package com.example.todoappss

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.example.todoappss.com.example.todoappss.RepeatTaskAdapter


class MainActivity : AppCompatActivity() {

    private var taskList = mutableListOf<Pair<String, String>>()
//    shared

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // レイアウトを画面にセット

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//--------------------------------------
        taskList = loadTasks()

        val adapter = UnifiedTaskAdapter(taskList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
//----------------------------------------shared
        // ← 繰り返し付きタスクも入れてOK   //タスク名 to 繰り返し情報



        // RecyclerViewにアダプターをセット
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        // リストに区切り線を追加
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )


        val btnAdd = findViewById<Button>(R.id.btnAddTask)
        btnAdd.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create()

            val editTaskName = dialogView.findViewById<EditText>(R.id.editTaskName)
            val switchRepeat = dialogView.findViewById<SwitchCompat>(R.id.switchRepeat)
            val radioGroupRepeat = dialogView.findViewById<RadioGroup>(R.id.radioGroupRepeat)
            val radioDaily = dialogView.findViewById<RadioButton>(R.id.radioDaily)
            val radioWeekly = dialogView.findViewById<RadioButton>(R.id.radioWeekly)
            val weekdayCheckboxes = dialogView.findViewById<LinearLayout>(R.id.weekdayCheckboxes)

            val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
            btnClose.setOnClickListener { dialog.dismiss() }

            // スイッチでラジオグループ表示切替
            radioGroupRepeat.visibility = View.GONE
            weekdayCheckboxes.visibility = View.GONE

            switchRepeat.setOnCheckedChangeListener { _, isChecked ->
                radioGroupRepeat.visibility = if (isChecked) View.VISIBLE else View.GONE
                if (!isChecked) weekdayCheckboxes.visibility = View.GONE
            }

            // 曜日選択表示
            radioGroupRepeat.setOnCheckedChangeListener { _, checkedId ->
                weekdayCheckboxes.visibility = if (checkedId == R.id.radioWeekly) View.VISIBLE else View.GONE
            }

            val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)
            btnAdd.setOnClickListener {
                val taskName = editTaskName.text.toString().trim()
                if (taskName.isEmpty()) return@setOnClickListener

                var repeatInfo = ""
                if (switchRepeat.isChecked) {
                    repeatInfo = when (radioGroupRepeat.checkedRadioButtonId) {
                        R.id.radioDaily -> "0"
                        R.id.radioWeekly -> {
                            val selectedDays = mutableListOf<Int>()
                            val dayMap = listOf(2, 3, 4, 5, 6, 7, 1) // 月〜日
                            for (i in 0 until weekdayCheckboxes.childCount) {
                                val cb = weekdayCheckboxes.getChildAt(i) as CheckBox
                                if (cb.isChecked) selectedDays.add(dayMap[i])
                            }
                            selectedDays.joinToString(",")
                        }
                        else -> ""
                    }
                }

                taskList.add(taskName to repeatInfo)
                saveTasks()
                recyclerView.adapter = UnifiedTaskAdapter(taskList)
                dialog.dismiss()
            }

            dialog.show()
        }



        //繰り返しタスクリスト
        val btnRepeatList = findViewById<Button>(R.id.btnRepeatList)

        btnRepeatList.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_list, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<ImageButton>(R.id.btnCloseRepeatList).setOnClickListener {
                dialog.dismiss()
            }

            val repeatRecyclerView = dialogView.findViewById<RecyclerView>(R.id.repeatRecyclerView)
            repeatRecyclerView.layoutManager = LinearLayoutManager(this)

            val repeatTaskList = taskList.filter { it.second.isNotEmpty() } //変更

            repeatRecyclerView.adapter = UnifiedTaskAdapter(repeatTaskList) //変更repeatADからUniへ

            dialog.show()
        }

    }

//    -----------------------------------
//タスク保存
private fun saveTasks() {
    val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val jsonArray = org.json.JSONArray()
    for (task in taskList) {
        val obj = org.json.JSONObject()
        obj.put("name", task.first)
        obj.put("repeat", task.second)
        jsonArray.put(obj)
    }

    editor.putString("tasks", jsonArray.toString())
    editor.apply()
}

    // タスク読み込み
    private fun loadTasks(): MutableList<Pair<String, String>> {
        val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("tasks", null)

        val list = mutableListOf<Pair<String, String>>()
        if (jsonString != null) {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val repeat = obj.getString("repeat")
                list.add(name to repeat)
            }
        }
        return list
    }
}


