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

package com.feup.contribution.druid.builder;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import com.feup.contribution.druid.DruidPlugin;

public class MethodAnnotationExtractor {
	public static IAnnotation[] extractAnnotations(IMethod method) throws JavaModelException{
		String source = method.getSource().replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
		
		Pattern pattern = Pattern.compile("@[a-zA-Z]*\\(.+?\\)");
		Matcher matcher = pattern.matcher(source);
		ArrayList<MethodAnnotation> annotations = new ArrayList<MethodAnnotation>();
		while (matcher.find()){
			String annotationSource = matcher.group();
			Pattern namePattern = Pattern.compile("@[a-zA-Z]*");
			Matcher nameMatcher = namePattern.matcher(annotationSource);
			nameMatcher.find();
			String annotationName = nameMatcher.group().substring(1);
			
			ArrayList<String> values = getValues(annotationSource);
			for (String value : values){
				MethodAnnotation annotation = new MethodAnnotation(method, annotationName, annotationSource, source.indexOf(annotationSource));
				annotation.addValuePair("value", value);
				annotations.add(annotation);
			}
		}
		return annotations.toArray(new IAnnotation[annotations.size()]);
	}

	private static ArrayList<String> getValues(String annotationSource) throws JavaModelException {
		ArrayList<String> values = new ArrayList<String>();
		Pattern patternNoName = Pattern.compile("\"(.*?)\"");
		Matcher matcherNoName = patternNoName.matcher(annotationSource);
		if (matcherNoName.find()){
			String value = matcherNoName.group(1);
			StringTokenizer st = new StringTokenizer(value, ",");
			while (st.hasMoreTokens()){
				String element = st.nextToken();
				element = element.trim();
				values.add(element);
			}
		}
		return values;
	}
}
