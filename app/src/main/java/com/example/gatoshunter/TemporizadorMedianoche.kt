import android.os.Handler
import android.widget.TextView
import java.util.concurrent.TimeUnit
import android.icu.util.Calendar

class TemporizadorMedianoche(
    private val textView: TextView,
    private val onMidnightReached: () -> Unit
) {
    private val handler = Handler()

    private val runnable = object : Runnable {
        override fun run() {
            val millisRemaining = getMillisUntilMidnight()

            if (millisRemaining > 0) {
                val tiempo = formatMillis(millisRemaining)
                textView.text = "Tiempo restante: $tiempo"
                handler.postDelayed(this, 1000)
            } else {
                textView.text = "Tiempo restante: 00:00:00"
                onMidnightReached()
                handler.postDelayed(this, 1000) // Reinicia para el nuevo día
            }
        }
    }

    fun iniciar() {
        handler.post(runnable)
    }

    fun detener() {
        handler.removeCallbacks(runnable)
    }

    private fun getMillisUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    private fun formatMillis(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
