package cz.maty.hledani111

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Jednoduchá obrazovka na jednorázové nastavení dvou oprávnění. */
class MainActivity : Activity() {

    private lateinit var status: TextView

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(24))
        }

        val title = TextView(this).apply {
            text = "Hledání 111"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
        }

        val intro = TextView(this).apply {
            text = "Když v telefonu vytočíš číslo 111, hovor se nezavolá a místo toho " +
                "se nahoře přes všechno zobrazí vyhledávací pole.\n\n" +
                "Aby to fungovalo, musíš povolit dvě věci:"
            setPadding(0, dp(12), 0, dp(12))
        }

        status = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(0, dp(8), 0, dp(16))
        }

        val overlayButton = Button(this).apply {
            text = "1) Povolit zobrazení přes ostatní aplikace"
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }

        val roleButton = Button(this).apply {
            text = "2) Nastavit jako přesměrování hovorů"
            setOnClickListener { requestCallRedirectionRole() }
        }

        val testButton = Button(this).apply {
            text = "Vyzkoušet overlay"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    IslandOverlay.show(this@MainActivity)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Nejdřív povol zobrazení přes ostatní aplikace",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        layout.addView(title)
        layout.addView(intro)
        layout.addView(status)
        layout.addView(overlayButton)
        layout.addView(roleButton)
        layout.addView(testButton)

        setContentView(ScrollView(this).apply { addView(layout) })
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun requestCallRedirectionRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION)) {
            Toast.makeText(this, "Tenhle telefon přesměrování hovorů nepodporuje", Toast.LENGTH_LONG).show()
            return
        }
        @Suppress("DEPRECATION")
        startActivityForResult(
            roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_REDIRECTION),
            REQUEST_ROLE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshStatus()
    }

    private fun refreshStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val roleManager = getSystemService(RoleManager::class.java)
        val roleOk = roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)

        val summary = if (overlayOk && roleOk) {
            "Hotovo – zkus vytočit 111."
        } else {
            "Dokonči zbývající kroky níže."
        }

        status.text = (if (overlayOk) "✅" else "❌") + " Zobrazení přes ostatní aplikace\n" +
            (if (roleOk) "✅" else "❌") + " Přesměrování hovorů\n\n" + summary
    }

    private companion object {
        const val REQUEST_ROLE = 42
    }
}
