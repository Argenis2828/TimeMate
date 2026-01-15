package com.example.timemate

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    // lista donde se muestran las tareas
    private lateinit var rvTasks: RecyclerView

    // mensaje que se muestra cuando no hay tareas
    private lateinit var tvEmpty: TextView

    // adaptador que controla los datos del recycler
    private lateinit var adapter: TaskAdapter

    // preferencias para guardar datos internos de la app
    private lateinit var prefs: SharedPreferences

    companion object {
        // codigo interno para el permiso de notificaciones
        private const val REQ_NOTIF = 101

        // archivo donde se guardan los datos internos de la app
        private const val PREFS = "timemate_prefs"

        // marca si ya se le pidió el permiso al usuario
        private const val ASKED = "asked_notif"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // activamos edge to edge para que la app se vea moderna
        enableEdgeToEdge()

        // indicamos que nosotros mismos controlamos los espacios de pantalla
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // cargamos la pantalla principal
        setContentView(R.layout.activity_main)

        // aplicamos los espacios correctos por notch y barras
        applySystemBarsPadding()

        // cargamos las preferencias internas
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE)

        // pedimos el permiso solo una vez
        if (!prefs.getBoolean(ASKED, false)) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIF)
            prefs.edit().putBoolean(ASKED, true).apply()
        }

        // obtenemos los controles de la pantalla
        rvTasks = findViewById(R.id.rvTasks)
        tvEmpty = findViewById(R.id.tvEmpty)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        // al tocar el icono del toolbar subimos la lista hacia arriba
        topAppBar.setNavigationOnClickListener {
            rvTasks.smoothScrollToPosition(0)
        }

        // manejamos las opciones del menu superior
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> { showAboutDialog(); true }
                else -> false
            }
        }

        // creamos el adaptador de tareas
        adapter = TaskAdapter(
            mutableListOf(),
            onClick = {
                // tocar una tarea permite editarla
                val i = Intent(this, AddTaskActivity::class.java)
                i.putExtra(AddTaskActivity.EXTRA_TASK_ID, it.id)
                startActivity(i)
            },
            onLongClick = { true }
        )

        // configuramos el recycler view
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        // activamos deslizar para borrar tareas
        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            // no usamos movimiento vertical
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            // cuando el usuario desliza, se elimina la tarea
            override fun onSwiped(vh: RecyclerView.ViewHolder, d: Int) {
                val pos = vh.adapterPosition
                val list = TaskStore.getAll(this@MainActivity)
                val deleted = list.getOrNull(pos) ?: run { cargarTareas(); return }

                TaskStore.delete(this@MainActivity, deleted.id)
                cargarTareas()

                // mostramos un mensaje para deshacer la acción
                val root = findViewById<View>(android.R.id.content)
                Snackbar.make(root, getString(R.string.msg_task_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.msg_undo)) {
                        TaskStore.restore(this@MainActivity, deleted)
                        cargarTareas()
                    }.show()
            }
        }

        ItemTouchHelper(swipe).attachToRecyclerView(rvTasks)

        // boton flotante para agregar tareas nuevas
        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // recargamos las tareas al volver a la pantalla
        cargarTareas()
    }

    // carga todas las tareas y muestra u oculta el mensaje vacío
    private fun cargarTareas() {
        val tasks = TaskStore.getAll(this)
        adapter.update(tasks)
        tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        rvTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
    }

    // muestra la ventana acerca de la aplicacion
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_body))
            .setPositiveButton("OK", null)
            .show()
    }

    // resultado del permiso de notificaciones
    override fun onRequestPermissionsResult(code: Int, p: Array<out String>, r: IntArray) {
        super.onRequestPermissionsResult(code, p, r)
        if (code == REQ_NOTIF) {
            if (r.isNotEmpty() && r[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "notificaciones activadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ajusta los espacios para que nada quede debajo del notch o barras
    private fun applySystemBarsPadding() {
        val root = findViewById<ViewGroup>(android.R.id.content)
        val child = if (root.childCount > 0) root.getChildAt(0) else root

        ViewCompat.setOnApplyWindowInsetsListener(child) { v, i ->
            val b = i.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            i
        }

        ViewCompat.requestApplyInsets(child)
    }
}
