package com.feup.contribution.druid.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.JavaModelException;

public class AdviceAnnotationExtractor {
	@SuppressWarnings("restriction")
	public static IAnnotation[] extractAnnotations(AdviceElement advice) throws JavaModelException{
		String source = advice.getSource();
		Pattern pattern = Pattern.compile("@[a-zA-Z]*\\(.+?\\)");
		Matcher matcher = pattern.matcher(source);
		ArrayList<AdviceAnnotation> annotations = new ArrayList<AdviceAnnotation>();
		while (matcher.find()){
			String annotationSource = matcher.group();
			Pattern namePattern = Pattern.compile("@[a-zA-Z]*");
			Matcher nameMatcher = namePattern.matcher(annotationSource);
			nameMatcher.find();
			String annotationName = nameMatcher.group().substring(1); 
			AdviceAnnotation annotation = new AdviceAnnotation(advice, annotationName, annotationSource, source.indexOf(annotationSource));
			addValuePairs(annotation, annotationSource);
			annotations.add(annotation);
		}
		return annotations.toArray(new IAnnotation[annotations.size()]);
	}

	private static void addValuePairs(AdviceAnnotation annotation, String annotationSource) throws JavaModelException {
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
