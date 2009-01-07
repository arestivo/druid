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

import java.util.ArrayList;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

public class MethodAnnotation implements IAnnotation{
	private String elementName;
	private IJavaElement method;
	private String source;
	private ArrayList<IMemberValuePair> valuePairs = new ArrayList<IMemberValuePair>();
	private SourceRange sourceRange;
	
	@SuppressWarnings("restriction")
	public MethodAnnotation(IMethod method, String elementName, String source, int offset) throws JavaModelException{
		this.method = method;
		this.elementName = elementName;
		this.source = source;
		sourceRange = new SourceRange(method.getSourceRange().getOffset() + offset, source.length());
	}
	
	public String getElementName() {
		return elementName;
	}

	public IMemberValuePair[] getMemberValuePairs() throws JavaModelException {
		return valuePairs.toArray(new IMemberValuePair[valuePairs.size()]);
	}

	public ISourceRange getNameRange() throws JavaModelException {
		return new SourceRange(getSourceRange().getOffset()+1, elementName.length());
	}

	public int getOccurrenceCount() {
		return 1;
	}

	public boolean exists() {
		return true;
	}

	public IJavaElement getAncestor(int ancestorType) {
		return method;
	}

	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	public IResource getCorrespondingResource() throws JavaModelException {
		return method.getResource();
	}

	public int getElementType() {
		return IJavaElement.ANNOTATION;
	}

	public String getHandleIdentifier() {
		return null;
	}

	public IJavaModel getJavaModel() {
		return method.getJavaModel();
	}

	public IJavaProject getJavaProject() {
		return method.getJavaProject();
	}

	public IOpenable getOpenable() {
		return null;
	}

	public IJavaElement getParent() {
		return method;
	}

	public IPath getPath() {
		return method.getPath();
	}

	public IJavaElement getPrimaryElement() {
		return method;
	}

	public IResource getResource() {
		return method.getResource();
	}

	public ISchedulingRule getSchedulingRule() {
		return null;
	}

	public IResource getUnderlyingResource() throws JavaModelException {
		return method.getResource();
	}

	public boolean isReadOnly() {
		return true;
	}

	public boolean isStructureKnown() throws JavaModelException {
		return true;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return method.getAdapter(adapter);
	}

	public String getSource() throws JavaModelException {
		return source;
	}

	public ISourceRange getSourceRange() throws JavaModelException {
		return sourceRange;
	}		
	
	public class SourceRange implements ISourceRange{
		private int offset;
		private int length;

		public SourceRange(int offset, int length){
			this.offset = offset;
			this.length = length;
		}
		
		public int getLength() {
			return length;
		}

		public int getOffset() {
			return offset;
		}
		
	}

	public void addValuePair(String name, String value) {
		valuePairs.add(new StringValuePair(name, value));		
	}
}
