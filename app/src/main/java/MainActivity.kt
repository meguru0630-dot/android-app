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
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject
import androidx.recyclerview.widget.ItemTouchHelper



class MainActivity : AppCompatActivity() {

    private var taskList = mutableListOf<Pair<String, String>>()
    private lateinit var adapter: UnifiedTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // 🔹 タスクをロード
        taskList = loadTasks().toMutableList()

        // 🔹 今日の繰り返しタスクを追加
        addTodayRepeatTasks()

        adapter = UnifiedTaskAdapter(taskList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        // スワイプ削除
        // RecyclerView にスワイプ削除を追加
        val itemTouchHelperMain = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                taskList.removeAt(position)  // メインリストから削除
                saveTasks()                  // 永続化
                recyclerView.adapter?.notifyItemRemoved(position)
            }
        })
        itemTouchHelperMain.attachToRecyclerView(recyclerView)


        // 🔹 タスク追加ボタン
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

            radioGroupRepeat.visibility = View.GONE
            weekdayCheckboxes.visibility = View.GONE

            switchRepeat.setOnCheckedChangeListener { _, isChecked ->
                radioGroupRepeat.visibility = if (isChecked) View.VISIBLE else View.GONE
                if (!isChecked) weekdayCheckboxes.visibility = View.GONE
            }

            radioGroupRepeat.setOnCheckedChangeListener { _, checkedId ->
                weekdayCheckboxes.visibility = if (checkedId == R.id.radioWeekly) View.VISIBLE else View.GONE
            }

            val btnAddTask = dialogView.findViewById<Button>(R.id.btnAdd)
            btnAddTask.setOnClickListener {
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
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            dialog.show()
        }

        // 🔹 繰り返しタスク一覧表示
        val btnRepeatList = findViewById<Button>(R.id.btnRepeatList)
        btnRepeatList.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_list, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create()

            dialogView.findViewById<ImageButton>(R.id.btnCloseRepeatList).setOnClickListener {
                dialog.dismiss()
            }

            val repeatRecyclerView = dialogView.findViewById<RecyclerView>(R.id.repeatRecyclerView)
            repeatRecyclerView.layoutManager = LinearLayoutManager(this)

            val repeatTaskList = taskList.filter { it.second.isNotEmpty() }
            repeatRecyclerView.adapter = UnifiedTaskAdapter(repeatTaskList)

            //スワイプ削除　繰り返しリストから
            btnRepeatList.setOnClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_list, null)
                val dialog = AlertDialog.Builder(this).setView(dialogView).create()

                dialogView.findViewById<ImageButton>(R.id.btnCloseRepeatList).setOnClickListener {
                    dialog.dismiss()
                }

                val repeatRecyclerView = dialogView.findViewById<RecyclerView>(R.id.repeatRecyclerView)
                repeatRecyclerView.layoutManager = LinearLayoutManager(this)

                // 繰り返しタスクだけを抽出
                val repeatTaskList = taskList.filter { it.second.isNotEmpty() }.toMutableList()
                val repeatAdapter = UnifiedTaskAdapter(repeatTaskList)
                repeatRecyclerView.adapter = repeatAdapter

                // 🔹 繰り返し一覧にスワイプ削除を追加
                val itemTouchHelperRepeat = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean = false

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        val removedTask = repeatTaskList[position]

                        // 🔹 元の taskList からも該当タスクを削除
                        taskList.removeIf { it.first == removedTask.first && it.second == removedTask.second }

                        saveTasks() // 永続化
                        repeatTaskList.removeAt(position)
                        repeatAdapter.notifyItemRemoved(position)
                    }
                })
                itemTouchHelperRepeat.attachToRecyclerView(repeatRecyclerView)

                dialog.show()
            }


            dialog.show()
        }
    }

    // 🔹 タスク保存
    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonArray = JSONArray()
        for (task in taskList) {
            val obj = JSONObject()
            obj.put("name", task.first)
            obj.put("repeat", task.second)
            jsonArray.put(obj)
        }

        editor.putString("tasks", jsonArray.toString())
        editor.apply()
    }

    // 🔹 タスク読み込み
    private fun loadTasks(): MutableList<Pair<String, String>> {
        val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("tasks", null)

        val list = mutableListOf<Pair<String, String>>()
        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val repeat = obj.getString("repeat")
                list.add(name to repeat)
            }
        }
        return list
    }

    // 🔹 今日の繰り返しタスクを追加
    private fun addTodayRepeatTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val lastAddedDay = sharedPreferences.getInt("lastAddedDay", -1)

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK) // 1=日曜, 2=月曜...7=土曜

        if (lastAddedDay == today) return // すでに追加済みなら何もしない

        val newTasks = mutableListOf<Pair<String, String>>()

        for (task in taskList) {
            val repeatInfo = task.second
            if (repeatInfo.isNotEmpty()) {
                val repeatDays = repeatInfo.split(",").map { it.toInt() }
                if (repeatDays.contains(0) || repeatDays.contains(today)) {
                    // 単発タスクとして追加
                    newTasks.add(task.first to "")
                }
            }
        }

        if (newTasks.isNotEmpty()) {
            taskList.addAll(newTasks)
            saveTasks()
        }

        // 今日の日付を保存
        sharedPreferences.edit().putInt("lastAddedDay", today).apply()
    }
}
