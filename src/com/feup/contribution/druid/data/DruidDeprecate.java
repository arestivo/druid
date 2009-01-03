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

package com.feup.contribution.druid.data;

import org.eclipse.core.resources.IResource;

public class DruidDeprecate {
	private DruidFeature deprecates; 
	private DruidFeature deprecated;
	private IResource resource;
	private int offset;
	private int length;
	
	public DruidDeprecate(DruidFeature deprecated, DruidFeature deprecates, IResource resource, int offset, int length) {
		this.deprecated = deprecated;
		this.deprecates = deprecates;
		this.setResource(resource);
		this.setOffset(offset);
		this.setLength(length);
	}

	public DruidFeature getDeprecates() {
		return deprecates;
	}

	public DruidFeature getDeprecated() {
		return deprecated;
	}
	
	@Override
	public String toString() {
		return deprecates.toString();
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public IResource getResource() {
		return resource;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}
}
