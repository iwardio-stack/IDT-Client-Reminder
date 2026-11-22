
package za.co.idt.reminders

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
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

    private fun button(
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
            "+ New" to { newJob() }
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
            newJob()
        }

        addCard("READY FOR COLLECTION", "Prepare a collection reminder", orange) {
            collectionInfo()
        }

        addCard("ALL REMINDERS", "View your saved client jobs", green) {
            savedJobs()
        }

        addCard("CALENDAR", "View scheduled jobs", orange) {
            savedJobs()
        }

        contentLayout.addView(label("OVERVIEW", 18f, green, true).apply {
            setPadding(0, 24, 0, 12)
        })

        val count = loadJobs().length()

        contentLayout.addView(label(
            "Saved Jobs: $count",
            17f, white, true
        ).apply {
            setPadding(16, 18, 16, 18)
            background = bg(cardBlack, green)
        })

        contentLayout.addView(label(
            "NEXT REMINDERS",
            18f, orange, true
        ).apply {
            setPadding(0, 24, 0, 12)
        })

        if (count == 0) {
            contentLayout.addView(label(
                "No jobs saved yet.",
                14f, grey
            ).apply {
                setPadding(16, 18, 16, 18)
                background = bg(cardBlack)
            })
        } else {
            val jobs = loadJobs()
            for (i in jobs.length() - 1 downTo maxOf(0, jobs.length() - 3)) {
                val job = jobs.getJSONObject(i)
                contentLayout.addView(label(
                    "${job.optString("client")}\\n${job.optString("project")}\\n${job.optString("date")} at ${job.optString("time")}",
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
        val card = button(title, subtitle, color, action)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 12, 0, 0)
        contentLayout.addView(card, params)
    }

    private fun newJob() {
        setupScreen("NEW JOB / REMINDER")

        val client = field("Client name *")
        val phone = field("WhatsApp number *")
        val project = field("Job / installation description *")
        val address = field("Installation address")
        val date = field("Installation date (tap to select)")
        val time = field("Installation time (tap to select)")
        val notes = field("Notes")

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

        addFormField("CLIENT", client)
        addFormField("WHATSAPP", phone)
        addFormField("JOB DESCRIPTION", project)
        addFormField("ADDRESS", address)
        addFormField("INSTALLATION DATE", date)
        addFormField("INSTALLATION TIME", time)
        addFormField("NOTES", notes)

        val save = button("SAVE JOB", "Save this client and installation", green) {
            if (client.text.toString().trim().isEmpty() ||
                phone.text.toString().trim().isEmpty() ||
                project.text.toString().trim().isEmpty()) {

                AlertDialog.Builder(this)
                    .setMessage("Please fill in Client name, WhatsApp number and Job description.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                val jobs = loadJobs()
                val job = JSONObject().apply {
                    put("client", client.text.toString())
                    put("phone", phone.text.toString())
                    put("project", project.text.toString())
                    put("address", address.text.toString())
                    put("date", date.text.toString())
                    put("time", time.text.toString())
                    put("notes", notes.text.toString())
                    put("collection", false)
                }
                jobs.put(job)
                prefs.edit().putString("jobs", jobs.toString()).apply()

                AlertDialog.Builder(this)
                    .setTitle("JOB SAVED")
                    .setMessage("The client and job details have been saved on this phone.")
                    .setPositiveButton("OK") { _, _ -> dashboard() }
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
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun loadJobs(): JSONArray {
        return try {
            JSONArray(prefs.getString("jobs", "[]"))
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun savedJobs() {
        setupScreen("SAVED JOBS")

        val jobs = loadJobs()

        if (jobs.length() == 0) {
            contentLayout.addView(label(
                "No jobs have been saved yet.",
                16f, grey
            ))
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
                "Date: ${job.optString("date")}   Time: ${job.optString("time")}",
                14f, orange
            ))
            card.addView(label(
                "WhatsApp: ${job.optString("phone")}",
                14f, grey
            ).apply {
                setPadding(0, 8, 0, 0)
            })
            card.addView(label(
                job.optString("address"),
                14f, grey
            ).apply {
                setPadding(0, 6, 0, 0)
            })

            card.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle(job.optString("client"))
                    .setMessage(
                        "Job: ${job.optString("project")}\\n" +
                        "Date: ${job.optString("date")}\\n" +
                        "Time: ${job.optString("time")}\\n" +
                        "WhatsApp: ${job.optString("phone")}\\n" +
                        "Address: ${job.optString("address")}\\n" +
                        "Notes: ${job.optString("notes")}"
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }

            contentLayout.addView(card, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            })
        }
    }

    private fun collectionInfo() {
        AlertDialog.Builder(this)
            .setTitle("Collection Reminder")
            .setMessage("Collection reminders will be linked to saved jobs in our next step.")
            .setPositiveButton("OK", null)
            .show()
    }
}
