/* TypeHandlerList.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.1 $
 * Author: $Author: brett $
 * Date: $Date: 2011/11/28 14:13:42 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
 */
package com.tangluobo.rdp4j.rdp5.cliprdr;

import com.tangluobo.rdp4j.Packet;

import java.util.ArrayList;
import java.util.Iterator;

public class TypeHandlerList {

	ArrayList handlers = new ArrayList();
	private int count;

	public TypeHandlerList() {
		count = 0;
	}

	public void add(TypeHandler t) {
		if (t != null) {
			handlers.add(t);
			count++;
		}
	}

	public int count() {
		return count;
	}

	public TypeHandler getFirst() {
		if (count > 0)
			return (TypeHandler) handlers.get(0);
		else
			return null;
	}

	public TypeHandler getHandlerForFormat(int format) {
		TypeHandler handler = null;
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			handler = (TypeHandler) i.next();
			if ((handler != null) && handler.formatValid(format))
				return handler;
		}
		return null;
	}

	public Iterator iterator() {
		return handlers.iterator();
	}
}
