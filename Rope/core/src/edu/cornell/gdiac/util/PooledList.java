 /*
 * PooledList.java
 *
 * We are at the 8th version of Java and we still do not have a LinkedList that
 * can remove nodes in O(1) time. So we were forced to write one.
 *
 * While we were doing that, we decided to turn this class into a teaching moment.
 * This class uses a LibGDX memory pool to allocate all of its internal entry nodes.
 * A memory pool does not delete old objects, but instead allows us to reused them
 * later.  This helps us cut down on pesky calls to the garbage collector.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.util;

import java.util.*;
import com.badlogic.gdx.utils.*;

/**
 * A doubly-linked list that uses LibGDX memory pools to optimize memory costs.
 *
 * This class supports O(1) deletion for internal nodes.  Simply use the entryIterator()
 * method to access the Entry nodes directly.
 */
public class PooledList<E> extends AbstractSequentialList<E> implements Iterable<E> {
	
	/**
	 * An internal node in the doubly-linked list */
	public class Entry implements Pool.Poolable {
		/** The entry value */
		private E value;
		/** The pointer to the next value */
		private Entry next;
		/** The pointer to the previous value */
		private Entry prev;
		
		/**
		 * Construct a new, isolated entry
		 */
		public Entry() {
			reset();
		}
		
		/**
		 * Returns the value for this entry
		 *
		 * @return the value for this entry
		 */
		public E getValue() {
			return value;
		}
		
		/**
		 * Removes this entry from the list in place
		 *
		 * This method supports O(1) deletion.
		 */
		public void remove() {
			if (prev != null) {
				prev.next = next;
			} else {
				head = next;
			}
			if (next != null) {
				next.prev = prev;
			} else {
				tail = prev;
			}
			size--;
			memory.free(this);
		}
		
		/**
		 * Resets this entry to an empty object for reuse later.
		 */
		public void reset() {
			value = null;
			next = null;
			prev = null;
		}
	}
	
	/** 
	 * Allocator for Entry objects
	 *
	 * This class does no preallocate, but will reuse objects that have
	 * been freed.
	 */
	private class EntryPool extends Pool<Entry> {
		
		/**
		 * Creates a new Pool for Entrys
		 */
		public EntryPool() {
			super();
		}
		
		/**
		 * Return a new entry object
		 *
		 * This method allocates a new object if there are no free ones.
		 *
		 * @return a new entry object
		 */
		protected Entry newObject () {
			return new Entry();
		}
	}

	/** Memory pool for reallocating deleted objects */
	private Pool<Entry> memory;
	
	/** The queue head */
	private Entry head;
	/** The queue tail */
	private Entry tail;
	/** The number of elements in the queue */
	private int size;
	
	/**
	 * Creates a new empty PooledList
	 */
	public PooledList() {
		memory = new EntryPool();
		head = null;
		tail = null;
		size = 0;
	}
	
	/**
	 * Returns the number of elements in this list.
	 *
	 * @return the number of elements in this list
	 */
	public int size() {
		return size;
	}
	
	/** 
	 * Removes the first element of the list.
	 *
	 * @return the element removed 
	 */
	public E poll() {
		return removeHead();
	}

	/** 
	 * Removes the last element of the list.
	 *
	 * @return the element removed 
	 */
	public E pop() {
		return removeTail();
	}
	
	/** 
	 * Adds an element to the end of the list
	 *
	 * @param e  the element to add
	 *
	 * @return whether the addition succeeeded
	 */
	public boolean push(E e) {
		return add(e);
	}
	
	/** 
	 * Returns the first element of the list.
	 *
	 * @return the first element of the list.
	 */
	public E getHead() {
		if (size == 0) {
			throw new IndexOutOfBoundsException();
		}

		return head.value;
	}

	/** 
	 * Returns the last element of the list.
	 *
	 * @return the last element of the list.
	 */
	public E getTail() {
		if (size == 0) {
			throw new IndexOutOfBoundsException();
		}

		return tail.value;
	}

