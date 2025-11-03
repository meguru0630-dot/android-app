package com.example.todoappss

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    // 🔹 単発タスクと繰り返しタスクを独立管理
    private var taskList = mutableListOf<Pair<String, String>>() // タスク名 to 繰り返し情報
    private var repeatTaskList = mutableListOf<Pair<String, String>>()

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var repeatAdapter: RepeatTaskAdapter

    // 🔹追加：日付チェック用Handler
    private val dateCheckHandler = android.os.Handler()
    private var lastCheckedDay = -1
    private val dateCheckRunnable = object : Runnable {
        override fun run() {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            if (lastCheckedDay != -1 && lastCheckedDay != today) {
                addTodayRepeatTasks()
                taskAdapter.notifyDataSetChanged()
            }
            lastCheckedDay = today
            dateCheckHandler.postDelayed(this, 10_000) // 10秒ごとにチェック
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<Button>(R.id.btnAddTask)
        val btnRepeatList = findViewById<Button>(R.id.btnRepeatList)

        // 🔹 データを読み込み
        taskList = loadTasks().toMutableList()
        repeatTaskList = loadRepeatTasks().toMutableList()

        // 🔹 今日分の繰り返しタスクを自動追加
        addTodayRepeatTasks()

        // 🔹 単発タスクリスト設定
        taskAdapter = TaskAdapter(taskList, ::formatRepeatInfo)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // 🔹 スワイプ削除（単発タスク）
        val itemTouchHelperMain = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                taskList.removeAt(position)
                saveTasks()
                taskAdapter.notifyItemRemoved(position)
            }
        })
        itemTouchHelperMain.attachToRecyclerView(recyclerView)

        // 🔹 タスク追加ボタン
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
                // 🔹キーボードを閉じる
                val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                val currentFocusView = dialogView.findFocus()
                currentFocusView?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

                // 🔹切り替え
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
                            val dayMap = listOf(2,3,4,5,6,7,1)
                            for (i in 0 until weekdayCheckboxes.childCount) {
                                val cb = weekdayCheckboxes.getChildAt(i) as CheckBox
                                if (cb.isChecked) selectedDays.add(dayMap[i])
                            }
                            selectedDays.joinToString(",")
                        }
                        else -> ""
                    }

                    repeatTaskList.add(taskName to repeatInfo)
                    saveRepeatTasks()

                    // メイン taskList にも追加
                    taskList.add(taskName to repeatInfo)
                    saveTasks()
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                } else {
                    taskList.add(taskName to "")
                    saveTasks()
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                }

                dialog.dismiss()
            }

            dialog.show()
        }

        // 🔹 繰り返しタスク一覧
        btnRepeatList.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_list, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create()

            dialogView.findViewById<ImageButton>(R.id.btnCloseRepeatList).setOnClickListener {
                dialog.dismiss()
            }

            val repeatRecyclerView = dialogView.findViewById<RecyclerView>(R.id.repeatRecyclerView)
            repeatRecyclerView.layoutManager = LinearLayoutManager(this)
            repeatAdapter = RepeatTaskAdapter(repeatTaskList, ::formatRepeatInfo)
            repeatRecyclerView.adapter = repeatAdapter

            val itemTouchHelperRepeat = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    repeatTaskList.removeAt(position)
                    saveRepeatTasks()
                    repeatAdapter.notifyItemRemoved(position)
                }
            })
            itemTouchHelperRepeat.attachToRecyclerView(repeatRecyclerView)

            dialog.show()
        }

        // 🔹追加：日付チェックをスタート
        dateCheckHandler.post(dateCheckRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🔹追加：Handlerを止める
        dateCheckHandler.removeCallbacks(dateCheckRunnable)
    }

    // 🔹 単発タスク保存
    private fun saveTasks() {
        val prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val arr = JSONArray()
        taskList.forEach { pair ->
            val obj = JSONObject()
            obj.put("name", pair.first)
            obj.put("repeat", pair.second)
            arr.put(obj)
        }
        prefs.edit().putString("tasks", arr.toString()).apply()
    }

    // 🔹 タスク読み込み
    private fun loadTasks(): MutableList<Pair<String, String>> {
        val prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val json = prefs.getString("tasks", null) ?: return mutableListOf()
        val arr = JSONArray(json)
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(obj.getString("name") to obj.getString("repeat"))
        }
        return list
    }

    // 🔹 繰り返しタスク保存
    private fun saveRepeatTasks() {
        val prefs = getSharedPreferences("RepeatPrefs", MODE_PRIVATE)
        val arr = JSONArray()
        repeatTaskList.forEach {
            val obj = JSONObject()
            obj.put("name", it.first)
            obj.put("repeat", it.second)
            arr.put(obj)
        }
        prefs.edit().putString("repeat_tasks", arr.toString()).apply()
    }

    // 🔹 繰り返しタスク読み込み
    private fun loadRepeatTasks(): MutableList<Pair<String, String>> {
        val prefs = getSharedPreferences("RepeatPrefs", MODE_PRIVATE)
        val json = prefs.getString("repeat_tasks", null) ?: return mutableListOf()
        val arr = JSONArray(json)
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(obj.getString("name") to obj.getString("repeat"))
        }
        return list
    }

    // 🔹 今日の繰り返しタスクを追加
    private fun addTodayRepeatTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        val lastAddedDay = sharedPreferences.getInt("lastAddedDay", -1)
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)

        if (lastAddedDay == today) return

        val newTasks = repeatTaskList.filter { task ->
            val repeatDays = task.second.split(",").mapNotNull { it.toIntOrNull() }
            repeatDays.contains(0) || repeatDays.contains(today)
        }.map { it.first to it.second }

        if (newTasks.isNotEmpty()) {
            taskList.addAll(newTasks)
            saveTasks()
        }

        sharedPreferences.edit().putInt("lastAddedDay", today).apply()
    }

    // 🔹 数字の繰り返し情報を日本語に変換
    private fun formatRepeatInfo(repeatInfo: String): String {
        if (repeatInfo.isEmpty()) return ""
        if (repeatInfo == "0") return "毎日"

        val dayMap = mapOf(
            1 to "日", 2 to "月", 3 to "火", 4 to "水",
            5 to "木", 6 to "金", 7 to "土"
        )

        return repeatInfo.split(",").mapNotNull { num ->
            num.toIntOrNull()?.let { dayMap[it] }
        }.joinToString("・")
    }
}
