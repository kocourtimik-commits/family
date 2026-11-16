package cz.maty.hledani111

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Černý "ostrov" s vyhledávacím polem nahoře přes všechno ostatní.
 * Žije mimo jakoukoli Activity, takže přežije i zánik CallRedirectionService.
 */
object IslandOverlay {

    private var root: IslandRoot? = null
    private var windowManager: WindowManager? = null

    fun show(context: Context) {
        if (root != null) return

        val app = context.applicationContext
        val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = IslandRoot(app) { dismiss() }

        val metrics = app.resources.displayMetrics
        val width = minOf(metrics.widthPixels * 0.92f, 520f * metrics.density).toInt()

        val params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            // Pod stavovým řádkem. Chceš ho výš/níž? Změň to "+ 6" (v dp).
            y = statusBarHeight(app) + (6 * metrics.density).toInt()
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        }

        wm.addView(view, params)
        root = view
        windowManager = wm

        view.animateIn()
        view.focusInput()
    }

    fun dismiss() {
        val view = root ?: return
        val wm = windowManager
        root = null
        windowManager = null

        view.hideKeyboard()
        view.animateOut {
            try {
                wm?.removeView(view)
            } catch (e: Exception) {
                // okno už je pryč
            }
        }
    }

    private fun statusBarHeight(context: Context): Int {
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            context.resources.getDimensionPixelSize(id)
        } else {
            (24 * context.resources.displayMetrics.density).toInt()
        }
    }
}

private fun Context.dpToPx(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

private class IslandRoot(
    context: Context,
    private val onClose: () -> Unit
) : FrameLayout(context) {

    private fun dp(value: Int): Int = context.dpToPx(value)

    private val input = EditText(context).apply {
        hint = "Hledat…"
        setHintTextColor(Color.parseColor("#8E8E93"))
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        setBackgroundColor(Color.TRANSPARENT)
        isSingleLine = true
        inputType = InputType.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        setPadding(dp(12), dp(8), dp(8), dp(8))
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    init {
        val pill = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.BLACK)
                cornerRadius = dp(30).toFloat()
            }
            setPadding(dp(18), dp(10), dp(8), dp(10))
        }

        val searchIcon = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_search)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
        }

        val closeButton = TextView(context).apply {
            text = "✕"
            setTextColor(Color.parseColor("#8E8E93"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setOnClickListener { onClose() }
        }

        pill.addView(searchIcon)
        pill.addView(input)
        pill.addView(closeButton)

        addView(
            pill,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        input.setOnEditorActionListener { _, actionId, event ->
            val enter = event != null &&
                event.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                search()
                true
            } else {
                false
            }
        }
    }

    private fun search() {
        val query = input.text.toString().trim()
        if (query.isEmpty()) return

        val url = "https://www.google.com/search?q=" + Uri.encode(query)
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // žádný prohlížeč – nic se nestane
        }
        onClose()
    }

    fun focusInput() {
        input.requestFocus()
        input.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }

    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)
    }

    fun animateIn() {
        alpha = 0f
        scaleX = 0.4f
        scaleY = 0.6f
        translationY = -dp(24).toFloat()
        post {
            pivotX = width / 2f
            pivotY = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(380)
                .setInterpolator(OvershootInterpolator(0.8f))
                .start()
        }
    }

    fun animateOut(onEnd: () -> Unit) {
        animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.6f)
            .translationY(-dp(16).toFloat())
            .setDuration(180)
            .withEndAction(onEnd)
            .start()
    }

    // Tlačítko Zpět zavře ostrov
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) onClose()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    // Klepnutí mimo ostrov ho zavře
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            onClose()
            return true
        }
        return super.onTouchEvent(event)
    }
}
