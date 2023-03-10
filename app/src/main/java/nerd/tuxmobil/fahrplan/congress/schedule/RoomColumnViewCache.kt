package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.schedule.RoomColumnViewCache.RoomColumnView

internal interface RoomColumnViewProvider {
    /**
     * Get the [RoomColumnView] for the given [roomName]
     */
    fun get(
        roomName: String,
        columnWidth: Int
    ): RoomColumnViewCache.RoomColumnView
}

/**
 * Cache for reusing RecycleView/Adapter inside the [FahrplanFragment] when stuff updates in the columns/Rooms
 */
internal class RoomColumnViewCache(
    private val context: Context,
    private val eventsHandler: SessionViewEventsHandler,
    onGetNormalizedBoxHeight: () -> Int
): RoomColumnViewProvider {

    private val roomColumnViewByRoomName: MutableMap<String, RoomColumnView> = mutableMapOf()

    private val layoutCalculator = LayoutCalculator(onGetNormalizedBoxHeight())

    /**
     * Look for a [RoomColumnView] for the room with the name [roomName].
     * If such a view does not exist, create one.
     */
    override fun get(
        roomName: String,
        columnWidth: Int,
    ): RoomColumnView {
        return roomColumnViewByRoomName.getOrPut(roomName) {
            createRoomColumnView(columnWidth)
        }
    }

    /**
     * In case there is no corresponding RoomColumnView in the cache, create one
     */
    private fun createRoomColumnView(
        columnWidth: Int,
    ): RoomColumnView {
        val recyclerView = RecyclerView(context).apply {
            setHasFixedSize(true)
            setFadingEdgeLength(0)
            isNestedScrollingEnabled = false // enables flinging
            layoutManager = LinearLayoutManager(context)
            layoutParams = LayoutParams(columnWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        return RoomColumnView(recyclerView)
    }

    inner class RoomColumnView(val recyclerView: RecyclerView) {

        /**
         * add session data to adapter
         *
         * Only replace the adapter if the [roomData]-sessions changed, that is,
         * a session changed its name or startTime or ..  (see Session.equals()).
         * But we don't update when e.g. a session was only favoured/highlighted
         *
         */
        fun updateData(
            roomData: RoomData,
            conference: Conference,
            drawer: SessionViewDrawer,
        ) {
            val adapter = recyclerView.adapter
            val currAdapterData = (adapter as? SessionViewColumnAdapter)?.sessions

            if (currAdapterData == roomData.sessions) {
                adapter.notifyDataSetChanged() //e.g. favoured/highlighted session
            } else {
                /**
                 * Complicated to just update the old adapter with new data,
                 * because Adapter-Layout-Parameter might have changed as well (which we would need to recalculate as well)
                 * Hence, we just create a new adapter
                 */
                addNewAdapter(roomData, conference, drawer)
            }
        }

        private fun addNewAdapter(
            roomData: RoomData,
            conference: Conference,
            drawer: SessionViewDrawer
        ) {
            val layoutParamsBySession = layoutCalculator.calculateLayoutParams(roomData, conference)

            recyclerView.adapter = SessionViewColumnAdapter(
                sessions = roomData.sessions,
                layoutParamsBySession = layoutParamsBySession,
                drawer = drawer,
                eventsHandler = eventsHandler
            )
        }
    }
}

