package com.example.timemate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object {
        // Clave que usamos para saber qué tarea se está editando
        const val EXTRA_TASK_ID = "TASK_ID"
    }

    private var editingId: Int? = null
    private var remindAt: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activamos edge-to-edge para que la app se vea moderna
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_add_task)

        // Ajustamos el contenido para que no se meta debajo del notch o la barra de abajo
        applySystemBarsPadding()

        val tvTitleAdd = findViewById<TextView>(R.id.tvTitleAdd)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val btnPickReminder = findViewById<Button>(R.id.btnPickReminder)
        val tvReminderInfo = findViewById<TextView>(R.id.tvReminderInfo)
        val btnClearReminder = findViewById<Button>(R.id.btnClearReminder)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Muestra el estado del recordatorio en pantalla de forma clara
        fun renderReminder() {
            val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            tvReminderInfo.text = if (remindAt == null) {
                "Sin recordatorio"
            } else {
                "Recordatorio: ${fmt.format(remindAt!!)}"
            }

            // Solo se puede quitar el recordatorio si existe uno
            btnClearReminder.isEnabled = remindAt != null
        }

        // Revisamos si entramos aquí para crear una tarea nueva o editar una existente
        editingId = intent.getIntExtra(EXTRA_TASK_ID, -1).takeIf { it != -1 }

        if (editingId == null) {
            tvTitleAdd.text = "Agregar nueva tarea"
            btnSave.text = "Guardar tarea"
        } else {
            tvTitleAdd.text = "Editar tarea"
            btnSave.text = "Actualizar"

            // Cargamos la tarea que se va a editar
            val task = TaskStore.getById(this, editingId!!)
            task?.let {
                etTitle.setText(it.title)
                etDesc.setText(it.desc)
                remindAt = it.remindAt
            }
        }

        renderReminder()

        // Botón para elegir fecha y hora del recordatorio
        btnPickReminder.setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)

                    TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            cal.set(Calendar.HOUR_OF_DAY, hour)
                            cal.set(Calendar.MINUTE, minute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)

                            remindAt = cal.timeInMillis
                            renderReminder()
                            Toast.makeText(this, "Recordatorio seleccionado", Toast.LENGTH_SHORT).show()
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Quitar el recordatorio si el usuario lo desea
        btnClearReminder.setOnClickListener {
            remindAt = null
            renderReminder()
            Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
        }

        // Guardar o actualizar la tarea
        btnSave.setOnClickListener {
            val titleTxt = etTitle.text.toString().trim()
            val descTxt = etDesc.text.toString().trim()

            // No permitimos guardar tareas sin título
            if (titleTxt.isEmpty()) {
                Toast.makeText(this, "Escribe un título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si hay recordatorio, debe ser una fecha futura
            if (remindAt != null && remindAt!! <= System.currentTimeMillis()) {
                Toast.makeText(this, "El recordatorio debe ser en el futuro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editingId == null) {
                TaskStore.add(this, titleTxt, descTxt, remindAt)
                Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
            } else {
                TaskStore.update(this, editingId!!, titleTxt, descTxt, remindAt)
                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
            }

            // Cerramos esta pantalla y volvemos al listado
            finish()
        }
    }

    /**
     * Este método se encarga de que el contenido no se meta
     * debajo del notch ni de la barra de navegación,
     * sin necesidad de tocar los XML.
     */
    private fun applySystemBarsPadding() {
        val content = findViewById<ViewGroup>(android.R.id.content)
        val child: View = if (content.childCount > 0) content.getChildAt(0) else content

        ViewCompat.setOnApplyWindowInsetsListener(child) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        ViewCompat.requestApplyInsets(child)
    }
}
