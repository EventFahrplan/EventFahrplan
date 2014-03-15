package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.text.format.Time;

public class DateInfos implements List<DateInfo> {

	protected List<DateInfo> mDateInfos;

	public DateInfos() {
		mDateInfos = new ArrayList<DateInfo>();
	}

	@Override
	public boolean add(DateInfo date) {
		return mDateInfos.add(date);
	}

	@Override
	public void add(int location, DateInfo date) {
		mDateInfos.add(location, date);
	}

	@Override
	public boolean addAll(Collection<? extends DateInfo> dates) {
		return addAll(dates);
	}

	@Override
	public boolean addAll(int location, Collection<? extends DateInfo> dates) {
		return addAll(location, dates);
	}

	@Override
	public void clear() {
		mDateInfos.clear();
	}

	@Override
	public boolean contains(Object object) {
		if (!(object instanceof DateInfo)) {
			return false;
		}
		return mDateInfos.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> objects) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public DateInfo get(int location) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public List<DateInfo> getAll() {
		return mDateInfos;
	}

	@Override
	public int indexOf(Object object) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean isEmpty() {
		return mDateInfos.isEmpty();
	}

	@Override
	public Iterator<DateInfo> iterator() {
		return mDateInfos.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ListIterator<DateInfo> listIterator() {
		return mDateInfos.listIterator();
	}

	@Override
	public ListIterator<DateInfo> listIterator(int location) {
		return mDateInfos.listIterator(location);
	}

	@Override
	public DateInfo remove(int location) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean remove(Object object) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean removeAll(Collection<?> dates) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean retainAll(Collection<?> dates) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public DateInfo set(int location, DateInfo date) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int size() {
		return mDateInfos.size();
	}

	@Override
	public List<DateInfo> subList(int start, int end) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Object[] toArray() {
		return mDateInfos.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public boolean sameDay(Time today, int lectureListDay) {
		String currentDate = DateHelper.getFormattedDate(today);
		for (DateInfo dateInfo : mDateInfos) {
			if ((dateInfo.dayIdx == lectureListDay) &&
				(dateInfo.date.equals(currentDate))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index of today
	 * @param hourOfDayChange Hour of day change (all lectures which start before count to the previous day)
	 * @param minuteOfDayChange Minute of day change
	 * @return dayIndex if found, -1 otherwise
	 */
	public int getIndexOfToday(int hourOfDayChange, int minuteOfDayChange) {
		if (mDateInfos == null || mDateInfos.isEmpty()) {
			return -1;
		}
		Time today = new Time();
		today.setToNow();
		today.hour -= hourOfDayChange;
		today.minute -= minuteOfDayChange;

		today.normalize(true);

		String currentDate = DateHelper.getFormattedDate(today);

		int dayIndex = -1;
		for (DateInfo dateInfo : mDateInfos) {
			dayIndex = dateInfo.getDayIndex(currentDate);
			if (dayIndex != -1) {
				return dayIndex;
			}
		}
		return dayIndex;
	}

}
