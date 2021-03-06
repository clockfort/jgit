/*
 * Copyright (C) 2014, Andrey Loskutov <loskutov@gmx.de>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eclipse.jgit.ignore.internal;

import static org.eclipse.jgit.ignore.internal.Strings.getPathSeparator;

/**
 * Matcher built from patterns for file names (single path segments). This class
 * is immutable and thread safe.
 */
public class NameMatcher extends AbstractMatcher {

	final boolean beginning;

	final char slash;

	final String subPattern;

	NameMatcher(String pattern, Character pathSeparator, boolean dirOnly,
			boolean deleteBackslash) {
		super(pattern, dirOnly);
		slash = getPathSeparator(pathSeparator);
		if (deleteBackslash) {
			pattern = Strings.deleteBackslash(pattern);
		}
		beginning = pattern.length() == 0 ? false : pattern.charAt(0) == slash;
		if (!beginning)
			this.subPattern = pattern;
		else
			this.subPattern = pattern.substring(1);
	}

	public boolean matches(String path, boolean assumeDirectory) {
		int end = 0;
		int firstChar = 0;
		do {
			firstChar = getFirstNotSlash(path, end);
			end = getFirstSlash(path, firstChar);
			boolean match = matches(path, firstChar, end, assumeDirectory);
			if (match)
				// make sure the directory matches: either if we are done with
				// segment and there is next one, or if the directory is assumed
				return !dirOnly ? true : (end > 0 && end != path.length())
						|| assumeDirectory;
		} while (!beginning && end != path.length());
		return false;
	}

	public boolean matches(String segment, int startIncl, int endExcl,
			boolean assumeDirectory) {
		// faster local access, same as in string.indexOf()
		String s = subPattern;
		if (s.length() != (endExcl - startIncl))
			return false;
		for (int i = 0; i < s.length(); i++) {
			char c1 = s.charAt(i);
			char c2 = segment.charAt(i + startIncl);
			if (c1 != c2)
				return false;
		}
		return true;
	}

	private int getFirstNotSlash(String s, int start) {
		int slashIdx = s.indexOf(slash, start);
		return slashIdx == start ? start + 1 : start;
	}

	private int getFirstSlash(String s, int start) {
		int slashIdx = s.indexOf(slash, start);
		return slashIdx == -1 ? s.length() : slashIdx;
	}

}
