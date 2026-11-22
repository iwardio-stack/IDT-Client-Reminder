
package za.co.idt.reminders

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : Activity() {

    private val black = Color.rgb(12, 14, 13)
    private val cardBlack = Color.rgb(23, 27, 25)
    private val green = Color.rgb(165, 255, 0)
    private val orange = Color.rgb(255, 137, 0)
    private val white = Color.WHITE
    private val grey = Color.rgb(175, 181, 177)

    private lateinit var mainLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("idt_jobs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboard()
    }

    private fun bg(color: Int, stroke: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 22f
            if (stroke != 0) setStroke(2, stroke)
        }
    }

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun makeButton(
        title: String,
        subtitle: String,
        color: Int,
        action: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 22, 18, 22)
            background = bg(cardBlack, color)
            isClickable = true
            isFocusable = true
            addView(label(title, 20f, color, true))
            addView(label(subtitle, 13f, white).apply {
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            })
            setOnClickListener { action() }
        }
    }

    private fun setupScreen(title: String) {
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 18)
        }

        header.addView(label("I.D.T.", 30f, green, true))
        header.addView(label("STEELWORKS & ENGINEERING", 15f, white, true))
        header.addView(label(title, 23f, orange, true).apply {
            setPadding(0, 24, 0, 4)
        })

        mainLayout.addView(header)

        val scroll = ScrollView(this)
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 24)
        }

        scroll.addView(contentLayout)
        mainLayout.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(mainLayout)
        addBottomNav()
    }

    private fun addBottomNav() {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(4, 10, 4, 10)
            setBackgroundColor(cardBlack)
        }

        val items = listOf(
            "Home" to { dashboard() },
            "Jobs" to { savedJobs() },
            "+ New" to { newJob(-1) }
        )

        items.forEach { item ->
            val b = TextView(this).apply {
                text = item.first
                textSize = 14f
                setTextColor(green)
                gravity = Gravity.CENTER
                setPadding(12, 14, 12, 14)
                setTypeface(null, Typeface.BOLD)
                setOnClickListener { item.second() }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        }

        mainLayout.addView(nav)
    }

    private fun dashboard() {
        setupScreen("CLIENT REMINDER")

        contentLayout.addView(label(
            "Installation • Collection • Follow-Up",
            14f, grey
        ))

        addCard("NEW JOB", "Add a client and installation details", green) {
            newJob(-1)
        }

        addCard("ALL JOBS", "View, edit or delete saved jobs", orange) {
            savedJobs()
        }

        addCard("COLLECTIONS", "Schedule collection for a saved job", green) {
            savedJobs()
        }

        contentLayout.addView(label("OVERVIEW", 18f, green, true).apply {
            setPadding(0, 24, 0, 12)
        })

        val jobs = loadJobs()
        contentLayout.addView(label(
            "Saved Jobs: ${jobs.length()}",
            17f, white, true
        ).apply {
            setPadding(16, 18, 16, 18)
            background = bg(cardBlack, green)
        })

        contentLayout.addView(label(
            "RECENT JOBS",
            18f, orange, true
        ).apply {
            setPadding(0, 24, 0, 12)
        })

        if (jobs.length() == 0) {
            contentLayout.addView(label(
                "No jobs saved yet.",
                14f, grey
            ).apply {
                setPadding(16, 18, 16, 18)
                background = bg(cardBlack)
            })
        } else {
            for (i in jobs.length() - 1 downTo maxOf(0, jobs.length() - 3)) {
                val job = jobs.getJSONObject(i)
                contentLayout.addView(label(
                    "${job.optString("client")}\nInstallation: ${job.optString("date")} at ${job.optString("time")}",
                    15f, white
                ).apply {
                    setPadding(16, 16, 16, 16)
                    background = bg(cardBlack)
                })
            }
        }
    }

    private fun addCard(
        title: String,
        subtitle: String,
        color: Int,
        action: () -> Unit
    ) {
        val card = makeButton(title, subtitle, color, action)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 12, 0, 0)
        contentLayout.addView(card, params)
    }

    private fun newJob(editIndex: Int) {
        val jobs = loadJobs()

        val existing = if (editIndex >= 0 && editIndex < jobs.length()) {
            jobs.getJSONObject(editIndex)
        } else {
            null
        }

        setupScreen(if (existing == null) "NEW JOB" else "EDIT JOB")

        val client = field("Client name *")
        val phone = field("WhatsApp number *")
        val project = field("Job / installation description *")
        val address = field("Installation address")
        val date = field("Installation date (tap to select)")
        val time = field("Installation time (tap to select)")
        val notes = field("Notes")

        if (existing != null) {
            client.setText(existing.optString("client"))
            phone.setText(existing.optString("phone"))
            project.setText(existing.optString("project"))
            address.setText(existing.optString("address"))
            date.setText(existing.optString("date"))
            time.setText(existing.optString("time"))
            notes.setText(existing.optString("notes"))
        }

        date.isFocusable = false
        date.isClickable = true
        date.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                date.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        time.isFocusable = false
        time.isClickable = true
        time.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                time.setText(String.format("%02d:%02d", hour, minute))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        addFormField("CLIENT NAME", client)
        addFormField("WHATSAPP NUMBER", phone)
        addFormField("JOB DESCRIPTION", project)
        addFormField("ADDRESS", address)
        addFormField("INSTALLATION DATE", date)
        addFormField("INSTALLATION TIME", time)
        addFormField("NOTES", notes)

        val save = makeButton(
            if (existing == null) "SAVE JOB" else "SAVE CHANGES",
            "Save client and installation details",
            green
        ) {
            if (client.text.toString().trim().isEmpty() ||
                phone.text.toString().trim().isEmpty() ||
                project.text.toString().trim().isEmpty() ||
                date.text.toString().trim().isEmpty() ||
                time.text.toString().trim().isEmpty()) {

                AlertDialog.Builder(this)
                    .setMessage("Please fill in Client name, WhatsApp number, Job description, installation date and time.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                val updatedJob = JSONObject().apply {
                    put("client", client.text.toString().trim())
                    put("phone", phone.text.toString().trim())
                    put("project", project.text.toString().trim())
                    put("address", address.text.toString().trim())
                    put("date", date.text.toString())
                    put("time", time.text.toString())
                    put("notes", notes.text.toString())
                    put("collectionDate", existing?.optString("collectionDate", "") ?: "")
                    put("collectionTime", existing?.optString("collectionTime", "") ?: "")
                }

                if (editIndex >= 0 && editIndex < jobs.length()) {
                    jobs.put(editIndex, updatedJob)
                } else {
                    jobs.put(updatedJob)
                }

                saveJobs(jobs)

                AlertDialog.Builder(this)
                    .setTitle(if (existing == null) "JOB SAVED" else "CHANGES SAVED")
                    .setMessage("Client and job details saved.")
                    .setPositiveButton("OK") { _, _ -> savedJobs() }
                    .show()
            }
        }

        contentLayout.addView(save, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 150
        ).apply {
            setMargins(0, 20, 0, 20)
        })
    }

    private fun field(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(grey)
            setTextColor(white)
            textSize = 16f
            setPadding(16, 14, 16, 14)
            background = bg(cardBlack, green)
        }
    }

    private fun addFormField(title: String, input: EditText) {
        contentLayout.addView(label(title, 13f, orange, true).apply {
            setPadding(4, 16, 4, 6)
        })
        contentLayout.addView(input, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun loadJobs(): JSONArray {
        return try {
            JSONArray(prefs.getString("jobs", "[]"))
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun saveJobs(jobs: JSONArray) {
        prefs.edit().putString("jobs", jobs.toString()).apply()
    }

    private fun savedJobs() {
        setupScreen("SAVED JOBS")

        val jobs = loadJobs()

        if (jobs.length() == 0) {
            contentLayout.addView(label("No jobs saved yet.", 16f, grey))
            return
        }

        for (i in jobs.length() - 1 downTo 0) {
            val job = jobs.getJSONObject(i)

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 18, 16, 18)
                background = bg(cardBlack, green)
            }

            card.addView(label(job.optString("client"), 20f, green, true))
            card.addView(label(job.optString("project"), 16f, white).apply {
                setPadding(0, 8, 0, 8)
            })
            card.addView(label(
                "Installation: ${job.optString("date")} at ${job.optString("time")}",
                14f, orange
            ))
            card.addView(label(
                "WhatsApp: ${job.optString("phone")}",
                14f, grey
            ).apply {
                setPadding(0, 8, 0, 0)
            })
            card.addView(label(job.optString("address"), 14f, grey).apply {
                setPadding(0, 6, 0, 0)
            })

            val collectionDate = job.optString("collectionDate", "")
            val collectionTime = job.optString("collectionTime", "")
            val collectionLabel = if (collectionDate.isNotBlank()) {
                "Collection: $collectionDate at $collectionTime"
            } else {
                "Collection not scheduled"
            }

            card.addView(label(collectionLabel, 14f, orange, true).apply {
                setPadding(0, 12, 0, 6)
            })

            card.addView(actionButton("INSTALLATION WHATSAPP", green) {
                sendInstallationWhatsApp(job)
            })

            card.addView(actionButton("SCHEDULE COLLECTION", orange) {
                scheduleCollection(i)
            })

            card.addView(actionButton("COLLECTION WHATSAPP", green) {
                sendCollectionWhatsApp(job)
            })

            card.addView(actionButton("EDIT JOB", white) {
                newJob(i)
            })

            card.addView(actionButton("DELETE JOB", Color.RED) {
                confirmDelete(i, job.optString("client"))
            })

            contentLayout.addView(card, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            })
        }
    }

    private fun actionButton(
        title: String,
        color: Int,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(color)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(12, 18, 12, 18)
            background = bg(black, color)
            setOnClickListener { action() }
        }
    }

    private fun confirmDelete(index: Int, clientName: String) {
        AlertDialog.Builder(this)
            .setTitle("DELETE JOB?")
            .setMessage("Are you sure you want to delete the job for $clientName?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DELETE") { _, _ ->
                val jobs = loadJobs()
                if (index >= 0 && index < jobs.length()) {
                    jobs.remove(index)
                    saveJobs(jobs)
                    savedJobs()
                }
            }
            .show()
    }

    private fun scheduleCollection(index: Int) {
        val dateCal = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, day ->
            val chosenDate = String.format("%02d/%02d/%04d", day, month + 1, year)

            TimePickerDialog(this, { _, hour, minute ->
                val chosenTime = String.format("%02d:%02d", hour, minute)

                val jobs = loadJobs()
                if (index >= 0 && index < jobs.length()) {
                    val job = jobs.getJSONObject(index)
                    job.put("collectionDate", chosenDate)
                    job.put("collectionTime", chosenTime)
                    saveJobs(jobs)

                    AlertDialog.Builder(this)
                        .setTitle("COLLECTION SCHEDULED")
                        .setMessage("$chosenDate at $chosenTime")
                        .setPositiveButton("OK") { _, _ -> savedJobs() }
                        .show()
                }
            }, dateCal.get(Calendar.HOUR_OF_DAY), dateCal.get(Calendar.MINUTE), true).show()

        }, dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH),
            dateCal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun sendInstallationWhatsApp(job: JSONObject) {
        val client = job.optString("client")
        val date = job.optString("date")
        val time = job.optString("time")

        val message = "Good day $client. This is a friendly reminder that your installation is scheduled for $date at $time. If this is not a convenient time, please reply to this message so we can reschedule. Kind regards, I.D.T. Steelworks & Engineering."

        openWhatsApp(job.optString("phone"), message)
    }

    private fun sendCollectionWhatsApp(job: JSONObject) {
        val client = job.optString("client")
        val date = job.optString("collectionDate", "")
        val time = job.optString("collectionTime", "")

        if (date.isBlank() || time.isBlank()) {
            AlertDialog.Builder(this)
                .setMessage("Please schedule a collection date and time first.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val message = "Good day $client. Your order is ready for collection. Your collection is scheduled for $date at $time. If this is not a convenient time, please reply to this message so we can reschedule. Kind regards, I.D.T. Steelworks & Engineering."

        openWhatsApp(job.optString("phone"), message)
    }

    private fun openWhatsApp(phoneInput: String, message: String) {
        var phone = phoneInput.filter { it.isDigit() }

        if (phone.startsWith("0")) {
            phone = "27" + phone.substring(1)
        }

        if (phone.isBlank()) {
            AlertDialog.Builder(this)
                .setMessage("This job does not have a valid WhatsApp number.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val url = "https://wa.me/$phone?text=${Uri.encode(message)}"

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("WhatsApp unavailable")
                .setMessage("Could not open WhatsApp. Please check that WhatsApp is installed.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
