package com.example.anaekran

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var appGrid: GridLayout
    private lateinit var search: EditText
    private val apps = mutableListOf<AppInfo>()

    data class AppInfo(
        val name: String,
        val icon: android.graphics.drawable.Drawable,
        val packageName: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(17, 18, 28)
        window.navigationBarColor = Color.rgb(17, 18, 28)

        loadApps()
        createHomeScreen()
        showApps("")
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        packageManager.queryIntentActivities(intent, 0)
            .forEach {
                apps.add(
                    AppInfo(
                        it.loadLabel(packageManager).toString(),
                        it.loadIcon(packageManager),
                        it.activityInfo.packageName
                    )
                )
            }

        apps.sortBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun background(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun createHomeScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 12)
            setBackgroundColor(Color.rgb(17, 18, 28))
        }

        val clock = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            textSize = 58f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        root.addView(clock)

        val date = TextView(this).apply {
            text = SimpleDateFormat(
                "EEEE, d MMMM",
                Locale("tr", "TR")
            ).format(Date())

            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(date)

        val greeting = TextView(this).apply {
            text = "Merhaba 👋"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 20)
        }

        root.addView(greeting)

        search = EditText(this).apply {
            hint = "  Uygulama ara..."
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            singleLine = true
            background = background(
                Color.rgb(43, 44, 58), 40f
            )
            setPadding(18, 5, 18, 5)
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 52
            )
        )

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int,
                before: Int, count: Int
            ) {
                showApps(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val scroll = ScrollView(this)

        appGrid = GridLayout(this).apply {
            columnCount = 4
            setPadding(0, 25, 0, 20)
        }

        scroll.addView(appGrid)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = background(
                Color.rgb(43, 44, 58), 35f
            )
            setPadding(12, 12, 12, 12)
        }

        root.addView(
            dock,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 75
            )
        )

        val dockNames = listOf("Telefon", "Mesajlar", "Kamera", "Ayarlar")

        dockNames.forEach { name ->
            val item = TextView(this).apply {
                text = name
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(5, 8, 5, 8)
                setOnClickListener {
                    openAppByName(name)
                }
            }

            dock.addView(
                item,
                LinearLayout.LayoutParams(0, 55, 1f)
            )
        }

        setContentView(root)
    }

    private fun showApps(query: String) {
        appGrid.removeAllViews()

        val filtered = apps.filter {
            it.name.contains(query, ignoreCase = true)
        }

        filtered.forEach { app ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(3, 8, 3, 12)
            }

            val icon = ImageView(this).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            item.addView(
                icon,
                LinearLayout.LayoutParams(55, 55)
            )

            val label = TextView(this).apply {
                text = app.name
                textSize = 11f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                maxLines = 2
                setPadding(0, 5, 0, 0)
            }

            item.addView(
                label,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 38
                )
            )

            item.setOnClickListener {
                val launch = packageManager
                    .getLaunchIntentForPackage(app.packageName)

                if (launch != null) {
                    startActivity(launch)
                }
            }

            appGrid.addView(
                item,
                GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(
                        GridLayout.UNDEFINED, 1f
                    )
                }
            )
        }
    }

    private fun openAppByName(name: String) {
        val target = when (name) {
            "Telefon" -> "com.google.android.dialer"
            "Mesajlar" -> "com.google.android.apps.messaging"
            "Kamera" -> "com.android.camera"
            "Ayarlar" -> "com.android.settings"
            else -> ""
        }

        val launch = packageManager
            .getLaunchIntentForPackage(target)

        if (launch != null) {
            startActivity(launch)
        } else {
            Toast.makeText(
                this, "$name bulunamadı",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
