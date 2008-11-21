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
