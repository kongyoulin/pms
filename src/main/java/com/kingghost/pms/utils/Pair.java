package com.kingghost.pms.utils;

import java.io.Serializable;
import java.util.Comparator;

public class Pair<H, T> implements Serializable {
	private H head;
	private T tail;

	public static <U, V> Pair<U, V> make(U head, V tail) {
		return new Pair<>(head, tail);
	}

	// Constructor
	public Pair(H head, T tail) {
		this.head = head;
		this.tail = tail;
	}

	public H getHead() {
		return head;
	}

	public void setHead(H head) {
		this.head = head;
	}

	public T getTail() {
		return tail;
	}

	public void setTail(T tail) {
		this.tail = tail;
	}

	@Override
	public String toString() {
		return "Pair [head=" + head + ", tail=" + tail + "]";
	}

	@Override
	public int hashCode() {
		return head.hashCode() + tail.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		Pair pairObj = (Pair) obj;
		return (head.equals(pairObj.getHead()) && tail.equals(pairObj.getTail()));
	}
}