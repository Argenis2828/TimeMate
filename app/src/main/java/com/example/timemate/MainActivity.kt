package com.example.timemate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Referencias de UI
        rvTasks = findViewById(R.id.rvTasks)
        tvEmpty = findViewById(R.id.tvEmpty)

        // AppBar (Toolbar)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        // Navegación: subir al inicio de la lista
        topAppBar.setNavigationOnClickListener {
            rvTasks.smoothScrollToPosition(0)
        }

        // Acciones del menú
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }

        // Adapter
        adapter = TaskAdapter(
            items = mutableListOf(),
            onClick = { task ->
                val i = Intent(this, AddTaskActivity::class.java)
                i.putExtra(AddTaskActivity.EXTRA_TASK_ID, task.id)
                startActivity(i)
            },
            onLongClick = { true }
        )

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter
        rvTasks.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

        // Swipe para eliminar con deshacer
        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val currentList = TaskStore.getAll(this@MainActivity)
                val deletedTask = currentList.getOrNull(pos)

                if (deletedTask == null) {
                    cargarTareas()
                    return
                }

                TaskStore.delete(this@MainActivity, deletedTask.id)
                cargarTareas()

                Snackbar.make(rvTasks, getString(R.string.msg_task_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.msg_undo)) {
                        TaskStore.restore(this@MainActivity, deletedTask)
                        cargarTareas()
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipe).attachToRecyclerView(rvTasks)

        // FAB
        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarTareas()
    }

    private fun cargarTareas() {
        val tasks = TaskStore.getAll(this)
        adapter.update(tasks)

        val empty = tasks.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        rvTasks.visibility = if (empty) View.GONE else View.VISIBLE
    }

    // Diálogo "Acerca de"
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_body))
            .setPositiveButton("OK", null)
            .show()
    }
}
