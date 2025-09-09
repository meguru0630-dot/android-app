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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // レイアウトを画面にセット

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // 仮のデータ（リストに表示する内容）
        val taskList = mutableListOf(
            "掃除" to "",
            "歯磨き" to "0",
            "ゴミ出し" to "2,5"
        )

        // ← 繰り返し付きタスクも入れてOK   //タスク名 to 繰り返し情報



        // RecyclerViewにアダプターをセット
        val adapter = UnifiedTaskAdapter(taskList)
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
}
