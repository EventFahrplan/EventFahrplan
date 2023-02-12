package nerd.tuxmobil.fahrplan.congress.base

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.annotation.LayoutRes
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.models.Session


abstract class SessionsAdapter protected constructor(

    context: Context,
    @LayoutRes layout: Int,
    list: List<Session>,
    numDays: Int,
    useDeviceTimeZone: Boolean

) : ArrayAdapter<Session>(context, layout, list) {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_SEPARATOR = 1
        private const val NUM_VIEW_TYPES = 2
    }

    @get:JvmName("getAdapterContext")
    protected var context: Context
        private set

    private val list: List<Session>
    private val numDays: Int
    private lateinit var mapper: MutableList<Int>
    private lateinit var separatorStrings: MutableList<String>
    private lateinit var separatorsSet: MutableSet<Int>
    protected val useDeviceTimeZone: Boolean

    init {
        this.context = ContextThemeWrapper(context, R.style.Theme_Congress_NoActionBar)
        this.list = list
        this.numDays = numDays
        this.useDeviceTimeZone = useDeviceTimeZone
        initMapper()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        var viewHolder: ViewHolder? = null
        var viewHolderSeparator: ViewHolderSeparator? = null
        val type = getItemViewType(position)

        if (convertView == null) {
            // clone the inflater using the ContextThemeWrapper
            val inflater = context.getLayoutInflater()
            val localInflater = inflater.cloneInContext(context)

            when (type) {
                TYPE_ITEM -> {
                    rowView = localInflater.inflate(R.layout.session_list_item, parent, false)
                    viewHolder = ViewHolder(
                        rowView.requireViewByIdCompat(R.id.session_list_item_title_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_subtitle_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_speakers_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_language_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_day_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_time_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_room_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_duration_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_video_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_no_video_view),
                        rowView.requireViewByIdCompat(R.id.session_list_item_without_video_recording_view),
                    )
                    rowView.tag = viewHolder
                }
                TYPE_SEPARATOR -> {
                    rowView = localInflater.inflate(R.layout.session_list_separator, parent, false)
                    viewHolderSeparator = ViewHolderSeparator()
                    viewHolderSeparator.text = rowView.requireViewByIdCompat(R.id.session_list_separator_title_view)
                    rowView.tag = viewHolderSeparator
                }
                else -> {
                    error("Unknown type: $type")
                }
            }
        } else {
            rowView = convertView
            when (type) {
                TYPE_ITEM -> viewHolder = rowView.tag as ViewHolder
                TYPE_SEPARATOR -> viewHolderSeparator = rowView.tag as ViewHolderSeparator
                else -> error("Unknown type: $type")
            }
        }
        when (type) {
            TYPE_ITEM -> setItemContent(position, viewHolder!!)
            TYPE_SEPARATOR -> setSeparatorContent(position, viewHolderSeparator!!)
            else -> error("Unknown type: $type")
        }
        return rowView
    }

    fun getSession(position: Int) = list[mapper[position]]

    protected fun resetItemStyles(viewHolder: ViewHolder) {
        resetTextStyle(viewHolder.title, R.style.ScheduleListPrimary)
        resetTextStyle(viewHolder.subtitle, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.speakers, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.lang, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.day, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.time, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.room, R.style.ScheduleListSecondary)
        resetTextStyle(viewHolder.duration, R.style.ScheduleListSecondary)
        viewHolder.withoutVideoRecording.setImageResource(R.drawable.ic_novideo)
    }

    protected open fun resetTextStyle(textView: TextView, style: Int) {
        textView.setTextAppearance(context, style)
    }

    protected abstract fun setItemContent(position: Int, viewHolder: ViewHolder)

    private fun setSeparatorContent(position: Int, viewHolderSeparator: ViewHolderSeparator) {
        viewHolderSeparator.text!!.text = separatorStrings[mapper[position]]
    }

    override fun getViewTypeCount() = NUM_VIEW_TYPES

    override fun areAllItemsEnabled() = false

    override fun isEnabled(position: Int) = position !in separatorsSet

    override fun getItemViewType(position: Int) =
        if (position in separatorsSet) TYPE_SEPARATOR else TYPE_ITEM

    override fun getCount() = list.size + separatorsSet.size

    private fun initMapper() {
        separatorsSet = mutableSetOf()
        separatorStrings = mutableListOf()
        mapper = mutableListOf()

        var day: Int
        var lastDay = 0
        var sepCount = 0

        val daySeparator = context.getString(R.string.day_separator)
        for (index in list.indices) {
            val session = list[index]
            day = session.day
            val formattedDate = DateFormatter.newInstance(useDeviceTimeZone)
                .getFormattedDate(session.dateUTC, session.timeZoneOffset)

            if (day != lastDay) {
                lastDay = day
                if (numDays > 1) {
                    val dayDateSeparator = String.format(daySeparator, day, formattedDate)
                    separatorStrings.add(dayDateSeparator)
                    separatorsSet.add(index + sepCount)
                    mapper.add(sepCount)
                    sepCount++
                }
            }
            mapper.add(index)
        }
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        initMapper()
    }

    data class ViewHolder(
        var title: TextView,
        var subtitle: TextView,
        var speakers: TextView,
        var lang: TextView,
        var day: TextView,
        var time: TextView,
        var room: TextView,
        var duration: TextView,
        var noVideo: ImageView,
        var video: ImageView,
        var withoutVideoRecording: ImageView
    )

    internal class ViewHolderSeparator {
        var text: TextView? = null
    }

}
