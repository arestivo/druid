package com.feup.contribution.druid.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.feup.contribution.druid.DruidPlugin;

public class SharedImages {
	private static Map<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>(11);
	
	public static Image getImage(String name){
		ImageDescriptor descriptor = getImageDescriptor(name);
		Image image = (Image)imageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			imageCache.put(descriptor, image);
		}
		return image;
	}

	@SuppressWarnings("deprecation")
	public static ImageDescriptor getImageDescriptor(String name) {
		String iconPath = "icons/";
		try {
			URL installURL = DruidPlugin.getPlugin().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + name);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
}
