package io.github.annusshahzad.solarx.data

import java.io.IOException

/**
 * One snapshot of the inverter's real-time data, in watts.
 *
 * @property solarW power produced by the PV array.
 * @property gridW power exchanged with the grid: positive when exporting, negative when importing.
 */
data class InverterReading(val solarW: Int, val gridW: Int) {

    /** Power the house is consuming: everything produced, minus what is exported. */
    val loadW: Int get() = solarW - gridW

    /** Power drawn from the grid, or 0 while exporting. */
    val gridImportW: Int get() = if (gridW < 0) -gridW else 0

    companion object {
        private const val SOLAR_INDEX = 9
        private const val GRID_LOW_INDEX = 29
        private const val GRID_HIGH_INDEX = 30

        /** Builds a reading from the `Data` array of a `ReadRealTimeData` response. */
        fun fromRealTimeData(data: List<Int>): InverterReading {
            if (data.size <= GRID_HIGH_INDEX) {
                throw IOException("Real-time data has ${data.size} values, expected more than $GRID_HIGH_INDEX")
            }
            // The grid register is reported in tenths of a watt.
            val gridW = signed32(low = data[GRID_LOW_INDEX], high = data[GRID_HIGH_INDEX]) / 10
            return InverterReading(solarW = data[SOLAR_INDEX], gridW = gridW)
        }

        /** Joins two unsigned 16-bit registers into one signed 32-bit value. */
        internal fun signed32(low: Int, high: Int): Int = (high shl 16) or (low and 0xFFFF)
    }
}
