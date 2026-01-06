package com.example.timemate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object { const val EXTRA_TASK_ID = "TASK_ID" }

    private var editingId: Int? = null
    private var remindAt: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

        val tvTitleAdd = findViewById<TextView>(R.id.tvTitleAdd)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val btnPickReminder = findViewById<Button>(R.id.btnPickReminder)
        val tvReminderInfo = findViewById<TextView>(R.id.tvReminderInfo)
        val btnClearReminder = findViewById<Button>(R.id.btnClearReminder)
        val btnSave = findViewById<Button>(R.id.btnSave)

        fun renderReminder() {
            val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            tvReminderInfo.text = if (remindAt == null) {
                "Sin recordatorio"
            } else {
                "Recordatorio: ${fmt.format(remindAt!!)}"
            }

            // habilita/deshabilita botón de quitar
            btnClearReminder.isEnabled = remindAt != null
        }

        // Editar o Agregar
        editingId = intent.getIntExtra(EXTRA_TASK_ID, -1).takeIf { it != -1 }

        if (editingId == null) {
            tvTitleAdd.text = "Agregar nueva tarea"
            btnSave.text = "Guardar tarea"
        } else {
            tvTitleAdd.text = "Editar tarea"
            btnSave.text = "Actualizar"
            val task = TaskStore.getById(this, editingId!!)
            task?.let {
                etTitle.setText(it.title)
                etDesc.setText(it.desc)
                remindAt = it.remindAt
            }
        }

        renderReminder()

        // Elegir recordatorio (fecha + hora)
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
                            Toast.makeText(this, "Recordatorio seleccionado ", Toast.LENGTH_SHORT).show()
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

        // Quitar recordatorio
        btnClearReminder.setOnClickListener {
            remindAt = null
            renderReminder()
            Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
        }

        // Guardar / Actualizar
        btnSave.setOnClickListener {
            val titleTxt = etTitle.text.toString().trim()
            val descTxt = etDesc.text.toString().trim()

            if (titleTxt.isEmpty()) {
                Toast.makeText(this, "Escribe un título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            finish()
        }
    }
}
