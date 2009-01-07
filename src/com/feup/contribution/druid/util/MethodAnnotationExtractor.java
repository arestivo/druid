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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class MethodAnnotationExtractor {
	@SuppressWarnings("restriction")
	public static IAnnotation[] extractAnnotations(IMethod method) throws JavaModelException{
		String source = method.getSource();
		Pattern pattern = Pattern.compile("@[a-zA-Z]*\\(.+?\\)");
		Matcher matcher = pattern.matcher(source);
		ArrayList<MethodAnnotation> annotations = new ArrayList<MethodAnnotation>();
		while (matcher.find()){
			String annotationSource = matcher.group();
			Pattern namePattern = Pattern.compile("@[a-zA-Z]*");
			Matcher nameMatcher = namePattern.matcher(annotationSource);
			nameMatcher.find();
			String annotationName = nameMatcher.group().substring(1); 
			MethodAnnotation annotation = new MethodAnnotation(method, annotationName, annotationSource, source.indexOf(annotationSource));
			addValuePairs(annotation, annotationSource);
			annotations.add(annotation);
		}
		return annotations.toArray(new IAnnotation[annotations.size()]);
	}

	private static void addValuePairs(MethodAnnotation annotation, String annotationSource) throws JavaModelException {
		Pattern pattern = Pattern.compile("([a-zA-Z]+)=\"(.*?)\"");
		Matcher matcher = pattern.matcher(annotationSource);
		while (matcher.find()){
			String name = matcher.group(1);
			String value = matcher.group(2);
			annotation.addValuePair(name, value);
		}
		if (annotation.getMemberValuePairs().length == 0) {
			Pattern patternNoName = Pattern.compile("\"(.*?)\"");
			Matcher matcherNoName = patternNoName.matcher(annotationSource);
			if (matcherNoName.find()){
				String value = matcherNoName.group(1);
				annotation.addValuePair("value", value);
			}
		}
	}
}