	/** 
	 * Removes the first element of the list.
	 *
	 * @return the element removed 
	 */
	public E removeHead() {
		if (size == 0) {
			throw new IndexOutOfBoundsException();
		}

		Entry last = head;
		E value = head.value;
		head = head.next;
		if (size > 1) {
			head.prev = null;
		}
		size--;
		memory.free(last);
		return value;
	}

	/** 
	 * Removes the last element of the list.
	 *
	 * @return the element removed 
	 */
	public E removeTail() {
		if (size == 0) {
			throw new IndexOutOfBoundsException();
		}

		Entry last = tail;
		E value = tail.value;
		tail = tail.prev;
		if (size > 1) {
			tail.next = null;
		}
		size--;
		memory.free(last);
		return value;
	}
	
	/** 
	 * Adds an element to the end of the list
	 *
	 * @param e  the element to add
	 *
	 * @return whether the addition succeeeded
	 */
	public boolean add(E e) {
		Entry entry = memory.obtain();
		if (entry == null) {
			return false;
		}
		entry.value = e;
		entry.prev = tail;
		if (size > 0) {
			tail.next = entry;
		} else {
			head = entry;
		}
		tail = entry;
		size++;
		return true;
	}

	/** 
	 * Inserts an element to the list in place
	 *
	 * All elements after index are shifted one element to the right.
	 *
	 * @param index the position to add the element
	 * @param element the element to add
	 *
	 * @return whether the addition succeeeded
	 */
	// Inserts the specified element at the specified position in this list (optional operation).
	public void add(int index, E element) {
		if (index > size) {
			throw new IndexOutOfBoundsException();
		}

		// I should probably memory pool this later when I have time.
		Entry entry = memory.obtain();
		if (entry == null) {
			return;
		}
		entry.value = element;
		
		if (index == 0) {
			entry.next = head;
			if (size > 0) {
				head.prev = entry;
			} else {
				tail = entry;
			}
			head = entry;
		} else if (index == size) {
			entry.prev = tail;
			if (size > 0) {
				tail.next = entry;
			} else {
				head = entry;
			}
			tail = entry;
		} else {	
			Entry curr = head;
			for (int ii = 1; ii < index; ii++) {
				curr = curr.next;
			}
			curr.prev.next = entry;
			entry.prev = curr.prev;
			curr.prev = entry;
			entry.next = curr;
		}
		size++;
	}
	
	/**
	 * Returns the element at the specified position
	 *
	 * @param index the position to access
	 *
	 * @return the element at the specified position
	 */
	public E get(int index) {
		if (index == 0) {
			return head.value;
		} else if (index == size-1) {
			return tail.value;
		} else {	
			Entry curr = head;
			for (int ii = 1; ii < index; ii++) {
				curr = curr.next;
			}
			return curr.value;
		}
	}

	/**
	 * Removes the element at the specified position
	 *
	 * @param index the position to access
	 *
	 * @return the element removed
	 */
	public E remove(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException();
		}
		
