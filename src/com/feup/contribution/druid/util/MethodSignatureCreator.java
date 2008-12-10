/*
 * This file is part of drUID.
 * 
 * drUID is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * drUID is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with drUID.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.feup.contribution.druid.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class MethodSignatureCreator {
	public static String createSignature(IMethod method) throws JavaModelException {
		StringBuffer buf = new StringBuffer();
		if (!method.isConstructor())
			buf.append(Signature.getSimpleName(Signature.toString(method.getReturnType())) + " ");
		buf.append(method.getElementName());
		try {
			String[] types = method.getParameterTypes();
			String[] names = method.getParameterNames();
			buf.append("(");
			for (int j = 0; j < types.length; ++j) {
				if (j != 0)	buf.append(", ");
				buf.append(Signature.getSimpleName(Signature.toString(types[j])) + " " + names[j]);
			}
			buf.append(")");
		} catch (Exception e) {e.printStackTrace();}
		return buf.toString();
	}
}
