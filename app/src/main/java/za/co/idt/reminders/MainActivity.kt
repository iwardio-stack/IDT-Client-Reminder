
package za.co.idt.reminders

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val black = Color.rgb(12, 14, 13)
    private val cardBlack = Color.rgb(23, 27, 25)
    private val green = Color.rgb(142, 255, 0)
    private val orange = Color.rgb(255, 137, 0)
    private val white = Color.WHITE
    private val grey = Color.rgb(175, 181, 177)

    private lateinit var mainLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboard()
    }

    private fun background(color: Int, stroke: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 20f
            if (stroke != 0) setStroke(2, stroke)
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun buttonCard(
        title: String,
        subtitle: String,
        icon: String,
        color: Int,
        action: () -> Unit
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 16, 18, 16)
            background = background(cardBlack, color)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        card.addView(
            text(icon, 28f, color, true).apply {
                gravity = Gravity.CENTER
            }
        )

        card.addView(
            text(title, 17f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 3)
            }
        )

        card.addView(
            text(subtitle, 12f, grey).apply {
                gravity = Gravity.CENTER
            }
        )

        return card
    }

    private fun counterCard(
        number: String,
        title: String,
        color: Int
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = background(cardBlack, color)
            setPadding(12, 16, 12, 16)
        }

        card.addView(
            text(number, 30f, color, true).apply {
                gravity = Gravity.CENTER
            }
        )

        card.addView(
            text(title, 12f, white, true).apply {
                gravity = Gravity.CENTER
            }
        )

        return card
    }

    private fun sectionTitle(title: String) {
        contentLayout.addView(
            text(title, 17f, green, true).apply {
                setPadding(4, 20, 4, 10)
            }
        )
    }

    private fun dashboard() {

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
        }

        val scroll = ScrollView(this)

        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 12, 18, 20)
        }

        scroll.addView(contentLayout)

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 24, 20, 24)
            background = background(cardBlack, green)
        }

        header.addView(
            text("I.D.T.", 34f, green, true).apply {
                gravity = Gravity.CENTER
            }
        )

        header.addView(
            text("STEELWORKS & ENGINEERING", 15f, white, true).apply {
                gravity = Gravity.CENTER
            }
        )

        header.addView(
            text("WELDING  •  FABRICATION  •  INSTALLATION", 10f, orange).apply {
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }
        )

        contentLayout.addView(header)

        // Dashboard heading
        contentLayout.addView(
            text("CLIENT REMINDER", 25f, green, true).apply {
                setPadding(2, 22, 0, 2)
            }
        )

        contentLayout.addView(
            text("Installation  •  Collection  •  Follow-Up", 13f, grey)
        )

        // Action cards
        sectionTitle("QUICK ACTIONS")

        val actionGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val installCard = buttonCard(
            "New Installation",
            "Schedule a client installation",
            "🔧",
            green
        ) {
            showMessage(
                "New Installation",
                "The installation booking form is the next feature we will build."
            )
        }

        val collectionCard = buttonCard(
            "Ready for Collection",
            "Notify client their item is ready",
            "📦",
            orange
        ) {
            showMessage(
                "Ready for Collection",
                "The collection recording form is the next feature we will build."
            )
        }

        row1.addView(
            installCard,
            LinearLayout.LayoutParams(0, 220, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
        )

        row1.addView(
            collectionCard,
            LinearLayout.LayoutParams(0, 220, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
        )

        actionGrid.addView(row1)

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row2.addView(
            buttonCard(
                "Calendar",
                "View scheduled jobs",
                "📅",
                green
            ) {
                showMessage("Calendar", "Calendar screen coming next.")
            },
            LinearLayout.LayoutParams(0, 155, 1f).apply {
                setMargins(0, 12, 8, 0)
            }
        )

        row2.addView(
            buttonCard(
                "All Reminders",
                "View and manage reminders",
                "📋",
                orange
            ) {
                showMessage("Reminders", "Reminders screen coming next.")
            },
            LinearLayout.LayoutParams(0, 155, 1f).apply {
                setMargins(8, 12, 0, 0)
            }
        )

        actionGrid.addView(row2)
        contentLayout.addView(actionGrid)

        // Summary cards
        sectionTitle("OVERVIEW")

        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        summaryRow.addView(
            counterCard("0", "Upcoming Jobs", green),
            LinearLayout.LayoutParams(0, 115, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
        )

        summaryRow.addView(
            counterCard("0", "Ready to Collect", orange),
            LinearLayout.LayoutParams(0, 115, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
        )

        contentLayout.addView(summaryRow)

        // Upcoming reminders
        sectionTitle("NEXT REMINDERS")

        val remindersBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 18)
            background = background(cardBlack, green)
        }

        remindersBox.addView(
            text("No reminders scheduled yet.", 14f, grey, true)
        )

        remindersBox.addView(
            text(
                "Your installations and collections will appear here once added.",
                12f,
                grey
            ).apply {
                setPadding(0, 6, 0, 0)
            }
        )

        contentLayout.addView(remindersBox)

        // Bottom navigation
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(4, 8, 4, 8)
            setBackgroundColor(cardBlack)
        }

        val navItems = listOf(
            "⌂\nDashboard",
            "📅\nCalendar",
            "＋\nNew Job",
            "📋\nReminders",
            "⚙\nSettings"
        )

        navItems.forEachIndexed { index, label ->

            val nav = TextView(this).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(if (index == 0) green else grey)
                setPadding(2, 6, 2, 6)
                setOnClickListener {
                    when (index) {
                        0 -> dashboard()
                        1 -> showMessage("Calendar", "Calendar screen coming next.")
                        2 -> showMessage("New Job", "New installation and collection form coming next.")
                        3 -> showMessage("Reminders", "Reminders screen coming next.")
                        4 -> showMessage("Settings", "App settings coming next.")
                    }
                }
            }

            bottom.addView(
                nav,
                LinearLayout.LayoutParams(0, 60, 1f)
            )
        }

        mainLayout.addView(
            scroll,
            LinearLayout.LayoutParams(0, 0, 1f)
        )

        mainLayout.addView(bottom)

        setContentView(mainLayout)
    }

    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