		Entry last;
		E value;
		if (index == 0) {
			last = head;
			value = last.value;
			head = head.next;
			if (size > 1) {
				head.prev = null;
			}
		} else if (index == size) {
			last = tail;
			value = last.value;
			tail = tail.prev;
			if (size > 1) {
				tail.next = null;
			}
		} else {	
			Entry curr = head;
			for (int ii = 1; ii < index; ii++) {
				curr = curr.next;
			}
			last = curr;
			value = last.value;
			curr.prev.next = curr.next;
			curr.next.prev = curr.prev;
		}
		size--;
		memory.free(last);
		return value;
	}
	
	/**
	 * Replaces the element at the specified position
	 *
	 * @param index the position to replace the element
	 * @param element the element to replace with
	 *
	 * @return the original element
	 */
	public E set(int index, E element) {
		if (index >= size) {
			throw new IndexOutOfBoundsException();
		}
		
		E value;
		if (index == 0) {
			value = head.value;
			head.value = element;
		} else if (index == size) {
			value = tail.value;
			tail.value = element;
		} else {	
			Entry curr = head;
			for (int ii = 1; ii < index; ii++) {
				curr = curr.next;
			}
			value = curr.value;
			curr.value = element;
		}
		return value;
	}
	
	/** Cached reference to the value iterator */
	private ValueIterator values  = new ValueIterator();
	/** Cached reference to the entry iterator */
	private EntryIterator entries = new EntryIterator();

	/**
	 * Returns an iterator over the list values
	 *
	 * @return an iterator over the list values
	 */
	public Iterator<E> iterator() {
		values.reset();
		return values;
	}

	/**
	 * Returns a list iterator over the list values
	 *
	 * @return a list iterator over the list values
	 */
	public ListIterator<E> listIterator(int index) {
		values.reset(index);
		return values;
	}
	
	/**
	 * Returns an iterator over the list entries
	 *
	 * @return an iterator over the list entries
	 */
	public Iterator<Entry> entryIterator() {
		entries.reset();
		return entries;
	}

	/**
	 * A standard list iterator for values
	 */
	private class ValueIterator implements ListIterator<E> {
		/** The next entry to return */
		private Entry next;
		/** The previous entry returned */
		private Entry last;
	
		/**
		 * Creates a new iterator starting at the beginning
		 */
		public ValueIterator() {
			reset();
		}

		/**
		 * Resets the iterator to start at the beginning
		 */
		public void reset() {
			next = head;
			last = null;
		}
		
		/**
		 * Resets the iterator to start at the given position
		 * 
		 * @param index The position to start
		 */
		public void reset(int index) {
			if (index > size) {
				throw new IndexOutOfBoundsException();
			}
			last = null;
			next = head;
			for(int ii = 1; ii < index; ii++) {
				next = next.next;
			}
		}
		
		/**
		 * Inserts the specified element into the list
		 *
		 * The element is inserted before the next position
		 *
		 * @param e the element to insert
		 */
		public void add(E e) {
			Entry entry = memory.obtain();
			if (entry == null) {
				return;
			}
			entry.value = e;
			if (next == head) {
				entry.next = head;
				if (next != null) {
					next.prev = entry;
				}
				head = entry;
				next = head;
			} else if (next != null) {
				next.prev.next = entry;
				entry.prev = next.prev;
				entry.next = next;
				next.prev  = entry;
			} else {
				entry.prev = tail;
				if (tail != null) {
					tail.next = entry;
				}
				tail = entry;
			}
			size++;
		}
		
		/**
		 * Returns true if this iterator has more forward elements
		 *
		 * @return true if this iterator has more forward elements
		 */
		public boolean hasNext() {
			return next != null;
		}

		/**
		 * Returns true if this iterator has more backward elements
		 *
		 * @return true if this iterator has more backward elements
		 */
		public boolean hasPrevious() {
			return next != null && next.prev != null;
		}

		/**
		 * Returns the next element in the list (and advances the cursor)
		 *
		 * @return .the next element in the list
		 */
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			last = next;
			next = next.next;
			return last.value;
		}
		
		/**
		 * Returns the index of the element that would be returned by next().
		 *
		 * @return the index of the next element
		 */
		public int nextIndex() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
		
			Entry curr = head;
			int index = 0;
			while (curr != next) {
				curr = curr.next;
				index++;
			}
			return index;
		}
		
		/**
		 * Returns the previous element in the list (and rolls back the cursor)
		 *
		 * @return .the previous element in the list
		 */
		public E previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			
			next = next.prev;
			last = next;
			return next.value;		
		}

		/**
		 * Returns the index of the element that would be returned by previous().
		 *
		 * @return the index of the previous element
		 */
		public int previousIndex() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}		
			Entry curr = head;
			int index = 0;
			while (curr != next.prev) {
				curr = curr.next;
				index++;
			}
			return index;
		}
		
		/** 
		 * Removes the last element that was returned.
		 */
		public void remove() {
			if (last == null) {
				throw new NoSuchElementException();
			}
			
			if (last.prev == null) {
				head = last.next;
				if (head != null) {
					head.prev = null;
				}
			} else {
				last.prev.next = last.next;
			}
			
			if (last.next == null) {
				tail = last.prev;
				if (tail != null) {
					tail.next = null;
				}
			} else {
				last.next.prev = last.prev;
			}
			memory.free(last);
			last = null;
			size--;
		}

		/**
		 * Replaces the last element returned with this element
		 *
		 * @param e the element to replace with
		 */
		public void set(E e) {
			if (last == null) {
				throw new NoSuchElementException();
			}

			last.value = e;
		}
	}

	/**
	 * A standard list iterator for entries
	 */
	private class EntryIterator implements ListIterator<Entry> {
		/** The next entry to return */
		private Entry next;
		/** The previous entry returned */
		private Entry last;
	
		/**
		 * Creates a new iterator starting at the beginning
		 */
		public EntryIterator() {
			reset();
		}

		/**
		 * Resets the iterator to start at the beginning
		 */
		public void reset() {
			next = head;
			last = null;
		}
		
		/**
		 * Inserts the specified element into the list
		 *
		 * The element is inserted before the next position
		 *
		 * @param entry the element to insert
		 */
		public void add(Entry entry) {
			if (next == head) {
				entry.next = head;
				if (next != null) {
					next.prev = entry;
				}
				head = entry;
				next = head;
			} else if (next != null) {
				next.prev.next = entry;
				entry.prev = next.prev;
				entry.next = next;
				next.prev  = entry;
			} else {
				entry.prev = tail;
				if (tail != null) {
					tail.next = entry;
				}
				tail = entry;
			}
			size++;
		}
		
		/**
		 * Returns true if this iterator has more forward elements
		 *
		 * @return true if this iterator has more forward elements
		 */
		public boolean hasNext() {
			return next != null;
		}

		/**
		 * Returns true if this iterator has more backward elements
		 *
		 * @return true if this iterator has more backward elements
		 */
		public boolean hasPrevious() {
			return next != null && next.prev != null;		
		}

		/**
		 * Returns the next element in the list (and advances the cursor)
		 *
		 * @return .the next element in the list
		 */
		public Entry next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			last = next;
			next = next.next;
			return last;
		}
		
		/**
		 * Returns the index of the element that would be returned by next().
		 *
		 * @return the index of the next element
		 */
		public int nextIndex() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
		
			Entry curr = head;
			int index = 0;
			while (curr != next) {
				curr = curr.next;
				index++;
			}
			return index;
		}
		
		/**
		 * Returns the previous element in the list (and rolls back the cursor)
		 *
		 * @return .the previous element in the list
		 */
		public Entry previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			
			next = next.prev;
			last = next;
			return next;
		}

		/**
		 * Returns the index of the element that would be returned by previous().
		 *
		 * @return the index of the previous element
		 */
		public int previousIndex() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}		
			Entry curr = head;
			int index = 0;
			while (curr != next.prev) {
				curr = curr.next;
				index++;
			}
			return index;
		}
		
		/** 
		 * Removes the last element that was returned.
		 */
		public void remove() {
			if (last == null) {
				throw new NoSuchElementException();
			}
			
			if (last.prev == null) {
				head = last.next;
				if (head != null) {
					head.prev = null;
				}
			} else {
				last.prev.next = last.next;
			}
			
			if (last.next == null) {
				tail = last.prev;
				if (tail != null) {
					tail.next = null;
				}
			} else {
				last.next.prev = last.prev;
			}
			memory.free(last);
			last = null;
			size--;
		}

		/**
		 * Replaces the last element returned with this element
		 *
		 * @param e the element to replace with
		 */
		public void set(Entry entry) {
			if (last == null) {
				throw new NoSuchElementException();
			}
			
			entry.prev = last.prev;
			entry.next = last.next;
			if (last.prev != null) {
				last.prev.next = entry;
			} else {
				head = last;
			}
			
			if (last.next != null) {
				last.next.prev = entry;
			} else {
				tail = last;
			}
			memory.free(last);
			last = entry;
		}
	}
}