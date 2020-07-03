package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session

internal interface SessionViewEventsHandler : View.OnCreateContextMenuListener, View.OnClickListener

internal class SessionViewColumnAdapter(
        private val sessions: List<Session>,
        private val layoutParamsBySession: Map<Session, LinearLayout.LayoutParams>,
        private val drawer: SessionViewDrawer,
        private val eventsHandler: SessionViewEventsHandler
) : RecyclerView.Adapter<SessionViewColumnAdapter.SessionViewHolder>() {

    override fun onBindViewHolder(viewHolder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        viewHolder.itemView.tag = session
        viewHolder.itemView.layoutParams = layoutParamsBySession[session]
        drawer.updateSessionView(viewHolder.itemView, session)
    }

    override fun getItemCount(): Int = sessions.size

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): SessionViewHolder {
        val sessionLayout = LayoutInflater.from(parent.context).inflate(R.layout.session_layout, parent, false) as LinearLayout
        sessionLayout.setOnCreateContextMenuListener(eventsHandler)
        sessionLayout.setOnClickListener(eventsHandler)
        return SessionViewHolder(sessionLayout)
    }

    class SessionViewHolder(sessionLayout: LinearLayout) : RecyclerView.ViewHolder(sessionLayout)
}