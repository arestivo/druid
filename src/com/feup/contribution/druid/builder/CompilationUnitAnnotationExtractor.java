package com.feup.contribution.druid.builder;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class CompilationUnitAnnotationExtractor {
	public static String[] extractAnnotations(ICompilationUnit unit, String annotationType) throws JavaModelException {
		String source = unit.getSource().replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
		
		Pattern pattern = Pattern.compile("@[a-zA-Z]*\\(.+?\\)");
		Matcher matcher = pattern.matcher(source);
		ArrayList<String> annotations = new ArrayList<String>();
		while (matcher.find()){
			String annotationSource = matcher.group();
			Pattern namePattern = Pattern.compile("@[a-zA-Z]*");
			Matcher nameMatcher = namePattern.matcher(annotationSource);
			nameMatcher.find();
			String annotationName = nameMatcher.group().substring(1);
			
			if (annotationName.equals(annotationType))
			{			
				ArrayList<String> values = getValues(annotationSource);
				for (String value : values)	annotations.add(value);
			}
		}
		return annotations.toArray(new String[annotations.size()]);
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
