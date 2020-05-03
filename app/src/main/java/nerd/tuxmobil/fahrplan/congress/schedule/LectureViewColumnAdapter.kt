package nerd.tuxmobil.fahrplan.congress.schedule

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Lecture

internal interface LectureViewEventsHandler : View.OnCreateContextMenuListener, View.OnClickListener

internal class LectureViewColumnAdapter(
        private val lectures: List<Lecture>,
        private val layoutParamsByLecture: Map<Lecture, LinearLayout.LayoutParams>,
        private val drawer: LectureViewDrawer,
        private val eventsHandler: LectureViewEventsHandler
) : RecyclerView.Adapter<LectureViewColumnAdapter.EventViewHolder>() {

    override fun onBindViewHolder(viewHolder: EventViewHolder, position: Int) {
        val lecture = lectures[position]
        viewHolder.itemView.tag = lecture
        viewHolder.itemView.layoutParams = layoutParamsByLecture[lecture]
        drawer.updateEventView(viewHolder.itemView, lecture)
    }

    override fun getItemCount(): Int = lectures.size

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): EventViewHolder {
        val eventLayout = LayoutInflater.from(parent.context).inflate(R.layout.event_layout, parent, false) as LinearLayout
        eventLayout.setOnCreateContextMenuListener(eventsHandler)
        eventLayout.setOnClickListener(eventsHandler)
        return EventViewHolder(eventLayout)
    }

    class EventViewHolder(eventLayout: LinearLayout) : RecyclerView.ViewHolder(eventLayout)
}