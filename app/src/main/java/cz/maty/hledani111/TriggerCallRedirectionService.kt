package cz.maty.hledani111

import android.net.Uri
import android.provider.Settings
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle

/**
 * Systém zavolá onPlaceCall při každém odchozím hovoru.
 * Když je vytáčené číslo přesně 111, hovor zrušíme a místo něj ukážeme overlay s vyhledávačem.
 */
class TriggerCallRedirectionService : CallRedirectionService() {

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        val dialed = (handle.schemeSpecificPart ?: "")
            .replace(Regex("[\\s\\-().]"), "")

        if (dialed == TRIGGER_NUMBER && Settings.canDrawOverlays(this)) {
            try {
                IslandOverlay.show(applicationContext)
                cancelCall()
                return
            } catch (e: Exception) {
                // když se overlay nepodaří zobrazit, radši hovor normálně proběhne
            }
        }
        placeCallUnmodified()
    }

    private companion object {
        const val TRIGGER_NUMBER = "111"
    }
}
