package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DateInfos implements List<DateList> {

	protected List<DateList> mDateInfos;

	public DateInfos() {
		mDateInfos = new ArrayList<DateList>();
	}

	@Override
	public boolean add(DateList date) {
		return mDateInfos.add(date);
	}

	@Override
	public void add(int location, DateList date) {
		mDateInfos.add(location, date);
	}

	@Override
	public boolean addAll(Collection<? extends DateList> dates) {
		return addAll(dates);
	}

	@Override
	public boolean addAll(int location, Collection<? extends DateList> dates) {
		return addAll(location, dates);
	}

	@Override
	public void clear() {
		mDateInfos.clear();
	}

	@Override
	public boolean contains(Object object) {
		if (!(object instanceof DateList)) {
			return false;
		}
		return mDateInfos.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> objects) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public DateList get(int location) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public List<DateList> getAll() {
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
	public Iterator<DateList> iterator() {
		return mDateInfos.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ListIterator<DateList> listIterator() {
		return mDateInfos.listIterator();
	}

	@Override
	public ListIterator<DateList> listIterator(int location) {
		return mDateInfos.listIterator(location);
	}

	@Override
	public DateList remove(int location) {
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
	public DateList set(int location, DateList date) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int size() {
		return mDateInfos.size();
	}

	@Override
	public List<DateList> subList(int start, int end) {
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

}
