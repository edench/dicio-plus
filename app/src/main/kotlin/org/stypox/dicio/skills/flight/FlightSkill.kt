package org.stypox.dicio.skills.flight

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.json.JSONException
import org.json.JSONObject
import org.stypox.dicio.sentences.Sentences.Flight
import org.stypox.dicio.util.ConnectionUtils
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Route from adsbdb.com, live position from adsb.lol. Both are free and need no key or account.
 */
class FlightSkill(
    correspondingSkillInfo: SkillInfo,
    data: StandardRecognizerData<Flight>
) : StandardRecognizerSkill<Flight>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Flight): SkillOutput {
        val spoken = when (inputData) {
            is Flight.Status -> inputData.flight
        } ?: ""
        val code = FlightNumberParser.parse(spoken)
            ?: return FlightOutput.NotUnderstood(spoken)

        return withContext(Dispatchers.IO) {
            try {
                val route = ConnectionUtils.getPageJson("$ADSBDB/$code")
                    .getJSONObject("response").getJSONObject("flightroute")
                val icao = route.optString("callsign_icao").ifEmpty { code }
                FlightOutput.Found(
                    code = route.optString("callsign_iata").ifEmpty { code },
                    airline = route.optJSONObject("airline")?.optString("name").orEmpty(),
                    from = place(route.optJSONObject("origin")),
                    to = place(route.optJSONObject("destination")),
                    position = livePosition(icao),
                )
            } catch (e: FileNotFoundException) {
                FlightOutput.NotFound(code) // adsbdb answers 404 for unknown callsigns
            } catch (e: JSONException) {
                FlightOutput.NotFound(code)
            } catch (e: IOException) {
                FlightOutput.NetworkError
            }
        }
    }

    private fun place(airport: JSONObject?): String {
        if (airport == null) return ""
        return airport.optString("municipality").ifEmpty { airport.optString("name") }
    }

    /** null when the aircraft isn't being tracked right now (on the ground, or not yet departed). */
    private fun livePosition(icaoCallsign: String): FlightOutput.Position? {
        val aircraft = try {
            ConnectionUtils.getPageJson("$ADSB_LOL/$icaoCallsign").optJSONArray("ac")
        } catch (e: IOException) {
            return null
        } catch (e: JSONException) {
            return null
        }
        val ac = aircraft?.optJSONObject(0) ?: return null
        val altitude = ac.opt("alt_baro")
        return FlightOutput.Position(
            altitudeFeet = (altitude as? Number)?.toInt(), // "ground" when taxiing
            speedKnots = ac.optDouble("gs").takeIf { !it.isNaN() }?.toInt(),
            lat = ac.optDouble("lat"),
            lon = ac.optDouble("lon"),
        )
    }

    companion object {
        private const val ADSBDB = "https://api.adsbdb.com/v0/callsign"
        private const val ADSB_LOL = "https://api.adsb.lol/v2/callsign"
    }
}
