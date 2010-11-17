package net.cftracker;

import coldfusion.runtime.TemplateClassLoader;

public class TemplateCache {

	public TemplateCache() {
		
	}

	public static double getHitRatio() {
		return TemplateClassLoader.getClassCacheHitRatio();
	}
}
